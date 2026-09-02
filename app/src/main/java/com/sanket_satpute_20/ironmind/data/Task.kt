package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false,
    val date: String = "",

    // --- The Daily 6: Priority System ---
    val importanceRank: Int = 3,           // 1: Critical, 2: Vital, 3: Growth
    val isMissionSix: Boolean = true,      // Only 6 tasks allowed per day

    // --- Elite Tier Features ---
    val isDeferred: Boolean = false,       // For the Bedtime Reaper
    val isInProgress: Boolean = false,     // For live session tracking
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val skippedAt: Long? = null,
    val deferredAt: Long? = null,
    val skipReason: String = "",
    val completionSource: String = "",
    val origin: String = "MANUAL",

    // --- Sync & Conflict Resolution ---
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING",

    // --- Reschedule & Pattern Analysis ---
    val isRescheduled: Boolean = false,
    val originalStartTime: String = "",
    val originalEndTime: String = "",
    val rescheduleReason: String = "",

    // --- AI & Identity Engine ---
    val focusScore: Int = 0,
    val focusNotes: String = "",

    // --- Gamification Engine ---
    val durationMinutes: Int = 0,
    val difficultyMultiplier: Float = 1.0f,

    // --- Focus Mode / Pomodoro ---
    val taskType: String = "OTHER",
    val focusModeEnabled: Boolean = false,
    val focusPreset: String = "CLASSIC_25_5",

    // --- Split Mission Support ---
    val parentMissionId: String = "",
    val segmentBaseName: String = "",
    val segmentIndex: Int = 0,
    val segmentCount: Int = 0,
    val isBreakSegment: Boolean = false,

    // --- Identification ---
    val packageName: String? = null // To track which app was used for work
)
