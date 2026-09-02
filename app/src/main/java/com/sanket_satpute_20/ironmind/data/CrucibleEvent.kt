package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crucible_events")
data class CrucibleEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: String,
    val crucibleId: String,
    val crucibleTitle: String,
    val eventType: String,
    val dayNumber: Int = 0,
    val completedDaysSnapshot: Int = 0,
    val totalDays: Int = 0,
    val penaltyType: String = "",
    val sessionDurationMinutes: Int = 0,
    val date: String,
    val timestamp: Long,
    val details: String = "",
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
