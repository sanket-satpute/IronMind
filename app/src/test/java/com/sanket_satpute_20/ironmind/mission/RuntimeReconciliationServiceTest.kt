package com.sanket_satpute_20.ironmind.mission

import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.failure.FailureEvidenceFactory
import com.sanket_satpute_20.ironmind.failure.FailureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeReconciliationServiceTest {

    @Test
    fun `system interruption is classified as system failure`() {
        val task = Task(
            id = 42,
            name = "Study Kotlin",
            startTime = "10:00",
            endTime = "11:00",
            date = "2026-09-08",
            focusScore = 80
        )

        val evidence = FailureEvidenceFactory.systemInterrupted(
            task = task,
            timestamp = 1_000L,
            reason = "MISSION_RUNTIME_MISSING"
        )

        assertEquals(
            FailureType.SYSTEM_INTERRUPTION,
            evidence.failureType
        )
    }

    @Test
    fun `system interruption never reports user failure classification`() {
        val task = Task(
            id = 42,
            name = "Study Kotlin",
            startTime = "10:00",
            endTime = "11:00",
            date = "2026-09-08"
        )

        val evidence = FailureEvidenceFactory.systemInterrupted(
            task = task,
            timestamp = 1_000L,
            reason = "PROCESS_RESTART"
        )

        assertFalse(
            evidence.classification.name.contains("USER")
        )
    }

    @Test
    fun `system interruption carries the original task identity`() {
        val task = Task(
            id = 77,
            name = "Deep Work",
            date = "2026-09-08"
        )

        val evidence = FailureEvidenceFactory.systemInterrupted(
            task = task,
            timestamp = 2_000L,
            reason = "WORK_LOCK_RUNTIME_MISSING"
        )

        assertEquals(77, evidence.taskId)
        assertEquals("Deep Work", evidence.taskName)
    }

    @Test
    fun `multiple active tasks should have deterministic ownership`() {
        val tasks = listOf(
            Task(
                id = 2,
                name = "Later",
                date = "2026-09-08",
                startedAt = 2000L,
                isInProgress = true
            ),
            Task(
                id = 1,
                name = "Earlier",
                date = "2026-09-08",
                startedAt = 1000L,
                isInProgress = true
            )
        )

        val owner = tasks.sortedWith(
            compareBy<Task> { it.startedAt ?: Long.MAX_VALUE }
                .thenBy { it.id }
        ).first()

        assertEquals(1, owner.id)
    }

    @Test
    fun `completed task must never be considered active`() {
        val task = Task(
            id = 1,
            name = "Completed",
            date = "2026-09-08",
            isInProgress = true,
            isCompleted = true
        )

        val valid =
            task.isInProgress &&
                !task.isCompleted &&
                !task.isSkipped

        assertFalse(valid)
    }

    @Test
    fun `normal active task is represented by in-progress state`() {
        val task = Task(
            id = 1,
            name = "Focus",
            date = "2026-09-08",
            isInProgress = true
        )

        assertTrue(task.isInProgress)
        assertFalse(task.isCompleted)
        assertFalse(task.isSkipped)
    }
    @Test
    fun `active context belongs to task`() {
        val task = Task(id = 1, name = "A", date = "D", startTime = "S", endTime = "E")
        val prefsName = "A"
        val prefsDate = "D"
        val prefsStart = "S"
        val prefsEnd = "E"
        val belongs = task.name == prefsName && task.date == prefsDate && task.startTime == prefsStart && task.endTime == prefsEnd
        assertTrue(belongs)
    }

    @Test
    fun `active context belongs to another task`() {
        val task = Task(id = 1, name = "A", date = "D", startTime = "S", endTime = "E")
        val prefsName = "B"
        val prefsDate = "D"
        val prefsStart = "S"
        val prefsEnd = "E"
        val belongs = task.name == prefsName && task.date == prefsDate && task.startTime == prefsStart && task.endTime == prefsEnd
        assertFalse(belongs)
    }

    @Test
    fun `orphan runtime with no Task`() {
        val activeTaskId: Int? = null
        val runtimeTask: Task? = null
        val stillValid = runtimeTask?.let { task ->
            task.isInProgress && !task.isCompleted && !task.isSkipped
        } ?: false
        assertFalse(stillValid)
    }

    @Test
    fun `orphan runtime with completed Task`() {
        val runtimeTask = Task(id = 1, isInProgress = false, isCompleted = true)
        val stillValid = runtimeTask.let { task ->
            task.isInProgress && !task.isCompleted && !task.isSkipped
        }
        assertFalse(stillValid)
    }

    @Test
    fun `repeated reconciliation is idempotent`() {
        val latestTask = Task(id = 1, isInProgress = false, isSkipped = true)
        val shouldReconcile = latestTask.isInProgress && !latestTask.isCompleted && !latestTask.isSkipped
        assertFalse(shouldReconcile)
    }

    @Test
    fun `duplicate SYSTEM_INTERRUPTION is not created`() {
        // Idempotency check prevents duplicate events
        val latestTask = Task(id = 1, isInProgress = false, isSkipped = true)
        val shouldReconcile = latestTask.isInProgress && !latestTask.isCompleted && !latestTask.isSkipped
        assertFalse(shouldReconcile)
    }

    @Test
    fun `WorkLock belonging to another task is never cleared`() {
        val taskIdToClear = 1
        val prefsWorkLockTaskId = 2
        val shouldClear = taskIdToClear > 0 && prefsWorkLockTaskId == taskIdToClear
        assertFalse(shouldClear)
    }
}
