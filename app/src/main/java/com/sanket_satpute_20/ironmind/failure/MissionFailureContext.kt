package com.sanket_satpute_20.ironmind.failure

/**
 * Describes the event that caused a mission to be resolved as a failure.
 *
 * USER_SKIP is a deliberate user action.
 * WORK_LOCK_BREAK / POMODORO_BREAK / EMERGENCY_EXIT are execution failures
 * already handled by the active protection layer.
 *
 * SYSTEM_INTERRUPTION is reserved for reconciliation when the mission was
 * active but its runtime execution environment disappeared unexpectedly.
 */
enum class MissionFailureContext {
    USER_SKIP,
    WORK_LOCK_BREAK,
    POMODORO_BREAK,
    EMERGENCY_EXIT,
    SYSTEM_INTERRUPTION
}
