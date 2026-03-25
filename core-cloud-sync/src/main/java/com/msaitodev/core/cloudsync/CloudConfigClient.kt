package com.msaitodev.core.cloudsync

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * リモート設定の低レベル操作を担うクライアント。
 */
interface CloudConfigClient {
    fun getLong(key: String): Long
    fun getBoolean(key: String): Boolean
    fun getString(key: String): String
    suspend fun fetchAndActivate(): Boolean
}

@Singleton
class CloudConfigClientImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : CloudConfigClient {
    init {
        // キャッシュの設定
        val configSettings = remoteConfigSettings {
            // 開発中は 0秒（常に最新を取得）、運用時は適宜（例: 3600秒 = 1時間）に設定
            // 0に設定すると、開発中の更新が即座にアプリに反映されます
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }
    override fun getLong(key: String): Long = remoteConfig.getLong(key)
    override fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)
    override fun getString(key: String): String = remoteConfig.getString(key)
    override suspend fun fetchAndActivate(): Boolean {
        return try {
            // kotlinx-coroutines-play-services の .await() を使用
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            false
        }
    }
}
