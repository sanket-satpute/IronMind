@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.sanket_satpute_20.ironmind.onboarding

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.sanket_satpute_20.ironmind.gamification.gamifiedClick
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionActivity
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionScheduler
import com.sanket_satpute_20.ironmind.ui.components.AppIconImage
import com.sanket_satpute_20.ironmind.ui.components.HorizontalCalendar
import com.sanket_satpute_20.ironmind.ui.components.OrbitalTimePicker
import com.sanket_satpute_20.ironmind.utils.TimeUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale /* --- Redesigned Data Models for Templates --- */
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

data class MissionTemplate(val name: String, val icon: ImageVector, val type: MissionType)

private data class OccupiedMissionWindow(
    val taskId: Int,
    val name: String,
    val start: LocalTime,
    val end: LocalTime
)

private data class OpenSlotSuggestion(val label: String, val start: LocalTime, val end: LocalTime)

private data class ProtectedTimeRange(val start: LocalTime, val end: LocalTime, val label: String)

private data class RecoveryGapSignal(
    val title: String,
    val detail: String,
    val suggestedStart: LocalTime? = null,
    val suggestedEnd: LocalTime? = null
)

private data class TimeResolutionSuggestion(
    val title: String,
    val detail: String,
    val start: LocalTime,
    val end: LocalTime
)

private data class BuilderMissionDisplayItem(
    val key: String,
    val representative: TaskBuilderState,
    val members: List<TaskBuilderState>
) {
    val isChain: Boolean = members.size > 1 && representative.parentMissionId.isNotBlank()
}

private enum class BuilderPlanningMode {
    QUICK,
    ADVANCED
}

private enum class BlueprintLibraryTab {
    QUICK,
    SAVED
}

private enum class SplitSpacingMode {
    RECOVERY_GAP,
    AFTER_DELAY
}

private data class SplitChainDefaults(
    val enabled: Boolean = false,
    val partCount: Int = 2,
    val gapMinutes: Int = 15,
    val spacingMode: SplitSpacingMode = SplitSpacingMode.RECOVERY_GAP
)

val DEEP_WORK_TEMPLATES =
    listOf(
        MissionTemplate("Coding", Icons.Rounded.Terminal, MissionType.DEEP_WORK),
        MissionTemplate("Study", Icons.AutoMirrored.Rounded.MenuBook, MissionType.DEEP_WORK),
        MissionTemplate("Office Work", Icons.Rounded.Work, MissionType.DEEP_WORK),
        MissionTemplate("Reading", Icons.Rounded.AutoStories, MissionType.DEEP_WORK),
        MissionTemplate("Design", Icons.Rounded.Palette, MissionType.DEEP_WORK),
        MissionTemplate("Writing", Icons.Rounded.EditNote, MissionType.DEEP_WORK)
    )
val PHYSICAL_TEMPLATES =
    listOf(
        MissionTemplate("Gym", Icons.Rounded.FitnessCenter, MissionType.PHYSICAL),
        MissionTemplate("Yoga", Icons.Rounded.SelfImprovement, MissionType.PHYSICAL),
        MissionTemplate("Running", Icons.AutoMirrored.Rounded.DirectionsRun, MissionType.PHYSICAL),
        MissionTemplate("Meditation", Icons.Rounded.Spa, MissionType.PHYSICAL),
        MissionTemplate("Cycling", Icons.AutoMirrored.Rounded.DirectionsBike, MissionType.PHYSICAL),
        MissionTemplate("Stretching", Icons.Rounded.AccessibilityNew, MissionType.PHYSICAL)
    )
val PREP_TEMPLATES =
    listOf(
        MissionTemplate("DSA Drill", Icons.Rounded.Code, MissionType.PREP),
        MissionTemplate("System Design Sprint", Icons.Rounded.AccountTree, MissionType.PREP),
        MissionTemplate("Behavioral Rep", Icons.Rounded.RecordVoiceOver, MissionType.PREP),
        MissionTemplate("Project Review", Icons.Rounded.Inventory2, MissionType.PREP)
    )

@Composable
fun TaskBuilderScreen(
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    initialDate: LocalDate? = null,
    initialMissionType: MissionType? = null,
    initialFocusTaskType: FocusTaskType? = null
) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        TaskBuilderPreviewContent()
        return
    }

    val context = LocalContext.current
    val haptic = com.sanket_satpute_20.ironmind.gamification.LocalHapticsManager.current
    val db = IronMindDatabase.getDatabase(context)
    val prefManager = remember(context) { PrefManager.getInstance(context) }
    val viewModel: TaskBuilderViewModel =
        viewModel(
            factory =
                TaskBuilderViewModelFactory(
                    db.taskDao(),
                    db.taskEventDao(),
                    db.savedTaskBlueprintDao(),
                    prefManager
                )
        )
    val tasks by viewModel.tasks.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val validationIssues by viewModel.validationIssues.collectAsState()
    val saveCompleted by viewModel.saveCompletedEvent.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    val planningSummary by viewModel.planningSummary.collectAsState()
    val saveSummaryMessage by viewModel.saveSummaryMessage.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val recommendedBlueprints by viewModel.recommendedBlueprints.collectAsState()
    val savedBlueprints by viewModel.savedBlueprints.collectAsState()
    val isPast = viewModel.isPastDate()
    val nightDecisionLockMessage =
        remember(selectedDate, saveSummaryMessage) { viewModel.nightDecisionLockMessage() }
    val isNightDecisionLocked = nightDecisionLockMessage != null
    val displayTasks = remember(tasks) { buildBuilderDisplayItems(tasks) }
    val nightDecisionManager = remember(context) { NightDecisionManager(context) }
    var planningMode by rememberSaveable { mutableStateOf(BuilderPlanningMode.QUICK) }
    var showQuickPlanTools by rememberSaveable { mutableStateOf(false) }
    var blueprintTab by rememberSaveable { mutableStateOf(BlueprintLibraryTab.QUICK) }
    var showFactoryDialog by remember { mutableStateOf(false) }
    var consumeInitialFactory by
        rememberSaveable(initialDate, initialMissionType, initialFocusTaskType) {
            mutableStateOf(initialMissionType != null)
        }
    var editingTask by remember { mutableStateOf<TaskBuilderState?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveBlueprintDialog by remember { mutableStateOf(false) }
    var showClearDayDialog by remember { mutableStateOf(false) }
    var pendingBlueprint by remember { mutableStateOf<TaskBlueprint?>(null) }
    var appendOverflowBlueprint by remember { mutableStateOf<TaskBlueprint?>(null) }
    var appendOverflowSlots by remember { mutableIntStateOf(0) }
    LaunchedEffect(initialDate) { initialDate?.let { viewModel.selectDate(it) } }
    LaunchedEffect(consumeInitialFactory, isNightDecisionLocked, isPast) {
        if (consumeInitialFactory && !isNightDecisionLocked && !isPast) {
            showFactoryDialog = true
            consumeInitialFactory = false
        }
    }
    LaunchedEffect(saveCompleted) {
        if (saveCompleted) {
            val shouldCompleteNightDecision =
                prefManager.nightDecisionActive &&
                    prefManager.nightDecisionDate == selectedDate.toString()
            if (shouldCompleteNightDecision) {
                nightDecisionManager.markPlanned()
                NightDecisionScheduler.schedule(context)
                context.sendBroadcast(NightDecisionActivity.finishIntent(context))
            }
            viewModel.clearSaveCompletedEvent()
            onNext()
        }
    }
    LaunchedEffect(saveSummaryMessage) {
        saveSummaryMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveSummaryMessage()
        }
    }
    fun attemptExit() {

        if (!isPast && hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onNext()
        }
    }
    BackHandler { attemptExit() }
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(modifier = Modifier.background(Color.Black)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "MISSION ARCHITECT",
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { attemptExit() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (!isPast) {

                            if (tasks.isNotEmpty()) {
                                IconButton(onClick = { showClearDayDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteSweep,
                                        contentDescription = "Clear day",
                                        tint = Color.Gray
                                    )
                                }
                            }
                            TextButton(
                                onClick = { 
                                    haptic.playSuccess()
                                    viewModel.attemptSave() 
                                },
                                enabled = !isNightDecisionLocked
                            ) {
                                Text(
                                    "SAVE",
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                )
                HorizontalCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        floatingActionButton = {
            if (!isPast) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (tasks.isNotEmpty()) {
                        SmallFloatingActionButton(
                            onClick = {
                                if (!isNightDecisionLocked) {
                                    showSaveBlueprintDialog = true
                                }
                            },
                            containerColor = SurfaceElevated,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Rounded.BookmarkAdd, contentDescription = "Save Blueprint")
                        }
                    }
                    if (tasks.size < 6) {
                        FloatingActionButton(
                            onClick = {
                                if (!isNightDecisionLocked) {
                                    showFactoryDialog = true
                                }
                            },
                            containerColor = SuccessGreen,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "New Mission")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val dateLabel =
                        if (selectedDate == LocalDate.now()) "TODAY"
                        else selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
                    Text(
                        text = dateLabel.uppercase(),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                        color = ErrorRed,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text =
                            if (isPast) "Reviewing historical systems."
                            else "Architect your day for maximum output.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
                item {
                    BuilderPlanningModeRow(
                        mode = planningMode,
                        onModeSelected = { selectedMode ->
                            planningMode = selectedMode
                            if (selectedMode == BuilderPlanningMode.ADVANCED) {
                                showQuickPlanTools = true
                            }
                        }
                    )
                }
                item {
                    PlanningSummaryCard(
                        summary = planningSummary,
                        validationIssues = validationIssues
                    )
                }
                if (nightDecisionLockMessage != null) {
                    item { NightDecisionLockCard(message = nightDecisionLockMessage) }
                }
                if (!isPast && planningMode == BuilderPlanningMode.QUICK && !showQuickPlanTools) {
                    item {
                        QuickPlannerToolsCard(
                            suggestionsCount = suggestions.size,
                            recommendedBlueprintCount = recommendedBlueprints.size,
                            savedBlueprintCount = savedBlueprints.size,
                            hasCloneSource = tasks.isEmpty(),
                            onOpen = { showQuickPlanTools = true }
                        )
                    }
                }
                if (
                    !isPast &&
                        suggestions.isNotEmpty() &&
                        (planningMode == BuilderPlanningMode.ADVANCED || showQuickPlanTools)
                ) {
                    item {
                        SuggestionDeck(
                            suggestions =
                                if (planningMode == BuilderPlanningMode.QUICK) suggestions.take(2)
                                else suggestions
                        )
                    }
                }
                if (
                    !isPast &&
                        (recommendedBlueprints.isNotEmpty() || savedBlueprints.isNotEmpty()) &&
                        (planningMode == BuilderPlanningMode.ADVANCED || showQuickPlanTools)
                ) {
                    item {
                        BlueprintLibrarySection(
                            selectedTab = blueprintTab,
                            onTabSelected = { blueprintTab = it },
                            recommendedBlueprints = recommendedBlueprints,
                            savedBlueprints = savedBlueprints,
                            onApply = { blueprintId ->
                                val blueprint =
                                    (recommendedBlueprints + savedBlueprints).firstOrNull {
                                        it.id == blueprintId
                                    }
                                if (tasks.isNotEmpty() && blueprint != null)
                                    pendingBlueprint = blueprint
                                else viewModel.applyBlueprint(blueprintId)
                            },
                            onDelete = { blueprintId ->
                                viewModel.deleteSavedBlueprint(blueprintId)
                            }
                        )
                    }
                }
                if (validationIssues.isNotEmpty()) {
                    item { ValidationAlertCard(issues = validationIssues) }
                }
                if (
                    tasks.isEmpty() &&
                        !isPast &&
                        (planningMode == BuilderPlanningMode.ADVANCED || showQuickPlanTools)
                ) {
                    item {
                        Button(
                            onClick = { viewModel.cloneFromPreviousDay() },
                            enabled = !isNightDecisionLocked,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "CLONE YESTERDAY'S BLUEPRINT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                itemsIndexed(displayTasks, key = { _, item -> item.key }) { index, displayItem ->
                    val taskState = displayItem.representative
                    val canRemove =
                        if (displayItem.isChain) {
                            displayItem.members.none { it.isCompleted || it.isSkipped }
                        } else {
                            !taskState.isCompleted && !taskState.isSkipped
                        }
                    MissionDossierCard(
                        task = taskState,
                        isFirst = index == 0,
                        isLast = index == displayTasks.size - 1,
                        isReadOnly = isPast,
                        chainBadge =
                            if (displayItem.isChain) {
                                "${displayItem.members.count { !it.isBreakSegment }}-PART CHAIN"
                            } else null,
                        titleOverride =
                            if (displayItem.isChain) {
                                taskState.segmentBaseName.ifBlank {
                                    taskState.name.substringBefore(" · ")
                                }
                            } else null,
                        timeOverride =
                            if (displayItem.isChain) {
                                val start =
                                    displayItem.members
                                        .minByOrNull {
                                            parseBuilderTimeOrNull(it.startTime) ?: LocalTime.MAX
                                        }
                                        ?.startTime
                                val end =
                                    displayItem.members
                                        .maxByOrNull {
                                            parseBuilderTimeOrNull(it.endTime) ?: LocalTime.MIN
                                        }
                                        ?.endTime
                                if (start != null && end != null) "$start - $end" else null
                            } else null,
                        summaryOverride =
                            if (displayItem.isChain) {
                                val focusParts = displayItem.members.count { !it.isBreakSegment }
                                val recoveryBlocks = displayItem.members.count { it.isBreakSegment }
                                "$focusParts focus parts" +
                                    if (recoveryBlocks > 0)
                                        " · $recoveryBlocks recovery block${if (recoveryBlocks == 1) "" else "s"}"
                                    else ""
                            } else null,
                        onEdit = {
                            editingTask =
                                if (taskState.parentMissionId.isNotBlank()) {
                                    tasks
                                        .filter { it.parentMissionId == taskState.parentMissionId }
                                        .sortedBy {
                                            parseBuilderTimeOrNull(it.startTime) ?: LocalTime.MAX
                                        }
                                        .firstOrNull { !it.isBreakSegment } ?: taskState
                                } else {
                                    taskState
                                }
                            showFactoryDialog = true
                        },
                        onDuplicate = {
                            if (taskState.parentMissionId.isNotBlank()) {
                                viewModel.duplicateMissionChain(taskState.parentMissionId)
                            } else {
                                viewModel.duplicateMission(taskState.id)
                            }
                        },
                        onMoveUp = {
                            if (taskState.parentMissionId.isNotBlank()) {
                                viewModel.moveTaskChain(taskState.parentMissionId, moveUp = true)
                            } else {
                                viewModel.moveTask(index, index - 1)
                            }
                        },
                        onMoveDown = {
                            if (taskState.parentMissionId.isNotBlank()) {
                                viewModel.moveTaskChain(taskState.parentMissionId, moveUp = false)
                            } else {
                                viewModel.moveTask(index, index + 1)
                            }
                        },
                        onRemove = {
                            if (taskState.parentMissionId.isNotBlank()) {
                                viewModel.removeTaskChain(taskState.parentMissionId)
                            } else {
                                viewModel.removeTask(taskState.id)
                            }
                        },
                        canRemove = canRemove
                    )
                }
                if (tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚡", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "NO MISSIONS ARCHITECTED",
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            if (showFactoryDialog) {
                MissionFactoryDialog(
                    initialTask = editingTask,
                    builderTasks = tasks,
                    initialType = initialMissionType,
                    initialFocusTaskType = initialFocusTaskType,
                    onDismiss = {
                        showFactoryDialog = false
                        editingTask = null
                    },
                    onConfirm = { newTasks ->
                        if (editingTask != null) {

                            if (
                                editingTask?.parentMissionId?.isNotBlank() == true &&
                                    (editingTask?.segmentCount ?: 0) > 1
                            ) {
                                viewModel.updateMissionChain(
                                    editingTask!!.parentMissionId,
                                    newTasks
                                )
                            } else {
                                viewModel.updateMission(newTasks.first())
                            }
                        } else {
                            viewModel.addNewMissions(newTasks)
                        }
                        showFactoryDialog = false
                        editingTask = null
                    },
                    viewModel = viewModel,
                    prefManager = prefManager
                )
            }
            if (showDiscardDialog) {
                DiscardChangesDialog(
                    onDismiss = { showDiscardDialog = false },
                    onDiscard = {
                        showDiscardDialog = false
                        onNext()
                    }
                )
            }
            if (showSaveBlueprintDialog) {
                SaveBlueprintDialog(
                    onDismiss = { showSaveBlueprintDialog = false },
                    onSave = { title ->
                        viewModel.saveCurrentAsBlueprint(title)
                        showSaveBlueprintDialog = false
                    }
                )
            }
            if (showClearDayDialog) {
                ConfirmClearDayDialog(
                    onDismiss = { showClearDayDialog = false },
                    onConfirm = {
                        viewModel.clearAllMissions()
                        showClearDayDialog = false
                    }
                )
            }
            pendingBlueprint?.let { blueprint ->
                BlueprintApplyDialog(
                    blueprint = blueprint,
                    onDismiss = { pendingBlueprint = null },
                    onReplace = {
                        viewModel.applyBlueprint(blueprint.id, append = false)
                        pendingBlueprint = null
                    },
                    onAppend = {
                        val openSlots = (6 - tasks.size).coerceAtLeast(0)
                        if (blueprint.missions.size > openSlots) {
                            appendOverflowBlueprint = blueprint
                            appendOverflowSlots = openSlots
                        } else {
                            viewModel.applyBlueprint(blueprint.id, append = true)
                        }
                        pendingBlueprint = null
                    }
                )
            }
            appendOverflowBlueprint?.let { blueprint ->
                AppendOverflowDialog(
                    blueprint = blueprint,
                    availableSlots = appendOverflowSlots,
                    onDismiss = {
                        appendOverflowBlueprint = null
                        appendOverflowSlots = 0
                    },
                    onAppendFit = {
                        viewModel.applyBlueprint(blueprint.id, append = true)
                        appendOverflowBlueprint = null
                        appendOverflowSlots = 0
                    },
                    onReplace = {
                        viewModel.applyBlueprint(blueprint.id, append = false)
                        appendOverflowBlueprint = null
                        appendOverflowSlots = 0
                    }
                )
            }
        }
    }
}

@Composable
fun MissionDossierCard(
    task: TaskBuilderState,
    isFirst: Boolean,
    isLast: Boolean,
    isReadOnly: Boolean,
    chainBadge: String? = null,
    titleOverride: String? = null,
    timeOverride: String? = null,
    summaryOverride: String? = null,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean = true
) {
    val accentColor =
        when (task.importanceRank) {
            1 -> GoldXP
            2 -> NeonCyan
            else -> Color.White
        }
    val segmentChip =
        when {
            task.isBreakSegment -> "RECOVERY GAP"
            task.segmentCount > 1 && task.segmentIndex > 0 ->
                "PART ${task.segmentIndex}/${task.segmentCount}"
            else -> null
        }
    val displayTitle = titleOverride ?: task.name
    val displayTime = timeOverride ?: "${task.startTime} - ${task.endTime}"
    val secondarySummary = summaryOverride
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier =
            Modifier.fillMaxWidth()
                .then(if (!isReadOnly) Modifier.gamifiedClick(onClick = onEdit) else Modifier)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!isReadOnly) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowUp,
                            contentDescription = null,
                            tint = if (isFirst) Color.DarkGray else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (isLast) Color.DarkGray else Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon =
                        when (task.type) {
                            MissionType.DEEP_WORK -> Icons.Rounded.Bolt
                            MissionType.APP_INVEST -> Icons.Rounded.PhoneAndroid
                            MissionType.PHYSICAL -> Icons.Rounded.FitnessCenter
                            MissionType.PREP -> Icons.Rounded.School
                        }
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayTitle,
                        style =
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    chainBadge?.let { badge ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = SurfaceElevated, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                text = badge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                    }
                    segmentChip?.let { chip ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color =
                                if (task.isBreakSegment) SurfaceElevated
                                else accentColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (task.isBreakSegment) SuccessGreen else accentColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = displayTime, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text =
                                when (task.importanceRank) {
                                    1 -> "CRITICAL"
                                    2 -> "VITAL"
                                    else -> "GROWTH"
                                },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                    if (!isReadOnly) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TAP TO EDIT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray
                        )
                    }
                }
                secondarySummary?.let { summary ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = summary, fontSize = 11.sp, color = Color.Gray)
                }
            }
            if (isReadOnly) {
                Icon(
                    imageVector =
                        if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = if (task.isCompleted) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
            } else {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDuplicate) {
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = onRemove, enabled = canRemove) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription =
                                if (canRemove) "Remove"
                                else "Completed or failed tasks cannot be removed",
                            tint =
                                if (canRemove) Color.DarkGray
                                else Color.DarkGray.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ValidationAlertCard(issues: List<TaskBuilderValidationIssue>) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "SCHEDULE ISSUES DETECTED",
                    color = WarningAmber,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            issues.forEach { issue ->
                Text(
                    "• ${issue.message}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun NightDecisionLockCard(message: String) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Nightlight,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "TOMORROW IS SEALED",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(message, color = TextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun BuilderPlanningModeRow(
    mode: BuilderPlanningMode,
    onModeSelected: (BuilderPlanningMode) -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BuilderModeChip(
                label = "QUICK PLAN",
                selected = mode == BuilderPlanningMode.QUICK,
                modifier = Modifier.weight(1f)
            ) {
                onModeSelected(BuilderPlanningMode.QUICK)
            }
            BuilderModeChip(
                label = "ADVANCED",
                selected = mode == BuilderPlanningMode.ADVANCED,
                modifier = Modifier.weight(1f)
            ) {
                onModeSelected(BuilderPlanningMode.ADVANCED)
            }
        }
    }
}

@Composable
private fun BuilderModeChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (selected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (selected) Color.Black else Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun QuickPlannerToolsCard(
    suggestionsCount: Int,
    recommendedBlueprintCount: Int,
    savedBlueprintCount: Int,
    hasCloneSource: Boolean,
    onOpen: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SMART TOOLS",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text =
                    "Blueprints, suggestions, and cloning are available when you want them. Keep the first planning pass simple.",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (suggestionsCount > 0) {
                    CompactCountPill(
                        "$suggestionsCount suggestion${if (suggestionsCount == 1) "" else "s"}"
                    )
                }
                if (recommendedBlueprintCount > 0) {
                    CompactCountPill(
                        "$recommendedBlueprintCount quick blueprint${if (recommendedBlueprintCount == 1) "" else "s"}"
                    )
                }
                if (savedBlueprintCount > 0) {
                    CompactCountPill(
                        "$savedBlueprintCount saved blueprint${if (savedBlueprintCount == 1) "" else "s"}"
                    )
                }
                if (hasCloneSource) {
                    CompactCountPill("clone yesterday")
                }
            }
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text("OPEN SMART TOOLS", fontWeight = FontWeight.Black, letterSpacing = 0.6.sp)
            }
        }
    }
}

@Composable
private fun CompactCountPill(label: String) {

    Surface(color = Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(999.dp)) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BuilderAdvancedOptionsCard(expanded: Boolean, onToggle: () -> Unit) {

    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ADVANCED OPTIONS",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text =
                        if (expanded)
                            "Focus presets, task category, split chains, and smart suggestions are open."
                        else "Open deeper planning controls only if you need them.",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
fun PlanningSummaryCard(
    summary: TaskBuilderPlanningSummary,
    validationIssues: List<TaskBuilderValidationIssue>
) {
    val guidance =
        when {
            summary.conflictCount > 0 -> "Resolve overlaps before you lock this day."
            summary.criticalCount >= 4 ->
                "Critical load is heavy. Consider downgrading one mission."
            summary.prepCount > 0 && summary.deepWorkCount == 0 ->
                "Prep is loaded. Pair it with one real execution block."
            summary.deepWorkCount == 0 && summary.missionCount > 0 ->
                "No deep-work block yet. The day may drift."
            summary.totalPlannedMinutes >= 8 * 60 ->
                "This is a full-load day. Protect your recovery gaps."
            summary.missionCount == 0 -> "Start with one decisive mission and build from there."
            else -> "The blueprint looks balanced enough to execute."
        }
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "DAY LOAD",
                color = SuccessGreen,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlanningMetric(
                    label = "MISSIONS",
                    value = summary.missionCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                PlanningMetric(
                    label = "PLANNED",
                    value = formatPlannedMinutes(summary.totalPlannedMinutes),
                    modifier = Modifier.weight(1f)
                )
                PlanningMetric(
                    label = "CRITICAL",
                    value = summary.criticalCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeMixChip("Deep", summary.deepWorkCount, WarningAmber)
                TypeMixChip("App", summary.appInvestCount, NeonCyan)
                TypeMixChip("Physical", summary.physicalCount, SuccessGreen)
                TypeMixChip("Prep", summary.prepCount, ElectricViolet)
                if (summary.conflictCount > 0) {
                    TypeMixChip("Conflicts", summary.conflictCount, ErrorRed)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                guidance,
                color = if (validationIssues.isNotEmpty()) WarningAmber else Color.Gray,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PlanningMetric(label: String, value: String, modifier: Modifier = Modifier) {

    Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TypeMixChip(label: String, count: Int, color: Color) {

    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
        Text(
            "$label $count",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatPlannedMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

@Composable
fun SuggestionDeck(suggestions: List<TaskBuilderSuggestion>) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "ARCHITECT GUIDANCE",
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            suggestions.forEach { suggestion ->
                val tone =
                    when (suggestion.severity) {
                        SuggestionSeverity.INFO -> NeonCyan
                        SuggestionSeverity.CAUTION -> WarningAmber
                        SuggestionSeverity.STRONG -> ErrorRed
                    }
                Surface(color = tone.copy(alpha = 0.08f), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            suggestion.title.uppercase(),
                            color = tone,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            suggestion.detail,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlueprintLibrarySection(
    selectedTab: BlueprintLibraryTab,
    onTabSelected: (BlueprintLibraryTab) -> Unit,
    recommendedBlueprints: List<TaskBlueprint>,
    savedBlueprints: List<TaskBlueprint>,
    onApply: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val resolvedTab =
        when {
            selectedTab == BlueprintLibraryTab.QUICK && recommendedBlueprints.isNotEmpty() ->
                BlueprintLibraryTab.QUICK
            selectedTab == BlueprintLibraryTab.SAVED && savedBlueprints.isNotEmpty() ->
                BlueprintLibraryTab.SAVED
            recommendedBlueprints.isNotEmpty() -> BlueprintLibraryTab.QUICK
            else -> BlueprintLibraryTab.SAVED
        }
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "BLUEPRINTS",
                color = SuccessGreen,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                "Use a ready-made structure when you want speed. Build from scratch when you want a clean custom day.",
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            if (recommendedBlueprints.isNotEmpty() && savedBlueprints.isNotEmpty()) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BuilderModeChip(
                        label = "QUICK",
                        selected = resolvedTab == BlueprintLibraryTab.QUICK,
                        modifier = Modifier.weight(1f)
                    ) {
                        onTabSelected(BlueprintLibraryTab.QUICK)
                    }
                    BuilderModeChip(
                        label = "SAVED",
                        selected = resolvedTab == BlueprintLibraryTab.SAVED,
                        modifier = Modifier.weight(1f)
                    ) {
                        onTabSelected(BlueprintLibraryTab.SAVED)
                    }
                }
            }
            RecommendedBlueprintsCard(
                title =
                    if (resolvedTab == BlueprintLibraryTab.QUICK) "QUICK BLUEPRINTS"
                    else "SAVED BLUEPRINTS",
                blueprints =
                    if (resolvedTab == BlueprintLibraryTab.QUICK) recommendedBlueprints
                    else savedBlueprints,
                onApply = onApply,
                onDelete = if (resolvedTab == BlueprintLibraryTab.SAVED) onDelete else null
            )
        }
    }
}

@Composable
fun RecommendedBlueprintsCard(
    title: String,
    blueprints: List<TaskBlueprint>,
    onApply: (String) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                color = SuccessGreen,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            blueprints.forEach { blueprint ->
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    blueprint.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    blueprint.summary,
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (onDelete != null) {
                                    IconButton(onClick = { onDelete(blueprint.id) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete blueprint",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                TextButton(onClick = { onApply(blueprint.id) }) {
                                    Text(
                                        "APPLY",
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaveBlueprintDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        title = { Text("SAVE BLUEPRINT", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text(
                    "Store this day structure so you can reuse it later without rebuilding it from zero.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Blueprint name", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title) }, enabled = title.isNotBlank()) {
                Text("SAVE", color = SuccessGreen, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ConfirmClearDayDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        title = { Text("CLEAR THIS DAY", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Text(
                "This clears the current builder plan so you can restart fast. Saved blueprints stay untouched.",
                color = Color.Gray
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("CLEAR", color = ErrorRed, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun BlueprintApplyDialog(
    blueprint: TaskBlueprint,
    onDismiss: () -> Unit,
    onReplace: () -> Unit,
    onAppend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "APPLY ${blueprint.title.uppercase()}",
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(
                "Replace the current day with this blueprint, or append it onto the current plan up to the six-mission cap.",
                color = Color.Gray
            )
        },
        confirmButton = {
            TextButton(onClick = onReplace) {
                Text("REPLACE", color = SuccessGreen, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onAppend) {
                    Text("APPEND", color = NeonCyan, fontWeight = FontWeight.Black)
                }
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun AppendOverflowDialog(
    blueprint: TaskBlueprint,
    availableSlots: Int,
    onDismiss: () -> Unit,
    onAppendFit: () -> Unit,
    onReplace: () -> Unit
) {
    val droppedCount = (blueprint.missions.size - availableSlots).coerceAtLeast(0)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        title = { Text("BLUEPRINT OVERFLOW", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Text(
                buildString {
                    append("Your board has room for only $availableSlots more mission")
                    if (availableSlots != 1) append("s")
                    append(". ")
                    append("$droppedCount mission")
                    if (droppedCount != 1) append("s")
                    append(" from ${blueprint.title} would be skipped if you append.")
                },
                color = Color.Gray
            )
        },
        confirmButton = {
            TextButton(onClick = onAppendFit) {
                Text("APPEND WHAT FITS", color = NeonCyan, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReplace) {
                    Text("REPLACE", color = SuccessGreen, fontWeight = FontWeight.Black)
                }
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun MissionFactoryDialog(
    initialTask: TaskBuilderState? = null,
    builderTasks: List<TaskBuilderState>,
    initialType: MissionType? = null,
    initialFocusTaskType: FocusTaskType? = null,
    onDismiss: () -> Unit,
    onConfirm: (List<TaskBuilderState>) -> Unit,
    viewModel: TaskBuilderViewModel,
    prefManager: PrefManager
) {
    var step by
        remember(initialTask) {
            mutableIntStateOf(if (initialTask == null) 1 else 2)
        } /* 1: Category & Name, 2: System Params */
    val splitDefaults =
        remember(initialTask, builderTasks) { resolveSplitChainDefaults(initialTask, builderTasks) }
    val resolvedInitialType = initialTask?.type ?: initialType ?: MissionType.DEEP_WORK
    val defaultTaskType =
        initialTask?.focusTaskType
            ?: initialFocusTaskType
            ?: defaultFocusTaskTypeForMission(resolvedInitialType)
    var selectedType by remember(initialTask, initialType) { mutableStateOf(resolvedInitialType) }
    var missionName by remember(initialTask) { mutableStateOf(initialTask?.name ?: "") }
    var selectedPackage by remember(initialTask) { mutableStateOf(initialTask?.packageName) }
    var selectedTaskType by
        remember(initialTask, initialType, initialFocusTaskType) { mutableStateOf(defaultTaskType) }
    var focusModeEnabled by
        remember(initialTask, initialType, initialFocusTaskType) {
            mutableStateOf(
                initialTask?.focusModeEnabled
                    ?: recommendedFocusModeEnabled(
                        initialTask?.focusTaskType
                            ?: initialFocusTaskType
                            ?: defaultFocusTaskTypeForMission(
                                initialTask?.type ?: initialType ?: MissionType.DEEP_WORK
                            )
                    )
            )
        }
    var focusPreset by
        remember(initialTask) {
            mutableStateOf(initialTask?.focusPreset ?: FocusPreset.CLASSIC_25_5)
        }
    var importance by remember(initialTask) { mutableIntStateOf(initialTask?.importanceRank ?: 3) }
    var startTime by remember(initialTask) { mutableStateOf(initialTask?.startTime ?: "09:00") }
    var endTime by remember(initialTask) { mutableStateOf(initialTask?.endTime ?: "10:00") }
    var showAdvancedOptions by
        remember(initialTask, splitDefaults, defaultTaskType, resolvedInitialType) {
            mutableStateOf(
                initialTask != null ||
                    splitDefaults.enabled ||
                    defaultTaskType != defaultFocusTaskTypeForMission(resolvedInitialType)
            )
        }
    var appSearchQuery by remember { mutableStateOf("") }
    val canUseSplitControls = initialTask == null || splitDefaults.enabled
    var splitIntoParts by
        remember(initialTask, splitDefaults) { mutableStateOf(splitDefaults.enabled) }
    var splitParts by
        remember(initialTask, splitDefaults) { mutableIntStateOf(splitDefaults.partCount) }
    var gapMinutes by
        remember(initialTask, splitDefaults) { mutableIntStateOf(splitDefaults.gapMinutes) }
    var spacingMode by
        remember(initialTask, splitDefaults) { mutableStateOf(splitDefaults.spacingMode) }
    val parentMissionId =
        remember(initialTask) {
            initialTask?.parentMissionId?.takeIf { it.isNotBlank() }
                ?: "split_${System.currentTimeMillis()}"
        }
    /* Internal state to track which time is being edited */
    var editingStartTime by remember { mutableStateOf(true) }
    val investableApps by viewModel.investableApps.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.fetchInvestableApps(context) }
    val selectedStart = remember(startTime) { parseBuilderTimeOrNull(startTime) }
    val selectedEnd = remember(endTime) { parseBuilderTimeOrNull(endTime) }
    val sleepLockWindow =
        remember(
            prefManager.sleepLockEnabled,
            prefManager.sleepLockBedHour,
            prefManager.sleepLockBedMinute,
            prefManager.sleepLockWakeHour,
            prefManager.sleepLockWakeMinute
        ) {
            resolveSleepLockWindow(prefManager)
        }
    val sleepLockStart = sleepLockWindow.start
    val sleepLockEnd = sleepLockWindow.end
    val sleepPlanningBoundary = sleepLockStart
    val sleepBlockedRanges =
        remember(sleepLockWindow) {
            buildSleepLockProtectedSegments(sleepLockWindow).map { (start, end) ->
                ProtectedTimeRange(start, end, "SLEEP_LOCK")
            }
        }
    val sleepCautionRanges =
        remember(sleepLockStart) {
            val cautionStart = sleepLockStart.minusMinutes(30)
            listOf(ProtectedTimeRange(cautionStart, sleepLockStart, "PRE-SLEEP_LOCK"))
        }
    val occupiedWindows =
        remember(builderTasks, initialTask) {
            builderTasks
                .mapNotNull { task ->
                    val taskId = task.dbId ?: task.id
                    if (
                        initialTask?.parentMissionId?.isNotBlank() == true &&
                            task.parentMissionId == initialTask.parentMissionId
                    ) {
                        return@mapNotNull null
                    }
                    if (taskId == (initialTask?.dbId ?: initialTask?.id)) return@mapNotNull null
                    val start = parseBuilderTimeOrNull(task.startTime) ?: return@mapNotNull null
                    val end = parseBuilderTimeOrNull(task.endTime) ?: return@mapNotNull null
                    if (!end.isAfter(start)) return@mapNotNull null
                    OccupiedMissionWindow(
                        taskId = taskId,
                        name = task.name,
                        start = start,
                        end = end
                    )
                }
                .sortedBy { it.start }
        }
    val selectedDurationMinutes =
        remember(selectedStart, selectedEnd) {
            if (
                selectedStart != null && selectedEnd != null && selectedEnd.isAfter(selectedStart)
            ) {
                java.time.Duration.between(selectedStart, selectedEnd).toMinutes().toInt()
            } else {
                45
            }
        }
    val generatedTasks =
        remember(
            missionName,
            selectedType,
            selectedPackage,
            selectedTaskType,
            focusModeEnabled,
            focusPreset,
            importance,
            startTime,
            endTime,
            splitIntoParts,
            splitParts,
            gapMinutes,
            spacingMode,
            parentMissionId,
            canUseSplitControls,
            initialTask
        ) {
            val start = parseBuilderTimeOrNull(startTime)
            val end = parseBuilderTimeOrNull(endTime)
            if (missionName.isBlank() || start == null || end == null || !end.isAfter(start)) {
                emptyList()
            } else if (splitIntoParts && canUseSplitControls) {
                buildSplitMissionPlan(
                    baseName = missionName,
                    type = selectedType,
                    focusTaskType = selectedTaskType,
                    focusModeEnabled = focusModeEnabled,
                    focusPreset = focusPreset,
                    packageName = selectedPackage,
                    importance = importance,
                    firstStart = start,
                    firstEnd = end,
                    parts = splitParts,
                    gapMinutes = gapMinutes,
                    spacingMode = spacingMode,
                    parentMissionId = parentMissionId
                )
            } else {
                listOf(
                    TaskBuilderState(
                        id = initialTask?.id ?: System.currentTimeMillis().toInt(),
                        dbId = initialTask?.dbId,
                        name = missionName,
                        startTime = startTime,
                        endTime = endTime,
                        importanceRank = importance,
                        type = selectedType,
                        focusTaskType = selectedTaskType,
                        focusModeEnabled = focusModeEnabled,
                        focusPreset = focusPreset,
                        parentMissionId = initialTask?.parentMissionId ?: "",
                        segmentBaseName = initialTask?.segmentBaseName ?: "",
                        segmentIndex = initialTask?.segmentIndex ?: 0,
                        segmentCount = initialTask?.segmentCount ?: 0,
                        isBreakSegment = initialTask?.isBreakSegment ?: false,
                        packageName = selectedPackage
                    )
                )
            }
        }
    val generatedWindows =
        remember(generatedTasks) {
            generatedTasks.mapNotNull { task ->
                val start = parseBuilderTimeOrNull(task.startTime) ?: return@mapNotNull null
                val end = parseBuilderTimeOrNull(task.endTime) ?: return@mapNotNull null
                if (!end.isAfter(start)) return@mapNotNull null
                OccupiedMissionWindow(
                    taskId = task.dbId ?: task.id,
                    name = task.name,
                    start = start,
                    end = end
                )
            }
        }
    val conflictingWindows =
        remember(generatedWindows, occupiedWindows) {
            occupiedWindows.filter { existing ->
                generatedWindows.any { generated ->
                    generated.start < existing.end && generated.end > existing.start
                }
            }
        }
    val sleepBlockedConflicts =
        remember(generatedWindows, sleepBlockedRanges) {
            sleepBlockedRanges.filter { blocked ->
                generatedWindows.any { generated ->
                    intersectsTimeRange(generated.start, generated.end, blocked.start, blocked.end)
                }
            }
        }
    val sleepCautionConflicts =
        remember(generatedWindows, sleepCautionRanges, sleepBlockedConflicts) {
            sleepCautionRanges
                .filter { caution ->
                    generatedWindows.any { generated ->
                        generated.start < caution.end && generated.end > caution.start
                    }
                }
                .filterNot { caution ->
                    sleepBlockedConflicts.any { blocked ->
                        caution.start == blocked.start && caution.end == blocked.end
                    }
                }
        }
    val recoveryGapSignals =
        remember(generatedWindows, occupiedWindows, sleepBlockedRanges) {
            buildRecoveryGapSignals(
                generatedWindows = generatedWindows,
                occupiedWindows = occupiedWindows,
                protectedRanges = sleepBlockedRanges,
                requiredGapMinutes = 15
            )
        }
    val hasInvalidTimeOrder =
        selectedStart != null && selectedEnd != null && !selectedEnd.isAfter(selectedStart)
    val hasConflict = conflictingWindows.isNotEmpty() || sleepBlockedConflicts.isNotEmpty()
    val hasValidWindow = generatedTasks.isNotEmpty()
    val boardOverflowCount =
        remember(builderTasks, generatedTasks, initialTask) {
            val currentCount =
                when {
                    initialTask?.parentMissionId?.isNotBlank() == true ->
                        builderTasks.count { it.parentMissionId != initialTask.parentMissionId }
                    initialTask != null -> builderTasks.size - 1
                    else -> builderTasks.size
                }
            (currentCount + generatedTasks.size - 6).coerceAtLeast(0)
        }
    val canConfirm =
        missionName.isNotBlank() && hasValidWindow && !hasConflict && boardOverflowCount == 0
    val openSlotSuggestions =
        remember(
            occupiedWindows,
            sleepBlockedRanges,
            selectedDurationMinutes,
            selectedStart,
            selectedEnd
        ) {
            buildOpenSlotSuggestions(
                occupiedWindows = occupiedWindows,
                preferredDurationMinutes = selectedDurationMinutes.coerceAtLeast(15),
                anchorTime = selectedStart ?: LocalTime.of(8, 0),
                dayEndBoundary = sleepPlanningBoundary
            )
        }
    val resolutionSuggestions =
        remember(
            hasInvalidTimeOrder,
            hasConflict,
            selectedStart,
            selectedEnd,
            selectedDurationMinutes,
            conflictingWindows,
            occupiedWindows,
            openSlotSuggestions,
            sleepBlockedConflicts,
            sleepLockStart,
            recoveryGapSignals
        ) {
            buildTimeResolutionSuggestions(
                hasInvalidTimeOrder = hasInvalidTimeOrder,
                hasConflict = hasConflict,
                start = selectedStart,
                end = selectedEnd,
                preferredDurationMinutes = selectedDurationMinutes.coerceAtLeast(15),
                conflictingWindows = conflictingWindows,
                occupiedWindows = occupiedWindows,
                openSlots = openSlotSuggestions,
                sleepBlockedConflicts = sleepBlockedConflicts,
                bedtime = sleepLockStart,
                recoveryGapSignals = recoveryGapSignals,
                dayEndBoundary = sleepPlanningBoundary
            )
        }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Column(modifier = Modifier.padding(24.dp)) {
                /* Header */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (step > 1) step-- else onDismiss() }) {
                        Icon(
                            if (step == 1) Icons.Rounded.Close
                            else Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Text(
                        text = if (step == 1) "MISSION SELECTION" else "MISSION WINDOW",
                        style =
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    if (step == 1) {
                        /* Category Selection */
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TypeSelectionButton(
                                "DEEP WORK",
                                Icons.Rounded.Bolt,
                                selectedType == MissionType.DEEP_WORK,
                                Modifier.weight(1f)
                            ) {
                                selectedType = MissionType.DEEP_WORK
                                selectedTaskType =
                                    defaultFocusTaskTypeForMission(MissionType.DEEP_WORK)
                                focusModeEnabled = recommendedFocusModeEnabled(selectedTaskType)
                                missionName = ""
                                selectedPackage = null
                            }
                            TypeSelectionButton(
                                "APP INVEST",
                                Icons.Rounded.PhoneAndroid,
                                selectedType == MissionType.APP_INVEST,
                                Modifier.weight(1f)
                            ) {
                                selectedType = MissionType.APP_INVEST
                                selectedTaskType =
                                    defaultFocusTaskTypeForMission(MissionType.APP_INVEST)
                                focusModeEnabled = recommendedFocusModeEnabled(selectedTaskType)
                                missionName = ""
                            }
                            TypeSelectionButton(
                                "PHYSICAL",
                                Icons.Rounded.FitnessCenter,
                                selectedType == MissionType.PHYSICAL,
                                Modifier.weight(1f)
                            ) {
                                selectedType = MissionType.PHYSICAL
                                selectedTaskType =
                                    defaultFocusTaskTypeForMission(MissionType.PHYSICAL)
                                focusModeEnabled = false
                                missionName = ""
                                selectedPackage = null
                            }
                            TypeSelectionButton(
                                "PREP",
                                Icons.Rounded.School,
                                selectedType == MissionType.PREP,
                                Modifier.weight(1f)
                            ) {
                                selectedType = MissionType.PREP
                                selectedTaskType = defaultFocusTaskTypeForMission(MissionType.PREP)
                                focusModeEnabled = recommendedFocusModeEnabled(selectedTaskType)
                                missionName = ""
                                selectedPackage = null
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 360.dp)
                        ) {
                            when (selectedType) {
                                MissionType.DEEP_WORK ->
                                    TemplateGrid(DEEP_WORK_TEMPLATES) {
                                        missionName = it
                                        step = 2
                                    }
                                MissionType.PHYSICAL ->
                                    TemplateGrid(PHYSICAL_TEMPLATES) {
                                        missionName = it
                                        step = 2
                                    }
                                MissionType.PREP ->
                                    TemplateGrid(PREP_TEMPLATES) {
                                        missionName = it
                                        step = 2
                                    }
                                MissionType.APP_INVEST ->
                                    Column {
                                        OutlinedTextField(
                                            value = appSearchQuery,
                                            onValueChange = { appSearchQuery = it },
                                            label = {
                                                Text("Search work apps", color = Color.Gray)
                                            },
                                            modifier =
                                                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            colors =
                                                OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = SuccessGreen,
                                                    unfocusedBorderColor = SurfaceElevated,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                        )
                                        AppInvestGrid(
                                            apps =
                                                investableApps.filter {
                                                    appSearchQuery.isBlank() ||
                                                        it.name.contains(
                                                            appSearchQuery,
                                                            ignoreCase = true
                                                        )
                                                }
                                        ) { name, pkg ->
                                            missionName = name
                                            selectedPackage = pkg
                                            step = 2
                                        }
                                    }
                            }
                        }
                        if (selectedType != MissionType.APP_INVEST) {
                            OutlinedTextField(
                                value = missionName,
                                onValueChange = { missionName = it },
                                label = { Text("Custom Mission Name", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SuccessGreen,
                                        unfocusedBorderColor = SurfaceElevated,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                            )
                            Button(
                                onClick = { step = 2 },
                                enabled = missionName.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text(
                                    "NEXT: PARAMETERS",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else {
                        /* Step 2: Time and Priority */
                        Text(
                            "PRIORITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PriorityCard(
                                1,
                                "CRITICAL",
                                GoldXP,
                                importance == 1,
                                Modifier.weight(1f)
                            ) {
                                importance = 1
                            }
                            PriorityCard(
                                2,
                                "VITAL",
                                NeonCyan,
                                importance == 2,
                                Modifier.weight(1f)
                            ) {
                                importance = 2
                            }
                            PriorityCard(
                                3,
                                "GROWTH",
                                Color.White,
                                importance == 3,
                                Modifier.weight(1f)
                            ) {
                                importance = 3
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        if (selectedType == MissionType.PREP) {
                            Text(
                                "PREP TRACK",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FocusTaskTypeChip(
                                        "DSA",
                                        selectedTaskType == FocusTaskType.DSA,
                                        Modifier.weight(1f)
                                    ) {
                                        selectedTaskType = FocusTaskType.DSA
                                        focusModeEnabled = true
                                    }
                                    FocusTaskTypeChip(
                                        "SYSTEM DESIGN",
                                        selectedTaskType == FocusTaskType.SYSTEM_DESIGN,
                                        Modifier.weight(1f)
                                    ) {
                                        selectedTaskType = FocusTaskType.SYSTEM_DESIGN
                                        focusModeEnabled = true
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FocusTaskTypeChip(
                                        "BEHAVIORAL",
                                        selectedTaskType == FocusTaskType.BEHAVIORAL,
                                        Modifier.weight(1f)
                                    ) {
                                        selectedTaskType = FocusTaskType.BEHAVIORAL
                                        focusModeEnabled = false
                                    }
                                    FocusTaskTypeChip(
                                        "PROJECT REVIEW",
                                        selectedTaskType == FocusTaskType.PROJECT_REVIEW,
                                        Modifier.weight(1f)
                                    ) {
                                        selectedTaskType = FocusTaskType.PROJECT_REVIEW
                                        focusModeEnabled = true
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        if (selectedType == MissionType.PREP) {
                            PrepTrackHintCard(track = selectedTaskType)
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        /* Dual Time Selector */
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TimeSelectionCard(
                                label = "START",
                                time = startTime,
                                isSelected = editingStartTime,
                                hasError = hasInvalidTimeOrder,
                                modifier = Modifier.weight(1f)
                            ) {
                                editingStartTime = true
                            }
                            TimeSelectionCard(
                                label = "END",
                                time = endTime,
                                isSelected = !editingStartTime,
                                hasError = hasInvalidTimeOrder,
                                modifier = Modifier.weight(1f)
                            ) {
                                editingStartTime = false
                            }
                        }
                        AnimatedVisibility(visible = hasInvalidTimeOrder) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "End time must be after start time. Fix the clock window to continue.",
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DurationPresetRow(
                            onSelectMinutes = { minutes ->
                                val parsedStart = TimeUtils.parseTimeSafe(startTime)
                                endTime =
                                    TimeUtils.formatToStandard(
                                        parsedStart.plusMinutes(minutes.toLong())
                                    )
                            }
                        )
                        if (canUseSplitControls) {
                            Spacer(modifier = Modifier.height(18.dp))
                            SplitMissionSettingsCard(
                                enabled = splitIntoParts,
                                partCount = splitParts,
                                gapMinutes = gapMinutes,
                                spacingMode = spacingMode,
                                onEnabledChange = { splitIntoParts = it },
                                onPartCountChange = { splitParts = it },
                                onGapMinutesChange = { gapMinutes = it },
                                onSpacingModeChange = {
                                    spacingMode = it
                                    gapMinutes =
                                        when (it) {
                                            SplitSpacingMode.RECOVERY_GAP ->
                                                gapMinutes.coerceIn(0, 120)
                                            SplitSpacingMode.AFTER_DELAY ->
                                                gapMinutes.coerceAtLeast(30)
                                        }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            OrbitalTimePicker(
                                initialTime =
                                    if (editingStartTime) TimeUtils.parseTimeSafe(startTime)
                                    else TimeUtils.parseTimeSafe(endTime),
                                occupiedRanges = occupiedWindows.map { it.start to it.end },
                                protectedRanges = sleepBlockedRanges.map { it.start to it.end },
                                cautionRanges = sleepCautionRanges.map { it.start to it.end },
                                hasConflict = hasConflict,
                                onTimeChange = {
                                    val formatted = TimeUtils.formatToStandard(it)
                                    if (editingStartTime) startTime = formatted
                                    else endTime = formatted
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TimelineLegendRow(hasSleepProtection = true)
                        Spacer(modifier = Modifier.height(20.dp))
                        TimeWindowStatusCard(
                            start = selectedStart,
                            end = selectedEnd,
                            conflictingWindows = conflictingWindows,
                            sleepBlockedConflicts = sleepBlockedConflicts,
                            sleepCautionConflicts = sleepCautionConflicts,
                            recoveryGapSignals = recoveryGapSignals
                        )
                        if (
                            resolutionSuggestions.isNotEmpty() &&
                                (hasConflict || hasInvalidTimeOrder)
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TimeResolutionSuggestionsCard(
                                hasInvalidTimeOrder = hasInvalidTimeOrder,
                                hasConflict = hasConflict,
                                suggestions = resolutionSuggestions,
                                onApply = { suggestion ->
                                    startTime = TimeUtils.formatToStandard(suggestion.start)
                                    endTime = TimeUtils.formatToStandard(suggestion.end)
                                    editingStartTime = true
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        BuilderAdvancedOptionsCard(
                            expanded = showAdvancedOptions,
                            onToggle = { showAdvancedOptions = !showAdvancedOptions }
                        )
                        if (showAdvancedOptions) {
                            Spacer(modifier = Modifier.height(18.dp))
                            if (selectedType != MissionType.PREP) {
                                Text(
                                    "TASK CATEGORY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FocusTaskTypeChip(
                                            "STUDY",
                                            selectedTaskType == FocusTaskType.STUDY,
                                            Modifier.weight(1f)
                                        ) {
                                            selectedTaskType = FocusTaskType.STUDY
                                            focusModeEnabled = true
                                        }
                                        FocusTaskTypeChip(
                                            "CODE",
                                            selectedTaskType == FocusTaskType.CODE,
                                            Modifier.weight(1f)
                                        ) {
                                            selectedTaskType = FocusTaskType.CODE
                                            focusModeEnabled = true
                                        }
                                        FocusTaskTypeChip(
                                            "DESIGN",
                                            selectedTaskType == FocusTaskType.DESIGN,
                                            Modifier.weight(1f)
                                        ) {
                                            selectedTaskType = FocusTaskType.DESIGN
                                            focusModeEnabled = true
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FocusTaskTypeChip(
                                            "READ",
                                            selectedTaskType == FocusTaskType.READ,
                                            Modifier.weight(1f)
                                        ) {
                                            selectedTaskType = FocusTaskType.READ
                                            focusModeEnabled = true
                                        }
                                        FocusTaskTypeChip(
                                            "PHYSICAL",
                                            selectedTaskType == FocusTaskType.PHYSICAL,
                                            Modifier.weight(1f)
                                        ) {
                                            selectedTaskType = FocusTaskType.PHYSICAL
                                            focusModeEnabled = false
                                        }
                                        FocusTaskTypeChip(
                                            "ADMIN",
                                            selectedTaskType == FocusTaskType.ADMIN,
                                            Modifier.weight(1f)
                                        ) {
                                            selectedTaskType = FocusTaskType.ADMIN
                                            focusModeEnabled = false
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FocusTaskTypeChip(
                                            "OTHER",
                                            selectedTaskType == FocusTaskType.OTHER,
                                            Modifier.weight(1f)
                                        ) {
                                            selectedTaskType = FocusTaskType.OTHER
                                        }
                                        Spacer(modifier = Modifier.weight(2f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            FocusModeSettingsCard(
                                enabled = focusModeEnabled,
                                canEnable =
                                    selectedTaskType != FocusTaskType.PHYSICAL &&
                                        selectedTaskType != FocusTaskType.BEHAVIORAL,
                                preset = focusPreset,
                                onEnabledChange = { enabled ->
                                    focusModeEnabled =
                                        enabled &&
                                            selectedTaskType != FocusTaskType.PHYSICAL &&
                                            selectedTaskType != FocusTaskType.BEHAVIORAL
                                },
                                onPresetChange = { focusPreset = it }
                            )
                            if (canUseSplitControls) {
                                Spacer(modifier = Modifier.height(18.dp))
                                SplitMissionSettingsCard(
                                    enabled = splitIntoParts,
                                    partCount = splitParts,
                                    gapMinutes = gapMinutes,
                                    spacingMode = spacingMode,
                                    onEnabledChange = { splitIntoParts = it },
                                    onPartCountChange = { splitParts = it },
                                    onGapMinutesChange = { gapMinutes = it },
                                    onSpacingModeChange = {
                                        spacingMode = it
                                        gapMinutes =
                                            when (it) {
                                                SplitSpacingMode.RECOVERY_GAP ->
                                                    gapMinutes.coerceIn(0, 120)
                                                SplitSpacingMode.AFTER_DELAY ->
                                                    gapMinutes.coerceAtLeast(30)
                                            }
                                    }
                                )
                            }
                            if (
                                resolutionSuggestions.isNotEmpty() &&
                                    !hasConflict &&
                                    !hasInvalidTimeOrder
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))
                                TimeResolutionSuggestionsCard(
                                    hasInvalidTimeOrder = hasInvalidTimeOrder,
                                    hasConflict = hasConflict,
                                    suggestions = resolutionSuggestions,
                                    onApply = { suggestion ->
                                        startTime = TimeUtils.formatToStandard(suggestion.start)
                                        endTime = TimeUtils.formatToStandard(suggestion.end)
                                        editingStartTime = true
                                    }
                                )
                            }
                            if (
                                splitIntoParts && canUseSplitControls && generatedTasks.isNotEmpty()
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))
                                SplitMissionPreviewCard(
                                    generatedTasks = generatedTasks,
                                    overflowCount = boardOverflowCount
                                )
                            }
                        }
                        if (occupiedWindows.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OccupiedWindowsCard(occupiedWindows = occupiedWindows)
                        }
                        if (
                            openSlotSuggestions.isNotEmpty() && !hasInvalidTimeOrder && !hasConflict
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OpenSlotSuggestionsCard(
                                suggestions = openSlotSuggestions,
                                onApply = { suggestion ->
                                    startTime = TimeUtils.formatToStandard(suggestion.start)
                                    endTime = TimeUtils.formatToStandard(suggestion.end)
                                    editingStartTime = true
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onConfirm(generatedTasks) },
                            enabled = canConfirm,
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text(
                                when {
                                    initialTask != null -> "UPDATE MISSION"
                                    splitIntoParts -> "AUTHORIZE MISSION CHAIN"
                                    else -> "AUTHORIZE MISSION"
                                },
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeWindowStatusCard(
    start: LocalTime?,
    end: LocalTime?,
    conflictingWindows: List<OccupiedMissionWindow>,
    sleepBlockedConflicts: List<ProtectedTimeRange>,
    sleepCautionConflicts: List<ProtectedTimeRange>,
    recoveryGapSignals: List<RecoveryGapSignal>
) {
    val isWindowInvalid = start == null || end == null || !end.isAfter(start)
    val (title, detail, tone) =
        when {
            isWindowInvalid ->
                Triple(
                    "INVALID WINDOW",
                    "End time must be after start time before this mission can be authorized.",
                    ErrorRed
                )
            sleepBlockedConflicts.isNotEmpty() ->
                Triple(
                    "SLEEP LOCK ACTIVE",
                    "This mission enters your Sleep Lock block. Move it earlier so work cannot stack into protected sleep.",
                    ElectricViolet
                )
            conflictingWindows.isNotEmpty() ->
                Triple(
                    "TIME CONFLICT DETECTED",
                    "Conflicts with ${conflictingWindows.joinToString { it.name }}. Move this mission into an open window.",
                    ErrorRed
                )
            sleepCautionConflicts.isNotEmpty() ->
                Triple(
                    "LATE WINDOW CAUTION",
                    "This mission runs too close to Sleep Lock. It can be saved, but it will compress the night.",
                    ElectricViolet
                )
            recoveryGapSignals.isNotEmpty() ->
                Triple("RECOVERY GAP MISSING", recoveryGapSignals.first().detail, WarningAmber)
            else ->
                Triple(
                    "WINDOW CLEAR",
                    "This mission sits inside an open slot and can be saved safely.",
                    SuccessGreen
                )
        }
    Surface(
        color = tone.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                color = tone,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(detail, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun TimelineLegendRow(hasSleepProtection: Boolean) {

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimelineLegendChip(label = "Occupied", color = ErrorRed)
            if (hasSleepProtection) {
                TimelineLegendChip(label = "Sleep", color = ElectricViolet)
                TimelineLegendChip(label = "Caution", color = ElectricViolet)
            }
        }
    }
}

@Composable
private fun TimelineLegendChip(label: String, color: Color) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label.uppercase(),
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun SplitMissionSettingsCard(
    enabled: Boolean,
    partCount: Int,
    gapMinutes: Int,
    spacingMode: SplitSpacingMode,
    onEnabledChange: (Boolean) -> Unit,
    onPartCountChange: (Int) -> Unit,
    onGapMinutesChange: (Int) -> Unit,
    onSpacingModeChange: (SplitSpacingMode) -> Unit
) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "SPLIT INTO PARTS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Use the selected time as one focus block, then auto-build the rest of the chain.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen
                        )
                )
            }
            if (enabled) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ModeChip(
                        label = "RECOVERY GAP",
                        selected = spacingMode == SplitSpacingMode.RECOVERY_GAP,
                        onClick = { onSpacingModeChange(SplitSpacingMode.RECOVERY_GAP) },
                        modifier = Modifier.weight(1f)
                    )
                    ModeChip(
                        label = "NEXT PART AFTER",
                        selected = spacingMode == SplitSpacingMode.AFTER_DELAY,
                        onClick = { onSpacingModeChange(SplitSpacingMode.AFTER_DELAY) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StepperChip(
                        label = "PARTS",
                        value = partCount.toString(),
                        onDecrease = { if (partCount > 2) onPartCountChange(partCount - 1) },
                        onIncrease = { if (partCount < 4) onPartCountChange(partCount + 1) },
                        modifier = Modifier.weight(1f)
                    )
                    StepperChip(
                        label =
                            if (spacingMode == SplitSpacingMode.RECOVERY_GAP) "BREAK" else "DELAY",
                        value = formatGapValue(gapMinutes, spacingMode),
                        onDecrease = {
                            val step = if (spacingMode == SplitSpacingMode.RECOVERY_GAP) 5 else 30
                            val minimum =
                                if (spacingMode == SplitSpacingMode.RECOVERY_GAP) 0 else 30
                            onGapMinutesChange((gapMinutes - step).coerceAtLeast(minimum))
                        },
                        onIncrease = {
                            val step = if (spacingMode == SplitSpacingMode.RECOVERY_GAP) 5 else 30
                            val maximum =
                                if (spacingMode == SplitSpacingMode.RECOVERY_GAP) 120 else 360
                            onGapMinutesChange((gapMinutes + step).coerceAtMost(maximum))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        onClick = onClick,
        color = if (selected) Color.White.copy(alpha = 0.1f) else DeepBackground,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (selected) Color.White else Color.Gray,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StepperChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {

    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Rounded.Remove,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                IconButton(onClick = onIncrease, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitMissionPreviewCard(generatedTasks: List<TaskBuilderState>, overflowCount: Int) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "MISSION CHAIN PREVIEW",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            generatedTasks.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            task.name,
                            color = if (task.isBreakSegment) SuccessGreen else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (task.segmentCount > 1 && !task.isBreakSegment) {
                            Text(
                                "Part ${task.segmentIndex}/${task.segmentCount}",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        "${task.startTime} - ${task.endTime}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
            if (overflowCount > 0) {
                Text(
                    "This chain needs $overflowCount more board slot${if (overflowCount == 1) "" else "s"} to fit.",
                    color = WarningAmber,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun OccupiedWindowsCard(occupiedWindows: List<OccupiedMissionWindow>) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "OCCUPIED WINDOWS",
                color = WarningAmber,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            occupiedWindows.forEach { window ->
                Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                window.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                "${TimeUtils.formatToStandard(window.start)} - ${TimeUtils.formatToStandard(window.end)}",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier =
                                Modifier.size(width = 10.dp, height = 36.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(ErrorRed)
                        )
                    }
                }
            }
        }
    }
}

private fun parseBuilderTimeOrNull(value: String): LocalTime? {
    val clean = value.trim()
    if (clean.isBlank()) return null
    val patterns = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a", "HH.mm")
    for (pattern in patterns) {
        runCatching {
            return LocalTime.parse(
                clean.uppercase(),
                DateTimeFormatter.ofPattern(pattern, Locale.US)
            )
        }
    }
    return null
}

private fun buildBuilderDisplayItems(
    tasks: List<TaskBuilderState>
): List<BuilderMissionDisplayItem> {

    if (tasks.isEmpty()) return emptyList()
    val orderedGroups = mutableListOf<BuilderMissionDisplayItem>()
    val visitedParents = mutableSetOf<String>()
    tasks.forEach { task ->
        if (task.parentMissionId.isNotBlank()) {

            if (!visitedParents.add(task.parentMissionId)) return@forEach
            val members =
                tasks
                    .filter { it.parentMissionId == task.parentMissionId }
                    .sortedBy { parseBuilderTimeOrNull(it.startTime) ?: LocalTime.MAX }
            val representative = members.firstOrNull { !it.isBreakSegment } ?: members.first()
            orderedGroups +=
                BuilderMissionDisplayItem(
                    key = task.parentMissionId,
                    representative = representative,
                    members = members
                )
        } else {
            orderedGroups +=
                BuilderMissionDisplayItem(
                    key = "single_${task.id}",
                    representative = task,
                    members = listOf(task)
                )
        }
    }
    return orderedGroups
}

private fun buildOpenSlotSuggestions(
    occupiedWindows: List<OccupiedMissionWindow>,
    preferredDurationMinutes: Int,
    anchorTime: LocalTime,
    dayEndBoundary: LocalTime = LocalTime.of(23, 0)
): List<OpenSlotSuggestion> {
    val dayStart = LocalTime.of(5, 0)
    val dayEnd = dayEndBoundary
    val sorted = occupiedWindows.sortedBy { it.start }
    val suggestions = mutableListOf<OpenSlotSuggestion>()
    var cursor = if (anchorTime.isAfter(dayStart)) anchorTime else dayStart
    fun tryAddSlot(start: LocalTime, end: LocalTime) {

        if (suggestions.size >= 3) return
        val availableMinutes = java.time.Duration.between(start, end).toMinutes().toInt()
        if (availableMinutes >= preferredDurationMinutes) {
            val slotEnd = start.plusMinutes(preferredDurationMinutes.toLong())
            suggestions +=
                OpenSlotSuggestion(
                    label =
                        if (suggestions.isEmpty()) "NEXT OPEN ${preferredDurationMinutes}M"
                        else "OPEN ${preferredDurationMinutes}M",
                    start = start,
                    end = slotEnd
                )
        }
    }
    sorted.forEach { window ->
        if (window.end <= cursor) return@forEach
        if (window.start > cursor) {
            tryAddSlot(cursor, window.start)
        }
        if (window.end > cursor) {
            cursor = window.end
        }
    }
    if (cursor < dayEnd) {
        tryAddSlot(cursor, dayEnd)
    }
    if (suggestions.isEmpty()) {
        val presets = listOf(25, 45, 60)
        presets.forEach { preset ->
            findFirstGap(occupiedWindows, preset, dayStart, dayEnd)?.let { gap ->
                suggestions +=
                    OpenSlotSuggestion(
                        label = "OPEN ${preset}M",
                        start = gap.first,
                        end = gap.second
                    )
            }
            if (suggestions.size >= 3) return@forEach
        }
    }
    return suggestions.distinctBy { "${it.start}-${it.end}" }
}

private fun buildTimeResolutionSuggestions(
    hasInvalidTimeOrder: Boolean,
    hasConflict: Boolean,
    start: LocalTime?,
    end: LocalTime?,
    preferredDurationMinutes: Int,
    conflictingWindows: List<OccupiedMissionWindow>,
    occupiedWindows: List<OccupiedMissionWindow>,
    openSlots: List<OpenSlotSuggestion>,
    sleepBlockedConflicts: List<ProtectedTimeRange>,
    bedtime: LocalTime?,
    recoveryGapSignals: List<RecoveryGapSignal>,
    dayEndBoundary: LocalTime
): List<TimeResolutionSuggestion> {
    val suggestions = mutableListOf<TimeResolutionSuggestion>()
    if (hasInvalidTimeOrder) {

        if (start != null) {
            suggestions +=
                TimeResolutionSuggestion(
                    title = "RESET END AFTER START",
                    detail =
                        "Keep the selected start and place the mission ${preferredDurationMinutes} minutes later.",
                    start = start,
                    end = start.plusMinutes(preferredDurationMinutes.toLong())
                )
        }
        if (start != null && end != null) {
            val earliest = minOf(start, end)
            val latest = maxOf(start, end)
            suggestions +=
                TimeResolutionSuggestion(
                    title = "SWAP THE WINDOW",
                    detail = "Use the earlier time as start and the later time as end.",
                    start = earliest,
                    end = latest
                )
        }
    }
    if (hasConflict) {
        sleepBlockedConflicts.firstOrNull()?.let { blocked ->
            val latestEnd = blocked.start
            if (start != null) {
                val adjustedEnd = start.plusMinutes(preferredDurationMinutes.toLong())
                if (!adjustedEnd.isAfter(latestEnd)) {
                    suggestions +=
                        TimeResolutionSuggestion(
                            title = "FINISH BEFORE SLEEP LOCK",
                            detail =
                                "Keep the start and end this mission before the Sleep Lock zone begins.",
                            start = start,
                            end = adjustedEnd
                        )
                }
            }
            findFirstGap(
                    occupiedWindows = occupiedWindows,
                    durationMinutes = preferredDurationMinutes,
                    dayStart = LocalTime.of(5, 0),
                    dayEnd = latestEnd
                )
                ?.let { resolvedGap ->
                    suggestions +=
                        TimeResolutionSuggestion(
                            title = "SHIFT BEFORE SLEEP LOCK",
                            detail =
                                "Move this mission into the nearest safe window before Sleep Lock starts${bedtime?.let { " at ${TimeUtils.formatToStandard(it)}" } ?: ""}.",
                            start = resolvedGap.first,
                            end = resolvedGap.second
                        )
                }
        }
        conflictingWindows.firstOrNull()?.let { conflict ->
            findFirstGap(
                    occupiedWindows = occupiedWindows,
                    durationMinutes = preferredDurationMinutes,
                    dayStart = conflict.end,
                    dayEnd = dayEndBoundary
                )
                ?.let { resolvedGap ->
                    suggestions +=
                        TimeResolutionSuggestion(
                            title = "START AFTER ${conflict.name.uppercase()}",
                            detail =
                                "Move this mission into the first real opening after ${conflict.name} ends.",
                            start = resolvedGap.first,
                            end = resolvedGap.second
                        )
                }
        }
        suggestions +=
            openSlots.take(3).map { slot ->
                TimeResolutionSuggestion(
                    title = slot.label,
                    detail = "Snap directly into an open, conflict-free mission window.",
                    start = slot.start,
                    end = slot.end
                )
            }
    }
    if (!hasConflict) {
        suggestions +=
            recoveryGapSignals.mapNotNull { signal ->
                val suggestionStart = signal.suggestedStart ?: return@mapNotNull null
                val suggestionEnd = signal.suggestedEnd ?: return@mapNotNull null
                TimeResolutionSuggestion(
                    title = signal.title,
                    detail = signal.detail,
                    start = suggestionStart,
                    end = suggestionEnd
                )
            }
    }
    return suggestions.distinctBy { "${it.start}-${it.end}" }
}

private fun buildRecoveryGapSignals(
    generatedWindows: List<OccupiedMissionWindow>,
    occupiedWindows: List<OccupiedMissionWindow>,
    protectedRanges: List<ProtectedTimeRange>,
    requiredGapMinutes: Int
): List<RecoveryGapSignal> {

    if (generatedWindows.isEmpty()) return emptyList()
    val signals = mutableListOf<RecoveryGapSignal>()
    val protectedStart = protectedRanges.minByOrNull { it.start }?.start
    val durationMinutes =
        generatedWindows.firstOrNull()?.let {
            java.time.Duration.between(it.start, it.end).toMinutes().toInt()
        } ?: 0
    if (durationMinutes <= 0) return emptyList()
    generatedWindows.forEach { generated ->
        val previous = occupiedWindows.filter { it.end <= generated.start }.maxByOrNull { it.end }
        val next = occupiedWindows.filter { it.start >= generated.end }.minByOrNull { it.start }
        previous?.let { prior ->
            val gap = java.time.Duration.between(prior.end, generated.start).toMinutes().toInt()
            if (gap in 0 until requiredGapMinutes) {
                val shiftedStart = prior.end.plusMinutes(requiredGapMinutes.toLong())
                val shiftedEnd = shiftedStart.plusMinutes(durationMinutes.toLong())
                val stillBeforeNext = next?.let { shiftedEnd <= it.start } ?: true
                val stillBeforeSleep = protectedStart?.let { shiftedEnd <= it } ?: true
                signals +=
                    RecoveryGapSignal(
                        title = "ADD RECOVERY BUFFER",
                        detail =
                            "This mission starts only ${gap}m after ${prior.name}. A ${requiredGapMinutes}m gap would make the schedule more realistic.",
                        suggestedStart =
                            shiftedStart.takeIf { stillBeforeNext && stillBeforeSleep },
                        suggestedEnd = shiftedEnd.takeIf { stillBeforeNext && stillBeforeSleep }
                    )
            }
        }
        next?.let { upcoming ->
            val gap = java.time.Duration.between(generated.end, upcoming.start).toMinutes().toInt()
            if (gap in 0 until requiredGapMinutes) {
                val shiftedEnd = upcoming.start.minusMinutes(requiredGapMinutes.toLong())
                val shiftedStart = shiftedEnd.minusMinutes(durationMinutes.toLong())
                val afterPrevious = previous?.let { shiftedStart >= it.end } ?: true
                signals +=
                    RecoveryGapSignal(
                        title = "LEAVE A RESET GAP",
                        detail =
                            "This mission ends only ${gap}m before ${upcoming.name}. A ${requiredGapMinutes}m reset gap would reduce compression.",
                        suggestedStart = shiftedStart.takeIf { afterPrevious },
                        suggestedEnd = shiftedEnd.takeIf { afterPrevious }
                    )
            }
        }
    }
    return signals.distinctBy { it.detail }
}

private fun buildSplitMissionPlan(
    baseName: String,
    type: MissionType,
    focusTaskType: FocusTaskType,
    focusModeEnabled: Boolean,
    focusPreset: FocusPreset,
    packageName: String?,
    importance: Int,
    firstStart: LocalTime,
    firstEnd: LocalTime,
    parts: Int,
    gapMinutes: Int,
    spacingMode: SplitSpacingMode,
    parentMissionId: String
): List<TaskBuilderState> {
    val segmentMinutes = java.time.Duration.between(firstStart, firstEnd).toMinutes().toInt()
    if (segmentMinutes <= 0 || parts <= 1) return emptyList()
    val tasks = mutableListOf<TaskBuilderState>()
    var cursor = firstStart
    repeat(parts) { index ->
        val partNumber = index + 1
        val partEnd = cursor.plusMinutes(segmentMinutes.toLong())
        tasks +=
            TaskBuilderState(
                id = (System.currentTimeMillis() + index).toInt(),
                name = "$baseName · Part $partNumber/$parts",
                startTime = TimeUtils.formatToStandard(cursor),
                endTime = TimeUtils.formatToStandard(partEnd),
                importanceRank = importance,
                type = type,
                focusTaskType = focusTaskType,
                focusModeEnabled = focusModeEnabled,
                focusPreset = focusPreset,
                parentMissionId = parentMissionId,
                segmentBaseName = baseName,
                segmentIndex = partNumber,
                segmentCount = parts,
                isBreakSegment = false,
                packageName = packageName
            )
        if (partNumber < parts && gapMinutes > 0) {

            if (spacingMode == SplitSpacingMode.RECOVERY_GAP && gapMinutes > 0) {
                val breakEnd = partEnd.plusMinutes(gapMinutes.toLong())
                tasks +=
                    TaskBuilderState(
                        id = (System.currentTimeMillis() + 10_000 + index).toInt(),
                        name = "$baseName · Recovery Gap",
                        startTime = TimeUtils.formatToStandard(partEnd),
                        endTime = TimeUtils.formatToStandard(breakEnd),
                        importanceRank = 3,
                        type = MissionType.PHYSICAL,
                        focusTaskType = FocusTaskType.PHYSICAL,
                        focusModeEnabled = false,
                        focusPreset = focusPreset,
                        parentMissionId = parentMissionId,
                        segmentBaseName = baseName,
                        segmentIndex = partNumber,
                        segmentCount = parts,
                        isBreakSegment = true,
                        packageName = null
                    )
                cursor = breakEnd
            } else {
                cursor = partEnd.plusMinutes(gapMinutes.toLong())
            }
        } else {
            cursor = partEnd
        }
    }
    return tasks
}

private fun resolveSplitChainDefaults(
    initialTask: TaskBuilderState?,
    builderTasks: List<TaskBuilderState>
): SplitChainDefaults {

    if (
        initialTask == null ||
            initialTask.parentMissionId.isBlank() ||
            initialTask.segmentCount <= 1
    ) {
        return SplitChainDefaults()
    }
    val chain =
        builderTasks
            .filter { it.parentMissionId == initialTask.parentMissionId }
            .sortedBy { parseBuilderTimeOrNull(it.startTime) ?: LocalTime.MAX }
    val focusParts = chain.filterNot { it.isBreakSegment }
    val breakTask = chain.firstOrNull { it.isBreakSegment }
    val spacingMode =
        if (breakTask != null) SplitSpacingMode.RECOVERY_GAP else SplitSpacingMode.AFTER_DELAY
    val gapMinutes =
        when {
            breakTask != null -> {
                val breakStart = parseBuilderTimeOrNull(breakTask.startTime)
                val breakEnd = parseBuilderTimeOrNull(breakTask.endTime)
                if (breakStart != null && breakEnd != null)
                    ChronoUnit.MINUTES.between(breakStart, breakEnd).toInt()
                else 15
            }
            focusParts.size >= 2 -> {
                val firstEnd = parseBuilderTimeOrNull(focusParts[0].endTime)
                val secondStart = parseBuilderTimeOrNull(focusParts[1].startTime)
                if (firstEnd != null && secondStart != null)
                    ChronoUnit.MINUTES.between(firstEnd, secondStart).toInt().coerceAtLeast(30)
                else 180
            }
            else -> 15
        }
    return SplitChainDefaults(
        enabled = true,
        partCount = focusParts.size.coerceAtLeast(2),
        gapMinutes = gapMinutes,
        spacingMode = spacingMode
    )
}

private fun formatGapValue(minutes: Int, spacingMode: SplitSpacingMode): String {
    return if (spacingMode == SplitSpacingMode.RECOVERY_GAP) {
        "${minutes}m"
    } else {
        val hours = minutes / 60
        val remainder = minutes % 60
        if (remainder == 0) "${hours}h" else "${hours}h ${remainder}m"
    }
}

private fun findFirstGap(
    occupiedWindows: List<OccupiedMissionWindow>,
    durationMinutes: Int,
    dayStart: LocalTime,
    dayEnd: LocalTime
): Pair<LocalTime, LocalTime>? {
    var cursor = dayStart
    occupiedWindows
        .sortedBy { it.start }
        .forEach { window ->
            if (window.end <= cursor) return@forEach
            val gapMinutes = java.time.Duration.between(cursor, window.start).toMinutes().toInt()
            if (gapMinutes >= durationMinutes) {
                return cursor to cursor.plusMinutes(durationMinutes.toLong())
            }
            if (window.end > cursor) cursor = window.end
        }
    val tailGap = java.time.Duration.between(cursor, dayEnd).toMinutes().toInt()
    return if (tailGap >= durationMinutes) {
        cursor to cursor.plusMinutes(durationMinutes.toLong())
    } else {
        null
    }
}

@Composable
private fun TimeResolutionSuggestionsCard(
    hasInvalidTimeOrder: Boolean,
    hasConflict: Boolean,
    suggestions: List<TimeResolutionSuggestion>,
    onApply: (TimeResolutionSuggestion) -> Unit
) {
    val accent =
        when {
            hasConflict || hasInvalidTimeOrder -> WarningAmber
            else -> NeonCyan
        }
    val title =
        when {
            hasInvalidTimeOrder -> "FIX THIS TIME WINDOW"
            hasConflict -> "RESOLVE THE OVERLAP"
            else -> "QUICK TIME FIXES"
        }
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            suggestions.forEach { suggestion ->
                Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                suggestion.title,
                                color = accent,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                suggestion.detail,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${TimeUtils.formatToStandard(suggestion.start)} - ${TimeUtils.formatToStandard(suggestion.end)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        TextButton(onClick = { onApply(suggestion) }) {
                            Text("USE THIS", color = accent, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenSlotSuggestionsCard(
    suggestions: List<OpenSlotSuggestion>,
    onApply: (OpenSlotSuggestion) -> Unit
) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "OPEN WINDOWS",
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            suggestions.forEach { suggestion ->
                Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                suggestion.label,
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${TimeUtils.formatToStandard(suggestion.start)} - ${TimeUtils.formatToStandard(suggestion.end)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        TextButton(onClick = { onApply(suggestion) }) {
                            Text("SNAP", color = NeonCyan, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscardChangesDialog(onDismiss: () -> Unit, onDiscard: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        title = { Text("UNSAVED BLUEPRINT", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Text(
                "You still have unsaved mission changes. Leave now and discard them, or stay and keep architecting.",
                color = Color.Gray
            )
        },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text("DISCARD", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("KEEP EDITING", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TemplateGrid(templates: List<MissionTemplate>, onSelect: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2
    ) {
        templates.forEach { template ->
            Surface(
                onClick = { onSelect(template.name) },
                color = DeepBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.widthIn(min = 0.dp).weight(1f, fill = true)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        template.icon,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        template.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AppInvestGrid(apps: List<InvestableApp>, onSelect: (String, String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 3
    ) {
        apps.forEach { app ->
            Column(
                modifier =
                    Modifier.widthIn(min = 88.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .gamifiedClick { onSelect(app.name, app.packageName) }
                        .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIconImage(app.packageName, Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    app.name,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TimeSelectionCard(
    label: String,
    time: String,
    isSelected: Boolean,
    hasError: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val borderColor =
        when {
            hasError -> ErrorRed
            isSelected -> Color.White
            else -> Color.White.copy(alpha = 0.05f)
        }
    val containerColor =
        when {
            hasError -> SurfaceDark
            isSelected -> Color.White.copy(alpha = 0.1f)
            else -> DeepBackground
        }
    val timeColor =
        when {
            hasError -> WarningAmber
            isSelected -> Color.White
            else -> Color.LightGray
        }
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                fontSize = 10.sp,
                color = if (hasError) WarningAmber else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Text(time, fontSize = 18.sp, color = timeColor, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun DurationPresetRow(onSelectMinutes: (Int) -> Unit) {
    val presets = listOf(25, 45, 60, 90)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { preset ->
            Surface(
                onClick = { onSelectMinutes(preset) },
                color = DeepBackground,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${preset}m",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityCard(
    rank: Int,
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        color = if (isSelected) color.copy(alpha = 0.15f) else DeepBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) color else Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                fontSize = 9.sp,
                color = if (isSelected) color else Color.Gray,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector =
                    when (rank) {
                        1 -> Icons.Rounded.KeyboardDoubleArrowUp
                        2 -> Icons.Rounded.KeyboardArrowUp
                        else -> Icons.AutoMirrored.Rounded.TrendingUp
                    },
                contentDescription = null,
                tint = if (isSelected) color else Color.DarkGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TypeSelectionButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        color = if (isSelected) Color.White.copy(alpha = 0.1f) else DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border =
            BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.05f)),
        modifier = modifier.height(70.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun PrepTrackHintCard(track: FocusTaskType) {
    val detail =
        when (track) {
            FocusTaskType.DSA -> "One problem. One clean attempt. Keep the rep small and sharp."
            FocusTaskType.SYSTEM_DESIGN ->
                "Take one system prompt and sketch only the core architecture and tradeoffs."
            FocusTaskType.BEHAVIORAL ->
                "Answer one interview question out loud. This is the most direct confidence rep."
            FocusTaskType.PROJECT_REVIEW ->
                "Explain one real IronMind decision like you're already in the interview."
            else -> "Prep works best when it stays focused and repeatable."
        }
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "PREP REP",
                color = ElectricViolet,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                detail,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun FocusTaskTypeChip(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {

    Surface(
        onClick = onClick,
        color = if (isSelected) TextPrimary.copy(alpha = 0.16f) else DeepBackground,
        shape = RoundedCornerShape(14.dp),
        border =
            BorderStroke(
                1.dp,
                if (isSelected) TextPrimary else Color.White.copy(alpha = 0.06f)
            ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) TextPrimary else Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
fun FocusModeSettingsCard(
    enabled: Boolean,
    canEnable: Boolean,
    preset: FocusPreset,
    onEnabledChange: (Boolean) -> Unit,
    onPresetChange: (FocusPreset) -> Unit
) {

    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FOCUS MODE",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text =
                            if (canEnable) {
                                "Runs Pomodoro inside this task window."
                            } else {
                                "Physical tasks keep their natural rhythm."
                            },
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
                Switch(
                    checked = enabled && canEnable,
                    onCheckedChange = { onEnabledChange(it) },
                    enabled = canEnable
                )
            }
            if (enabled && canEnable) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FocusPresetChip(
                        label = "25 / 5",
                        subtitle = "Classic",
                        selected = preset == FocusPreset.CLASSIC_25_5,
                        modifier = Modifier.weight(1f)
                    ) {
                        onPresetChange(FocusPreset.CLASSIC_25_5)
                    }
                    FocusPresetChip(
                        label = "50 / 10",
                        subtitle = "Deep",
                        selected = preset == FocusPreset.DEEP_50_10,
                        modifier = Modifier.weight(1f)
                    ) {
                        onPresetChange(FocusPreset.DEEP_50_10)
                    }
                    FocusPresetChip(
                        label = "15 / 5",
                        subtitle = "Quick",
                        selected = preset == FocusPreset.QUICK_15_5,
                        modifier = Modifier.weight(1f)
                    ) {
                        onPresetChange(FocusPreset.QUICK_15_5)
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusPresetChip(
    label: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        color =
            if (selected) ElectricViolet.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border =
            BorderStroke(
                1.dp,
                if (selected) ElectricViolet else Color.White.copy(alpha = 0.07f)
            ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else Color.LightGray,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle.uppercase(Locale.getDefault()),
                color = if (selected) TextPrimary else Color.Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun TemplateGridPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        TemplateGrid(
            templates = listOf(
                MissionTemplate("Morning Workout", androidx.compose.material.icons.Icons.Rounded.DirectionsRun, com.sanket_satpute_20.ironmind.onboarding.MissionType.PHYSICAL),
                MissionTemplate("Deep Work", androidx.compose.material.icons.Icons.Rounded.Star, com.sanket_satpute_20.ironmind.onboarding.MissionType.DEEP_WORK),
                MissionTemplate("Digital Detox", androidx.compose.material.icons.Icons.Rounded.Lock, com.sanket_satpute_20.ironmind.onboarding.MissionType.APP_INVEST)
            ),
            onSelect = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
fun TaskBuilderScreenPreview() {
    if (!androidx.compose.ui.platform.LocalInspectionMode.current) return
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme { TaskBuilderPreviewContent() }
}

@Composable
private fun TaskBuilderPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("BUILD YOUR DAY", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("Choose a mission template, then make it real.", color = Color.Gray)
        Surface(color = SurfaceDark, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DEEP WORK", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text("System Design Sprint", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("09:00 — 10:30 · 90 min", color = Color.Gray)
            }
        }
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("ADD MISSION", fontWeight = FontWeight.Black)
        }
    }
}
