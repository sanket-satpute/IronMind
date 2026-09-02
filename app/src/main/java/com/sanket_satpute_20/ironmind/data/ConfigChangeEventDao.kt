package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigChangeEventDao {

    @Insert
    suspend fun insert(event: ConfigChangeEvent)

    @Query("SELECT * FROM config_change_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ConfigChangeEvent>>

    @Query("SELECT * FROM config_change_events ORDER BY timestamp DESC")
    suspend fun getAllSnapshot(): List<ConfigChangeEvent>
}
