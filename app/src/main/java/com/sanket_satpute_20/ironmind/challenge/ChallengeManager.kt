package com.sanket_satpute_20.ironmind.challenge

import android.content.Context
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.GlobalConstants.CHALLENGE_DURATION_DAYS
import com.sanket_satpute_20.ironmind.data.GlobalConstants.CHAT_READ_ACCESS_STREAK
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object ChallengeManager {

    fun startChallenge(context: Context) {
        val prefs = PrefManager.getInstance(context)
        val runStartedAt = System.currentTimeMillis()
        val previousWakeHour = prefs.sleepLockWakeHour
        val previousWakeMinute = prefs.sleepLockWakeMinute
        prefs.challengeActive = true
        prefs.challengeAlarmActive = false
        prefs.challengeAlarmDate = ""
        prefs.challengeStartDate = LocalDate.now().toString()
        prefs.challengeDaysCompleted = 0
        prefs.challengeLastCompleted = ""
        prefs.challengeRunStartedAt = runStartedAt
        prefs.routineCompletedToday = false
        prefs.routineDate = ""
        prefs.sleepLockStoredWakeHourBeforeChallenge = previousWakeHour
        prefs.sleepLockStoredWakeMinuteBeforeChallenge = previousWakeMinute
        prefs.sleepLockWakeHour = 5
        prefs.sleepLockWakeMinute = 0

        if (previousWakeHour != 5 || previousWakeMinute != 0) {
            HistoryRecorder.recordConfigChange(
                context,
                "SLEEP_LOCK_WAKE_TIME",
                "%02d:%02d".format(previousWakeHour, previousWakeMinute),
                "05:00",
                "CHALLENGE_START"
            )
        }

        HistoryRecorder.recordChallengeEvent(
            context = context,
            runId = buildRunId(runStartedAt),
            eventType = "STARTED",
            routineType = prefs.routineType
        )

        // Schedule the 5 AM alarm
        ChallengeAlarmScheduler.scheduleDailyAlarm(context)
    }

    fun recordDayCompleted(context: Context): ChallengeDayResult {
        val prefs = PrefManager.getInstance(context)
        reconcileChallengeState(context)

        if (!prefs.challengeActive) {
            return ChallengeDayResult(
                recorded = false,
                daysCompleted = prefs.challengeDaysCompleted,
                challengeCompleted = prefs.ironStatusUnlocked,
                alreadyCompletedToday = false
            )
        }

        if (hasCompletedToday(context)) {
            return ChallengeDayResult(
                recorded = false,
                daysCompleted = prefs.challengeDaysCompleted,
                challengeCompleted = prefs.ironStatusUnlocked,
                alreadyCompletedToday = true
            )
        }

        val current = prefs.challengeDaysCompleted
        val newCount = current + 1

        prefs.challengeDaysCompleted = newCount
        prefs.challengeLastCompleted = LocalDate.now().toString()
        val runId = getRunId(prefs)
        HistoryRecorder.recordChallengeDay(
            context = context,
            runId = runId,
            dayNumber = newCount,
            routineType = prefs.routineType
        )
        HistoryRecorder.recordChallengeEvent(
            context = context,
            runId = runId,
            eventType = "COMPLETED_DAY",
            dayNumber = newCount,
            routineType = prefs.routineType
        )

        val unlockedReadAccess = !prefs.chatMinStreakMet && newCount >= CHAT_READ_ACCESS_STREAK
        updateChatUnlocks(prefs)
        if (unlockedReadAccess) {
            HistoryRecorder.recordChallengeEvent(
                context = context,
                runId = runId,
                eventType = "UNLOCKED_READ_ACCESS",
                dayNumber = newCount,
                routineType = prefs.routineType
            )
        }

        val challengeCompleted = newCount >= CHALLENGE_DURATION_DAYS
        if (challengeCompleted) {
            prefs.ironStatusUnlocked = true
            prefs.chatUnlocked = true
            prefs.challengeActive = false
            prefs.challengeAlarmActive = false
            prefs.challengeAlarmDate = ""
            ChallengeAlarmScheduler.cancelAlarm(context)
            restoreSleepLockWakeTimeIfNeeded(context, prefs)
            HistoryRecorder.recordChallengeEvent(
                context = context,
                runId = runId,
                eventType = "COMPLETED_CHALLENGE",
                dayNumber = newCount,
                routineType = prefs.routineType
            )
        }

        return ChallengeDayResult(
            recorded = true,
            daysCompleted = newCount,
            challengeCompleted = challengeCompleted,
            alreadyCompletedToday = false
        )
    }

    fun getChallengeStatus(context: Context): ChallengeStatus {
        val prefs = PrefManager.getInstance(context)
        reconcileChallengeState(context)
        return ChallengeStatus(
            isActive    = prefs.challengeActive,
            daysCompleted = prefs.challengeDaysCompleted,
            startDate   = prefs.challengeStartDate,
            ironStatus  = prefs.ironStatusUnlocked,
            lastCompletedDate = prefs.challengeLastCompleted
        )
    }

    fun hasCompletedToday(context: Context): Boolean {
        val prefs = PrefManager.getInstance(context)
        val lastCompleted = prefs.challengeLastCompleted
        return lastCompleted == LocalDate.now().toString()
    }

    fun calculateQuitPenalty(context: Context): Int {
        val prefs = PrefManager.getInstance(context)
        if (!prefs.challengeActive) return 0
        val basePenalty = 40
        val progressPenalty = prefs.challengeDaysCompleted * 6
        return (basePenalty + progressPenalty).coerceAtMost(180)
    }

    fun stopChallenge(context: Context, reason: String = "USER_DISABLED"): Int {
        val prefs = PrefManager.getInstance(context)
        if (!prefs.challengeActive) return 0

        val xpPenalty = calculateQuitPenalty(context)
        val runId = getRunId(prefs)
        com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(context).penalizeXp(xpPenalty.toLong())
        prefs.challengeActive = false
        prefs.challengeAlarmActive = false
        prefs.challengeAlarmDate = ""
        ChallengeAlarmScheduler.cancelAlarm(context)
        restoreSleepLockWakeTimeIfNeeded(context, prefs)

        HistoryRecorder.recordChallengeEvent(
            context = context,
            runId = runId,
            eventType = "STOPPED_EARLY",
            dayNumber = prefs.challengeDaysCompleted,
            routineType = prefs.routineType,
            details = "$reason|XP_LOSS=$xpPenalty"
        )

        return xpPenalty
    }

    fun reconcileChallengeState(context: Context): ChallengeStatus {
        val prefs = PrefManager.getInstance(context)
        val today = LocalDate.now().toString()
        if (prefs.challengeAlarmDate != today) {
            prefs.challengeAlarmActive = false
            prefs.challengeAlarmDate = ""
        }
        if (shouldResetForMissedDay(prefs)) {
            val failedRunId = getRunId(prefs)
            val failedDays = prefs.challengeDaysCompleted
            HistoryRecorder.recordChallengeEvent(
                context = context,
                runId = failedRunId,
                eventType = "FAILED",
                dayNumber = failedDays,
                routineType = prefs.routineType,
                details = "MISSED_DAY"
            )
            resetActiveRun(prefs)
            HistoryRecorder.recordChallengeEvent(
                context = context,
                runId = getRunId(prefs),
                eventType = "RESET",
                routineType = prefs.routineType,
                details = "MISSED_DAY"
            )
        }

        return ChallengeStatus(
            isActive = prefs.challengeActive,
            daysCompleted = prefs.challengeDaysCompleted,
            startDate = prefs.challengeStartDate,
            ironStatus = prefs.ironStatusUnlocked,
            lastCompletedDate = prefs.challengeLastCompleted
        )
    }

    fun canReadClubChat(context: Context): Boolean {
        val prefs = PrefManager.getInstance(context)
        reconcileChallengeState(context)
        return prefs.chatMinStreakMet || prefs.chatUnlocked
    }

    fun canPostClubChat(context: Context): Boolean {
        val prefs = PrefManager.getInstance(context)
        reconcileChallengeState(context)
        return prefs.chatUnlocked
    }

    private fun shouldResetForMissedDay(prefs: PrefManager): Boolean {
        if (!prefs.challengeActive) return false

        val today = LocalDate.now()
        val lastCompleted = prefs.challengeLastCompleted.takeIf { it.isNotBlank() }

        if (lastCompleted != null) {
            val lastCompletedDate = runCatching { LocalDate.parse(lastCompleted) }.getOrNull() ?: return false
            return lastCompletedDate.plusDays(1).isBefore(today)
        }

        val firstRequiredMorning = firstRequiredMorningDate(prefs) ?: return false
        return firstRequiredMorning.isBefore(today)
    }

    private fun resetActiveRun(prefs: PrefManager) {
        prefs.challengeRunStartedAt = System.currentTimeMillis()
        prefs.challengeDaysCompleted = 0
        prefs.challengeLastCompleted = ""
        prefs.challengeAlarmActive = false
        prefs.challengeAlarmDate = ""
        prefs.challengeStartDate = LocalDate.now().toString()
        prefs.routineCompletedToday = false
        prefs.routineDate = ""
    }

    private fun updateChatUnlocks(prefs: PrefManager) {
        if (prefs.challengeDaysCompleted >= CHAT_READ_ACCESS_STREAK) {
            prefs.chatMinStreakMet = true
        }
        if (prefs.challengeDaysCompleted >= CHALLENGE_DURATION_DAYS || prefs.ironStatusUnlocked) {
            prefs.chatUnlocked = true
        }
    }

    private fun getRunId(prefs: PrefManager): String {
        val startedAt = prefs.challengeRunStartedAt.takeIf { it > 0L } ?: System.currentTimeMillis().also {
            prefs.challengeRunStartedAt = it
        }
        return buildRunId(startedAt)
    }

    private fun buildRunId(startedAt: Long): String = "challenge_run_$startedAt"

    private fun firstRequiredMorningDate(prefs: PrefManager): LocalDate? {
        val startedAt = prefs.challengeRunStartedAt.takeIf { it > 0L } ?: return null
        val startedDateTime = Instant.ofEpochMilli(startedAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val startedDate = startedDateTime.toLocalDate()
        val startedBeforeOrAtWake =
            startedDateTime.hour < 5 || (startedDateTime.hour == 5 && startedDateTime.minute == 0)

        return if (startedBeforeOrAtWake) startedDate else startedDate.plusDays(1)
    }

    private fun restoreSleepLockWakeTimeIfNeeded(context: Context, prefs: PrefManager) {
        val storedHour = prefs.sleepLockStoredWakeHourBeforeChallenge
        val storedMinute = prefs.sleepLockStoredWakeMinuteBeforeChallenge
        if (storedHour !in 0..23 || storedMinute !in 0..59) return

        val previousWake = "%02d:%02d".format(prefs.sleepLockWakeHour, prefs.sleepLockWakeMinute)
        prefs.sleepLockWakeHour = storedHour
        prefs.sleepLockWakeMinute = storedMinute
        prefs.sleepLockStoredWakeHourBeforeChallenge = -1
        prefs.sleepLockStoredWakeMinuteBeforeChallenge = -1

        HistoryRecorder.recordConfigChange(
            context,
            "SLEEP_LOCK_WAKE_TIME",
            previousWake,
            "%02d:%02d".format(storedHour, storedMinute),
            "CHALLENGE_END"
        )
    }

    fun generateMathProblem(): MathProblem {
        val operations = listOf('+', '-', '*')
        val op = operations.random()

        val (a, b, answer) = when (op) {
            '+' -> {
                val x = (20..99).random()
                val y = (10..60).random()
                Triple(x, y, x + y)
            }
            '-' -> {
                val x = (40..99).random()
                val y = (10..39).random()
                Triple(x, y, x - y)
            }
            '*' -> {
                val x = (3..12).random()
                val y = (3..12).random()
                Triple(x, y, x * y)
            }
            else -> Triple(5, 5, 10)
        }

        val symbol = when (op) {
            '+' -> "+"
            '-' -> "−"
            '*' -> "×"
            else -> "+"
        }

        return MathProblem(
            question = "$a $symbol $b = ?",
            answer = answer
        )
    }
}

data class ChallengeStatus(
    val isActive: Boolean,
    val daysCompleted: Int,
    val startDate: String?,
    val ironStatus: Boolean,
    val lastCompletedDate: String?
)

data class MathProblem(
    val question: String,
    val answer: Int
)

data class ChallengeDayResult(
    val recorded: Boolean,
    val daysCompleted: Int,
    val challengeCompleted: Boolean,
    val alreadyCompletedToday: Boolean
)
