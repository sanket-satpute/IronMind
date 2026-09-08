package com.sanket_satpute_20.ironmind.mission

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the identity checks that guard cross-mission cleanup (Phase 1B-2): completing/
 * skipping/deferring Mission A must never clear a runtime flag actually owned by Mission B.
 */
class MissionOwnershipTest {

    @Test
    fun `owner check is true only when flag active and id matches`() {
        assertTrue(isOwnerOf(activeFlag = true, activeOwnerTaskId = 7, taskId = 7))
    }

    @Test
    fun `owner check is false when flag inactive even if id matches`() {
        assertFalse(isOwnerOf(activeFlag = false, activeOwnerTaskId = 7, taskId = 7))
    }

    @Test
    fun `owner check is false when a different mission owns the flag`() {
        // Mission A (id=7) is active under WorkLock; Mission B (id=9) resolving must not
        // be able to clear Mission A's WorkLock.
        assertFalse(isOwnerOf(activeFlag = true, activeOwnerTaskId = 7, taskId = 9))
    }

    @Test
    fun `active window owner check matches only on exact name, date, start time, and end time`() {
        assertTrue(
            isActiveWindowOwnerOf(
                activeName = "Deep Work",
                activeDate = "2026-09-08",
                activeStartTime = "09:00",
                activeEndTime = "11:00",
                taskName = "Deep Work",
                taskDate = "2026-09-08",
                taskStartTime = "09:00",
                taskEndTime = "11:00"
            )
        )
    }

    @Test
    fun `active window owner check fails for a different task name on the same date`() {
        assertFalse(
            isActiveWindowOwnerOf(
                activeName = "Deep Work",
                activeDate = "2026-09-08",
                activeStartTime = "09:00",
                activeEndTime = "11:00",
                taskName = "Workout",
                taskDate = "2026-09-08",
                taskStartTime = "09:00",
                taskEndTime = "11:00"
            )
        )
    }

    @Test
    fun `active window owner check fails for the same name on a different time`() {
        assertFalse(
            isActiveWindowOwnerOf(
                activeName = "Deep Work",
                activeDate = "2026-09-08",
                activeStartTime = "09:00",
                activeEndTime = "11:00",
                taskName = "Deep Work",
                taskDate = "2026-09-08",
                taskStartTime = "11:00",
                taskEndTime = "13:00"
            )
        )
    }

    @Test
    fun `active window owner check fails when no active window is set`() {
        assertFalse(
            isActiveWindowOwnerOf(
                activeName = "",
                activeDate = "2026-09-08",
                activeStartTime = "09:00",
                activeEndTime = "11:00",
                taskName = "Deep Work",
                taskDate = "2026-09-08",
                taskStartTime = "09:00",
                taskEndTime = "11:00"
            )
        )
    }
}
