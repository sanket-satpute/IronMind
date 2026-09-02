package com.sanket_satpute_20.ironmind.gamification

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.sanket_satpute_20.ironmind.data.PrefManager

class HapticsManager(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val pref = PrefManager.getInstance(context)

    private val isEnabled: Boolean
        get() = !pref.reducedHaptics

    companion object {
        @Volatile
        private var INSTANCE: HapticsManager? = null

        fun getInstance(context: Context): HapticsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HapticsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * VIRTUAL_KEY pattern for generic success actions
     */
    fun playSuccess() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(30)
        }
    }

    /**
     * LONG_PRESS heavy for Error/Block/Deny
     */
    fun playError() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(60)
        }
    }

    /**
     * Triple burst pattern for Completions
     */
    fun playCompletion() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 50, 30, 50, 40)
            val amplitudes = intArrayOf(0, 100, 0, 180, 0, 255)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 30, 50, 30, 50, 40), -1)
        }
    }

    /**
     * Light tick pattern for XP/Timer tick
     */
    fun playTick() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }
}
