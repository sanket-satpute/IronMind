package com.sanket_satpute_20.ironmind.failure

import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.mission.PomodoroSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MissionFailureIntegrationTest {

    private lateinit var storage: InMemoryFailureStorage
    private lateinit var failureRepo: DefaultFailureRepository
    private lateinit var recoveryRepo: DefaultRecoveryRepository
    private lateinit var failureService: FailureService

    @Before
    fun setup() {
        storage = InMemoryFailureStorage()
        failureRepo = DefaultFailureRepository(storage)
        recoveryRepo = DefaultRecoveryRepository(storage)
        failureService = FailureService(failureRepo, recoveryRepo)
    }

    private fun mockTask(id: Int, name: String) = Task(
        id = id,
        name = name,
        date = "2026-09-08",
        startTime = "10:00",
        endTime = "11:00",
        focusScore = 100
    )

    @Test
    fun `USER_SKIP creates USER_SKIPPED evidence and recommends RetryMission`() {
        val task = mockTask(1, "Mission 1")
        val evidence = FailureEvidenceFactory.userSkipped(
            task = task,
            timestamp = 1000L,
            reason = "Just skipped",
            protectionWasActive = false
        )

        val result = failureService.recordFailure(evidence)
        assertTrue(result is FailureRecordResult.Recorded)

        val recovery = failureService.getCurrentRecovery()
        assertTrue(recovery is RecoveryState.Required)
        assertEquals(FailureType.USER_SKIPPED, (recovery as RecoveryState.Required).evidence.failureType)
        assertEquals(RecoveryAction.RetryMission(1), recovery.recommendedAction)
    }

    @Test
    fun `WORK_LOCK_BREAK creates WORK_LOCK_BROKEN evidence and recommends TakeRecoveryBreak`() {
        val task = mockTask(2, "Mission 2")
        val summary = PomodoroSummary(task.name, "BROKEN", 1, 25, 1, 80)
        val evidence = FailureEvidenceFactory.workLockBroken(
            task = task,
            timestamp = 2000L,
            reason = "WorkLock Broken",
            pomodoroSummary = summary
        )

        val result = failureService.recordFailure(evidence)
        assertTrue(result is FailureRecordResult.Recorded)

        val recovery = failureService.getCurrentRecovery()
        assertTrue(recovery is RecoveryState.Required)
        assertEquals(FailureType.WORK_LOCK_BROKEN, (recovery as RecoveryState.Required).evidence.failureType)
        assertEquals(RecoveryAction.TakeRecoveryBreak(5), recovery.recommendedAction)
        
        // Pomodoro metrics are preserved
        assertEquals(25, recovery.evidence.focusMinutes)
        assertEquals(1, recovery.evidence.breachCount)
    }

    @Test
    fun `duplicate skip does not create a second failure`() {
        val task = mockTask(1, "Mission 1")
        val evidence = FailureEvidenceFactory.userSkipped(
            task = task,
            timestamp = 1000L, // Exact same timestamp = duplicate
            reason = "Skip",
            protectionWasActive = false
        )

        failureService.recordFailure(evidence)
        val result2 = failureService.recordFailure(evidence)

        assertTrue(result2 is FailureRecordResult.AlreadyRecorded)
        assertEquals(1, failureRepo.countFailures(task.id, task.date))
    }

    @Test
    fun `Mission A failure does not overwrite Mission B recovery evidence`() {
        val taskA = mockTask(1, "Mission A")
        val taskB = mockTask(2, "Mission B")

        val evidenceA = FailureEvidenceFactory.workLockBroken(
            task = taskA,
            timestamp = 1000L,
            reason = "A Broken"
        )
        
        val evidenceB = FailureEvidenceFactory.userSkipped(
            task = taskB,
            timestamp = 2000L,
            reason = "B Skipped"
        )

        failureService.recordFailure(evidenceA)
        
        // Assert A is recorded
        var recovery = failureService.getCurrentRecovery()
        assertEquals(1, (recovery as RecoveryState.Required).evidence.taskId)
        assertEquals(FailureType.WORK_LOCK_BROKEN, recovery.evidence.failureType)

        failureService.recordFailure(evidenceB)

        // Assert B is recorded and is current
        recovery = failureService.getCurrentRecovery()
        assertEquals(2, (recovery as RecoveryState.Required).evidence.taskId)
        assertEquals(FailureType.USER_SKIPPED, recovery.evidence.failureType)
    }

    @Test
    fun `Emergency exit creates EMERGENCY_EXIT evidence`() {
        val task = mockTask(4, "Emergency Mission")
        val evidence = FailureEvidenceFactory.emergencyExit(
            task = task,
            timestamp = 3000L,
            reason = "Emergency"
        )

        val result = failureService.recordFailure(evidence)
        assertTrue(result is FailureRecordResult.Recorded)

        val recovery = failureService.getCurrentRecovery()
        assertEquals(FailureType.EMERGENCY_EXIT, (recovery as RecoveryState.Required).evidence.failureType)
    }
}
