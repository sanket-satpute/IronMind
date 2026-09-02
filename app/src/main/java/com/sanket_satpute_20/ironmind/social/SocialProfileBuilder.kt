package com.sanket_satpute_20.ironmind.social

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.TaskEvent
import java.time.LocalDate

object SocialProfileBuilder {

    suspend fun build(
        context: Context,
        settingsOverride: SocialPrivacySettings? = null
    ): SocialProfile {
        val prefs = PrefManager.getInstance(context)
        val settings = settingsOverride ?: buildSettings(context)
        val db = IronMindDatabase.getDatabase(context)
        val taskEvents = db.taskEventDao().getAllEventsSnapshot()
        val challengeDays = db.challengeDayRecordDao().getAllSnapshot()
        val crucibleDays = db.crucibleDayRecordDao().getAllSnapshot()
        val firebaseUser = Firebase.auth.currentUser
        val now = System.currentTimeMillis()

        return SocialProfile(
            uid = firebaseUser?.uid ?: prefs.firebaseUid,
            displayName = firebaseUser?.displayName ?: prefs.userName.ifBlank { "IronMind User" },
            inviteCode = buildInviteCode(firebaseUser?.uid ?: prefs.firebaseUid),
            streakBand = if (settings.shareStreakBand) buildStreakBand(prefs.streakCount) else null,
            consistencyTrend = if (settings.shareConsistencyTrend) buildConsistencyTrend(taskEvents) else null,
            verifiedFiveAmThisWeek = if (settings.shareFiveAmWeekly) {
                countVerifiedFiveAmThisWeek(challengeDays.map { it.date })
            } else {
                null
            },
            titles = if (settings.shareTitles) buildTitles(prefs) else emptyList(),
            activeThisWeek = hasActivityThisWeek(taskEvents, challengeDays.map { it.date }, crucibleDays.map { it.date }),
            profileUpdatedAt = now
        )
    }

    fun buildSettings(context: Context): SocialPrivacySettings {
        val prefs = PrefManager.getInstance(context)
        return SocialPrivacySettings(
            ironCircleEnabled = prefs.ironCircleEnabled,
            discoverableByContacts = prefs.socialDiscoverable,
            shareStreakBand = prefs.socialShareStreakBand,
            shareConsistencyTrend = prefs.socialShareConsistencyTrend,
            shareFiveAmWeekly = prefs.socialShareFiveAmWeekly,
            shareTitles = prefs.socialShareTitles
        )
    }

    private fun buildInviteCode(uid: String): String {
        if (uid.isBlank()) return "IRON-LOCKED"
        return "IRON-${uid.takeLast(6).uppercase()}"
    }

    private fun buildStreakBand(streak: Int): String {
        return when {
            streak <= 0 -> "Offline"
            streak <= 3 -> "1-3"
            streak <= 7 -> "4-7"
            streak <= 14 -> "8-14"
            streak <= 30 -> "15-30"
            else -> "30+"
        }
    }

    private fun buildConsistencyTrend(events: List<TaskEvent>): ConsistencyTrend {
        val today = LocalDate.now()
        val recent = completionRateForWindow(events, today.minusDays(6), today)
        val previous = completionRateForWindow(events, today.minusDays(13), today.minusDays(7))
        return when {
            recent - previous >= 0.15f -> ConsistencyTrend.RISING
            previous - recent >= 0.15f -> ConsistencyTrend.SLIPPING
            else -> ConsistencyTrend.STABLE
        }
    }

    private fun completionRateForWindow(events: List<TaskEvent>, start: LocalDate, end: LocalDate): Float {
        val relevant = events.filter { event ->
            val date = runCatching { LocalDate.parse(event.date) }.getOrNull() ?: return@filter false
            !date.isBefore(start) && !date.isAfter(end) && (event.eventType == "COMPLETED" || event.eventType == "SKIPPED")
        }
        if (relevant.isEmpty()) return 0f
        val completed = relevant.count { it.eventType == "COMPLETED" }
        return completed.toFloat() / relevant.size.toFloat()
    }

    private fun countVerifiedFiveAmThisWeek(dates: List<String>): Int {
        val cutoff = LocalDate.now().minusDays(6)
        return dates.count { dateString ->
            val date = runCatching { LocalDate.parse(dateString) }.getOrNull() ?: return@count false
            !date.isBefore(cutoff)
        }
    }

    private fun buildTitles(prefs: PrefManager): List<String> {
        val titles = mutableListOf<String>()
        if (prefs.ironStatusUnlocked) titles += "Iron Status"
        titles += prefs.earnedCrucibleTitles.sorted()
        return titles.take(4)
    }

    private fun hasActivityThisWeek(taskEvents: List<TaskEvent>, challengeDates: List<String>, crucibleDates: List<String>): Boolean {
        val cutoff = LocalDate.now().minusDays(6)
        val hasTaskActivity = taskEvents.any { event ->
            val date = runCatching { LocalDate.parse(event.date) }.getOrNull() ?: return@any false
            !date.isBefore(cutoff)
        }
        val hasChallenge = challengeDates.any { dateString ->
            val date = runCatching { LocalDate.parse(dateString) }.getOrNull() ?: return@any false
            !date.isBefore(cutoff)
        }
        val hasCrucible = crucibleDates.any { dateString ->
            val date = runCatching { LocalDate.parse(dateString) }.getOrNull() ?: return@any false
            !date.isBefore(cutoff)
        }
        return hasTaskActivity || hasChallenge || hasCrucible
    }
}
