package com.sanket_satpute_20.ironmind.nightdecision

import android.content.Context
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NightDecisionManager(
    context: Context
) {
    companion object {
        const val EMERGENCY_LEAVE_XP_COST = 25L
    }

    private val appContext = context.applicationContext
    private val prefs = PrefManager.getInstance(appContext)
    private val taskDao = IronMindDatabase.getDatabase(appContext).taskDao()

    fun resetIfDayRolled(now: LocalDateTime = LocalDateTime.now()) {
        val targetDate = prefs.nightDecisionDate.takeIf { it.isNotBlank() } ?: return
        val today = now.toLocalDate().toString()
        if (targetDate < today) {
            prefs.clearNightDecision()
        }
    }

    fun resolveTargetDate(now: LocalDateTime = LocalDateTime.now()): LocalDate {
        return now.toLocalDate().plusDays(1)
    }

    fun resolveTriggerDateTime(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val level = com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine.getLevelFromXp(prefs.totalXp)
        val friction = com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine.getGlobalFrictionMultiplier(level, prefs.frictionLevelOverride)
        // Level 1 = 0 mins early. Level 20 = 30 mins early. Level 50 = 90 mins early.
        val minutesEarly = ((friction - 0.5f) * 60f).toLong().coerceAtLeast(0L)
        
        val triggerTime = if (prefs.sleepLockEnabled) {
            LocalTime.of(
                prefs.sleepLockBedHour.coerceIn(0, 23),
                prefs.sleepLockBedMinute.coerceIn(0, 59)
            ).minusMinutes(minutesEarly)
        } else {
            LocalTime.of(21, 0)
        }
        var trigger = now.toLocalDate().atTime(triggerTime)
        if (!trigger.isAfter(now)) {
            trigger = trigger.plusDays(1)
        }
        return trigger
    }

    fun shouldTriggerTonight(now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (!prefs.nightDecisionEnabled) return false
        resetIfDayRolled(now)
        val targetDate = resolveTargetDate(now).toString()
        val alreadyDecided = prefs.nightDecisionDate == targetDate &&
            NightDecisionStatus.fromWireValue(prefs.nightDecisionStatus) != NightDecisionStatus.UNDECIDED &&
            prefs.nightDecisionCompletedAt > 0L
        return !alreadyDecided
    }

    fun reconcileRuntimeState(now: LocalDateTime = LocalDateTime.now()): NightDecisionStatus {
        resetIfDayRolled(now)
        val today = now.toLocalDate().toString()
        if (prefs.nightDecisionActive && prefs.nightDecisionDate < today) {
            prefs.clearNightDecision()
        }
        if (prefs.nightDecisionDate == today && prefs.nightDecisionActive) {
            prefs.nightDecisionActive = false
        }
        if (
            prefs.nightDecisionActive &&
            prefs.nightDecisionDate.isBlank()
        ) {
            prefs.clearNightDecision()
        }
        return NightDecisionStatus.fromWireValue(prefs.nightDecisionStatus)
    }

    fun startDecision(source: String, now: Long = System.currentTimeMillis()) {
        val targetDate = resolveTargetDate().toString()
        prefs.nightDecisionActive = true
        prefs.nightDecisionStatus = NightDecisionStatus.UNDECIDED.name
        prefs.nightDecisionDate = targetDate
        prefs.nightDecisionReason = ""
        prefs.nightDecisionTriggeredAt = now
        prefs.nightDecisionCompletedAt = 0L
        prefs.nightDecisionXpPenaltyApplied = false
        prefs.nightDecisionPlannerCompleted = false
        prefs.nightDecisionSource = source
    }

    fun markHoliday(now: Long = System.currentTimeMillis()) {
        HistoryRecorder.recordConfigChange(
            appContext,
            "NIGHT_DECISION_HOLIDAY",
            false,
            true,
            "NIGHT_DECISION"
        )
        complete(NightDecisionStatus.HOLIDAY, now)
    }

    fun markEmergencyLeave(reason: String?, now: Long = System.currentTimeMillis()) {
        prefs.nightDecisionReason = reason.orEmpty()
        if (!prefs.nightDecisionXpPenaltyApplied) {
            com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(appContext).penalizeXp(EMERGENCY_LEAVE_XP_COST)
            prefs.nightDecisionXpPenaltyApplied = true
        }
        HistoryRecorder.recordConfigChange(
            appContext,
            "NIGHT_DECISION_EMERGENCY_LEAVE",
            false,
            true,
            reason.orEmpty().ifBlank { "XP_LOSS_$EMERGENCY_LEAVE_XP_COST" }
        )
        complete(NightDecisionStatus.EMERGENCY_LEAVE, now)
    }

    fun markPlanned(now: Long = System.currentTimeMillis()) {
        prefs.nightDecisionPlannerCompleted = true
        HistoryRecorder.recordConfigChange(
            appContext,
            "NIGHT_DECISION_PLANNED",
            false,
            true,
            "NIGHT_DECISION"
        )
        complete(NightDecisionStatus.PLANNED, now)
    }

    fun statusForDate(date: LocalDate): NightDecisionStatus {
        return if (prefs.nightDecisionDate == date.toString()) {
            NightDecisionStatus.fromWireValue(prefs.nightDecisionStatus)
        } else {
            NightDecisionStatus.UNDECIDED
        }
    }

    fun isTaskCreationAllowedForDate(date: LocalDate): Boolean {
        return when (statusForDate(date)) {
            NightDecisionStatus.HOLIDAY, NightDecisionStatus.EMERGENCY_LEAVE -> false
            else -> true
        }
    }

    suspend fun hasRemainingTasksForToday(now: LocalDateTime = LocalDateTime.now()): Boolean {
        val today = now.toLocalDate().toString()
        return taskDao.getRemainingTaskCountForDate(today) > 0
    }

    private fun complete(status: NightDecisionStatus, now: Long) {
        prefs.nightDecisionStatus = status.name
        prefs.nightDecisionCompletedAt = now
        prefs.nightDecisionActive = false
    }
}
