package com.msaitodev.core.common.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "BillingManager"
private const val ERR_SEPARATOR = ": "
private const val REFRESH_THRESHOLD_MS = 5 * 60 * 1000L // 5分間は再同期しない

/**
 * 課金（SUBS/INAPP）の購買・状態同期を担うユーティリティ。
 * アプリのライフサイクルを監視し、復帰時に自動で同期を行う。
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val billingProvider: BillingProvider
) : PurchasesUpdatedListener, DefaultLifecycleObserver {

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(BillingConfig.PREFS_NAME, Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val connectionMutex = Mutex()

    private val client: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private var isConnected = false

    private val _premiumPlan = MutableStateFlow(loadPremiumPlanFromPrefs())
    val premiumPlan: StateFlow<PremiumPlan> = _premiumPlan.asStateFlow()

    private val _isPremium = MutableStateFlow(loadPremiumFromPrefs())
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        // アプリ全体のライフサイクル監視に登録
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // 起動時に初回同期を試行
        scope.launch {
            // ★リセット完了のためコメントアウト
            // debugResetLifetimePlan()
            refreshEntitlements()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // フォアグラウンド復帰時に、一定時間経過していれば同期を実行
        val lastRefresh = prefs.getLong(BillingConfig.KEY_LAST_REFRESH_EPOCH_MS, 0L)
        val now = System.currentTimeMillis()
        
        if (now - lastRefresh > REFRESH_THRESHOLD_MS) {
            scope.launch {
                Log.d(TAG, "Foreground auto-refresh starting...")
                refreshEntitlements()
            }
        }
    }

    /**
     * テスト用：購入済みの買い切りアイテムを強制的に消費（未購入状態に）する
     */
    suspend fun debugResetLifetimePlan() {
        Log.d(TAG, "debugResetLifetimePlan: リセット処理を開始します...")
        if (!isConnected && !connect()) {
            Log.e(TAG, "debugResetLifetimePlan: 接続に失敗したため中断します")
            return
        }
        
        val inAppOwned = queryOwnedPurchases(BillingClient.ProductType.INAPP)
        val target = inAppOwned.find { it.products.contains(billingProvider.productIdLifetime) }
        
        if (target != null) {
            Log.d(TAG, "debugResetLifetimePlan: ターゲット発見(${target.products})、消費を実行します...")
            
            suspendCancellableCoroutine<Unit> { cont ->
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(target.purchaseToken)
                    .build()
                
                client.consumeAsync(consumeParams) { result, _ ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "debugResetLifetimePlan: SUCCESS - 購入情報が消去されました。")
                        setPremiumForDebug(PremiumPlan.NONE)
                    } else {
                        Log.e(TAG, "debugResetLifetimePlan: FAILED - ${result.debugMessage}")
                    }
                    cont.resume(Unit)
                }
            }
        } else {
            Log.d(TAG, "debugResetLifetimePlan: リセット対象の購入が見つかりませんでした。")
        }
    }

    sealed interface PurchaseEvent {
        data class Success(val purchase: Purchase) : PurchaseEvent
        data object Canceled : PurchaseEvent
        data object AlreadyOwned : PurchaseEvent
        data class Error(val message: String) : PurchaseEvent
    }

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>(
        replay = 0, extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val purchaseEvents: SharedFlow<PurchaseEvent> = _purchaseEvents.asSharedFlow()

    suspend fun connect(): Boolean = connectionMutex.withLock {
        if (isConnected) return@withLock true
        
        suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    isConnected = false
                    Log.d(TAG, "Billing Service Disconnected")
                }

                override fun onBillingSetupFinished(result: BillingResult) {
                    isConnected = result.responseCode == BillingClient.BillingResponseCode.OK
                    if (isConnected) {
                        Log.d(TAG, "Billing Service Connected Successfully")
                    } else {
                        Log.e(TAG, "Billing Service Connection Failed: ${result.responseCode} - ${result.debugMessage}")
                    }
                    cont.resume(isConnected)
                }
            })
        }
    }

    suspend fun queryProductDetails(productId: String, productType: String): ProductDetails? {
        if (!isConnected && !connect()) return null

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                )
            ).build()

        return suspendCancellableCoroutine { cont ->
            client.queryProductDetailsAsync(params) { result, productDetailsResult ->
                val list = productDetailsResult.productDetailsList
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !list.isNullOrEmpty()) {
                    cont.resume(list.first())
                } else {
                    cont.resume(null)
                }
            }
        }
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails) {
        val flowParams = if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerToken = productDetails.subscriptionOfferDetails
                ?.firstOrNull { it.basePlanId == billingProvider.basePlanId }
                ?.offerToken
                ?: run {
                    _purchaseEvents.tryEmit(PurchaseEvent.Error(billingProvider.errorOfferNotFound))
                    return
                }
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                ).build()
        } else {
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                ).build()
        }

        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) return
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            val ackParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            client.acknowledgePurchase(ackParams) { ackResult ->
                                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                    _purchaseEvents.tryEmit(PurchaseEvent.Success(purchase))
                                    scope.launch { refreshEntitlements() }
                                } else {
                                    _purchaseEvents.tryEmit(
                                        PurchaseEvent.Error("${billingProvider.errorAcknowledgeFailed}$ERR_SEPARATOR${ackResult.responseCode}")
                                    )
                                }
                            }
                        } else {
                            _purchaseEvents.tryEmit(PurchaseEvent.Success(purchase))
                            scope.launch { refreshEntitlements() }
                        }
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        _purchaseEvents.tryEmit(
                            PurchaseEvent.Error(billingProvider.errorPending)
                        )
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Canceled)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseEvents.tryEmit(PurchaseEvent.AlreadyOwned)
                scope.launch { refreshEntitlements() }
            }
            else -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Error("${billingProvider.errorGeneral}$ERR_SEPARATOR${result.responseCode}"))
            }
        }
    }

    /**
     * Google Play から最新の権利情報を取得し、同期する。
     */
    suspend fun refreshEntitlements(): Boolean {
        Log.d(TAG, "refreshEntitlements: 同期処理を開始します...")
        if (!isConnected && !connect()) {
            Log.w(TAG, "refreshEntitlements: 接続に失敗したため中断します")
            return false
        }

        val subsOwned = queryOwnedPurchases(BillingClient.ProductType.SUBS)
        val inAppOwned = queryOwnedPurchases(BillingClient.ProductType.INAPP)

        Log.d(TAG, "refreshEntitlements: 取得件数 -> 定期購入: ${subsOwned.size}件, 買い切り: ${inAppOwned.size}件")
        
        subsOwned.forEach { p ->
            Log.d(TAG, "refreshEntitlements: 定期購入アイテム -> IDs: ${p.products}, 状態: ${p.purchaseState}, 承認済: ${p.isAcknowledged}")
        }
        inAppOwned.forEach { p ->
            Log.d(TAG, "refreshEntitlements: 買い切りアイテム -> IDs: ${p.products}, 状態: ${p.purchaseState}, 承認済: ${p.isAcknowledged}")
        }

        val hasLifetime = inAppOwned.any { p ->
            val isTargetId = p.products.contains(billingProvider.productIdLifetime)
            val isPurchased = p.purchaseState == Purchase.PurchaseState.PURCHASED
            if (isTargetId) {
                Log.d(TAG, "refreshEntitlements: 無制限プランID一致を確認. 購入状態一致: $isPurchased")
            }
            isTargetId && isPurchased
        }
        val hasMonthly = subsOwned.any { p ->
            val isTargetId = p.products.contains(billingProvider.productIdMonthly)
            val isPurchased = p.purchaseState == Purchase.PurchaseState.PURCHASED
            if (isTargetId) {
                Log.d(TAG, "refreshEntitlements: 月額プランID一致を確認. 購入状態一致: $isPurchased")
            }
            isTargetId && isPurchased
        }

        val plan = when {
            hasLifetime -> PremiumPlan.LIFETIME
            hasMonthly -> PremiumPlan.MONTHLY
            else -> PremiumPlan.NONE
        }

        Log.d(TAG, "refreshEntitlements: 最終判定プラン -> $plan")

        savePremiumPlanToPrefs(plan)
        _premiumPlan.value = plan
        _isPremium.value = plan != PremiumPlan.NONE
        
        return plan != PremiumPlan.NONE
    }

    private suspend fun queryOwnedPurchases(type: String): List<Purchase> = suspendCancellableCoroutine { cont ->
        val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                cont.resume(purchases)
            } else {
                Log.e(TAG, "queryOwnedPurchases error: ${result.responseCode} - ${result.debugMessage}")
                cont.resume(emptyList())
            }
        }
    }

    fun setPremiumForDebug(plan: PremiumPlan) {
        Log.d(TAG, "setPremiumForDebug: $plan に強制設定します")
        savePremiumPlanToPrefs(plan)
        _premiumPlan.value = plan
        _isPremium.value = plan != PremiumPlan.NONE
    }

    private fun loadPremiumFromPrefs(): Boolean =
        prefs.getBoolean(BillingConfig.KEY_IS_PREMIUM, false)

    private fun loadPremiumPlanFromPrefs(): PremiumPlan {
        val name = prefs.getString(BillingConfig.KEY_PREMIUM_PLAN, PremiumPlan.NONE.name)
        return try {
            PremiumPlan.valueOf(name ?: PremiumPlan.NONE.name)
        } catch (e: Exception) {
            PremiumPlan.NONE
        }
    }

    private fun savePremiumPlanToPrefs(plan: PremiumPlan) {
        prefs.edit(commit = true) {
            putString(BillingConfig.KEY_PREMIUM_PLAN, plan.name)
            putBoolean(BillingConfig.KEY_IS_PREMIUM, plan != PremiumPlan.NONE)
            putLong(BillingConfig.KEY_LAST_REFRESH_EPOCH_MS, System.currentTimeMillis())
        }
    }
}
