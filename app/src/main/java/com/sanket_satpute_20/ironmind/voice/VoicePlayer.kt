package com.sanket_satpute_20.ironmind.voice

import android.media.MediaPlayer
import java.io.File

class VoicePlayer {

    private var player: MediaPlayer? = null
    var isPlaying = false
        private set

    fun play(filePath: String, onComplete: () -> Unit) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) {
            onComplete()
            return
        }

        player = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
            setOnCompletionListener {
                this@VoicePlayer.isPlaying = false
                onComplete()
            }
        }
        isPlaying = true
    }

    fun stopPlayback() {
        runCatching {
            player?.apply {
                if (isPlaying) stop()
                release()
            }
        }
        player = null
        isPlaying = false
    }
}