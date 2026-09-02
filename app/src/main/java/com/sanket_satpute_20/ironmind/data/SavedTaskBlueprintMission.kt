package com.sanket_satpute_20.ironmind.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_task_blueprint_missions",
    foreignKeys = [
        ForeignKey(
            entity = SavedTaskBlueprint::class,
            parentColumns = ["id"],
            childColumns = ["blueprintId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["blueprintId"])]
)
data class SavedTaskBlueprintMission(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val blueprintId: String,
    val missionOrder: Int,
    val name: String,
    val startTime: String,
    val endTime: String,
    val importanceRank: Int,
    val type: String,
    val taskType: String = "OTHER",
    val focusModeEnabled: Boolean = false,
    val focusPreset: String = "CLASSIC_25_5",
    val parentMissionId: String = "",
    val segmentBaseName: String = "",
    val segmentIndex: Int = 0,
    val segmentCount: Int = 0,
    val isBreakSegment: Boolean = false,
    val packageName: String?
)
