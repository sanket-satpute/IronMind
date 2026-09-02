package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeEventDao {

    @Insert
    suspend fun insert(event: ChallengeEvent)

    @Query("SELECT * FROM challenge_events WHERE runId = :runId ORDER BY timestamp ASC")
    fun getEventsForRun(runId: String): Flow<List<ChallengeEvent>>

    @Query("SELECT * FROM challenge_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ChallengeEvent>>

    @Query("SELECT * FROM challenge_events ORDER BY timestamp DESC")
    suspend fun getAllSnapshot(): List<ChallengeEvent>

    @Query("SELECT * FROM challenge_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEvent(): ChallengeEvent?

    @Query("SELECT * FROM challenge_events WHERE eventType IN (:eventTypes) ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMatchingEvent(eventTypes: List<String>): ChallengeEvent?
}
