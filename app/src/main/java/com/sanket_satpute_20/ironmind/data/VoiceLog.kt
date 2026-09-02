package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_logs")
data class VoiceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskName: String,
    val date: String,           // "2025-01-20"
    val month: String,          // "2025-01"
    val filePath: String,       // absolute path to .3gp file
    val durationSeconds: Int,
    val timestamp: Long,
    
    // AI Focus Partner 2.0 Fields
    val transcription: String = "",
    val moodVerdict: String = "NEUTRAL", // STRESSED, NEUTRAL, POWERFUL
    val aiSuggestion: String = "",

    // Confidence Voice Log Fields
    val entryType: String = "GENERAL",
    val promptType: String = "",
    val clarityScore: Int = 0,
    val confidenceScore: Int = 0,
    val playbackCompleted: Boolean = false
)
