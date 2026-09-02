package com.sanket_satpute_20.ironmind.home

import com.sanket_satpute_20.ironmind.gamification.ConfettiOverlay

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.sanket_satpute_20.ironmind.gamification.gamifiedClick
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.focus.PomodoroEngine
import com.sanket_satpute_20.ironmind.focus.PomodoroChamberActivity
import com.sanket_satpute_20.ironmind.focus.PomodoroPreset
import com.sanket_satpute_20.ironmind.focus.PomodoroSessionConfig
import com.sanket_satpute_20.ironmind.focus.PomodoroSessionSource
import com.sanket_satpute_20.ironmind.focus.WorkStartActivity
import com.sanket_satpute_20.ironmind.focus.WorkLockActivity
import com.sanket_satpute_20.ironmind.focus.getFocusColor
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionActivity
import com.sanket_satpute_20.ironmind.integrity.IronStrictnessManager
import com.sanket_satpute_20.ironmind.integrity.IronStrictnessProfile
import com.sanket_satpute_20.ironmind.integrity.IntegrityShieldViewModel
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine
import com.sanket_satpute_20.ironmind.psychology.UserType
import com.sanket_satpute_20.ironmind.product.CurrentVersionScope
import com.sanket_satpute_20.ironmind.utils.PermissionHelper
import com.sanket_satpute_20.ironmind.utils.PermissionPriority
import com.sanket_satpute_20.ironmind.crucible.CrucibleManager
import com.sanket_satpute_20.ironmind.crucible.CrucibleRepository
import com.sanket_satpute_20.ironmind.challenge.ChallengeManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionActivity
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionScheduler
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionStatus
import com.sanket_satpute_20.ironmind.nightdecision.clearNightDecisionTasksWithHistory
import com.sanket_satpute_20.ironmind.ui.components.LiveIntegrityShieldCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit
import java.util.Locale
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    viewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onTaskSkipped: () -> Unit,
    onStatsClick: () -> Unit,
    onWeeklyScoreClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTemptationLogClick: () -> Unit,
    onGraveyardClick: () -> Unit,
    onPermissionCenterClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onIgnitionHistoryClick: () -> Unit,
    onVoiceLogsClick: () -> Unit,
    onVoiceRepClick: () -> Unit,
    onPrepBuilderClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onEmergencyValveClick: () -> Unit,
    onIdentitySetupClick: () -> Unit,
    onCommitmentContractClick: () -> Unit,
    onDistractionsSetupClick: () -> Unit,
    onCrucibleClick: (String) -> Unit,
    onCrucibleWon: (String) -> Unit,
    onCrucibleFailed: (String) -> Unit,
    onClubChatClick: () -> Unit,
    onIronCircleClick: () -> Unit,
    onStreakMilestone: (Int) -> Unit = {},
    onAccountLinkClick: () -> Unit = {},
    onDailyCompleteClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val integrityShieldViewModel: IntegrityShieldViewModel = viewModel()
    val shieldUiState by integrityShieldViewModel.uiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val homeMissionItems by viewModel.homeMissionItems.collectAsState()
    
    val streak = viewModel.prefManager.streakCount
    
    val currentXp by viewModel.totalXp.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val levelUpEvent by viewModel.levelUpEvent.collectAsState()
    val milestoneReached by viewModel.milestoneReached.collectAsState()
    val crucibleWonEvent by viewModel.crucibleWonEvent.collectAsState()
    val crucibleFailedEvent by viewModel.crucibleFailedEvent.collectAsState()
    val newArtifactEvent by viewModel.newArtifactUnlocked.collectAsState()
    val bossModeEvent by viewModel.bossModeUpgradeEvent.collectAsState()
    
    val bossModeTrigger by viewModel.bossModeTrigger.collectAsState()
    val bossModeActive = viewModel.prefManager.bossModeActive
    
    val userType = AdaptiveEngine.getCurrentType(viewModel.prefManager)
    val currentMode = AdaptiveEngine.getCurrentMode(viewModel.prefManager)
    val ironStrictnessProfile = remember(currentMode) {
        if (currentMode == AppMode.IRON) {
            IronStrictnessManager(context).getTodayProfile(LocalDate.now())
        } else {
            null
        }
    }
    val identityTitle = IdentityLevelEngine.getIdentityTitle(userType, currentLevel)
    val nextLevelProgress = IdentityLevelEngine.getProgressToNextLevel(currentXp)

    val anyTaskInProgress = tasks.any { it.isInProgress && !it.isBreakSegment }
    val focusTasks = tasks.filterNot { it.isBreakSegment }
    val hasUnresolvedFocusTasks = focusTasks.any { !it.isCompleted && !it.isSkipped }
    val allDone = focusTasks.isNotEmpty() && focusTasks.all { it.isCompleted }
    val doNowMission = remember(homeMissionItems) { selectDoNowMission(homeMissionItems) }
    val nightDecisionManager = remember { NightDecisionManager(context) }


    var showEnergyCheckIn by remember {
        mutableStateOf(
            AdaptiveEngine.needsEnergyCheckIn(viewModel.prefManager) && hasUnresolvedFocusTasks
        )
    }
    
    var showSchedule by rememberSaveable { mutableStateOf(true) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryBudget by remember { mutableIntStateOf(viewModel.prefManager.recoveryDaysBudget) }
    var isRecoveryDayActive by remember { mutableStateOf(viewModel.prefManager.isRecoveryDayActive) }
    
    var pendingNavigationAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val guardNavigation: (() -> Unit) -> Unit = { action ->
        if (anyTaskInProgress) pendingNavigationAction = action else action()
    }
    val startPomodoroForTask: (Task) -> Unit = { task ->
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playTick()
        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTimerTick()
        startPomodoroForTask(context, task)
    }
    val startCountdownForTask: (Task) -> Unit = { task ->
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playTick()
        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTimerTick()
        startCountdownForTask(
            context = context,
            task = task,
            onVoiceRepClick = onVoiceRepClick,
            guardNavigation = guardNavigation
        )
    }
    val openLiveTask: (Task) -> Unit = { task ->
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playTick()
        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTimerTick()
        openLiveTask(context, task)
    }
    
    val startQuickFocus: () -> Unit = {
        val quickTask = Task(
            id = -1,
            name = "Quick Focus",
            focusModeEnabled = true
        )
        startPomodoroForTask(context, quickTask)
    }

    var nowDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    val currentTime = nowDateTime.toLocalTime()
    val tomorrowDate = nowDateTime.toLocalDate().plusDays(1)
    LaunchedEffect(Unit) {
        ChallengeManager.reconcileChallengeState(context)
        NightDecisionManager(context).resetIfDayRolled()
        NightDecisionScheduler.schedule(context)
    }
    LaunchedEffect(Unit) {
        if (viewModel.prefManager.firstHomeArrivalDate.isBlank()) {
            viewModel.prefManager.firstHomeArrivalDate = LocalDate.now().toString()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowDateTime = LocalDateTime.now()
        }
    }
    LaunchedEffect(tasks) {
        showEnergyCheckIn =
            AdaptiveEngine.needsEnergyCheckIn(viewModel.prefManager) && hasUnresolvedFocusTasks
    }

    var resolvedExpanded by remember { mutableStateOf(false) }
    var tomorrowNightDecisionStatus by remember { mutableStateOf(nightDecisionManager.statusForDate(tomorrowDate)) }
    var showEmergencyLeaveDialog by remember { mutableStateOf(false) }
    var emergencyLeaveReason by remember { mutableStateOf("") }
    val uiScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, tomorrowDate) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                nowDateTime = LocalDateTime.now()
                tomorrowNightDecisionStatus =
                    nightDecisionManager.statusForDate(nowDateTime.toLocalDate().plusDays(1))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(tomorrowDate) {
        tomorrowNightDecisionStatus = nightDecisionManager.statusForDate(tomorrowDate)
    }

    LaunchedEffect(crucibleWonEvent) {
        crucibleWonEvent?.let { id ->
            onCrucibleWon(id)
            viewModel.clearCrucibleWon()
        }
    }

    LaunchedEffect(crucibleFailedEvent) {
        crucibleFailedEvent?.let { title ->
            onCrucibleFailed(title)
            viewModel.clearCrucibleFailed()
        }
    }

    LaunchedEffect(milestoneReached) {
        milestoneReached?.let { streakDays ->
            if (CurrentVersionScope.SHOW_SHAREABLE_CARDS) {
                onStreakMilestone(streakDays)
            }
            viewModel.clearMilestone()
        }
    }

    val dailyComplete by viewModel.dailyCompleteEvent.collectAsState()
    LaunchedEffect(dailyComplete) {
        if (dailyComplete) {
            onDailyCompleteClick()
            viewModel.clearDailyComplete()
        }
    }

    var showFloatingXP by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(HomeCanvas)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. GAMIFIED HEADER
            GamifiedHeader(
                level = currentLevel,
                progress = nextLevelProgress,
                totalXp = currentXp,
                streak = streak,
                identityTitle = identityTitle,
                currentMode = currentMode,
                  modifier = Modifier
                      .statusBarsPadding()
                      .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                onGraveyardClick = { if (anyTaskInProgress) pendingNavigationAction = onGraveyardClick else onGraveyardClick() },
                                onSettingsClick = { if (anyTaskInProgress) pendingNavigationAction = onSettingsClick else onSettingsClick() },
                onStatsClick = { if (anyTaskInProgress) pendingNavigationAction = onStatsClick else onStatsClick() }
            )

            // 2. UNIFIED SCROLLABLE AREA
            com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry(
                delayMillis = 150,
                durationMillis = 800,
                modifier = Modifier.weight(1f) // Ensure it fills remaining space in Column
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LiveIntegrityShieldCard(
                        state = shieldUiState,
                        onEventHandled = integrityShieldViewModel::onEventHandled
                    )
                }

                HomeFrictionlessSection(
                    doNowMission = doNowMission,
                    showSchedule = showSchedule,
                    onToggleSchedule = { showSchedule = !showSchedule },
                    recoveryBudget = recoveryBudget,
                    isRecoveryDayActive = isRecoveryDayActive,
                    onDeclareRecoveryDayClick = { showRecoveryDialog = true },
                    ironStrictnessProfile = ironStrictnessProfile,
                    tasks = tasks,
                    homeMissionItems = homeMissionItems,
                    currentTime = currentTime,
                    userType = userType,
                    allDone = allDone,
                    resolvedExpanded = resolvedExpanded,
                    onResolvedExpandedChange = {
                        resolvedExpanded = it
                        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playTick()
                        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTimerTick()
                    },
                    onAddTaskClick = {
                        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playTick()
                        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTimerTick()
                        guardNavigation(onAddTaskClick)
                    },
                    onDone = { task ->
                        viewModel.markDone(task)
                        showFloatingXP = true
                        showConfetti = true
                        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playSuccess(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTaskComplete()
                    },
                    onSkip = {
                        viewModel.markSkipped(it)
                        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playError()
                        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTemptationBlocked()
                        onTaskSkipped()
                    },
                    onDoneChain = { chain ->
                        viewModel.markChainDone(chain)
                        showFloatingXP = true
                        showConfetti = true
                        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playSuccess(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTaskComplete()
                    },
                    onSkipChain = {
                        viewModel.markChainSkipped(it)
                        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playError()
                        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTemptationBlocked()
                        onTaskSkipped()
                    },
                    onStartCountdown = startCountdownForTask,
                    onStartPomodoro = startPomodoroForTask,
                    onOpenLiveTask = openLiveTask,
                    onStartQuickFocus = {
                        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playTick()
                        com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTimerTick()
                        startQuickFocus()
                    }
                )

                // Final Spacing
                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
            }
        }

        HomeOverlayHost(
            prefManager = viewModel.prefManager,
            userType = userType,
            bossModeTrigger = bossModeTrigger && CurrentVersionScope.SHOW_BOSS_MODE,
            onDismissBossModeTrigger = viewModel::clearBossModeTrigger,
            pendingNavigationAction = pendingNavigationAction,
            onDismissPendingNavigation = { pendingNavigationAction = null },
            onConfirmPendingNavigation = { action ->
                pendingNavigationAction = null
                action()
            },
            showEmergencyLeaveDialog = showEmergencyLeaveDialog,
            emergencyLeaveReason = emergencyLeaveReason,
            onEmergencyLeaveReasonChange = { emergencyLeaveReason = it },
            onDismissEmergencyLeaveDialog = { showEmergencyLeaveDialog = false },
            onConfirmEmergencyLeave = {
                showEmergencyLeaveDialog = false
                uiScope.launch {
                    clearNightDecisionTasksWithHistory(
                        context,
                        tomorrowDate,
                        "NIGHT_DECISION_EMERGENCY_LEAVE_HOME"
                    )
                    nightDecisionManager.markEmergencyLeave(emergencyLeaveReason.trim().ifBlank { null })
                    NightDecisionScheduler.schedule(context)
                    tomorrowNightDecisionStatus = nightDecisionManager.statusForDate(tomorrowDate)
                    emergencyLeaveReason = ""
                }
            },
            showEnergyCheckIn = showEnergyCheckIn,
            onEnergyCheckInComplete = { showEnergyCheckIn = false },
            levelUpEvent = levelUpEvent,
            onDismissLevelUp = viewModel::clearLevelUp,
            newArtifactEvent = newArtifactEvent.takeIf { CurrentVersionScope.SHOW_ARTIFACTS },
            onDismissArtifact = viewModel::clearArtifactUnlocked,
            bossModeEvent = bossModeEvent.takeIf { CurrentVersionScope.SHOW_BOSS_MODE },
            onDismissBossModeUpgrade = viewModel::clearBossModeUpgrade
        )

        if (showRecoveryDialog) {
            com.sanket_satpute_20.ironmind.settings.RecoveryDayOverlay(
                remainingDays = recoveryBudget,
                onActivate = {
                    if (recoveryBudget > 0) {
                        viewModel.prefManager.recoveryDaysBudget = recoveryBudget - 1
                        viewModel.prefManager.recoveryDayTimestamp = System.currentTimeMillis()
                        recoveryBudget -= 1
                        viewModel.prefManager.isRecoveryDayActive = true
                        isRecoveryDayActive = true
                    }
                    showRecoveryDialog = false
                },
                onDismiss = { showRecoveryDialog = false }
            )
        }

        if (showFloatingXP) {
            com.sanket_satpute_20.ironmind.ui.components.FloatingXP(
                text = "+15 XP",
                onAnimationEnd = { showFloatingXP = false }
            )
        }

        ConfettiOverlay(
            isVisible = showConfetti,
            onAnimationEnd = { showConfetti = false }
        )
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun HomeScreenPreview() {
    if (!androidx.compose.ui.platform.LocalInspectionMode.current) return
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HomeScreenPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun HomeScreenPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            GamifiedHeader(
                level = 7,
                progress = 0.65f,
                totalXp = 6_200,
                streak = 21,
                identityTitle = "Iron Operator",
                currentMode = com.sanket_satpute_20.ironmind.psychology.AppMode.IRON,
                onGraveyardClick = {},
                onSettingsClick = {},
                onStatsClick = {}
            )
            val previewTask = Task(
                id = 1,
                name = "Finish the launch brief",
                startTime = "10:00",
                endTime = "11:30",
                focusModeEnabled = true
            )
            HomeDailyCommandCenter(
                doNowMission = HomeMissionItem(previewTask, HomeMissionState.UP_NEXT, "UP NEXT"),
                tasks = listOf(previewTask, Task(id = 2, name = "Strength training", isCompleted = true)),
                allDone = false,
                isRecoveryDayActive = false,
                onPrimaryAction = {},
                onAddMission = {},
                onQuickFocus = {}
            )
        }
    }
}
