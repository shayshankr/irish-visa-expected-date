package com.shayshankrathore.irishvisadate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL

class DecisionWatchdogWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val embassyId = inputData.getString(KEY_EMBASSY_ID) ?: return Result.success()
        val url       = inputData.getString(KEY_URL)        ?: return Result.success()
        val label     = inputData.getString(KEY_LABEL)      ?: embassyId

        val content = fetchPage(url) ?: return Result.retry()
        val hash    = content.hashCode().toString()

        val lastHash = AppPreferences.getWatchdogHash(applicationContext, embassyId)
        if (lastHash != null && hash != lastHash) {
            notifyNewDecisions(label, url)
        }
        AppPreferences.setWatchdogHash(applicationContext, embassyId, hash)
        return Result.success()
    }

    private fun fetchPage(urlString: String): String? = runCatching {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout    = 15_000
        conn.setRequestProperty("User-Agent", "IrishVisaTracker/1.7")
        conn.inputStream.bufferedReader().readText().also { conn.disconnect() }
    }.getOrNull()

    private fun notifyNewDecisions(embassyLabel: String, url: String) {
        createChannel()
        val tapIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIF_ID,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("watchdog_url", url)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New decisions published 🗓")
            .setContentText("$embassyLabel decisions page has been updated. Check if your name is listed.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$embassyLabel decisions page has been updated. Tap to open the decisions list and check if your name appears."
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notification)
        }
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Decisions Watchdog", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Alerts when the embassy decisions page changes" }
        applicationContext.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID  = "watchdog_channel"
        const val KEY_URL       = "watchdog_url"
        const val KEY_EMBASSY_ID = "watchdog_embassy_id"
        const val KEY_LABEL     = "watchdog_label"
        const val WORK_TAG      = "visa_watchdog"
        private const val NOTIF_ID = 9001
    }
}
