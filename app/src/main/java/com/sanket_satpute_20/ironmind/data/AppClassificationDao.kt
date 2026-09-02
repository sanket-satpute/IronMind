package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppClassificationDao {

    @Query("SELECT * FROM app_classifications ORDER BY appName ASC")
    fun getAll(): Flow<List<AppClassification>>

    @Query("SELECT * FROM app_classifications ORDER BY appName ASC")
    suspend fun getAllOnce(): List<AppClassification>

    @Query("SELECT * FROM app_classifications WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): AppClassification?

    @Query("SELECT * FROM app_classifications WHERE category = :category ORDER BY appName ASC")
    fun getByCategory(category: String): Flow<List<AppClassification>>

    @Query("""
        SELECT * FROM app_classifications
        WHERE source = :pendingSource
        ORDER BY timesOpenedDuringFocus DESC, timesOpenedOverall DESC, appName ASC
    """)
    fun getReviewQueue(pendingSource: String = "UNKNOWN_PENDING"): Flow<List<AppClassification>>

    @Query("""
        SELECT * FROM app_classifications
        WHERE category = 'CONTEXT'
        ORDER BY appName ASC
    """)
    suspend fun getContextAppsOnce(): List<AppClassification>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(classification: AppClassification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(classifications: List<AppClassification>)

    @Query("""
        UPDATE app_classifications
        SET lastSeenAt = :timestamp,
            timesOpenedOverall = timesOpenedOverall + 1,
            updatedAt = :timestamp
        WHERE packageName = :packageName
    """)
    suspend fun recordSeen(packageName: String, timestamp: Long)

    @Query("""
        UPDATE app_classifications
        SET lastSeenAt = :timestamp,
            timesOpenedDuringFocus = timesOpenedDuringFocus + 1,
            timesOpenedOverall = timesOpenedOverall + 1,
            updatedAt = :timestamp
        WHERE packageName = :packageName
    """)
    suspend fun recordFocusOpen(packageName: String, timestamp: Long)

    @Query("""
        UPDATE app_classifications
        SET lastPromptedAt = :timestamp,
            timesPrompted = timesPrompted + 1,
            updatedAt = :timestamp
        WHERE packageName = :packageName
    """)
    suspend fun markPrompted(packageName: String, timestamp: Long)

    @Query("DELETE FROM app_classifications")
    suspend fun clearAll()
}
