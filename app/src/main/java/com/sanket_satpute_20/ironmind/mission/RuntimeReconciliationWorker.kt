package com.sanket_satpute_20.ironmind.mission

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Periodically reconciles durable mission state with the runtime execution environment.
 *
 * WorkManager provides eventual background execution; reconciliation itself remains
 * deterministic and idempotent.
 */
class RuntimeReconciliationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            RuntimeReconciliationService(applicationContext)
                .reconcile()

            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
