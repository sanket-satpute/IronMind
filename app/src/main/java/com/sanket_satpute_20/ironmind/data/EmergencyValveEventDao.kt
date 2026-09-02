package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyValveEventDao {

    @Insert
    suspend fun insert(event: EmergencyValveEvent)

    @Query("SELECT * FROM emergency_valve_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EmergencyValveEvent>>

    @Query("SELECT * FROM emergency_valve_events ORDER BY timestamp DESC")
    suspend fun getAllEventsSnapshot(): List<EmergencyValveEvent>

    @Query("SELECT * FROM emergency_valve_events WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getEventsForDate(date: String): List<EmergencyValveEvent>

    @Query("SELECT COUNT(*) FROM emergency_valve_events")
    suspend fun getTotalCount(): Int
}
