package com.sanket_satpute_20.ironmind.mission

import org.junit.Assert.assertEquals
import org.junit.Test

class MissionStateTest {

    @Test
    fun `new task with no flags is PLANNED`() {
        assertEquals(
            MissionState.PLANNED,
            missionStateOf(isInProgress = false, isCompleted = false, isSkipped = false, isDeferred = false)
        )
    }

    @Test
    fun `in progress wins regardless of other flags`() {
        assertEquals(
            MissionState.ACTIVE,
            missionStateOf(isInProgress = true, isCompleted = true, isSkipped = true, isDeferred = true)
        )
    }

    @Test
    fun `completed flag maps to COMPLETED when not in progress`() {
        assertEquals(
            MissionState.COMPLETED,
            missionStateOf(isInProgress = false, isCompleted = true, isSkipped = false, isDeferred = false)
        )
    }

    @Test
    fun `skipped flag maps to SKIPPED when not completed or in progress`() {
        assertEquals(
            MissionState.SKIPPED,
            missionStateOf(isInProgress = false, isCompleted = false, isSkipped = true, isDeferred = false)
        )
    }

    @Test
    fun `deferred flag maps to DEFERRED when no stronger flag is set`() {
        assertEquals(
            MissionState.DEFERRED,
            missionStateOf(isInProgress = false, isCompleted = false, isSkipped = false, isDeferred = true)
        )
    }

    @Test
    fun `completed wins over skipped and deferred`() {
        assertEquals(
            MissionState.COMPLETED,
            missionStateOf(isInProgress = false, isCompleted = true, isSkipped = true, isDeferred = true)
        )
    }

    @Test
    fun `skipped wins over deferred`() {
        assertEquals(
            MissionState.SKIPPED,
            missionStateOf(isInProgress = false, isCompleted = false, isSkipped = true, isDeferred = true)
        )
    }
}
