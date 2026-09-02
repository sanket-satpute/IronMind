package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config_change_events")
data class ConfigChangeEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val date: String,
    val configType: String,
    val oldValueJson: String,
    val newValueJson: String,
    val sourceScreen: String,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
