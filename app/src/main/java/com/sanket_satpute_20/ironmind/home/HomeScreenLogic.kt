package com.sanket_satpute_20.ironmind.home

import android.content.Context
import android.widget.Toast
import com.sanket_satpute_20.ironmind.data.ConfidenceIgnitionEntry
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.data.VoiceLog
import com.sanket_satpute_20.ironmind.focus.PomodoroChamberActivity
import com.sanket_satpute_20.ironmind.focus.PomodoroEngine
import com.sanket_satpute_20.ironmind.focus.PomodoroPreset
import com.sanket_satpute_20.ironmind.focus.PomodoroSessionConfig
import com.sanket_satpute_20.ironmind.focus.PomodoroSessionSource
import com.sanket_satpute_20.ironmind.focus.WorkLockActivity
import com.sanket_satpute_20.ironmind.focus.WorkStartActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

internal fun selectDoNowMission(homeMissionItems: List<HomeMissionItem>): HomeMissionItem? {
    return homeMissionItems.firstOrNull { it.state == HomeMissionState.LIVE_NOW }
        ?: homeMissionItems.firstOrNull { it.state == HomeMissionState.UP_NEXT || it.state == HomeMissionState.OVERDUE }
        ?: homeMissionItems.firstOrNull { it.state == HomeMissionState.LATER_TODAY }
}

internal fun buildCareerConfidenceSnapshot(
    tasks: List<Task>,
    voiceLogs: List<VoiceLog>,
    ignitionEntry: ConfidenceIgnitionEntry?,
    ignitionEntries: List<ConfidenceIgnitionEntry>,
    todayKey: String,
    lastWeeklyHonestScore: Int,
    lastWeeklyHonestScoreWeek: String
): CareerConfidenceSnapshot {
    val dailyActionTask = tasks.firstOrNull { it.origin == DAILY_ONE_PERCENT_ORIGIN }
    val ignitionByDate = ignitionEntries.associateBy { it.date }
    val ignitionRecentDays = (6L downTo 0L).map { offset ->
        val date = LocalDate.now().minusDays(offset).toString()
        ignitionByDate[date]?.completed == true
    }

    var ignitionStreak = 0
    var cursor = LocalDate.now()
    var checkedDays = 0
    while (checkedDays < 30 && ignitionByDate[cursor.toString()]?.completed == true) {
        ignitionStreak++
        cursor = cursor.minusDays(1)
        checkedDays++
    }

    val prepCount = tasks.count {
        !it.isBreakSegment && it.taskType.uppercase(Locale.ROOT) in setOf("DSA", "SYSTEM_DESIGN", "BEHAVIORAL", "PROJECT_REVIEW")
    }
    val firstPrepTask = tasks
        .filter {
            !it.isBreakSegment &&
                it.taskType.uppercase(Locale.ROOT) in setOf("DSA", "SYSTEM_DESIGN", "BEHAVIORAL", "PROJECT_REVIEW")
        }
        .sortedWith(
            compareBy<Task> { !it.isInProgress }
                .thenBy { it.isCompleted || it.isSkipped }
                .thenBy { it.startTime }
        )
        .firstOrNull()

    val confidenceRepsToday = voiceLogs.count { log ->
        log.date == todayKey && log.entryType == "CONFIDENCE"
    }
    val currentWeekStart = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .toString()

    return CareerConfidenceSnapshot(
        ignitionDone = ignitionEntry?.completed == true,
        ignitionRecoverable = ignitionEntry?.completed != true && LocalTime.now().isAfter(LocalTime.NOON),
        ignitionCue = ignitionEntry?.let { entry ->
            runCatching {
                com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionLibrary
                    .couragePromptById(entry.couragePromptId)
                    .cue
            }.getOrNull()
        },
        ignitionStreak = ignitionStreak,
        ignitionCompletedLast7Days = ignitionRecentDays.count { it },
        ignitionRecentDays = ignitionRecentDays,
        dailyActionDone = dailyActionTask?.isCompleted == true,
        dailyActionCue = dailyActionTask?.focusNotes?.takeIf { it.isNotBlank() },
        prepMissionsToday = prepCount,
        firstPrepTask = firstPrepTask,
        confidenceRepsToday = confidenceRepsToday,
        weeklyScore = lastWeeklyHonestScore,
        weeklyScoreFresh = lastWeeklyHonestScoreWeek == currentWeekStart
    )
}

internal fun shouldShowFoundationFull(
    hasFoundationStepsPending: Boolean,
    foundationRemainingCount: Int,
    foundationFirstHomeDate: LocalDate?
): Boolean {
    return hasFoundationStepsPending && (
        foundationRemainingCount > 1 ||
            foundationFirstHomeDate == null ||
            ChronoUnit.DAYS.between(foundationFirstHomeDate, LocalDate.now()) <= 7
        )
}

internal fun resolveDailyActionRoute(task: Task): String? {
    if (task.origin != DAILY_ONE_PERCENT_ORIGIN) return null
    return when (task.taskType.uppercase(Locale.ROOT)) {
        "BEHAVIORAL", "PROJECT_REVIEW" -> "VOICE_REP"
        "DSA" -> "PREP_FOCUS"
        "SYSTEM_DESIGN" -> if (task.focusModeEnabled) "PREP_FOCUS" else "VOICE_REP"
        "PHYSICAL" -> "PHYSICAL_RESET"
        else -> null
    }
}

internal fun startPomodoroForTask(context: Context, task: Task) {
    val pomodoroEngine = PomodoroEngine(context)
    val started = pomodoroEngine.startForTask(task)
    if (!started) {
        val start = parseHomeTime(task.startTime)
        val end = parseHomeTime(task.endTime)
        val totalMinutes = if (start != null && end != null) {
            ChronoUnit.MINUTES.between(start, end).coerceAtLeast(0).toInt()
        } else {
            25
        }
        val fallbackToday = LocalDate.now().toString()
        val fallbackNow = LocalTime.now()
        val hardEnd = runCatching {
            LocalDateTime.of(
                LocalDate.parse(task.date.ifBlank { fallbackToday }),
                end ?: fallbackNow.plusMinutes(totalMinutes.toLong())
            ).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrDefault(System.currentTimeMillis() + totalMinutes * 60_000L)
        pomodoroEngine.startForSession(
            PomodoroSessionConfig(
                sessionId = "task-${task.id}-${System.currentTimeMillis()}",
                source = PomodoroSessionSource.TASK,
                taskId = task.id,
                title = task.name.ifBlank { "Some Work" },
                date = task.date.ifBlank { fallbackToday },
                startTime = task.startTime.ifBlank { fallbackNow.format(DateTimeFormatter.ofPattern("HH:mm")) },
                endTime = task.endTime.ifBlank { task.startTime },
                totalDurationMinutes = totalMinutes.coerceAtLeast(1),
                preset = PomodoroPreset.fromWireValue(task.focusPreset),
                sessionHardEndsAt = hardEnd
            )
        )
    }
    context.startActivity(PomodoroChamberActivity.createIntent(context))
}

internal fun startCountdownForTask(
    context: Context,
    task: Task,
    onVoiceRepClick: () -> Unit,
    guardNavigation: (() -> Unit) -> Unit
) {
    when (resolveDailyActionRoute(task)) {
        "VOICE_REP" -> guardNavigation(onVoiceRepClick)
        "PREP_FOCUS" -> startPomodoroForTask(context, task)
        else -> {
            context.startActivity(
                WorkStartActivity.createIntent(
                    context = context,
                    taskId = task.id,
                    taskName = task.name
                )
            )
        }
    }
}

internal fun openLiveTask(context: Context, task: Task) {
    val taskEndAt = runCatching {
        val endTime = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a").firstNotNullOfOrNull { pattern ->
            runCatching {
                LocalTime.parse(
                    task.endTime.trim().uppercase(java.util.Locale.US),
                    DateTimeFormatter.ofPattern(pattern, java.util.Locale.US)
                )
            }.getOrNull()
        } ?: return@runCatching null
        LocalDateTime.of(LocalDate.parse(task.date), endTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()

    if (taskEndAt == null || taskEndAt <= System.currentTimeMillis()) {
        Toast.makeText(
            context,
            "This task window has already ended. Reschedule it before reopening.",
            Toast.LENGTH_LONG
        ).show()
    } else {
        context.startActivity(
            WorkLockActivity.createIntent(
                context = context,
                taskId = task.id,
                taskName = task.name,
                taskDate = task.date,
                taskStartTime = task.startTime,
                taskEndTime = task.endTime
            )
        )
    }
}
