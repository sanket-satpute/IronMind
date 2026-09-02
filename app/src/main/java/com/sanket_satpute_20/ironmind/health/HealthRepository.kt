package com.sanket_satpute_20.ironmind.health

import android.util.Log
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Repository for fetching health data from Health Connect.
 */
class HealthRepository(private val healthConnectManager: HealthConnectManager) {

    /**
     * Fetches sleep sessions for the last X days.
     */
    suspend fun getSleepSessions(days: Int): List<SleepSessionRecord> = withContext(Dispatchers.IO) {
        val client = healthConnectManager.getClient()
        val endTime = Instant.now()
        val startTime = endTime.minus(days.toLong(), ChronoUnit.DAYS)
        
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        try {
            val response = client.readRecords(request)
            Log.d("HealthRepository", "Successfully read ${response.records.size} sleep records")
            return@withContext response.records
        } catch (e: Exception) {
            Log.e("HealthRepository", "Error reading sleep records from Health Connect", e)
            return@withContext emptyList()
        }
    }
}
