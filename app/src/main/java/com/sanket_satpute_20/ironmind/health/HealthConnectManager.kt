package com.sanket_satpute_20.ironmind.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    private val TAG = "HealthConnectManager"

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    fun getClient(): HealthConnectClient = healthConnectClient

    val permissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return try {
            val status = HealthConnectClient.getSdkStatus(context)
            Log.d(TAG, "SDK Status: $status")
            status == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Log.e(TAG, "Health Connect not available", e)
            false
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            val hasAll = granted.containsAll(permissions)
            Log.d(TAG, "Permissions check: $hasAll")
            hasAll
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check permissions", e)
            false
        }
    }

    /**
     * DIAGNOSTIC FUNCTION — Call this to verify if any data exists in the system.
     * Check this in Logcat with tag "HealthConnectManager"
     */
    suspend fun debugSleepData() {
        try {
            val endTime = Instant.now()
            val startTime = endTime.minus(30, ChronoUnit.DAYS)
            val zone = ZoneId.systemDefault()

            Log.d(TAG, "========== SLEEP DEBUG START ==========")
            Log.d(TAG, "Device timezone: $zone")
            Log.d(TAG, "Query range: $startTime to $endTime")

            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )

            val response = healthConnectClient.readRecords(request)
            Log.d(TAG, "Total SleepSessionRecords found: ${response.records.size}")

            if (response.records.isEmpty()) {
                Log.w(TAG, "⚠️ NO SLEEP RECORDS FOUND! Verify Fitbit sync is enabled.")
            }

            for ((index, record) in response.records.withIndex()) {
                val startLocal = record.startTime.atZone(zone)
                val durationMinutes = ChronoUnit.MINUTES.between(record.startTime, record.endTime)
                Log.d(TAG, "Record #${index + 1}: ${startLocal.toLocalDate()} | ${durationMinutes/60f}h | Source: ${record.metadata.dataOrigin.packageName}")
            }
            Log.d(TAG, "========== SLEEP DEBUG END ==========")
        } catch (e: Exception) {
            Log.e(TAG, "Debug sleep data failed", e)
        }
    }

    suspend fun getSleepDataForLastWeek(): Map<String, Float> {
        val result = mutableMapOf<String, Float>()
        try {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val endTime = today.plusDays(1).atStartOfDay(zone).toInstant()
            val startTime = today.minusDays(10).atStartOfDay(zone).toInstant()

            Log.d(TAG, "Querying sleep from $startTime to $endTime")

            val sessionRequest = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )

            val sessionResponse = healthConnectClient.readRecords(sessionRequest)
            
            for (record in sessionResponse.records) {
                val wakeUpDate = record.endTime.atZone(zone).toLocalDate().toString()
                val sleepHours = calculateActualSleepHours(record)
                result[wakeUpDate] = (result[wakeUpDate] ?: 0f) + sleepHours
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading sleep data", e)
        }
        return result
    }

    private fun calculateActualSleepHours(record: SleepSessionRecord): Float {
        // Exclude AWAKE stages if the data source provides them (common with Fitbit)
        return if (record.stages.isNotEmpty()) {
            var actualSleepMinutes = 0L
            for (stage in record.stages) {
                when (stage.stage) {
                    SleepSessionRecord.STAGE_TYPE_LIGHT,
                    SleepSessionRecord.STAGE_TYPE_DEEP,
                    SleepSessionRecord.STAGE_TYPE_REM,
                    SleepSessionRecord.STAGE_TYPE_SLEEPING -> {
                        actualSleepMinutes += ChronoUnit.MINUTES.between(stage.startTime, stage.endTime)
                    }
                }
            }
            actualSleepMinutes / 60f
        } else {
            ChronoUnit.MINUTES.between(record.startTime, record.endTime) / 60f
        }
    }
}
