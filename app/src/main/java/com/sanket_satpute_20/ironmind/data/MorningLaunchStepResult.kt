package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "morning_launch_step_results",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["mode"]),
        Index(value = ["stepId"])
    ]
)
data class MorningLaunchStepResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val mode: String,
    val stepId: String,
    val stepOrder: Int,
    val targetValue: Int,
    val actualValue: Int,
    val unit: String,
    val completed: Boolean,
    val skipped: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L
)
