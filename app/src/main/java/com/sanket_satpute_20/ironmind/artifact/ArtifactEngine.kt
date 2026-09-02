package com.sanket_satpute_20.ironmind.artifact

import android.app.usage.UsageStatsManager
import android.content.Context
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

/**
 * The logic engine that detects when discipline patterns match Artifact requirements.
 */
object ArtifactEngine {

    /**
     * Central evaluation hub. Called whenever a task is completed.
     */
    suspend fun evaluateAll(context: Context, latestTask: Task): String? {
        val prefs = PrefManager.getInstance(context)
        val manager = ArtifactManager(prefs)
        
        // 1. Check Iron Anchor (Deep Work)
        if (!manager.isUnlocked(ArtifactRepository.IRON_ANCHOR.id)) {
            if (checkIronAnchor(context, latestTask)) return ArtifactRepository.IRON_ANCHOR.id
        }
        
        // 2. Check Midnight Lamp (Late night focus)
        if (!manager.isUnlocked(ArtifactRepository.MIDNIGHT_LAMP.id)) {
            if (checkMidnightLamp(context)) return ArtifactRepository.MIDNIGHT_LAMP.id
        }
        
        // 3. Check Dawn Breaker (5 AM Club)
        if (!manager.isUnlocked(ArtifactRepository.DAWN_BREAKER.id)) {
            if (checkDawnBreaker(prefs)) return ArtifactRepository.DAWN_BREAKER.id
        }

        return null
    }

    private suspend fun checkIronAnchor(context: Context, task: Task): Boolean {
        // Requirement: 2+ hours with zero focus breaches.
        val duration = calculateDurationMinutes(task.startTime, task.endTime)
        if (duration < 120) return false

        val db = IronMindDatabase.getDatabase(context)
        val logs = db.temptationLogDao().getLogsForDate(task.date).first()
        
        // Filter logs to only those that happened DURING this specific task window
        val taskStart = parseToMinutes(task.startTime)
        val taskEnd = parseToMinutes(task.endTime)
        
        val breachesDuringTask = logs.any { log ->
            val logTime = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            val logMinutes = logTime.get(Calendar.HOUR_OF_DAY) * 60 + logTime.get(Calendar.MINUTE)
            logMinutes in taskStart..taskEnd
        }

        if (!breachesDuringTask) {
            return ArtifactManager(PrefManager.getInstance(context)).unlockArtifact(ArtifactRepository.IRON_ANCHOR.id)
        }
        return false
    }

    private fun checkMidnightLamp(context: Context): Boolean {
        // Requirement: 5 missions after 11:00 PM with focus > 80.
        // For simplicity in this version, we check if current mission is after 11 PM
        // Real implementation would query history, but let's start with a "Current Win" logic
        return false // Placeholder for complex history query
    }

    private fun checkDawnBreaker(prefs: PrefManager): Boolean {
        // Requirement: 7 consecutive 5 AM Club completions.
        return if (prefs.challengeDaysCompleted >= 7) {
            ArtifactManager(prefs).unlockArtifact(ArtifactRepository.DAWN_BREAKER.id)
        } else false
    }

    /**
     * Tracks usage of the Autopsy feature to unlock 'Terminal of Truth'
     */
    fun onAutopsyOpened(context: Context) {
        val prefs = PrefManager.getInstance(context)
        val manager = ArtifactManager(prefs)
        if (manager.isUnlocked(ArtifactRepository.TERMINAL_OF_TRUTH.id)) return

        val today = java.time.LocalDate.now().toString()
        if (prefs.lastAutopsyOpenDate != today) {
            prefs.autopsyConsecutiveDays++
            prefs.lastAutopsyOpenDate = today
            
            if (prefs.autopsyConsecutiveDays >= 3) {
                manager.unlockArtifact(ArtifactRepository.TERMINAL_OF_TRUTH.id)
            }
        }
    }

    // --- Helper Utilities ---
    private fun calculateDurationMinutes(start: String, end: String): Int {
        val s = parseToMinutes(start)
        val e = parseToMinutes(end)
        return if (e > s) e - s else (1440 - s) + e
    }

    private fun parseToMinutes(timeStr: String): Int {
        val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
        for (f in formats) {
            runCatching { 
                val time = LocalTime.parse(timeStr.trim().uppercase(), DateTimeFormatter.ofPattern(f, Locale.US))
                return time.hour * 60 + time.minute
            }
        }
        return 0
    }
}
