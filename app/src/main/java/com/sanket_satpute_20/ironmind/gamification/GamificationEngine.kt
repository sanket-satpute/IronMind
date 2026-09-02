package com.sanket_satpute_20.ironmind.gamification

import android.content.Context
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class GamificationEvent {
    data class LevelUp(val newLevel: Int) : GamificationEvent()
    data class XpEarned(val amount: Int) : GamificationEvent()
    data class StreakUpdated(val newStreak: Int) : GamificationEvent()
}

class GamificationEngine private constructor(private val context: Context) {
    private val prefManager = PrefManager.getInstance(context)
    private val soundManager = SoundManager.getInstance(context)
    private val hapticsManager = HapticsManager.getInstance(context)

    private val _events = MutableSharedFlow<GamificationEvent>(extraBufferCapacity = 5)
    val events = _events.asSharedFlow()

    companion object {
        @Volatile
        private var instance: GamificationEngine? = null

        fun getInstance(context: Context): GamificationEngine {
            return instance ?: synchronized(this) {
                instance ?: GamificationEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    fun addXp(amount: Long) {
        val oldLevel = IdentityLevelEngine.getLevelFromXp(prefManager.totalXp)
        prefManager.totalXp += amount
        val newLevel = IdentityLevelEngine.getLevelFromXp(prefManager.totalXp)

        _events.tryEmit(GamificationEvent.XpEarned(amount.toInt()))

        if (newLevel > oldLevel) {
            soundManager.playLevelUpFanfare()
            hapticsManager.playCompletion()
            _events.tryEmit(GamificationEvent.LevelUp(newLevel))
        } else if (amount > 0) {
            soundManager.playXpGain()
            hapticsManager.playTick()
        }
    }

    fun penalizeXp(amount: Long) {
        prefManager.totalXp = (prefManager.totalXp - amount).coerceAtLeast(0L)
        soundManager.playTemptationBlocked()
        hapticsManager.playError()
    }

    fun incrementStreak() {
        prefManager.streakCount += 1
        _events.tryEmit(GamificationEvent.StreakUpdated(prefManager.streakCount))
        soundManager.playStreakMilestone()
        hapticsManager.playCompletion()
    }

    fun resetStreak() {
        prefManager.streakCount = 0
        _events.tryEmit(GamificationEvent.StreakUpdated(0))
        soundManager.playTemptationBlocked()
        hapticsManager.playError()
    }
}
