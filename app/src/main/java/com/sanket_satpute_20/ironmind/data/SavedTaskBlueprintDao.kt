package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

data class SavedTaskBlueprintWithMissions(
    @Embedded
    val blueprint: SavedTaskBlueprint,
    @Relation(
        parentColumn = "id",
        entityColumn = "blueprintId",
        entity = SavedTaskBlueprintMission::class
    )
    val missions: List<SavedTaskBlueprintMission>
)

@Dao
interface SavedTaskBlueprintDao {

    @Transaction
    @Query("SELECT * FROM saved_task_blueprints ORDER BY createdAt DESC")
    suspend fun getAllWithMissions(): List<SavedTaskBlueprintWithMissions>

    @Query("SELECT id FROM saved_task_blueprints")
    suspend fun getAllIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlueprint(blueprint: SavedTaskBlueprint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<SavedTaskBlueprintMission>)

    @Query("DELETE FROM saved_task_blueprint_missions WHERE blueprintId = :blueprintId")
    suspend fun deleteMissionsForBlueprint(blueprintId: String)

    @Query("DELETE FROM saved_task_blueprints WHERE id = :blueprintId")
    suspend fun deleteBlueprintById(blueprintId: String)

    @Query("DELETE FROM saved_task_blueprints WHERE id IN (:blueprintIds)")
    suspend fun deleteBlueprintsByIds(blueprintIds: List<String>)

    @Query("SELECT id FROM saved_task_blueprints ORDER BY createdAt DESC")
    suspend fun getBlueprintIdsNewestFirst(): List<String>

    @Transaction
    suspend fun upsertBlueprintWithMissions(
        blueprint: SavedTaskBlueprint,
        missions: List<SavedTaskBlueprintMission>
    ) {
        insertBlueprint(blueprint)
        deleteMissionsForBlueprint(blueprint.id)
        if (missions.isNotEmpty()) {
            insertMissions(missions)
        }
    }
}
