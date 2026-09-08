package com.sanket_satpute_20.ironmind.mission

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RuntimeReconciliationScheduler {

    private const val UNIQUE_WORK_NAME = "ironmind_runtime_reconciliation"

    fun schedule(context: Context) {
        val request =
            PeriodicWorkRequestBuilder<RuntimeReconciliationWorker>(
                15,
                TimeUnit.MINUTES
            ).build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
