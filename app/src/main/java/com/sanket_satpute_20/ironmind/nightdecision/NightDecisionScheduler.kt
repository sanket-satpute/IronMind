package com.sanket_satpute_20.ironmind.nightdecision

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId

object NightDecisionScheduler {

    private const val REQUEST_NIGHT_DECISION = 7201

    fun schedule(context: Context) {
        val manager = NightDecisionManager(context)
        if (!manager.shouldTriggerTonight()) {
            cancel(context)
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = manager.resolveTriggerDateTime()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_NIGHT_DECISION,
            Intent(context, NightDecisionAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_NIGHT_DECISION,
            Intent(context, NightDecisionAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}
