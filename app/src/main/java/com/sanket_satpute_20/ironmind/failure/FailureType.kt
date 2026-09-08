package com.sanket_satpute_20.ironmind.failure

/**
 * Why a mission did not resolve as COMPLETED. Distinct from [com.sanket_satpute_20.ironmind.mission.MissionState] -
 * this classifies the *cause*, not the resulting Task lifecycle state.
 */
enum class FailureType {
    USER_SKIPPED,
    MISSION_EXPIRED,
    WORK_LOCK_BROKEN,
    POMODORO_BROKEN,
    EMERGENCY_EXIT,
    SYSTEM_INTERRUPTION,
    PROTECTION_FAILURE
}
