package com.sanket_satpute_20.ironmind.mission

import com.sanket_satpute_20.ironmind.data.Task
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 1B-3: a repeated call that finds the task already resolved must be distinguishable
 * from a call that genuinely performed a new transition, so callers never repeat side effects
 * (XP, sounds, summaries, cleanup) for an idempotent no-op.
 */
class MissionExecutionResultIdempotencyTest {

    private val task = Task(id = 1, name = "Deep Work", isCompleted = true, completedAt = 1_000L)

    @Test
    fun `Completed and AlreadyCompleted are distinct result types`() {
        val freshTransition: MissionExecutionResult = MissionExecutionResult.Completed(task, 1_000L)
        val idempotentRepeat: MissionExecutionResult = MissionExecutionResult.AlreadyCompleted(task, 1_000L)

        assertTrue(freshTransition is MissionExecutionResult.Completed)
        assertTrue(idempotentRepeat is MissionExecutionResult.AlreadyCompleted)
        assertNotEquals(freshTransition, idempotentRepeat)
    }

    @Test
    fun `Skipped and AlreadySkipped are distinct result types`() {
        val freshTransition: MissionExecutionResult = MissionExecutionResult.Skipped(task, 1_000L)
        val idempotentRepeat: MissionExecutionResult = MissionExecutionResult.AlreadySkipped(task, 1_000L)

        assertTrue(freshTransition is MissionExecutionResult.Skipped)
        assertTrue(idempotentRepeat is MissionExecutionResult.AlreadySkipped)
        assertNotEquals(freshTransition, idempotentRepeat)
    }

    @Test
    fun `Deferred and AlreadyDeferred are distinct result types`() {
        val freshTransition: MissionExecutionResult = MissionExecutionResult.Deferred(task, 1_000L)
        val idempotentRepeat: MissionExecutionResult = MissionExecutionResult.AlreadyDeferred(task, 1_000L)

        assertTrue(freshTransition is MissionExecutionResult.Deferred)
        assertTrue(idempotentRepeat is MissionExecutionResult.AlreadyDeferred)
        assertNotEquals(freshTransition, idempotentRepeat)
    }
}
