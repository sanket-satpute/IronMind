package com.sanket_satpute_20.ironmind.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceLogDao {

    @Insert
    suspend fun insert(log: VoiceLog)

    @Query("SELECT * FROM voice_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<VoiceLog>>

    @Query("SELECT * FROM voice_logs WHERE month = :month ORDER BY timestamp DESC")
    suspend fun getLogsForMonth(month: String): List<VoiceLog>

    @Query("SELECT COUNT(*) FROM voice_logs WHERE month = :month")
    suspend fun getCountForMonth(month: String): Int

    @Query("SELECT * FROM voice_logs WHERE taskName = :taskName ORDER BY timestamp DESC")
    suspend fun getLogsForTask(taskName: String): List<VoiceLog>

    @Delete
    suspend fun delete(log: VoiceLog)

    @Query("SELECT COUNT(*) FROM voice_logs")
    suspend fun getTotalCount(): Int
}