package com.sanket_satpute_20.ironmind.sleeplock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchActivity

class SleepLockSoundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pauseInternal()
            ACTION_PLAY -> playInternal()
            ACTION_TOGGLE -> if (isPlaying) pauseInternal() else playInternal()
            ACTION_STOP -> {
                stopInternal()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> playInternal()
        }

        startForegroundCompat(buildNotification())
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopInternal()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun playInternal() {
        val prefs = PrefManager.getInstance(this)
        val controller = SleepLockSoundController(this)
        val playableIds = controller.playableSoundIds()
        val mode = SleepLockSoundMode.fromWireValue(prefs.sleepLockSoundMode)
        if (!controller.canPlay() || playableIds.isEmpty()) return
        val prepared = runCatching {
            val playbackSignature = buildPlaybackSignature(mode, playableIds)
            if (currentPlaybackSignature != playbackSignature || mediaPlayers.isEmpty()) {
                stopInternal()
                val activeIds = if (mode == SleepLockSoundMode.ROTATE) {
                    listOf(playableIds.first())
                } else {
                    playableIds
                }
                activeIds.forEachIndexed { index, soundId ->
                    val file = controller.getLocalFile(soundId) ?: return@forEachIndexed
                    if (!file.exists()) return@forEachIndexed
                    mediaPlayers += MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        setDataSource(file.absolutePath)
                        isLooping = true
                        setBlendVolume(index = index, count = activeIds.size)
                        prepare()
                    }
                }
                currentPlaybackSignature = playbackSignature
                if (mode == SleepLockSoundMode.ROTATE) {
                    currentRotateIds = playableIds
                    currentRotateIndex = 0
                    scheduleRotateAdvance()
                }
            }
            mediaPlayers.forEach { player ->
                if (!player.isPlaying) {
                    player.start()
                }
            }
        }.isSuccess

        if (!prepared) {
            stopInternal()
            stopSelf()
            return
        }
        isPlaying = true
        scheduleAutoStop()
        notifyUpdate()
        broadcastPlaybackState()
    }

    private fun pauseInternal() {
        mediaPlayers.forEach { player ->
            if (player.isPlaying) {
                player.pause()
            }
        }
        isPlaying = false
        rotateRunnable?.let(mainHandler::removeCallbacks)
        autoStopRunnable?.let(mainHandler::removeCallbacks)
        notifyUpdate()
        broadcastPlaybackState()
    }

    private fun stopInternal() {
        rotateRunnable?.let(mainHandler::removeCallbacks)
        rotateRunnable = null
        autoStopRunnable?.let(mainHandler::removeCallbacks)
        autoStopRunnable = null
        mediaPlayers.forEach { player ->
            player.runCatching {
                if (isPlaying) stop()
                release()
            }
        }
        mediaPlayers.clear()
        currentPlaybackSignature = null
        currentRotateIds = emptyList()
        currentRotateIndex = 0
        isPlaying = false
        broadcastPlaybackState()
    }

    private fun notifyUpdate() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val prefs = PrefManager.getInstance(this)
        val sound = SleepLockSoundCatalog.byId(prefs.sleepLockSelectedSound)
        val mode = SleepLockSoundMode.fromWireValue(prefs.sleepLockSoundMode)
        val playableCount = SleepLockSoundController(this).playableSoundIds().size.coerceAtLeast(1)
        val isProtectedSleepLock = !prefs.morningLaunchActive && SleepLockManager(this).syncPersistentState()
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            if (prefs.morningLaunchActive) {
                MorningLaunchActivity.createIntent(this)
            } else {
                SleepLockActivity.createIntent(this)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleAction = PendingIntent.getService(
            this,
            1,
            Intent(this, SleepLockSoundService::class.java).setAction(ACTION_TOGGLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = PendingIntent.getService(
            this,
            2,
            Intent(this, SleepLockSoundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_ironmind_mark_monochrome)
            .setContentTitle(
                when {
                    prefs.morningLaunchActive -> "Morning ambient"
                    isProtectedSleepLock -> "Sleep support active"
                    else -> "Sleep support"
                }
            )
            .setContentText(
                when {
                    isProtectedSleepLock && mode == SleepLockSoundMode.BLEND -> "$playableCount sounds are blended for Sleep Lock."
                    isProtectedSleepLock && mode == SleepLockSoundMode.ROTATE -> "$playableCount sounds rotate softly for Sleep Lock."
                    isProtectedSleepLock -> "${sound.title} is playing for Sleep Lock."
                    mode == SleepLockSoundMode.BLEND -> "$playableCount sounds ${if (isPlaying) "are blended softly" else "are paused"} • stops after 1 hour"
                    mode == SleepLockSoundMode.ROTATE -> "$playableCount sounds ${if (isPlaying) "rotate softly" else "are paused"} • stops after 1 hour"
                    else -> "${sound.title} ${if (isPlaying) "is playing softly" else "is paused"} • stops after 1 hour"
                }
            )
            .setSubText(if (prefs.morningLaunchActive) "Morning Launch" else "Sleep Support")
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(if (isProtectedSleepLock) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_SERVICE)
            .setPriority(if (isProtectedSleepLock) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(contentIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                toggleAction
            )

        if (!isProtectedSleepLock) {
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopAction
            )
        }

        return builder.build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep support",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background sleep audio support"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun scheduleAutoStop() {
        autoStopRunnable?.let(mainHandler::removeCallbacks)
        autoStopRunnable = Runnable {
            stopInternal()
            stopSelf()
        }
        mainHandler.postDelayed(autoStopRunnable!!, AUTO_STOP_MS)
    }

    private fun scheduleRotateAdvance() {
        rotateRunnable?.let(mainHandler::removeCallbacks)
        if (currentRotateIds.size <= 1) return
        rotateRunnable = Runnable {
            rotateToNextTrack()
        }
        mainHandler.postDelayed(rotateRunnable!!, ROTATE_ADVANCE_MS)
    }

    private fun rotateToNextTrack() {
        if (currentRotateIds.size <= 1) return
        val prefs = PrefManager.getInstance(this)
        currentRotateIndex = (currentRotateIndex + 1) % currentRotateIds.size
        val nextId = currentRotateIds[currentRotateIndex]
        prefs.sleepLockSelectedSound = nextId
        currentPlaybackSignature = null
        playInternal()
        notifyUpdate()
    }

    private fun broadcastPlaybackState() {
        sendBroadcast(
            Intent(ACTION_STATE_CHANGED)
                .setPackage(packageName)
                .putExtra(EXTRA_IS_PLAYING, isPlaying)
        )
    }

    companion object {
        private const val CHANNEL_ID = "sleep_lock_sound"
        private const val NOTIFICATION_ID = 6710
        private const val AUTO_STOP_MS = 60L * 60L * 1000L
        private const val ROTATE_ADVANCE_MS = 20L * 60L * 1000L
        private const val ACTION_PLAY = "com.sanket_satpute_20.ironmind.sleeplock.sound.PLAY"
        private const val ACTION_PAUSE = "com.sanket_satpute_20.ironmind.sleeplock.sound.PAUSE"
        private const val ACTION_TOGGLE = "com.sanket_satpute_20.ironmind.sleeplock.sound.TOGGLE"
        private const val ACTION_STOP = "com.sanket_satpute_20.ironmind.sleeplock.sound.STOP"

        private val mainHandler = Handler(Looper.getMainLooper())
        private val mediaPlayers = mutableListOf<MediaPlayer>()
        private var currentPlaybackSignature: String? = null
        private var autoStopRunnable: Runnable? = null
        private var rotateRunnable: Runnable? = null
        private var currentRotateIds: List<String> = emptyList()
        private var currentRotateIndex: Int = 0

        var isPlaying: Boolean = false
            private set

        const val ACTION_STATE_CHANGED = "com.sanket_satpute_20.ironmind.sleeplock.sound.STATE_CHANGED"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        fun start(context: Context) {
            val intent = Intent(context, SleepLockSoundService::class.java).setAction(ACTION_PLAY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            context.startService(Intent(context, SleepLockSoundService::class.java).setAction(ACTION_PAUSE))
        }

        fun stop(context: Context) {
            context.startService(Intent(context, SleepLockSoundService::class.java).setAction(ACTION_STOP))
        }

        private fun buildPlaybackSignature(mode: SleepLockSoundMode, soundIds: List<String>): String =
            "${mode.name}:${soundIds.sorted().joinToString(",")}"
    }

    private fun MediaPlayer.setBlendVolume(index: Int, count: Int) {
        val volume = when {
            count <= 1 -> 1f
            count == 2 -> if (index == 0) 0.6f else 0.48f
            else -> 0.34f
        }
        setVolume(volume, volume)
    }
}
