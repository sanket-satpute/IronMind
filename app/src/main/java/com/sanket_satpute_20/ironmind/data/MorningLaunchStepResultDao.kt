package com.sanket_satpute_20.ironmind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MorningLaunchStepResultDao {

    @Insert
    suspend fun insert(result: MorningLaunchStepResult): Long

    @Update
    suspend fun update(result: MorningLaunchStepResult)

    @Query(
        "SELECT * FROM morning_launch_step_results WHERE sessionId = :sessionId ORDER BY stepOrder ASC, id ASC"
    )
    suspend fun getForSession(sessionId: Long): List<MorningLaunchStepResult>

    @Query(
        "SELECT * FROM morning_launch_step_results WHERE sessionId = :sessionId AND stepId = :stepId LIMIT 1"
    )
    suspend fun getForSessionAndStep(sessionId: Long, stepId: String): MorningLaunchStepResult?

    @Query("DELETE FROM morning_launch_step_results WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: Long)
}
