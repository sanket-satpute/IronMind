package com.sanket_satpute_20.ironmind.sleeplock

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.settings.getDefaultDialerPackage
import com.sanket_satpute_20.ironmind.settings.getDefaultSmsPackage
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

class SleepLockManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val prefs = PrefManager.getInstance(appContext)
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun getBedtime(): LocalTime = LocalTime.of(
        prefs.sleepLockBedHour.coerceIn(0, 23),
        prefs.sleepLockBedMinute.coerceIn(0, 59)
    )

    fun getWakeTime(): LocalTime = LocalTime.of(
        prefs.sleepLockWakeHour.coerceIn(0, 23),
        prefs.sleepLockWakeMinute.coerceIn(0, 59)
    )

    fun isDndPermissionGranted(): Boolean = notificationManager.isNotificationPolicyAccessGranted

    fun areNotificationsEnabled(): Boolean = NotificationManagerCompat.from(appContext).areNotificationsEnabled()

    fun isEmergencyOverrideActive(now: LocalDateTime = LocalDateTime.now()): Boolean {
        val overrideUntil = prefs.sleepLockEmergencyOverrideUntil
        if (overrideUntil <= 0L) return false
        val nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (overrideUntil <= nowMillis) {
            prefs.sleepLockEmergencyOverrideUntil = 0L
            return false
        }
        return true
    }

    fun currentWindowEndMillis(now: LocalDateTime = LocalDateTime.now()): Long? {
        val zoneId = ZoneId.systemDefault()
        return activeWindow(now)?.second?.atZone(zoneId)?.toInstant()?.toEpochMilli()
    }

    fun remainingEmergencyExitAllowance(): Int {
        syncEmergencyExitQuota()
        return (MAX_EMERGENCY_EXITS_PER_MONTH - prefs.sleepLockEmergencyExitCount).coerceAtLeast(0)
    }

    fun canUseEmergencyExit(): Boolean = remainingEmergencyExitAllowance() > 0

    fun useEmergencyExit(): Boolean {
        if (!canUseEmergencyExit()) return false
        prefs.sleepLockEmergencyExitCount = prefs.sleepLockEmergencyExitCount + 1
        return true
    }

    fun isActive(now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (!prefs.sleepLockEnabled) return false
        if (isEmergencyOverrideActive(now)) return false
        return activeWindow(now) != null || prefs.sleepLockActive
    }

    fun currentWindow(now: LocalDateTime = LocalDateTime.now()): Pair<LocalDateTime, LocalDateTime>? {
        if (!prefs.sleepLockEnabled) return null
        if (isEmergencyOverrideActive(now)) return null
        return activeWindow(now)
    }

    fun syncPersistentState(now: LocalDateTime = LocalDateTime.now()): Boolean {
        val window = currentWindow(now)
        return if (window != null) {
            val zoneId = ZoneId.systemDefault()
            prefs.sleepLockActive = true
            prefs.sleepLockStartedAt = window.first.atZone(zoneId).toInstant().toEpochMilli()
            prefs.sleepLockEndsAt = window.second.atZone(zoneId).toInstant().toEpochMilli()
            prefs.sleepLockLastWarningStage = SleepLockStage.ACTIVE.name
            true
        } else {
            prefs.sleepLockActive = false
            prefs.sleepLockStartedAt = 0L
            prefs.sleepLockEndsAt = 0L
            if (prefs.sleepLockLastWarningStage == SleepLockStage.ACTIVE.name) {
                prefs.sleepLockLastWarningStage = SleepLockStage.RELEASING.name
            }
            false
        }
    }

    fun getStage(now: LocalDateTime = LocalDateTime.now()): SleepLockStage {
        if (!prefs.sleepLockEnabled) return SleepLockStage.OFF
        if (isActive(now)) return SleepLockStage.ACTIVE

        val minutesUntil = minutesUntilNextStart(now)
        return when {
            prefs.sleepLockWarningFinalEnabled && minutesUntil in 0..5 -> SleepLockStage.WARNING_FINAL
            minutesUntil in 16..20 -> SleepLockStage.WARNING_20
            prefs.sleepLockWarning15Enabled && minutesUntil in 6..15 -> SleepLockStage.WARNING_15
            prefs.sleepLockWarning30Enabled && minutesUntil in 21..30 -> SleepLockStage.WARNING_30
            else -> SleepLockStage.OFF
        }
    }

    fun scheduleLabel(): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return "${getBedtime().format(formatter)} - ${getWakeTime().format(formatter)}"
    }

    fun nextTransitionLabel(now: LocalDateTime = LocalDateTime.now()): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return if (isActive(now)) {
            val activeEnd = activeWindow(now)?.second ?: resolveWindow(now.toLocalDate()).second
            "Active until ${activeEnd.toLocalTime().format(formatter)}"
        } else {
            val nextStart = nextStartDateTime(now)
            val minutes = minutesUntilNextStart(now)
            if (minutes < 1) {
                "Starts now"
            } else {
                "Starts in ${formatMinutes(minutes)} at ${nextStart.toLocalTime().format(formatter)}"
            }
        }
    }

    fun nextOccurrence(
        now: LocalDateTime = LocalDateTime.now(),
        action: String
    ): LocalDateTime? {
        if (!prefs.sleepLockEnabled) return null
        val todayWindow = resolveWindow(now.toLocalDate())
        val tomorrowWindow = resolveWindow(now.toLocalDate().plusDays(1))

        fun candidate(window: Pair<LocalDateTime, LocalDateTime>): LocalDateTime? {
            return when (action) {
                SleepLockAlarmReceiver.ACTION_WARNING_30 ->
                    if (prefs.sleepLockWarning30Enabled) window.first.minusMinutes(30) else null
                SleepLockAlarmReceiver.ACTION_WARNING_20 ->
                    window.first.minusMinutes(20)
                SleepLockAlarmReceiver.ACTION_WARNING_15 ->
                    if (prefs.sleepLockWarning15Enabled) window.first.minusMinutes(15) else null
                SleepLockAlarmReceiver.ACTION_WARNING_FINAL ->
                    if (prefs.sleepLockWarningFinalEnabled) window.first.minusMinutes(5) else null
                SleepLockAlarmReceiver.ACTION_START -> window.first
                SleepLockAlarmReceiver.ACTION_END -> window.second
                else -> null
            }
        }

        val todayCandidate = candidate(todayWindow)
        if (todayCandidate != null && todayCandidate.isAfter(now)) {
            return todayCandidate
        }
        val tomorrowCandidate = candidate(tomorrowWindow)
        return tomorrowCandidate?.takeIf { it.isAfter(now) }
    }

    fun coreEmergencyPackages(): Set<String> {
        return linkedSetOf<String>().apply {
            getDefaultDialerPackage(appContext).takeIf { it.isNotBlank() }?.let(::add)
            getDefaultSmsPackage(appContext).takeIf { it.isNotBlank() }?.let(::add)
            add(appContext.packageName)
        }
    }

    private fun minutesUntilNextStart(now: LocalDateTime): Long {
        val nextStart = nextStartDateTime(now)
        return Duration.between(now, nextStart).toMinutes().coerceAtLeast(0)
    }

    private fun nextStartDateTime(now: LocalDateTime): LocalDateTime {
        val todayWindow = resolveWindow(now.toLocalDate())
        return if (now.isBefore(todayWindow.first)) {
            todayWindow.first
        } else {
            resolveWindow(now.toLocalDate().plusDays(1)).first
        }
    }

    private fun activeWindow(now: LocalDateTime): Pair<LocalDateTime, LocalDateTime>? {
        val yesterday = resolveWindow(now.toLocalDate().minusDays(1))
        if (now >= yesterday.first && now < yesterday.second) return yesterday
        val today = resolveWindow(now.toLocalDate())
        if (now >= today.first && now < today.second) return today
        return null
    }

    private fun resolveWindow(anchorDate: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        val start = anchorDate.atTime(getBedtime())
        var end = anchorDate.atTime(getWakeTime())
        if (!end.isAfter(start)) {
            end = end.plusDays(1)
        }
        return start to end
    }

    private fun formatMinutes(totalMinutes: Long): String {
        val safeMinutes = totalMinutes.absoluteValue
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    private fun syncEmergencyExitQuota() {
        val monthKey = YearMonth.now().toString()
        if (prefs.sleepLockEmergencyExitMonth != monthKey) {
            prefs.sleepLockEmergencyExitMonth = monthKey
            prefs.sleepLockEmergencyExitCount = 0
        }
    }

    companion object {
        private const val MAX_EMERGENCY_EXITS_PER_MONTH = 3
    }
}
