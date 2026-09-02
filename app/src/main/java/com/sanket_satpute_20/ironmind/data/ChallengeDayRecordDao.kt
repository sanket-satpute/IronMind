package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDayRecordDao {

    @Insert
    suspend fun insert(record: ChallengeDayRecord)

    @Query("SELECT * FROM challenge_day_records WHERE runId = :runId ORDER BY dayNumber ASC")
    fun getRecordsForRun(runId: String): Flow<List<ChallengeDayRecord>>

    @Query("SELECT * FROM challenge_day_records ORDER BY completedAt DESC")
    fun getAll(): Flow<List<ChallengeDayRecord>>

    @Query("SELECT * FROM challenge_day_records ORDER BY completedAt DESC")
    suspend fun getAllSnapshot(): List<ChallengeDayRecord>
}
