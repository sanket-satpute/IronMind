package com.sanket_satpute_20.ironmind.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.focus.WorkStartActivity
import com.sanket_satpute_20.ironmind.focus.BedtimeReaperActivity
import com.sanket_satpute_20.ironmind.psychology.NotificationPersonaEngine
import com.sanket_satpute_20.ironmind.psychology.PersonaNotifyType
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_REMINDER = "ironmind_reminder"
        const val CHANNEL_ACTIVE   = "ironmind_active"
        const val CHANNEL_REAPER   = "ironmind_reaper"
        const val EXTRA_TASK_NAME  = "task_name"
        const val EXTRA_TASK_ID    = "task_id"
        const val EXTRA_IS_ACTIVE  = "is_active" 
        const val EXTRA_TRIGGER_OVERLAY = "EXTRA_TRIGGER_OVERLAY"
        const val EXTRA_IS_BEDTIME_REAPER = "EXTRA_IS_BEDTIME_REAPER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isBedtimeReaper = intent.getBooleanExtra(EXTRA_IS_BEDTIME_REAPER, false)
        
        if (isBedtimeReaper) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    handleBedtimeReaper(context)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "Task"
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        val isActive = intent.getBooleanExtra(EXTRA_IS_ACTIVE, false)
        val triggerOverlay = intent.getBooleanExtra(EXTRA_TRIGGER_OVERLAY, false)

        val prefs = PrefManager.getInstance(context)
        val userType = AdaptiveEngine.getCurrentType(prefs)
        
        val notifyType = if (isActive) PersonaNotifyType.TASK_ACTIVE else PersonaNotifyType.STARTING_SOON
        
        val title = NotificationPersonaEngine.getTitle(userType, notifyType, taskName)
        val body = NotificationPersonaEngine.getBody(userType, notifyType, taskName)

        createChannels(context)

        // --- MOMENTUM GUARD: Launch Full-Screen Activity at T-0 ---
        if (triggerOverlay && taskId != -1) {
            context.startActivity(
                WorkStartActivity.createIntent(
                    context = context,
                    taskId = taskId,
                    taskName = taskName,
                    newTask = true
                )
            )
        }

        val openAppIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, if (isActive) CHANNEL_ACTIVE else CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(isActive)
            .setPriority(if (isActive) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppIntent)
            .setAutoCancel(!isActive)
            .build()

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = if (isActive) taskName.hashCode() + 1000 else taskName.hashCode()
        notifManager.notify(notifId, notification)
    }

    private suspend fun handleBedtimeReaper(context: Context) {
        createChannels(context)
        val pendingTasks = IronMindDatabase.getDatabase(context)
            .taskDao()
            .getPendingDeferredTasks(LocalDate.now().toString())
        if (pendingTasks.isEmpty()) {
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.cancel(9999)
            return
        }
        
        // Launch the high-stakes reconciliation screen
        val reaperIntent = BedtimeReaperActivity.createIntent(context)
        context.startActivity(reaperIntent)

        val openAppIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REAPER)
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setContentTitle("💀 INTEGRITY CHECK")
            .setContentText("You have unfinished deferred tasks. Complete them now.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(null, true) // Priority notification
            .setContentIntent(openAppIntent)
            .build()

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.notify(9999, notification)
    }

    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_REMINDER, "Task Reminders", NotificationManager.IMPORTANCE_HIGH)
            )
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ACTIVE, "Active Task", NotificationManager.IMPORTANCE_MAX)
            )
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_REAPER, "Bedtime Reconciliation", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }
}
