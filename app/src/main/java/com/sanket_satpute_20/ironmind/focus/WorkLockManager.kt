package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import android.content.Intent
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.integrity.IronStrictnessManager
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class WorkLockManager(private val context: Context) {

    private val prefs = PrefManager.getInstance(context)
    private val ironStrictnessManager = IronStrictnessManager(context)

    fun startForTask(
        taskId: Int,
        taskName: String,
        taskDate: String,
        taskEndTime: String
    ): Boolean {
        if (!prefs.workLockEnabled) return false
        val endAt = resolveTaskEndMillis(taskDate, taskEndTime) ?: return false
        val now = System.currentTimeMillis()
        if (endAt <= now) return false

        resetEmergencyMonthIfNeeded()
        prefs.workLockActive = true
        prefs.workLockStage = WorkLockStage.ACTIVE.name
        prefs.workLockTaskId = taskId
        prefs.workLockTaskName = taskName
        prefs.workLockStartedAt = now
        prefs.workLockEndsAt = endAt
        prefs.workLockPenaltyArmed = true
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) })
        return true
    }

    fun startTestLock(
        taskName: String = "SPRING TEST MISSION",
        durationMinutes: Int = 5
    ): Boolean {
        if (!prefs.workLockEnabled) return false
        val now = System.currentTimeMillis()
        val endAt = now + (durationMinutes.coerceAtLeast(1) * 60_000L)
        resetEmergencyMonthIfNeeded()
        prefs.workLockActive = true
        prefs.workLockStage = WorkLockStage.ACTIVE.name
        prefs.workLockTaskId = -999
        prefs.workLockTaskName = taskName
        prefs.workLockStartedAt = now
        prefs.workLockEndsAt = endAt
        prefs.workLockPenaltyArmed = true
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) })
        return true
    }

    fun isActive(): Boolean {
        if (!prefs.workLockActive) return false
        if (prefs.workLockEndsAt <= System.currentTimeMillis()) {
            endForExpiry()
            return false
        }
        return WorkLockStage.fromWireValue(prefs.workLockStage) == WorkLockStage.ACTIVE
    }

    fun timeRemainingMillis(): Long {
        return (prefs.workLockEndsAt - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun endForCompletion() {
        prefs.workLockStage = WorkLockStage.COMPLETED.name
        prefs.workLockPenaltyArmed = false
        prefs.clearWorkLock()
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) })
    }

    fun endForExpiry() {
        prefs.workLockStage = WorkLockStage.EXPIRED.name
        prefs.workLockPenaltyArmed = false
        prefs.clearWorkLock()
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) })
    }

    fun breakMissionWithPenalty(): Int {
        val penalty = calculateBreakPenalty()
        com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(context).penalizeXp(penalty.toLong())
        prefs.workLockBreakCountToday = if (prefs.activeTaskDate == LocalDate.now().toString()) {
            prefs.workLockBreakCountToday + 1
        } else {
            1
        }
        prefs.workLockStage = WorkLockStage.BROKEN.name
        prefs.clearWorkLock()
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) })
        return penalty
    }

    fun canUseEmergencyExit(): Boolean {
        resetEmergencyMonthIfNeeded()
        return prefs.workLockEmergencyExitCount < effectiveMaxEmergencyExits()
    }

    fun useEmergencyExit(): Boolean {
        if (!canUseEmergencyExit()) return false
        prefs.workLockEmergencyExitCount += 1
        prefs.workLockStage = WorkLockStage.BROKEN.name
        prefs.clearWorkLock()
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) })
        return true
    }

    fun calculateBreakPenalty(): Int {
        val basePenalty = 40
        val durationPenalty = ((prefs.workLockEndsAt - prefs.workLockStartedAt).coerceAtLeast(0L) / 1000L / 60L / 15L).toInt() * 5
        val strictnessPenalty = ironStrictnessManager.getTodayProfile().workLockPenaltyBonus
        return (basePenalty + durationPenalty + strictnessPenalty).coerceAtMost(140)
    }

    fun remainingEmergencyExitAllowance(): Int {
        return (effectiveMaxEmergencyExits() - prefs.workLockEmergencyExitCount).coerceAtLeast(0)
    }

    private fun effectiveMaxEmergencyExits(): Int {
        val reduction = ironStrictnessManager.getTodayProfile().emergencyExitReduction
        return (BASE_MAX_MONTHLY_EMERGENCY_EXITS - reduction).coerceAtLeast(0)
    }

    private fun resetEmergencyMonthIfNeeded() {
        val currentMonth = LocalDate.now().withDayOfMonth(1).toString()
        if (prefs.workLockEmergencyExitMonth != currentMonth) {
            prefs.workLockEmergencyExitMonth = currentMonth
            prefs.workLockEmergencyExitCount = 0
        }
    }

    private fun resolveTaskEndMillis(taskDate: String, taskEndTime: String): Long? {
        val date = runCatching { LocalDate.parse(taskDate) }.getOrNull() ?: return null
        val time = parseTime(taskEndTime) ?: return null
        return LocalDateTime.of(date, time)
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

    companion object {
        private const val BASE_MAX_MONTHLY_EMERGENCY_EXITS = 5
    }
}
