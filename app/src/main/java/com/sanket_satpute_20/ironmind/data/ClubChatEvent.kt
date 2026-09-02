package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "club_chat_events")
data class ClubChatEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val membershipTier: String = "",
    val challengeDaysSnapshot: Int = 0,
    val streakBand: String = "",
    val postCountDelta: Int = 0,
    val date: String,
    val timestamp: Long,
    val details: String = "",
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
