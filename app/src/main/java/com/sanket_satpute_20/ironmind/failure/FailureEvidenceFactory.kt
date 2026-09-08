package com.sanket_satpute_20.ironmind.failure

import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.mission.PomodoroSummary

object FailureEvidenceFactory {

    fun userSkipped(
        task: Task,
        timestamp: Long,
        reason: String,
        protectionWasActive: Boolean = false
    ): FailureEvidence =
        FailureEvidence(
            taskId = task.id,
            taskName = task.name,
            date = task.date,
            failureType = FailureType.USER_SKIPPED,
            timestamp = timestamp,
            reason = reason,
            protectionWasActive = protectionWasActive,
            focusScore = task.focusScore
        )

    fun workLockBroken(
        task: Task,
        timestamp: Long,
        reason: String,
        pomodoroSummary: PomodoroSummary? = null
    ): FailureEvidence =
        FailureEvidence(
            taskId = task.id,
            taskName = task.name,
            date = task.date,
            failureType = FailureType.WORK_LOCK_BROKEN,
            timestamp = timestamp,
            reason = reason,
            protectionWasActive = true,
            focusMinutes = pomodoroSummary?.focusMinutes,
            breachCount = pomodoroSummary?.breachCount,
            focusScore = pomodoroSummary?.focusScore
        )

    fun pomodoroBroken(
        task: Task,
        timestamp: Long,
        reason: String,
        pomodoroSummary: PomodoroSummary? = null
    ): FailureEvidence =
        FailureEvidence(
            taskId = task.id,
            taskName = task.name,
            date = task.date,
            failureType = FailureType.POMODORO_BROKEN,
            timestamp = timestamp,
            reason = reason,
            protectionWasActive = true,
            focusMinutes = pomodoroSummary?.focusMinutes,
            breachCount = pomodoroSummary?.breachCount,
            focusScore = pomodoroSummary?.focusScore
        )

    fun emergencyExit(
        task: Task,
        timestamp: Long,
        reason: String,
        pomodoroSummary: PomodoroSummary? = null
    ): FailureEvidence =
        FailureEvidence(
            taskId = task.id,
            taskName = task.name,
            date = task.date,
            failureType = FailureType.EMERGENCY_EXIT,
            timestamp = timestamp,
            reason = reason,
            protectionWasActive = true,
            focusMinutes = pomodoroSummary?.focusMinutes,
            breachCount = pomodoroSummary?.breachCount,
            focusScore = pomodoroSummary?.focusScore
        )

    /**
     * System interruption means the mission was active in durable state,
     * but the execution environment could no longer be proven to be alive
     * for that mission.
     *
     * This MUST remain a system failure and therefore must never be treated
     * as a user discipline failure.
     */
    fun systemInterrupted(
        task: Task,
        timestamp: Long,
        reason: String,
        protectionWasActive: Boolean = false,
        pomodoroSummary: PomodoroSummary? = null
    ): FailureEvidence =
        FailureEvidence(
            taskId = task.id,
            taskName = task.name,
            date = task.date,
            failureType = FailureType.SYSTEM_INTERRUPTION,
            timestamp = timestamp,
            reason = reason,
            protectionWasActive = protectionWasActive,
            focusMinutes = pomodoroSummary?.focusMinutes,
            breachCount = pomodoroSummary?.breachCount,
            focusScore = pomodoroSummary?.focusScore
        )
}
