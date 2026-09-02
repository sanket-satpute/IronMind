package com.sanket_satpute_20.ironmind.sleeplock

import android.app.NotificationManager
import android.content.Context
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager

class SleepLockPolicyManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val prefs = PrefManager.getInstance(appContext)
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun canSilenceNotifications(): Boolean {
        return prefs.sleepLockSilenceNotifications && notificationManager.isNotificationPolicyAccessGranted
    }

    fun activateNightSilence() {
        if (!canSilenceNotifications()) {
            prefs.sleepLockSilenceActive = false
            return
        }
        if (prefs.sleepLockSilenceActive) return

        prefs.sleepLockPreviousInterruptionFilter = notificationManager.currentInterruptionFilter
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        prefs.sleepLockSilenceActive = true
        HistoryRecorder.recordConfigChange(
            appContext,
            "SLEEP_LOCK_DND",
            false,
            true,
            "SLEEP_LOCK_POLICY"
        )
    }

    fun restoreNotificationPolicy() {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            prefs.sleepLockSilenceActive = false
            return
        }
        if (!prefs.sleepLockSilenceActive) return

        notificationManager.setInterruptionFilter(prefs.sleepLockPreviousInterruptionFilter)
        prefs.sleepLockSilenceActive = false
        HistoryRecorder.recordConfigChange(
            appContext,
            "SLEEP_LOCK_DND",
            true,
            false,
            "SLEEP_LOCK_POLICY"
        )
    }
}
