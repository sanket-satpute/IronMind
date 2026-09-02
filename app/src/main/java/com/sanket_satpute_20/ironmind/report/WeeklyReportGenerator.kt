package com.sanket_satpute_20.ironmind.report

import android.content.Context
import com.sanket_satpute_20.ironmind.apps.AppClassificationRepository
import com.sanket_satpute_20.ironmind.data.ConfigChangeEvent
import com.sanket_satpute_20.ironmind.data.ChallengeDayRecord
import com.sanket_satpute_20.ironmind.data.ChallengeEvent
import com.sanket_satpute_20.ironmind.data.DailyCheckIn
import com.sanket_satpute_20.ironmind.data.EmergencyValveEvent
import com.sanket_satpute_20.ironmind.data.FocusSession
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.TaskEvent
import com.sanket_satpute_20.ironmind.focus.EmergencyCooldownPolicy
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class WeeklyReport(
    val completedTasks: Int,
    val totalTasks: Int,
    val skippedTasks: Int,
    val completionPercent: Int,
    val totalWastedMs: Long,
    val currentStreak: Int,
    val streakBrokenOn: String?,
    val mostSkippedTask: String?,
    val focusMinutes: Int,
    val avgEnergy: Int?,
    val avgStress: Int?,
    val modeChanges: Int,
    val resetProtocolUses: Int,
    val resetProtocolReturnRate: Int,
    val resetProtocolTopTrigger: String?,
    val resetProtocolTopAction: String?,
    val challengeMorningCount: Int,
    val challengeRunStarts: Int,
    val challengeResets: Int,
    val challengeTopRoutine: String?,
    val verdict: String,
    val grade: String,
    val gradeColor: Long
)

object WeeklyReportGenerator {

    suspend fun generateReport(context: Context): WeeklyReport {
        val prefs = PrefManager.getInstance(context)
        val db = IronMindDatabase.getDatabase(context)
        val weeklyWindow = buildWeeklyWindow()
        val taskEvents = db.taskEventDao().getAllEventsSnapshot()
        val focusSessions = db.focusSessionDao().getAllSessionsSnapshot()
        val dailyCheckIns = db.dailyCheckInDao().getAllSnapshot()
        val configChanges = db.configChangeEventDao().getAllSnapshot()
        val challengeEvents = db.challengeEventDao().getAllSnapshot()
        val challengeDayRecords = db.challengeDayRecordDao().getAllSnapshot()

        val weeklyTaskSummary = buildWeeklyTaskSummary(taskEvents, weeklyWindow)
        val weeklyFocusSummary = buildWeeklyFocusSummary(focusSessions, weeklyWindow)
        val weeklyCheckInSummary = buildWeeklyCheckInSummary(dailyCheckIns, weeklyWindow)
        val weeklyConfigSummary = buildWeeklyConfigSummary(configChanges, weeklyWindow)
        val weeklyChallengeSummary = buildWeeklyChallengeSummary(challengeEvents, challengeDayRecords, weeklyWindow)

        val completionPercent = if (weeklyTaskSummary.totalTasks > 0)
            (weeklyTaskSummary.completedTasks * 100 / weeklyTaskSummary.totalTasks) else 0

        // Get total wasted time this week from UsageStats
        val totalWasted = getWeeklyWastedTime(context)
        val resetSummary = buildWeeklyResetSummary(
            events = db.emergencyValveEventDao().getAllEventsSnapshot()
        )

        val (grade, gradeColor) = when {
            completionPercent >= 90 -> "S" to 0xFFFFD700L
            completionPercent >= 75 -> "A" to 0xFF4CAF50L
            completionPercent >= 60 -> "B" to 0xFF2196F3L
            completionPercent >= 40 -> "C" to 0xFFFF9800L
            else -> "F" to 0xFFF44336L
        }

        return WeeklyReport(
            completedTasks = weeklyTaskSummary.completedTasks,
            totalTasks = weeklyTaskSummary.totalTasks,
            skippedTasks = weeklyTaskSummary.skippedTasks,
            completionPercent = completionPercent,
            totalWastedMs = totalWasted,
            currentStreak = prefs.streakCount,
            streakBrokenOn = weeklyTaskSummary.streakBrokenOn,
            mostSkippedTask = weeklyTaskSummary.mostSkippedTask,
            focusMinutes = weeklyFocusSummary.focusMinutes,
            avgEnergy = weeklyCheckInSummary.avgEnergy,
            avgStress = weeklyCheckInSummary.avgStress,
            modeChanges = weeklyConfigSummary.modeChanges,
            resetProtocolUses = resetSummary.totalUses,
            resetProtocolReturnRate = resetSummary.returnToMissionRate,
            resetProtocolTopTrigger = resetSummary.topTrigger,
            resetProtocolTopAction = resetSummary.topAction,
            challengeMorningCount = weeklyChallengeSummary.completedMornings,
            challengeRunStarts = weeklyChallengeSummary.runStarts,
            challengeResets = weeklyChallengeSummary.resets,
            challengeTopRoutine = weeklyChallengeSummary.topRoutine,
            verdict = generateVerdict(
                percent = completionPercent,
                mostSkipped = weeklyTaskSummary.mostSkippedTask,
                wastedMs = totalWasted,
                streak = prefs.streakCount,
                brokenOn = weeklyTaskSummary.streakBrokenOn,
                resetSummary = resetSummary,
                focusSummary = weeklyFocusSummary,
                checkInSummary = weeklyCheckInSummary,
                configSummary = weeklyConfigSummary,
                challengeSummary = weeklyChallengeSummary
            ),
            grade = grade,
            gradeColor = gradeColor
        )
    }

    private fun generateVerdict(
        percent: Int,
        mostSkipped: String?,
        wastedMs: Long,
        streak: Int,
        brokenOn: String?,
        resetSummary: WeeklyResetSummary,
        focusSummary: WeeklyFocusSummary,
        checkInSummary: WeeklyCheckInSummary,
        configSummary: WeeklyConfigSummary,
        challengeSummary: WeeklyChallengeSummary
    ): String {
        val wastedHours = wastedMs / 1000 / 3600
        val sb = StringBuilder()

        sb.appendLine("THIS WEEK YOU:")
        sb.appendLine()

        when {
            percent >= 90 -> sb.appendLine("✅ Completed $percent% of tasks. Elite performance.")
            percent >= 70 -> sb.appendLine("⚠️ Completed $percent% of tasks. Acceptable — but you know you can do better.")
            else -> sb.appendLine("❌ Completed only $percent% of tasks. You're lying to yourself.")
        }

        if (wastedHours > 0) {
            sb.appendLine("📱 Wasted $wastedHours hours on blocked apps.")
        }

        if (mostSkipped != null) {
            sb.appendLine("😴 \"$mostSkipped\" was your biggest weakness this week.")
        }

        if (brokenOn != null) {
            sb.appendLine("🔥 Your streak broke on $brokenOn.")
        } else if (streak > 0) {
            sb.appendLine("🔥 Streak intact — $streak days and counting.")
        }

        if (resetSummary.totalUses > 0) {
            sb.appendLine("🛡 ${EmergencyCooldownPolicy.RESET_PROTOCOL_NAME} used ${resetSummary.totalUses} times.")
            resetSummary.topTrigger?.let { sb.appendLine("⚡ Most common pressure: $it.") }
            sb.appendLine("🎯 Return-to-mission rate: ${resetSummary.returnToMissionRate}%.")
        }

        if (focusSummary.focusMinutes > 0) {
            sb.appendLine("⏱ Logged ${focusSummary.focusMinutes} minutes of tracked mission focus.")
        }

        if (checkInSummary.avgEnergy != null && checkInSummary.avgStress != null) {
            sb.appendLine("🧠 Check-in baseline: energy ${checkInSummary.avgEnergy}/5, stress ${checkInSummary.avgStress}/5.")
        }

        if (configSummary.modeChanges > 0) {
            sb.appendLine("🛠 Mode changed ${configSummary.modeChanges} time(s) this week.")
        }

        if (challengeSummary.completedMornings > 0) {
            sb.appendLine("⏰ Secured ${challengeSummary.completedMornings} verified 5 AM mornings.")
            challengeSummary.topRoutine?.let { sb.appendLine("🧱 Most used morning stack: $it.") }
        }

        if (challengeSummary.resets > 0) {
            sb.appendLine("↺ The 5 AM Club reset ${challengeSummary.resets} time(s) this week.")
        }

        sb.appendLine()

        sb.append(when {
            percent >= 90 -> "VERDICT: You're not the same person you were a week ago. Prove it again next week."
            percent >= 70 -> "VERDICT: You're trying but not enough. Close the gap next week."
            percent >= 50 -> "VERDICT: Mediocrity is a choice. You made it 7 times this week."
            else -> "VERDICT: You are not currently the person you said you want to be. Change that. Now."
        })

        return sb.toString()
    }

    private suspend fun getWeeklyWastedTime(context: Context): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
            as android.app.usage.UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000L)

        val stats = usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_WEEKLY,
            startTime, endTime
        ) ?: return 0L

        val blockedApps = AppClassificationRepository.getInstance(context).getBlockedLikePackagesSnapshot()

        return stats.filter { it.packageName in blockedApps }
            .sumOf { it.totalTimeInForeground }
    }

    private fun buildWeeklyResetSummary(events: List<EmergencyValveEvent>): WeeklyResetSummary {
        val recentEvents = events.filter { event -> eventInWindow(event.timestamp, buildWeeklyWindow()) }

        if (recentEvents.isEmpty()) return WeeklyResetSummary()

        val completedEvents = recentEvents.filter { it.completed }
        val returnToMissionCount = completedEvents.count {
            humanizeResetLabel(it.selectedRecoveryAction) == "Return To Mission"
        }
        val topTrigger = recentEvents
            .groupingBy { humanizeResetLabel(it.triggerReason) }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        val topAction = completedEvents
            .groupingBy { humanizeResetLabel(it.selectedRecoveryAction) }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return WeeklyResetSummary(
            totalUses = recentEvents.size,
            returnToMissionRate = if (completedEvents.isNotEmpty()) {
                (returnToMissionCount * 100) / completedEvents.size
            } else 0,
            topTrigger = topTrigger,
            topAction = topAction
        )
    }

    private fun humanizeResetLabel(value: String): String {
        if (value.isBlank()) return "Unknown"
        if (value.startsWith("OPEN_EMERGENCY_APP")) return "Open Emergency App"
        return value.lowercase()
            .split('_')
            .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
    }

    private fun buildWeeklyWindow(): Set<String> {
        return (1..7).map {
            LocalDate.now().minusDays(it.toLong()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }.toSet()
    }

    private fun eventInWindow(timestamp: Long, weeklyWindow: Set<String>): Boolean {
        val eventDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        return weeklyWindow.contains(eventDate)
    }

    private fun buildWeeklyTaskSummary(events: List<TaskEvent>, weeklyWindow: Set<String>): WeeklyTaskSummary {
        val weeklyEvents = events.filter { it.date in weeklyWindow }
        val createdEvents = weeklyEvents.filter { it.eventType == "CREATED" }
        val completedEvents = weeklyEvents.filter { it.eventType == "COMPLETED" }
        val skippedEvents = weeklyEvents.filter { it.eventType == "SKIPPED" }
        val mostSkipped = skippedEvents.groupingBy { it.taskName }.eachCount().maxByOrNull { it.value }?.key
        val streakBrokenOn = skippedEvents
            .minByOrNull { it.timestamp }
            ?.timestamp
            ?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).dayOfWeek.name
                    .lowercase()
                    .replaceFirstChar { ch -> ch.uppercase() }
            }

        return WeeklyTaskSummary(
            totalTasks = createdEvents.size,
            completedTasks = completedEvents.size,
            skippedTasks = skippedEvents.size,
            mostSkippedTask = mostSkipped,
            streakBrokenOn = streakBrokenOn
        )
    }

    private fun buildWeeklyFocusSummary(sessions: List<FocusSession>, weeklyWindow: Set<String>): WeeklyFocusSummary {
        val weeklySessions = sessions.filter { it.date in weeklyWindow && it.endTimestamp != null }
        return WeeklyFocusSummary(
            focusMinutes = weeklySessions.sumOf { it.actualDurationMinutes },
            completedSessions = weeklySessions.count { it.completed }
        )
    }

    private fun buildWeeklyCheckInSummary(checkIns: List<DailyCheckIn>, weeklyWindow: Set<String>): WeeklyCheckInSummary {
        val weeklyCheckIns = checkIns.filter { it.date in weeklyWindow }
        if (weeklyCheckIns.isEmpty()) return WeeklyCheckInSummary()
        return WeeklyCheckInSummary(
            avgEnergy = weeklyCheckIns.map { it.energyScore }.average().toInt(),
            avgStress = weeklyCheckIns.map { it.stressScore }.average().toInt(),
            topMood = weeklyCheckIns.groupingBy { it.moodWord.ifBlank { "Unknown" } }.eachCount().maxByOrNull { it.value }?.key
        )
    }

    private fun buildWeeklyConfigSummary(events: List<ConfigChangeEvent>, weeklyWindow: Set<String>): WeeklyConfigSummary {
        val weeklyEvents = events.filter { it.date in weeklyWindow }
        return WeeklyConfigSummary(
            modeChanges = weeklyEvents.count { it.configType == "APP_MODE" }
        )
    }

    private fun buildWeeklyChallengeSummary(
        events: List<ChallengeEvent>,
        dayRecords: List<ChallengeDayRecord>,
        weeklyWindow: Set<String>
    ): WeeklyChallengeSummary {
        val weeklyEvents = events.filter { it.date in weeklyWindow }
        val weeklyRecords = dayRecords.filter { it.date in weeklyWindow }
        val topRoutine = weeklyRecords
            .groupingBy { humanizeRoutineLabel(it.routineType) }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return WeeklyChallengeSummary(
            completedMornings = weeklyRecords.count { it.completed },
            runStarts = weeklyEvents.count { it.eventType == "STARTED" },
            resets = weeklyEvents.count { it.eventType == "RESET" },
            topRoutine = topRoutine
        )
    }

    private fun humanizeRoutineLabel(value: String): String {
        return when (value) {
            "MONK" -> "Monk Mode"
            "WARRIOR" -> "Warrior Mode"
            "BALANCED" -> "Balanced Mode"
            else -> value.ifBlank { "Unknown" }
        }
    }
}

private data class WeeklyResetSummary(
    val totalUses: Int = 0,
    val returnToMissionRate: Int = 0,
    val topTrigger: String? = null,
    val topAction: String? = null
)

private data class WeeklyTaskSummary(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val skippedTasks: Int = 0,
    val mostSkippedTask: String? = null,
    val streakBrokenOn: String? = null
)

private data class WeeklyFocusSummary(
    val focusMinutes: Int = 0,
    val completedSessions: Int = 0
)

private data class WeeklyCheckInSummary(
    val avgEnergy: Int? = null,
    val avgStress: Int? = null,
    val topMood: String? = null
)

private data class WeeklyConfigSummary(
    val modeChanges: Int = 0
)

private data class WeeklyChallengeSummary(
    val completedMornings: Int = 0,
    val runStarts: Int = 0,
    val resets: Int = 0,
    val topRoutine: String? = null
)
