package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

class PomodoroEngine(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = PrefManager.getInstance(appContext)

    fun startForTask(task: Task): Boolean {
        if (!task.focusModeEnabled || task.isBreakSegment) return false
        val title = task.name.ifBlank { "Some Work" }
        val preset = PomodoroPreset.fromWireValue(task.focusPreset)
        return startForSession(
            PomodoroSessionConfig(
                sessionId = "task-${task.id}-${System.currentTimeMillis()}",
                source = PomodoroSessionSource.TASK,
                taskId = task.id,
                title = title,
                date = task.date,
                startTime = task.startTime,
                endTime = task.endTime,
                totalDurationMinutes = resolveTaskDurationMinutes(task.startTime, task.endTime),
                preset = preset,
                sessionHardEndsAt = resolveSessionHardEnd(task.date, task.endTime)
            )
        )
    }

    fun startForSession(config: PomodoroSessionConfig): Boolean {
        val now = System.currentTimeMillis()
        val initialPhaseEnd = if (config.sessionHardEndsAt > 0L) {
            min(now + config.preset.workMinutes * 60_000L, config.sessionHardEndsAt)
        } else {
            now + config.preset.workMinutes * 60_000L
        }

        prefs.pomodoroActive = true
        prefs.pomodoroTaskId = config.taskId ?: -1
        prefs.pomodoroSessionId = config.sessionId
        prefs.pomodoroSource = config.source.name
        prefs.pomodoroTitle = config.title.ifBlank { "Some Work" }
        prefs.pomodoroDate = config.date
        prefs.pomodoroStartTime = config.startTime
        prefs.pomodoroEndTime = config.endTime
        prefs.pomodoroTotalDurationMinutes = config.totalDurationMinutes
        prefs.pomodoroPreset = config.preset.name
        prefs.pomodoroPhase = PomodoroPhase.WORK.name
        prefs.pomodoroPhaseEndsAt = initialPhaseEnd
        prefs.pomodoroIntervalIndex = 1
        prefs.pomodoroCompletedWorkIntervals = 0
        prefs.pomodoroBreachCount = 0
        prefs.pomodoroBreakUnlocked = false
        prefs.pomodoroStartedAt = now
        prefs.pomodoroSessionHardEndsAt = config.sessionHardEndsAt
        return true
    }

    fun isActive(): Boolean {
        if (!prefs.pomodoroActive) return false
        return currentState().active
    }

    fun currentState(): PomodoroSessionState {
        return PomodoroSessionState(
            active = prefs.pomodoroActive,
            sessionId = prefs.pomodoroSessionId,
            source = PomodoroSessionSource.fromWireValue(prefs.pomodoroSource),
            taskId = prefs.pomodoroTaskId,
            title = prefs.pomodoroTitle.ifBlank { "Some Work" },
            date = prefs.pomodoroDate,
            startTime = prefs.pomodoroStartTime,
            endTime = prefs.pomodoroEndTime,
            totalDurationMinutes = prefs.pomodoroTotalDurationMinutes,
            preset = PomodoroPreset.fromWireValue(prefs.pomodoroPreset),
            phase = PomodoroPhase.fromWireValue(prefs.pomodoroPhase),
            phaseEndsAt = prefs.pomodoroPhaseEndsAt,
            sessionHardEndsAt = prefs.pomodoroSessionHardEndsAt,
            intervalIndex = prefs.pomodoroIntervalIndex,
            completedWorkIntervals = prefs.pomodoroCompletedWorkIntervals,
            breachCount = prefs.pomodoroBreachCount,
            breakUnlocked = prefs.pomodoroBreakUnlocked,
            startedAt = prefs.pomodoroStartedAt
        )
    }

    fun currentSessionConfig(): PomodoroSessionConfig? {
        if (!prefs.pomodoroActive) return null
        return PomodoroSessionConfig(
            sessionId = prefs.pomodoroSessionId,
            source = PomodoroSessionSource.fromWireValue(prefs.pomodoroSource),
            taskId = prefs.pomodoroTaskId.takeIf { it != -1 },
            title = prefs.pomodoroTitle.ifBlank { "Some Work" },
            date = prefs.pomodoroDate,
            startTime = prefs.pomodoroStartTime,
            endTime = prefs.pomodoroEndTime,
            totalDurationMinutes = prefs.pomodoroTotalDurationMinutes,
            preset = PomodoroPreset.fromWireValue(prefs.pomodoroPreset),
            sessionHardEndsAt = prefs.pomodoroSessionHardEndsAt
        )
    }

    fun timeRemainingMillis(now: Long = System.currentTimeMillis()): Long {
        return (prefs.pomodoroPhaseEndsAt - now).coerceAtLeast(0L)
    }

    fun advanceIfNeeded(now: Long = System.currentTimeMillis()): PomodoroSessionState {
        if (!prefs.pomodoroActive) return currentState()
        if (prefs.pomodoroPhaseEndsAt > now) return currentState()

        val preset = PomodoroPreset.fromWireValue(prefs.pomodoroPreset)
        when (PomodoroPhase.fromWireValue(prefs.pomodoroPhase)) {
            PomodoroPhase.WORK -> {
                val completed = prefs.pomodoroCompletedWorkIntervals + 1
                prefs.pomodoroCompletedWorkIntervals = completed
                val longBreakDue = completed % preset.cyclesBeforeLongBreak == 0
                prefs.pomodoroPhase = if (longBreakDue) PomodoroPhase.LONG_BREAK.name else PomodoroPhase.SHORT_BREAK.name
                prefs.pomodoroPhaseEndsAt = now + if (longBreakDue) preset.longBreakMinutes * 60_000L else preset.shortBreakMinutes * 60_000L
                prefs.pomodoroBreakUnlocked = true
            }

            PomodoroPhase.SHORT_BREAK,
            PomodoroPhase.LONG_BREAK -> {
                prefs.pomodoroPhase = PomodoroPhase.WORK.name
                prefs.pomodoroPhaseEndsAt = now + preset.workMinutes * 60_000L
                prefs.pomodoroBreakUnlocked = false
                prefs.pomodoroIntervalIndex = prefs.pomodoroCompletedWorkIntervals + 1
            }

            else -> Unit
        }
        return currentState()
    }

    fun registerVoidBreachDuringWork(resetCurrentInterval: Boolean): PomodoroSessionState {
        if (!prefs.pomodoroActive) return currentState()
        if (PomodoroPhase.fromWireValue(prefs.pomodoroPhase) != PomodoroPhase.WORK) return currentState()

        prefs.pomodoroBreachCount = prefs.pomodoroBreachCount + 1
        if (resetCurrentInterval) {
            val preset = PomodoroPreset.fromWireValue(prefs.pomodoroPreset)
            prefs.pomodoroPhaseEndsAt = System.currentTimeMillis() + preset.workMinutes * 60_000L
        }
        return currentState()
    }

    fun completeSession(): PomodoroSessionState {
        if (!prefs.pomodoroActive) return currentState()
        prefs.pomodoroPhase = PomodoroPhase.COMPLETED.name
        prefs.pomodoroBreakUnlocked = false
        val finalState = currentState()
        clear()
        return finalState
    }

    fun breakSession(): PomodoroSessionState {
        if (!prefs.pomodoroActive) return currentState()
        prefs.pomodoroPhase = PomodoroPhase.BROKEN.name
        prefs.pomodoroBreakUnlocked = false
        val finalState = currentState()
        clear()
        return finalState
    }

    fun clearIfTaskRolled(today: LocalDate = LocalDate.now()) {
        if (!prefs.pomodoroActive) return
        if (prefs.activeTaskDate.isNotBlank() && prefs.activeTaskDate != today.toString()) {
            clear()
        }
    }

    fun clear() {
        prefs.clearPomodoro()
    }

    private fun resolveSessionHardEnd(date: String, endTime: String): Long {
        if (date.isBlank() || endTime.isBlank()) return 0L
        val localDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return 0L
        val localTime = parseTime(endTime) ?: return 0L
        return LocalDateTime.of(localDate, localTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun parseTime(value: String): LocalTime? {
        val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
        return formats.firstNotNullOfOrNull { pattern ->
            runCatching {
                LocalTime.parse(value.trim().uppercase(Locale.US), DateTimeFormatter.ofPattern(pattern, Locale.US))
            }.getOrNull()
        }
    }

    private fun resolveTaskDurationMinutes(startTime: String, endTime: String): Int {
        val start = parseTime(startTime) ?: return 0
        val end = parseTime(endTime) ?: return 0
        return Duration.between(start, end).toMinutes().coerceAtLeast(0L).toInt()
    }
}
