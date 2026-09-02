package com.sanket_satpute_20.ironmind.challenge

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
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate

class ChallengeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = ChallengeManager.reconcileChallengeState(context)
        if (!status.isActive) {
            ChallengeAlarmScheduler.cancelAlarm(context)
            return
        }

        val prefs = PrefManager.getInstance(context)
        prefs.challengeAlarmActive = true
        prefs.challengeAlarmDate = LocalDate.now().toString()

        // Launch the math challenge activity over lock screen
        showAlarmSurface(context)

        // Reschedule for tomorrow
        if (ChallengeManager.getChallengeStatus(context).isActive) {
            ChallengeAlarmScheduler.scheduleDailyAlarm(context)
        }
    }

    private fun showAlarmSurface(context: Context) {
        createChannel(context)
        val activityIntent = ChallengeAlarmActivity.createIntent(context)
        runCatching { context.startActivity(activityIntent) }

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            9991,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setContentTitle("5 AM Club wake check")
            .setContentText("Solve the wake challenge to start your day.")
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
                    "5 AM Club Alarm",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    companion object {
        private const val CHANNEL_ID = "challenge_alarm"
        private const val NOTIFICATION_ID = 9990

        fun showActiveAlarmSurface(context: Context) {
            ChallengeAlarmReceiver().showAlarmSurface(context)
        }

        fun dismissAlarmNotification(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        }
    }
}
