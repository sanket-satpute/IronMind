package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_classifications")
data class AppClassification(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: String,
    val source: String,
    val confidence: String,
    val lastDecisionSource: String = "",
    val isSystemApp: Boolean,
    val lastSeenAt: Long,
    val lastPromptedAt: Long,
    val timesPrompted: Int,
    val timesOpenedDuringFocus: Int,
    val timesOpenedOverall: Int,
    val isLockedByUser: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
