package com.sanket_satpute_20.ironmind.mission

import android.content.Context
import com.sanket_satpute_20.ironmind.alarm.AlarmScheduler
import com.sanket_satpute_20.ironmind.data.FocusSession
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.data.TaskEvent
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

/**
 * Canonical result of a mission lifecycle operation. Callers branch on this instead of
 * inspecting Task flags directly.
 */
sealed interface MissionExecutionResult {

    data class Started(val task: Task, val startedAt: Long) : MissionExecutionResult

    data class AlreadyActive(val task: Task) : MissionExecutionResult

    data class Completed(val task: Task, val completedAt: Long) : MissionExecutionResult

    data class Skipped(val task: Task, val timestamp: Long) : MissionExecutionResult

    data class Deferred(val task: Task, val timestamp: Long) : MissionExecutionResult

    data class InvalidState(val task: Task, val reason: String) : MissionExecutionResult

    data class InvalidWindow(val task: Task, val reason: String) : MissionExecutionResult

    data class MissingTask(val taskId: Int) : MissionExecutionResult

    data class Failed(val reason: String, val cause: Throwable? = null) : MissionExecutionResult
}

/**
 * Canonical domain/service entry point for mission execution.
 *
 * This normalizes logic that used to be duplicated/inlined across [com.sanket_satpute_20.ironmind.focus.WorkStartActivity],
 * [com.sanket_satpute_20.ironmind.focus.WorkLockActivity] and [com.sanket_satpute_20.ironmind.home.TaskViewModel].
 * It does not replace [WorkLockManager] or [PomodoroEngine] - it orchestrates them, exactly as
 * the callers previously did inline.
 */
class MissionExecutionService(context: Context) {

    private val appContext = context.applicationContext
    private val db = IronMindDatabase.getDatabase(appContext)
    private val prefs = PrefManager.getInstance(appContext)
    private val workLockManager = WorkLockManager(appContext)
    private val pomodoroEngine = PomodoroEngine(appContext)
    private val runtimePolicyController = RuntimePolicyController(appContext)

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

            prefs.activeMissionContextApps = allowedPackages
            prefs.activeTaskName = task.name
            prefs.activeTaskStartTime = task.startTime
            prefs.activeTaskEndTime = task.endTime
            prefs.activeTaskDate = task.date

            val updatedTask = task.copy(
                isDeferred = false,
                isInProgress = true,
                startedAt = task.startedAt ?: now,
                lastModified = now,
                syncStatus = "PENDING"
            )
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

            MissionExecutionResult.Started(updatedTask, now)
        }.getOrElse { MissionExecutionResult.Failed("startMission failed", it) }
    }

    suspend fun completeMission(
        taskId: Int,
        completionSource: String,
        eventReason: String = completionSource
    ): MissionExecutionResult = mutex.withLock {
        runCatching {
            val task = db.taskDao().getTaskById(taskId) ?: return@withLock MissionExecutionResult.MissingTask(taskId)

            if (task.isCompleted) {
                return@withLock MissionExecutionResult.Completed(task, task.completedAt ?: System.currentTimeMillis())
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
            db.taskDao().updateTask(updatedTask)
            AlarmScheduler.cancelTaskAlarms(appContext, updatedTask.id, updatedTask.name)
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
            runtimePolicyController.refresh()

            MissionExecutionResult.Completed(updatedTask, now)
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
                return@withLock MissionExecutionResult.Skipped(task, task.skippedAt ?: System.currentTimeMillis())
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
            db.taskDao().updateTask(updatedTask)
            AlarmScheduler.cancelTaskAlarms(appContext, updatedTask.id, updatedTask.name)
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
            runtimePolicyController.refresh()

            MissionExecutionResult.Skipped(updatedTask, now)
        }.getOrElse { MissionExecutionResult.Failed("skipMission failed", it) }
    }

    suspend fun deferMission(
        taskId: Int,
        reason: String
    ): MissionExecutionResult = mutex.withLock {
        runCatching {
            val task = db.taskDao().getTaskById(taskId) ?: return@withLock MissionExecutionResult.MissingTask(taskId)

            if (task.isDeferred) {
                return@withLock MissionExecutionResult.Deferred(task, task.deferredAt ?: System.currentTimeMillis())
            }

            val now = System.currentTimeMillis()
            val updatedTask = task.copy(
                isDeferred = true,
                isInProgress = false,
                deferredAt = now,
                lastModified = now,
                syncStatus = "PENDING"
            )
            db.taskDao().updateTask(updatedTask)
            AlarmScheduler.cancelTaskAlarms(appContext, updatedTask.id, updatedTask.name)
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
            AlarmScheduler.scheduleTaskAlarms(appContext, updatedTask)
            runtimePolicyController.refresh()

            MissionExecutionResult.Deferred(updatedTask, now)
        }.getOrElse { MissionExecutionResult.Failed("deferMission failed", it) }
    }

    companion object {
        // Shared across instances: mission state transitions are infrequent and must be
        // serialized to guarantee the idempotency rules above hold under concurrent calls.
        private val mutex = Mutex()
    }
}

// Pure, dependency-free helpers kept at file scope (not class members) so they can be
// unit tested directly without instantiating MissionExecutionService's Context/Room/prefs deps.

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
