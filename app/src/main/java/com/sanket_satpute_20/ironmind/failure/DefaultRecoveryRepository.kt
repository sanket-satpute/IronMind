package com.sanket_satpute_20.ironmind.failure

import org.json.JSONObject

class DefaultRecoveryRepository(
    private val storage: FailureStorage
) : RecoveryRepository {

    override fun getCurrent(): RecoveryState {
        val raw = storage.recoveryJson

        if (raw.isBlank()) {
            return RecoveryState.None
        }

        return runCatching {
            fromJson(JSONObject(raw))
        }.getOrElse {
            RecoveryState.None
        }
    }

    override fun setRequired(state: RecoveryState.Required) {
        storage.recoveryJson = toJson(state).toString()
    }

    override fun markCompleted(
        action: RecoveryAction,
        completedAt: Long
    ) {
        storage.recoveryJson = JSONObject()
            .apply {
                put("status", "COMPLETED")
                put("completedAt", completedAt)
                put("action", actionToJson(action))
            }
            .toString()
    }

    override fun clear() {
        storage.recoveryJson = ""
    }

    private fun toJson(
        state: RecoveryState.Required
    ): JSONObject {
        return JSONObject().apply {
            put("status", "REQUIRED")
            put("evidence", evidenceToJson(state.evidence))
            put(
                "recommendedAction",
                actionToJson(state.recommendedAction)
            )
        }
    }

    private fun fromJson(
        json: JSONObject
    ): RecoveryState {
        return when (json.optString("status")) {
            "REQUIRED" -> {
                RecoveryState.Required(
                    evidence = evidenceFromJson(
                        json.getJSONObject("evidence")
                    ),
                    recommendedAction = actionFromJson(
                        json.getJSONObject("recommendedAction")
                    )
                )
            }

            "COMPLETED" -> {
                RecoveryState.Completed(
                    action = actionFromJson(
                        json.getJSONObject("action")
                    ),
                    completedAt = json.getLong("completedAt")
                )
            }

            else -> RecoveryState.None
        }
    }

    private fun evidenceToJson(
        evidence: FailureEvidence
    ): JSONObject {
        return JSONObject().apply {
            put("taskId", evidence.taskId)
            put("taskName", evidence.taskName)
            put("date", evidence.date)
            put("failureType", evidence.failureType.name)
            put("timestamp", evidence.timestamp)
            put("reason", evidence.reason)
            put(
                "protectionWasActive",
                evidence.protectionWasActive
            )

            if (evidence.focusMinutes == null) {
                put("focusMinutes", JSONObject.NULL)
            } else {
                put("focusMinutes", evidence.focusMinutes)
            }

            if (evidence.breachCount == null) {
                put("breachCount", JSONObject.NULL)
            } else {
                put("breachCount", evidence.breachCount)
            }

            if (evidence.focusScore == null) {
                put("focusScore", JSONObject.NULL)
            } else {
                put("focusScore", evidence.focusScore)
            }
        }
    }

    private fun evidenceFromJson(
        json: JSONObject
    ): FailureEvidence {
        return FailureEvidence(
            taskId = json.getInt("taskId"),
            taskName = json.getString("taskName"),
            date = json.getString("date"),
            failureType = FailureType.valueOf(
                json.getString("failureType")
            ),
            timestamp = json.getLong("timestamp"),
            reason = if (json.isNull("reason")) {
                null
            } else {
                json.getString("reason")
            },
            protectionWasActive = json.optBoolean(
                "protectionWasActive",
                false
            ),
            focusMinutes = nullableInt(json, "focusMinutes"),
            breachCount = nullableInt(json, "breachCount"),
            focusScore = nullableInt(json, "focusScore")
        )
    }

    private fun actionToJson(
        action: RecoveryAction
    ): JSONObject {
        return when (action) {
            is RecoveryAction.RetryMission -> JSONObject()
                .put("type", "RETRY_MISSION")
                .put("taskId", action.taskId)

            is RecoveryAction.RescheduleMission -> JSONObject()
                .put("type", "RESCHEDULE_MISSION")
                .put("taskId", action.taskId)

            is RecoveryAction.ReplaceMission -> JSONObject()
                .put("type", "REPLACE_MISSION")
                .put("taskId", action.taskId)

            is RecoveryAction.TakeRecoveryBreak -> JSONObject()
                .put("type", "TAKE_RECOVERY_BREAK")
                .put("minutes", action.minutes)

            RecoveryAction.EndDay -> JSONObject()
                .put("type", "END_DAY")
        }
    }

    private fun actionFromJson(
        json: JSONObject
    ): RecoveryAction {
        return when (json.getString("type")) {
            "RETRY_MISSION" ->
                RecoveryAction.RetryMission(
                    json.getInt("taskId")
                )

            "RESCHEDULE_MISSION" ->
                RecoveryAction.RescheduleMission(
                    json.getInt("taskId")
                )

            "REPLACE_MISSION" ->
                RecoveryAction.ReplaceMission(
                    json.getInt("taskId")
                )

            "TAKE_RECOVERY_BREAK" ->
                RecoveryAction.TakeRecoveryBreak(
                    json.getInt("minutes")
                )

            "END_DAY" ->
                RecoveryAction.EndDay

            else ->
                error("Unknown recovery action")
        }
    }

    private fun nullableInt(
        json: JSONObject,
        key: String
    ): Int? {
        return if (json.isNull(key)) {
            null
        } else {
            json.optInt(key)
        }
    }
}
