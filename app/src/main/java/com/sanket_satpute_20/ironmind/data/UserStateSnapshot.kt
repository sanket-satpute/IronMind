package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_state_snapshot")
data class UserStateSnapshot(
    @PrimaryKey
    val key: String = "primary",
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val language: String = "en",
    val userType: String = "BROKEN_STRIVER",
    val secondaryType: String = "",
    val appMode: String = "BUILD",
    val totalXp: Long = 0L,
    val currentIdentityLevel: Int = 1,
    val streakCount: Int = 0,
    val totalCompleted: Int = 0,
    val totalSkipped: Int = 0,
    val lastCompleteDate: String = "",
    val challengeActive: Boolean = false,
    val challengeDaysCompleted: Int = 0,
    val ironStatusUnlocked: Boolean = false,
    val crucibleActive: Boolean = false,
    val activeCrucibleId: String = "",
    val bossModeActive: Boolean = false,
    val detoxEnabled: Boolean = false,
    val detoxStartHour: Int = 22,
    val detoxEndHour: Int = 6,
    val blockedAppsSerialized: String = "",
    val blockedWebsitesSerialized: String = "",
    val emergencyAppsSerialized: String = "",
    val identityStatementsSerialized: String = "",
    val routineType: String = "MONK",
    val emergencyValveTokens: Int = 3,
    val emergencyValveCooldownUntil: Long = 0L,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
