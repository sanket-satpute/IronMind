package com.sanket_satpute_20.ironmind.health

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyCorrelation(
    val date: String,
    val sleepHours: Float,
    val avgFocusScore: Float
)

sealed class HealthUiState {
    object Idle : HealthUiState()
    object Loading : HealthUiState()
    object NoPermission : HealthUiState()
    data class Success(
        val correlations: List<DailyCorrelation>,
        val insight: String,
        val source: SleepSource
    ) : HealthUiState()
    data class NoData(val message: String) : HealthUiState()
    data class Error(val message: String) : HealthUiState()
}

class HealthAnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val sleepRepo = SleepRepository(application)
    private val healthManager = HealthConnectManager(application)
    private val taskDao = IronMindDatabase.getDatabase(application).taskDao()

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Idle)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    fun loadData() = viewModelScope.launch {
        _uiState.value = HealthUiState.Loading
        
        if (!healthManager.isHealthConnectAvailable()) {
            _uiState.value = HealthUiState.Error("Health Connect is not installed.")
            return@launch
        }

        if (!healthManager.hasAllPermissions()) {
            _uiState.value = HealthUiState.NoPermission
            return@launch
        }

        try {
            val result = sleepRepo.getSleepData()
            val daysToAnalyze = 7
            val today = LocalDate.now()
            
            if (result.source == SleepSource.NONE) {
                _uiState.value = HealthUiState.NoData("No sleep data found from Fitbit, Google Fit, or Bedtime patterns.")
                return@launch
            }

            val correlations = mutableListOf<DailyCorrelation>()
            val startDate = today.minusDays(daysToAnalyze.toLong())
            val tasks = taskDao.getTasksWithFocusScores(startDate.toString())

            for (i in 0 until daysToAnalyze) {
                val dateStr = today.minusDays(i.toLong()).toString()
                val sleepHours = result.data[dateStr] ?: 0f
                
                val tasksForDay = tasks.filter { it.date == dateStr }
                val avgFocus = if (tasksForDay.isNotEmpty()) {
                    tasksForDay.map { it.focusScore }.average().toFloat()
                } else 0f

                correlations.add(DailyCorrelation(dateStr, sleepHours, avgFocus))
            }

            val sortedCorrelations = correlations.sortedBy { it.date }
            val insight = generateInsight(sortedCorrelations)
            
            _uiState.value = HealthUiState.Success(sortedCorrelations, insight, result.source)
            
        } catch (e: Exception) {
            Log.e("HealthAnalyticsVM", "Error loading data", e)
            _uiState.value = HealthUiState.Error("System Error: ${e.message}")
        }
    }

    private fun generateInsight(data: List<DailyCorrelation>): String {
        val validData = data.filter { it.sleepHours > 0 || it.avgFocusScore > 0 }
        if (validData.size < 3) return "Analyze more days to reveal performance multipliers."
        
        val highSleepDays = validData.filter { it.sleepHours >= 7 }
        val lowSleepDays = validData.filter { it.sleepHours > 0 && it.sleepHours < 6 }
        
        if (highSleepDays.isEmpty() || lowSleepDays.isEmpty()) {
            return "Stable patterns emerging. Keep maintaining your tracking standards."
        }

        val highAvg = highSleepDays.map { it.avgFocusScore }.average()
        val lowAvg = lowSleepDays.map { it.avgFocusScore }.average()

        return if (highAvg > lowAvg) {
            val diff = ((highAvg - lowAvg) / (if (lowAvg > 0) lowAvg else 1.0) * 100).toInt()
            "Focus is $diff% higher with 7+ hours of sleep. Physical recovery is force-multiplying your discipline."
        } else {
            "Focus remains consistent across sleep variations. Your mental discipline is currently overriding physical fatigue."
        }
    }
}
