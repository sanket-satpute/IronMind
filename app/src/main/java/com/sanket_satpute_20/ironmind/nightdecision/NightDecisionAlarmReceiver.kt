package com.sanket_satpute_20.ironmind.nightdecision

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NightDecisionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = NightDecisionManager(context)
                if (!manager.shouldTriggerTonight()) {
                    NightDecisionScheduler.schedule(context)
                    return@launch
                }

                manager.startDecision(source = SOURCE_ALARM)
                HistoryRecorder.recordConfigChange(context, "NIGHT_DECISION_TRIGGERED", false, true, "NIGHT_DECISION")
                showNightDecisionSurface(context)
                NightDecisionScheduler.schedule(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNightDecisionSurface(context: Context) {
        createChannel(context)
        val activityIntent = NightDecisionActivity.createIntent(context)
        runCatching { context.startActivity(activityIntent) }

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            7202,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setContentTitle("Evening Checkpoint")
            .setContentText("Close out the day and set up tomorrow.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Night Decision",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    companion object {
        const val SOURCE_ALARM = "ALARM"
        private const val CHANNEL_ID = "night_decision"
        private const val NOTIFICATION_ID = 7201

        fun showActiveSurface(context: Context) {
            NightDecisionAlarmReceiver().showNightDecisionSurface(context)
        }

        fun dismissNotification(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        }
    }
}
