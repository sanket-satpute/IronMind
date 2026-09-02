package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crucible_day_records")
data class CrucibleDayRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: String,
    val crucibleId: String,
    val crucibleTitle: String,
    val date: String,
    val dayNumber: Int,
    val completedAt: Long,
    val sessionDurationMinutes: Int = 0,
    val penaltyType: String = "",
    val completed: Boolean = true,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
