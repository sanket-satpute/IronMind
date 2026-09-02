package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_day_records")
data class ChallengeDayRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: String,
    val date: String,
    val dayNumber: Int,
    val completedAt: Long,
    val routineType: String = "",
    val mathSolved: Boolean = true,
    val completed: Boolean = true,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
