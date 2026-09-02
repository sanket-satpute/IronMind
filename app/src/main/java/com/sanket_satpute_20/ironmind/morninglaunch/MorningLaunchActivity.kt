package com.sanket_satpute_20.ironmind.morninglaunch

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.challenge.ChallengeManager
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.DailyIntegrityRecord
import com.sanket_satpute_20.ironmind.integrity.DailyIntegrityEngine
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.psychology.HonestyOverridePanel
import com.sanket_satpute_20.ironmind.ui.components.ShieldMode
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class MorningLaunchActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_CODE_REMINDER = 5107
        private const val FIVE_MINUTES_MS = 5L * 60L * 1000L
        private const val EXTRA_FORCE_TEST = "extra_force_test"

        fun createIntent(context: Context, forceTest: Boolean = false): Intent =
            Intent(context, MorningLaunchActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra(EXTRA_FORCE_TEST, forceTest)
            }

        fun scheduleSnoozeReminder(
            context: Context,
            triggerAtMillis: Long,
            forceTest: Boolean = false
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                return
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_REMINDER,
                createIntent(context, forceTest = forceTest),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }

        fun cancelSnoozeReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_REMINDER,
                createIntent(context),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelSnoozeReminder(this)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        onBackPressedDispatcher.addCallback(this) { }

        setContent {
            IronMindTheme {
                MorningLaunchRoute(
                    onFinished = { finish() }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val prefs = PrefManager.getInstance(this)
        if (!isFinishing && prefs.morningLaunchActive) {
            startActivity(createIntent(this, forceTest = intent.getBooleanExtra(EXTRA_FORCE_TEST, false)))
        }
    }

    override fun onStop() {
        super.onStop()
        val prefs = PrefManager.getInstance(this)
        if (!isFinishing && !isChangingConfigurations && prefs.morningLaunchActive) {
            startActivity(createIntent(this, forceTest = intent.getBooleanExtra(EXTRA_FORCE_TEST, false)))
        }
    }

    private fun scheduleFiveMinuteReminder() {
        scheduleSnoozeReminder(
            context = this,
            triggerAtMillis = System.currentTimeMillis() + FIVE_MINUTES_MS,
            forceTest = intent.getBooleanExtra(EXTRA_FORCE_TEST, false)
        )
    }

    @Composable
    private fun MorningLaunchRoute(
        onFinished: () -> Unit
    ) {
        val context = this
        val prefs = remember { PrefManager.getInstance(context) }
        val manager = remember { MorningLaunchManager(context) }
        val coroutineScope = rememberCoroutineScope()
        val forceTest = remember { intent.getBooleanExtra(EXTRA_FORCE_TEST, false) }
        var ready by remember { mutableStateOf(false) }
        var launchStarted by remember { mutableStateOf(false) }
        var mode by remember { mutableStateOf(manager.getMode()) }
        var shieldMode by remember { mutableStateOf(prefs.shieldMode()) }
        var steps by remember { mutableStateOf(manager.getSteps()) }
        var emergencyRemaining by remember { mutableStateOf(manager.getRemainingEmergencyExits()) }
        val ritualViewModel: MorningRitualViewModel = viewModel()
        val ritualState by ritualViewModel.uiState.collectAsState()
        var yesterdayRecord by remember { mutableStateOf<DailyIntegrityRecord?>(null) }

        val handleEmergencyExit: () -> Unit = {
            coroutineScope.launch {
                if (manager.useEmergencyExit("USER_EMERGENCY_EXIT")) {
                    emergencyRemaining = manager.getRemainingEmergencyExits()
                    onFinished()
                }
            }
        }

        LaunchedEffect(Unit) {
            val currentStage = manager.reconcileRuntimeState()
            val hasOpenSession = prefs.morningLaunchSessionId > 0L &&
                currentStage != MorningLaunchStage.IDLE

            if (!hasOpenSession) {
                if (!forceTest && !manager.shouldTriggerAfterAlarmDismiss()) {
                    onFinished()
                    return@LaunchedEffect
                }
                manager.beginPrompting(source = if (forceTest) "TEST_LAUNCH" else "CHALLENGE_ALARM")
            }

            mode = manager.getMode()
            steps = manager.getSteps()
            launchStarted = currentStage == MorningLaunchStage.IN_PROGRESS
            ready = true
        }

        LaunchedEffect(ritualState.currentStep) {
            if (ritualState.currentStep == RitualStep.YESTERDAY_RECAP && yesterdayRecord == null) {
                yesterdayRecord = ritualViewModel.getYesterdayRecord()
            }
        }

        if (!ready) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WarningAmber)
            }
            return
        }

        if (launchStarted) {
            val onComplete: () -> Unit = {
                coroutineScope.launch {
                    manager.markCompleted(steps.size)
                    if (!forceTest) {
                        completeChallengeDay(prefs)
                    } else {
                        manager.clearRuntimeState(keepDayOutcome = true)
                    }
                    armMorningBonus(prefs)
                    ritualViewModel.resetRitual()
                    openHome()
                    onFinished()
                }
            }

            when (mode) {
                MorningLaunchMode.MONK -> MorningMonkFlow(
                    steps = steps,
                    onStepChanged = manager::updateCurrentStep,
                    emergencyRemaining = emergencyRemaining,
                    onEmergencyExit = handleEmergencyExit,
                    onComplete = onComplete
                )
                MorningLaunchMode.WARRIOR -> MorningWarriorFlow(
                    steps = steps,
                    onStepChanged = manager::updateCurrentStep,
                    emergencyRemaining = emergencyRemaining,
                    onEmergencyExit = handleEmergencyExit,
                    onComplete = onComplete
                )
                MorningLaunchMode.BALANCED -> MorningBalancedFlow(
                    steps = steps,
                    onStepChanged = manager::updateCurrentStep,
                    emergencyRemaining = emergencyRemaining,
                    onEmergencyExit = handleEmergencyExit,
                    onComplete = onComplete
                )
            }
            return
        }

        if (ritualState.currentStep == RitualStep.YESTERDAY_RECAP) {
            val record = yesterdayRecord
            if (record == null) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = WarningAmber) }
                return
            }
            MorningLaunchRecapScreen(
                yesterdayFillLevel = record.scorePercent / 100f,
                yesterdayCracks = record.shieldState,
                mode = record.shieldModeOr(shieldMode),
                onContinue = { ritualViewModel.advanceTo(RitualStep.TASK_COMMITMENT) }
            )
            return
        }

        if (ritualState.currentStep == RitualStep.TASK_COMMITMENT) {
            val tasks by ritualViewModel.todayTasks.collectAsState(initial = emptyList())
            MorningLaunchTaskCommitmentScreen(
                mode = shieldMode,
                tasks = tasks,
                selectedTaskIds = ritualState.selectedTaskIds,
                onTaskSelected = { ritualViewModel.selectTask(it) },
                onCommit = { ritualViewModel.advanceTo(RitualStep.OATH_HOLD) }
            )
            return
        }

        if (ritualState.currentStep == RitualStep.OATH_HOLD) {
            val level = remember { com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine.getLevelFromXp(prefs.totalXp) }
            MorningLaunchOathScreen(
                mode = shieldMode,
                level = level,
                onOathSworn = { ritualViewModel.completeOath() }
            )
            return
        }

        if (ritualState.currentStep == RitualStep.MODE_RECOMMENDATION) {
            MorningLaunchModeConfirmScreen(
                currentMode = ritualState.recommendedMode?.let { ShieldMode.valueOf(it) } ?: shieldMode,
                recommendationReason = ritualState.recommendationReason,
                onModeConfirmed = { confirmedMode ->
                    AdaptiveEngine.switchMode(
                        prefs = prefs,
                        newMode = AppMode.valueOf(confirmedMode.name),
                        isManual = true,
                        context = context,
                        source = "MORNING_LAUNCH_MODE_CONFIRM"
                    )
                    shieldMode = confirmedMode
                    coroutineScope.launch {
                        manager.markStartedImmediately()
                        launchStarted = true
                        ritualViewModel.confirmMode(confirmedMode.name)
                        ritualViewModel.finishRitual()
                    }
                }
            )
            return
        }

        if (ritualState.currentStep == RitualStep.WAKE_ENTRY) {
            MorningLaunchGateScreen(
            mode = mode,
            steps = steps,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = handleEmergencyExit,
            onStart = {
                ritualViewModel.advanceTo(RitualStep.YESTERDAY_RECAP)
            },
            onRemindFive = {
                coroutineScope.launch {
                    manager.markSnoozedFiveMinutes()
                    scheduleFiveMinuteReminder()
                    onFinished()
                }
            },
            onSkipToday = {
                coroutineScope.launch {
                    manager.markSkipped(details = "USER_SKIPPED_FROM_GATE")
                    onFinished()
                }
            }
        )
        }
    }

    private fun completeChallengeDay(prefs: PrefManager) {
        val today = LocalDate.now().toString()
        prefs.routineCompletedToday = true
        prefs.routineDate = today

        val result = ChallengeManager.recordDayCompleted(this)
        if (!result.recorded && result.alreadyCompletedToday) {
            return
        }
    }

    private fun armMorningBonus(prefs: PrefManager) {
        prefs.morningBonusArmedDate = LocalDate.now().toString()
    }

    private fun openHome() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
        )
    }
}

private fun PrefManager.shieldMode(): ShieldMode = when (appMode) {
    ShieldMode.IRON.name -> ShieldMode.IRON
    ShieldMode.RECOVERY.name -> ShieldMode.RECOVERY
    ShieldMode.EXPERIMENT.name -> ShieldMode.EXPERIMENT
    else -> ShieldMode.BUILD
}

private fun DailyIntegrityRecord.shieldModeOr(fallback: ShieldMode): ShieldMode =
    ShieldMode.entries.firstOrNull { it.name == modeAtFinalization } ?: fallback

@Composable
private fun MorningLaunchGateScreen(
    mode: MorningLaunchMode,
    steps: List<MorningLaunchStep>,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onStart: () -> Unit,
    onRemindFive: () -> Unit,
    onSkipToday: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val accent = when (mode) {
        MorningLaunchMode.MONK -> NeonCyan
        MorningLaunchMode.WARRIOR -> WarningAmber
        MorningLaunchMode.BALANCED -> SuccessGreen
    }
    var showSkipDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Soft sensory feedback upon launch
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playSuccess(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTaskComplete()
    }
    
    val formattedDate = remember {
        java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")
        )
    }

    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = {
                Text(
                    text = "Skip Morning Launch?",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "This records a missed launch and closes the morning sequence before it starts."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSkipDialog = false
                    onSkipToday()
                }) {
                    Text("Skip anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false }) {
                    Text("Stay in launch")
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.74f)
        )
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = {
                Text(
                    text = "Use Emergency Exit?",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "Emergency Exit closes Morning Launch and marks today as skipped. Remaining this month: $emergencyRemaining of 5."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showEmergencyDialog = false
                    onEmergencyExit()
                }) {
                    Text("Use exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Keep going")
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.74f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceDark, DeepBackground)
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedDate.uppercase(),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Surface(
                    color = accent.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "MORNING LAUNCH",
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Run the ${mode.label.lowercase()} launch.",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = mode.subtitle,
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "TODAY'S STACK",
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                    steps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(28.dp)
                                    .background(accent.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = accent,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (step.durationSeconds > 0) {
                                        "${step.durationSeconds / 60} min guided"
                                    } else {
                                        "manual completion"
                                    },
                                    color = Color.White.copy(alpha = 0.50f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "START MY DAY",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Button(
                    onClick = onRemindFive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        contentColor = Color.White.copy(alpha = 0.78f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = "REMIND IN 5 MINUTES",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "OTHER OPTIONS",
                            color = Color.White.copy(alpha = 0.42f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showSkipDialog = true },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "SKIP TODAY",
                                    color = Color.White.copy(alpha = 0.58f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.4.sp
                                )
                            }
                            TextButton(
                                onClick = { if (emergencyRemaining > 0) showEmergencyDialog = true },
                                enabled = emergencyRemaining > 0,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (emergencyRemaining > 0) {
                                        "EMERGENCY EXIT • $emergencyRemaining LEFT"
                                    } else {
                                        "EMERGENCY EXIT UNAVAILABLE"
                                    },
                                    color = if (emergencyRemaining > 0) {
                                        Color.White.copy(alpha = 0.58f)
                                    } else {
                                        Color.White.copy(alpha = 0.24f)
                                    },
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }

                        // Honesty mode override
                        HonestyOverridePanel(
                            lockType = "MORNING_LAUNCH",
                            onOverride = onSkipToday
                        )
                    }
                }
            }
        }
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF07080F)
@androidx.compose.runtime.Composable
fun MorningLaunchPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        MorningLaunchPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun MorningLaunchPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceElevated)
                )
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            modifier = androidx.compose.ui.Modifier.padding(32.dp)
        ) {
            androidx.compose.material3.Text(
                "🌅 MORNING LAUNCH",
                color = WarningAmber,
                fontSize = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                letterSpacing = 4.sp
            )
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(8.dp))
            androidx.compose.material3.Text(
                "Day 21",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 48.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black
            )
            androidx.compose.material3.Text(
                "Streak: 21 days 🔥",
                color = WarningAmber,
                fontSize = 16.sp
            )
        }
    }
}