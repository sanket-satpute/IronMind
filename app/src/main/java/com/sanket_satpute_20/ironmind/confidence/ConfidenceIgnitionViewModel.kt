package com.sanket_satpute_20.ironmind.confidence

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.ironmind.data.ConfidenceIgnitionEntry
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.home.DAILY_ONE_PERCENT_ORIGIN
import com.sanket_satpute_20.ironmind.home.DailyMicroActionLibrary
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class ConfidenceIgnitionStep {
    BREATHE,
    SPEAK,
    MOVE,
    COURAGE,
    COMPLETE
}

data class ConfidenceIgnitionUiState(
    val date: LocalDate = LocalDate.now(),
    val line: IgnitionLine = ConfidenceIgnitionLibrary.planFor(LocalDate.now()).line,
    val voicePrompt: IgnitionVoicePrompt = ConfidenceIgnitionLibrary.planFor(LocalDate.now()).voicePrompt,
    val movePrompt: IgnitionMovePrompt = ConfidenceIgnitionLibrary.planFor(LocalDate.now()).movePrompt,
    val couragePrompt: IgnitionCouragePrompt = ConfidenceIgnitionLibrary.planFor(LocalDate.now()).couragePrompt,
    val entry: ConfidenceIgnitionEntry? = null
) {
    val currentStep: ConfidenceIgnitionStep
        get() = when {
            entry == null -> ConfidenceIgnitionStep.BREATHE
            !entry.breathDone -> ConfidenceIgnitionStep.BREATHE
            !entry.voiceRecorded -> ConfidenceIgnitionStep.SPEAK
            !entry.moveDone -> ConfidenceIgnitionStep.MOVE
            !entry.courageAccepted -> ConfidenceIgnitionStep.COURAGE
            else -> ConfidenceIgnitionStep.COMPLETE
        }
}

class ConfidenceIgnitionViewModel(application: Application) : AndroidViewModel(application) {

    private val today = LocalDate.now()
    private val todayKey = today.toString()
    private val database = IronMindDatabase.getDatabase(application)
    private val dao = database.confidenceIgnitionDao()
    private val taskDao = database.taskDao()
    private val defaultPlan = ConfidenceIgnitionLibrary.planFor(today)
    private val planState = MutableStateFlow(defaultPlan)
    private val saveState = MutableStateFlow(false)

    val uiState: StateFlow<ConfidenceIgnitionUiState> = combine(
        dao.observeEntry(todayKey),
        planState
    ) { entry, plan ->
            ConfidenceIgnitionUiState(
                date = today,
                line = ConfidenceIgnitionLibrary.lineById(entry?.ignitionLineId ?: plan.line.id),
                voicePrompt = ConfidenceIgnitionLibrary.voicePromptById(entry?.voicePromptId ?: plan.voicePrompt.id),
                movePrompt = ConfidenceIgnitionLibrary.movePromptById(entry?.movePromptId ?: plan.movePrompt.id),
                couragePrompt = ConfidenceIgnitionLibrary.couragePromptById(entry?.couragePromptId ?: plan.couragePrompt.id),
                entry = entry
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConfidenceIgnitionUiState(
                date = today,
                line = defaultPlan.line,
                voicePrompt = defaultPlan.voicePrompt,
                movePrompt = defaultPlan.movePrompt,
                couragePrompt = defaultPlan.couragePrompt
            )
        )

    val isSaving: StateFlow<Boolean> = saveState.asStateFlow()

    init {
        viewModelScope.launch {
            val recentEntries = dao.getRecent(21).filterNot { it.date == todayKey }
            val adaptivePlan = ConfidenceIgnitionLibrary.adaptivePlanFor(today, recentEntries)
            planState.value = adaptivePlan
            if (dao.getEntry(todayKey) == null) {
                dao.upsert(
                    ConfidenceIgnitionEntry(
                        date = todayKey,
                        ignitionLineId = adaptivePlan.line.id,
                        voicePromptId = adaptivePlan.voicePrompt.id,
                        movePromptId = adaptivePlan.movePrompt.id,
                        couragePromptId = adaptivePlan.couragePrompt.id
                    )
                )
            }
        }
    }

    fun markBreathDone() = updateEntry(transform = { it.copy(breathDone = true) })

    fun markVoiceRecorded() = updateEntry(transform = { it.copy(voiceRecorded = true) })

    fun markMoveDone() = updateEntry(transform = { it.copy(moveDone = true) })

    fun markCourageAccepted() = updateEntry(
        transform = { entry ->
            val completed = entry.breathDone && entry.voiceRecorded && entry.moveDone
            entry.copy(
                courageAccepted = true,
                completed = completed,
                completedAt = if (completed) System.currentTimeMillis() else 0L
            )
        },
        afterSave = { updated ->
            syncDailyActionWithCourageEdge(updated.couragePromptId)
        }
    )

    private fun updateEntry(
        transform: (ConfidenceIgnitionEntry) -> ConfidenceIgnitionEntry,
        afterSave: suspend (ConfidenceIgnitionEntry) -> Unit = {}
    ) {
        viewModelScope.launch {
            saveState.value = true
            val current = dao.getEntry(todayKey)
                ?: ConfidenceIgnitionEntry(
                    date = todayKey,
                    ignitionLineId = planState.value.line.id,
                    voicePromptId = planState.value.voicePrompt.id,
                    movePromptId = planState.value.movePrompt.id,
                    couragePromptId = planState.value.couragePrompt.id
                )
            val updated = transform(current)
            val finalEntry = if (updated.breathDone && updated.voiceRecorded && updated.moveDone && updated.courageAccepted && !updated.completed) {
                updated.copy(completed = true, completedAt = System.currentTimeMillis())
            } else {
                updated
            }
            dao.upsert(finalEntry)
            afterSave(finalEntry)
            saveState.value = false
        }
    }

    private suspend fun syncDailyActionWithCourageEdge(couragePromptId: String) {
        val courageCue = ConfidenceIgnitionLibrary.couragePromptById(couragePromptId).cue
        val todayTasks = taskDao.getTasksForDateOnce(todayKey)
        val dailyTask = todayTasks.firstOrNull { it.origin == DAILY_ONE_PERCENT_ORIGIN } ?: return
        val microAction = DailyMicroActionLibrary.actionForDate(today)
        taskDao.updateTask(
            dailyTask.copy(
                focusNotes = DailyMicroActionLibrary.ignitionAlignedCue(microAction, courageCue),
                lastModified = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
        )
    }
}
