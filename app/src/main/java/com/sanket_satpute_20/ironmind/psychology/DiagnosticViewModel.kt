package com.sanket_satpute_20.ironmind.psychology

import android.content.Context
import androidx.lifecycle.ViewModel
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class DiagnosticViewModel : ViewModel() {

    private val _currentQuestion = MutableStateFlow(0)
    val currentQuestion: StateFlow<Int> = _currentQuestion.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<List<DiagnosticAnswer>>(emptyList())
    val selectedAnswers: StateFlow<List<DiagnosticAnswer>> = _selectedAnswers.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    val questions = listOf(
        DiagnosticQuestion(
            id       = 0,
            question = "What is your deepest motivation\nfor changing?",
            subtext  = "Be honest. This shapes everything.",
            answers  = TypeDetector.q1Answers
        ),
        DiagnosticQuestion(
            id       = 1,
            question = "When you think about the future,\nwhat is your biggest fear?",
            subtext  = "Your real motivation — not what sounds good.",
            answers  = TypeDetector.q2Answers
        ),
        DiagnosticQuestion(
            id       = 2,
            question = "How do you want to be remembered?",
            subtext  = "What legacy do you want to leave behind?",
            answers  = TypeDetector.q3Answers
        ),
        DiagnosticQuestion(
            id       = 3,
            question = "If you could design the perfect day,\nwhat would it look like?",
            subtext  = "Not what you think you should say. What you actually want.",
            answers  = TypeDetector.q4Answers
        )
    )

    fun selectAnswer(answer: DiagnosticAnswer) {
        val current = _selectedAnswers.value.toMutableList()
        if (current.size > _currentQuestion.value) {
            current[_currentQuestion.value] = answer
        } else {
            current.add(answer)
        }
        _selectedAnswers.value = current
    }

    fun nextQuestion(): Boolean {
        return if (_currentQuestion.value < questions.size - 1) {
            _currentQuestion.value++
            true
        } else {
            false
        }
    }

    fun previousQuestion() {
        if (_currentQuestion.value > 0) {
            _currentQuestion.value--
        }
    }

    fun finishDiagnostic(context: Context) {
        val profile = TypeDetector.detectType(_selectedAnswers.value)
        val finalProfile = profile.copy(
            profileCreatedDate = LocalDate.now().toString()
        )
        _profile.value = finalProfile
        saveProfile(context, finalProfile)
    }

    private fun saveProfile(context: Context, profile: UserProfile) {
        val prefs = PrefManager.getInstance(context)
        val previousMode = prefs.appMode
        
        prefs.userType = profile.primaryType.name
        prefs.secondaryType = profile.secondaryType?.name ?: ""
        prefs.appMode = profile.assignedMode.name
        prefs.diagnosticDone = true
        prefs.profileDate = profile.profileCreatedDate
        
        HistoryRecorder.recordConfigChange(
            context = context,
            configType = "ASPIRATIONAL_PERSONA_UPDATE",
            oldValue = "Unknown",
            newValue = profile.primaryType.name,
            sourceScreen = "ASPIRATIONAL_DIAGNOSTIC"
        )
        
        AdaptiveEngine.switchMode(prefs, profile.assignedMode, false, context, previousMode, "ASPIRATIONAL_DIAGNOSTIC")
    }
}
