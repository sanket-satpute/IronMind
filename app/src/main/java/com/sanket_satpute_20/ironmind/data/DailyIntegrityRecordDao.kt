package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyIntegrityRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DailyIntegrityRecord)

    @Query("SELECT * FROM daily_integrity_records WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): DailyIntegrityRecord?

    @Query("SELECT * FROM daily_integrity_records ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DailyIntegrityRecord>
}
