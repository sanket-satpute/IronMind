package com.sanket_satpute_20.ironmind.nightdecision

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class NightRitualViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val prefs = PrefManager.getInstance(application)
    private val _uiState = MutableStateFlow<NightRitualState>(NightRitualState.Loading(""))
    val uiState: StateFlow<NightRitualState> = _uiState.asStateFlow()

    init {
        restoreOrInitializeState()
    }

    private fun restoreOrInitializeState() {
        val today = LocalDate.now().toString()
        
        // If the date has changed, start fresh
        if (prefs.nightRitualDate != today) {
            prefs.nightRitualDate = today
            prefs.nightRitualCurrentStep = NightRitualStep.SHIELD_RECAP.name
            prefs.nightRitualReflectionText = ""
        }

        val step = try {
            NightRitualStep.valueOf(prefs.nightRitualCurrentStep)
        } catch (e: Exception) {
            NightRitualStep.SHIELD_RECAP
        }

        val initialState = NightRitualState.Active(
            date = today,
            currentStep = step,
            integrityScore = prefs.lastKnownIntegrityLevel,
            isRoughDay = prefs.lastKnownIntegrityLevel < 0.6f,
            strugglingDays = prefs.integrityStrugglingDaysInRow,
            reflectionText = prefs.nightRitualReflectionText,
            sleepLockEnabled = prefs.sleepLockEnabled
        )
        _uiState.value = initialState
    }

    private fun updateState(newState: NightRitualState) {
        _uiState.value = newState
        if (newState is NightRitualState.Active) {
            prefs.nightRitualCurrentStep = newState.currentStep.name
            prefs.nightRitualReflectionText = newState.reflectionText
        }
    }

    fun resetRitual() {
        prefs.nightRitualCurrentStep = NightRitualStep.SHIELD_RECAP.name
        prefs.nightRitualReflectionText = ""
        // Keep the date so we know they completed it today, but reset the flow
    }

    fun completeShieldRecap() {
        val currentState = _uiState.value as? NightRitualState.Active ?: return
        updateState(currentState.copy(currentStep = NightRitualStep.REFLECTION))
    }

    fun saveReflection(text: String) {
        val currentState = _uiState.value as? NightRitualState.Active ?: return
        prefs.lastReflectionDate = currentState.date
        prefs.lastReflectionText = text
        updateState(currentState.copy(reflectionText = text))
        moveToNextAfterReflection(currentState)
    }

    fun skipReflection() {
        val currentState = _uiState.value as? NightRitualState.Active ?: return
        moveToNextAfterReflection(currentState)
    }

    private fun moveToNextAfterReflection(currentState: NightRitualState.Active) {
        if (currentState.strugglingDays >= 3) {
            updateState(currentState.copy(currentStep = NightRitualStep.RECOVERY_RECOMMENDATION))
        } else {
            updateState(currentState.copy(currentStep = NightRitualStep.PLANNING))
        }
    }

    fun acceptRecoveryRecommendation() {
        val currentState = _uiState.value as? NightRitualState.Active ?: return
        if (prefs.recoveryDaysBudget > 0) {
            prefs.recoveryDaysBudget -= 1
            prefs.isRecoveryDayActive = true
        }
        updateState(currentState.copy(currentStep = NightRitualStep.PLANNING))
    }

    fun declineRecoveryRecommendation() {
        val currentState = _uiState.value as? NightRitualState.Active ?: return
        updateState(currentState.copy(currentStep = NightRitualStep.PLANNING))
    }

    fun completePlanning() {
        val currentState = _uiState.value as? NightRitualState.Active ?: return
        updateState(currentState.copy(currentStep = NightRitualStep.HANDOFF))
    }

    fun completeRitual() {
        val currentState = _uiState.value as? NightRitualState.Active ?: return
        updateState(NightRitualState.Finished(currentState.date))
    }
}
