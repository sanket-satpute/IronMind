package com.sanket_satpute_20.ironmind.focus

object EmergencyCooldownPolicy {
    const val RESET_PROTOCOL_NAME = "RESET PROTOCOL"
    const val FIVE_MIN_RESET_MILLIS = 5 * 60 * 1000L
    const val DECISION_DELAY_MILLIS = 10 * 60 * 1000L
    const val EMERGENCY_APP_MILLIS = 5 * 60 * 1000L

    fun durationForAction(action: String): Long? {
        return when (action) {
            "FIVE_MIN_RESET" -> FIVE_MIN_RESET_MILLIS
            "DELAY_DECISION_10_MIN" -> DECISION_DELAY_MILLIS
            "OPEN_EMERGENCY_APP" -> EMERGENCY_APP_MILLIS
            else -> null
        }
    }

    fun confirmationForAction(action: String): String? {
        return when (action) {
            "FIVE_MIN_RESET" -> "5-minute recovery shield active"
            "DELAY_DECISION_10_MIN" -> "10-minute decision delay active"
            "OPEN_EMERGENCY_APP" -> "Emergency app shield active for 5 minutes"
            "RETURN_TO_MISSION" -> "Recovery shield cleared. Return clean."
            "BACK_TO_HOME" -> "Recovery shield cleared."
            else -> null
        }
    }

    fun totalDurationForMode(mode: String): Long {
        return when (mode) {
            "delay" -> DECISION_DELAY_MILLIS
            else -> FIVE_MIN_RESET_MILLIS
        }
    }
}
