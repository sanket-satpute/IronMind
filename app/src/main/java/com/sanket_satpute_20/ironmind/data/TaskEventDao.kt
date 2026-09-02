package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskEventDao {

    @Insert
    suspend fun insert(event: TaskEvent)

    @Query("SELECT * FROM task_events WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getEventsForTask(taskId: Int): Flow<List<TaskEvent>>

    @Query("SELECT * FROM task_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<TaskEvent>>

    @Query("SELECT * FROM task_events ORDER BY timestamp DESC")
    suspend fun getAllEventsSnapshot(): List<TaskEvent>
}
