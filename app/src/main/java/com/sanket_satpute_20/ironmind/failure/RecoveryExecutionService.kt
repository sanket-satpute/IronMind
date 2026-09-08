package com.sanket_satpute_20.ironmind.failure

import android.content.Context
import android.util.Log
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.mission.MissionExecutionResult
import com.sanket_satpute_20.ironmind.mission.MissionExecutionService
import com.sanket_satpute_20.ironmind.mission.MissionExecutor

/**
 * Executes an already-recommended recovery action.
 *
 * RecoveryDecisionEngine decides WHAT should happen.
 * RecoveryExecutionService performs the action.
 * UI is responsible only for presenting the result / collecting extra user input.
 */
class RecoveryExecutionService(
    private val failureService: FailureService,
    private val missionExecutor: MissionExecutor,
    private val db: IronMindDatabase
) {

    constructor(
        context: Context,
        missionExecutor: MissionExecutor = MissionExecutionService(context.applicationContext)
    ) : this(
        failureService = FailureService.create(context.applicationContext),
        missionExecutor = missionExecutor,
        db = IronMindDatabase.getDatabase(context.applicationContext)
    )

    fun currentRecovery(): RecoveryState {
        return failureService.getCurrentRecovery()
    }

    suspend fun execute(
        action: RecoveryAction
    ): RecoveryExecutionResult {

        return runCatching {

            val currentRecovery = failureService.getCurrentRecovery()

            if (currentRecovery !is RecoveryState.Required) {
                return@runCatching RecoveryExecutionResult.NoRecoveryRequired
            }

            if (currentRecovery.recommendedAction != action) {
                return@runCatching RecoveryExecutionResult.InvalidAction(
                    "The selected recovery action is not the currently recommended action."
                )
            }

            when (action) {

                is RecoveryAction.RetryMission -> {
                    executeRetry(action, currentRecovery)
                }

                is RecoveryAction.RescheduleMission -> {
                    db.taskDao().getTaskById(action.taskId)
                        ?: return@runCatching RecoveryExecutionResult.InvalidAction("The mission no longer exists.")

                    /*
                     * Rescheduling requires a new schedule/time.
                     * Do not invent one inside the recovery engine.
                     * Existing task scheduling UI/service will consume this result.
                     */
                    RecoveryExecutionResult.RescheduleRequired(
                        taskId = action.taskId
                    )
                }

                is RecoveryAction.ReplaceMission -> {
                    db.taskDao().getTaskById(action.taskId)
                        ?: return@runCatching RecoveryExecutionResult.InvalidAction("The mission no longer exists.")

                    /*
                     * Replacement requires a new mission definition.
                     * Do not silently fabricate a replacement task.
                     */
                    RecoveryExecutionResult.ReplacementRequired(
                        taskId = action.taskId
                    )
                }

                is RecoveryAction.TakeRecoveryBreak -> {
                    executeRecoveryBreak(
                        minutes = action.minutes,
                        recovery = currentRecovery
                    )
                }

                RecoveryAction.EndDay -> {
                    executeEndDay()
                }
            }

        }.getOrElse {
            Log.e(TAG, "Recovery execution failed", it)

            RecoveryExecutionResult.Failed(
                reason = "Unable to execute recovery action.",
                cause = it
            )
        }
    }

    /**
     * Called by the UI/business flow after RescheduleMission has actually been completed.
     */
    fun completeRescheduleRecovery(
        action: RecoveryAction.RescheduleMission,
        completedAt: Long = System.currentTimeMillis()
    ): RecoveryState {
        return failureService.completeRecovery(
            action = action,
            completedAt = completedAt
        )
    }

    /**
     * Called after ReplaceMission has actually created/replaced the mission.
     */
    fun completeReplacementRecovery(
        action: RecoveryAction.ReplaceMission,
        completedAt: Long = System.currentTimeMillis()
    ): RecoveryState {
        return failureService.completeRecovery(
            action = action,
            completedAt = completedAt
        )
    }

    private suspend fun executeRetry(
        action: RecoveryAction.RetryMission,
        recovery: RecoveryState.Required
    ): RecoveryExecutionResult {

        val task = db.taskDao().getTaskById(action.taskId)
            ?: return RecoveryExecutionResult.InvalidAction(
                "The mission no longer exists."
            )

        if (task.isCompleted) {
            return RecoveryExecutionResult.InvalidAction(
                "This mission has already been completed."
            )
        }

        if (task.isInProgress) {
            return RecoveryExecutionResult.InvalidAction(
                "This mission is already in progress."
            )
        }

        if (!task.isSkipped) {
            return RecoveryExecutionResult.InvalidAction(
                "This mission is not in a failed/skipped state eligible for retry."
            )
        }

        val result = missionExecutor.retryMission(
            taskId = action.taskId,
            retryReason = "RECOVERY_RETRY"
        )

        return when (result) {
            is MissionExecutionResult.Started ->
                RecoveryExecutionResult.RetryStarted(
                    taskId = action.taskId
                )

            is MissionExecutionResult.AlreadyActive ->
                RecoveryExecutionResult.InvalidAction(
                    "Mission is already active."
                )

            is MissionExecutionResult.InvalidWindow ->
                RecoveryExecutionResult.InvalidAction(
                    "The mission window is no longer valid."
                )

            is MissionExecutionResult.InvalidState ->
                RecoveryExecutionResult.InvalidAction(
                    "Mission is no longer eligible for retry."
                )

            is MissionExecutionResult.MissingTask ->
                RecoveryExecutionResult.InvalidAction(
                    "The mission no longer exists."
                )

            is MissionExecutionResult.Failed ->
                RecoveryExecutionResult.Failed(
                    reason = result.reason,
                    cause = result.cause
                )

            else ->
                RecoveryExecutionResult.InvalidAction(
                    "Mission could not be restarted."
                )
        }.also {
            if (it is RecoveryExecutionResult.RetryStarted) {
                failureService.completeRecovery(
                    action = action
                )
            }
        }
    }

    private fun executeRecoveryBreak(
        minutes: Int,
        recovery: RecoveryState.Required
    ): RecoveryExecutionResult {
        return RecoveryExecutionResult.InvalidAction(
            "Recovery break execution is unavailable."
        )
    }

    private fun executeEndDay(): RecoveryExecutionResult {

        failureService.completeRecovery(
            action = RecoveryAction.EndDay
        )

        return RecoveryExecutionResult.DayEnded
    }

    companion object {
        private const val TAG = "RecoveryExecution"
    }
}
