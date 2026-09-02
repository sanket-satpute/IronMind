package com.sanket_satpute_20.ironmind.morninglaunch

import android.content.Context
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.MorningLaunchSession
import com.sanket_satpute_20.ironmind.data.MorningLaunchStepResult
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate
import java.time.YearMonth

class MorningLaunchManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val prefs = PrefManager.getInstance(appContext)
    private val database by lazy { IronMindDatabase.getDatabase(appContext) }

    fun isEnabled(): Boolean = prefs.morningLaunchEnabled

    fun getStage(): MorningLaunchStage =
        MorningLaunchStage.fromWireValue(prefs.morningLaunchStage)

    fun getMode(): MorningLaunchMode =
        MorningLaunchMode.fromWireValue(prefs.morningLaunchMode)

    fun getSteps(): List<MorningLaunchStep> =
        MorningLaunchStep.forMode(getMode())

    fun shouldTriggerAfterAlarmDismiss(): Boolean {
        resetDailyFlagsIfNeeded()
        return prefs.challengeActive &&
            prefs.morningLaunchEnabled &&
            !prefs.morningLaunchCompletedToday &&
            !prefs.morningLaunchSkippedToday
    }

    fun syncModeFromRoutineType() {
        val target = MorningLaunchMode.fromWireValue(prefs.routineType)
        if (prefs.morningLaunchMode != target.name) {
            prefs.morningLaunchMode = target.name
        }
    }

    suspend fun beginPrompting(
        alarmDismissedAt: Long = System.currentTimeMillis(),
        source: String = "CHALLENGE_ALARM"
    ): Long {
        resetDailyFlagsIfNeeded()
        syncModeFromRoutineType()
        val today = LocalDate.now().toString()
        val now = System.currentTimeMillis()
        val sessionId = database.morningLaunchSessionDao().insert(
            MorningLaunchSession(
                date = today,
                alarmDismissedAt = alarmDismissedAt,
                mode = getMode().name,
                outcome = MorningLaunchOutcome.PENDING.name,
                source = source,
                startedAt = 0L,
                completedAt = 0L,
                currentStepIndex = 0,
                stepsCompleted = 0,
                delayCount = 0,
                totalDurationMs = 0L,
                details = "",
                lastModified = now
            )
        )
        prefs.morningLaunchActive = true
        prefs.morningLaunchStage = MorningLaunchStage.PROMPTING.name
        prefs.morningLaunchSessionId = sessionId
        prefs.morningLaunchAlarmDismissedAt = alarmDismissedAt
        prefs.morningLaunchStartedAt = 0L
        prefs.morningLaunchCurrentStepIndex = 0
        prefs.morningLaunchDelayCount = 0
        prefs.morningLaunchLastOutcome = MorningLaunchOutcome.PENDING.name
        prefs.morningLaunchCompletedToday = false
        prefs.morningLaunchSkippedToday = false
        prefs.morningLaunchDate = today
        notifyGuardReload()
        return sessionId
    }

    suspend fun markStartedImmediately(sessionId: Long = prefs.morningLaunchSessionId) {
        val session = database.morningLaunchSessionDao().getById(sessionId) ?: run {
            clearRuntimeState(keepDayOutcome = true)
            return
        }
        val now = System.currentTimeMillis()
        val outcome = if (session.delayCount > 0) {
            MorningLaunchOutcome.STARTED_AFTER_DELAY
        } else {
            MorningLaunchOutcome.STARTED_IMMEDIATELY
        }
        database.morningLaunchSessionDao().update(
            session.copy(
                outcome = outcome.name,
                startedAt = now,
                lastModified = now
            )
        )
        prefs.morningLaunchActive = true
        prefs.morningLaunchStage = MorningLaunchStage.IN_PROGRESS.name
        prefs.morningLaunchStartedAt = now
        prefs.morningLaunchLastOutcome = outcome.name
        notifyGuardReload()
    }

    suspend fun markSnoozedFiveMinutes(sessionId: Long = prefs.morningLaunchSessionId) {
        val session = database.morningLaunchSessionDao().getById(sessionId) ?: run {
            clearRuntimeState(keepDayOutcome = true)
            return
        }
        val now = System.currentTimeMillis()
        val nextDelayCount = session.delayCount + 1
        database.morningLaunchSessionDao().update(
            session.copy(
                outcome = MorningLaunchOutcome.REMIND_5.name,
                delayCount = nextDelayCount,
                lastModified = now
            )
        )
        prefs.morningLaunchActive = false
        prefs.morningLaunchStage = MorningLaunchStage.SNOOZED.name
        prefs.morningLaunchDelayCount = nextDelayCount
        prefs.morningLaunchSnoozedUntil = now + FIVE_MINUTES_MS
        prefs.morningLaunchLastOutcome = MorningLaunchOutcome.REMIND_5.name
        notifyGuardReload()
    }

    suspend fun markSkipped(sessionId: Long = prefs.morningLaunchSessionId, details: String = "") {
        val session = database.morningLaunchSessionDao().getById(sessionId) ?: run {
            clearRuntimeState(keepDayOutcome = true)
            return
        }
        val now = System.currentTimeMillis()
        database.morningLaunchSessionDao().update(
            session.copy(
                outcome = MorningLaunchOutcome.SKIPPED.name,
                completedAt = now,
                details = details,
                lastModified = now
            )
        )
        prefs.morningLaunchStage = MorningLaunchStage.SKIPPED.name
        prefs.morningLaunchSkippedToday = true
        prefs.morningLaunchLastOutcome = MorningLaunchOutcome.SKIPPED.name
        clearRuntimeState(keepDayOutcome = true)
        com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logMorningRitualOutcome("SKIPPED")
    }

    suspend fun markCompleted(
        stepsCompleted: Int,
        sessionId: Long = prefs.morningLaunchSessionId
    ) {
        val session = database.morningLaunchSessionDao().getById(sessionId) ?: run {
            clearRuntimeState(keepDayOutcome = true)
            return
        }
        val now = System.currentTimeMillis()
        val startedAt = if (session.startedAt > 0L) session.startedAt else prefs.morningLaunchStartedAt
        database.morningLaunchSessionDao().update(
            session.copy(
                outcome = MorningLaunchOutcome.COMPLETED.name,
                completedAt = now,
                stepsCompleted = stepsCompleted,
                currentStepIndex = stepsCompleted,
                totalDurationMs = if (startedAt > 0L) now - startedAt else 0L,
                lastModified = now
            )
        )
        prefs.morningLaunchStage = MorningLaunchStage.COMPLETED.name
        prefs.morningLaunchCompletedToday = true
        prefs.morningLaunchLastOutcome = MorningLaunchOutcome.COMPLETED.name
        clearRuntimeState(keepDayOutcome = true)
        com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logMorningRitualCompleted()
        com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logMorningRitualOutcome("COMPLETED")
    }

    suspend fun saveStepResult(
        stepId: String,
        stepOrder: Int,
        targetValue: Int,
        actualValue: Int,
        unit: String,
        completed: Boolean,
        skipped: Boolean,
        sessionId: Long = prefs.morningLaunchSessionId
    ) {
        if (sessionId <= 0L) return
        val now = System.currentTimeMillis()
        val existing = database.morningLaunchStepResultDao().getForSessionAndStep(sessionId, stepId)
        val result = MorningLaunchStepResult(
            id = existing?.id ?: 0L,
            sessionId = sessionId,
            mode = getMode().name,
            stepId = stepId,
            stepOrder = stepOrder,
            targetValue = targetValue,
            actualValue = actualValue,
            unit = unit,
            completed = completed,
            skipped = skipped,
            createdAt = existing?.createdAt ?: now,
            completedAt = now
        )
        if (existing == null) {
            database.morningLaunchStepResultDao().insert(result)
        } else {
            database.morningLaunchStepResultDao().update(result)
        }
    }

    suspend fun getStepResults(sessionId: Long = prefs.morningLaunchSessionId): List<MorningLaunchStepResult> {
        if (sessionId <= 0L) return emptyList()
        return database.morningLaunchStepResultDao().getForSession(sessionId)
    }

    suspend fun clearStepResults(sessionId: Long = prefs.morningLaunchSessionId) {
        if (sessionId <= 0L) return
        database.morningLaunchStepResultDao().deleteForSession(sessionId)
    }

    fun updateCurrentStep(index: Int) {
        prefs.morningLaunchCurrentStepIndex = index
    }

    fun resetDailyFlagsIfNeeded() {
        val today = LocalDate.now().toString()
        if (prefs.morningLaunchDate != today) {
            val preserveOutcome = prefs.morningLaunchCompletedToday || prefs.morningLaunchSkippedToday
            if (
                prefs.morningLaunchActive ||
                prefs.morningLaunchSessionId > 0L ||
                prefs.morningLaunchSnoozedUntil > 0L ||
                getStage() != MorningLaunchStage.IDLE
            ) {
                clearRuntimeState(keepDayOutcome = preserveOutcome)
            }
            prefs.morningLaunchDate = today
            prefs.morningLaunchCompletedToday = false
            prefs.morningLaunchSkippedToday = false
            prefs.morningLaunchDelayCount = 0
            prefs.morningLaunchLastOutcome = MorningLaunchOutcome.PENDING.name
            prefs.morningBonusArmedDate = ""
        }
        syncEmergencyExitQuota()
    }

    suspend fun reconcileRuntimeState(nowMs: Long = System.currentTimeMillis()): MorningLaunchStage {
        resetDailyFlagsIfNeeded()
        val today = LocalDate.now().toString()
        val stage = getStage()
        val sessionId = prefs.morningLaunchSessionId

        if (sessionId <= 0L) {
            if (
                prefs.morningLaunchActive ||
                prefs.morningLaunchSnoozedUntil > 0L ||
                stage != MorningLaunchStage.IDLE
            ) {
                clearRuntimeState(keepDayOutcome = true)
            }
            return getStage()
        }

        val session = database.morningLaunchSessionDao().getById(sessionId)
        if (session == null || session.date != today) {
            clearRuntimeState(keepDayOutcome = true)
            return getStage()
        }

        when (stage) {
            MorningLaunchStage.SNOOZED -> {
                prefs.morningLaunchActive = false
                if (prefs.morningLaunchSnoozedUntil in 1..nowMs) {
                    prefs.morningLaunchStage = MorningLaunchStage.PROMPTING.name
                    prefs.morningLaunchActive = true
                    prefs.morningLaunchSnoozedUntil = 0L
                }
            }
            MorningLaunchStage.PROMPTING,
            MorningLaunchStage.IN_PROGRESS -> {
                prefs.morningLaunchActive = true
            }
            else -> {
                prefs.morningLaunchActive = false
            }
        }

        return getStage()
    }

    fun getRemainingEmergencyExits(): Int {
        syncEmergencyExitQuota()
        return (MAX_EMERGENCY_EXITS_PER_MONTH - prefs.morningLaunchEmergencyExitCount).coerceAtLeast(0)
    }

    fun canUseEmergencyExit(): Boolean = getRemainingEmergencyExits() > 0

    suspend fun useEmergencyExit(details: String = "EMERGENCY_EXIT"): Boolean {
        if (!canUseEmergencyExit()) return false
        prefs.morningLaunchEmergencyExitCount = prefs.morningLaunchEmergencyExitCount + 1
        markSkipped(details = details)
        return true
    }

    fun clearRuntimeState(keepDayOutcome: Boolean = false) {
        prefs.morningLaunchActive = false
        prefs.morningLaunchSessionId = 0L
        prefs.morningLaunchStartedAt = 0L
        prefs.morningLaunchAlarmDismissedAt = 0L
        prefs.morningLaunchSnoozedUntil = 0L
        prefs.morningLaunchCurrentStepIndex = 0
        prefs.morningLaunchStage = MorningLaunchStage.IDLE.name
        if (!keepDayOutcome) {
            prefs.morningLaunchDelayCount = 0
            prefs.morningLaunchLastOutcome = MorningLaunchOutcome.PENDING.name
        }
        MorningLaunchActivity.cancelSnoozeReminder(appContext)
        notifyGuardReload()
    }

    companion object {
        private const val FIVE_MINUTES_MS = 5L * 60L * 1000L
        private const val MAX_EMERGENCY_EXITS_PER_MONTH = 5
    }

    private fun syncEmergencyExitQuota() {
        val monthKey = YearMonth.now().toString()
        if (prefs.morningLaunchEmergencyExitMonth != monthKey) {
            prefs.morningLaunchEmergencyExitMonth = monthKey
            prefs.morningLaunchEmergencyExitCount = 0
        }
    }

    private fun notifyGuardReload() {
        appContext.sendBroadcast(android.content.Intent("com.ironmind.RELOAD_GUARD"))
    }
}
