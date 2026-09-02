package com.sanket_satpute_20.ironmind.psychology

import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.utils.TimeUtils
import java.time.temporal.ChronoUnit
import kotlin.math.sqrt

object IdentityLevelEngine {

    /**
     * Calculates XP with failsafes, complexity rewards, and Rank-Based Multipliers.
     */
    fun calculateXp(task: Task, currentStreak: Int): Int {
        // Duration Failsafe
        var effectiveDuration = task.durationMinutes
        if (effectiveDuration <= 0) {
            val start = TimeUtils.parseTimeSafe(task.startTime)
            val end = TimeUtils.parseTimeSafe(task.endTime)
            effectiveDuration = ChronoUnit.MINUTES.between(start, end).toInt()
        }
        if (effectiveDuration <= 0) effectiveDuration = 30

        // Focus Multiplier
        val focusMultiplier = when (task.focusScore) {
            5 -> 2.5f 
            4 -> 1.8f
            3 -> 1.0f
            2 -> 0.4f
            1 -> 0.1f
            else -> 1.0f
        }
        
        // --- ELITE FEATURE: Rank-Based Multiplier ---
        val rankMultiplier = when (task.importanceRank) {
            1 -> 3.0f // CRITICAL
            2 -> 2.0f // VITAL
            else -> 1.0f // GROWTH
        }
        
        // Difficulty Bonus (Boss Mode)
        val difficultyBonus = task.difficultyMultiplier.coerceAtLeast(1.0f)
        
        // Streak bonus: 10% per day, capped at 100%
        val streakMultiplier = 1.0f + (currentStreak.coerceAtMost(10) * 0.1f)
        
        val total = (effectiveDuration * focusMultiplier * rankMultiplier * difficultyBonus * streakMultiplier).toInt()
        return total.coerceAtLeast(15) 
    }

    /**
     * Applied at the end of the day if all missions are secured.
     * Rewards users for 'Concentrated Force' (fewer, high-impact tasks).
     */
    fun calculateDailyPerfectBonus(totalTasks: Int, totalXpEarned: Long): Long {
        val densityMultiplier = if (totalTasks in 1..3) 1.2f else 1.0f
        return (totalXpEarned * densityMultiplier).toLong()
    }

    fun getLevelFromXp(totalXp: Long): Int {
        return (sqrt(totalXp.toDouble() / 100.0)).toInt().coerceAtLeast(1)
    }

    fun getXpForLevel(level: Int): Long {
        return (100L * level * level)
    }

    fun getProgressToNextLevel(totalXp: Long): Float {
        val currentLevel = getLevelFromXp(totalXp)
        val currentLevelStart = getXpForLevel(currentLevel)
        val nextLevelStart = getXpForLevel(currentLevel + 1)
        
        val progress = (totalXp - currentLevelStart).toFloat() / (nextLevelStart - currentLevelStart).toFloat()
        return progress.coerceIn(0f, 1f)
    }

    /**
     * Phase 3: Progressive Overload Configuration
     * Returns a float multiplier representing how strict the system should be.
     * Level 1 = 0.5f (Observer/Easy)
     * Level 20 = 1.0f (Standard IronMind)
     * Level 50 = 2.0f (Max Friction)
     */
    fun getGlobalFrictionMultiplier(level: Int, overrideLevel: Int = -1): Float {
        val effectiveLevel = if (overrideLevel > 0) overrideLevel else level
        // Linearly scale from 0.5f at level 1 to 2.0f at level 50+
        val base = 0.5f
        val maxBonus = 1.5f
        val progress = (effectiveLevel.coerceAtMost(50) - 1) / 49f // 0.0 to 1.0
        return base + (maxBonus * progress)
    }

    fun getIdentityTitle(userType: UserType, level: Int): String {
        return when (userType) {
            UserType.ACHIEVER -> when {
                level >= 40 -> "Apex Legend 🏆"
                level >= 25 -> "Executioner ⚔️"
                level >= 10 -> "Grinder ⚙️"
                else -> "Aspirant 🧭"
            }
            UserType.BROKEN_STRIVER -> when {
                level >= 40 -> "Healed Master 🌳"
                level >= 25 -> "Steady Builder 🔨"
                level >= 10 -> "Resilient 🦾"
                else -> "Rising 🌱"
            }
            UserType.PERFECTIONIST -> when {
                level >= 40 -> "Pure Architect 🏛️"
                level >= 25 -> "Craftsman 🛠️"
                level >= 10 -> "Streamlined ⚙️"
                else -> "Precise 🌀"
            }
            UserType.BURNED_OUT -> when {
                level >= 40 -> "Ever-Burning 🔥"
                level >= 25 -> "Recovered 🔋"
                level >= 10 -> "Steady 🕯️"
                else -> "Embers 🌬️"
            }
            UserType.IDENTITY_SEEKER -> when {
                level >= 40 -> "Found Soul ✨"
                level >= 25 -> "Self-Defined 👤"
                level >= 10 -> "Wanderer 🗺️"
                else -> "Drifter 🌫️"
            }
            UserType.SYSTEMS_PERSON -> when {
                level >= 40 -> "The Machine 🦾"
                level >= 25 -> "Optimizer ⚡"
                level >= 10 -> "Analyst 📊"
                else -> "Manual 📝"
            }
            UserType.DOOMSCROLLER -> when {
                level >= 40 -> "Untethered 🦅"
                level >= 25 -> "Focused Mind 🧠"
                level >= 10 -> "Awakened 👁️"
                else -> "Plugged In 🔌"
            }
            UserType.PROCRASTINATOR -> when {
                level >= 40 -> "Relentless Force 🌊"
                level >= 25 -> "Executor ⚡"
                level >= 10 -> "In Motion 🏃"
                else -> "Waiting ⏳"
            }
        }
    }
}
