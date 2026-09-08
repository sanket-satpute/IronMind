package com.sanket_satpute_20.ironmind.mission

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.sanket_satpute_20.ironmind.alarm.AlarmScheduler
import com.sanket_satpute_20.ironmind.data.FocusSession
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.data.TaskEvent
import com.sanket_satpute_20.ironmind.focus.EarnedUnlockManager
import com.sanket_satpute_20.ironmind.focus.FocusSessionService
import com.sanket_satpute_20.ironmind.focus.PomodoroEngine
import com.sanket_satpute_20.ironmind.focus.WorkLockManager
import com.sanket_satpute_20.ironmind.protection.RuntimePolicyController
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Outcome of a Pomodoro session that was owned by the task a lifecycle operation resolved. */
data class PomodoroSummary(
    val taskName: String,
    val outcome: String,
    val intervalsCompleted: Int,
    val focusMinutes: Int,
    val breachCount: Int,
    val focusScore: Int
)

/**
 * Canonical result of a mission lifecycle operation. Callers branch on this instead of
 * inspecting Task flags directly.
 */
sealed interface MissionExecutionResult {

    data class Started(val task: Task, val startedAt: Long) : MissionExecutionResult

    data class AlreadyActive(val task: Task) : MissionExecutionResult

    /** A completion transition genuinely occurred as a result of this call. */
    data class Completed(val task: Task, val completedAt: Long, val pomodoroSummary: PomodoroSummary? = null) :
        MissionExecutionResult

    /** The task was already completed before this call; no new transition/side effects occurred. */
    data class AlreadyCompleted(val task: Task, val completedAt: Long) : MissionExecutionResult

    /** A skip transition genuinely occurred as a result of this call. */
    data class Skipped(val task: Task, val timestamp: Long, val pomodoroSummary: PomodoroSummary? = null) :
        MissionExecutionResult

    /** The task was already skipped before this call; no new transition/side effects occurred. */
    data class AlreadySkipped(val task: Task, val timestamp: Long) : MissionExecutionResult

    /** A defer transition genuinely occurred as a result of this call. */
    data class Deferred(val task: Task, val timestamp: Long) : MissionExecutionResult

    /** The task was already deferred before this call; no new transition/side effects occurred. */
    data class AlreadyDeferred(val task: Task, val timestamp: Long) : MissionExecutionResult

    data class InvalidState(val task: Task, val reason: String) : MissionExecutionResult

    data class InvalidWindow(val task: Task, val reason: String) : MissionExecutionResult

    data class MissingTask(val taskId: Int) : MissionExecutionResult

    data class Failed(val reason: String, val cause: Throwable? = null) : MissionExecutionResult
}

/**
 * Canonical domain/service entry point for mission execution.
 *
 * This is the single owner of mission lifecycle transitions (start/complete/skip/defer),
 * the Task/TaskEvent mutation they cause, and the execution cleanup (WorkLock, Pomodoro,
 * FocusSession, active-mission runtime state, RuntimePolicy) that must accompany them.
 * It does not replace [WorkLockManager] or [PomodoroEngine] - it orchestrates them.
 *
 * UI concerns (animations, sounds, haptics, dialogs, summary rendering) stay in the callers
 * ([com.sanket_satpute_20.ironmind.focus.WorkStartActivity], [com.sanket_satpute_20.ironmind.focus.WorkLockActivity],
 * [com.sanket_satpute_20.ironmind.home.TaskViewModel]).
 */
class MissionExecutionService(context: Context) {

    private val appContext = context.applicationContext
    private val db = IronMindDatabase.getDatabase(appContext)
    private val prefs = PrefManager.getInstance(appContext)
    private val workLockManager = WorkLockManager(appContext)
    private val pomodoroEngine = PomodoroEngine(appContext)
    private val runtimePolicyController = RuntimePolicyController(appContext)
    private val earnedUnlockManager = EarnedUnlockManager(appContext)

    suspend fun startMission(
        taskId: Int,
        allowedPackages: Set<String> = emptySet()
    ): MissionExecutionResult = mutex.withLock {
        runCatching {
            val task = db.taskDao().getTaskById(taskId) ?: return@withLock MissionExecutionResult.MissingTask(taskId)

            if (task.isCompleted) return@withLock MissionExecutionResult.InvalidState(task, "Task already completed")
            if (task.isSkipped) return@withLock MissionExecutionResult.InvalidState(task, "Task already skipped")
            if (task.isInProgress) return@withLock MissionExecutionResult.AlreadyActive(task)

            val now = System.currentTimeMillis()
            val taskEndAt = resolveTaskEndMillis(task.date, task.endTime)
            if (taskEndAt == null || taskEndAt <= now) {
                return@withLock MissionExecutionResult.InvalidWindow(
                    task,
                    "This task window has already ended. Reschedule it before starting."
                )
            }

            val updatedTask = task.copy(
                isDeferred = false,
                isInProgress = true,
                startedAt = task.startedAt ?: now,
                lastModified = now,
                syncStatus = "PENDING"
            )

            // Durable state first: Task + STARTED event + FocusSession commit atomically.
            db.withTransaction {
                db.taskDao().updateTask(updatedTask)
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = updatedTask.id,
                        taskName = updatedTask.name,
                        date = updatedTask.date,
                        eventType = "STARTED",
                        timestamp = now,
                        newStartTime = updatedTask.startTime,
                        newEndTime = updatedTask.endTime,
                        focusScoreSnapshot = updatedTask.focusScore,
                        reason = "MISSION_BRIEFING_START"
                    )
                )
                db.focusSessionDao().insert(
                    FocusSession(
                        taskId = updatedTask.id,
                        taskName = updatedTask.name,
                        date = updatedTask.date,
                        startTimestamp = now,
                        plannedDurationMinutes = plannedDurationMinutes(updatedTask.startTime, updatedTask.endTime)
                    )
                )
            }

            // Runtime/execution environment second. If this throws, the durable ACTIVE
            // transition above must not be left stranded with no protection behind it.
            runCatching {
                prefs.activeMissionContextApps = allowedPackages
                prefs.activeTaskName = updatedTask.name
                prefs.activeTaskStartTime = updatedTask.startTime
                prefs.activeTaskEndTime = updatedTask.endTime
                prefs.activeTaskDate = updatedTask.date

                val workLockArmed = workLockManager.startForTask(
                    taskId = updatedTask.id,
                    taskName = updatedTask.name,
                    taskDate = updatedTask.date,
                    taskEndTime = updatedTask.endTime
                )
                val pomodoroStarted = pomodoroEngine.startForTask(updatedTask)

                if (workLockArmed) {
                    db.taskEventDao().insert(
                        TaskEvent(
                            taskId = updatedTask.id,
                            taskName = updatedTask.name,
                            date = updatedTask.date,
                            eventType = "WORK_LOCK_STARTED",
                            timestamp = now,
                            oldStartTime = updatedTask.startTime,
                            oldEndTime = updatedTask.endTime,
                            newStartTime = updatedTask.startTime,
                            newEndTime = updatedTask.endTime,
                            focusScoreSnapshot = updatedTask.focusScore,
                            reason = "SPRING_PROTOCOL_ARMED"
                        )
                    )
                }
                if (pomodoroStarted) {
                    db.taskEventDao().insert(
                        TaskEvent(
                            taskId = updatedTask.id,
                            taskName = updatedTask.name,
                            date = updatedTask.date,
                            eventType = "POMODORO_STARTED",
                            timestamp = now,
                            oldStartTime = updatedTask.startTime,
                            oldEndTime = updatedTask.endTime,
                            newStartTime = updatedTask.startTime,
                            newEndTime = updatedTask.endTime,
                            focusScoreSnapshot = updatedTask.focusScore,
                            reason = "MISSION_BRIEFING_START"
                        )
                    )
                }

                FocusSessionService.start(appContext, updatedTask.name)
                runtimePolicyController.activateForMission(updatedTask, allowedPackages)
            }.getOrElse { runtimeError ->
                rollbackFailedStart(task, updatedTask, now)
                return@withLock MissionExecutionResult.Failed("startMission runtime setup failed", runtimeError)
            }

            MissionExecutionResult.Started(updatedTask, now)
        }.getOrElse { MissionExecutionResult.Failed("startMission failed", it) }
    }

    /** Reverts a mission whose durable ACTIVE transition committed but whose execution
     * environment (WorkLock/Pomodoro/FocusSessionService/RuntimePolicy) failed to establish.
     * The mission is restored to its pre-start state rather than left "active" with no
     * protection behind it (Strategy A: rollback over silent zombie state). */
    private suspend fun rollbackFailedStart(originalTask: Task, failedStartTask: Task, now: Long) {
        runCatching {
            db.withTransaction {
                db.taskDao().updateTask(originalTask.copy(lastModified = now, syncStatus = "PENDING"))
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = failedStartTask.id,
                        taskName = failedStartTask.name,
                        date = failedStartTask.date,
                        eventType = "START_ROLLED_BACK",
                        timestamp = now,
                        reason = "MISSION_START_RUNTIME_SETUP_FAILED"
                    )
                )
            }
            prefs.clearActiveMissionContextApps()
            if (isWorkLockOwnedBy(failedStartTask.id)) prefs.clearWorkLock()
            if (isPomodoroOwnedBy(failedStartTask.id)) prefs.clearPomodoro()
        }.onFailure { Log.e(TAG, "rollbackFailedStart failed for task ${failedStartTask.id}", it) }
    }

    suspend fun completeMission(
        taskId: Int,
        completionSource: String,
        eventReason: String = completionSource
    ): MissionExecutionResult = mutex.withLock {
        runCatching {
            val task = db.taskDao().getTaskById(taskId) ?: return@withLock MissionExecutionResult.MissingTask(taskId)

            if (task.isCompleted) {
                return@withLock MissionExecutionResult.AlreadyCompleted(task, task.completedAt ?: System.currentTimeMillis())
            }

            val now = System.currentTimeMillis()
            val updatedTask = task.copy(
                isCompleted = true,
                isSkipped = false,
                isDeferred = false,
                isInProgress = false,
                completedAt = now,
                completionSource = completionSource,
                lastModified = now,
                syncStatus = "PENDING"
            )

            db.withTransaction {
                db.taskDao().updateTask(updatedTask)
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = updatedTask.id,
                        taskName = updatedTask.name,
                        date = updatedTask.date,
                        eventType = "COMPLETED",
                        timestamp = now,
                        oldStartTime = task.startTime,
                        oldEndTime = task.endTime,
                        newStartTime = updatedTask.startTime,
                        newEndTime = updatedTask.endTime,
                        focusScoreSnapshot = updatedTask.focusScore,
                        reason = eventReason
                    )
                )
            }

            // Best-effort execution cleanup: the lifecycle transition above already committed,
            // so a cleanup failure must not undo a legitimate completion.
            val pomodoroSummary = runCatching {
                terminateOwnedExecution(updatedTask, outcome = "COMPLETE", now = now)
            }.getOrElse {
                Log.e(TAG, "completeMission cleanup failed for task $taskId", it)
                null
            }

            MissionExecutionResult.Completed(updatedTask, now, pomodoroSummary)
        }.getOrElse { MissionExecutionResult.Failed("completeMission failed", it) }
    }

    suspend fun skipMission(
        taskId: Int,
        skipReason: String,
        eventReason: String = skipReason
    ): MissionExecutionResult = mutex.withLock {
        runCatching {
            val task = db.taskDao().getTaskById(taskId) ?: return@withLock MissionExecutionResult.MissingTask(taskId)

            if (task.isSkipped) {
                return@withLock MissionExecutionResult.AlreadySkipped(task, task.skippedAt ?: System.currentTimeMillis())
            }

            val now = System.currentTimeMillis()
            val updatedTask = task.copy(
                isSkipped = true,
                isCompleted = false,
                isDeferred = false,
                isInProgress = false,
                skippedAt = now,
                skipReason = skipReason,
                lastModified = now,
                syncStatus = "PENDING"
            )

            db.withTransaction {
                db.taskDao().updateTask(updatedTask)
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = updatedTask.id,
                        taskName = updatedTask.name,
                        date = updatedTask.date,
                        eventType = "SKIPPED",
                        timestamp = now,
                        oldStartTime = task.startTime,
                        oldEndTime = task.endTime,
                        newStartTime = updatedTask.startTime,
                        newEndTime = updatedTask.endTime,
                        focusScoreSnapshot = updatedTask.focusScore,
                        reason = eventReason
                    )
                )
            }

            val pomodoroSummary = runCatching {
                terminateOwnedExecution(updatedTask, outcome = "BROKEN", now = now)
            }.getOrElse {
                Log.e(TAG, "skipMission cleanup failed for task $taskId", it)
                null
            }

            MissionExecutionResult.Skipped(updatedTask, now, pomodoroSummary)
        }.getOrElse { MissionExecutionResult.Failed("skipMission failed", it) }
    }

    suspend fun deferMission(
        taskId: Int,
        reason: String
    ): MissionExecutionResult = mutex.withLock {
        runCatching {
            val task = db.taskDao().getTaskById(taskId) ?: return@withLock MissionExecutionResult.MissingTask(taskId)

            if (task.isDeferred) {
                return@withLock MissionExecutionResult.AlreadyDeferred(task, task.deferredAt ?: System.currentTimeMillis())
            }

            val now = System.currentTimeMillis()
            val updatedTask = task.copy(
                isDeferred = true,
                isInProgress = false,
                deferredAt = now,
                lastModified = now,
                syncStatus = "PENDING"
            )

            db.withTransaction {
                db.taskDao().updateTask(updatedTask)
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = updatedTask.id,
                        taskName = updatedTask.name,
                        date = updatedTask.date,
                        eventType = "DEFERRED",
                        timestamp = now,
                        oldStartTime = task.startTime,
                        oldEndTime = task.endTime,
                        newStartTime = updatedTask.startTime,
                        newEndTime = updatedTask.endTime,
                        reason = reason
                    )
                )
            }

            runCatching {
                AlarmScheduler.cancelTaskAlarms(appContext, updatedTask.id, updatedTask.name)
                AlarmScheduler.scheduleTaskAlarms(appContext, updatedTask)
                // Defer should not leave contradictory state (Task deferred, WorkLock/Pomodoro
                // still active for it). No neutral WorkLockManager terminal state exists that
                // avoids implying success/penalty, so the owned runtime flags are cleared directly.
                if (isWorkLockOwnedBy(updatedTask.id)) prefs.clearWorkLock()
                if (isPomodoroOwnedBy(updatedTask.id)) prefs.clearPomodoro()
                clearActiveTaskWindowIfOwnedBy(updatedTask)
                runtimePolicyController.refresh()
            }.getOrElse {
                Log.e(TAG, "deferMission cleanup failed for task $taskId", it)
            }

            MissionExecutionResult.Deferred(updatedTask, now)
        }.getOrElse { MissionExecutionResult.Failed("deferMission failed", it) }
    }

    private fun isWorkLockOwnedBy(taskId: Int): Boolean =
        isOwnerOf(prefs.workLockActive, prefs.workLockTaskId, taskId)

    private fun isPomodoroOwnedBy(taskId: Int): Boolean =
        isOwnerOf(prefs.pomodoroActive, prefs.pomodoroTaskId, taskId)

    private fun isActiveTaskWindowOwnedBy(task: Task): Boolean =
        isActiveWindowOwnerOf(prefs.activeTaskName, prefs.activeTaskDate, task.name, task.date)

    private fun clearActiveTaskWindowIfOwnedBy(task: Task) {
        if (!isActiveTaskWindowOwnedBy(task)) return
        prefs.activeTaskName = ""
        prefs.activeTaskStartTime = ""
        prefs.activeTaskEndTime = ""
        prefs.activeTaskDate = LocalDate.now().toString()
        prefs.clearActiveMissionContextApps()
    }

    /**
     * Terminates WorkLock/Pomodoro/FocusSession/active-runtime-state ONLY if they are currently
     * owned by [task] (identity verified via workLockTaskId/pomodoroTaskId/activeTaskName+date,
     * never assumed), so completing/skipping one mission cannot clear a *different* mission's
     * protection. Returns the Pomodoro summary if a Pomodoro session owned by this task ended.
     */
    private suspend fun terminateOwnedExecution(task: Task, outcome: String, now: Long): PomodoroSummary? {
        val pomodoroSummary = if (isPomodoroOwnedBy(task.id)) {
            endPomodoroAndRecordEvent(task, outcome, now)
        } else {
            null
        }

        if (isWorkLockOwnedBy(task.id)) {
            when (outcome) {
                "COMPLETE" -> {
                    db.taskEventDao().insert(
                        TaskEvent(
                            taskId = task.id,
                            taskName = task.name,
                            date = task.date,
                            eventType = "WORK_LOCK_COMPLETED",
                            timestamp = now,
                            oldStartTime = task.startTime,
                            oldEndTime = task.endTime,
                            newStartTime = task.startTime,
                            newEndTime = task.endTime,
                            focusScoreSnapshot = task.focusScore,
                            reason = "SPRING_PROTOCOL_COMPLETE"
                        )
                    )
                    workLockManager.endForCompletion()
                }
                "BROKEN" -> {
                    db.taskEventDao().insert(
                        TaskEvent(
                            taskId = task.id,
                            taskName = task.name,
                            date = task.date,
                            eventType = "WORK_LOCK_BROKEN",
                            timestamp = now,
                            oldStartTime = task.startTime,
                            oldEndTime = task.endTime,
                            newStartTime = task.startTime,
                            newEndTime = task.endTime,
                            focusScoreSnapshot = task.focusScore,
                            reason = "SPRING_PROTOCOL_BROKEN"
                        )
                    )
                    workLockManager.breakMissionWithPenalty()
                }
            }
        }

        AlarmScheduler.cancelTaskAlarms(appContext, task.id, task.name)
        finalizeFocusSession(task.id, result = if (outcome == "COMPLETE") "COMPLETED" else "SKIPPED", completed = outcome == "COMPLETE")
        FocusSessionService.stop(appContext)
        clearActiveTaskWindowIfOwnedBy(task)
        earnedUnlockManager.syncTodayFromDatabase()
        runtimePolicyController.refresh()

        return pomodoroSummary
    }

    private suspend fun endPomodoroAndRecordEvent(task: Task, outcome: String, timestamp: Long): PomodoroSummary {
        val state = if (outcome == "COMPLETE") pomodoroEngine.completeSession() else pomodoroEngine.breakSession()
        val focusMinutes = state.completedWorkIntervals * state.preset.workMinutes
        val penalty = if (outcome == "COMPLETE") 0 else 15
        val focusScore = (100 - (state.breachCount * 20) - penalty).coerceIn(0, 100)
        val summary = PomodoroSummary(
            taskName = task.name,
            outcome = outcome,
            intervalsCompleted = state.completedWorkIntervals,
            focusMinutes = focusMinutes,
            breachCount = state.breachCount,
            focusScore = focusScore
        )
        val eventType = if (outcome == "COMPLETE") "POMODORO_COMPLETED" else "POMODORO_BROKEN"
        val reason = buildString {
            append("INTERVALS=").append(summary.intervalsCompleted)
            append(";MINUTES=").append(summary.focusMinutes)
            append(";BREACHES=").append(summary.breachCount)
            append(";SCORE=").append(summary.focusScore)
            append(";OUTCOME=").append(outcome)
        }
        db.taskEventDao().insert(
            TaskEvent(
                taskId = task.id,
                taskName = task.name,
                date = task.date,
                eventType = eventType,
                timestamp = timestamp,
                oldStartTime = task.startTime,
                oldEndTime = task.endTime,
                newStartTime = task.startTime,
                newEndTime = task.endTime,
                focusScoreSnapshot = task.focusScore,
                reason = reason
            )
        )
        return summary
    }

    private suspend fun finalizeFocusSession(taskId: Int, result: String, completed: Boolean) {
        val activeSession = db.focusSessionDao().getActiveSessionForTask(taskId) ?: return
        val endTimestamp = System.currentTimeMillis()
        val durationMinutes = ((endTimestamp - activeSession.startTimestamp) / 60_000L).coerceAtLeast(0L).toInt()
        db.focusSessionDao().update(
            activeSession.copy(
                endTimestamp = endTimestamp,
                actualDurationMinutes = durationMinutes,
                result = result,
                completed = completed,
                usedResetProtocol = prefs.emergencyValveCooldownUntil > activeSession.startTimestamp,
                cooldownUsed = prefs.emergencyValveCooldownActive,
                lastModified = endTimestamp,
                syncStatus = "PENDING"
            )
        )
    }

    companion object {
        private const val TAG = "MissionExecutionService"

        // Shared across instances: mission state transitions are infrequent and must be
        // serialized to guarantee the idempotency rules above hold under concurrent calls.
        private val mutex = Mutex()
    }
}

// Pure, dependency-free helpers kept at file scope (not class members) so they can be
// unit tested directly without instantiating MissionExecutionService's Context/Room/prefs deps.

/**
 * Identity check used before clearing WorkLock/Pomodoro state: a lifecycle transition for
 * [taskId] may only clear that runtime flag if [taskId] is genuinely the one that owns it
 * (mission A completing must never clear mission B's active WorkLock/Pomodoro).
 */
internal fun isOwnerOf(activeFlag: Boolean, activeOwnerTaskId: Int, taskId: Int): Boolean =
    activeFlag && activeOwnerTaskId == taskId

/** Same identity guarantee as [isOwnerOf], but for the name+date based active-task window
 * (PrefManager has no standalone active-task-id field to compare against). */
internal fun isActiveWindowOwnerOf(activeName: String, activeDate: String, taskName: String, taskDate: String): Boolean =
    activeName.isNotBlank() && activeName == taskName && activeDate == taskDate

internal fun plannedDurationMinutes(startTime: String, endTime: String): Int {
    val start = parseTime(startTime) ?: return 0
    val end = parseTime(endTime) ?: return 0
    return Duration.between(start, end).toMinutes().coerceAtLeast(0).toInt()
}

internal fun resolveTaskEndMillis(taskDate: String, taskEndTime: String): Long? {
    val date = runCatching { LocalDate.parse(taskDate) }.getOrNull() ?: return null
    val end = parseTime(taskEndTime) ?: return null
    return LocalDateTime.of(date, end)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

internal fun parseTime(timeStr: String): LocalTime? {
    val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
    for (format in formats) {
        runCatching {
            return LocalTime.parse(timeStr.uppercase(), DateTimeFormatter.ofPattern(format, Locale.US))
        }
    }
    return null
}
