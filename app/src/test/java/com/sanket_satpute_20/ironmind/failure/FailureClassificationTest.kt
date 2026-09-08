package com.sanket_satpute_20.ironmind.failure

import org.junit.Assert.assertEquals
import org.junit.Test

/** Every FailureType must map to exactly one classification, and system causes must never
 * be classified as user failures - this is the trust-critical invariant of Phase 1C. */
class FailureClassificationTest {

    @Test
    fun `user skipped is a user failure`() {
        assertEquals(FailureClassification.USER_FAILURE, classificationOf(FailureType.USER_SKIPPED))
    }

    @Test
    fun `mission expired is a user failure`() {
        assertEquals(FailureClassification.USER_FAILURE, classificationOf(FailureType.MISSION_EXPIRED))
    }

    @Test
    fun `work lock broken is a user failure`() {
        assertEquals(FailureClassification.USER_FAILURE, classificationOf(FailureType.WORK_LOCK_BROKEN))
    }

    @Test
    fun `pomodoro broken is a user failure`() {
        assertEquals(FailureClassification.USER_FAILURE, classificationOf(FailureType.POMODORO_BROKEN))
    }

    @Test
    fun `emergency exit is a user failure`() {
        assertEquals(FailureClassification.USER_FAILURE, classificationOf(FailureType.EMERGENCY_EXIT))
    }

    @Test
    fun `system interruption is a system failure, never a user failure`() {
        assertEquals(FailureClassification.SYSTEM_FAILURE, classificationOf(FailureType.SYSTEM_INTERRUPTION))
    }

    @Test
    fun `protection failure is a system failure, never a user failure`() {
        assertEquals(FailureClassification.SYSTEM_FAILURE, classificationOf(FailureType.PROTECTION_FAILURE))
    }

    @Test
    fun `FailureEvidence exposes the same classification as the standalone mapper`() {
        val evidence = FailureEvidence(
            taskId = 1,
            taskName = "Deep Work",
            date = "2026-09-08",
            failureType = FailureType.PROTECTION_FAILURE,
            timestamp = 0L
        )
        assertEquals(FailureClassification.SYSTEM_FAILURE, evidence.classification)
    }
}
