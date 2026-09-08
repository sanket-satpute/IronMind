package com.sanket_satpute_20.ironmind.failure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Deterministic decision-table tests for RecoveryDecisionEngine. Mirrors the Phase 1C minimum
 * test matrix: classification, first-failure retry, expiration reschedule, WorkLock/Pomodoro
 * breach recovery, emergency exit recovery, system-failure no-punishment, and repeated-failure
 * escalation.
 */
class RecoveryDecisionEngineTest {

    private fun evidenceFor(type: FailureType, taskId: Int = 1): FailureEvidence = FailureEvidence(
        taskId = taskId,
        taskName = "Deep Work",
        date = "2026-09-08",
        failureType = type,
        timestamp = 0L
    )

    @Test
    fun `first user skip recommends retry`() {
        val action = RecoveryDecisionEngine.recommend(evidenceFor(FailureType.USER_SKIPPED))
        assertEquals(RecoveryAction.RetryMission(1), action)
    }

    @Test
    fun `expired mission recommends reschedule regardless of failure count`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.MISSION_EXPIRED),
            recentFailureCount = 5
        )
        assertEquals(RecoveryAction.RescheduleMission(1), action)
    }

    @Test
    fun `work lock breach recommends a short recovery break`() {
        val action = RecoveryDecisionEngine.recommend(evidenceFor(FailureType.WORK_LOCK_BROKEN))
        assertTrue(action is RecoveryAction.TakeRecoveryBreak)
        assertEquals(5, (action as RecoveryAction.TakeRecoveryBreak).minutes)
    }

    @Test
    fun `pomodoro breach recommends retry`() {
        val action = RecoveryDecisionEngine.recommend(evidenceFor(FailureType.POMODORO_BROKEN))
        assertEquals(RecoveryAction.RetryMission(1), action)
    }

    @Test
    fun `emergency exit recommends retry`() {
        val action = RecoveryDecisionEngine.recommend(evidenceFor(FailureType.EMERGENCY_EXIT))
        assertEquals(RecoveryAction.RetryMission(1), action)
    }

    @Test
    fun `system interruption never punishes the user - always resolves to retry`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.SYSTEM_INTERRUPTION),
            recentFailureCount = 10 // even with many prior failures, no escalation
        )
        assertEquals(RecoveryAction.RetryMission(1), action)
    }

    @Test
    fun `protection failure never punishes the user - always resolves to retry`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.PROTECTION_FAILURE),
            recentFailureCount = 10
        )
        assertEquals(RecoveryAction.RetryMission(1), action)
    }

    @Test
    fun `repeated user failure escalates to reduced mission scope`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.USER_SKIPPED),
            recentFailureCount = 2
        )
        assertEquals(RecoveryAction.ReplaceMission(1), action)
    }

    @Test
    fun `repeated work lock breach also escalates to reduced mission scope`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.WORK_LOCK_BROKEN),
            recentFailureCount = 3
        )
        assertEquals(RecoveryAction.ReplaceMission(1), action)
    }

    @Test
    fun `single prior failure does not yet escalate`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.USER_SKIPPED),
            recentFailureCount = 1
        )
        assertEquals(RecoveryAction.RetryMission(1), action)
    }

    @Test
    fun `no meaningful time left in the day always ends the day`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.USER_SKIPPED),
            remainingMinutesToday = 5
        )
        assertEquals(RecoveryAction.EndDay, action)
    }

    @Test
    fun `end of day takes precedence even over a system failure`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.SYSTEM_INTERRUPTION),
            remainingMinutesToday = 0
        )
        assertEquals(RecoveryAction.EndDay, action)
    }

    @Test
    fun `plenty of remaining time does not force end of day`() {
        val action = RecoveryDecisionEngine.recommend(
            evidenceFor(FailureType.USER_SKIPPED),
            remainingMinutesToday = 120
        )
        assertEquals(RecoveryAction.RetryMission(1), action)
    }
}
