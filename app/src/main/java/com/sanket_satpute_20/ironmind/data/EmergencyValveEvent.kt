package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_valve_events")
data class EmergencyValveEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val date: String,
    val triggerReason: String = "UNSPECIFIED",
    val activeTaskName: String = "",
    val focusSessionActive: Boolean = false,
    val tokenConsumed: Boolean = false,
    val tokensRemainingAfter: Int = 0,
    val selectedRecoveryAction: String = "",
    val completed: Boolean = false
)
