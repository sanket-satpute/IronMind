package com.sanket_satpute_20.ironmind.integrity

import android.content.Context
import com.sanket_satpute_20.ironmind.data.DailyIntegrityRecord
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.utils.AppClockProvider

class DailyIntegrityEngine(
    context: Context
) {
    companion object {
        private const val LOW_SCORE_THRESHOLD = 70
        private const val CONSISTENCY_BONUS_THRESHOLD = 85
        private const val MAX_VISIBLE_SHIELD_CRACKS = 6
    }

    private val appContext = context.applicationContext
    private val db = IronMindDatabase.getDatabase(appContext)
    private val prefs = PrefManager.getInstance(appContext)

    suspend fun finalizeDay(date: LocalDate, source: String): DailyIntegrityRecord {
        val dateKey = date.toString()
        val existing = db.dailyIntegrityRecordDao().getForDate(dateKey)
        if (existing != null) {
            prefs.lastIntegrityFinalizedDate = dateKey
            return existing
        }

        val liveTasks = db.taskDao().getTasksForDateOnce(dateKey)
            .filter { !it.isBreakSegment }
        val temptationCount = db.temptationLogDao().getCountForDate(dateKey)

        val plannedCount = liveTasks.size
        val completedCount = liveTasks.count { it.isCompleted }
        val scorePercent = if (plannedCount > 0) {
            ((completedCount * 100f) / plannedCount.toFloat()).toInt()
        } else {
            100
        }
        val mode = AdaptiveEngine.getCurrentMode(prefs)
        
        if (prefs.isRecoveryDayActive) {
            prefs.isRecoveryDayActive = false
            val record = DailyIntegrityRecord(
                date = dateKey,
                plannedCount = 0,
                completedCount = 0,
                scorePercent = 100, // No penalties applied
                finalizedAt = System.currentTimeMillis(),
                modeAtFinalization = "RECOVERY",
                strictnessEscalatedForNextDay = false,
                source = source + "_RECOVERY_DAY",
                missCount = temptationCount,
                shieldState = temptationCount.coerceAtMost(MAX_VISIBLE_SHIELD_CRACKS)
            )
            db.dailyIntegrityRecordDao().upsert(record)
            prefs.lastIntegrityFinalizedDate = dateKey
            return record
        }

        val isStruggling = isStrugglingPattern(mode, scorePercent)
        val isConsistent = scorePercent >= CONSISTENCY_BONUS_THRESHOLD

        val record = DailyIntegrityRecord(
            date = dateKey,
            plannedCount = plannedCount,
            completedCount = completedCount,
            scorePercent = scorePercent,
            finalizedAt = System.currentTimeMillis(),
            modeAtFinalization = mode.name,
            strictnessEscalatedForNextDay = false,
            source = source,
            missCount = temptationCount,
            shieldState = temptationCount.coerceAtMost(MAX_VISIBLE_SHIELD_CRACKS)
        )
        db.dailyIntegrityRecordDao().upsert(record)
        prefs.lastIntegrityFinalizedDate = dateKey

        HistoryRecorder.recordConfigChange(
            appContext,
            "DAILY_INTEGRITY_FINALIZED",
            "",
            "$dateKey:$scorePercent",
            source
        )

        // Instead of punishing struggling users, surface the pattern and suggest support
        if (isStruggling) {
            prefs.integrityStrugglingDaysInRow = (prefs.integrityStrugglingDaysInRow) + 1
            prefs.integritySuggestRecovery = true
            HistoryRecorder.recordConfigChange(
                appContext,
                "INTEGRITY_STRUGGLING_PATTERN",
                false,
                true,
                "DAILY_INTEGRITY"
            )
        } else {
            prefs.integrityStrugglingDaysInRow = 0
            prefs.integritySuggestRecovery = false
        }

        // Reward consistency: consecutive good days earn bonus emergency exit
        if (isConsistent) {
            prefs.integrityConsistentDaysInRow = (prefs.integrityConsistentDaysInRow) + 1
            if (prefs.integrityConsistentDaysInRow >= 3 && prefs.integrityConsistentDaysInRow % 3 == 0) {
                prefs.emergencyExitBudget = prefs.emergencyExitBudget + 1
                HistoryRecorder.recordConfigChange(
                    appContext,
                    "CONSISTENCY_BONUS_EXIT_EARNED",
                    prefs.emergencyExitBudget - 1,
                    prefs.emergencyExitBudget,
                    "DAILY_INTEGRITY"
                )
            }
        } else {
            prefs.integrityConsistentDaysInRow = 0
        }

        val isPerfect = scorePercent == 100
        if (isPerfect) {
            prefs.integrityPerfectDaysInRow = prefs.integrityPerfectDaysInRow + 1
            if (prefs.integrityPerfectDaysInRow >= 7 && prefs.integrityPerfectDaysInRow % 7 == 0) {
                if (prefs.streakShields < 2) {
                    prefs.streakShields = prefs.streakShields + 1
                    HistoryRecorder.recordConfigChange(
                        appContext,
                        "SHIELD_EARNED",
                        prefs.streakShields - 1,
                        prefs.streakShields,
                        "DAILY_INTEGRITY"
                    )
                }
            }
        } else {
            prefs.integrityPerfectDaysInRow = 0
        }

        return record
    }

    suspend fun finalizeYesterdayIfNeeded(source: String): DailyIntegrityRecord? {
        val yesterday = AppClockProvider.clock.today().minusDays(1)
        val dateKey = yesterday.toString()
        if (prefs.lastIntegrityFinalizedDate == dateKey) return db.dailyIntegrityRecordDao().getForDate(dateKey)
        return finalizeDay(yesterday, source)
    }

    suspend fun getRecentDays(limit: Int): List<DailyIntegrityRecord> {
        return db.dailyIntegrityRecordDao().getRecent(limit).sortedBy { it.date }
    }

    fun applyStrictnessBoostIfExpired(today: LocalDate = AppClockProvider.clock.today()) {
        if (!prefs.ironIntegrityStrictnessBoostActive) return
        if (prefs.ironIntegrityStrictnessBoostDate != today.toString()) {
            prefs.ironIntegrityStrictnessBoostActive = false
            prefs.ironIntegrityStrictnessBoostDate = ""
            prefs.ironIntegrityStrictnessBoostSeconds = 0L
            prefs.ironIntegrityStrictnessBoostWorkLockPenaltyBonus = 0
            prefs.ironIntegrityStrictnessBoostEmergencyExitReduction = 0
            prefs.ironIntegrityStrictnessBoostRequireCleanEarnedUnlock = false
            HistoryRecorder.recordConfigChange(
                appContext,
                "IRON_STRICTNESS_EXPIRED",
                true,
                false,
                "DAILY_INTEGRITY"
            )
        }
    }

    private suspend fun isStrugglingPattern(mode: AppMode, currentScore: Int): Boolean {
        if (currentScore >= LOW_SCORE_THRESHOLD) return false
        val recent = db.dailyIntegrityRecordDao().getRecent(2)
        if (recent.size < 2) return false
        return recent.all { it.scorePercent < LOW_SCORE_THRESHOLD }
    }

}
