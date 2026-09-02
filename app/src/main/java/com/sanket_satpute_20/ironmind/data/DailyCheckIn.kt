package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_checkins")
data class DailyCheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val timestamp: Long,
    val energyScore: Int = 0,
    val stressScore: Int = 0,
    val moodWord: String = "",
    val userType: String = "",
    val mode: String = "",
    val source: String = "",
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
