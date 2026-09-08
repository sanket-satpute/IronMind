package com.sanket_satpute_20.ironmind.failure

import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.mission.PomodoroSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FailureEvidenceFactoryTest {

    private val baseTask = Task(
        id = 1,
        name = "Test Task",
        date = "2026-09-08",
        startTime = "09:00",
        endTime = "10:00",
        focusScore = 85
    )

    private val pomodoroSummary = PomodoroSummary(
        taskName = "Test Task",
        outcome = "BROKEN",
        intervalsCompleted = 2,
        focusMinutes = 50,
        breachCount = 1,
        focusScore = 80
    )

    @Test
    fun `userSkipped correctly maps properties`() {
        val evidence = FailureEvidenceFactory.userSkipped(
            task = baseTask.copy(isInProgress = false),
            timestamp = 1000L,
            reason = "Skip Reason",
            protectionWasActive = true
        )

        assertEquals(1, evidence.taskId)
        assertEquals("Test Task", evidence.taskName)
        assertEquals("2026-09-08", evidence.date)
        assertEquals(FailureType.USER_SKIPPED, evidence.failureType)
        assertEquals(1000L, evidence.timestamp)
        assertEquals("Skip Reason", evidence.reason)
        assertTrue(evidence.protectionWasActive)
        assertEquals(85, evidence.focusScore)
        assertNull(evidence.focusMinutes)
        assertNull(evidence.breachCount)
    }

    @Test
    fun `workLockBroken includes pomodoro summary if available`() {
        val evidence = FailureEvidenceFactory.workLockBroken(
            task = baseTask,
            timestamp = 2000L,
            reason = "WorkLock Break",
            pomodoroSummary = pomodoroSummary
        )

        assertEquals(FailureType.WORK_LOCK_BROKEN, evidence.failureType)
        assertTrue(evidence.protectionWasActive)
        assertEquals(50, evidence.focusMinutes)
        assertEquals(1, evidence.breachCount)
        assertEquals(80, evidence.focusScore)
    }

    @Test
    fun `pomodoroBroken handles null summary gracefully`() {
        val evidence = FailureEvidenceFactory.pomodoroBroken(
            task = baseTask,
            timestamp = 3000L,
            reason = "Pomodoro Break",
            pomodoroSummary = null
        )

        assertEquals(FailureType.POMODORO_BROKEN, evidence.failureType)
        assertTrue(evidence.protectionWasActive)
        assertNull(evidence.focusMinutes)
        assertNull(evidence.breachCount)
        assertNull(evidence.focusScore)
    }

    @Test
    fun `emergencyExit always sets protection active`() {
        val evidence = FailureEvidenceFactory.emergencyExit(
            task = baseTask,
            timestamp = 4000L,
            reason = "Emergency",
            pomodoroSummary = pomodoroSummary
        )

        assertEquals(FailureType.EMERGENCY_EXIT, evidence.failureType)
        assertTrue(evidence.protectionWasActive)
        assertEquals(50, evidence.focusMinutes)
    }
}
