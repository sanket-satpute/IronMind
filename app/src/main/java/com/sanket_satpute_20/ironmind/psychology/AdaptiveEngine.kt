package com.sanket_satpute_20.ironmind.psychology

import android.content.Context
import com.sanket_satpute_20.ironmind.data.DailyIntegrityRecord
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate

data class ModeRecommendation(
    val mode: AppMode,
    val reason: String
)

object AdaptiveEngine {

    // ── Read current mode from prefs ──────────────────────────
    fun getCurrentMode(prefs: PrefManager): AppMode {
        return AppMode.valueOf(prefs.appMode)
    }

    fun getCurrentType(prefs: PrefManager): UserType {
        return UserType.valueOf(prefs.userType)
    }

    /**
     * Recommends a mode based on the user's reported energy levels.
     * High Energy (4-5) -> Iron or Build
     * Low Energy (1-2)  -> Recovery
     */
    fun recommendModeBasedOnEnergy(energyScore: Int): AppMode {
        return when (energyScore) {
            in 1..2 -> AppMode.RECOVERY
            3       -> AppMode.BUILD
            in 4..5 -> AppMode.IRON
            else    -> AppMode.BUILD
        }
    }

    /**
     * Advanced Mode Recommendation Engine (Phase 4)
     * Evaluates Energy, Historical Trends, Sleep Data, and Feedback loops to recommend the optimal mode.
     */
    fun recommendMorningMode(
        energyScore: Int,
        recentRecords: List<DailyIntegrityRecord>,
        sleepHours: Float?,
        prefs: PrefManager
    ): ModeRecommendation {
        val today = LocalDate.now().toString()
        val yesterdayRecord = recentRecords.lastOrNull()
        val yesterdayCompletionPercent = yesterdayRecord?.scorePercent ?: 100
        val yesterdayTemptations = yesterdayRecord?.shieldState ?: 0

        // 0. Feedback Loop Rejection Adjustment
        // If they rejected Iron yesterday for Build, pace them today.
        val rejectedYesterday = (prefs.lastRejectedRecommendationDate == LocalDate.now().minusDays(1).toString())
        val rejectedMode = prefs.lastRejectedRecommendationMode

        // 1. Hard Constraints (Sleep)
        if (sleepHours != null && sleepHours < 5.0f) {
            return ModeRecommendation(
                AppMode.RECOVERY,
                "You only slept %.1f hours. Pushing hard today is unsafe. Focus on rest and minimal essential tasks.".format(sleepHours)
            )
        }

        if (energyScore <= 2) {
            return ModeRecommendation(
                AppMode.RECOVERY,
                "Your energy is very low today. Taking time to recover is crucial for long-term consistency."
            )
        }

        // 2. Trend Analysis
        val last3Days = recentRecords.takeLast(3)
        val strugglingDaysCount = last3Days.count { it.scorePercent < 50 && it.shieldState >= 3 }
        
        // "Recovery after low completion + high temptation"
        if (strugglingDaysCount >= 2) {
            return ModeRecommendation(
                AppMode.RECOVERY,
                "You've had a rough few days battling distractions. Take a Recovery day to reset without pressure."
            )
        }

        // "Build after inconsistent but improving behavior"
        if (last3Days.size >= 2) {
            val dayBefore = last3Days[last3Days.size - 2].scorePercent
            val yesterday = last3Days.last().scorePercent
            if (dayBefore < 40 && yesterday in 50..80) {
                return ModeRecommendation(
                    AppMode.BUILD,
                    "Great job improving yesterday! Let's solidify this momentum in Build mode."
                )
            }
        }

        // 3. Iron Gate ("Iron only when user has handled intensity well")
        val handledIntensity = last3Days.size >= 2 && last3Days.takeLast(2).all { 
            (it.modeAtFinalization == AppMode.BUILD.name || it.modeAtFinalization == AppMode.IRON.name) && it.scorePercent >= 80 
        }

        if (energyScore >= 4 && handledIntensity) {
            if (rejectedYesterday && rejectedMode == AppMode.IRON.name) {
                return ModeRecommendation(
                    AppMode.BUILD,
                    "You opted for Build over Iron yesterday, so we are pacing you. Build mode is recommended."
                )
            }
            return ModeRecommendation(
                AppMode.IRON,
                "You are fully charged and have crushed the last few days. Time to push your limits in Iron mode."
            )
        }

        // High temptations -> Needs stricter environment
        if (yesterdayTemptations >= 5) {
            if (energyScore >= 4) { // Only if they have energy to fight
                return ModeRecommendation(
                    AppMode.IRON,
                    "You faced many distractions yesterday. Iron mode will lock out the noise so you can focus."
                )
            } else {
                return ModeRecommendation(
                    AppMode.BUILD,
                    "You struggled with distractions yesterday, and you're tired. Build mode offers a balanced shield."
                )
            }
        }

        // Low completion -> Back to basics
        if (yesterdayCompletionPercent < 50) {
            return ModeRecommendation(
                AppMode.BUILD,
                "Task completion was low yesterday. Let's stick to the fundamentals in Build mode."
            )
        }

        // Default to Build
        return ModeRecommendation(
            AppMode.BUILD,
            "Build mode offers steady, balanced protection for today's tasks."
        )
    }

    /**
     * Checks if the user needs a fresh energy check-in today.
     */


    fun needsEnergyCheckIn(prefs: PrefManager): Boolean {
        val lastCheckin = prefs.lastCheckinDate
        val today = LocalDate.now().toString()
        return lastCheckin != today
    }

    // ── Punishment duration per mode ──────────────────────────
    fun getPunishmentDurationSeconds(mode: AppMode, prefs: PrefManager? = null): Long {
        val level = if (prefs != null) IdentityLevelEngine.getLevelFromXp(prefs.totalXp) else 4
        val overrideLevel = prefs?.frictionLevelOverride ?: -1
        val frictionMultiplier = IdentityLevelEngine.getGlobalFrictionMultiplier(level, overrideLevel)
        
        val baseDuration = when (mode) {
            AppMode.IRON       -> 300L // 5 minutes base
            AppMode.BUILD      -> 60L  // 1 minute reflection
            AppMode.RECOVERY   -> 0L   // No punishment
            AppMode.EXPERIMENT -> 30L  // 30 second pause
        }
        
        val scaledDuration = (baseDuration * frictionMultiplier).toLong()
        
        val currentPrefs = prefs
        return if (mode == AppMode.IRON &&
            currentPrefs != null &&
            currentPrefs.ironIntegrityStrictnessBoostActive &&
            currentPrefs.ironIntegrityStrictnessBoostDate == LocalDate.now().toString()
        ) {
            scaledDuration + currentPrefs.ironIntegrityStrictnessBoostSeconds
        } else {
            scaledDuration
        }
    }


    // ── Should show punishment screen at all? ─────────────────
    fun shouldShowPunishmentScreen(mode: AppMode, prefs: PrefManager? = null): Boolean {
        val level = if (prefs != null) IdentityLevelEngine.getLevelFromXp(prefs.totalXp) else 4
        return when (mode) {
            AppMode.IRON       -> level >= 2 // Don't show intense punishment screen for Level 1
            AppMode.BUILD      -> false   // show reflection instead
            AppMode.RECOVERY   -> false
            AppMode.EXPERIMENT -> false
        }
    }

    // ── Should send accountability SMS? ──────────────────────
    fun shouldSendAccountabilitySMS(mode: AppMode): Boolean {
        return mode == AppMode.IRON
    }
    // ── Task difficulty settings per mode ────────────────────
    fun getMaxTaskDurationMinutes(mode: AppMode): Int {
        return when (mode) {
            AppMode.IRON       -> 120   // up to 2 hours
            AppMode.BUILD      -> 90    // up to 90 min
            AppMode.RECOVERY   -> 30    // max 30 min tasks
            AppMode.EXPERIMENT -> 90
        }
    }

    fun getDefaultTaskDurationMinutes(mode: AppMode): Int {
        return when (mode) {
            AppMode.IRON       -> 60
            AppMode.BUILD      -> 45
            AppMode.RECOVERY   -> 20    // starts small
            AppMode.EXPERIMENT -> 45
        }
    }

    fun getMaxDailyTasks(mode: AppMode): Int {
        return when (mode) {
            AppMode.IRON       -> 6
            AppMode.BUILD      -> 4
            AppMode.RECOVERY   -> 2    // max 2 per day
            AppMode.EXPERIMENT -> 5
        }
    }

    // ── Skip response type ────────────────────────────────────
    fun getSkipResponseType(mode: AppMode): SkipResponse {
        return when (mode) {
            AppMode.IRON       -> SkipResponse.PUNISHMENT
            AppMode.BUILD      -> SkipResponse.REFLECTION
            AppMode.RECOVERY   -> SkipResponse.COMPASSION
            AppMode.EXPERIMENT -> SkipResponse.DATA_COLLECTION
        }
    }

    // ── Streak display ────────────────────────────────────────
    fun shouldShowStreakCounter(mode: AppMode): Boolean {
        return mode != AppMode.EXPERIMENT
    }

    // ── Rolling rate visible in Experiment + Build ────────────
    fun shouldShowRollingRate(mode: AppMode): Boolean {
        return mode in listOf(AppMode.EXPERIMENT, AppMode.BUILD)
    }

    // ── Boss Mode eligible ────────────────────────────────────
    fun isBossModeEligible(mode: AppMode): Boolean {
        return mode == AppMode.IRON
    }

    // ── Switch mode ───────────────────────────────────────────
    fun switchMode(
        prefs: PrefManager,
        newMode: AppMode,
        isManual: Boolean = false,
        context: Context? = null,
        previousMode: String = prefs.appMode,
        source: String = if (isManual) "MANUAL_MODE_SWITCH" else "ADAPTIVE_ENGINE"
    ) {
        prefs.appMode = newMode.name
        prefs.modeOverrideActive = isManual
        if (context != null) {
            HistoryRecorder.recordConfigChange(
                context = context,
                configType = "APP_MODE",
                oldValue = previousMode,
                newValue = prefs.appMode,
                sourceScreen = source
            )
        }
        com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logModeChanged(newMode.name)

        // ── Phase 6: Recovery Mode retention analytics ─────────────────────
        val isEnteringRecovery = newMode.name == "RECOVERY"
        val isLeavingRecovery = previousMode == "RECOVERY" && !isEnteringRecovery

        if (isEnteringRecovery && prefs.recoveryModeEnteredDate.isEmpty()) {
            prefs.recoveryModeEnteredDate = java.time.LocalDate.now().toString()
            com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logRecoveryModeEntered()
        }

        if (isLeavingRecovery && prefs.recoveryModeEnteredDate.isNotEmpty()) {
            val enteredDate = runCatching {
                java.time.LocalDate.parse(prefs.recoveryModeEnteredDate)
            }.getOrNull()
            val daysInRecovery = if (enteredDate != null) {
                java.time.temporal.ChronoUnit.DAYS.between(enteredDate, java.time.LocalDate.now()).toInt()
            } else 0
            com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logRecoveryModeExited(daysInRecovery)
            prefs.recoveryModeEnteredDate = ""
        }
        // ─────────────────────────────────────────────────────────────────────
    }
}

enum class SkipResponse {
    PUNISHMENT,         // Iron Mode — lockout screen
    REFLECTION,         // Build Mode — "what happened?" question
    COMPASSION,         // Recovery Mode — gentle message only
    DATA_COLLECTION     // Experiment Mode — log the skip as data point
}

