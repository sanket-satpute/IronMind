package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserStateSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: UserStateSnapshot)

    @Query("SELECT * FROM user_state_snapshot WHERE key = 'primary' LIMIT 1")
    suspend fun getPrimary(): UserStateSnapshot?
}
