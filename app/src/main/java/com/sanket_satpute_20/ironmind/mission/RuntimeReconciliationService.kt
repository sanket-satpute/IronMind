package com.sanket_satpute_20.ironmind.mission

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.data.TaskEvent
import com.sanket_satpute_20.ironmind.failure.FailureEvidenceFactory
import com.sanket_satpute_20.ironmind.failure.FailureService
import com.sanket_satpute_20.ironmind.focus.FocusSessionService
import com.sanket_satpute_20.ironmind.focus.PomodoroEngine
import com.sanket_satpute_20.ironmind.focus.WorkLockManager
import com.sanket_satpute_20.ironmind.protection.RuntimePolicyController
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Repairs inconsistencies between durable mission state and the runtime execution environment.
 *
 * The database is authoritative about lifecycle.
 * Persisted runtime state is authoritative about whether the execution environment can
 * be reconstructed after process death / restart.
 *
 * This service does not create user-facing UI.
 */
class RuntimeReconciliationService(context: Context) {

    private val appContext = context.applicationContext
    private val db = IronMindDatabase.getDatabase(appContext)
    private val prefs = PrefManager.getInstance(appContext)

    private val workLockManager = WorkLockManager(appContext)
    private val pomodoroEngine = PomodoroEngine(appContext)
    private val runtimePolicyController = RuntimePolicyController(appContext)
    private val failureService = FailureService.create(appContext)

    suspend fun reconcile(
        now: Long = System.currentTimeMillis()
    ): ReconciliationReport = mutex.withLock {

        var report = ReconciliationReport()

        runCatching {
            val activeTasks = db.taskDao().getInProgressTasks()

            if (activeTasks.isEmpty()) {
                reconcileOrphanedRuntime()
                return@runCatching
            }

            /*
             * IronMind should have at most one mission actively executing.
             * If bad state contains multiple active missions, the oldest started mission
             * wins deterministic ownership. The others are treated as interruptions.
             */
            val orderedTasks = activeTasks.sortedWith(
                compareBy<Task> { it.startedAt ?: Long.MAX_VALUE }
                    .thenBy { it.id }
            )

            val owner = orderedTasks.first()

            for (task in orderedTasks) {
                if (task.id != owner.id) {
                    resolveSystemInterruption(
                        task = task,
                        now = now,
                        reason = "MULTIPLE_ACTIVE_MISSIONS"
                    )
                    report = report.copy(
                        interruptedCount = report.interruptedCount + 1
                    )
                }
            }

            val currentOwner = db.taskDao().getTaskById(owner.id)

            if (currentOwner == null || !currentOwner.isInProgress) {
                reconcileOrphanedRuntime()
                return@runCatching
            }

            val expiration = resolveTaskEndMillis(
                date = currentOwner.date,
                endTime = currentOwner.endTime
            )

            if (expiration != null && expiration <= now) {
                resolveSystemInterruption(
                    task = currentOwner,
                    now = now,
                    reason = "MISSION_WINDOW_EXPIRED_WHILE_IN_PROGRESS"
                )

                report = report.copy(
                    expiredCount = report.expiredCount + 1
                )

                reconcileOrphanedRuntime()
                return@runCatching
            }

            if (isRuntimeOwnedBy(currentOwner)) {
                /*
                 * Runtime survived strongly enough to reconstruct ownership.
                 * Re-publish runtime policy after process restart so enforcement
                 * components can reload their current policy.
                 */
                runtimePolicyController.activateForMission(
                    task = currentOwner,
                    allowedPackages = prefs.activeMissionContextApps
                )

                restartFocusRuntimeIfRequired(currentOwner)

                report = report.copy(
                    rehydratedCount = report.rehydratedCount + 1
                )
            } else {
                resolveSystemInterruption(
                    task = currentOwner,
                    now = now,
                    reason = "MISSION_RUNTIME_MISSING"
                )

                report = report.copy(
                    interruptedCount = report.interruptedCount + 1
                )

                reconcileOrphanedRuntime()
            }
        }.onFailure {
            Log.e(TAG, "Runtime reconciliation failed", it)
            report = report.copy(
                errorCount = report.errorCount + 1
            )
        }

        report
    }

    /**
     * Determines whether the persisted runtime still belongs to this task.
     *
     * We intentionally use more than one signal:
     * - WorkLock task ownership
     * - Pomodoro task ownership
     * - active-task context
     *
     * A mission with protection features disabled can still be valid if its active-task
     * context survived.
     */
    private fun isRuntimeOwnedBy(task: Task): Boolean {
        val activeTaskContextMatches =
            prefs.activeTaskName == task.name &&
                prefs.activeTaskDate == task.date &&
                prefs.activeTaskStartTime == task.startTime &&
                prefs.activeTaskEndTime == task.endTime

        val workLockMatches =
            prefs.workLockActive &&
                prefs.workLockTaskId == task.id &&
                workLockManager.isActive()

        val pomodoroMatches =
            prefs.pomodoroActive &&
                prefs.pomodoroTaskId == task.id &&
                pomodoroEngine.isActive()

        return activeTaskContextMatches || workLockMatches || pomodoroMatches
    }

    /**
     * Reconstruct FocusSessionService state after a process restart if the mission's
     * persisted configuration indicates that focus execution is expected.
     *
     * We do not invent a new Pomodoro session. PomodoroEngine already persists its
     * exact session state and can continue from it.
     */
    private fun restartFocusRuntimeIfRequired(task: Task) {
        if (task.focusModeEnabled) {
            runCatching {
                FocusSessionService.start(
                    appContext,
                    task.name
                )
            }.onFailure {
                Log.w(
                    TAG,
                    "Could not rehydrate FocusSessionService for task ${task.id}",
                    it
                )
            }
        }
    }

    /**
     * Resolves a mission interruption through durable lifecycle state plus system-failure
     * evidence. This intentionally does NOT count as USER failure.
     */
    private suspend fun resolveSystemInterruption(
        task: Task,
        now: Long,
        reason: String
    ) {
        val latestTask = db.taskDao().getTaskById(task.id)
            ?: return

        if (!latestTask.isInProgress || latestTask.isCompleted || latestTask.isSkipped) {
            return
        }

        val updatedTask = latestTask.copy(
            isSkipped = true,
            isCompleted = false,
            isDeferred = false,
            isInProgress = false,
            skippedAt = now,
            skipReason = "SYSTEM_INTERRUPTION:$reason",
            lastModified = now,
            syncStatus = "PENDING"
        )

        /*
         * First repair durable lifecycle state.
         * Then write failure evidence.
         *
         * The failure evidence is outside this transaction because FailureService owns
         * its own persistence abstraction.
         */
        db.withTransaction {
            db.taskDao().updateTask(updatedTask)

            db.taskEventDao().insert(
                TaskEvent(
                    taskId = updatedTask.id,
                    taskName = updatedTask.name,
                    date = updatedTask.date,
                    eventType = "SYSTEM_INTERRUPTION",
                    timestamp = now,
                    oldStartTime = latestTask.startTime,
                    oldEndTime = latestTask.endTime,
                    newStartTime = updatedTask.startTime,
                    newEndTime = updatedTask.endTime,
                    focusScoreSnapshot = updatedTask.focusScore,
                    reason = reason
                )
            )
        }

        recordSystemFailureSafely(
            task = updatedTask,
            now = now,
            reason = reason
        )

        clearOwnedRuntime(taskId = task.id)
    }

    private suspend fun recordSystemFailureSafely(
        task: Task,
        now: Long,
        reason: String
    ) {
        runCatching {
            val evidence = FailureEvidenceFactory.systemInterrupted(
                task = task,
                timestamp = now,
                reason = reason,
                protectionWasActive =
                    prefs.workLockActive && prefs.workLockTaskId == task.id,
                pomodoroSummary = null
            )

            /*
             * FailureService is synchronous at the call site because reconciliation
             * itself is already suspended.
             */
            failureService.recordFailure(evidence)
        }.onFailure {
            Log.e(
                TAG,
                "Failed to persist system interruption evidence for task ${task.id}",
                it
            )
        }
    }

    /**
     * Clears runtime state that no longer points to a live in-progress mission.
     */
    private suspend fun reconcileOrphanedRuntime() {
        val activeTaskId = when {
            prefs.workLockActive && prefs.workLockTaskId > 0 ->
                prefs.workLockTaskId

            prefs.pomodoroActive && prefs.pomodoroTaskId > 0 ->
                prefs.pomodoroTaskId

            prefs.activeTaskName.isNotBlank() ->
                -2

            else -> null
        }

        if (activeTaskId == null) {
            return
        }

        val runtimeTask = if (activeTaskId > 0) {
            db.taskDao().getTaskById(activeTaskId)
        } else {
            null
        }

        val stillValid = runtimeTask?.let { task ->
            task.isInProgress && !task.isCompleted && !task.isSkipped
        } ?: false

        if (!stillValid) {
            clearOwnedRuntime(
                taskId = runtimeTask?.id ?: activeTaskId
            )

            runCatching {
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = runtimeTask?.id ?: 0,
                        taskName = runtimeTask?.name ?: "UNKNOWN",
                        date = runtimeTask?.date ?: LocalDate.now().toString(),
                        eventType = "ORPHAN_RUNTIME_CLEARED",
                        timestamp = System.currentTimeMillis(),
                        reason = "RUNTIME_REFERENCED_NO_ACTIVE_MISSION"
                    )
                )
            }.onFailure {
                Log.w(TAG, "Unable to write orphan-runtime event", it)
            }
        }
    }

    /**
     * Cleanup is ownership-gated so reconciliation cannot destroy a runtime session
     * that belongs to another mission.
     */
    private fun clearOwnedRuntime(taskId: Int) {

        if (taskId > 0 &&
            prefs.workLockTaskId == taskId &&
            prefs.workLockActive
        ) {
            runCatching {
                workLockManager.endForCompletion()
            }.onFailure {
                Log.w(TAG, "Failed to clear WorkLock for task $taskId", it)
            }
        }

        if (taskId > 0 &&
            prefs.pomodoroTaskId == taskId &&
            prefs.pomodoroActive
        ) {
            runCatching {
                pomodoroEngine.clear()
            }.onFailure {
                Log.w(TAG, "Failed to clear Pomodoro for task $taskId", it)
            }
        }

        val activeContextBelongsToTask =
            if (taskId > 0) {
                prefs.activeTaskName.isNotBlank()
            } else {
                false
            }

        if (activeContextBelongsToTask) {
            runCatching {
                prefs.clearActiveMissionContextApps()
                prefs.activeTaskName = ""
                prefs.activeTaskStartTime = ""
                prefs.activeTaskEndTime = ""
                prefs.activeTaskDate = LocalDate.now().toString()
            }.onFailure {
                Log.w(TAG, "Failed to clear active mission context", it)
            }
        }

        runCatching {
            runtimePolicyController.refresh()
        }.onFailure {
            Log.w(TAG, "Failed to refresh runtime policy after reconciliation", it)
        }
    }

    private fun resolveTaskEndMillis(
        date: String,
        endTime: String
    ): Long? {
        val localDate = runCatching {
            LocalDate.parse(date)
        }.getOrNull() ?: return null

        val localTime = parseTime(endTime) ?: return null

        return LocalDateTime.of(localDate, localTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun parseTime(value: String): LocalTime? {
        val formats = listOf(
            "HH:mm",
            "H:mm",
            "hh:mm a",
            "h:mm a"
        )

        return formats.firstNotNullOfOrNull { pattern ->
            runCatching {
                LocalTime.parse(
                    value.trim().uppercase(Locale.US),
                    DateTimeFormatter.ofPattern(pattern, Locale.US)
                )
            }.getOrNull()
        }
    }

    data class ReconciliationReport(
        val rehydratedCount: Int = 0,
        val interruptedCount: Int = 0,
        val expiredCount: Int = 0,
        val errorCount: Int = 0
    )

    companion object {
        private const val TAG = "RuntimeReconcile"
        private val mutex = Mutex()
    }
}
