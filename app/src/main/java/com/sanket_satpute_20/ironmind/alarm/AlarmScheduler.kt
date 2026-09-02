package com.sanket_satpute_20.ironmind.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.util.Log
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object AlarmScheduler {

    /**
     * Schedules dual events for every task:
     * 1. T-10 Mins: A psychological "Priming" notification.
     * 2. T-0 Mins: A full-screen "Momentum Guard" desk.
     */
    fun scheduleTaskAlarms(context: Context, task: Task) {
        cancelTaskAlarms(context, task.id, task.name)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val taskDate = parseDateResilient(task.date) ?: LocalDate.now()
        val taskStart = parseTimeResilient(task.startTime)?.let { taskDate.atTime(it) } ?: return
        val primingReminder = taskStart.minusMinutes(10)

        // 1. T-10 Minute Priming Notification
        scheduleAlarm(
            context, alarmManager,
            triggerAt = primingReminder,
            taskName = task.name,
            taskId = task.id,
            isActive = false,
            triggerOverlay = false,
            requestCode = task.id * 10
        )

        // 2. T-0 Minute Work Start Overlay
        scheduleAlarm(
            context, alarmManager,
            triggerAt = taskStart,
            taskName = task.name,
            taskId = task.id,
            isActive = true,
            triggerOverlay = true,
            requestCode = task.id * 10 + 1
        )
        
        Log.d("AlarmScheduler", "Alarms set for ${task.name} at T-10 and T-0")
    }

    fun cancelTaskAlarms(context: Context, taskId: Int, taskName: String? = null) {
        if (taskId != -1) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            listOf(taskId * 10, taskId * 10 + 1).forEach { requestCode ->
                val pending = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    Intent(context, AlarmReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pending?.let { alarmManager.cancel(it) }
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        taskName?.let {
            notificationManager.cancel(it.hashCode())
            notificationManager.cancel(it.hashCode() + 1000)
        }
    }

    /**
     * Schedules the evening reconciliation alarm based on user's bedtime.
     * Fires 1 hour before bedtime.
     */
    fun scheduleBedtimeReaper(context: Context) {
        val prefs = PrefManager.getInstance(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val bedTime = parseTimeResilient(prefs.bedTime) ?: LocalTime.of(22, 0)
        val reaperTime = bedTime.minusHours(1)
        
        var triggerAt = LocalDate.now().atTime(reaperTime)
        if (triggerAt.isBefore(LocalDateTime.now())) {
            triggerAt = triggerAt.plusDays(1)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_IS_BEDTIME_REAPER", true)
        }

        val pending = PendingIntent.getBroadcast(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pending
        )
        Log.d("AlarmScheduler", "Bedtime Reaper scheduled for $triggerAt")
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerAt: LocalDateTime,
        taskName: String,
        taskId: Int,
        isActive: Boolean,
        triggerOverlay: Boolean,
        requestCode: Int
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_NAME, taskName)
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_IS_ACTIVE, isActive)
            putExtra(AlarmReceiver.EXTRA_TRIGGER_OVERLAY, triggerOverlay)
        }

        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMillis = triggerAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerMillis > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pending
            )
        }
    }

    private fun parseTimeResilient(timeStr: String): LocalTime? {
        val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
        for (f in formats) {
            try {
                return LocalTime.parse(timeStr.trim().uppercase(), DateTimeFormatter.ofPattern(f, Locale.US))
            } catch (e: Exception) { continue }
        }
        return null
    }

    private fun parseDateResilient(dateStr: String): LocalDate? {
        return runCatching { LocalDate.parse(dateStr.trim()) }.getOrNull()
    }
}
