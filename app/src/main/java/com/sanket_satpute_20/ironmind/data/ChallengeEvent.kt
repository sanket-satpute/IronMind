package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_events")
data class ChallengeEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: String,
    val eventType: String,
    val dayNumber: Int = 0,
    val date: String,
    val timestamp: Long,
    val routineType: String = "",
    val details: String = "",
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
