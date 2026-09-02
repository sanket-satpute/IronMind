package com.sanket_satpute_20.ironmind.data

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

object HistoryRecorder {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun recordDailyCheckIn(
        context: Context,
        energyScore: Int,
        stressScore: Int,
        moodWord: String,
        source: String
    ) {
        val prefs = PrefManager.getInstance(context)
        val now = System.currentTimeMillis()
        scope.launch {
            IronMindDatabase.getDatabase(context).dailyCheckInDao().insert(
                DailyCheckIn(
                    date = LocalDate.now().toString(),
                    timestamp = now,
                    energyScore = energyScore,
                    stressScore = stressScore,
                    moodWord = moodWord,
                    userType = prefs.userType,
                    mode = prefs.appMode,
                    source = source,
                    lastModified = now
                )
            )
            upsertUserStateSnapshot(context)
        }
    }

    fun recordConfigChange(
        context: Context,
        configType: String,
        oldValue: Any?,
        newValue: Any?,
        sourceScreen: String
    ) {
        val oldSerialized = serializeValue(oldValue)
        val newSerialized = serializeValue(newValue)
        if (oldSerialized == newSerialized) return

        val now = System.currentTimeMillis()
        scope.launch {
            IronMindDatabase.getDatabase(context).configChangeEventDao().insert(
                ConfigChangeEvent(
                    timestamp = now,
                    date = LocalDate.now().toString(),
                    configType = configType,
                    oldValueJson = oldSerialized,
                    newValueJson = newSerialized,
                    sourceScreen = sourceScreen,
                    lastModified = now
                )
            )
            upsertUserStateSnapshot(context)
        }
    }

    fun recordUserStateSnapshot(context: Context) {
        scope.launch {
            upsertUserStateSnapshot(context)
        }
    }

    fun recordChallengeEvent(
        context: Context,
        runId: String,
        eventType: String,
        dayNumber: Int = 0,
        routineType: String = "",
        details: String = ""
    ) {
        if (runId.isBlank()) return

        val now = System.currentTimeMillis()
        scope.launch {
            IronMindDatabase.getDatabase(context).challengeEventDao().insert(
                ChallengeEvent(
                    runId = runId,
                    eventType = eventType,
                    dayNumber = dayNumber,
                    date = LocalDate.now().toString(),
                    timestamp = now,
                    routineType = routineType,
                    details = details,
                    lastModified = now
                )
            )
        }
    }

    fun recordChallengeDay(
        context: Context,
        runId: String,
        dayNumber: Int,
        routineType: String,
        completedAt: Long = System.currentTimeMillis()
    ) {
        if (runId.isBlank()) return

        scope.launch {
            IronMindDatabase.getDatabase(context).challengeDayRecordDao().insert(
                ChallengeDayRecord(
                    runId = runId,
                    date = LocalDate.now().toString(),
                    dayNumber = dayNumber,
                    completedAt = completedAt,
                    routineType = routineType,
                    lastModified = completedAt
                )
            )
        }
    }

    fun recordCrucibleEvent(
        context: Context,
        runId: String,
        crucibleId: String,
        crucibleTitle: String,
        eventType: String,
        dayNumber: Int = 0,
        completedDaysSnapshot: Int = 0,
        totalDays: Int = 0,
        penaltyType: String = "",
        sessionDurationMinutes: Int = 0,
        details: String = ""
    ) {
        if (runId.isBlank() || crucibleId.isBlank()) return

        val now = System.currentTimeMillis()
        scope.launch {
            IronMindDatabase.getDatabase(context).crucibleEventDao().insert(
                CrucibleEvent(
                    runId = runId,
                    crucibleId = crucibleId,
                    crucibleTitle = crucibleTitle,
                    eventType = eventType,
                    dayNumber = dayNumber,
                    completedDaysSnapshot = completedDaysSnapshot,
                    totalDays = totalDays,
                    penaltyType = penaltyType,
                    sessionDurationMinutes = sessionDurationMinutes,
                    date = LocalDate.now().toString(),
                    timestamp = now,
                    details = details,
                    lastModified = now
                )
            )
        }
    }

    fun recordCrucibleDay(
        context: Context,
        runId: String,
        crucibleId: String,
        crucibleTitle: String,
        dayNumber: Int,
        sessionDurationMinutes: Int,
        penaltyType: String,
        completedAt: Long = System.currentTimeMillis()
    ) {
        if (runId.isBlank() || crucibleId.isBlank()) return

        scope.launch {
            IronMindDatabase.getDatabase(context).crucibleDayRecordDao().insert(
                CrucibleDayRecord(
                    runId = runId,
                    crucibleId = crucibleId,
                    crucibleTitle = crucibleTitle,
                    date = LocalDate.now().toString(),
                    dayNumber = dayNumber,
                    completedAt = completedAt,
                    sessionDurationMinutes = sessionDurationMinutes,
                    penaltyType = penaltyType,
                    lastModified = completedAt
                )
            )
        }
    }

    fun recordClubChatEvent(
        context: Context,
        eventType: String,
        membershipTier: String,
        challengeDaysSnapshot: Int,
        streakBand: String,
        postCountDelta: Int = 0,
        details: String = ""
    ) {
        val now = System.currentTimeMillis()
        scope.launch {
            IronMindDatabase.getDatabase(context).clubChatEventDao().insert(
                ClubChatEvent(
                    eventType = eventType,
                    membershipTier = membershipTier,
                    challengeDaysSnapshot = challengeDaysSnapshot,
                    streakBand = streakBand,
                    postCountDelta = postCountDelta,
                    date = LocalDate.now().toString(),
                    timestamp = now,
                    details = details,
                    lastModified = now
                )
            )
        }
    }

    fun buildUserStateSnapshot(context: Context): UserStateSnapshot {
        val prefs = PrefManager.getInstance(context)
        val firebaseUser = Firebase.auth.currentUser
        val now = System.currentTimeMillis()
        return UserStateSnapshot(
            uid = firebaseUser?.uid ?: prefs.firebaseUid,
            name = firebaseUser?.displayName ?: prefs.userName,
            email = firebaseUser?.email ?: "",
            photoUrl = firebaseUser?.photoUrl?.toString() ?: "",
            language = prefs.appLanguage,
            userType = prefs.userType,
            secondaryType = prefs.secondaryType,
            appMode = prefs.appMode,
            totalXp = prefs.totalXp,
            currentIdentityLevel = prefs.currentIdentityLevel,
            streakCount = prefs.streakCount,
            totalCompleted = prefs.totalCompleted,
            totalSkipped = prefs.totalSkipped,
            lastCompleteDate = prefs.lastCompleteDate,
            challengeActive = prefs.challengeActive,
            challengeDaysCompleted = prefs.challengeDaysCompleted,
            ironStatusUnlocked = prefs.ironStatusUnlocked,
            crucibleActive = prefs.crucibleActive,
            activeCrucibleId = prefs.activeCrucibleId,
            bossModeActive = prefs.bossModeActive,
            detoxEnabled = prefs.detoxEnabled,
            detoxStartHour = prefs.detoxStartHour,
            detoxEndHour = prefs.detoxEndHour,
            blockedAppsSerialized = serializeValue(prefs.blockedApps),
            blockedWebsitesSerialized = serializeValue(prefs.blockedWebsites),
            emergencyAppsSerialized = serializeValue(prefs.emergencyApps),
            identityStatementsSerialized = serializeValue(prefs.identityStatements),
            routineType = prefs.routineType,
            emergencyValveTokens = prefs.emergencyValveTokens,
            emergencyValveCooldownUntil = prefs.emergencyValveCooldownUntil,
            lastModified = now
        )
    }

    fun applyUserStateSnapshotToPrefs(snapshot: UserStateSnapshot, prefs: PrefManager) {
        if (snapshot.name.isNotBlank()) prefs.userName = snapshot.name
        prefs.appLanguage = snapshot.language
        prefs.userType = snapshot.userType
        prefs.secondaryType = snapshot.secondaryType
        prefs.appMode = snapshot.appMode
        prefs.totalXp = snapshot.totalXp
        prefs.currentIdentityLevel = snapshot.currentIdentityLevel
        prefs.streakCount = snapshot.streakCount
        prefs.totalCompleted = snapshot.totalCompleted
        prefs.totalSkipped = snapshot.totalSkipped
        prefs.lastCompleteDate = snapshot.lastCompleteDate
        prefs.challengeActive = snapshot.challengeActive
        prefs.challengeDaysCompleted = snapshot.challengeDaysCompleted
        prefs.ironStatusUnlocked = snapshot.ironStatusUnlocked
        prefs.crucibleActive = snapshot.crucibleActive
        prefs.activeCrucibleId = snapshot.activeCrucibleId
        prefs.bossModeActive = snapshot.bossModeActive
        prefs.detoxEnabled = snapshot.detoxEnabled
        prefs.detoxStartHour = snapshot.detoxStartHour
        prefs.detoxEndHour = snapshot.detoxEndHour
        prefs.blockedApps = deserializeSet(snapshot.blockedAppsSerialized)
        prefs.blockedWebsites = deserializeSet(snapshot.blockedWebsitesSerialized)
        prefs.emergencyApps = deserializeSet(snapshot.emergencyAppsSerialized)
        prefs.identityStatements = deserializeSet(snapshot.identityStatementsSerialized)
        prefs.routineType = snapshot.routineType
        prefs.emergencyValveTokens = snapshot.emergencyValveTokens
        prefs.emergencyValveCooldownUntil = snapshot.emergencyValveCooldownUntil
        if (snapshot.uid.isNotBlank()) prefs.firebaseUid = snapshot.uid
    }

    private fun serializeValue(value: Any?): String {
        return when (value) {
            null -> ""
            is Set<*> -> value.filterNotNull().map { it.toString() }.sorted().joinToString("|")
            is List<*> -> value.filterNotNull().map { it.toString() }.joinToString("|")
            else -> value.toString()
        }
    }

    private suspend fun upsertUserStateSnapshot(context: Context) {
        IronMindDatabase.getDatabase(context).userStateSnapshotDao().upsert(
            buildUserStateSnapshot(context)
        )
    }

    private fun deserializeSet(serialized: String): Set<String> {
        if (serialized.isBlank()) return emptySet()
        return serialized.split('|').filter { it.isNotBlank() }.toSet()
    }
}
