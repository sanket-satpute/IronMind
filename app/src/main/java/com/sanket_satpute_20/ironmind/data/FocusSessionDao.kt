package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insert(session: FocusSession): Long

    @Update
    suspend fun update(session: FocusSession)

    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId AND endTimestamp IS NULL ORDER BY startTimestamp DESC LIMIT 1")
    suspend fun getActiveSessionForTask(taskId: Int): FocusSession?

    @Query("SELECT * FROM focus_sessions ORDER BY startTimestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions ORDER BY startTimestamp DESC")
    suspend fun getAllSessionsSnapshot(): List<FocusSession>

    /** Completed sessions are the only sessions that repair the live Integrity Shield. */
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE date = :date AND completed = 1")
    fun observeCompletedCountForDate(date: String): Flow<Int>

    /** Debug only — deletes all sessions for a given date. */
    @Query("DELETE FROM focus_sessions WHERE date = :date")
    suspend fun deleteForDate(date: String)
}
