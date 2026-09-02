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

class HealthDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val healthManager = HealthConnectManager(application)
    private val taskDao = IronMindDatabase.getDatabase(application).taskDao()

    data class DayCorrelation(
        val date: String,
        val sleepHours: Float,
        val focusScore: Float,       // average focus score that day
        val completionRate: Float    // percentage of tasks completed
    )

    private val _correlations = MutableStateFlow<List<DayCorrelation>>(emptyList())
    val correlations: StateFlow<List<DayCorrelation>> = _correlations.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkHealthStatus()
    }

    fun checkHealthStatus() {
        viewModelScope.launch {
            _isAvailable.value = healthManager.isHealthConnectAvailable()
            if (_isAvailable.value) {
                _hasPermission.value = healthManager.hasAllPermissions()
                if (_hasPermission.value) loadCorrelations()
            }
        }
    }

    fun onPermissionResult() {
        viewModelScope.launch {
            _hasPermission.value = healthManager.hasAllPermissions()
            if (_hasPermission.value) {
                loadCorrelations()
            }
        }
    }

    private suspend fun loadCorrelations() {
        _isLoading.value = true
        try {
            val sleepData = healthManager.getSleepDataForLastWeek()
            Log.d("HealthDashboardVM", "Loaded sleep data: ${sleepData.size} days")
            
            val result = mutableListOf<DayCorrelation>()
            val today = LocalDate.now()

            // Iterate through last 7 days to ensure we don't skip days with sleep but no tasks
            for (i in 0..6) {
                val dateStr = today.minusDays(i.toLong()).toString()
                val sleepHours = sleepData[dateStr] ?: 0f
                
                val tasks = taskDao.getTasksForDateOnce(dateStr)
                
                val focusTasks = tasks.filter { it.focusScore > 0 }
                val avgFocus = if (focusTasks.isNotEmpty()) {
                    focusTasks.map { it.focusScore.toFloat() }.average().toFloat()
                } else {
                    0f
                }

                val completionRate = if (tasks.isNotEmpty()) {
                    tasks.count { it.isCompleted }.toFloat() / tasks.size
                } else {
                    0f
                }

                // Add if we have either sleep data OR task data for this day
                if (sleepHours > 0 || tasks.isNotEmpty()) {
                    result.add(DayCorrelation(dateStr, sleepHours, avgFocus, completionRate))
                }
            }

            _correlations.value = result.sortedBy { it.date }
            Log.d("HealthDashboardVM", "Final correlations: ${result.size}")
        } catch (e: Exception) {
            Log.e("HealthDashboardVM", "Error loading correlations", e)
        } finally {
            _isLoading.value = false
        }
    }
}
