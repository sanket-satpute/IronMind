package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubChatEventDao {

    @Insert
    suspend fun insert(event: ClubChatEvent)

    @Query("SELECT * FROM club_chat_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClubChatEvent>>

    @Query("SELECT * FROM club_chat_events ORDER BY timestamp DESC")
    suspend fun getAllSnapshot(): List<ClubChatEvent>
}
