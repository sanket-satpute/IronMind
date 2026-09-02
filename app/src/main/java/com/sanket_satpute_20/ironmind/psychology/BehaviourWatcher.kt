package com.sanket_satpute_20.ironmind.psychology

import android.content.Context
import com.sanket_satpute_20.ironmind.data.DailyCheckIn
import com.sanket_satpute_20.ironmind.data.FocusSession
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.TaskEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ModeSuggestion(
    val suggestedMode : AppMode,
    val reason        : String,
    val urgency       : SuggestionUrgency
)

enum class SuggestionUrgency { LOW, MEDIUM, HIGH }

object BehaviourWatcher {

    suspend fun analyseAndSuggest(context: Context): ModeSuggestion? {
        val db           = IronMindDatabase.getDatabase(context)
        val prefs        = PrefManager.getInstance(context)
        val currentMode  = AdaptiveEngine.getCurrentMode(prefs)
        val formatter    = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Gather last 7 days of data
        val last7Days    = (1..7).map {
            LocalDate.now().minusDays(it.toLong()).format(formatter)
        }

        val taskEvents = db.taskEventDao().getAllEventsSnapshot().filter { it.date in last7Days }
        val focusSessions = db.focusSessionDao().getAllSessionsSnapshot().filter { it.date in last7Days }
        val checkIns = db.dailyCheckInDao().getAllSnapshot().filter { it.date in last7Days }

        // Fetch temptations over last 7 days
        var totalTemptations = 0
        last7Days.forEach { date ->
            totalTemptations += db.temptationLogDao().getCountForDate(date)
        }
        val avgTemptations = totalTemptations / 7.0f

        val createdTaskIds = taskEvents.filter { it.eventType == "CREATED" }.map { "${it.date}:${it.taskId}" }.toSet()
        val completedTaskIds = taskEvents.filter { it.eventType == "COMPLETED" }.map { "${it.date}:${it.taskId}" }.toSet()
        val skippedTaskIds = taskEvents.filter { it.eventType == "SKIPPED" }.map { "${it.date}:${it.taskId}" }.toSet()

        val totalTasks = createdTaskIds.size
        val completedTasks = completedTaskIds.size
        val skippedTasks = skippedTaskIds.size
        val consecutiveSkipDays = calculateConsecutiveSkipDays(taskEvents, last7Days)

        val completionRate = if (totalTasks > 0)
            completedTasks.toFloat() / totalTasks else 0f

        val avgFocusMinutes = if (focusSessions.isNotEmpty()) {
            focusSessions.map { it.actualDurationMinutes }.average().toFloat()
        } else 0f
        val avgEnergy = if (checkIns.isNotEmpty()) checkIns.map { it.energyScore }.average().toFloat() else 3f
        val avgStress = if (checkIns.isNotEmpty()) checkIns.map { it.stressScore }.average().toFloat() else 3f

        var suggestion: ModeSuggestion? = null

        // ── Suggestion logic ──────────────────────────────────

        // URGENT — switch to Recovery if badly struggling
        if ((consecutiveSkipDays >= 4 || (completionRate < 0.4f && avgTemptations > 4f) || (avgEnergy <= 2.0f && avgStress >= 4.0f)) && currentMode != AppMode.RECOVERY) {
            suggestion = ModeSuggestion(
                suggestedMode = AppMode.RECOVERY,
                reason        = if (avgEnergy <= 2.0f && avgStress >= 4.0f) {
                    "Your recent check-ins show low energy and high stress. Recovery Mode removes pressure before the system breaks harder."
                } else if (completionRate < 0.4f && avgTemptations > 4f) {
                    "You've completed only ${(completionRate * 100).toInt()}% of tasks recently and faced high temptation. Recovery Mode removes all pressure."
                } else {
                    "You've had $consecutiveSkipDays days without completing tasks. Something's going on. Recovery Mode removes all pressure for a week."
                },
                urgency       = SuggestionUrgency.HIGH
            )
        }
        // Switch up from Recovery if doing better
        else if (currentMode == AppMode.RECOVERY && completionRate >= 0.6f && avgEnergy >= 3.0f && avgTemptations < 3f) {
            suggestion = ModeSuggestion(
                suggestedMode = AppMode.BUILD,
                reason        = "You've completed ${(completionRate * 100).toInt()}% of tasks this week " +
                                "in Recovery Mode with energy recovering and low temptations. You're ready for Build Mode.",
                urgency       = SuggestionUrgency.MEDIUM
            )
        }
        // Struggling in Iron Mode — suggest Build
        else if (currentMode == AppMode.IRON && (completionRate < 0.6f || avgStress >= 4.0f || avgTemptations > 4f)) {
            suggestion = ModeSuggestion(
                suggestedMode = AppMode.BUILD,
                reason        = "Iron Mode is hitting hard right now. " +
                                "${(completionRate * 100).toInt()}% completion rate this week. " +
                                "Build Mode keeps you accountable without the punishment spiral.",
                urgency       = SuggestionUrgency.MEDIUM
            )
        }
        // Thriving in Build — suggest Iron
        else if (currentMode == AppMode.BUILD && completionRate >= 0.85f &&
            avgFocusMinutes >= 35f && avgEnergy >= 4.0f && avgStress <= 3.0f && avgTemptations <= 2f) {
            suggestion = ModeSuggestion(
                suggestedMode = AppMode.IRON,
                reason        = "You've completed ${(completionRate * 100).toInt()}% of tasks " +
                                "with strong recent focus sessions, low temptations ($avgTemptations/day), and stable check-ins. " +
                                "You're ready for Iron Mode.",
                urgency       = SuggestionUrgency.LOW
            )
        }

        // No app opens in 3 days
        if (suggestion == null) {
            val lastOpenDate = prefs.lastAppOpen
            if (lastOpenDate.isNotEmpty()) {
                val last = runCatching { LocalDate.parse(lastOpenDate) }.getOrNull()
                if (last != null && last.isBefore(LocalDate.now().minusDays(2))) {
                    if (currentMode != AppMode.RECOVERY) {
                        suggestion = ModeSuggestion(
                            suggestedMode = AppMode.RECOVERY,
                            reason        = "You haven't opened IronMind in a few days. " +
                                            "Life happens. Recovery Mode starts with zero pressure.",
                            urgency       = SuggestionUrgency.MEDIUM
                        )
                    }
                }
            }
        }

        // Check if suggestion was recently rejected (7-day cooldown)
        if (suggestion != null) {
            val rejectedMode = prefs.lastRejectedRecommendationMode
            val rejectedDateStr = prefs.lastRejectedRecommendationDate
            
            if (suggestion.suggestedMode.name == rejectedMode && rejectedDateStr.isNotEmpty()) {
                val rejectedDate = runCatching { LocalDate.parse(rejectedDateStr) }.getOrNull()
                if (rejectedDate != null && ChronoUnit.DAYS.between(rejectedDate, LocalDate.now()) < 7) {
                    return null // Suppress this suggestion because it's in cooldown
                }
            }
        }

        return suggestion
    }

    fun recordAppOpen(context: Context) {
        PrefManager.getInstance(context).lastAppOpen = LocalDate.now().toString()
    }

    private fun calculateConsecutiveSkipDays(
        taskEvents: List<TaskEvent>,
        last7Days: List<String>
    ): Int {
        var longest = 0
        var current = 0
        last7Days.forEach { date ->
            val dayEvents = taskEvents.filter { it.date == date }
            val hadSkip = dayEvents.any { it.eventType == "SKIPPED" }
            val hadComplete = dayEvents.any { it.eventType == "COMPLETED" }
            if (hadSkip && !hadComplete) {
                current++
                if (current > longest) longest = current
            } else {
                current = 0
            }
        }
        return longest
    }
}
