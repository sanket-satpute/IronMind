package com.sanket_satpute_20.ironmind.failure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** RecoveryState is domain state, not Compose UI state - verify its three cases are distinct
 * and carry the data a persistence layer / recovery screen needs (added in a later 1C part). */
class RecoveryStateTest {

    private val evidence = FailureEvidence(
        taskId = 1,
        taskName = "Deep Work",
        date = "2026-09-08",
        failureType = FailureType.USER_SKIPPED,
        timestamp = 1_000L
    )

    @Test
    fun `None carries no data`() {
        val state: RecoveryState = RecoveryState.None
        assertTrue(state is RecoveryState.None)
    }

    @Test
    fun `Required carries evidence and the recommended action`() {
        val action = RecoveryAction.RetryMission(evidence.taskId)
        val state: RecoveryState = RecoveryState.Required(evidence, action)

        require(state is RecoveryState.Required)
        assertEquals(evidence, state.evidence)
        assertEquals(action, state.recommendedAction)
    }

    @Test
    fun `Completed carries the action that was taken and when`() {
        val action = RecoveryAction.RescheduleMission(evidence.taskId)
        val state: RecoveryState = RecoveryState.Completed(action, completedAt = 2_000L)

        require(state is RecoveryState.Completed)
        assertEquals(action, state.action)
        assertEquals(2_000L, state.completedAt)
    }
}
