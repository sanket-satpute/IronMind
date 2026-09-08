package com.sanket_satpute_20.ironmind.mission

/**
 * Canonical lifecycle state of a mission.
 *
 * This is a domain model. It does not replace the Room [com.sanket_satpute_20.ironmind.data.Task]
 * entity, which remains the durable persistence model for Phase 1.
 */
enum class MissionState {
    PLANNED,
    READY,
    ACTIVE,
    COMPLETED,
    SKIPPED,
    DEFERRED,
    FAILED
}

/**
 * Converts the existing Task persistence flags into one canonical state.
 *
 * Ordering matters: ACTIVE must win over older completion/defer flags, since a task
 * can accumulate stale flags across retries.
 */
fun missionStateOf(
    isInProgress: Boolean,
    isCompleted: Boolean,
    isSkipped: Boolean,
    isDeferred: Boolean
): MissionState {
    return when {
        isInProgress -> MissionState.ACTIVE
        isCompleted -> MissionState.COMPLETED
        isSkipped -> MissionState.SKIPPED
        isDeferred -> MissionState.DEFERRED
        else -> MissionState.PLANNED
    }
}
