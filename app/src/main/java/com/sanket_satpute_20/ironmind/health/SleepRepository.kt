package com.sanket_satpute_20.ironmind.health

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SleepSource {
    HEALTH_CONNECT, // Covers Fitbit, Samsung Health, Google Fit (via HC)
    BEDTIME,        // Device inactivity estimation
    NONE
}

data class SleepDataResult(
    val data: Map<String, Float>,
    val source: SleepSource
)

class SleepRepository(private val context: Context) {

    private val healthManager = HealthConnectManager(context)
    private val bedtimeEstimator = DeviceSleepEstimator(context)
    private val TAG = "SleepRepository"

    suspend fun getSleepData(): SleepDataResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting tiered sleep data retrieval...")

        // Tier 1: Health Connect (Standard for wearables like Fitbit)
        try {
            val hcData = healthManager.getSleepDataForLastWeek()
            if (hcData.values.any { it > 0f }) {
                Log.d(TAG, "Tier 1 Success: Health Connect data found.")
                return@withContext SleepDataResult(hcData, SleepSource.HEALTH_CONNECT)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Health Connect check failed", e)
        }

        // Tier 2: Bedtime / Device Activity Estimation (No wearable fallback)
        Log.d(TAG, "Tier 1 Empty. Falling back to Tier 2: Bedtime Estimation...")
        try {
            val bedtimeData = bedtimeEstimator.estimateSleepForLastWeek()
            if (bedtimeData.values.any { it > 0f }) {
                Log.d(TAG, "Tier 2 Success: Bedtime patterns found.")
                return@withContext SleepDataResult(bedtimeData, SleepSource.BEDTIME)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bedtime estimation failed", e)
        }

        // Tier 3: No Data Found anywhere
        Log.d(TAG, "Tier 3: No sleep data discovered from any source.")
        return@withContext SleepDataResult(emptyMap(), SleepSource.NONE)
    }
}
