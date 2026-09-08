package com.sanket_satpute_20.ironmind.failure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryRepositoryTest {

    @Test
    fun `empty returns None`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultRecoveryRepository(storage)

        assertEquals(RecoveryState.None, repo.getCurrent())
    }

    @Test
    fun `Required round trip`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultRecoveryRepository(storage)

        val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null)
        val state = RecoveryState.Required(evidence, RecoveryAction.RetryMission(42))

        repo.setRequired(state)

        val current = repo.getCurrent()
        assertTrue(current is RecoveryState.Required)
        assertEquals(42, (current as RecoveryState.Required).evidence.taskId)
        assertEquals(RecoveryAction.RetryMission(42), current.recommendedAction)
    }

    @Test
    fun `Completed round trip`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultRecoveryRepository(storage)

        repo.markCompleted(RecoveryAction.RescheduleMission(42), 5000L)

        val current = repo.getCurrent()
        assertTrue(current is RecoveryState.Completed)
        assertEquals(RecoveryAction.RescheduleMission(42), (current as RecoveryState.Completed).action)
        assertEquals(5000L, current.completedAt)
    }

    @Test
    fun `every RecoveryAction round trip`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultRecoveryRepository(storage)
        
        val actions = listOf(
            RecoveryAction.RetryMission(1),
            RecoveryAction.RescheduleMission(2),
            RecoveryAction.ReplaceMission(3),
            RecoveryAction.TakeRecoveryBreak(15),
            RecoveryAction.EndDay
        )

        for (action in actions) {
            val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null)
            repo.setRequired(RecoveryState.Required(evidence, action))
            val current = repo.getCurrent() as RecoveryState.Required
            assertEquals(action, current.recommendedAction)
        }
    }

    @Test
    fun `clear returns None`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultRecoveryRepository(storage)

        repo.markCompleted(RecoveryAction.EndDay, 1000L)
        repo.clear()

        assertEquals(RecoveryState.None, repo.getCurrent())
    }

    @Test
    fun `malformed JSON returns None`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultRecoveryRepository(storage)

        storage.recoveryJson = "not valid json"
        assertEquals(RecoveryState.None, repo.getCurrent())
    }
}
