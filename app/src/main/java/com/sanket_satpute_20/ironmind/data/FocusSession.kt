package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Int,
    val taskName: String,
    val date: String,
    val startTimestamp: Long,
    val endTimestamp: Long? = null,
    val plannedDurationMinutes: Int = 0,
    val actualDurationMinutes: Int = 0,
    val result: String = "IN_PROGRESS",
    val interruptionCount: Int = 0,
    val usedResetProtocol: Boolean = false,
    val cooldownUsed: Boolean = false,
    val punishmentTriggered: Boolean = false,
    val completed: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
