package com.sanket_satpute_20.ironmind.focus

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.components.HapticPattern
import com.sanket_satpute_20.ironmind.ui.components.ShieldSound

class FocusSessionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var startTimeMillis = 0L
    private var isRunning = false
    private var currentTaskName = "Task"
    private var lastCrackCount = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskName = intent?.getStringExtra("TASK_NAME") ?: currentTaskName

        if (!isRunning) {
            startTimeMillis = System.currentTimeMillis()
            isRunning = true
            currentTaskName = taskName
            startForeground(201, createNotification(taskName, "00:00", lastCrackCount))

            serviceScope.launch {
                while (isRunning) {
                    delay(1000)
                    updateNotification(currentTaskName, formatElapsedTime(), lastCrackCount)
                }
            }

            serviceScope.launch {
                val db = IronMindDatabase.getDatabase(this@FocusSessionService)
                val today = LocalDate.now().toString()
                db.temptationLogDao().getLogsForDate(today)
                    .map { it.size }
                    .distinctUntilChanged()
                    .collectLatest { count ->
                        if (count > lastCrackCount && lastCrackCount > -1) {
                            // A new temptation was caught during this session
                            com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(this@FocusSessionService).playError()
                            com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(this@FocusSessionService).playTemptationBlocked()
                        }
                        lastCrackCount = count
                        updateNotification(currentTaskName, formatElapsedTime(), lastCrackCount)
                    }
            }
        } else if (taskName != currentTaskName) {
            currentTaskName = taskName
            updateNotification(currentTaskName, formatElapsedTime(), lastCrackCount)
        }

        return START_NOT_STICKY
    }

    private fun updateNotification(taskName: String, time: String, cracks: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(201, createNotification(taskName, time, cracks))
    }

    private fun createNotification(taskName: String, time: String, cracks: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "focus_session_channel")
            .setContentTitle("Focusing: $taskName")
            .setContentText("Time: $time • Shield Cracks: ${if (cracks == -1) 0 else cracks}")
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "focus_session_channel",
                "Focus Session",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    private fun formatElapsedTime(now: Long = System.currentTimeMillis()): String {
        val elapsedSeconds = ((now - startTimeMillis) / 1000L).coerceAtLeast(0L)
        return formatTime(elapsedSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }

    companion object {
        fun start(context: Context, taskName: String) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                putExtra("TASK_NAME", taskName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java)
            context.stopService(intent)
        }
    }
}
