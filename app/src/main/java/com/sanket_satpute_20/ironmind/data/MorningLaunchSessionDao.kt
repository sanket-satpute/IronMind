package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MorningLaunchSessionDao {

    @Insert
    suspend fun insert(session: MorningLaunchSession): Long

    @Update
    suspend fun update(session: MorningLaunchSession)

    @Query("SELECT * FROM morning_launch_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MorningLaunchSession?

    @Query("SELECT * FROM morning_launch_sessions ORDER BY alarmDismissedAt DESC")
    fun getAll(): Flow<List<MorningLaunchSession>>

    @Query("SELECT * FROM morning_launch_sessions ORDER BY alarmDismissedAt DESC LIMIT 1")
    suspend fun getLatest(): MorningLaunchSession?
}
