package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfidenceIgnitionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ConfidenceIgnitionEntry)

    @Query("SELECT * FROM confidence_ignition_entries WHERE date = :date LIMIT 1")
    fun observeEntry(date: String): Flow<ConfidenceIgnitionEntry?>

    @Query("SELECT * FROM confidence_ignition_entries WHERE date = :date LIMIT 1")
    suspend fun getEntry(date: String): ConfidenceIgnitionEntry?

    @Query("SELECT * FROM confidence_ignition_entries ORDER BY date DESC")
    fun getAll(): Flow<List<ConfidenceIgnitionEntry>>

    @Query("SELECT * FROM confidence_ignition_entries ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ConfidenceIgnitionEntry>
}
