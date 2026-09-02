package com.sanket_satpute_20.ironmind.data

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseSyncRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun ensureAnonymousAuth() {
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
            } catch (e: Exception) {
                Log.e("SANKET_ERROR_1", "Anonymous auth failed: ${e.message}", e)
            }
        }
    }

    /**
     * Links Google and saves ALL local stats to Firestore.
     */
    suspend fun linkAnonymousWithGoogle(
        credential: AuthCredential, 
        currentXp: Long, 
        currentStreak: Int, 
        currentLevel: Int
    ): String? {
        val user = auth.currentUser ?: return null

        return try {
            val result = user.linkWithCredential(credential).await()
            
            val googleProfile = result.additionalUserInfo?.profile
            val name = googleProfile?.get("name") as? String ?: "Elite Operator"
            val photo = googleProfile?.get("picture") as? String
            val email = result.user?.email

            val profileUpdates = userProfileChangeRequest {
                displayName = name
                photoUri = photo?.let { android.net.Uri.parse(it) }
            }
            user.updateProfile(profileUpdates).await()
            user.reload().await()

            // Save User Document with Stats
            val userData = hashMapOf(
                "uid" to user.uid,
                "name" to name,
                "email" to email,
                "photoUrl" to photo,
                "totalXp" to currentXp,
                "streakCount" to currentStreak,
                "level" to currentLevel,
                "isLinked" to true,
                "lastSync" to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid).set(userData).await()

            name 
        } catch (e: Exception) {
            Log.e("SANKET_ERROR_1", "FIREBASE_LINK_FAILURE: ${e.message}", e)
            null
        }
    }

    /**
     * Updates only the stats in Firestore.
     */
    suspend fun updateCloudStats(xp: Long, streak: Int, level: Int) {
        val user = auth.currentUser ?: return
        try {
            db.collection("users").document(user.uid).update(
                mapOf(
                    "totalXp" to xp,
                    "streakCount" to streak,
                    "level" to level,
                    "lastSync" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("SANKET_ERROR_1", "STATS_SYNC_FAILURE: ${e.message}")
        }
    }

    suspend fun uploadUserStateSnapshot(snapshot: UserStateSnapshot) {
        val userId = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(userId).set(
                mapOf(
                    "uid" to userId,
                    "name" to snapshot.name,
                    "email" to snapshot.email,
                    "photoUrl" to snapshot.photoUrl,
                    "language" to snapshot.language,
                    "userType" to snapshot.userType,
                    "secondaryType" to snapshot.secondaryType,
                    "appMode" to snapshot.appMode,
                    "totalXp" to snapshot.totalXp,
                    "streakCount" to snapshot.streakCount,
                    "level" to snapshot.currentIdentityLevel,
                    "totalCompleted" to snapshot.totalCompleted,
                    "totalSkipped" to snapshot.totalSkipped,
                    "lastCompleteDate" to snapshot.lastCompleteDate,
                    "challengeActive" to snapshot.challengeActive,
                    "challengeDaysCompleted" to snapshot.challengeDaysCompleted,
                    "ironStatusUnlocked" to snapshot.ironStatusUnlocked,
                    "crucibleActive" to snapshot.crucibleActive,
                    "activeCrucibleId" to snapshot.activeCrucibleId,
                    "bossModeActive" to snapshot.bossModeActive,
                    "detoxEnabled" to snapshot.detoxEnabled,
                    "detoxStartHour" to snapshot.detoxStartHour,
                    "detoxEndHour" to snapshot.detoxEndHour,
                    "blockedApps" to snapshot.blockedAppsSerialized.split('|').filter { it.isNotBlank() },
                    "blockedWebsites" to snapshot.blockedWebsitesSerialized.split('|').filter { it.isNotBlank() },
                    "emergencyApps" to snapshot.emergencyAppsSerialized.split('|').filter { it.isNotBlank() },
                    "identityStatements" to snapshot.identityStatementsSerialized.split('|').filter { it.isNotBlank() },
                    "routineType" to snapshot.routineType,
                    "emergencyValveTokens" to snapshot.emergencyValveTokens,
                    "emergencyValveCooldownUntil" to snapshot.emergencyValveCooldownUntil,
                    "lastSync" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("SANKET_ERROR_1", "USER_STATE_SYNC_FAILURE: ${e.message}", e)
        }
    }

    suspend fun uploadTasks(localTasks: List<Task>) {
        val userId = auth.currentUser?.uid ?: return
        val userTasksCollection = db.collection("users").document(userId).collection("tasks")
        for (localTask in localTasks) {
            try {
                // Use task.id as doc ID to prevent duplicates
                userTasksCollection.document(localTask.id.toString()).set(localTask).await()
            } catch (e: Exception) {
                Log.e("SANKET_ERROR_1", "TASK_UPLOAD_FAILURE: ${e.message}", e)
            }
        }
    }

    suspend fun uploadTaskEvents(events: List<TaskEvent>) {
        uploadDocuments("task_events", events) { it.id.toString() }
    }

    suspend fun uploadFocusSessions(sessions: List<FocusSession>) {
        uploadDocuments("focus_sessions", sessions) { it.id.toString() }
    }

    suspend fun uploadDailyCheckIns(checkIns: List<DailyCheckIn>) {
        uploadDocuments("daily_checkins", checkIns) { it.id.toString() }
    }

    suspend fun uploadConfigChangeEvents(events: List<ConfigChangeEvent>) {
        uploadDocuments("config_change_events", events) { it.id.toString() }
    }

    suspend fun uploadEmergencyValveEvents(events: List<EmergencyValveEvent>) {
        uploadDocuments("reset_protocol_events", events) { it.id.toString() }
    }

    suspend fun uploadChallengeEvents(events: List<ChallengeEvent>) {
        uploadDocuments("challenge_events", events) { it.id.toString() }
    }

    suspend fun uploadChallengeDayRecords(records: List<ChallengeDayRecord>) {
        uploadDocuments("challenge_day_records", records) { it.id.toString() }
    }

    suspend fun uploadCrucibleEvents(events: List<CrucibleEvent>) {
        uploadDocuments("crucible_events", events) { it.id.toString() }
    }

    suspend fun uploadCrucibleDayRecords(records: List<CrucibleDayRecord>) {
        uploadDocuments("crucible_day_records", records) { it.id.toString() }
    }

    suspend fun uploadClubChatEvents(events: List<ClubChatEvent>) {
        uploadDocuments("club_chat_events", events) { it.id.toString() }
    }

    suspend fun syncStructuredHistory(db: IronMindDatabase) {
        uploadTaskEvents(db.taskEventDao().getAllEventsSnapshot())
        uploadFocusSessions(db.focusSessionDao().getAllSessionsSnapshot())
        uploadDailyCheckIns(db.dailyCheckInDao().getAllSnapshot())
        uploadConfigChangeEvents(db.configChangeEventDao().getAllSnapshot())
        uploadEmergencyValveEvents(db.emergencyValveEventDao().getAllEventsSnapshot())
        uploadChallengeEvents(db.challengeEventDao().getAllSnapshot())
        uploadChallengeDayRecords(db.challengeDayRecordDao().getAllSnapshot())
        uploadCrucibleEvents(db.crucibleEventDao().getAllSnapshot())
        uploadCrucibleDayRecords(db.crucibleDayRecordDao().getAllSnapshot())
        uploadClubChatEvents(db.clubChatEventDao().getAllSnapshot())
    }

    suspend fun downloadAllTasks(): List<Task> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(userId).collection("tasks").get().await()
            snapshot.toObjects(Task::class.java)
        } catch (e: Exception) {
            Log.e("SANKET_ERROR_1", "TASK_DOWNLOAD_FAILURE: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchCloudStats(): Map<String, Any>? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.data
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchCloudUserStateSnapshot(): UserStateSnapshot? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(userId).get().await()
            val data = doc.data ?: return null
            UserStateSnapshot(
                uid = data["uid"] as? String ?: userId,
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String ?: "",
                language = data["language"] as? String ?: "en",
                userType = data["userType"] as? String ?: "BROKEN_STRIVER",
                secondaryType = data["secondaryType"] as? String ?: "",
                appMode = data["appMode"] as? String ?: "BUILD",
                totalXp = data["totalXp"] as? Long ?: 0L,
                currentIdentityLevel = ((data["level"] as? Long) ?: 1L).toInt(),
                streakCount = ((data["streakCount"] as? Long) ?: 0L).toInt(),
                totalCompleted = ((data["totalCompleted"] as? Long) ?: 0L).toInt(),
                totalSkipped = ((data["totalSkipped"] as? Long) ?: 0L).toInt(),
                lastCompleteDate = data["lastCompleteDate"] as? String ?: "",
                challengeActive = data["challengeActive"] as? Boolean ?: false,
                challengeDaysCompleted = ((data["challengeDaysCompleted"] as? Long) ?: 0L).toInt(),
                ironStatusUnlocked = data["ironStatusUnlocked"] as? Boolean ?: false,
                crucibleActive = data["crucibleActive"] as? Boolean ?: false,
                activeCrucibleId = data["activeCrucibleId"] as? String ?: "",
                bossModeActive = data["bossModeActive"] as? Boolean ?: false,
                detoxEnabled = data["detoxEnabled"] as? Boolean ?: false,
                detoxStartHour = ((data["detoxStartHour"] as? Long) ?: 22L).toInt(),
                detoxEndHour = ((data["detoxEndHour"] as? Long) ?: 6L).toInt(),
                blockedAppsSerialized = ((data["blockedApps"] as? List<*>) ?: emptyList<Any>()).joinToString("|") { it.toString() },
                blockedWebsitesSerialized = ((data["blockedWebsites"] as? List<*>) ?: emptyList<Any>()).joinToString("|") { it.toString() },
                emergencyAppsSerialized = ((data["emergencyApps"] as? List<*>) ?: emptyList<Any>()).joinToString("|") { it.toString() },
                identityStatementsSerialized = ((data["identityStatements"] as? List<*>) ?: emptyList<Any>()).joinToString("|") { it.toString() },
                routineType = data["routineType"] as? String ?: "MONK",
                emergencyValveTokens = ((data["emergencyValveTokens"] as? Long) ?: 3L).toInt(),
                emergencyValveCooldownUntil = data["emergencyValveCooldownUntil"] as? Long ?: 0L
            )
        } catch (e: Exception) {
            Log.e("SANKET_ERROR_1", "USER_STATE_FETCH_FAILURE: ${e.message}", e)
            null
        }
    }

    private suspend fun <T : Any> uploadDocuments(
        collectionName: String,
        items: List<T>,
        idSelector: (T) -> String
    ) {
        val userId = auth.currentUser?.uid ?: return
        val collection = db.collection("users").document(userId).collection(collectionName)
        items.forEach { item ->
            try {
                collection.document(idSelector(item)).set(item).await()
            } catch (e: Exception) {
                Log.e("SANKET_ERROR_1", "$collectionName sync failure: ${e.message}", e)
            }
        }
    }
}
