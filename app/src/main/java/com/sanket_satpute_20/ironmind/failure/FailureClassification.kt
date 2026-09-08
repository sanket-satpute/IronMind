package com.sanket_satpute_20.ironmind.failure

/**
 * Whether a failure was caused by the user or by IronMind itself. This distinction must be
 * enforced here, in the domain layer, so it can never be lost or reinterpreted downstream:
 * SYSTEM_FAILURE must never reduce the user's integrity score.
 */
enum class FailureClassification {
    USER_FAILURE,
    SYSTEM_FAILURE,
    NEUTRAL
}

/** Deterministic mapping from cause to blame. Do not make this configurable/AI-driven here. */
fun classificationOf(type: FailureType): FailureClassification = when (type) {
    FailureType.USER_SKIPPED -> FailureClassification.USER_FAILURE
    FailureType.MISSION_EXPIRED -> FailureClassification.USER_FAILURE
    FailureType.WORK_LOCK_BROKEN -> FailureClassification.USER_FAILURE
    FailureType.POMODORO_BROKEN -> FailureClassification.USER_FAILURE
    FailureType.EMERGENCY_EXIT -> FailureClassification.USER_FAILURE
    FailureType.SYSTEM_INTERRUPTION -> FailureClassification.SYSTEM_FAILURE
    FailureType.PROTECTION_FAILURE -> FailureClassification.SYSTEM_FAILURE
}
