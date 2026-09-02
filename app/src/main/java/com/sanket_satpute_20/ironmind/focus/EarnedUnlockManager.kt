package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import android.content.Intent
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.integrity.IronStrictnessManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import java.time.LocalDate

enum class EarnedUnlockMode {
    DAY,
    IRON_DAY
}

class EarnedUnlockManager(private val context: Context) {

    private val prefs = PrefManager.getInstance(context)
    private val db = IronMindDatabase.getDatabase(context)
    private val ironStrictnessManager = IronStrictnessManager(context)

    suspend fun syncTodayFromDatabase() {
        val today = LocalDate.now().toString()
        syncForDate(today, db.taskDao().getTasksForDateOnce(today))
    }

    fun syncForToday(tasks: List<Task>) {
        syncForDate(LocalDate.now().toString(), tasks)
    }

    fun isSealedToday(): Boolean {
        val today = LocalDate.now().toString()
        if (!prefs.earnedUnlockEnabled) return false
        if (prefs.earnedUnlockDate != today) return false
        return prefs.earnedUnlockActive && !prefs.earnedUnlockUnlockedToday
    }

    fun resetIfDayRolled() {
        val today = LocalDate.now().toString()
        if (prefs.earnedUnlockDate.isNotBlank() && prefs.earnedUnlockDate != today) {
            prefs.clearEarnedUnlock()
            broadcastGuardReload()
        }
    }

    private fun syncForDate(date: String, tasks: List<Task>) {
        if (!prefs.earnedUnlockEnabled) {
            prefs.clearEarnedUnlock()
            broadcastGuardReload()
            return
        }

        val today = LocalDate.now().toString()
        if (date != today) return

        val focusTasks = tasks
            .asSequence()
            .filter { it.date == today }
            .filterNot { it.isBreakSegment }
            .toList()

        if (focusTasks.isEmpty()) {
            prefs.clearEarnedUnlock()
            broadcastGuardReload()
            return
        }

        val completedCount = focusTasks.count { it.isCompleted }
        val skippedCount = focusTasks.count { it.isSkipped }
        val unresolvedCount = focusTasks.count { !it.isCompleted && !it.isSkipped }
        val allCompleted = completedCount == focusTasks.size
        val currentMode = AdaptiveEngine.getCurrentMode(prefs)
        val requireCleanUnlock = currentMode == AppMode.IRON &&
            ironStrictnessManager.getTodayProfile().requireCleanEarnedUnlock
        val unlockedToday = if (requireCleanUnlock) {
            allCompleted && skippedCount == 0
        } else {
            allCompleted
        }

        prefs.earnedUnlockMode = when (currentMode) {
            AppMode.IRON -> EarnedUnlockMode.IRON_DAY.name
            else -> EarnedUnlockMode.DAY.name
        }
        prefs.earnedUnlockDate = today
        prefs.earnedUnlockTotalCount = focusTasks.size
        prefs.earnedUnlockCompletedCount = completedCount
        prefs.earnedUnlockSkippedCount = skippedCount
        prefs.earnedUnlockRemainingCount = unresolvedCount
        prefs.earnedUnlockUnlockedToday = unlockedToday
        prefs.earnedUnlockActive = !unlockedToday

        broadcastGuardReload()
    }

    private fun broadcastGuardReload() {
        context.sendBroadcast(
            Intent("com.ironmind.RELOAD_GUARD").apply {
                setPackage(context.packageName)
            }
        )
    }
}
