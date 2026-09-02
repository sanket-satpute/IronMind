package com.sanket_satpute_20.ironmind.bossmode

import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.TaskDao
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object BossModeAnalyzer {

    // Call this every Sunday or after task completion
    // Returns true if Boss Mode was newly activated
    suspend fun checkAndApplyBossMode(
        taskDao: TaskDao,
        prefs: PrefManager
    ): Boolean {
        // Only eligible in Iron Mode
        if (AdaptiveEngine.getCurrentMode(prefs) != AppMode.IRON) {
            prefs.bossModeActive = false
            return false
        }

        // Do not apply twice in the same week
        val today = LocalDate.now()
        val lastApplied = prefs.lastBossModeAppliedDate
        if (lastApplied.isNotEmpty()) {
            val lastDate = LocalDate.parse(lastApplied)
            val daysSince = ChronoUnit.DAYS.between(lastDate, today)
            if (daysSince < 7) return false
        }

        // Get last 7 days of tasks
        val results = mutableListOf<Boolean>()
        for (i in 1..7) {
            val date = today.minusDays(i.toLong()).toString()
            val tasks = taskDao.getTasksForDateOnce(date)
            if (tasks.isEmpty()) {
                results.add(false)
                continue
            }
            val allDone = tasks.all { it.isCompleted }
            results.add(allDone)
        }

        // Perfect week = all 7 days fully completed
        val isPerfectWeek = results.size == 7 && results.all { it }

        if (isPerfectWeek) {
            prefs.bossModeActive = true
            prefs.lastBossModeAppliedDate = today.toString()
            // Increase difficulty by 15 percent
            prefs.bossModeDurationBonus = 15
            return true
        }

        return false
    }

    // Call this every Monday to reset if applicable
    fun resetBossMode(prefs: PrefManager) {
        val today = LocalDate.now()
        if (today.dayOfWeek == DayOfWeek.MONDAY) {
            prefs.bossModeActive = false
            prefs.bossModeDurationBonus = 0
        }
    }

    // Apply boss mode multiplier to a task duration
    fun getAdjustedDuration(baseDurationMinutes: Int, prefs: PrefManager): Int {
        if (!prefs.bossModeActive) return baseDurationMinutes
        val bonus = prefs.bossModeDurationBonus
        return baseDurationMinutes + (baseDurationMinutes * bonus / 100)
    }
}
