package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "confidence_ignition_entries",
    indices = [Index(value = ["completed"])]
)
data class ConfidenceIgnitionEntry(
    @PrimaryKey
    val date: String,
    val ignitionLineId: String,
    val voicePromptId: String,
    val movePromptId: String,
    val couragePromptId: String,
    val breathDone: Boolean = false,
    val voiceRecorded: Boolean = false,
    val moveDone: Boolean = false,
    val courageAccepted: Boolean = false,
    val completed: Boolean = false,
    val completedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
