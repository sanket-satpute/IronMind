package com.sanket_satpute_20.ironmind.failure

sealed interface RecoveryExecutionResult {

    data class RetryStarted(
        val taskId: Int
    ) : RecoveryExecutionResult

    data class RescheduleRequired(
        val taskId: Int
    ) : RecoveryExecutionResult

    data class ReplacementRequired(
        val taskId: Int
    ) : RecoveryExecutionResult

    data class RecoveryBreakStarted(
        val minutes: Int
    ) : RecoveryExecutionResult

    data object DayEnded : RecoveryExecutionResult

    data object NoRecoveryRequired : RecoveryExecutionResult

    data class InvalidAction(
        val reason: String
    ) : RecoveryExecutionResult

    data class Failed(
        val reason: String,
        val cause: Throwable? = null
    ) : RecoveryExecutionResult
}
