package com.msaitodev.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.msaitodev.core.common.config.AdUnits
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class RewardedHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnits: AdUnits,
    private val repository: RewardedAdRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var ad: RewardedAd? = null
    private var isSideloading = false
    private var retryAttempt = 0

    private val _isLoaded = MutableStateFlow(false)

    /**
     * 広告がロード済みで、表示可能な状態かどうかを監視するための StateFlow。
     */
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    companion object {
        /** リワード広告の1日あたりの視聴上限数 */
        const val MAX_DAILY_QUOTA = 1
        private const val MAX_RETRY_DELAY_MS = 30000L // リトライ間隔の上限（30秒）
        private const val LOAD_WAIT_TIMEOUT_MS = 5000L // tryShow でロードを待機する最大時間
    }

    /**
     * 本日さらにリワード広告を視聴して報酬を獲得できるかどうか（カウントベース）。
     */
    val canShowToday: Flow<Boolean> = repository.countDaily.map { it < MAX_DAILY_QUOTA }

    /**
     * 広告をバックグラウンドで読み込む。
     * 失敗した場合は指数バックオフを用いて自動的にリトライする。
     */
    fun preload() {
        if (ad != null || isSideloading) return
        if (!ConsentManager.canRequestAds(context)) return

        // SDK 初期化を確認
        AdsSdk.initIfNeeded(context)

        isSideloading = true
        val req = AdRequest.Builder().build()
        val unitId = adUnits.rewardedUnitA

        RewardedAd.load(context, unitId, req, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(p0: RewardedAd) {
                ad = p0
                isSideloading = false
                _isLoaded.value = true
                retryAttempt = 0 // 成功したのでリトライ回数をリセット
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                ad = null
                isSideloading = false
                _isLoaded.value = false

                // 失敗時はリトライ回数に応じて間隔を広げて再試行
                retryAttempt++
                val delayTime = (Math.pow(2.0, retryAttempt.toDouble()).toLong() * 1000L)
                    .coerceAtMost(MAX_RETRY_DELAY_MS)
                
                scope.launch {
                    delay(delayTime)
                    preload()
                }
            }
        })
    }

    /**
     * リワード広告の表示を試みる。
     * 広告が未ロードの場合は最大 5 秒間ロード完了を待機する。
     * @param isPremium プレミアムユーザーかどうか。true の場合は表示しない。
     */
    suspend fun tryShow(
        activity: Activity,
        isPremium: Boolean,
        maxDailyQuota: Int = MAX_DAILY_QUOTA
    ): Boolean {
        if (isPremium) return false
        if (!ConsentManager.canRequestAds(activity)) return false

        // SDK 初期化を確認
        AdsSdk.initIfNeeded(activity)

        // 本日の獲得回数をチェック
        val rewardedCount = repository.countDaily.first()
        if (rewardedCount >= maxDailyQuota) return false

        // 広告がない場合はロードを開始し、一定時間（最大5秒）待機する
        if (ad == null) {
            preload()
            withTimeoutOrNull(LOAD_WAIT_TIMEOUT_MS) {
                // ロード完了 (true) になるのを待つ
                _isLoaded.filter { it }.first()
            }
        }

        val currentAd = ad ?: return false

        return suspendCancellableCoroutine { cont ->
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    scope.launch {
                        repository.updateLastShownTimestamp()
                    }
                    ad = null
                    _isLoaded.value = false
                    preload() // 次のためにプリロード
                    if (cont.isActive) cont.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    ad = null
                    _isLoaded.value = false
                    preload()
                    if (cont.isActive) cont.resume(false)
                }
            }

            try {
                currentAd.show(activity) {
                    // 報酬獲得確定時にカウントアップ
                    scope.launch {
                        repository.incrementCount()
                    }
                }
            } catch (t: Throwable) {
                ad = null
                _isLoaded.value = false
                preload()
                if (cont.isActive) cont.resume(false)
            }
        }
    }

    /**
     * 既存コードとの互換性のための非推奨メソッド。
     */
    @Deprecated("Use tryShow instead")
    fun show(
        activity: Activity,
        canShowToday: () -> Boolean,
        onEarned: () -> Unit,
        onFail: () -> Unit
    ) {
        if (!canShowToday() || !ConsentManager.canRequestAds(activity)) {
            onFail()
            return
        }

        AdsSdk.initIfNeeded(activity)
        val request = AdRequest.Builder().build()

        RewardedAd.load(activity, adUnits.rewardedUnitA, request, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onFail()
            }

            override fun onAdLoaded(adLoaded: RewardedAd) {
                adLoaded.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        onFail()
                    }
                }
                adLoaded.show(activity) {
                    onEarned()
                }
            }
        })
    }
}
