package com.sanket_satpute_20.ironmind.failure

interface RecoveryRepository {

    /**
     * Returns the currently pending recovery, or null when none exists.
     */
    fun getCurrent(): RecoveryState

    /**
     * Replaces the currently pending recovery.
     *
     * This should be idempotent when the same recovery state is written repeatedly.
     */
    fun setRequired(state: RecoveryState.Required)

    /**
     * Marks the current recovery as completed.
     *
     * The stored completed state remains available until clear(), allowing callers
     * to inspect the most recent recovery outcome.
     */
    fun markCompleted(
        action: RecoveryAction,
        completedAt: Long
    )

    /**
     * Removes recovery state completely.
     */
    fun clear()
}
