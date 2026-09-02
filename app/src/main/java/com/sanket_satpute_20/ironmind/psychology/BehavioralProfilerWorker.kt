package com.sanket_satpute_20.ironmind.psychology

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sanket_satpute_20.ironmind.health.HealthConnectManager
import java.util.Calendar

class BehavioralProfilerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ProfilerWorker", "Starting behavioral profiling...")
        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val healthManager = HealthConnectManager(applicationContext)
        
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis
        
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        if (usageStatsList.isNullOrEmpty()) {
            Log.w("ProfilerWorker", "No usage stats available. Permission might be missing.")
            return Result.failure()
        }
        
        var totalScreenTime = 0L
        var totalSocialMediaTime = 0L
        var totalUnlocks = 0 // UsageEvents is required for unlocks, simplifying for now
        
        val distractionApps = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.facebook.katana"
        )
        
        for (usageStats in usageStatsList) {
            totalScreenTime += usageStats.totalTimeInForeground
            if (distractionApps.contains(usageStats.packageName)) {
                totalSocialMediaTime += usageStats.totalTimeInForeground
            }
        }
        
        val totalScreenTimeMin = totalScreenTime / (1000 * 60)
        val totalSocialTimeMin = totalSocialMediaTime / (1000 * 60)
        
        // Try fetching sleep
        var sleepTimeMin = -1L
        if (healthManager.isHealthConnectAvailable() && healthManager.hasAllPermissions()) {
            val sleepData = healthManager.getSleepDataForLastWeek()
            // We just need yesterday's data. 
            // In a real app we'd map it perfectly to the day, here we just sum values for today if they exist.
            // Simplified: we'll take the most recent sleep duration.
            if (sleepData.isNotEmpty()) {
                 val lastSleepEntry = sleepData.entries.lastOrNull()
                 if (lastSleepEntry != null) {
                     sleepTimeMin = (lastSleepEntry.value * 60).toLong() // Convert hours to mins
                 }
            }
        }
        
        val prefs = applicationContext.getSharedPreferences(PersonaAnalysisEngine.PREFS_NAME, Context.MODE_PRIVATE)
        val currentDayIndex = prefs.getInt(PersonaAnalysisEngine.KEY_DAYS_TRACKED, 0)
        
        prefs.edit().apply {
            putLong("day_${currentDayIndex}_total_time", totalScreenTimeMin)
            putLong("day_${currentDayIndex}_social_time", totalSocialTimeMin)
            putLong("day_${currentDayIndex}_sleep_time", sleepTimeMin)
            putInt("day_${currentDayIndex}_unlocks", totalUnlocks)
            putInt(PersonaAnalysisEngine.KEY_DAYS_TRACKED, currentDayIndex + 1)
            apply()
        }
        
        val newDaysTracked = currentDayIndex + 1
        if (newDaysTracked >= 3) {
            Log.d("ProfilerWorker", "Calibration complete. Ready for analysis.")
            // Wait for user to open app, or dispatch a notification.
        }
        
        return Result.success()
    }
}
