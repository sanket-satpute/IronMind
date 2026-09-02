package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCheckInDao {

    @Insert
    suspend fun insert(checkIn: DailyCheckIn)

    @Query("SELECT * FROM daily_checkins ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DailyCheckIn>>

    @Query("SELECT * FROM daily_checkins ORDER BY timestamp DESC")
    suspend fun getAllSnapshot(): List<DailyCheckIn>
}
