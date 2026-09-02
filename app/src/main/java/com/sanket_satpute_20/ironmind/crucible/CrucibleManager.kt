package com.sanket_satpute_20.ironmind.crucible

import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class CrucibleSessionCompletion(
    val runId: String,
    val crucibleId: String,
    val title: String,
    val completedDays: Int,
    val totalDays: Int,
    val campaignCompleted: Boolean,
    val rewardTitle: String
)

/**
 * Manages the user's state and progress within an active Crucible.
 */
class CrucibleManager(private val prefs: PrefManager) {

    fun getCurrentRunId(): String {
        if (prefs.activeCrucibleId.isBlank() || prefs.crucibleRunStartedAt == 0L) return ""
        return "crucible_${prefs.activeCrucibleId}_${prefs.crucibleRunStartedAt}"
    }

    fun getActiveCrucible(): CrucibleDefinition? {
        if (prefs.activeCrucibleId.isEmpty()) return null
        return CrucibleRepository.getCrucibleById(prefs.activeCrucibleId)
    }

    fun acceptCrucible(crucibleId: String) {
        prefs.activeCrucibleId = crucibleId
        prefs.crucibleStartDate = LocalDate.now().toString()
        prefs.crucibleCompletedDates = emptySet()
        prefs.crucibleRunStartedAt = System.currentTimeMillis()
    }

    fun beginCrucibleRunIfNeeded(crucibleId: String): CrucibleDefinition? {
        val activeCrucible = getActiveCrucible()
        if (activeCrucible?.id == crucibleId) return activeCrucible

        acceptCrucible(crucibleId)
        return CrucibleRepository.getCrucibleById(crucibleId)
    }

    fun logDayComplete(date: LocalDate = LocalDate.now()) {
        val completed = prefs.crucibleCompletedDates.toMutableSet()
        completed.add(date.toString())
        prefs.crucibleCompletedDates = completed
    }

    fun hasCompletedToday(): Boolean {
        return prefs.crucibleCompletedDates.contains(LocalDate.now().toString())
    }

    fun getCurrentDayOfCrucible(): Long {
        if (prefs.crucibleStartDate.isEmpty()) return 0
        val start = LocalDate.parse(prefs.crucibleStartDate, DateTimeFormatter.ISO_LOCAL_DATE)
        return ChronoUnit.DAYS.between(start, LocalDate.now()) + 1
    }

    /**
     * Checks if the Crucible is failed.
     * A Crucible is failed if a day was missed.
     */
    fun isCrucibleFailed(): Boolean {
        val activeCrucible = getActiveCrucible() ?: return false
        val currentDay = getCurrentDayOfCrucible()

        // Can't fail on day 1
        if (currentDay <= 1) return false

        // If today is not yet complete, check up to yesterday
        val daysToCheck = if (hasCompletedToday()) currentDay else currentDay - 1
        
        return prefs.crucibleCompletedDates.size < daysToCheck
    }

    fun isCrucibleComplete(): Boolean {
        val activeCrucible = getActiveCrucible() ?: return false
        return prefs.crucibleCompletedDates.size >= activeCrucible.durationDays
    }

    fun recordSessionSuccess(date: LocalDate = LocalDate.now()): CrucibleSessionCompletion? {
        val activeCrucible = getActiveCrucible() ?: return null
        val runId = getCurrentRunId()
        val completed = prefs.crucibleCompletedDates.toMutableSet()
        completed.add(date.toString())
        prefs.crucibleCompletedDates = completed

        val completedDays = completed.size
        val campaignCompleted = completedDays >= activeCrucible.durationDays

        if (campaignCompleted) {
            completeCrucible()
        }

        return CrucibleSessionCompletion(
            runId = runId,
            crucibleId = activeCrucible.id,
            title = activeCrucible.title,
            completedDays = completedDays,
            totalDays = activeCrucible.durationDays,
            campaignCompleted = campaignCompleted,
            rewardTitle = activeCrucible.rewardTitle
        )
    }

    fun failActiveCrucibleRun(): CrucibleDefinition? {
        val activeCrucible = getActiveCrucible()
        abandonCrucible()
        return activeCrucible
    }

    fun completeCrucible() {
        val activeCrucible = getActiveCrucible() ?: return
        val currentTitles = prefs.earnedCrucibleTitles.toMutableSet()
        currentTitles.add(activeCrucible.rewardTitle)
        prefs.earnedCrucibleTitles = currentTitles
        
        // Reset the active crucible state
        prefs.activeCrucibleId = ""
        prefs.crucibleStartDate = ""
        prefs.crucibleCompletedDates = emptySet()
        prefs.crucibleRunStartedAt = 0L
    }

    fun abandonCrucible() {
        prefs.activeCrucibleId = ""
        prefs.crucibleStartDate = ""
        prefs.crucibleCompletedDates = emptySet()
        prefs.crucibleRunStartedAt = 0L
    }
}
