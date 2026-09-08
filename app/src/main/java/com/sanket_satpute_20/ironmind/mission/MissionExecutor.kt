package com.sanket_satpute_20.ironmind.mission

/**
 * Defines the architectural boundary for mission execution.
 * 
 * Exposes only the operations required by the recovery layer (or other orchestrators)
 * without leaking the complex Android runtime implementation details (Room, WorkLock, Pomodoro, etc.).
 */
interface MissionExecutor {
    
    suspend fun retryMission(
        taskId: Int,
        retryReason: String = "RECOVERY_RETRY"
    ): MissionExecutionResult
}
