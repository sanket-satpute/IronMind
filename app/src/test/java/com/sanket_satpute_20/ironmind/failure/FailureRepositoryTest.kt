package com.sanket_satpute_20.ironmind.failure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class FailureRepositoryTest {

    @Test
    fun `empty state returns null`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)

        assertNull(repo.getLatest())
        assertEquals(0, repo.countFailures(42, "2026-09-08"))
    }

    @Test
    fun `record and read latest`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)

        val evidence = FailureEvidence(
            taskId = 42,
            taskName = "Read Book",
            date = "2026-09-08",
            failureType = FailureType.WORK_LOCK_BROKEN,
            timestamp = 1000L,
            reason = "Interrupted",
            protectionWasActive = true,
            focusMinutes = 20,
            breachCount = 1,
            focusScore = 85
        )

        repo.record(evidence)
        val latest = repo.getLatest()
        
        assertEquals(evidence.taskId, latest?.taskId)
        assertEquals(evidence.failureType, latest?.failureType)
        assertEquals(evidence.focusMinutes, latest?.focusMinutes)
    }

    @Test
    fun `count by task and date`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)

        val ev1 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null)
        val ev2 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 2000L, null, false, null, null, null)
        val ev3 = FailureEvidence(42, "Task 42", "2026-09-09", FailureType.WORK_LOCK_BROKEN, 3000L, null, false, null, null, null)
        val ev4 = FailureEvidence(43, "Task 43", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 4000L, null, false, null, null, null)

        repo.record(ev1)
        repo.record(ev2)
        repo.record(ev3)
        repo.record(ev4)

        assertEquals(2, repo.countFailures(42, "2026-09-08"))
        assertEquals(1, repo.countFailures(42, "2026-09-09"))
        assertEquals(1, repo.countFailures(43, "2026-09-08"))
    }

    @Test
    fun `duplicate identity is ignored`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)

        val evidence1 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null)
        val evidence2 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null) // Same identity

        repo.record(evidence1)
        assertTrue(repo.contains(42, FailureType.WORK_LOCK_BROKEN, 1000L))
        
        repo.record(evidence2) // Should be a no-op
        assertEquals(1, repo.countFailures(42, "2026-09-08"))
    }

    @Test
    fun `different timestamp is a distinct failure`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)

        val evidence1 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null)
        val evidence2 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 2000L, null, false, null, null, null) // Different timestamp

        repo.record(evidence1)
        repo.record(evidence2)

        assertEquals(2, repo.countFailures(42, "2026-09-08"))
    }

    @Test
    fun `different failure type is a distinct failure`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)

        val evidence1 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.WORK_LOCK_BROKEN, 1000L, null, false, null, null, null)
        val evidence2 = FailureEvidence(42, "Task 42", "2026-09-08", FailureType.POMODORO_BROKEN, 1000L, null, false, null, null, null) // Different type

        repo.record(evidence1)
        repo.record(evidence2)

        assertEquals(2, repo.countFailures(42, "2026-09-08"))
    }

    @Test
    fun `history is bounded`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)

        for (i in 1..105) {
            repo.record(FailureEvidence(42, "Task", "2026-09-08", FailureType.WORK_LOCK_BROKEN, i.toLong(), null, false, null, null, null))
        }

        // History max is 100.
        // But countFailures counts the list size based on reading history
        val count = repo.countFailures(42, "2026-09-08")
        assertEquals(100, count)
    }

    @Test
    fun `malformed JSON safely recovers`() {
        val storage = InMemoryFailureStorage()
        val repo = DefaultFailureRepository(storage)
        
        storage.latestJson = "{ malformed"
        storage.historyJson = "[ { bad } ]"
        
        assertNull(repo.getLatest())
        assertEquals(0, repo.countFailures(42, "2026-09-08"))
    }
}
