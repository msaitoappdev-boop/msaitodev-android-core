package com.msaitodev.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
class InterstitialHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnits: AdUnits,
    private val repository: InterstitialAdRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var ad: InterstitialAd? = null
    private var isSideloading = false
    private var retryAttempt = 0

    private val _isLoaded = MutableStateFlow(false)

    /**
     * 広告がロード済みで、表示可能な状態かどうかを監視するための StateFlow。
     */
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    companion object {
        /** インタースティシャル広告の1日あたりの表示上限数 */
        const val MAX_DAILY_QUOTA = 1
        private const val MAX_RETRY_DELAY_MS = 30000L // リトライ間隔の上限（30秒）
        private const val LOAD_WAIT_TIMEOUT_MS = 3000L // tryShow でロードを待機する最大時間（リワードより短めに設定）
    }

    /**
     * 本日さらにインタースティシャル広告を表示できるかどうか（カウントベース）。
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
        val unitId = adUnits.interstitialUnitA
        InterstitialAd.load(context, unitId, req, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                ad = p0
                isSideloading = false
                _isLoaded.value = true
                retryAttempt = 0
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                ad = null
                isSideloading = false
                _isLoaded.value = false

                // 失敗時はリトライ
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
     * インタースティシャル広告の表示を試みる。
     * 広告が未ロードの場合は短時間待機する。
     * @param isPremium プレミアムユーザーかどうか。true の場合は表示しない。
     */
    suspend fun tryShow(
        activity: Activity,
        isPremium: Boolean,
        enabled: Boolean = true
    ): Boolean {
        if (isPremium) return false
        if (!ConsentManager.canRequestAds(activity)) return false
        if (!enabled) return false

        // SDK 初期化を確認
        AdsSdk.initIfNeeded(activity)

        // 本日の表示回数をチェック
        val shownCount = repository.countDaily.first()
        if (shownCount >= MAX_DAILY_QUOTA) return false

        // 広告がない場合はロードを開始し、短時間待機する
        if (ad == null) {
            preload()
            withTimeoutOrNull(LOAD_WAIT_TIMEOUT_MS) {
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
                    preload()
                    if (cont.isActive) cont.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    ad = null
                    _isLoaded.value = false
                    preload()
                    if (cont.isActive) cont.resume(false)
                }

                override fun onAdShowedFullScreenContent() {
                    scope.launch {
                        repository.incrementCount()
                    }
                }
            }
            try {
                currentAd.show(activity)
            } catch (t: Throwable) {
                ad = null
                _isLoaded.value = false
                preload()
                if (cont.isActive) cont.resume(false)
            }
        }
    }
}
