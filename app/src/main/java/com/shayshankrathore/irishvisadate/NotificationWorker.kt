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

class NotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val type    = inputData.getString(KEY_TYPE)    ?: return Result.success()
        val embassy = inputData.getString(KEY_EMBASSY) ?: ""
        val vType   = inputData.getString(KEY_VISA)    ?: ""

        val (title, body) = when (type) {
            TYPE_WINDOW_OPENS -> "Decision window opens tomorrow 🗓" to
                    "Your $vType application via $embassy may receive a decision soon."
            TYPE_MIDPOINT     -> "Halfway through your decision window ⏳" to
                    "Your $vType application via $embassy is progressing. Check the decisions page."
            TYPE_OVERDUE      -> "Application may be overdue ⚠️" to
                    "Your $vType application via $embassy appears past the expected window. Tap to email the Visa Office."
            else              -> return Result.success()
        }

        createChannel()

        val tapIntent = PendingIntent.getActivity(
            applicationContext,
            type.hashCode(),
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_NOTIFICATION_TYPE, type)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            NotificationManagerCompat.from(applicationContext)
                .notify(type.hashCode(), notification)
        }

        return Result.success()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Irish Visa Tracker", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Visa decision window reminders" }
        applicationContext.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID             = "visa_tracker_channel"
        const val KEY_TYPE               = "type"
        const val KEY_EMBASSY            = "embassy"
        const val KEY_VISA               = "visaType"
        const val TYPE_WINDOW_OPENS      = "WINDOW_OPENS"
        const val TYPE_MIDPOINT          = "MIDPOINT"
        const val TYPE_OVERDUE           = "OVERDUE"
        const val WORK_TAG               = "visa_notifications"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    }
}
