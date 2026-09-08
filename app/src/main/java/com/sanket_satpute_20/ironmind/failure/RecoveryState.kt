package com.sanket_satpute_20.ironmind.failure

/**
 * Domain state of the recovery flow (not Compose UI state), so it can be persisted and
 * survive process death.
 */
sealed interface RecoveryState {

    data object None : RecoveryState

    data class Required(
        val evidence: FailureEvidence,
        val recommendedAction: RecoveryAction
    ) : RecoveryState

    data class Completed(
        val action: RecoveryAction,
        val completedAt: Long
    ) : RecoveryState
}
