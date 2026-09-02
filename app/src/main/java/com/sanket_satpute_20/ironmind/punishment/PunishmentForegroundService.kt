package com.sanket_satpute_20.ironmind.punishment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.psychology.AppCopy

class PunishmentForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "punishment_channel"
        const val NOTIFICATION_ID = 1001
        
        var isRunning = false
        var remainingSeconds = 0L
        var endTimeMillis = 0L

        fun start(context: Context, durationMillis: Long) {
            val intent = Intent(context, PunishmentForegroundService::class.java).apply {
                putExtra("DURATION", durationMillis)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PunishmentForegroundService::class.java))
        }
    }

    private var timer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val duration = intent?.getLongExtra("DURATION", 0L) ?: 0L
        
        val notification = buildNotification()

        // Android 14 requires the second parameter to match foregroundServiceType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (duration > 0) {
            startTimer(duration)
            isRunning = true
        } else if (!isRunning) {
            stopSelf()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Punishment Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "IronMind discipline enforcement timer"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val prefs = PrefManager.getInstance(this)
        val mode = AdaptiveEngine.getCurrentMode(prefs)

        val title = when (mode) {
            AppMode.IRON -> "Discipline Enforced"
            else -> "Reflection Time"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Stay on screen. Timer is running.")
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun startTimer(durationMillis: Long) {
        endTimeMillis = System.currentTimeMillis() + durationMillis
        timer?.cancel()
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = millisUntilFinished / 1000
            }

            override fun onFinish() {
                remainingSeconds = 0
                isRunning = false
                sendBroadcast(Intent("PUNISHMENT_ENDED"))
                stopSelf()
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        isRunning = false
        remainingSeconds = 0
        endTimeMillis = 0
    }
}
