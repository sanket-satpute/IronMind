package com.sanket_satpute_20.ironmind.crucible

import com.sanket_satpute_20.ironmind.data.PrefManager

data class CrucibleSession(
    val durationMinutes: Int,       // how long the session is
    val taskName: String,           // what the user is focusing on
    val penaltyType: CruciblePenalty
)

enum class CruciblePenalty {
    STREAK_RESET,           // resets current streak to zero
    SKIP_COUNT,             // counts as 2 skips
    PUNISHMENT_DOUBLE       // double length punishment screen
}

enum class CrucibleResult {
    NONE, COMPLETED, FAILED
}

object CruciblePenaltyEngine {
    fun applyPenalty(context: android.content.Context, prefs: PrefManager, penalty: CruciblePenalty) {
        when (penalty) {
            CruciblePenalty.STREAK_RESET -> {
                if (prefs.streakShields > 0) {
                    prefs.streakShields = prefs.streakShields - 1
                } else if (prefs.isStreakInCriticalState) {
                    com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(context).resetStreak()
                    prefs.isStreakInCriticalState = false
                } else {
                    prefs.isStreakInCriticalState = true
                    prefs.streakCriticalTimestamp = System.currentTimeMillis()
                }
            }
            CruciblePenalty.SKIP_COUNT -> {
                prefs.totalSkipped += 2
            }
            CruciblePenalty.PUNISHMENT_DOUBLE -> {
                prefs.bossModeActive = true
            }
        }
    }
}
