package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temptation_logs")
data class TemptationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blockedAppName: String,      // "Instagram"
    val blockedPackage: String,      // "com.instagram.android"
    val duringTaskName: String,      // "CodeMate Work"
    val timestamp: Long,             // System.currentTimeMillis()
    val date: String                 // "2025-01-20" for daily grouping
)