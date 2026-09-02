package com.sanket_satpute_20.ironmind.sleeplock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.integrity.DailyIntegrityEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class SleepLockAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prefs = PrefManager.getInstance(context)
        if (!prefs.sleepLockEnabled && action != ACTION_END) return
        val policyManager = SleepLockPolicyManager(context)

        createChannels(context)

        when (action) {
            ACTION_WARNING_30 -> {
                prefs.sleepLockLastWarningStage = SleepLockStage.WARNING_30.name
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_WARNING", "", "30_MINUTES", "SLEEP_LOCK")
                notify(
                    context = context,
                    channelId = CHANNEL_WARNING,
                    notificationId = 6610,
                    title = "Sleep Lock begins in 30 minutes",
                    body = "Finish what you need now. Normal notifications will go quiet except calls.",
                    priority = NotificationCompat.PRIORITY_HIGH
                )
            }
            ACTION_WARNING_20 -> {
                val manager = SleepLockManager(context)
                if (manager.syncPersistentState()) {
                    context.sendBroadcast(BedtimeIncomingActivity.finishIntent(context))
                    launchSleepLockSurface(context)
                    return
                }
                prefs.sleepLockLastWarningStage = SleepLockStage.WARNING_20.name
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_WARNING", "", "20_MINUTES", "SLEEP_LOCK")
                context.startActivity(BedtimeIncomingActivity.createIntent(context))
            }
            ACTION_WARNING_15 -> {
                prefs.sleepLockLastWarningStage = SleepLockStage.WARNING_15.name
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_WARNING", "", "15_MINUTES", "SLEEP_LOCK")
                notify(
                    context = context,
                    channelId = CHANNEL_WARNING,
                    notificationId = 6611,
                    title = "Sleep Lock begins in 15 minutes",
                    body = "Wrap up now. The phone is about to enter its sleep window.",
                    priority = NotificationCompat.PRIORITY_HIGH
                )
            }
            ACTION_WARNING_FINAL -> {
                prefs.sleepLockLastWarningStage = SleepLockStage.WARNING_FINAL.name
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_WARNING", "", "FINAL", "SLEEP_LOCK")
                notify(
                    context = context,
                    channelId = CHANNEL_WARNING,
                    notificationId = 6612,
                    title = "Sleep Lock is almost here",
                    body = "Final warning. Calls stay alive, but the rest of the phone is about to stand down.",
                    priority = NotificationCompat.PRIORITY_MAX
                )
            }
            ACTION_START -> {
                val manager = SleepLockManager(context)
                if (manager.isEmergencyOverrideActive()) {
                    notify(
                        context = context,
                        channelId = CHANNEL_STATUS,
                        notificationId = 6614,
                        title = "Sleep Lock emergency exit in effect",
                        body = "Night screen is suspended until wake time for this window.",
                        priority = NotificationCompat.PRIORITY_DEFAULT
                    )
                    SleepLockAlarmScheduler.schedule(context)
                    return
                }
                manager.syncPersistentState()
                policyManager.activateNightSilence()
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_ACTIVE", false, true, "SLEEP_LOCK")
                CoroutineScope(Dispatchers.IO).launch {
                    DailyIntegrityEngine(context).finalizeDay(LocalDate.now(), "SLEEP_LOCK")
                }
                context.sendBroadcast(BedtimeIncomingActivity.finishIntent(context))
                launchSleepLockSurface(context)
                notify(
                    context = context,
                    channelId = CHANNEL_STATUS,
                    notificationId = 6613,
                    title = "Sleep Lock is active",
                    body = "Night protection is live until your wake time.",
                    priority = NotificationCompat.PRIORITY_HIGH
                )
            }
            ACTION_END -> {
                val wasActive = prefs.sleepLockActive
                prefs.sleepLockActive = false
                prefs.sleepLockStartedAt = 0L
                prefs.sleepLockEndsAt = 0L
                prefs.sleepLockEmergencyOverrideUntil = 0L
                prefs.sleepLockLastWarningStage = SleepLockStage.RELEASING.name
                SleepLockSoundController(context).stop()
                policyManager.restoreNotificationPolicy()
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_ACTIVE", wasActive, false, "SLEEP_LOCK")
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(6613)
                manager.cancel(6614)
                context.sendBroadcast(SleepLockActivity.finishIntent(context))
                notify(
                    context = context,
                    channelId = CHANNEL_STATUS,
                    notificationId = 6614,
                    title = "Sleep Lock has ended",
                    body = "Morning access is restored.",
                    priority = NotificationCompat.PRIORITY_DEFAULT
                )
            }
            ACTION_EMERGENCY_EXIT -> {
                val manager = SleepLockManager(context)
                if (!manager.useEmergencyExit()) {
                    notify(
                        context = context,
                        channelId = CHANNEL_STATUS,
                        notificationId = 6614,
                        title = "No Sleep Lock exits remain",
                        body = "The monthly emergency-exit allowance for night protection has been used up.",
                        priority = NotificationCompat.PRIORITY_DEFAULT
                    )
                    SleepLockAlarmScheduler.schedule(context)
                    return
                }
                val overrideUntil = manager.currentWindowEndMillis() ?: prefs.sleepLockEndsAt
                prefs.sleepLockBypassUsedAt = System.currentTimeMillis()
                prefs.sleepLockEmergencyOverrideUntil = overrideUntil
                val wasActive = prefs.sleepLockActive
                prefs.sleepLockActive = false
                prefs.sleepLockStartedAt = 0L
                prefs.sleepLockEndsAt = 0L
                prefs.sleepLockLastWarningStage = SleepLockStage.RELEASING.name
                SleepLockSoundController(context).stop()
                policyManager.restoreNotificationPolicy()
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_EMERGENCY_EXIT", false, true, "SLEEP_LOCK")
                HistoryRecorder.recordConfigChange(context, "SLEEP_LOCK_ACTIVE", wasActive, false, "SLEEP_LOCK")
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(6613)
                context.sendBroadcast(SleepLockActivity.finishIntent(context))

                val openAppIntent = PendingIntent.getActivity(
                    context,
                    6614,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
                    .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
                    .setContentTitle("Sleep Lock emergency exit used")
                    .setContentText("Night screen closed for the rest of this sleep window.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentIntent(openAppIntent)
                    .build()

                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    notificationManager.notify(6614, notification)
                }
            }
        }

        SleepLockAlarmScheduler.schedule(context)
    }

    private fun notify(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        body: String,
        priority: Int
    ) {
        val openAppIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(openAppIntent)
            .setAutoCancel(channelId != CHANNEL_STATUS)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }
    }

    private fun launchSleepLockSurface(context: Context) {
        runCatching {
            context.startActivity(SleepLockActivity.createIntent(context))
        }

        val activityIntent = SleepLockActivity.createIntent(context)
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            6615,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setContentTitle("Sleep Lock is active")
            .setContentText("Night protection is live until your wake time.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(6613, notification)
        }
    }

    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_WARNING,
                    "Sleep Lock Warnings",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_STATUS,
                    "Sleep Lock Status",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    companion object {
        const val ACTION_WARNING_30 = "com.sanket_satpute_20.ironmind.sleeplock.WARNING_30"
        const val ACTION_WARNING_20 = "com.sanket_satpute_20.ironmind.sleeplock.WARNING_20"
        const val ACTION_WARNING_15 = "com.sanket_satpute_20.ironmind.sleeplock.WARNING_15"
        const val ACTION_WARNING_FINAL = "com.sanket_satpute_20.ironmind.sleeplock.WARNING_FINAL"
        const val ACTION_START = "com.sanket_satpute_20.ironmind.sleeplock.START"
        const val ACTION_END = "com.sanket_satpute_20.ironmind.sleeplock.END"
        const val ACTION_EMERGENCY_EXIT = "com.sanket_satpute_20.ironmind.sleeplock.EMERGENCY_EXIT"

        private const val CHANNEL_WARNING = "sleep_lock_warning"
        private const val CHANNEL_STATUS = "sleep_lock_status"

        fun createIntent(context: Context, action: String): Intent {
            return Intent(context, SleepLockAlarmReceiver::class.java).setAction(action)
        }

        fun dispatchNow(context: Context, action: String) {
            context.sendBroadcast(createIntent(context, action))
        }

        fun showActiveSurface(context: Context) {
            val receiver = SleepLockAlarmReceiver()
            receiver.createChannels(context)
            receiver.launchSleepLockSurface(context)
        }
    }
}
