package com.sanket_satpute_20.ironmind.gamification

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import com.sanket_satpute_20.ironmind.data.PrefManager

/**
 * Centralized, preference-aware UI sound effects.
 *
 * Effects are synthesized rather than loaded from binary resources. This keeps the
 * APK lean while still providing distinct, deliberate cues for every gamification
 * event. Every scheduled note independently checks the current mute and ringer
 * state, so an in-progress fanfare stops immediately if the user silences audio.
 */
class SoundManager(private val context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val preferences = PrefManager.getInstance(appContext)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var toneGenerator: ToneGenerator? = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
    private var isEnabled: Boolean = true
    private var sequenceToken = 0L

    private data class ToneStep(
        val tone: Int,
        val durationMillis: Int,
        val delayMillis: Long
    )

    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * Satisfying chime/ding
     */
    fun playTaskComplete() {
        playPattern(
            ToneStep(ToneGenerator.TONE_PROP_ACK, 85, 0),
            ToneStep(ToneGenerator.TONE_PROP_BEEP2, 130, 105)
        )
    }

    /**
     * Epic fanfare
     */
    fun playStreakMilestone() {
        playPattern(
            ToneStep(ToneGenerator.TONE_PROP_BEEP2, 100, 0),
            ToneStep(ToneGenerator.TONE_PROP_ACK, 130, 120),
            ToneStep(ToneGenerator.TONE_SUP_CONFIRM, 280, 280)
        )
    }

    /**
     * Heavy "thud" deny sound
     */
    fun playTemptationBlocked() {
        playPattern(
            ToneStep(ToneGenerator.TONE_PROP_NACK, 165, 0),
            ToneStep(ToneGenerator.TONE_PROP_NACK, 110, 185)
        )
    }

    /**
     * Coin/sparkle sound
     */
    fun playXpGain() {
        playPattern(
            ToneStep(ToneGenerator.TONE_PROP_BEEP2, 45, 0),
            ToneStep(ToneGenerator.TONE_PROP_ACK, 55, 60)
        )
    }

    /**
     * Subtle tick for Pomodoro
     */
    fun playTimerTick() {
        playPattern(ToneStep(ToneGenerator.TONE_PROP_BEEP, 18, 0))
    }

    /**
     * Triumphant announcement
     */
    fun playLevelUpFanfare() {
        playPattern(
            ToneStep(ToneGenerator.TONE_PROP_BEEP2, 105, 0),
            ToneStep(ToneGenerator.TONE_SUP_CONFIRM, 155, 125),
            ToneStep(ToneGenerator.TONE_CDMA_HIGH_L, 420, 300)
        )
    }

    fun release() {
        sequenceToken += 1
        mainHandler.removeCallbacksAndMessages(null)
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun playPattern(vararg steps: ToneStep) {
        if (!canPlay()) return
        val token = ++sequenceToken
        steps.forEach { step ->
            mainHandler.postDelayed({
                if (token == sequenceToken && canPlay()) {
                    toneGenerator?.startTone(step.tone, step.durationMillis)
                }
            }, step.delayMillis)
        }
    }

    private fun canPlay(): Boolean {
        if (!isEnabled || preferences.muteAudio) return false
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return false
        return audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) > 0
    }
}
