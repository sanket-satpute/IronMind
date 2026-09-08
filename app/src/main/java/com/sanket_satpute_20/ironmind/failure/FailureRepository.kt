package com.sanket_satpute_20.ironmind.failure

interface FailureRepository {

    /**
     * Returns the latest recorded failure, or null when no failure has been recorded.
     */
    fun getLatest(): FailureEvidence?

    /**
     * Returns how many failures the given mission has had on the specified date.
     *
     * The count includes previously recorded failures and excludes the failure currently
     * being recorded.
     */
    fun countFailures(taskId: Int, date: String): Int

    /**
     * Returns true when the exact failure instance has already been recorded.
     *
     * Idempotency key:
     * taskId + failureType + timestamp
     */
    fun contains(taskId: Int, failureType: FailureType, timestamp: Long): Boolean

    /**
     * Records a new failure.
     *
     * Implementations must preserve enough information to support later replay/debugging.
     */
    fun record(evidence: FailureEvidence)
}
