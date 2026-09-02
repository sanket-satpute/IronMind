package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_integrity_records")
data class DailyIntegrityRecord(
    @PrimaryKey
    val date: String,
    val plannedCount: Int,
    val completedCount: Int,
    val scorePercent: Int,
    val finalizedAt: Long,
    val modeAtFinalization: String,
    val strictnessEscalatedForNextDay: Boolean,
    val source: String,
    val missCount: Int = 0,
    val shieldState: Int = 0, // crackCount
    val sleepHoursPulled: Float = -1f,
    val reflectionNote: String? = null
)
