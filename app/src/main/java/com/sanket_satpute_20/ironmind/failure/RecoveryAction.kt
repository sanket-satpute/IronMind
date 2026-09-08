package com.sanket_satpute_20.ironmind.failure

/**
 * The next executable behavior IronMind recommends after a failure. Deliberately small -
 * failure should produce an action, not a punishment screen.
 */
sealed interface RecoveryAction {

    data class RetryMission(val taskId: Int) : RecoveryAction

    data class RescheduleMission(val taskId: Int) : RecoveryAction

    data class ReplaceMission(val taskId: Int) : RecoveryAction

    data class TakeRecoveryBreak(val minutes: Int) : RecoveryAction

    data object EndDay : RecoveryAction
}
