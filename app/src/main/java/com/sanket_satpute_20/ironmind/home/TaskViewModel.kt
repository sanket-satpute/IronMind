package com.sanket_satpute_20.ironmind.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sanket_satpute_20.ironmind.accountability.AccountabilityManager
import com.sanket_satpute_20.ironmind.data.*
import com.sanket_satpute_20.ironmind.psychology.*
import com.sanket_satpute_20.ironmind.widget.ZenithWidget
import com.sanket_satpute_20.ironmind.crucible.CrucibleManager
import com.sanket_satpute_20.ironmind.artifact.ArtifactEngine
import com.sanket_satpute_20.ironmind.bossmode.BossModeAnalyzer
import com.sanket_satpute_20.ironmind.bossmode.BossModeUpgrade
import com.sanket_satpute_20.ironmind.focus.EarnedUnlockManager
import com.sanket_satpute_20.ironmind.focus.FocusSessionService
import com.sanket_satpute_20.ironmind.mission.MissionExecutionResult
import com.sanket_satpute_20.ironmind.mission.MissionExecutionService
import com.sanket_satpute_20.ironmind.share.MilestoneDetector
import com.sanket_satpute_20.ironmind.social.FirebaseSocialRepository
import com.sanket_satpute_20.ironmind.social.SocialOverview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class UserSnapshot(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
)

data class HomePerformanceSnapshot(
    val doneToday: Int,
    val skippedToday: Int,
    val pendingToday: Int,
    val completionRate: Int,
    val verdict: String,
    val isCleanDay: Boolean,
    val recentCompletionRate: Int?,
    val trendLabel: String?
)

enum class HomeMissionState {
    LIVE_NOW,
    UP_NEXT,
    LATER_TODAY,
    OVERDUE,
    COMPLETED,
    SKIPPED
}

data class HomeMissionItem(
    val task: Task,
    val state: HomeMissionState,
    val stateLabel: String,
    val supportingLabel: String? = null,
    val minutesUntilStart: Long? = null,
    val displayName: String = task.name,
    val groupBadge: String? = null,
    val linkedTasks: List<Task> = emptyList()
)

fun FirebaseUser.toSnapshot(): UserSnapshot {
    return UserSnapshot(
        uid = this.uid,
        displayName = this.displayName,
        email = this.email,
        photoUrl = this.photoUrl?.toString(),
        isAnonymous = this.isAnonymous
    )
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val localDb = IronMindDatabase.getDatabase(application)
    private val taskDao: TaskDao = localDb.taskDao()
    private val taskEventDao: TaskEventDao = localDb.taskEventDao()
    private val focusSessionDao: FocusSessionDao = localDb.focusSessionDao()
    private val userStateSnapshotDao: UserStateSnapshotDao = localDb.userStateSnapshotDao()
    private val confidenceIgnitionDao: ConfidenceIgnitionDao = localDb.confidenceIgnitionDao()
    private val voiceLogDao: VoiceLogDao = localDb.voiceLogDao()
    private val configChangeEventDao: ConfigChangeEventDao = localDb.configChangeEventDao()
    private val syncRepository = FirebaseSyncRepository()
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    val prefManager: PrefManager = PrefManager.getInstance(application)
    private val crucibleManager = CrucibleManager(prefManager)
    private val earnedUnlockManager = EarnedUnlockManager(application)
    private val missionExecutionService = MissionExecutionService(application)

    // Reactive states
    private val _userSnapshot = MutableStateFlow<UserSnapshot?>(auth.currentUser?.toSnapshot())
    val userSnapshot: StateFlow<UserSnapshot?> = _userSnapshot.asStateFlow()

    private val _authState = MutableStateFlow(AuthState(
        isLoggedIn = auth.currentUser != null,
        isAnonymous = auth.currentUser?.isAnonymous ?: true,
        displayName = auth.currentUser?.displayName,
        email = auth.currentUser?.email,
        photoUrl = auth.currentUser?.photoUrl?.toString(),
        uid = auth.currentUser?.uid
    ))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date -> taskDao.getTasksForDate(date.toString()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val recentTasks: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date ->
            val endDate = date.toString()
            val startDate = date.minusDays(6).toString()
            taskDao.getTasksInDateRange(startDate, endDate)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val identityEvidence: StateFlow<List<Task>> = tasks.map { list ->
        list.filter { it.isCompleted }.sortedByDescending { it.focusScore }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val homePerformanceSnapshot: StateFlow<HomePerformanceSnapshot> = combine(tasks, recentTasks) { todayTasks, recentTasks ->
        buildHomePerformanceSnapshot(todayTasks, recentTasks)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            buildHomePerformanceSnapshot(emptyList(), emptyList())
        )

    val homeMissionItems: StateFlow<List<HomeMissionItem>> = tasks
        .combine(flow {
            while (true) {
                emit(LocalTime.now())
                delay(30_000L)
            }
        }) { taskList, now ->
            buildHomeMissionItems(taskList, now)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val voiceLogs: StateFlow<List<VoiceLog>> = voiceLogDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val todayIgnitionEntry: StateFlow<ConfidenceIgnitionEntry?> = confidenceIgnitionDao
        .observeEntry(LocalDate.now().toString())
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allIgnitionEntries: StateFlow<List<ConfidenceIgnitionEntry>> = confidenceIgnitionDao
        .getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val configChangeEvents: StateFlow<List<ConfigChangeEvent>> = configChangeEventDao
        .getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val socialRepository = FirebaseSocialRepository()
    val socialOverview: StateFlow<SocialOverview?> = flow {
        emit(runCatching { socialRepository.getSocialOverview() }.getOrNull())
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _totalXp = MutableStateFlow(prefManager.totalXp)
    val totalXp = _totalXp.asStateFlow()

    private val _currentLevel = MutableStateFlow(prefManager.currentIdentityLevel)
    val currentLevel = _currentLevel.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent = _levelUpEvent.asStateFlow()

    private val _milestoneReached = MutableStateFlow<Int?>(null)
    val milestoneReached: StateFlow<Int?> = _milestoneReached.asStateFlow()

    private val _crucibleWonEvent = MutableStateFlow<String?>(null)
    val crucibleWonEvent = _crucibleWonEvent.asStateFlow()

    private val _crucibleFailedEvent = MutableStateFlow<String?>(null)
    val crucibleFailedEvent = _crucibleFailedEvent.asStateFlow()

    private val _newArtifactUnlocked = MutableStateFlow<String?>(null)
    val newArtifactUnlocked = _newArtifactUnlocked.asStateFlow()

    private val gamificationEngine = com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(application)

    private val _bossModeUpgradeEvent = MutableStateFlow<BossModeUpgrade?>(null)
    val bossModeUpgradeEvent = _bossModeUpgradeEvent.asStateFlow()
    
    private val _bossModeTrigger = MutableStateFlow(false)
    val bossModeTrigger: StateFlow<Boolean> = _bossModeTrigger.asStateFlow()

    private val _dailyCompleteEvent = MutableStateFlow(false)
    val dailyCompleteEvent: StateFlow<Boolean> = _dailyCompleteEvent.asStateFlow()

    private val authListener = FirebaseAuth.IdTokenListener { firebaseAuth ->
        _userSnapshot.value = firebaseAuth.currentUser?.toSnapshot()
        _authState.value = buildAuthState()
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { _ ->
        _authState.value = buildAuthState()
    }

    init {
        auth.addIdTokenListener(authListener)
        auth.addAuthStateListener(authStateListener)
        
        viewModelScope.launch {
            gamificationEngine.events.collect { event ->
                when (event) {
                    is com.sanket_satpute_20.ironmind.gamification.GamificationEvent.LevelUp -> {
                        prefManager.currentIdentityLevel = event.newLevel
                        _currentLevel.value = event.newLevel
                        _levelUpEvent.value = event.newLevel
                    }
                    is com.sanket_satpute_20.ironmind.gamification.GamificationEvent.XpEarned -> {
                        _totalXp.value = prefManager.totalXp
                    }
                    is com.sanket_satpute_20.ironmind.gamification.GamificationEvent.StreakUpdated -> {
                        if (com.sanket_satpute_20.ironmind.share.MilestoneDetector.isMilestone(event.newStreak)) {
                            _milestoneReached.value = event.newStreak
                        }
                    }
                }
            }
        }
        
        viewModelScope.launch {
            syncRepository.ensureAnonymousAuth()
            syncInitialStats()
            ensureDailyMicroActionForToday()
        }

        viewModelScope.launch {
            tasks.collect { taskList ->
                if (_selectedDate.value == LocalDate.now()) {
                    updateActiveTaskWindow()
                    earnedUnlockManager.syncForToday(taskList)
                }
            }
        }

        viewModelScope.launch {
            _selectedDate.collect { selected ->
                if (selected == LocalDate.now()) {
                    ensureDailyMicroActionForToday()
                }
            }
        }

        viewModelScope.launch {
            BossModeAnalyzer.resetBossMode(prefManager)
            val bossActivated = BossModeAnalyzer.checkAndApplyBossMode(taskDao, prefManager)
            if (bossActivated) {
                _bossModeTrigger.value = true
            }
        }
    }

    /**
     * Ensures Firestore has the latest stats on first load.
     */
    private suspend fun syncInitialStats() {
        userStateSnapshotDao.getPrimary()?.let { localSnapshot ->
            if (prefManager.totalXp == 0L && localSnapshot.totalXp > 0L) {
                HistoryRecorder.applyUserStateSnapshotToPrefs(localSnapshot, prefManager)
            }
        }

        val cloudSnapshot = syncRepository.fetchCloudUserStateSnapshot()
        if (cloudSnapshot != null) {
            if (cloudSnapshot.totalXp > prefManager.totalXp) {
                HistoryRecorder.applyUserStateSnapshotToPrefs(cloudSnapshot, prefManager)
            }
            _totalXp.value = prefManager.totalXp
            _currentLevel.value = prefManager.currentIdentityLevel
        }

        HistoryRecorder.recordUserStateSnapshot(getApplication())
        syncRepository.uploadUserStateSnapshot(HistoryRecorder.buildUserStateSnapshot(getApplication()))
        syncRepository.syncStructuredHistory(localDb)
        syncRepository.updateCloudStats(prefManager.totalXp, prefManager.streakCount, prefManager.currentIdentityLevel)
    }

    private fun buildAuthState(): AuthState {
        val user = auth.currentUser
        return AuthState(
            isLoggedIn = user != null,
            isAnonymous = user?.isAnonymous ?: true,
            displayName = user?.displayName,
            email = user?.email,
            photoUrl = user?.photoUrl?.toString(),
            uid = user?.uid
        )
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeIdTokenListener(authListener)
        auth.removeAuthStateListener(authStateListener)
    }

    fun refreshUser() {
        _authState.value = buildAuthState()
        _userSnapshot.value = auth.currentUser?.toSnapshot()
        HistoryRecorder.recordUserStateSnapshot(getApplication())
    }

    suspend fun secureIdentity(credential: AuthCredential): String? {
        val name = syncRepository.linkAnonymousWithGoogle(
            credential, 
            prefManager.totalXp, 
            prefManager.streakCount, 
            prefManager.currentIdentityLevel
        )
        if (name != null) {
            refreshUser()
            syncRepository.uploadUserStateSnapshot(HistoryRecorder.buildUserStateSnapshot(getApplication()))
        }
        return name
    }

    fun updateProfile(newName: String, newPhotoUrl: String) = viewModelScope.launch {
        val user = auth.currentUser ?: return@launch
        try {
            val updates = userProfileChangeRequest {
                displayName = newName
                if (newPhotoUrl.isNotEmpty()) {
                    photoUri = Uri.parse(newPhotoUrl)
                }
            }
            user.updateProfile(updates).await()
            user.reload().await()
            db.collection("users").document(user.uid).update(
                mapOf("name" to newName, "photoUrl" to newPhotoUrl)
            ).await()
            refreshUser()
            syncRepository.uploadUserStateSnapshot(HistoryRecorder.buildUserStateSnapshot(getApplication()))
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Profile update failed", e)
        }
    }

    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    fun markDone(task: Task) = viewModelScope.launch {
        resolveTasks(listOf(task), completed = true, reason = "HOME_COMPLETE")
    }

    private fun updateStatsOnCompletion(task: Task) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1).toString()
        val todayStr = today.toString()

        if (prefManager.lastCompleteDate != todayStr) {
            if (prefManager.lastCompleteDate == yesterday) {
                gamificationEngine.incrementStreak()
            } else {
                gamificationEngine.resetStreak()
                gamificationEngine.incrementStreak()
            }
            prefManager.lastCompleteDate = todayStr
        }
        prefManager.totalCompleted++
        
        val earnedXp = IdentityLevelEngine.calculateXp(task, prefManager.streakCount)
        gamificationEngine.addXp(earnedXp.toLong())
    }

    fun markSkipped(task: Task) = viewModelScope.launch {
        if (task.origin == DAILY_ONE_PERCENT_ORIGIN) return@launch
        resolveTasks(listOf(task), completed = false, reason = "HOME_SKIP")
    }

    fun markChainDone(parentMissionId: String) = viewModelScope.launch {
        if (parentMissionId.isBlank()) return@launch
        val unresolved = tasks.value.filter {
            it.parentMissionId == parentMissionId && !it.isBreakSegment && !it.isCompleted && !it.isSkipped
        }
        if (unresolved.isNotEmpty()) {
            resolveTasks(unresolved, completed = true, reason = "HOME_COMPLETE_CHAIN")
        }
    }

    fun markChainSkipped(parentMissionId: String) = viewModelScope.launch {
        if (parentMissionId.isBlank()) return@launch
        val unresolved = tasks.value.filter {
            it.parentMissionId == parentMissionId && !it.isBreakSegment && !it.isCompleted && !it.isSkipped
        }
        if (unresolved.isNotEmpty()) {
            resolveTasks(unresolved, completed = false, reason = "HOME_SKIP_CHAIN")
        }
    }

    fun updateActiveTaskWindow() {
        val now = LocalTime.now()
        val today = LocalDate.now().toString()
        val activeTask = tasks.value.firstOrNull { task ->
            if (task.isCompleted || task.isSkipped || task.isBreakSegment) return@firstOrNull false
            val start = parseTime(task.startTime) ?: return@firstOrNull false
            val end = parseTime(task.endTime) ?: return@firstOrNull false
            now.isAfter(start) && now.isBefore(end)
        }
        if (activeTask != null) {
            prefManager.activeTaskName = activeTask.name
            prefManager.activeTaskStartTime = activeTask.startTime
            prefManager.activeTaskEndTime = activeTask.endTime
            prefManager.activeTaskDate = today
        } else {
            prefManager.activeTaskName = ""
            prefManager.activeTaskStartTime = ""
            prefManager.activeTaskEndTime = ""
            prefManager.activeTaskDate = today
            prefManager.clearActiveMissionContextApps()
        }
        val context: Context = getApplication()
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) })
        viewModelScope.launch { ZenithWidget().updateAll(context) }
    }

    private fun stopActiveFocusSession() {
        val context: Context = getApplication()
        FocusSessionService.stop(context)
    }

    private suspend fun ensureDailyMicroActionForToday() {
        val today = LocalDate.now()
        val todayTasks = taskDao.getTasksForDateOnce(today.toString())
        val action = DailyMicroActionLibrary.actionForDate(today)
        val ignitionCue = confidenceIgnitionDao.getEntry(today.toString())?.let { entry ->
            runCatching {
                com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionLibrary
                    .couragePromptById(entry.couragePromptId)
                    .cue
            }.getOrNull()
        }
        val alignedCue = DailyMicroActionLibrary.ignitionAlignedCue(action, ignitionCue)
        val existing = todayTasks.firstOrNull { it.origin == DAILY_ONE_PERCENT_ORIGIN }
        if (existing == null) {
            taskDao.insertTask(
                DailyMicroActionLibrary.buildTask(action, today).copy(focusNotes = alignedCue)
            )
            return
        }

        if (existing.focusNotes != alignedCue) {
            taskDao.updateTask(
                existing.copy(
                    focusNotes = alignedCue,
                    lastModified = System.currentTimeMillis(),
                    syncStatus = "PENDING"
                )
            )
        }
    }

    private suspend fun resolveTasks(tasksToResolve: List<Task>, completed: Boolean, reason: String) {
        val pendingTasks = tasksToResolve.filter { task ->
            !task.isCompleted &&
                !task.isSkipped &&
                (completed || task.origin != DAILY_ONE_PERCENT_ORIGIN)
        }
        if (pendingTasks.isEmpty()) return

        val updatedTasks = pendingTasks.map { task ->
            val missionResult = if (completed) {
                missionExecutionService.completeMission(
                    taskId = task.id,
                    completionSource = "HOME_MANUAL",
                    eventReason = reason
                )
            } else {
                missionExecutionService.skipMission(
                    taskId = task.id,
                    skipReason = normalizeSkipReason(reason, task),
                    eventReason = reason
                )
            }
            val updatedTask = when (missionResult) {
                is MissionExecutionResult.Completed -> missionResult.task
                is MissionExecutionResult.Skipped -> missionResult.task
                else -> task
            }

            completeFocusSessionForTask(
                updatedTask,
                result = if (completed) "COMPLETED" else "SKIPPED",
                completed = completed
            )
            updatedTask
        }

        syncRepository.uploadTasks(updatedTasks)
        stopActiveFocusSession()

        if (completed) {
            updatedTasks.forEach { updatedTask ->
                if (!updatedTask.isBreakSegment) {
                    updateStatsOnCompletion(updatedTask)
                    val unlockedArtifactId = ArtifactEngine.evaluateAll(getApplication(), updatedTask)
                    if (unlockedArtifactId != null) {
                        _newArtifactUnlocked.value = unlockedArtifactId
                    }
                }
            }
        } else {
            val skippedFocusParts = updatedTasks.count { !it.isBreakSegment }
            if (skippedFocusParts > 0) {
                if (prefManager.streakShields > 0) {
                    prefManager.streakShields = prefManager.streakShields - 1
                } else if (prefManager.isStreakInCriticalState) {
                    gamificationEngine.resetStreak()
                    prefManager.isStreakInCriticalState = false
                } else {
                    prefManager.isStreakInCriticalState = true
                    prefManager.streakCriticalTimestamp = System.currentTimeMillis()
                }
                prefManager.totalSkipped += skippedFocusParts
            }
        }

        if (completed) {
            applyMorningCompletionBonusIfEligible()
            checkDailyComplete()
        }

        HistoryRecorder.recordUserStateSnapshot(getApplication())
        syncRepository.uploadUserStateSnapshot(HistoryRecorder.buildUserStateSnapshot(getApplication()))
        syncRepository.syncStructuredHistory(localDb)
        syncRepository.updateCloudStats(prefManager.totalXp, prefManager.streakCount, prefManager.currentIdentityLevel)
        updateActiveTaskWindow()
        earnedUnlockManager.syncTodayFromDatabase()
    }

    private suspend fun applyMorningCompletionBonusIfEligible() {
        val today = LocalDate.now()
        val todayKey = today.toString()
        if (prefManager.morningBonusArmedDate != todayKey) return
        if (prefManager.morningBonusAppliedDate == todayKey) return
        if (LocalTime.now().isAfter(LocalTime.NOON)) return

        val todaysTasks = taskDao.getTasksForDateOnce(todayKey)
        val focusTasks = todaysTasks.filterNot { it.isBreakSegment }
        if (focusTasks.isEmpty()) return
        if (focusTasks.any { it.isSkipped || !it.isCompleted }) return

        val totalBaseXp = focusTasks.sumOf { task ->
            IdentityLevelEngine.calculateXp(task, prefManager.streakCount)
        }
        val bonusXp = ((totalBaseXp * (prefManager.morningBonusMultiplier - 1f)).toLong()).coerceAtLeast(1L)
        prefManager.totalXp += bonusXp
        gamificationEngine.addXp(bonusXp)
        prefManager.morningBonusAppliedDate = todayKey
        _totalXp.value = prefManager.totalXp
    }

    private suspend fun completeFocusSessionForTask(task: Task, result: String, completed: Boolean) {
        val activeSession = focusSessionDao.getActiveSessionForTask(task.id) ?: return
        val endTimestamp = System.currentTimeMillis()
        val durationMinutes = ((endTimestamp - activeSession.startTimestamp) / 60_000L).coerceAtLeast(0L).toInt()
        focusSessionDao.update(
            activeSession.copy(
                endTimestamp = endTimestamp,
                actualDurationMinutes = durationMinutes,
                result = result,
                completed = completed,
                usedResetProtocol = prefManager.emergencyValveCooldownUntil > activeSession.startTimestamp,
                cooldownUsed = prefManager.emergencyValveCooldownActive,
                lastModified = endTimestamp,
                syncStatus = "PENDING"
            )
        )
    }

    private fun normalizeSkipReason(reason: String, task: Task): String {
        if (reason.isBlank()) return "SKIPPED_FROM_HOME"
        return when (reason) {
            "HOME_SKIP" -> if (task.parentMissionId.isNotBlank()) "CHAIN_PART_SKIPPED_FROM_HOME" else "MISSION_SKIPPED_FROM_HOME"
            "HOME_SKIP_CHAIN" -> "WHOLE_CHAIN_SKIPPED_FROM_HOME"
            else -> reason
        }
    }

    private fun parseTime(timeStr: String): LocalTime? {
        val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
        for (f in formats) {
            runCatching { return LocalTime.parse(timeStr.uppercase(), DateTimeFormatter.ofPattern(f, Locale.US)) }
        }
        return null
    }

    private fun buildHomePerformanceSnapshot(tasks: List<Task>, recentTasks: List<Task>): HomePerformanceSnapshot {
        val focusTasks = tasks.filterNot { it.isBreakSegment }
        val recentFocusTasks = recentTasks.filterNot { it.isBreakSegment }
        val doneToday = focusTasks.count { it.isCompleted }
        val skippedToday = focusTasks.count { it.isSkipped }
        val pendingToday = focusTasks.count { !it.isCompleted && !it.isSkipped }
        val resolvedToday = doneToday + skippedToday
        val completionRate = if (resolvedToday > 0) {
            ((doneToday * 100f) / resolvedToday).toInt()
        } else {
            0
        }
        val resolvedRecent = recentFocusTasks.count { it.isCompleted || it.isSkipped }
        val recentCompletionRate = if (resolvedRecent > 0) {
            ((recentFocusTasks.count { it.isCompleted } * 100f) / resolvedRecent).toInt()
        } else {
            null
        }
        val trendLabel = recentCompletionRate?.let { rate ->
            when {
                rate >= 90 -> "7-day trend: elite follow-through"
                rate >= 75 -> "7-day trend: strong and stable"
                rate >= 55 -> "7-day trend: mixed discipline"
                else -> "7-day trend: skip pressure rising"
            }
        }

        val verdict = when {
            focusTasks.isEmpty() -> "No missions loaded yet. Build the day before judging it."
            skippedToday == 0 && doneToday == 0 -> "No proof yet. Start the first mission before drift takes the wheel."
            skippedToday == 0 && pendingToday == 0 -> "Perfect execution. Every mission on today's board is secured."
            skippedToday == 0 -> "Clean day so far. No skips yet — keep it going."
            skippedToday == 1 && doneToday >= 1 -> "One skipped. Recover the rest of the board."
            skippedToday == 1 -> "One mission skipped. Refocus and continue."
            skippedToday >= doneToday -> "Some tasks were skipped. Review and adjust your plan."
            else -> "The board is still recoverable, but skipped promises are stacking up."
        }

        return HomePerformanceSnapshot(
            doneToday = doneToday,
            skippedToday = skippedToday,
            pendingToday = pendingToday,
            completionRate = completionRate,
            verdict = verdict,
            isCleanDay = skippedToday == 0,
            recentCompletionRate = recentCompletionRate,
            trendLabel = trendLabel
        )
    }

    private fun buildHomeMissionItems(tasks: List<Task>, now: LocalTime): List<HomeMissionItem> {
        val grouped = tasks.groupBy { task ->
            task.parentMissionId.takeIf { it.isNotBlank() && task.segmentCount > 1 } ?: "single_${task.id}"
        }

        return grouped.values.map { taskGroup ->
            if (taskGroup.size == 1 && taskGroup.first().parentMissionId.isBlank()) {
                buildSingleHomeMissionItem(taskGroup.first(), now)
            } else {
                buildGroupedHomeMissionItem(taskGroup, now)
            }
        }.sortedWith(
            compareBy<HomeMissionItem>(
                { item -> parseTime(item.task.startTime) ?: LocalTime.MAX },
                { item -> item.task.id }
            )
        )
    }

    private fun buildSingleHomeMissionItem(task: Task, now: LocalTime): HomeMissionItem {
        val start = parseTime(task.startTime)
        val end = parseTime(task.endTime)
        val minutesUntilStart = start?.let { java.time.temporal.ChronoUnit.MINUTES.between(now, it) }
        val isLiveWindow = task.isInProgress || (start != null && end != null && now.isAfter(start) && now.isBefore(end))
        val isOverdue = end != null && now.isAfter(end)
        val segmentContext = when {
            task.isBreakSegment && task.segmentBaseName.isNotBlank() -> "Recovery gap for ${task.segmentBaseName}"
            task.segmentCount > 1 && task.segmentIndex > 0 -> "Part ${task.segmentIndex}/${task.segmentCount} of ${task.segmentBaseName.ifBlank { task.name }}"
            else -> null
        }

        return when {
            task.isCompleted -> HomeMissionItem(
                task = task,
                state = HomeMissionState.COMPLETED,
                stateLabel = "SECURED",
                supportingLabel = segmentContext ?: "Completed today"
            )

            task.isSkipped -> HomeMissionItem(
                task = task,
                state = HomeMissionState.SKIPPED,
                stateLabel = "SKIPPED",
                supportingLabel = segmentContext ?: "Skipped today"
            )

            isLiveWindow -> HomeMissionItem(
                task = task,
                state = HomeMissionState.LIVE_NOW,
                stateLabel = "LIVE NOW",
                supportingLabel = segmentContext ?: "Execution window is open",
                minutesUntilStart = minutesUntilStart
            )

            minutesUntilStart != null && minutesUntilStart in 0..20 -> HomeMissionItem(
                task = task,
                state = HomeMissionState.UP_NEXT,
                stateLabel = "UP NEXT",
                supportingLabel = segmentContext ?: "Starts in ${minutesUntilStart.toInt()} min",
                minutesUntilStart = minutesUntilStart
            )

            isOverdue -> HomeMissionItem(
                task = task,
                state = HomeMissionState.OVERDUE,
                stateLabel = "OVERDUE",
                supportingLabel = segmentContext ?: "The planned window closed unresolved",
                minutesUntilStart = minutesUntilStart
            )

            else -> HomeMissionItem(
                task = task,
                state = HomeMissionState.LATER_TODAY,
                stateLabel = "LATER",
                supportingLabel = segmentContext ?: minutesUntilStart?.let { "Starts in ${it.toInt()} min" }
            )
        }
    }

    private fun buildGroupedHomeMissionItem(taskGroup: List<Task>, now: LocalTime): HomeMissionItem {
        val ordered = taskGroup.sortedBy { parseTime(it.startTime) ?: LocalTime.MAX }
        val focusParts = ordered.filterNot { it.isBreakSegment }
        val firstFocus = focusParts.firstOrNull() ?: ordered.first()
        val unresolved = ordered.firstOrNull { !it.isCompleted && !it.isSkipped }
        val unresolvedFocus = focusParts.firstOrNull { !it.isCompleted && !it.isSkipped }
        val representative = unresolvedFocus ?: unresolved ?: ordered.last()
        val completedFocus = focusParts.count { it.isCompleted }
        val skippedFocus = focusParts.count { it.isSkipped }
        val totalFocus = focusParts.size.coerceAtLeast(1)
        val start = parseTime(representative.startTime)
        val end = parseTime(representative.endTime)
        val minutesUntilStart = start?.let { java.time.temporal.ChronoUnit.MINUTES.between(now, it) }
        val isLiveWindow = representative.isInProgress || (start != null && end != null && now.isAfter(start) && now.isBefore(end))
        val isOverdue = unresolved != null && end != null && now.isAfter(end)
        val baseName = firstFocus.segmentBaseName.ifBlank { firstFocus.name.substringBefore(" · ") }
        val support = when {
            skippedFocus > 0 && unresolved == null -> "$completedFocus/$totalFocus parts secured · chain incomplete"
            unresolved?.isBreakSegment == true -> "$completedFocus/$totalFocus parts secured · recovery gap in play"
            unresolved != null -> "$completedFocus/$totalFocus parts secured"
            else -> "$totalFocus/$totalFocus parts secured"
        }

        val state = when {
            skippedFocus > 0 && unresolved == null -> HomeMissionState.SKIPPED
            completedFocus == totalFocus && skippedFocus == 0 -> HomeMissionState.COMPLETED
            isLiveWindow -> HomeMissionState.LIVE_NOW
            minutesUntilStart != null && minutesUntilStart in 0..20 -> HomeMissionState.UP_NEXT
            isOverdue -> HomeMissionState.OVERDUE
            else -> HomeMissionState.LATER_TODAY
        }

        val stateLabel = when (state) {
            HomeMissionState.LIVE_NOW -> "LIVE NOW"
            HomeMissionState.UP_NEXT -> "UP NEXT"
            HomeMissionState.LATER_TODAY -> "LATER"
            HomeMissionState.OVERDUE -> "OVERDUE"
            HomeMissionState.COMPLETED -> "SECURED"
            HomeMissionState.SKIPPED -> "SKIPPED"
        }

        return HomeMissionItem(
            task = representative,
            state = state,
            stateLabel = stateLabel,
            supportingLabel = support,
            minutesUntilStart = minutesUntilStart,
            displayName = baseName,
            groupBadge = "${totalFocus}-PART CHAIN",
            linkedTasks = ordered
        )
    }

    fun clearMilestone() { _milestoneReached.value = null }
    fun clearLevelUp() { _levelUpEvent.value = null }
    fun clearCrucibleWon() { _crucibleWonEvent.value = null }
    fun clearCrucibleFailed() { _crucibleFailedEvent.value = null }
    fun clearArtifactUnlocked() { _newArtifactUnlocked.value = null }
    fun clearBossModeUpgrade() { _bossModeUpgradeEvent.value = null }
    fun clearBossModeTrigger() { _bossModeTrigger.value = false }
    fun clearDailyComplete() { _dailyCompleteEvent.value = false }

    private suspend fun checkDailyComplete() {
        val todayTasks = taskDao.getTasksForDateOnce(LocalDate.now().toString())
        val focusTasks = todayTasks.filter { !it.isBreakSegment }
        if (focusTasks.isNotEmpty() && focusTasks.all { it.isCompleted }) {
            _dailyCompleteEvent.value = true
        }
    }
}
