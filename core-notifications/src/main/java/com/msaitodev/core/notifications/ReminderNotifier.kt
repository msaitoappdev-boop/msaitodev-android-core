package com.msaitodev.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotifier @Inject constructor(
    private val policy: NotificationPolicy
) {
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val METADATA_DEFAULT_COLOR = "com.google.firebase.messaging.default_notification_color"
    }

    /**
     * 指定された内容で通知を表示します。
     * 内容が指定されない場合は、ポリシーのデフォルト値を使用します。
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun show(
        context: Context,
        title: String? = null,
        text: String? = null
    ) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(policy.deepLinkUri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        val contentPI = PendingIntent.getActivity(context, 0, intent, flags)

        val finalTitle = title ?: policy.defaultTitle
        val finalText = text ?: policy.defaultText

        val builder = NotificationCompat.Builder(context, policy.channelId)
            .setSmallIcon(policy.smallIconResId)
            .setContentTitle(finalTitle)
            .setContentText(finalText)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Manifest のメタデータから通知色を取得して適用
        getNotificationColorFromManifest(context)?.let { color ->
            builder.setColor(color)
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission missing at runtime
        }
    }

    /**
     * AndroidManifest.xml のメタデータから通知用の色を取得します。
     */
    private fun getNotificationColorFromManifest(context: Context): Int? {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
            }

            val colorResId = appInfo.metaData?.getInt(METADATA_DEFAULT_COLOR, 0) ?: 0
            if (colorResId != 0) {
                ContextCompat.getColor(context, colorResId)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
