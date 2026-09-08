package com.sanket_satpute_20.ironmind.failure

/**
 * Everything a recovery decision (or a recovery screen) needs to know about one failure,
 * gathered in one place so callers don't have to reconstruct it from Task/TaskEvent/
 * PomodoroSummary separately.
 */
data class FailureEvidence(
    val taskId: Int,
    val taskName: String,
    val date: String,
    val failureType: FailureType,
    val timestamp: Long,
    val reason: String? = null,
    val protectionWasActive: Boolean = false,
    val focusMinutes: Int? = null,
    val breachCount: Int? = null,
    val focusScore: Int? = null
) {
    val classification: FailureClassification get() = classificationOf(failureType)
}
