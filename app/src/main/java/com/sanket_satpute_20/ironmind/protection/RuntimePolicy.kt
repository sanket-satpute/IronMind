package com.sanket_satpute_20.ironmind.protection

/**
 * Immutable runtime protection state consumed by the enforcement layer.
 *
 * Product/business logic should construct this object. The AccessibilityService
 * enforces it; it does not invent policy.
 */
data class RuntimePolicy(
    val enabled: Boolean = false,
    val activeMissionId: Int? = null,
    val activeMissionName: String? = null,

    /** Packages explicitly allowed while this mission/session is active. */
    val allowedPackages: Set<String> = emptySet(),

    /** Packages explicitly blocked while this mission/session is active. */
    val blockedPackages: Set<String> = emptySet(),

    val workLockActive: Boolean = false,
    val pomodoroActive: Boolean = false,
    val sleepLockActive: Boolean = false,
    val morningLaunchActive: Boolean = false,

    /**
     * Emergency exit does not mean policy is globally disabled.
     * It means this specific protection session was intentionally exited.
     */
    val emergencyExitUsed: Boolean = false,

    val reason: RuntimePolicyReason = RuntimePolicyReason.NONE
)

enum class RuntimePolicyReason {
    NONE,
    MISSION,
    WORK_LOCK,
    POMODORO,
    SLEEP_LOCK,
    MORNING_LAUNCH,
    DETOX,
    CRUCIBLE
}
