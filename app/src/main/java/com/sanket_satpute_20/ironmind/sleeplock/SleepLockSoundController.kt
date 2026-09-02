package com.sanket_satpute_20.ironmind.sleeplock

import android.content.Context
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class SleepLockSoundController(
    context: Context
) {
    private val appContext = context.applicationContext
    private val prefs = com.sanket_satpute_20.ironmind.data.PrefManager.getInstance(appContext)

    fun canPlay(): Boolean {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return prefs.sleepLockSoundEnabled &&
            !prefs.muteAudio &&
            audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL &&
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0 &&
            playableSoundIds().isNotEmpty()
    }

    fun isPlaying(): Boolean = SleepLockSoundService.isPlaying

    fun isDownloaded(soundId: String): Boolean = getLocalFile(soundId)?.exists() == true

    fun getLocalFile(soundId: String): File? {
        val dir = File(appContext.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "sleep_lock")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "$soundId.mp3")
    }

    fun playableSoundIds(): List<String> {
        val selected = SleepLockSoundCatalog.normalizeIds(
            prefs.sleepLockSelectedSounds,
            prefs.sleepLockSelectedSound
        )
        return when (SleepLockSoundMode.fromWireValue(prefs.sleepLockSoundMode)) {
            SleepLockSoundMode.SINGLE -> listOf(prefs.sleepLockSelectedSound)
            SleepLockSoundMode.BLEND -> selected.toList()
            SleepLockSoundMode.ROTATE -> selected.toList()
        }.filter { isDownloaded(it) }
    }

    suspend fun downloadSound(soundId: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val sound = SleepLockSoundCatalog.byId(soundId)
            val target = requireNotNull(getLocalFile(soundId))
            val temp = File(target.parentFile, "${target.name}.part")
            val connection = (URL(sound.downloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                requestMethod = "GET"
                instanceFollowRedirects = true
            }
            try {
                connection.connect()
                if (connection.responseCode !in 200..299) {
                    error("Download failed with code ${connection.responseCode}")
                }
                connection.inputStream.use { input ->
                    temp.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (target.exists()) {
                    target.delete()
                }
                check(temp.renameTo(target)) { "Failed to finalize sound download." }
                target
            } finally {
                connection.disconnect()
            }
        }.onFailure {
            val temp = getLocalFile(soundId)?.let { file -> File(file.parentFile, "${file.name}.part") }
            temp?.takeIf { it.exists() }?.delete()
        }
    }

    fun play() {
        if (!canPlay()) return
        SleepLockSoundService.start(appContext)
    }

    fun pause() {
        SleepLockSoundService.pause(appContext)
    }

    fun togglePlayback() {
        if (SleepLockSoundService.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun stop() {
        SleepLockSoundService.stop(appContext)
    }
}
