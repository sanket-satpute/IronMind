package com.sanket_satpute_20.ironmind.sleeplock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDateTime
import java.time.ZoneId

object SleepLockAlarmScheduler {

    private const val REQUEST_WARNING_30 = 6100
    private const val REQUEST_WARNING_20 = 6101
    private const val REQUEST_WARNING_15 = 6102
    private const val REQUEST_WARNING_FINAL = 6103
    private const val REQUEST_START = 6104
    private const val REQUEST_END = 6105

    fun schedule(context: Context) {
        val prefs = PrefManager.getInstance(context)
        if (!prefs.sleepLockEnabled) {
            cancel(context)
            return
        }

        val manager = SleepLockManager(context)
        val now = LocalDateTime.now()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        scheduleExact(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_WARNING_30,
            action = SleepLockAlarmReceiver.ACTION_WARNING_30,
            triggerAt = manager.nextOccurrence(now, SleepLockAlarmReceiver.ACTION_WARNING_30)
        )
        scheduleExact(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_WARNING_20,
            action = SleepLockAlarmReceiver.ACTION_WARNING_20,
            triggerAt = manager.nextOccurrence(now, SleepLockAlarmReceiver.ACTION_WARNING_20)
        )
        scheduleExact(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_WARNING_15,
            action = SleepLockAlarmReceiver.ACTION_WARNING_15,
            triggerAt = manager.nextOccurrence(now, SleepLockAlarmReceiver.ACTION_WARNING_15)
        )
        scheduleExact(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_WARNING_FINAL,
            action = SleepLockAlarmReceiver.ACTION_WARNING_FINAL,
            triggerAt = manager.nextOccurrence(now, SleepLockAlarmReceiver.ACTION_WARNING_FINAL)
        )
        scheduleExact(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_START,
            action = SleepLockAlarmReceiver.ACTION_START,
            triggerAt = manager.nextOccurrence(now, SleepLockAlarmReceiver.ACTION_START)
        )
        scheduleExact(
            context = context,
            alarmManager = alarmManager,
            requestCode = REQUEST_END,
            action = SleepLockAlarmReceiver.ACTION_END,
            triggerAt = manager.nextOccurrence(now, SleepLockAlarmReceiver.ACTION_END)
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(
            REQUEST_WARNING_30 to SleepLockAlarmReceiver.ACTION_WARNING_30,
            REQUEST_WARNING_20 to SleepLockAlarmReceiver.ACTION_WARNING_20,
            REQUEST_WARNING_15 to SleepLockAlarmReceiver.ACTION_WARNING_15,
            REQUEST_WARNING_FINAL to SleepLockAlarmReceiver.ACTION_WARNING_FINAL,
            REQUEST_START to SleepLockAlarmReceiver.ACTION_START,
            REQUEST_END to SleepLockAlarmReceiver.ACTION_END
        ).forEach { (requestCode, action) ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, SleepLockAlarmReceiver::class.java).setAction(action),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    private fun scheduleExact(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        action: String,
        triggerAt: LocalDateTime?
    ) {
        if (triggerAt == null) return
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, SleepLockAlarmReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingIntent
        )
    }
}
