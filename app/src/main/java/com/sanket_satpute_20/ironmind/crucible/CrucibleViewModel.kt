package com.sanket_satpute_20.ironmind.crucible

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrucibleViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefManager.getInstance(application)
    private val crucibleManager = CrucibleManager(prefs)

    private val _timeRemainingSeconds = MutableStateFlow(0L)
    val timeRemainingSeconds: StateFlow<Long> = _timeRemainingSeconds.asStateFlow()

    private val _sessionFailedTitle = MutableStateFlow<String?>(null)
    val sessionFailedTitle: StateFlow<String?> = _sessionFailedTitle.asStateFlow()

    private val _sessionCompletion = MutableStateFlow<CrucibleSessionCompletion?>(null)
    val sessionCompletion: StateFlow<CrucibleSessionCompletion?> = _sessionCompletion.asStateFlow()

    private var timerJob: Job? = null

    // Called when user starts a crucible session
    fun startSession(
        crucibleId: String,
        durationMinutes: Int,
        taskName: String,
        penalty: CruciblePenalty
    ) {
        val existingRunId = crucibleManager.getCurrentRunId()
        crucibleManager.beginCrucibleRunIfNeeded(crucibleId)
        val runId = crucibleManager.getCurrentRunId()
        val activeCrucible = crucibleManager.getActiveCrucible()
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        prefs.crucibleActive = true
        prefs.crucibleEndTime = endTime
        prefs.crucibleTaskName = taskName
        prefs.cruciblePenalty = penalty.name
        prefs.crucibleResult = CrucibleResult.NONE.name
        _sessionFailedTitle.value = null
        _sessionCompletion.value = null

        // Tell accessibility service crucible is active
        getApplication<Application>().sendBroadcast(
            Intent("com.ironmind.RELOAD_GUARD").apply {
                setPackage(getApplication<Application>().packageName)
            }
        )

        if (activeCrucible != null && runId.isNotBlank() && runId != existingRunId) {
            HistoryRecorder.recordCrucibleEvent(
                context = getApplication(),
                runId = runId,
                crucibleId = activeCrucible.id,
                crucibleTitle = activeCrucible.title,
                eventType = "STARTED",
                totalDays = activeCrucible.durationDays,
                penaltyType = penalty.name,
                sessionDurationMinutes = durationMinutes,
                details = "Crucible run accepted"
            )
        }

        startTimer(endTime)
    }

    // Called on ViewModel init if a session is already running
    // (handles app being killed and reopened mid-session)
    fun resumeSessionIfActive() {
        if (!prefs.crucibleActive) return
        val endTime = prefs.crucibleEndTime
        if (endTime <= System.currentTimeMillis()) {
            // Time already passed — complete it
            completeSession()
        } else {
            startTimer(endTime)
        }
    }

    private fun startTimer(endTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = endTime - System.currentTimeMillis()
                if (remaining <= 0) {
                    _timeRemainingSeconds.value = 0
                    completeSession()
                    break
                }
                _timeRemainingSeconds.value = remaining / 1000
                delay(1000)
            }
        }
    }

    // Called by accessibility service breach OR user manually giving up
    fun failSession() {
        if (!prefs.crucibleActive) return
        timerJob?.cancel()
        prefs.crucibleActive = false
        prefs.crucibleResult = CrucibleResult.FAILED.name
        prefs.crucibleTotalFailed++
        val runId = crucibleManager.getCurrentRunId()
        val activeCrucible = crucibleManager.getActiveCrucible()
        val completedDays = prefs.crucibleCompletedDates.size
        val failedCrucible = crucibleManager.failActiveCrucibleRun()

        // Apply penalty
        CruciblePenaltyEngine.applyPenalty(getApplication(), prefs, CruciblePenalty.valueOf(prefs.cruciblePenalty))

        getApplication<Application>().sendBroadcast(
            Intent("com.ironmind.RELOAD_GUARD").apply {
                setPackage(getApplication<Application>().packageName)
            }
        )

        if (activeCrucible != null && runId.isNotBlank()) {
            HistoryRecorder.recordCrucibleEvent(
                context = getApplication(),
                runId = runId,
                crucibleId = activeCrucible.id,
                crucibleTitle = activeCrucible.title,
                eventType = "FAILED",
                dayNumber = completedDays + 1,
                completedDaysSnapshot = completedDays,
                totalDays = activeCrucible.durationDays,
                penaltyType = prefs.cruciblePenalty,
                sessionDurationMinutes = activeCrucible.sessionDurationMinutes,
                details = "Session failed or surrendered"
            )
        }

        _sessionFailedTitle.value = failedCrucible?.title ?: prefs.crucibleTaskName.ifEmpty { "Unknown Crucible" }
    }

    private fun completeSession() {
        val activeCrucible = crucibleManager.getActiveCrucible()
        prefs.crucibleActive = false
        prefs.crucibleResult = CrucibleResult.COMPLETED.name
        prefs.crucibleTotalCompleted++
        timerJob?.cancel()
        _sessionCompletion.value = crucibleManager.recordSessionSuccess()

        getApplication<Application>().sendBroadcast(
            Intent("com.ironmind.RELOAD_GUARD").apply {
                setPackage(getApplication<Application>().packageName)
            }
        )

        val completion = _sessionCompletion.value
        if (activeCrucible != null && completion != null) {
            HistoryRecorder.recordCrucibleDay(
                context = getApplication(),
                runId = completion.runId,
                crucibleId = activeCrucible.id,
                crucibleTitle = activeCrucible.title,
                dayNumber = completion.completedDays,
                sessionDurationMinutes = activeCrucible.sessionDurationMinutes,
                penaltyType = activeCrucible.defaultPenalty.name
            )
            HistoryRecorder.recordCrucibleEvent(
                context = getApplication(),
                runId = completion.runId,
                crucibleId = activeCrucible.id,
                crucibleTitle = activeCrucible.title,
                eventType = "COMPLETED_DAY",
                dayNumber = completion.completedDays,
                completedDaysSnapshot = completion.completedDays,
                totalDays = completion.totalDays,
                penaltyType = activeCrucible.defaultPenalty.name,
                sessionDurationMinutes = activeCrucible.sessionDurationMinutes,
                details = "Day ${completion.completedDays} secured"
            )
            if (completion.campaignCompleted) {
                HistoryRecorder.recordCrucibleEvent(
                    context = getApplication(),
                    runId = completion.runId,
                    crucibleId = activeCrucible.id,
                    crucibleTitle = activeCrucible.title,
                    eventType = "COMPLETED_CRUCIBLE",
                    dayNumber = completion.completedDays,
                    completedDaysSnapshot = completion.completedDays,
                    totalDays = completion.totalDays,
                    penaltyType = activeCrucible.defaultPenalty.name,
                    sessionDurationMinutes = activeCrucible.sessionDurationMinutes,
                    details = completion.rewardTitle
                )
            }
        }
    }

    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
