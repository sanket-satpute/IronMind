package com.sanket_satpute_20.ironmind.failure

/**
 * Deterministic recovery recommendation rules. No AI/ML - a fixed decision table that turns
 * a failure into the next executable action.
 *
 * This is intentionally the only place these rules live. Do not duplicate them in UI or in
 * MissionExecutionService.
 */
object RecoveryDecisionEngine {

    private const val END_OF_DAY_THRESHOLD_MINUTES = 10
    private const val REPEATED_FAILURE_THRESHOLD = 2
    private const val DEFAULT_RECOVERY_BREAK_MINUTES = 5

    /**
     * @param evidence what failed and why.
     * @param recentFailureCount how many times this same mission has already failed today
     *   (excluding this occurrence). Used to escalate from retry to reduced scope.
     * @param remainingMinutesToday minutes left in the user's productive day, if known. When
     *   this is at or below [END_OF_DAY_THRESHOLD_MINUTES], every failure resolves to [RecoveryAction.EndDay]
     *   regardless of type or classification - there is no meaningful recovery window left.
     */
    fun recommend(
        evidence: FailureEvidence,
        recentFailureCount: Int = 0,
        remainingMinutesToday: Int? = null
    ): RecoveryAction {
        if (remainingMinutesToday != null && remainingMinutesToday <= END_OF_DAY_THRESHOLD_MINUTES) {
            return RecoveryAction.EndDay
        }

        if (evidence.failureType == FailureType.MISSION_EXPIRED) {
            return RecoveryAction.RescheduleMission(evidence.taskId)
        }

        // System/protection failures are never escalated or penalized by repetition - that
        // would punish the user for IronMind's own reliability problems.
        if (evidence.classification == FailureClassification.SYSTEM_FAILURE) {
            return RecoveryAction.RetryMission(evidence.taskId)
        }

        if (recentFailureCount >= REPEATED_FAILURE_THRESHOLD) {
            return RecoveryAction.ReplaceMission(evidence.taskId)
        }

        // Remaining reachable types here are USER_SKIPPED, WORK_LOCK_BROKEN, POMODORO_BROKEN
        // and EMERGENCY_EXIT (MISSION_EXPIRED and the two system types were resolved above).
        return if (evidence.failureType == FailureType.WORK_LOCK_BROKEN) {
            RecoveryAction.TakeRecoveryBreak(DEFAULT_RECOVERY_BREAK_MINUTES)
        } else {
            RecoveryAction.RetryMission(evidence.taskId)
        }
    }
}
