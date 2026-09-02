package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrucibleDayRecordDao {

    @Insert
    suspend fun insert(record: CrucibleDayRecord)

    @Query("SELECT * FROM crucible_day_records WHERE runId = :runId ORDER BY dayNumber ASC")
    fun getRecordsForRun(runId: String): Flow<List<CrucibleDayRecord>>

    @Query("SELECT * FROM crucible_day_records ORDER BY completedAt DESC")
    fun getAll(): Flow<List<CrucibleDayRecord>>

    @Query("SELECT * FROM crucible_day_records ORDER BY completedAt DESC")
    suspend fun getAllSnapshot(): List<CrucibleDayRecord>
}
