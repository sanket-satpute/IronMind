package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_task_blueprints")
data class SavedTaskBlueprint(
    @PrimaryKey
    val id: String,
    val title: String,
    val summary: String,
    val createdAt: Long,
    val lastModified: Long = System.currentTimeMillis()
)
