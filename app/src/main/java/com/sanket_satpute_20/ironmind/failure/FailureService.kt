package com.sanket_satpute_20.ironmind.failure

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

sealed interface FailureRecordResult {

    data class Recorded(
        val evidence: FailureEvidence,
        val recovery: RecoveryState.Required
    ) : FailureRecordResult

    data class AlreadyRecorded(
        val evidence: FailureEvidence,
        val recovery: RecoveryState
    ) : FailureRecordResult

    data class Failed(
        val reason: String,
        val cause: Throwable? = null
    ) : FailureRecordResult
}

class FailureService(
    private val failureRepository: FailureRepository,
    private val recoveryRepository: RecoveryRepository
) {

    fun recordFailure(
        evidence: FailureEvidence,
        remainingMinutesToday: Int? = null
    ): FailureRecordResult {

        return runCatching {

            synchronized(lockFor(evidence)) {

                /*
                 * First check exact idempotency.
                 *
                 * This prevents repeated callbacks from creating multiple failures
                 * and multiple recovery states.
                 */
                if (
                    failureRepository.contains(
                        taskId = evidence.taskId,
                        failureType = evidence.failureType,
                        timestamp = evidence.timestamp
                    )
                ) {
                    val currentRecovery = recoveryRepository.getCurrent()

                    return@synchronized FailureRecordResult.AlreadyRecorded(
                        evidence = evidence,
                        recovery = currentRecovery
                    )
                }

                val priorFailures =
                    failureRepository.countFailures(
                        taskId = evidence.taskId,
                        date = evidence.date
                    )

                val recommendedAction =
                    RecoveryDecisionEngine.recommend(
                        evidence = evidence,
                        recentFailureCount = priorFailures,
                        remainingMinutesToday = remainingMinutesToday
                    )

                val recoveryState =
                    RecoveryState.Required(
                        evidence = evidence,
                        recommendedAction = recommendedAction
                    )

                /*
                 * Persist failure first.
                 *
                 * If recovery persistence fails, the failure still exists and can be
                 * reconciled later rather than disappearing silently.
                 */
                failureRepository.record(evidence)

                recoveryRepository.setRequired(
                    recoveryState
                )

                FailureRecordResult.Recorded(
                    evidence = evidence,
                    recovery = recoveryState
                )
            }

        }.getOrElse {
            FailureRecordResult.Failed(
                reason = "Unable to record failure",
                cause = it
            )
        }
    }

    fun getCurrentRecovery(): RecoveryState {
        return recoveryRepository.getCurrent()
    }

    fun completeRecovery(
        action: RecoveryAction,
        completedAt: Long = System.currentTimeMillis()
    ): RecoveryState {

        val current = recoveryRepository.getCurrent()

        /*
         * No pending recovery means there is nothing to complete.
         */
        if (current !is RecoveryState.Required) {
            return current
        }

        /*
         * Prevent a caller from completing a different action than the one
         * IronMind actually recommended.
         */
        if (current.recommendedAction != action) {
            return current
        }

        recoveryRepository.markCompleted(
            action = action,
            completedAt = completedAt
        )

        return RecoveryState.Completed(
            action = action,
            completedAt = completedAt
        )
    }

    fun clearRecovery() {
        recoveryRepository.clear()
    }

    private fun lockFor(
        evidence: FailureEvidence
    ): Any {
        val key = "${evidence.taskId}|${evidence.failureType}|${evidence.timestamp}"

        return locks.getOrPut(key) {
            Any()
        }
    }

    companion object {
        private val locks =
            ConcurrentHashMap<String, Any>()

        fun create(context: Context): FailureService {
            val storage =
                PrefManagerFailureStorage(context.applicationContext)

            return FailureService(
                failureRepository = DefaultFailureRepository(storage),
                recoveryRepository = DefaultRecoveryRepository(storage)
            )
        }
    }
}
