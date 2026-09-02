package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_events")
data class TaskEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Int,
    val taskName: String,
    val date: String,
    val eventType: String,
    val timestamp: Long,
    val oldStartTime: String = "",
    val oldEndTime: String = "",
    val newStartTime: String = "",
    val newEndTime: String = "",
    val reason: String = "",
    val focusScoreSnapshot: Int = 0,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
