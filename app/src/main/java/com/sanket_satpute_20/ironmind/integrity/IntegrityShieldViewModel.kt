package com.sanket_satpute_20.ironmind.integrity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.ui.components.ShieldEvent
import com.sanket_satpute_20.ironmind.ui.components.ShieldMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.utils.AppClockProvider

/** A single event source for the home, morning, and night Integrity Shield hosts. */
data class ShieldUiState(
    val integrityLevel: Float = 1f,
    val mode: ShieldMode = ShieldMode.BUILD,
    val temptationCount: Int = 0,
    val completedSessionCount: Int = 0,
    val currentEvent: ShieldEvent? = null,
    val currentEventId: Long = 0L
)

class IntegrityShieldViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val database = IronMindDatabase.getDatabase(appContext)
    private val prefs = PrefManager.getInstance(appContext)
    private val integrityEngine = DailyIntegrityEngine(appContext)

    private val _uiState = MutableStateFlow(
        ShieldUiState(mode = prefs.toShieldMode())
    )
    val uiState: StateFlow<ShieldUiState> = _uiState.asStateFlow()

    private val dateKey = MutableStateFlow(AppClockProvider.clock.today().toString())
    private val eventQueue = ArrayDeque<ShieldEvent>()
    private var nextEventId = 1L
    private var previousSnapshot: ShieldSnapshot? = null
    private val firedMilestones = mutableSetOf<Int>()

    init {
        observeDayRollover()
        observeLiveCounts()
    }

    /** Call after a user confirms a morning-mode override. */
    fun refreshMode() {
        _uiState.update { it.copy(mode = prefs.toShieldMode()) }
    }

    /** Night Flow calls this after its recap is accepted. Persistence stays in DailyIntegrityEngine. */
    fun sealDay() {
        viewModelScope.launch {
            val record = integrityEngine.finalizeDay(AppClockProvider.clock.today(), "INTEGRITY_SHIELD_NIGHT_FLOW")
            enqueue(ShieldEvent.DaySealed(wasGoodDay = record.scorePercent >= GOOD_DAY_SCORE))
        }
    }

    fun onEventHandled() {
        val next = eventQueue.removeFirstOrNull()
        if (next == null) {
            _uiState.update { it.copy(currentEvent = null) }
        } else {
            emit(next)
        }
    }

    private fun observeDayRollover() {
        viewModelScope.launch {
            while (isActive) {
                val today = AppClockProvider.clock.today().toString()
                if (dateKey.value != today) {
                    dateKey.value = today
                    previousSnapshot = null
                    firedMilestones.clear()
                    _uiState.update {
                        it.copy(
                            integrityLevel = 1f,
                            temptationCount = 0,
                            completedSessionCount = 0,
                            currentEvent = null
                        )
                    }
                    eventQueue.clear()
                }
                delay(DATE_CHECK_INTERVAL_MS)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeLiveCounts() {
        viewModelScope.launch {
            dateKey.flatMapLatest { date ->
                combine(
                    database.temptationLogDao().observeCountForDate(date),
                    database.focusSessionDao().observeCompletedCountForDate(date)
                ) { temptations, sessions -> ShieldSnapshot(date, temptations, sessions) }
            }.collect { snapshot ->
                val previous = previousSnapshot
                if (previous?.date == snapshot.date) {
                    val newTemptations = (snapshot.temptations - previous.temptations).coerceAtLeast(0)
                    if (newTemptations > 0) {
                        // Rate limit visual/haptic spam: only queue ONE visual event per batch update,
                        // even if the mathematical integrity drops multiple times.
                        enqueue(ShieldEvent.TemptationCaught)
                    }
                    
                    val newSessions = (snapshot.sessions - previous.sessions).coerceAtLeast(0)
                    repeat(newSessions) { enqueue(ShieldEvent.SessionCompleted) }
                    
                    val milestone = snapshot.sessions / SESSIONS_PER_MILESTONE
                    if (newSessions > 0 && milestone > 0 && firedMilestones.add(milestone)) {
                        enqueue(ShieldEvent.MilestoneHit)
                    }
                }
                previousSnapshot = snapshot
                _uiState.update {
                    it.copy(
                        integrityLevel = integrityLevel(snapshot.temptations, snapshot.sessions),
                        temptationCount = snapshot.temptations,
                        completedSessionCount = snapshot.sessions,
                        mode = prefs.toShieldMode()
                    )
                }
            }
        }
    }

    private fun enqueue(event: ShieldEvent) {
        if (_uiState.value.currentEvent == null && eventQueue.isEmpty()) emit(event)
        else eventQueue.addLast(event)
    }

    private fun emit(event: ShieldEvent) {
        _uiState.update { it.copy(currentEvent = event, currentEventId = nextEventId++) }
    }

    private fun integrityLevel(temptations: Int, completedSessions: Int): Float {
        return (1f - temptations * TEMPTATION_PENALTY + completedSessions * SESSION_REWARD)
            .coerceIn(MINIMUM_INTEGRITY, 1f)
    }

    private fun PrefManager.toShieldMode(): ShieldMode = when (AdaptiveEngine.getCurrentMode(this).name) {
        ShieldMode.IRON.name -> ShieldMode.IRON
        ShieldMode.RECOVERY.name -> ShieldMode.RECOVERY
        ShieldMode.EXPERIMENT.name -> ShieldMode.EXPERIMENT
        else -> ShieldMode.BUILD
    }

    private data class ShieldSnapshot(val date: String, val temptations: Int, val sessions: Int)

    private companion object {
        const val DATE_CHECK_INTERVAL_MS = 60_000L
        const val SESSIONS_PER_MILESTONE = 3
        const val GOOD_DAY_SCORE = 60
        const val TEMPTATION_PENALTY = 0.12f
        const val SESSION_REWARD = 0.08f
        const val MINIMUM_INTEGRITY = 0.10f
    }
}
