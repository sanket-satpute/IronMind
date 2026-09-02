package com.sanket_satpute_20.ironmind.integrity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sanket_satpute_20.ironmind.MainActivity

object OngoingShieldNotification {

    private const val CHANNEL_ID = "integrity_shield_channel"
    private const val CHANNEL_NAME = "Integrity Shield Monitor"
    const val NOTIFICATION_ID = 1001

    fun createNotification(context: Context, state: ShieldUiState): Notification {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors focus integrity throughout the day"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percent = (state.integrityLevel * 100).toInt()
        val statusText = when {
            percent >= 85 -> "Integrity Intact"
            percent >= 60 -> "Holding the Line"
            else -> "Reforge Required"
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("IronMind Shield: $percent%")
            .setContentText("${state.mode.name} Mode | $statusText")
            // Assuming there's a generic icon, using R.drawable.ic_launcher_foreground fallback
            // In a real app we'd use a specific shield icon. For now fallback to a standard system or app icon.
            .setSmallIcon(android.R.drawable.ic_secure) 
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun updateNotification(context: Context, state: ShieldUiState) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(context, state))
    }
}
