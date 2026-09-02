package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrucibleEventDao {

    @Insert
    suspend fun insert(event: CrucibleEvent)

    @Query("SELECT * FROM crucible_events WHERE runId = :runId ORDER BY timestamp ASC")
    fun getEventsForRun(runId: String): Flow<List<CrucibleEvent>>

    @Query("SELECT * FROM crucible_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CrucibleEvent>>

    @Query("SELECT * FROM crucible_events ORDER BY timestamp DESC")
    suspend fun getAllSnapshot(): List<CrucibleEvent>
}
