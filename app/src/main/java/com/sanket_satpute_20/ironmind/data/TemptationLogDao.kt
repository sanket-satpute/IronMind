package com.sanket_satpute_20.ironmind.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TemptationLogDao {

    @Insert
    suspend fun insertLog(log: TemptationLog)

    // Get all logs for a specific date
    @Query("SELECT * FROM temptation_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): Flow<List<TemptationLog>>

    // Get count of total temptations resisted today
    @Query("SELECT COUNT(*) FROM temptation_logs WHERE date = :date")
    suspend fun getCountForDate(date: String): Int

    /** Emits immediately, then again for every new blocked-app attempt on this date. */
    @Query("SELECT COUNT(*) FROM temptation_logs WHERE date = :date")
    fun observeCountForDate(date: String): Flow<Int>

    // Get grouped summary — how many times each app was blocked today
    @Query("""
        SELECT blockedAppName, blockedPackage, duringTaskName, COUNT(*) as count 
        FROM temptation_logs 
        WHERE date = :date 
        GROUP BY blockedPackage, duringTaskName 
        ORDER BY count DESC
    """)
    suspend fun getSummaryForDate(date: String): List<TemptationSummary>

    // All time total
    @Query("SELECT COUNT(*) FROM temptation_logs")
    suspend fun getTotalAllTime(): Int

    /** Debug only — deletes all logs for a given date. */
    @Query("DELETE FROM temptation_logs WHERE date = :date")
    suspend fun deleteForDate(date: String)
}

// This is a helper data class for the grouped query above
// Not a Room entity — just a result holder
data class TemptationSummary(
    val blockedAppName: String,
    val blockedPackage: String,
    val duringTaskName: String,
    val count: Int
)
