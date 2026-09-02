package com.sanket_satpute_20.ironmind.onboarding

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.ironmind.apps.AppClassificationRepository
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.SavedTaskBlueprintMissionRecord
import com.sanket_satpute_20.ironmind.data.SavedTaskBlueprintRecord
import com.sanket_satpute_20.ironmind.data.SavedTaskBlueprint
import com.sanket_satpute_20.ironmind.data.SavedTaskBlueprintDao
import com.sanket_satpute_20.ironmind.data.SavedTaskBlueprintMission
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.data.TaskDao
import com.sanket_satpute_20.ironmind.data.TaskEvent
import com.sanket_satpute_20.ironmind.data.TaskEventDao
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Collections
import java.util.Locale

enum class MissionType { DEEP_WORK, APP_INVEST, PHYSICAL, PREP }
enum class FocusTaskType { STUDY, CODE, DESIGN, READ, PHYSICAL, ADMIN, DSA, SYSTEM_DESIGN, BEHAVIORAL, PROJECT_REVIEW, OTHER }
enum class FocusPreset { CLASSIC_25_5, DEEP_50_10, QUICK_15_5 }

data class TaskBuilderState(
    val id: Int = System.currentTimeMillis().toInt(),
    val dbId: Int? = null,
    val name: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val importanceRank: Int = 3,
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false,
    val type: MissionType = MissionType.DEEP_WORK,
    val focusTaskType: FocusTaskType = FocusTaskType.OTHER,
    val focusModeEnabled: Boolean = false,
    val focusPreset: FocusPreset = FocusPreset.CLASSIC_25_5,
    val parentMissionId: String = "",
    val segmentBaseName: String = "",
    val segmentIndex: Int = 0,
    val segmentCount: Int = 0,
    val isBreakSegment: Boolean = false,
    val packageName: String? = null // For APP_INVEST
)

data class InvestableApp(
    val name: String,
    val packageName: String
)

data class TaskBuilderValidationIssue(
    val message: String
)

data class TaskBuilderPlanningSummary(
    val missionCount: Int = 0,
    val totalPlannedMinutes: Int = 0,
    val criticalCount: Int = 0,
    val deepWorkCount: Int = 0,
    val appInvestCount: Int = 0,
    val physicalCount: Int = 0,
    val prepCount: Int = 0,
    val conflictCount: Int = 0,
    val longestMissionMinutes: Int = 0
)

data class TaskBuilderSuggestion(
    val title: String,
    val detail: String,
    val severity: SuggestionSeverity = SuggestionSeverity.INFO
)

enum class SuggestionSeverity { INFO, CAUTION, STRONG }

private fun TaskBuilderState.isLockedResolvedMission(): Boolean = isCompleted || isSkipped

data class TaskBlueprint(
    val id: String,
    val title: String,
    val summary: String,
    val missions: List<TaskBuilderState>
)

private data class TaskBuilderComparableState(
    val dbId: Int?,
    val name: String,
    val startTime: String,
    val endTime: String,
    val importanceRank: Int,
    val type: MissionType,
    val focusTaskType: FocusTaskType,
    val focusModeEnabled: Boolean,
    val focusPreset: FocusPreset,
    val parentMissionId: String,
    val segmentBaseName: String,
    val segmentIndex: Int,
    val segmentCount: Int,
    val isBreakSegment: Boolean,
    val packageName: String?
)

internal data class SleepLockWindow(
    val start: LocalTime,
    val end: LocalTime
)

internal fun resolveSleepLockWindow(prefManager: PrefManager): SleepLockWindow {
    return if (prefManager.sleepLockEnabled) {
        SleepLockWindow(
            start = LocalTime.of(
                prefManager.sleepLockBedHour.coerceIn(0, 23),
                prefManager.sleepLockBedMinute.coerceIn(0, 59)
            ),
            end = LocalTime.of(
                prefManager.sleepLockWakeHour.coerceIn(0, 23),
                prefManager.sleepLockWakeMinute.coerceIn(0, 59)
            )
        )
    } else {
        SleepLockWindow(
            start = LocalTime.of(23, 0),
            end = LocalTime.of(4, 0)
        )
    }
}

internal fun buildSleepLockProtectedSegments(window: SleepLockWindow): List<Pair<LocalTime, LocalTime>> {
    return buildList {
        if (window.end != LocalTime.MIDNIGHT) {
            add(LocalTime.MIDNIGHT to window.end)
        }
        add(window.start to LocalTime.MAX)
    }
}

internal fun intersectsTimeRange(
    start: LocalTime,
    end: LocalTime,
    rangeStart: LocalTime,
    rangeEnd: LocalTime
): Boolean {
    return start < rangeEnd && end > rangeStart
}

internal fun intersectsSleepLockWindow(
    start: LocalTime,
    end: LocalTime,
    window: SleepLockWindow
): Boolean {
    return buildSleepLockProtectedSegments(window).any { (rangeStart, rangeEnd) ->
        intersectsTimeRange(start, end, rangeStart, rangeEnd)
    }
}

internal fun recommendedFocusModeEnabled(taskType: FocusTaskType): Boolean {
    return taskType == FocusTaskType.STUDY ||
        taskType == FocusTaskType.CODE ||
        taskType == FocusTaskType.DESIGN ||
        taskType == FocusTaskType.READ ||
        taskType == FocusTaskType.DSA ||
        taskType == FocusTaskType.SYSTEM_DESIGN ||
        taskType == FocusTaskType.PROJECT_REVIEW
}

internal fun defaultFocusTaskTypeForMission(missionType: MissionType): FocusTaskType {
    return when (missionType) {
        MissionType.DEEP_WORK -> FocusTaskType.STUDY
        MissionType.APP_INVEST -> FocusTaskType.ADMIN
        MissionType.PHYSICAL -> FocusTaskType.PHYSICAL
        MissionType.PREP -> FocusTaskType.DSA
    }
}

internal fun isPrepTaskType(taskType: FocusTaskType): Boolean {
    return taskType == FocusTaskType.DSA ||
        taskType == FocusTaskType.SYSTEM_DESIGN ||
        taskType == FocusTaskType.BEHAVIORAL ||
        taskType == FocusTaskType.PROJECT_REVIEW
}

internal fun parseFocusTaskType(value: String): FocusTaskType {
    return runCatching { FocusTaskType.valueOf(value) }.getOrDefault(FocusTaskType.OTHER)
}

internal fun parseFocusPreset(value: String): FocusPreset {
    return runCatching { FocusPreset.valueOf(value) }.getOrDefault(FocusPreset.CLASSIC_25_5)
}

class TaskBuilderViewModel(
    private val taskDao: TaskDao,
    private val taskEventDao: TaskEventDao,
    private val savedTaskBlueprintDao: SavedTaskBlueprintDao,
    private val prefManager: PrefManager
) : ViewModel() {
    private var investableAppsLoaded = false

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskBuilderState>>(emptyList())
    val tasks: StateFlow<List<TaskBuilderState>> = _tasks.asStateFlow()

    private val _investableApps = MutableStateFlow<List<InvestableApp>>(emptyList())
    val investableApps: StateFlow<List<InvestableApp>> = _investableApps.asStateFlow()

    private val _validationIssues = MutableStateFlow<List<TaskBuilderValidationIssue>>(emptyList())
    val validationIssues: StateFlow<List<TaskBuilderValidationIssue>> = _validationIssues.asStateFlow()

    private val _saveCompletedEvent = MutableStateFlow(false)
    val saveCompletedEvent: StateFlow<Boolean> = _saveCompletedEvent.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _planningSummary = MutableStateFlow(TaskBuilderPlanningSummary())
    val planningSummary: StateFlow<TaskBuilderPlanningSummary> = _planningSummary.asStateFlow()

    private val _saveSummaryMessage = MutableStateFlow<String?>(null)
    val saveSummaryMessage: StateFlow<String?> = _saveSummaryMessage.asStateFlow()

    private val _suggestions = MutableStateFlow<List<TaskBuilderSuggestion>>(emptyList())
    val suggestions: StateFlow<List<TaskBuilderSuggestion>> = _suggestions.asStateFlow()

    private val _recommendedBlueprints = MutableStateFlow<List<TaskBlueprint>>(emptyList())
    val recommendedBlueprints: StateFlow<List<TaskBlueprint>> = _recommendedBlueprints.asStateFlow()

    private val _savedBlueprints = MutableStateFlow<List<TaskBlueprint>>(emptyList())
    val savedBlueprints: StateFlow<List<TaskBlueprint>> = _savedBlueprints.asStateFlow()

    private var lastCommittedSnapshot: List<TaskBuilderComparableState> = emptyList()

    init {
        viewModelScope.launch {
            migrateLegacySavedBlueprintsIfNeeded()
            loadSavedBlueprints()
        }
        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadTasksForDate(date)
            }
        }
    }

    private suspend fun loadTasksForDate(date: LocalDate) {
        val existingTasks = taskDao.getTasksForDateOnce(date.toString())
        _tasks.value = existingTasks.map {
            TaskBuilderState(
                id = it.id,
                dbId = it.id,
                name = it.name,
                startTime = it.startTime,
                endTime = it.endTime,
                importanceRank = it.importanceRank,
                isCompleted = it.isCompleted,
                isSkipped = it.isSkipped,
                focusTaskType = parseFocusTaskType(it.taskType),
                focusModeEnabled = it.focusModeEnabled,
                focusPreset = parseFocusPreset(it.focusPreset),
                parentMissionId = it.parentMissionId,
                segmentBaseName = it.segmentBaseName,
                segmentIndex = it.segmentIndex,
                segmentCount = it.segmentCount,
                isBreakSegment = it.isBreakSegment,
                packageName = it.packageName,
                type = when {
                    isPrepTaskType(parseFocusTaskType(it.taskType)) -> MissionType.PREP
                    it.packageName != null -> MissionType.APP_INVEST
                    it.difficultyMultiplier > 1.2f -> MissionType.DEEP_WORK
                    else -> MissionType.PHYSICAL
                }
            )
        }
        lastCommittedSnapshot = _tasks.value.toComparableSnapshot()
        _hasUnsavedChanges.value = false
        _validationIssues.value = emptyList()
        _saveCompletedEvent.value = false
        _saveSummaryMessage.value = null
        syncPlanningSummary()
        syncSuggestions()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun isPastDate(): Boolean = _selectedDate.value.isBefore(LocalDate.now())

    fun isNightDecisionLocked(): Boolean = nightDecisionLockMessage() != null

    fun nightDecisionLockMessage(): String? {
        val selected = _selectedDate.value
        if (selected != LocalDate.now().plusDays(1)) return null
        if (prefManager.nightDecisionDate != selected.toString()) return null
        return when (NightDecisionStatus.fromWireValue(prefManager.nightDecisionStatus)) {
            NightDecisionStatus.HOLIDAY -> "Tomorrow is marked as a holiday. Task creation is sealed for that day."
            NightDecisionStatus.EMERGENCY_LEAVE -> "Tomorrow is marked as emergency leave. Task creation is sealed for that day."
            else -> null
        }
    }

    fun fetchInvestableApps(context: Context) {
        if (investableAppsLoaded && _investableApps.value.isNotEmpty()) return
        viewModelScope.launch {
            val pm = context.packageManager
            val blocked = AppClassificationRepository.getInstance(context).getBlockedLikePackagesSnapshot()
            
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && it.packageName !in blocked }
                .map { InvestableApp(pm.getApplicationLabel(it).toString(), it.packageName) }
                .sortedBy { it.name }
            
            _investableApps.value = apps
            investableAppsLoaded = true
        }
    }

    fun addNewMission(newTask: TaskBuilderState) {
        nightDecisionLockMessage()?.let {
            _saveSummaryMessage.value = it
            return
        }
        if (_tasks.value.size < 6 && !isPastDate()) {
            _tasks.update { it + newTask }
            _validationIssues.value = emptyList()
            syncUnsavedChanges()
            syncPlanningSummary()
            syncSuggestions()
        }
    }

    fun addNewMissions(newTasks: List<TaskBuilderState>) {
        nightDecisionLockMessage()?.let {
            _saveSummaryMessage.value = it
            return
        }
        if (isPastDate() || newTasks.isEmpty()) return
        val remainingSlots = (6 - _tasks.value.size).coerceAtLeast(0)
        val tasksToInsert = newTasks.take(remainingSlots)
        if (tasksToInsert.isEmpty()) return
        _tasks.update { it + tasksToInsert }
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun updateMission(updatedTask: TaskBuilderState) {
        if (isPastDate()) return
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == updatedTask.id) updatedTask else task
            }
        }
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun updateMissionChain(parentMissionId: String, updatedTasks: List<TaskBuilderState>) {
        if (isPastDate() || parentMissionId.isBlank() || updatedTasks.isEmpty()) return
        _tasks.update { currentTasks ->
            (currentTasks.filterNot { it.parentMissionId == parentMissionId } + updatedTasks)
                .sortedWith(compareBy<TaskBuilderState> { parseTime(it.startTime) ?: LocalTime.MAX }.thenBy { it.id })
        }
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun removeTask(id: Int) {
        if (!isPastDate()) {
            _tasks.update { currentTasks ->
                val target = currentTasks.firstOrNull { it.id == id } ?: return@update currentTasks
                if (target.isLockedResolvedMission()) return@update currentTasks
                currentTasks.filter { it.id != id }
            }
            _validationIssues.value = emptyList()
            syncUnsavedChanges()
            syncPlanningSummary()
            syncSuggestions()
        }
    }

    fun removeTaskChain(parentMissionId: String) {
        if (isPastDate() || parentMissionId.isBlank()) return
        _tasks.update { currentTasks ->
            val chain = currentTasks.filter { it.parentMissionId == parentMissionId }
            if (chain.any { it.isLockedResolvedMission() }) return@update currentTasks
            currentTasks.filterNot { it.parentMissionId == parentMissionId }
        }
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun duplicateMission(id: Int) {
        nightDecisionLockMessage()?.let {
            _saveSummaryMessage.value = it
            return
        }
        if (isPastDate() || _tasks.value.size >= 6) return
        val source = _tasks.value.firstOrNull { it.id == id } ?: return
        val duplicate = source.copy(
            id = System.currentTimeMillis().toInt(),
            dbId = null,
            isCompleted = false,
            isSkipped = false,
            name = "${source.name} Copy"
        )
        _tasks.update { it + duplicate }
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun duplicateMissionChain(parentMissionId: String) {
        nightDecisionLockMessage()?.let {
            _saveSummaryMessage.value = it
            return
        }
        if (isPastDate() || parentMissionId.isBlank()) return
        val sourceChain = _tasks.value
            .filter { it.parentMissionId == parentMissionId }
            .sortedBy { parseTime(it.startTime) ?: LocalTime.MAX }
        if (sourceChain.isEmpty()) return

        val remainingSlots = (6 - _tasks.value.size).coerceAtLeast(0)
        if (remainingSlots <= 0) {
            _saveSummaryMessage.value = "No open mission slots left for this chain."
            return
        }
        if (remainingSlots < sourceChain.size) {
            _saveSummaryMessage.value = "Need ${sourceChain.size} open slots to duplicate this full chain."
            return
        }

        val newParentId = "split_${System.currentTimeMillis()}"
        val duplicated = sourceChain
            .mapIndexed { index, task ->
                val copiedBaseName = task.segmentBaseName.ifBlank { task.name.substringBefore(" · ") } + " Copy"
                task.copy(
                    id = (System.currentTimeMillis() + index).toInt(),
                    dbId = null,
                    isCompleted = false,
                    isSkipped = false,
                    parentMissionId = newParentId,
                    segmentBaseName = copiedBaseName,
                    name = when {
                        task.isBreakSegment -> "$copiedBaseName · Recovery Gap"
                        task.segmentCount > 1 && task.segmentIndex > 0 -> "$copiedBaseName · Part ${task.segmentIndex}/${task.segmentCount}"
                        else -> "$copiedBaseName"
                    }
                )
            }

        _tasks.update { it + duplicated }
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun moveTaskChain(parentMissionId: String, moveUp: Boolean) {
        if (isPastDate() || parentMissionId.isBlank()) return
        val chunks = buildMissionChunks(_tasks.value).toMutableList()
        val currentIndex = chunks.indexOfFirst { chunk -> chunk.any { it.parentMissionId == parentMissionId } }
        if (currentIndex == -1) return

        val targetIndex = if (moveUp) currentIndex - 1 else currentIndex + 1
        if (targetIndex !in chunks.indices) return

        Collections.swap(chunks, currentIndex, targetIndex)
        _tasks.value = chunks.flatten()
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun clearAllMissions() {
        nightDecisionLockMessage()?.let {
            _saveSummaryMessage.value = it
            return
        }
        if (isPastDate()) return
        _tasks.update { currentTasks ->
            currentTasks.filter { it.isLockedResolvedMission() }
        }
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun moveTask(fromIndex: Int, toIndex: Int) {
        if (isPastDate()) return
        val currentList = _tasks.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            Collections.swap(currentList, fromIndex, toIndex)
            _tasks.value = currentList
            _validationIssues.value = emptyList()
            syncUnsavedChanges()
            syncPlanningSummary()
            syncSuggestions()
        }
    }

    fun attemptSave() {
        nightDecisionLockMessage()?.let {
            _saveSummaryMessage.value = it
            _saveCompletedEvent.value = false
            return
        }
        if (isPastDate()) return
        viewModelScope.launch {
            val issues = validateCurrentTasks()
            if (issues.isNotEmpty()) {
                _validationIssues.value = issues
                _saveCompletedEvent.value = false
                _saveSummaryMessage.value = null
                syncPlanningSummary(issues)
                syncSuggestions(issues)
                return@launch
            }

            _validationIssues.value = emptyList()
            val dateStr = _selectedDate.value.toString()
            val existingTasks = taskDao.getTasksForDateOnce(dateStr)
            val retainedDbIds = _tasks.value.mapNotNull { it.dbId }.toSet()

            existingTasks
                .filter { it.id !in retainedDbIds }
                .filterNot { it.isCompleted || it.isSkipped }
                .forEach { removedTask ->
                    taskDao.deleteTaskById(removedTask.id)
                    taskEventDao.insert(
                        TaskEvent(
                            taskId = removedTask.id,
                            taskName = removedTask.name,
                            date = removedTask.date,
                            eventType = "DELETED",
                            timestamp = System.currentTimeMillis(),
                            oldStartTime = removedTask.startTime,
                            oldEndTime = removedTask.endTime,
                            reason = "MISSION_ARCHITECT_DELETE"
                        )
                    )
                }

            _tasks.value.forEach { state ->
                val startTime = state.startTime.ifEmpty { "08:00" }
                val endTime = state.endTime.ifEmpty { "09:00" }
                val difficultyMultiplier = when (state.type) {
                    MissionType.DEEP_WORK -> 1.5f
                    MissionType.APP_INVEST -> 1.2f
                    MissionType.PHYSICAL -> 1.0f
                    MissionType.PREP -> 1.25f
                }

                if (state.dbId != null) {
                    val existingTask = taskDao.getTaskById(state.dbId) ?: return@forEach
                    val updatedTask = existingTask.copy(
                        name = state.name,
                        startTime = startTime,
                        endTime = endTime,
                        date = dateStr,
                        importanceRank = state.importanceRank,
                        taskType = state.focusTaskType.name,
                        focusModeEnabled = state.focusModeEnabled,
                        focusPreset = state.focusPreset.name,
                        packageName = state.packageName,
                        parentMissionId = state.parentMissionId,
                        segmentBaseName = state.segmentBaseName,
                        segmentIndex = state.segmentIndex,
                        segmentCount = state.segmentCount,
                        isBreakSegment = state.isBreakSegment,
                        difficultyMultiplier = difficultyMultiplier,
                        lastModified = System.currentTimeMillis(),
                        syncStatus = "PENDING"
                    )
                    taskDao.updateTask(updatedTask)

                    if (hasMeaningfulTaskEdit(existingTask, updatedTask)) {
                        taskEventDao.insert(
                            TaskEvent(
                                taskId = updatedTask.id,
                                taskName = updatedTask.name,
                                date = updatedTask.date,
                                eventType = "EDITED",
                                timestamp = System.currentTimeMillis(),
                                oldStartTime = existingTask.startTime,
                                oldEndTime = existingTask.endTime,
                                newStartTime = updatedTask.startTime,
                                newEndTime = updatedTask.endTime,
                                reason = "MISSION_ARCHITECT_SAVE"
                            )
                        )
                    }
                } else {
                    val newTask = Task(
                        name = state.name,
                        startTime = startTime,
                        endTime = endTime,
                        date = dateStr,
                        isMissionSix = true,
                        importanceRank = state.importanceRank,
                        taskType = state.focusTaskType.name,
                        focusModeEnabled = state.focusModeEnabled,
                        focusPreset = state.focusPreset.name,
                        packageName = state.packageName,
                        parentMissionId = state.parentMissionId,
                        segmentBaseName = state.segmentBaseName,
                        segmentIndex = state.segmentIndex,
                        segmentCount = state.segmentCount,
                        isBreakSegment = state.isBreakSegment,
                        difficultyMultiplier = difficultyMultiplier,
                        origin = "MISSION_ARCHITECT",
                        lastModified = System.currentTimeMillis(),
                        syncStatus = "PENDING"
                    )
                    val taskId = taskDao.insertTask(newTask).toInt()
                    taskEventDao.insert(
                        TaskEvent(
                            taskId = taskId,
                            taskName = newTask.name,
                            date = newTask.date,
                            eventType = "CREATED",
                            timestamp = System.currentTimeMillis(),
                            newStartTime = newTask.startTime,
                            newEndTime = newTask.endTime,
                            reason = "MISSION_ARCHITECT_SAVE"
                        )
                    )
                }
            }
            lastCommittedSnapshot = _tasks.value.toComparableSnapshot()
            _hasUnsavedChanges.value = false
            _saveSummaryMessage.value = buildSaveSummaryMessage()
            syncPlanningSummary()
            syncSuggestions()
            _saveCompletedEvent.value = true
        }
    }

    fun clearSaveCompletedEvent() {
        _saveCompletedEvent.value = false
    }

    fun clearSaveSummaryMessage() {
        _saveSummaryMessage.value = null
    }

    fun applyBlueprint(blueprintId: String, append: Boolean = false) {
        nightDecisionLockMessage()?.let {
            _saveSummaryMessage.value = it
            return
        }
        if (isPastDate()) return
        val blueprint = (recommendedBlueprints.value + savedBlueprints.value).firstOrNull { it.id == blueprintId } ?: return
        val preparedMissions = blueprint.missions.map { mission ->
            mission.copy(
                id = System.currentTimeMillis().toInt() + mission.name.hashCode() + mission.importanceRank
            )
        }
        _tasks.value = if (append) (_tasks.value + preparedMissions).take(6) else preparedMissions
        _validationIssues.value = emptyList()
        syncUnsavedChanges()
        syncPlanningSummary()
        syncSuggestions()
    }

    fun getBlueprintById(blueprintId: String): TaskBlueprint? {
        return (recommendedBlueprints.value + savedBlueprints.value).firstOrNull { it.id == blueprintId }
    }

    fun saveCurrentAsBlueprint(title: String) {
        if (isPastDate() || _tasks.value.isEmpty()) return

        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return

        viewModelScope.launch {
            val createdAt = System.currentTimeMillis()
            val missions = _tasks.value.map {
                it.copy(
                    id = 0,
                    dbId = null,
                    isCompleted = false,
                    isSkipped = false
                )
            }
            val summary = buildBlueprintSummary(missions)
            val existingWithSameTitle = savedTaskBlueprintDao.getAllWithMissions()
                .firstOrNull { it.blueprint.title.equals(cleanTitle, ignoreCase = true) }

            val blueprintId = existingWithSameTitle?.blueprint?.id ?: "saved_$createdAt"
            savedTaskBlueprintDao.upsertBlueprintWithMissions(
                blueprint = SavedTaskBlueprint(
                    id = blueprintId,
                    title = cleanTitle,
                    summary = summary,
                    createdAt = existingWithSameTitle?.blueprint?.createdAt ?: createdAt,
                    lastModified = createdAt
                ),
                missions = missions.mapIndexed { index, mission ->
                    SavedTaskBlueprintMission(
                        blueprintId = blueprintId,
                        missionOrder = index,
                        name = mission.name,
                        startTime = mission.startTime,
                        endTime = mission.endTime,
                        importanceRank = mission.importanceRank,
                        type = mission.type.name,
                        taskType = mission.focusTaskType.name,
                        focusModeEnabled = mission.focusModeEnabled,
                        focusPreset = mission.focusPreset.name,
                        parentMissionId = mission.parentMissionId,
                        segmentBaseName = mission.segmentBaseName,
                        segmentIndex = mission.segmentIndex,
                        segmentCount = mission.segmentCount,
                        isBreakSegment = mission.isBreakSegment,
                        packageName = mission.packageName
                    )
                }
            )
            trimSavedBlueprints(limit = 8)
            loadSavedBlueprints()
            _saveSummaryMessage.value = "Blueprint saved as $cleanTitle."
        }
    }

    fun deleteSavedBlueprint(blueprintId: String) {
        viewModelScope.launch {
            savedTaskBlueprintDao.deleteBlueprintById(blueprintId)
            loadSavedBlueprints()
        }
    }

    fun cloneFromPreviousDay() {
        viewModelScope.launch {
            val yesterday = _selectedDate.value.minusDays(1)
            val previousTasks = taskDao.getTasksForDateOnce(yesterday.toString())
            if (previousTasks.isNotEmpty()) {
                _tasks.value = previousTasks.map { 
                    TaskBuilderState(
                        name = it.name, 
                        startTime = it.startTime, 
                        endTime = it.endTime, 
                        importanceRank = it.importanceRank,
                        focusTaskType = parseFocusTaskType(it.taskType),
                        focusModeEnabled = it.focusModeEnabled,
                        focusPreset = parseFocusPreset(it.focusPreset),
                        parentMissionId = it.parentMissionId,
                        segmentBaseName = it.segmentBaseName,
                        segmentIndex = it.segmentIndex,
                        segmentCount = it.segmentCount,
                        isBreakSegment = it.isBreakSegment,
                        packageName = it.packageName,
                        type = when {
                            isPrepTaskType(parseFocusTaskType(it.taskType)) -> MissionType.PREP
                            it.packageName != null -> MissionType.APP_INVEST
                            it.difficultyMultiplier > 1.2f -> MissionType.DEEP_WORK
                            else -> MissionType.PHYSICAL
                        }
                    )
                }
                _validationIssues.value = emptyList()
                syncUnsavedChanges()
                syncPlanningSummary()
                syncSuggestions()
            }
        }
    }

    private fun syncUnsavedChanges() {
        _hasUnsavedChanges.value = _tasks.value.toComparableSnapshot() != lastCommittedSnapshot
    }

    private fun syncPlanningSummary(
        issues: List<TaskBuilderValidationIssue> = _validationIssues.value
    ) {
        val normalized = _tasks.value.mapNotNull { task ->
            val start = parseTime(task.startTime)
            val end = parseTime(task.endTime)
            if (task.name.isBlank() || start == null || end == null || !end.isAfter(start)) {
                null
            } else {
                Triple(task, start, end)
            }
        }

        val totalMinutes = normalized.sumOf { (_, start, end) ->
            java.time.Duration.between(start, end).toMinutes().toInt()
        }
        val longestMissionMinutes = normalized.maxOfOrNull { (_, start, end) ->
            java.time.Duration.between(start, end).toMinutes().toInt()
        } ?: 0
        val conflictCount = issues.count { it.message.contains("overlaps", ignoreCase = true) }

        _planningSummary.value = TaskBuilderPlanningSummary(
            missionCount = _tasks.value.size,
            totalPlannedMinutes = totalMinutes,
            criticalCount = _tasks.value.count { it.importanceRank == 1 },
            deepWorkCount = _tasks.value.count { it.type == MissionType.DEEP_WORK },
            appInvestCount = _tasks.value.count { it.type == MissionType.APP_INVEST },
            physicalCount = _tasks.value.count { it.type == MissionType.PHYSICAL },
            prepCount = _tasks.value.count { it.type == MissionType.PREP },
            conflictCount = conflictCount,
            longestMissionMinutes = longestMissionMinutes
        )
    }

    private fun syncSuggestions(
        issues: List<TaskBuilderValidationIssue> = _validationIssues.value
    ) {
        val summary = _planningSummary.value
        val suggestions = mutableListOf<TaskBuilderSuggestion>()

        if (summary.missionCount == 0) {
            suggestions += TaskBuilderSuggestion(
                title = "Start with one decisive block",
                detail = "Lock one high-value mission first, then build the rest of the day around it."
            )
        }
        if (summary.conflictCount > 0) {
            suggestions += TaskBuilderSuggestion(
                title = "Resolve collisions first",
                detail = "Overlapping missions destroy trust in the board. Separate them before saving.",
                severity = SuggestionSeverity.STRONG
            )
        }
        if (summary.criticalCount >= 4) {
            suggestions += TaskBuilderSuggestion(
                title = "Critical overload detected",
                detail = "Drop at least one critical mission to VITAL or move it to another day.",
                severity = SuggestionSeverity.STRONG
            )
        }
        if (summary.deepWorkCount == 0 && summary.missionCount > 1) {
            suggestions += TaskBuilderSuggestion(
                title = "No deep work block yet",
                detail = "Add one protected focus mission or the day may dissolve into shallow motion.",
                severity = SuggestionSeverity.CAUTION
            )
        }
        if (summary.physicalCount == 0 && summary.totalPlannedMinutes >= 6 * 60) {
            suggestions += TaskBuilderSuggestion(
                title = "Recovery is missing",
                detail = "A long load day with no physical or recovery block can collapse late.",
                severity = SuggestionSeverity.CAUTION
            )
        }
        if (summary.prepCount > 0 && summary.deepWorkCount == 0 && summary.missionCount <= 2) {
            suggestions += TaskBuilderSuggestion(
                title = "Prep needs execution beside it",
                detail = "Keep at least one real build or study block next to interview prep so the board still creates proof.",
                severity = SuggestionSeverity.CAUTION
            )
        }
        if (summary.longestMissionMinutes >= 120) {
            suggestions += TaskBuilderSuggestion(
                title = "One mission may be too long",
                detail = "Try splitting the longest block into two parts unless it truly needs a long runway.",
                severity = SuggestionSeverity.CAUTION
            )
        }
        if (summary.appInvestCount >= 3 && summary.deepWorkCount == 0) {
            suggestions += TaskBuilderSuggestion(
                title = "Tool-heavy, outcome-light",
                detail = "You have several app-invest blocks. Make sure at least one mission is outcome-driven deep work.",
                severity = SuggestionSeverity.CAUTION
            )
        }
        if (issues.isEmpty() && summary.missionCount in 3..5 && summary.criticalCount <= 2 && summary.deepWorkCount >= 1) {
            suggestions += TaskBuilderSuggestion(
                title = "This day looks executable",
                detail = "The load is controlled enough to push hard without turning the board into fiction."
            )
        }

        _suggestions.value = suggestions.distinctBy { it.title }
        _recommendedBlueprints.value = buildRecommendedBlueprints(summary)
    }

    private suspend fun loadSavedBlueprints() {
        _savedBlueprints.value = savedTaskBlueprintDao.getAllWithMissions().map { record ->
            TaskBlueprint(
                id = record.blueprint.id,
                title = record.blueprint.title,
                summary = record.blueprint.summary,
                missions = record.missions.sortedBy { it.missionOrder }.map { mission ->
                    TaskBuilderState(
                        name = mission.name,
                        startTime = mission.startTime,
                        endTime = mission.endTime,
                        importanceRank = mission.importanceRank,
                        type = runCatching { MissionType.valueOf(mission.type) }.getOrDefault(MissionType.DEEP_WORK),
                        focusTaskType = parseFocusTaskType(mission.taskType),
                        focusModeEnabled = mission.focusModeEnabled,
                        focusPreset = parseFocusPreset(mission.focusPreset),
                        parentMissionId = mission.parentMissionId,
                        segmentBaseName = mission.segmentBaseName,
                        segmentIndex = mission.segmentIndex,
                        segmentCount = mission.segmentCount,
                        isBreakSegment = mission.isBreakSegment,
                        packageName = mission.packageName
                    )
                }
            )
        }
    }

    private suspend fun migrateLegacySavedBlueprintsIfNeeded() {
        val legacyRecords = prefManager.loadSavedTaskBlueprints()
        if (legacyRecords.isEmpty()) return

        val existingIds = savedTaskBlueprintDao.getAllIds().toSet()
        val missingRecords = legacyRecords.filterNot { it.id in existingIds }

        missingRecords.forEach { record ->
            savedTaskBlueprintDao.upsertBlueprintWithMissions(
                blueprint = SavedTaskBlueprint(
                    id = record.id,
                    title = record.title,
                    summary = record.summary,
                    createdAt = record.createdAt,
                    lastModified = record.createdAt
                ),
                missions = record.missions.mapIndexed { index, mission ->
                    SavedTaskBlueprintMission(
                        blueprintId = record.id,
                        missionOrder = index,
                        name = mission.name,
                        startTime = mission.startTime,
                        endTime = mission.endTime,
                        importanceRank = mission.importanceRank,
                        type = mission.type,
                        taskType = mission.taskType,
                        focusModeEnabled = mission.focusModeEnabled,
                        focusPreset = mission.focusPreset,
                        parentMissionId = mission.parentMissionId,
                        segmentBaseName = mission.segmentBaseName,
                        segmentIndex = mission.segmentIndex,
                        segmentCount = mission.segmentCount,
                        isBreakSegment = mission.isBreakSegment,
                        packageName = mission.packageName
                    )
                }
            )
        }

        trimSavedBlueprints(limit = 8)

        val importedIds = savedTaskBlueprintDao.getAllIds().toSet()
        if (legacyRecords.all { it.id in importedIds }) {
            prefManager.saveTaskBlueprints(emptyList())
        }
    }

    private suspend fun trimSavedBlueprints(limit: Int) {
        val ids = savedTaskBlueprintDao.getBlueprintIdsNewestFirst()
        val overflowIds = ids.drop(limit)
        if (overflowIds.isNotEmpty()) {
            savedTaskBlueprintDao.deleteBlueprintsByIds(overflowIds)
        }
    }

    private fun buildRecommendedBlueprints(summary: TaskBuilderPlanningSummary): List<TaskBlueprint> {
        val blueprints = mutableListOf<TaskBlueprint>()

        if (summary.missionCount == 0) {
            blueprints += TaskBlueprint(
                id = "focused_trinity",
                title = "Focused Trinity",
                summary = "One deep block, one app-invest block, one physical reset.",
                missions = listOf(
                    TaskBuilderState(name = "Deep Work", startTime = "08:00", endTime = "09:30", importanceRank = 1, type = MissionType.DEEP_WORK, focusTaskType = FocusTaskType.STUDY, focusModeEnabled = true),
                    TaskBuilderState(name = "App Invest", startTime = "10:00", endTime = "10:45", importanceRank = 2, type = MissionType.APP_INVEST, focusTaskType = FocusTaskType.ADMIN),
                    TaskBuilderState(name = "Physical Reset", startTime = "18:00", endTime = "18:45", importanceRank = 3, type = MissionType.PHYSICAL, focusTaskType = FocusTaskType.PHYSICAL)
                )
            )
            blueprints += TaskBlueprint(
                id = "monk_morning",
                title = "Monk Morning",
                summary = "Two focused morning blocks with a physical anchor.",
                missions = listOf(
                    TaskBuilderState(name = "Deep Work", startTime = "06:30", endTime = "08:00", importanceRank = 1, type = MissionType.DEEP_WORK, focusTaskType = FocusTaskType.CODE, focusModeEnabled = true),
                    TaskBuilderState(name = "Study / Writing", startTime = "08:15", endTime = "09:15", importanceRank = 2, type = MissionType.DEEP_WORK, focusTaskType = FocusTaskType.READ, focusModeEnabled = true),
                    TaskBuilderState(name = "Training", startTime = "18:30", endTime = "19:15", importanceRank = 3, type = MissionType.PHYSICAL, focusTaskType = FocusTaskType.PHYSICAL)
                )
            )
            blueprints += TaskBlueprint(
                id = "job_hunt_prep",
                title = "Job Hunt Prep",
                summary = "One build block, one interview prep rep, one physical reset.",
                missions = listOf(
                    TaskBuilderState(name = "Project Build", startTime = "08:00", endTime = "09:30", importanceRank = 1, type = MissionType.DEEP_WORK, focusTaskType = FocusTaskType.CODE, focusModeEnabled = true),
                    TaskBuilderState(name = "Behavioral Interview Rep", startTime = "10:00", endTime = "10:30", importanceRank = 2, type = MissionType.PREP, focusTaskType = FocusTaskType.BEHAVIORAL),
                    TaskBuilderState(name = "Walk / Reset", startTime = "18:00", endTime = "18:30", importanceRank = 3, type = MissionType.PHYSICAL, focusTaskType = FocusTaskType.PHYSICAL)
                )
            )
        } else if (summary.deepWorkCount == 0) {
            blueprints += TaskBlueprint(
                id = "add_focus_anchor",
                title = "Add Focus Anchor",
                summary = "A clean deep-work block to stop the day drifting into admin.",
                missions = listOf(
                    TaskBuilderState(name = "Deep Work", startTime = "08:00", endTime = "09:00", importanceRank = 1, type = MissionType.DEEP_WORK, focusTaskType = FocusTaskType.CODE, focusModeEnabled = true)
                )
            )
        } else if (summary.physicalCount == 0 && summary.totalPlannedMinutes >= 6 * 60) {
            blueprints += TaskBlueprint(
                id = "recovery_patch",
                title = "Recovery Patch",
                summary = "Add a short physical reset to protect late-day execution.",
                missions = listOf(
                    TaskBuilderState(name = "Walk / Mobility", startTime = "17:30", endTime = "18:00", importanceRank = 3, type = MissionType.PHYSICAL, focusTaskType = FocusTaskType.PHYSICAL)
                )
            )
        }

        return blueprints
    }

    private fun buildBlueprintSummary(missions: List<TaskBuilderState>): String {
        val critical = missions.count { it.importanceRank == 1 }
        val deep = missions.count { it.type == MissionType.DEEP_WORK }
        val prep = missions.count { it.type == MissionType.PREP }
        val physical = missions.count { it.type == MissionType.PHYSICAL }
        return buildString {
            append("${missions.size} missions")
            if (critical > 0) append(" · $critical critical")
            if (deep > 0) append(" · $deep deep")
            if (prep > 0) append(" · $prep prep")
            if (physical > 0) append(" · $physical physical")
        }
    }

    private fun buildSaveSummaryMessage(): String {
        val summary = _planningSummary.value
        val hours = summary.totalPlannedMinutes / 60
        val minutes = summary.totalPlannedMinutes % 60
        val timeText = buildString {
            if (hours > 0) append("${hours}h")
            if (minutes > 0) {
                if (isNotEmpty()) append(" ")
                append("${minutes}m")
            }
            if (isEmpty()) append("0m")
        }
        return "Blueprint saved: ${summary.missionCount} missions, $timeText planned."
    }

    private fun validateCurrentTasks(): List<TaskBuilderValidationIssue> {
        val issues = mutableListOf<TaskBuilderValidationIssue>()
        val normalizedTasks = _tasks.value.mapIndexedNotNull { index, task ->
            if (task.name.isBlank()) {
                issues += TaskBuilderValidationIssue("Mission ${index + 1} is missing a name.")
                return@mapIndexedNotNull null
            }

            val start = parseTime(task.startTime)
            val end = parseTime(task.endTime)
            if (start == null || end == null) {
                issues += TaskBuilderValidationIssue("${task.name} has an invalid time.")
                return@mapIndexedNotNull null
            }
            if (!end.isAfter(start)) {
                issues += TaskBuilderValidationIssue("${task.name} ends before it starts.")
                return@mapIndexedNotNull null
            }
            Triple(task, start, end)
        }.sortedBy { it.second }

        for (i in normalizedTasks.indices) {
            val current = normalizedTasks[i]
            for (j in i + 1 until normalizedTasks.size) {
                val next = normalizedTasks[j]
                if (!next.second.isBefore(current.third)) break
                issues += TaskBuilderValidationIssue(
                    "${current.first.name} overlaps with ${next.first.name}."
                )
            }
        }

        val sleepLockWindow = resolveSleepLockWindow(prefManager)
        val sleepLockStart = sleepLockWindow.start
        val sleepLockEnd = sleepLockWindow.end

        val sleepLockStartLabel = sleepLockStart.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
        val sleepLockEndLabel = sleepLockEnd.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
        normalizedTasks.forEach { (task, start, end) ->
            if (intersectsSleepLockWindow(start, end, sleepLockWindow)) {
                issues += TaskBuilderValidationIssue(
                    "${task.name} enters the Sleep Lock window from $sleepLockStartLabel to $sleepLockEndLabel."
                )
            }
        }

        val criticalCount = _tasks.value.count { it.importanceRank == 1 }
        if (criticalCount > 3) {
            issues += TaskBuilderValidationIssue("You have $criticalCount critical missions. That load is probably unrealistic.")
        }

        syncPlanningSummary(issues)
        return issues.distinctBy { it.message }
    }

    private fun parseTime(value: String): LocalTime? {
        val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
        for (pattern in formats) {
            runCatching {
                return LocalTime.parse(value.uppercase(), DateTimeFormatter.ofPattern(pattern, Locale.US))
            }
        }
        return null
    }

    private fun buildMissionChunks(tasks: List<TaskBuilderState>): List<List<TaskBuilderState>> {
        if (tasks.isEmpty()) return emptyList()

        val chunks = mutableListOf<MutableList<TaskBuilderState>>()
        tasks.forEach { task ->
            val key = task.parentMissionId.takeIf { it.isNotBlank() } ?: "single_${task.id}"
            val lastChunk = chunks.lastOrNull()
            val lastKey = lastChunk?.firstOrNull()?.let { previous ->
                previous.parentMissionId.takeIf { it.isNotBlank() } ?: "single_${previous.id}"
            }

            if (lastChunk != null && lastKey == key) {
                lastChunk += task
            } else {
                chunks += mutableListOf(task)
            }
        }

        return chunks
    }
}

private fun hasMeaningfulTaskEdit(oldTask: Task, newTask: Task): Boolean {
    return oldTask.name != newTask.name ||
        oldTask.startTime != newTask.startTime ||
        oldTask.endTime != newTask.endTime ||
        oldTask.importanceRank != newTask.importanceRank ||
        oldTask.taskType != newTask.taskType ||
        oldTask.focusModeEnabled != newTask.focusModeEnabled ||
        oldTask.focusPreset != newTask.focusPreset ||
        oldTask.parentMissionId != newTask.parentMissionId ||
        oldTask.segmentBaseName != newTask.segmentBaseName ||
        oldTask.segmentIndex != newTask.segmentIndex ||
        oldTask.segmentCount != newTask.segmentCount ||
        oldTask.isBreakSegment != newTask.isBreakSegment ||
        oldTask.packageName != newTask.packageName ||
        oldTask.difficultyMultiplier != newTask.difficultyMultiplier
}

private fun List<TaskBuilderState>.toComparableSnapshot(): List<TaskBuilderComparableState> {
    return map { task ->
        TaskBuilderComparableState(
            dbId = task.dbId,
            name = task.name.trim(),
            startTime = task.startTime.trim(),
            endTime = task.endTime.trim(),
            importanceRank = task.importanceRank,
            type = task.type,
            focusTaskType = task.focusTaskType,
            focusModeEnabled = task.focusModeEnabled,
            focusPreset = task.focusPreset,
            parentMissionId = task.parentMissionId,
            segmentBaseName = task.segmentBaseName,
            segmentIndex = task.segmentIndex,
            segmentCount = task.segmentCount,
            isBreakSegment = task.isBreakSegment,
            packageName = task.packageName
        )
    }
}
