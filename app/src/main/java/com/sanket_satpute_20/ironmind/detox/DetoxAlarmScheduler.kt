package com.sanket_satpute_20.ironmind.detox

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.util.Calendar

object DetoxAlarmScheduler {

    fun schedule(context: Context, startHour: Int, endHour: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Start detox alarm
        val startIntent = PendingIntent.getBroadcast(
            context, 500,
            Intent(context, DetoxReceiver::class.java).apply {
                putExtra("DETOX_ACTION", "START")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            getNextAlarmTime(startHour),
            AlarmManager.INTERVAL_DAY,
            startIntent
        )

        // End detox alarm
        val endIntent = PendingIntent.getBroadcast(
            context, 501,
            Intent(context, DetoxReceiver::class.java).apply {
                putExtra("DETOX_ACTION", "END")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            getNextAlarmTime(endHour),
            AlarmManager.INTERVAL_DAY,
            endIntent
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(500, 501).forEach { reqCode ->
            val pi = PendingIntent.getBroadcast(
                context, reqCode,
                Intent(context, DetoxReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }
    }

    // Also call this from the Accessibility Service to check if detox is active
    fun isDetoxActive(context: Context): Boolean {
        val prefs = PrefManager.getInstance(context)
        if (!prefs.detoxEnabled) return false
        return prefs.detoxCurrentlyActive
    }

    private fun getNextAlarmTime(hour: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }
}