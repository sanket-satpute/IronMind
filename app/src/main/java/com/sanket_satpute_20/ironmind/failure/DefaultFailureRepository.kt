package com.sanket_satpute_20.ironmind.failure

import org.json.JSONArray
import org.json.JSONObject

class DefaultFailureRepository(
    private val storage: FailureStorage
) : FailureRepository {

    override fun getLatest(): FailureEvidence? {
        val raw = storage.latestJson
        if (raw.isBlank()) return null

        return runCatching {
            fromJson(JSONObject(raw))
        }.getOrNull()
    }

    override fun countFailures(taskId: Int, date: String): Int {
        val history = readHistory()

        return history.count {
            it.taskId == taskId && it.date == date
        }
    }

    override fun contains(
        taskId: Int,
        failureType: FailureType,
        timestamp: Long
    ): Boolean {
        return readHistory().any {
            it.taskId == taskId &&
                it.failureType == failureType &&
                it.timestamp == timestamp
        }
    }

    override fun record(evidence: FailureEvidence) {
        if (contains(
                taskId = evidence.taskId,
                failureType = evidence.failureType,
                timestamp = evidence.timestamp
            )
        ) {
            return
        }

        val updatedHistory = buildList {
            addAll(readHistory())
            add(evidence)
        }

        /*
         * Bound the preference payload.
         *
         * This is intentionally not an unlimited analytics store.
         * Room history can be introduced later when we need full historical analysis.
         */
        val boundedHistory = updatedHistory
            .sortedByDescending { it.timestamp }
            .take(MAX_HISTORY_ENTRIES)

        storage.latestJson = JSONObject()
            .apply {
                put("taskId", evidence.taskId)
                put("taskName", evidence.taskName)
                put("date", evidence.date)
                put("failureType", evidence.failureType.name)
                put("timestamp", evidence.timestamp)
                put("reason", evidence.reason)
                put("protectionWasActive", evidence.protectionWasActive)
                putNullableInt("focusMinutes", evidence.focusMinutes)
                putNullableInt("breachCount", evidence.breachCount)
                putNullableInt("focusScore", evidence.focusScore)
            }
            .toString()

        storage.historyJson = JSONArray()
            .apply {
                boundedHistory.forEach { put(toJson(it)) }
            }
            .toString()
    }

    private fun readHistory(): List<FailureEvidence> {
        val raw = storage.historyJson
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)

            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue

                    runCatching {
                        add(fromJson(item))
                    }
                }
            }
        }.getOrElse {
            emptyList()
        }
    }

    private fun toJson(evidence: FailureEvidence): JSONObject {
        return JSONObject().apply {
            put("taskId", evidence.taskId)
            put("taskName", evidence.taskName)
            put("date", evidence.date)
            put("failureType", evidence.failureType.name)
            put("timestamp", evidence.timestamp)
            put("reason", evidence.reason)
            put("protectionWasActive", evidence.protectionWasActive)
            putNullableInt("focusMinutes", evidence.focusMinutes)
            putNullableInt("breachCount", evidence.breachCount)
            putNullableInt("focusScore", evidence.focusScore)
        }
    }

    private fun fromJson(json: JSONObject): FailureEvidence {
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
            focusMinutes = json.optNullableInt("focusMinutes"),
            breachCount = json.optNullableInt("breachCount"),
            focusScore = json.optNullableInt("focusScore")
        )
    }

    private fun JSONObject.putNullableInt(
        key: String,
        value: Int?
    ) {
        if (value == null) {
            put(key, JSONObject.NULL)
        } else {
            put(key, value)
        }
    }

    private fun JSONObject.optNullableInt(
        key: String
    ): Int? {
        return if (isNull(key)) {
            null
        } else {
            optInt(key)
        }
    }

    companion object {
        private const val MAX_HISTORY_ENTRIES = 100
    }
}
