package com.sanket_satpute_20.ironmind.nightdecision

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sanket_satpute_20.ironmind.health.HealthConnectManager
import com.sanket_satpute_20.ironmind.data.PrefManager

class NightDecisionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val healthConnectManager = HealthConnectManager(applicationContext)
        val prefs = PrefManager.getInstance(applicationContext)
        val decisionManager = NightDecisionManager(applicationContext)

        if (!healthConnectManager.isHealthConnectAvailable() || !healthConnectManager.hasAllPermissions()) {
            return Result.success()
        }

        try {
            val sleepData = healthConnectManager.getSleepDataForLastWeek()
            if (sleepData.isNotEmpty()) {
                val avgSleep = sleepData.values.average()
                if (avgSleep < 6.0) {
                    // low sleep detected
                    prefs.routineType = "BALANCED" // Or Burnout
                }
            }

            if (decisionManager.shouldTriggerTonight()) {
                // Preemptively trigger if they are lacking sleep? Or just update state
            }
            
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}
