package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "morning_launch_sessions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["outcome"])
    ]
)
data class MorningLaunchSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val alarmDismissedAt: Long,
    val mode: String,
    val outcome: String,
    val source: String,
    val startedAt: Long,
    val completedAt: Long,
    val currentStepIndex: Int,
    val stepsCompleted: Int,
    val delayCount: Int,
    val totalDurationMs: Long,
    val details: String,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
