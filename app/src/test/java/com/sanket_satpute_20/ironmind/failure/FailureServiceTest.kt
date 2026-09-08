package com.sanket_satpute_20.ironmind.failure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FailureServiceTest {

    @Test
    fun `first failure returns Recorded and sets Required`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 1000L, null, false, null, null, null)

        val result = service.recordFailure(evidence, remainingMinutesToday = 120)

        assertTrue(result is FailureRecordResult.Recorded)
        val recorded = result as FailureRecordResult.Recorded
        assertEquals(evidence, recorded.evidence)
        assertEquals(RecoveryAction.RetryMission(42), recorded.recovery.recommendedAction)

        val current = service.getCurrentRecovery()
        assertTrue(current is RecoveryState.Required)
    }

    @Test
    fun `duplicate failure returns AlreadyRecorded`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 1000L, null, false, null, null, null)

        service.recordFailure(evidence)
        val result = service.recordFailure(evidence) // duplicate

        assertTrue(result is FailureRecordResult.AlreadyRecorded)
        assertEquals(1, failureRepo.countFailures(42, "2026-09-08"))
    }

    @Test
    fun `prior failure count is passed into RecoveryDecisionEngine correctly`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val ev1 = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 1000L, null, false, null, null, null)
        val ev2 = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 2000L, null, false, null, null, null)
        val ev3 = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 3000L, null, false, null, null, null)

        service.recordFailure(ev1)
        service.recordFailure(ev2)
        val result = service.recordFailure(ev3)

        assertTrue(result is FailureRecordResult.Recorded)
        // With 2 recent failures, the engine should recommend ReplaceMission
        assertEquals(RecoveryAction.ReplaceMission(42), (result as FailureRecordResult.Recorded).recovery.recommendedAction)
    }
    
    @Test
    fun `expiration gets RescheduleMission`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.MISSION_EXPIRED, 1000L, null, false, null, null, null)

        val result = service.recordFailure(evidence)
        assertTrue(result is FailureRecordResult.Recorded)
        assertEquals(RecoveryAction.RescheduleMission(42), (result as FailureRecordResult.Recorded).recovery.recommendedAction)
    }

    @Test
    fun `repeated user failure gets ReplaceMission`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val ev1 = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 1000L, null, false, null, null, null)
        val ev2 = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 2000L, null, false, null, null, null)
        val ev3 = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 3000L, null, false, null, null, null)
        
        service.recordFailure(ev1) // count 0 -> retry
        service.recordFailure(ev2) // count 1 -> retry
        val result = service.recordFailure(ev3) // count 2 -> replace

        assertTrue(result is FailureRecordResult.Recorded)
        assertEquals(RecoveryAction.ReplaceMission(42), (result as FailureRecordResult.Recorded).recovery.recommendedAction)
    }

    @Test
    fun `system failure remains RetryMission despite high repeat count`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val ev1 = FailureEvidence(42, "Task", "2026-09-08", FailureType.PROTECTION_FAILURE, 1000L, null, false, null, null, null)
        val ev2 = FailureEvidence(42, "Task", "2026-09-08", FailureType.PROTECTION_FAILURE, 2000L, null, false, null, null, null)
        val ev3 = FailureEvidence(42, "Task", "2026-09-08", FailureType.PROTECTION_FAILURE, 3000L, null, false, null, null, null)
        
        service.recordFailure(ev1) 
        service.recordFailure(ev2) 
        val result = service.recordFailure(ev3) 

        assertTrue(result is FailureRecordResult.Recorded)
        assertEquals(RecoveryAction.RetryMission(42), (result as FailureRecordResult.Recorded).recovery.recommendedAction)
    }

    @Test
    fun `completeRecovery succeeds only for exact recommended action`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 1000L, null, false, null, null, null)
        service.recordFailure(evidence) // Recommends RetryMission(42)

        val result = service.completeRecovery(RecoveryAction.RetryMission(42))
        assertTrue(result is RecoveryState.Completed)
        assertEquals(RecoveryAction.RetryMission(42), (result as RecoveryState.Completed).action)
    }

    @Test
    fun `wrong action leaves recovery Required`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.USER_SKIPPED, 1000L, null, false, null, null, null)
        service.recordFailure(evidence) // Recommends RetryMission(42)

        val result = service.completeRecovery(RecoveryAction.RescheduleMission(42))
        assertTrue(result is RecoveryState.Required)
        assertEquals(RecoveryAction.RetryMission(42), (result as RecoveryState.Required).recommendedAction)
    }

    @Test
    fun `clearRecovery clears persisted state`() {
        val storage = InMemoryFailureStorage()
        val failureRepo = DefaultFailureRepository(storage)
        val recoveryRepo = DefaultRecoveryRepository(storage)
        val service = FailureService(failureRepo, recoveryRepo)

        val evidence = FailureEvidence(42, "Task", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null)
        service.recordFailure(evidence)
        
        service.clearRecovery()
        assertEquals(RecoveryState.None, service.getCurrentRecovery())
    }
}
