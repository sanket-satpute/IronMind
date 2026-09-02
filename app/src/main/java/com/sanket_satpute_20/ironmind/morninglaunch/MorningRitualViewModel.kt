package com.sanket_satpute_20.ironmind.morninglaunch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.ironmind.data.DailyIntegrityRecord
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.integrity.DailyIntegrityEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MorningRitualViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val integrityEngine = DailyIntegrityEngine(application)
    private val prefs = PrefManager.getInstance(application)
    private val database = com.sanket_satpute_20.ironmind.data.IronMindDatabase.getDatabase(application)

    val todayTasks = database.taskDao().getTasksForDate(java.time.LocalDate.now().toString())

    private val _uiState = MutableStateFlow(
        MorningRitualState(
            currentStep = try {
                RitualStep.valueOf(prefs.morningRitualCurrentStep)
            } catch (e: Exception) {
                RitualStep.WAKE_ENTRY
            }
        )
    )
    val uiState: StateFlow<MorningRitualState> = _uiState.asStateFlow()

    fun advanceTo(step: RitualStep) {
        updateState { it.copy(currentStep = step) }
    }

    fun selectTask(taskId: Long) {
        updateState { state ->
            val current = state.selectedTaskIds.toMutableList()
            if (current.contains(taskId)) {
                current.remove(taskId)
            } else if (current.size < 3) {
                current.add(taskId)
            }
            state.copy(selectedTaskIds = current)
        }
    }

    fun completeOath() {
        viewModelScope.launch {
            // 1. Force finalize yesterday if needed
            getYesterdayRecord()
            
            // 2. Gather history
            val recentRecords = integrityEngine.getRecentDays(7)
            val energyScore = prefs.lastEnergyScore

            // 3. Gather sleep data (if available)
            val healthManager = com.sanket_satpute_20.ironmind.health.HealthConnectManager(getApplication())
            var sleepHours: Float? = null
            if (healthManager.isHealthConnectAvailable() && healthManager.hasAllPermissions()) {
                val sleepData = healthManager.getSleepDataForLastWeek()
                val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()
                sleepHours = sleepData[yesterdayStr]
            }

            val recommendation = com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine.recommendMorningMode(
                energyScore = energyScore,
                recentRecords = recentRecords,
                sleepHours = sleepHours,
                prefs = prefs
            )

            updateState { 

                it.copy(
                    isOathCompleted = true, 
                    currentStep = RitualStep.MODE_RECOMMENDATION,
                    recommendedMode = recommendation.mode.name,
                    recommendationReason = recommendation.reason
                ) 
            }
        }
    }

    fun confirmMode(mode: String) {
        val currentState = _uiState.value
        if (currentState.recommendedMode != null && mode != currentState.recommendedMode) {
            // User rejected the recommendation
            prefs.lastRejectedRecommendationMode = currentState.recommendedMode
            prefs.lastRejectedRecommendationDate = java.time.LocalDate.now().toString()
        }
        
        updateState { it.copy(confirmedMode = mode, currentStep = RitualStep.SHIELD_REASSEMBLY) }
    }
    
    fun finishRitual() {
        updateState { it.copy(currentStep = RitualStep.MAIN_FLOW_HANDOFF) }
    }
    
    fun resetRitual() {
        updateState { it.copy(currentStep = RitualStep.WAKE_ENTRY, isOathCompleted = false) }
    }

    private fun updateState(update: (MorningRitualState) -> MorningRitualState) {
        _uiState.update { currentState ->
            val newState = update(currentState)
            // Persist the current step to PrefManager so it survives a force-kill swipe
            prefs.morningRitualCurrentStep = newState.currentStep.name
            newState
        }
    }
    
    suspend fun getYesterdayRecord(): DailyIntegrityRecord? {
        return integrityEngine.finalizeYesterdayIfNeeded("MORNING_LAUNCH_RECAP")
    }
}
