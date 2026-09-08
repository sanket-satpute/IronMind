package com.sanket_satpute_20.ironmind.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("""
        SELECT * FROM tasks
        WHERE date = :date
        ORDER BY
            CASE
                WHEN isInProgress = 1 THEN 0
                WHEN isCompleted = 0 AND isSkipped = 0 THEN 1
                WHEN isCompleted = 1 THEN 2
                ELSE 3
            END,
            startTime ASC,
            importanceRank ASC,
            id ASC
    """)
    fun getTasksForDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startDate AND :endDate")
    fun getTasksInDateRange(startDate: String, endDate: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("""
        SELECT * FROM tasks
        WHERE isInProgress = 1
        AND isCompleted = 0
        AND isSkipped = 0
        ORDER BY startedAt ASC, id ASC
    """)
    suspend fun getInProgressTasks(): List<Task>

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("DELETE FROM tasks WHERE date = :date")
    suspend fun deleteTasksForDate(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    @Query("SELECT * FROM tasks WHERE date = :date")
    suspend fun getTasksForDateOnce(date: String): List<Task>

    @Query("""
        SELECT COUNT(*) FROM tasks
        WHERE date = :date
        AND isBreakSegment = 0
        AND isCompleted = 0
        AND isSkipped = 0
    """)
    suspend fun getRemainingTaskCountForDate(date: String): Int

    @Query("SELECT * FROM tasks ORDER BY date DESC, startTime ASC")
    fun getAllTasks(): Flow<List<Task>>

    // --- Momentum Guard Queries ---
    @Query("SELECT * FROM tasks WHERE date = :date AND isDeferred = 1 AND isCompleted = 0")
    suspend fun getPendingDeferredTasks(date: String): List<Task>

    @Query("SELECT * FROM tasks WHERE isSkipped = 1 ORDER BY date DESC, startTime DESC")
    fun getAllSkippedTasks(): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isSkipped = 1")
    suspend fun getTotalSkippedAllTime(): Int

    // Get all rescheduled tasks for pattern analysis
    @Query("SELECT * FROM tasks WHERE isRescheduled = 1 ORDER BY date DESC")
    suspend fun getAllRescheduledTasks(): List<Task>

    // Get reschedule count for a specific task name in last N weeks
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE name = :taskName 
        AND isRescheduled = 1 
        AND date >= :sinceDate
    """)
    suspend fun getRescheduleCount(taskName: String, sinceDate: String): Int

    // Get all tasks with focus scores for graphing
    @Query("""
        SELECT * FROM tasks 
        WHERE focusScore > 0 
        AND date >= :sinceDate 
        ORDER BY date ASC
    """)
    suspend fun getTasksWithFocusScores(sinceDate: String): List<Task>

    // Get average focus score for a specific task name
    @Query("""
        SELECT AVG(focusScore) FROM tasks 
        WHERE name = :taskName 
        AND focusScore > 0
    """)
    suspend fun getAverageFocusScore(taskName: String): Float

    // Weekly reschedule count across all tasks
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE isRescheduled = 1 
        AND date >= :weekStart
    """)
    suspend fun getWeeklyRescheduleCount(weekStart: String): Int

    // Update focus score after task completion
    @Query("UPDATE tasks SET focusScore = :score WHERE id = :taskId")
    suspend fun updateFocusScore(taskId: Int, score: Int)

    // --- Identification ---
    @Query("SELECT DISTINCT packageName FROM tasks WHERE packageName IS NOT NULL")
    suspend fun getAllUsedPackageNames(): List<String>

    /** Debug only — deletes tasks whose name starts with "DEBUG" for a given date. */
    @Query("DELETE FROM tasks WHERE date = :date AND name LIKE 'DEBUG%'")
    suspend fun deleteDebugTasksForDate(date: String)
}
