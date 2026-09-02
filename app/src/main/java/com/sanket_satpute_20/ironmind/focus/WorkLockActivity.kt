package com.sanket_satpute_20.ironmind.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.alarm.AlarmScheduler
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.TaskEvent
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

/**
 * Matrix-style falling code rain — uses native Canvas for text rendering.
 * Each column has an independent fall speed; columns reset to top when they exit.
 */
@Composable
private fun MatrixRain(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") tint: androidx.compose.ui.graphics.Color = SuccessGreen
) {
    val charPool = "01ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#\$%^&*"
    val columns = remember { mutableListOf<Triple<Float, Float, Int>>() }
    val random = remember { java.util.Random(42) }

    // Animation tick — drives recomposition so Canvas redraws
    val transition = rememberInfiniteTransition(label = "matrix")
    val tick by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrix_tick"
    )

    val paint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.argb(90, 0, 230, 65)
            textSize = 40f  // px — set properly after first measure
            typeface = android.graphics.Typeface.MONOSPACE
        }
    }

    Canvas(modifier = modifier) {
        paint.textSize = 14.dp.toPx()

        if (columns.isEmpty()) {
            val colCount = (size.width / 18.dp.toPx()).toInt().coerceAtLeast(1)
            repeat(colCount) { i ->
                columns.add(Triple(i * 18.dp.toPx(), random.nextFloat() * size.height, 1 + random.nextInt(3)))
            }
        }

        val step = 14.dp.toPx() * tick // advances each frame
        val updated = columns.map { (x, y, speed) ->
            val newY = y + speed * step
            val resetY = if (newY > size.height + 20f) -20f else newY
            Triple(x, resetY, speed)
        }
        columns.clear()
        columns.addAll(updated)

        val nativeCanvas = drawContext.canvas.nativeCanvas
        columns.forEach { (x, y, _) ->
            val ch = charPool[random.nextInt(charPool.length)].toString()
            nativeCanvas.drawText(ch, x, y, paint)
        }
    }
}


class WorkLockActivity : ComponentActivity() {

    companion object {
        private const val ACTION_FINISH = "com.sanket_satpute_20.ironmind.focus.WORK_LOCK_FINISH"
        private const val EXTRA_TASK_ID = "extra_task_id"
        private const val EXTRA_TASK_NAME = "extra_task_name"
        private const val EXTRA_TASK_DATE = "extra_task_date"
        private const val EXTRA_TASK_START = "extra_task_start"
        private const val EXTRA_TASK_END = "extra_task_end"
        var bypassLaunching = false

        fun createIntent(
            context: Context,
            taskId: Int = -1,
            taskName: String = "",
            taskDate: String = "",
            taskStartTime: String = "",
            taskEndTime: String = ""
        ): Intent =
            Intent(context, WorkLockActivity::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_NAME, taskName)
                putExtra(EXTRA_TASK_DATE, taskDate)
                putExtra(EXTRA_TASK_START, taskStartTime)
                putExtra(EXTRA_TASK_END, taskEndTime)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }

        fun finishIntent(context: Context): Intent =
            Intent(ACTION_FINISH).setPackage(context.packageName)
    }

    private var finishReceiver: BroadcastReceiver? = null
    private var lockTaskAttempted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowFlags()
        onBackPressedDispatcher.addCallback(this) { }
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                finish()
            }
        }
        registerReceiver(
            finishReceiver,
            IntentFilter(ACTION_FINISH),
            RECEIVER_NOT_EXPORTED
        )
        val initialTaskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        val initialTaskName = intent.getStringExtra(EXTRA_TASK_NAME).orEmpty()
        val initialTaskDate = intent.getStringExtra(EXTRA_TASK_DATE).orEmpty()
        val initialTaskStart = intent.getStringExtra(EXTRA_TASK_START).orEmpty()
        val initialTaskEnd = intent.getStringExtra(EXTRA_TASK_END).orEmpty()
        setContent {
            IronMindTheme {
                WorkLockScreen(
                    initialTaskId = initialTaskId,
                    initialTaskName = initialTaskName,
                    initialTaskDate = initialTaskDate,
                    initialTaskStartTime = initialTaskStart,
                    initialTaskEndTime = initialTaskEnd,
                    onClose = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = PrefManager.getInstance(this)
        if (!lockTaskAttempted && prefs.workLockActive) {
            lockTaskAttempted = true
            SpringLockTaskController.tryEnterLockTask(this)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val prefs = PrefManager.getInstance(this)
        if (bypassLaunching) {
            bypassLaunching = false
            return
        }
        if (!isFinishing && prefs.workLockActive) {
            startActivity(createIntent(this))
        }
    }

    override fun onStop() {
        super.onStop()
        val prefs = PrefManager.getInstance(this)
        if (bypassLaunching) {
            bypassLaunching = false
            return
        }
        if (!isFinishing && !isChangingConfigurations && prefs.workLockActive) {
            startActivity(createIntent(this))
        }
    }

    override fun onDestroy() {
        SpringLockTaskController.tryExitLockTask(this)
        super.onDestroy()
        runCatching { unregisterReceiver(finishReceiver) }
    }

    private fun setupWindowFlags() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }
}

@Composable
private fun WorkLockScreen(
    initialTaskId: Int,
    initialTaskName: String,
    initialTaskDate: String,
    initialTaskStartTime: String,
    initialTaskEndTime: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val prefs = remember { PrefManager.getInstance(context) }
    val manager = remember { WorkLockManager(context) }
    val pomodoroEngine = remember { PomodoroEngine(context) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showBreakDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var resolutionState by remember { mutableStateOf<SpringResolutionState?>(null) }
    val effectiveTaskId = remember(prefs.workLockTaskId, initialTaskId) {
        prefs.workLockTaskId.takeIf { it != -1 } ?: initialTaskId
    }
    val effectiveTaskName = remember(prefs.activeTaskName, initialTaskName) {
        prefs.activeTaskName.ifBlank { initialTaskName }
    }
    val effectiveTaskDate = remember(prefs.activeTaskDate, initialTaskDate) {
        prefs.activeTaskDate.takeIf { it.isNotBlank() } ?: initialTaskDate
    }
    val effectiveTaskStartTime = remember(prefs.activeTaskStartTime, initialTaskStartTime) {
        prefs.activeTaskStartTime.ifBlank { initialTaskStartTime }
    }
    val effectiveTaskEndTime = remember(prefs.activeTaskEndTime, initialTaskEndTime) {
        prefs.activeTaskEndTime.ifBlank { initialTaskEndTime }
    }
    val passiveTaskEndMillis = remember(
        effectiveTaskDate,
        effectiveTaskEndTime
    ) {
        resolveTaskWindowMillis(effectiveTaskDate, effectiveTaskEndTime)
    }
    val passiveTaskStartMillis = remember(
        effectiveTaskDate,
        effectiveTaskStartTime
    ) {
        resolveTaskWindowMillis(effectiveTaskDate, effectiveTaskStartTime)
    }
    val passiveLiveMode = remember(
        nowMillis,
        prefs.workLockActive,
        effectiveTaskDate,
        effectiveTaskName,
        effectiveTaskEndTime
    ) {
        !manager.isActive() && isPassiveLiveTaskActive(
            taskDate = effectiveTaskDate,
            taskName = effectiveTaskName,
            passiveTaskEndMillis = passiveTaskEndMillis,
            nowMillis = nowMillis
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            nowMillis = System.currentTimeMillis()
            pomodoroEngine.advanceIfNeeded(nowMillis)
            if (!manager.isActive() && !isPassiveLiveTaskActive(
                    taskDate = effectiveTaskDate,
                    taskName = effectiveTaskName,
                    passiveTaskEndMillis = passiveTaskEndMillis,
                    nowMillis = nowMillis
                )
            ) {
                sendFinishAndClose(context, onClose)
                return@LaunchedEffect
            }
        }
    }

    val pomodoroState = remember(
        nowMillis,
        prefs.pomodoroActive,
        prefs.pomodoroTaskId,
        prefs.pomodoroPhase,
        prefs.pomodoroPhaseEndsAt,
        prefs.pomodoroIntervalIndex,
        prefs.pomodoroCompletedWorkIntervals,
        prefs.pomodoroBreakUnlocked
    ) {
        pomodoroEngine.currentState().takeIf {
            it.active && it.taskId != -1 && it.taskId == effectiveTaskId
        }
    }
    val isPomodoroBreak = pomodoroState?.isBreakPhase == true
    val totalWindowMillis = if (manager.isActive()) {
        (prefs.workLockEndsAt - prefs.workLockStartedAt).coerceAtLeast(1L)
    } else {
        ((passiveTaskEndMillis ?: nowMillis) - (passiveTaskStartMillis ?: nowMillis)).coerceAtLeast(1L)
    }
    val remainingMillis = pomodoroState?.let { pomodoroEngine.timeRemainingMillis(nowMillis) } ?: if (manager.isActive()) {
        manager.timeRemainingMillis()
    } else {
        ((passiveTaskEndMillis ?: nowMillis) - nowMillis).coerceAtLeast(0L)
    }
    
    LaunchedEffect(remainingMillis / 1000L) {
        if (remainingMillis > 0 && remainingMillis <= 60_000L) {
            com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playTick()
        }
    }

    val phaseWindowMillis = pomodoroState?.let { state ->
        when (state.phase) {
            PomodoroPhase.WORK -> state.preset.workMinutes * 60_000L
            PomodoroPhase.SHORT_BREAK -> state.preset.shortBreakMinutes * 60_000L
            PomodoroPhase.LONG_BREAK -> state.preset.longBreakMinutes * 60_000L
            else -> totalWindowMillis
        }
    } ?: totalWindowMillis
    val progress = (remainingMillis.toFloat() / phaseWindowMillis.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val emergencyRemaining = manager.remainingEmergencyExitAllowance()
    val accent = if (isPomodoroBreak) NeonCyan else if (passiveLiveMode) NeonCyan else ErrorRed
    val amber = WarningAmber
    val danger = WarningAmber
    val pulse = rememberInfiniteTransition(label = "spring_pulse")
    val ringGlow by pulse.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_glow"
    )
    val sweepRotation by pulse.animateFloat(
        initialValue = -90f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(4400),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_rotation"
    )
    val chamberSweep by pulse.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6200),
            repeatMode = RepeatMode.Restart
        ),
        label = "chamber_sweep"
    )

    LaunchedEffect((remainingMillis / 1_000L).coerceAtLeast(0L)) {
        val secondsLeft = (remainingMillis / 1_000L).toInt()
        when {
            secondsLeft in 1..5 -> {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                playSpringCue(context, SpringCueProfile.Tick)
            }
            secondsLeft > 0 && secondsLeft % 60 == 0 -> {
                playSpringCue(context, SpringCueProfile.MinuteMark)
            }
        }
    }

    LaunchedEffect(resolutionState) {
        val state = resolutionState ?: return@LaunchedEffect
        delay(820L)
        when (state) {
            SpringResolutionState.COMPLETE -> {
                completeCurrentMission(context, manager)
                sendFinishAndClose(context, onClose)
            }
            SpringResolutionState.EMERGENCY -> {
                emergencyExit(context, manager)
                sendFinishAndClose(context, onClose)
            }
            SpringResolutionState.BREAK -> {
                breakCurrentMission(context, manager)
                sendFinishAndClose(context, onClose)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.18f * ringGlow), Color.Transparent),
                        center = Offset(560f, 220f),
                        radius = 860f
                    )
                )
        )
        // Matrix rain background layer
        MatrixRain(
            modifier = Modifier.fillMaxSize(),
            tint = accent
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = accent.copy(alpha = 0.05f)
            val step = size.height / 8f
            for (index in 0..8) {
                val y = index * step
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (chamberSweep * 420f).dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.02f),
                            accent.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(6.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SpringWallChip("SPRING PROTOCOL", accent)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = prefs.workLockTaskName.ifBlank { effectiveTaskName.ifBlank { "ACTIVE MISSION" } },
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = pomodoroState?.let { phaseLabelFor(it.phase) } ?: if (passiveLiveMode) "PASSIVE TASK WINDOW" else "SPRING SEALED",
                    color = accent.copy(alpha = 0.82f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
            }

            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                com.sanket_satpute_20.ironmind.ui.components.EnergyCoreUI(
                    progress = progress,
                    modifier = Modifier.size(318.dp),
                    remainingLabel = formatWorkLockRemaining(remainingMillis),
                    accent = accent,
                    phaseLabel = pomodoroState?.let { phaseCenterLabelFor(it.phase) } ?: "SEALED"
                )
            }

            SpringInfoRail(
                leftTitle = if (pomodoroState != null) "FOCUS STATE" else if (passiveLiveMode) "SESSION" else "BREACH STATE",
                leftValue = if (pomodoroState != null) {
                    if (isPomodoroBreak) "SIGNAL WINDOW" else "DEEP FOCUS"
                } else if (passiveLiveMode) {
                    "LIVE TIMER"
                } else if (prefs.workLockPenaltyArmed) "PENALTY LIVE" else "SAFE",
                leftAccent = if (prefs.workLockPenaltyArmed) amber else accent,
                rightTitle = if (pomodoroState != null) "BREACHES" else if (passiveLiveMode) "ENDS" else "EXITS LEFT",
                rightValue = if (pomodoroState != null) pomodoroState.breachCount.toString() else if (passiveLiveMode) formatWallClockTime(passiveTaskEndMillis) else emergencyRemaining.toString(),
                rightAccent = amber
            )

            Spacer(Modifier.height(16.dp))

            WorkLockStateCard(
                title = when {
                    pomodoroState != null && isPomodoroBreak -> "Break open"
                    pomodoroState != null -> "Work phase live"
                    passiveLiveMode -> "Passive task window"
                    else -> "Spring sealed"
                },
                body = when {
                    pomodoroState != null && isPomodoroBreak -> "Signal apps can pass during this break. Void apps still bounce back into the wall."
                    pomodoroState != null -> "Leaving now will throw you back into the wall. Void breaches can also reset the Pomodoro interval."
                    passiveLiveMode -> "This task is still live even without a full Spring lock. Ending it here will mark it against the board."
                    else -> "This mission stays sealed until you complete it, use an emergency exit, or break the mission."
                },
                accent = accent
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    playSpringCue(context, SpringCueProfile.Complete)
                    resolutionState = SpringResolutionState.COMPLETE
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("COMPLETE MISSION", fontWeight = FontWeight.Black, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            SpringActionRow(
                primaryIcon = Icons.Rounded.Emergency,
                primaryTitle = if (passiveLiveMode) "Leave Session" else "Emergency Exit",
                primarySubtitle = if (passiveLiveMode) "End the live task without completion" else if (manager.canUseEmergencyExit()) "$emergencyRemaining monthly uses remain" else "No emergency exits remain this month",
                primaryAccent = amber,
                primaryEnabled = if (passiveLiveMode) true else manager.canUseEmergencyExit(),
                onPrimaryClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    playSpringCue(context, SpringCueProfile.Emergency)
                    showEmergencyDialog = true
                },
                secondaryIcon = Icons.Rounded.WarningAmber,
                secondaryTitle = if (passiveLiveMode) "Fail Task" else "Break Mission",
                secondarySubtitle = if (passiveLiveMode) "Mark the mission failed and stop the timer" else "Take the XP hit and mark the mission as failed",
                secondaryAccent = danger,
                onSecondaryClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    playSpringCue(context, SpringCueProfile.Break)
                    showBreakDialog = true
                }
            )

            Spacer(Modifier.height(14.dp))
        }
    }

    if (showBreakDialog) {
        AlertDialog(
            onDismissRequest = { showBreakDialog = false },
            title = { Text("End this mission as failed?", fontWeight = FontWeight.Black) },
            text = {
                Text(
                    if (manager.isActive()) {
                        "This will release Spring, mark the mission failed, and cost ${manager.calculateBreakPenalty()} XP."
                    } else {
                        "This will stop the live task and mark it failed on today’s board."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        resolutionState = SpringResolutionState.BREAK
                        showBreakDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Fail mission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakDialog = false }) {
                    Text(if (manager.isActive()) "Stay sealed" else "Keep working")
                }
            }
        )
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Use emergency exit?", fontWeight = FontWeight.Black) },
            text = {
                Text(
                    if (manager.isActive()) {
                        "Emergency exit is a safety valve with limited monthly uses. This will release Spring without completing the mission."
                    } else {
                        "This will end the live task without completing it, but it will not count as a clean finish."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        resolutionState = SpringResolutionState.EMERGENCY
                        showEmergencyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text("Use safety exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text(if (manager.isActive()) "Stay sealed" else "Keep working")
                }
            }
        )
    }

    resolutionState?.let { state ->
        SpringResolutionOverlay(state = state)
    }
}

@Composable
private fun WorkLockStateCard(
    title: String,
    body: String,
    accent: Color
) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title.uppercase(),
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.1.sp
            )
            Text(
                body,
                color = Color.White.copy(alpha = 0.76f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun WorkLockRing(
    progress: Float,
    modifier: Modifier,
    remainingLabel: String,
    accent: Color,
    penaltyLabel: String,
    phaseLabel: String,
    intervalLabel: String?,
    sweepRotation: Float,
    glowScale: Float
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val radius = (size.minDimension / 2f) - stroke
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = radius,
                center = center,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawCircle(
                color = accent.copy(alpha = 0.08f),
                radius = radius - stroke * 0.9f,
                center = center
            )

            drawCircle(
                color = DeepBackground,
                radius = radius - stroke * 1.8f,
                center = center
            )

            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        accent.copy(alpha = 0.08f * glowScale),
                        accent.copy(alpha = 0.36f * glowScale),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = sweepRotation,
                sweepAngle = 52f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke * 1.18f, cap = StrokeCap.Round)
            )

            drawCircle(
                color = accent.copy(alpha = 0.08f),
                radius = radius + 12.dp.toPx(),
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f))
                )
            )

            val sweepRad = sweepRotation * (PI / 180f).toFloat()
            val tipX = center.x + radius * cos(sweepRad)
            val tipY = center.y + radius * sin(sweepRad)
            drawCircle(
                color = accent.copy(alpha = 0.22f * glowScale),
                radius = stroke * 0.9f,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = accent.copy(alpha = 0.78f),
                radius = stroke * 0.24f,
                center = Offset(tipX, tipY)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SpringWallChip(phaseLabel, accent)
            Spacer(Modifier.height(14.dp))
            AnimatedContent(
                targetState = remainingLabel,
                transitionSpec = {
                    (slideInVertically { it / 3 } + fadeIn(tween(180))).togetherWith(
                        slideOutVertically { -it / 3 } + fadeOut(tween(180))
                    )
                },
                label = "spring_countdown"
            ) { animatedLabel ->
                Text(
                    text = animatedLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = intervalLabel ?: "TIME REMAINING",
                color = Color.White.copy(alpha = 0.54f),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (intervalLabel != null) "SPRING ADAPTS BY INTERVAL" else "SPRING HOLDS THE LINE",
                color = accent.copy(alpha = 0.86f),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp
            )
            Spacer(Modifier.height(12.dp))
            SpringWallChip(penaltyLabel, WarningAmber)
        }
    }
}

@Composable
private fun SpringWallChip(label: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = accent,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun SpringPill(label: String, value: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.14f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(label, color = accent, fontWeight = FontWeight.Black, fontSize = 10.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SpringStatCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, color = Color.White.copy(alpha = 0.56f), fontWeight = FontWeight.Black, fontSize = 10.sp)
                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SpringInfoRail(
    leftTitle: String,
    leftValue: String,
    leftAccent: Color,
    rightTitle: String,
    rightValue: String,
    rightAccent: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.035f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SpringRailMetric(
                title = leftTitle,
                value = leftValue,
                accent = leftAccent,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            SpringRailMetric(
                title = rightTitle,
                value = rightValue,
                accent = rightAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SpringRailMetric(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = Color.White.copy(alpha = 0.52f),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = accent,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SpringActionRow(
    primaryIcon: ImageVector,
    primaryTitle: String,
    primarySubtitle: String,
    primaryAccent: Color,
    primaryEnabled: Boolean,
    onPrimaryClick: () -> Unit,
    secondaryIcon: ImageVector,
    secondaryTitle: String,
    secondarySubtitle: String,
    secondaryAccent: Color,
    onSecondaryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SpringActionCard(
            modifier = Modifier.weight(1f),
            icon = primaryIcon,
            title = primaryTitle,
            subtitle = primarySubtitle,
            accent = primaryAccent,
            enabled = primaryEnabled,
            onClick = onPrimaryClick
        )
        SpringActionCard(
            modifier = Modifier.weight(1f),
            icon = secondaryIcon,
            title = secondaryTitle,
            subtitle = secondarySubtitle,
            accent = secondaryAccent,
            enabled = true,
            outlined = true,
            onClick = onSecondaryClick
        )
    }
}

@Composable
private fun SpringActionCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    enabled: Boolean,
    outlined: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = tween(120),
        label = "spring_action_scale"
    )
    Surface(
        modifier = modifier.scale(scale),
        color = if (outlined) Color.Transparent else Color.White.copy(alpha = if (enabled) 0.04f else 0.02f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (enabled) 0.18f else 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accent.copy(alpha = if (enabled) 0.14f else 0.06f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent.copy(alpha = if (enabled) 1f else 0.45f), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.42f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                color = Color.White.copy(alpha = if (enabled) 0.56f else 0.30f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SpringResolutionOverlay(state: SpringResolutionState) {
    val accent = when (state) {
        SpringResolutionState.COMPLETE -> NeonCyan
        SpringResolutionState.EMERGENCY -> WarningAmber
        SpringResolutionState.BREAK -> WarningAmber
    }
    val title = when (state) {
        SpringResolutionState.COMPLETE -> "MISSION SEALED"
        SpringResolutionState.EMERGENCY -> "EMERGENCY RELEASE"
        SpringResolutionState.BREAK -> "MISSION BROKEN"
    }
    val subtitle = when (state) {
        SpringResolutionState.COMPLETE -> "Spring is clearing the chamber."
        SpringResolutionState.EMERGENCY -> "Spring is unlocking the emergency path."
        SpringResolutionState.BREAK -> "Penalty is being applied."
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpringWallChip("SPRING TRANSITION", accent)
                Spacer(Modifier.height(14.dp))
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.66f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private enum class SpringResolutionState {
    COMPLETE,
    EMERGENCY,
    BREAK
}

private enum class SpringCueProfile { Tick, MinuteMark, Complete, Emergency, Break }

private fun playSpringCue(context: Context, profile: SpringCueProfile) {
    val soundManager = com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context)
    when (profile) {
        SpringCueProfile.Tick,
        SpringCueProfile.MinuteMark -> soundManager.playTimerTick()
        SpringCueProfile.Complete -> soundManager.playTaskComplete()
        SpringCueProfile.Emergency,
        SpringCueProfile.Break -> soundManager.playTemptationBlocked()
    }
}

private fun formatWorkLockRemaining(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis / 1_000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatWallClockTime(epochMillis: Long?): String {
    if (epochMillis == null || epochMillis <= 0L) return "--:--"
    return runCatching {
        val dateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(epochMillis),
            ZoneId.systemDefault()
        )
        dateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    }.getOrDefault("--:--")
}

private fun resolveTaskWindowMillis(taskDate: String, taskTime: String): Long? {
    val date = runCatching { LocalDate.parse(taskDate) }.getOrNull() ?: return null
    val time = parseFlexibleTime(taskTime) ?: return null
    return LocalDateTime.of(date, time)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

private fun parseFlexibleTime(value: String): LocalTime? {
    val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
    return formats.firstNotNullOfOrNull { pattern ->
        runCatching {
            LocalTime.parse(value.trim().uppercase(Locale.US), DateTimeFormatter.ofPattern(pattern, Locale.US))
        }.getOrNull()
    }
}

private fun isPassiveLiveTaskActive(
    taskDate: String,
    taskName: String,
    passiveTaskEndMillis: Long?,
    nowMillis: Long
): Boolean {
    val today = LocalDate.now().toString()
    if (taskDate != today) return false
    if (taskName.isBlank()) return false
    return passiveTaskEndMillis?.let { it > nowMillis } == true
}

private fun phaseLabelFor(phase: PomodoroPhase): String {
    return when (phase) {
        PomodoroPhase.WORK -> "WORK INTERVAL ACTIVE"
        PomodoroPhase.SHORT_BREAK -> "BREAK WINDOW ACTIVE"
        PomodoroPhase.LONG_BREAK -> "LONG BREAK WINDOW"
        PomodoroPhase.COMPLETED -> "SESSION COMPLETE"
        PomodoroPhase.BROKEN -> "SESSION BROKEN"
        PomodoroPhase.IDLE -> "SEALED UNTIL RESOLUTION"
    }
}

private fun phaseCenterLabelFor(phase: PomodoroPhase): String {
    return when (phase) {
        PomodoroPhase.WORK -> "WORK"
        PomodoroPhase.SHORT_BREAK -> "BREAK"
        PomodoroPhase.LONG_BREAK -> "LONG BREAK"
        PomodoroPhase.COMPLETED -> "DONE"
        PomodoroPhase.BROKEN -> "BROKEN"
        PomodoroPhase.IDLE -> "SEALED"
    }
}

private fun sendFinishAndClose(context: Context, onClose: () -> Unit) {
    context.sendBroadcast(WorkLockActivity.finishIntent(context))
    onClose()
}

private data class PomodoroSummaryPayload(
    val taskName: String,
    val outcome: String,
    val intervalsCompleted: Int,
    val focusMinutes: Int,
    val breachCount: Int,
    val focusScore: Int
)

private fun resolvePomodoroSummary(
    context: Context,
    taskId: Int,
    taskName: String,
    outcome: String
): PomodoroSummaryPayload? {
    val prefs = PrefManager.getInstance(context)
    if (!prefs.pomodoroActive || prefs.pomodoroTaskId != taskId) return null

    val state = when (outcome) {
        "COMPLETE" -> PomodoroEngine(context).completeSession()
        else -> PomodoroEngine(context).breakSession()
    }
    val focusMinutes = state.completedWorkIntervals * state.preset.workMinutes
    val penalty = if (outcome == "COMPLETE") 0 else 15
    val focusScore = (100 - (state.breachCount * 20) - penalty).coerceIn(0, 100)

    return PomodoroSummaryPayload(
        taskName = taskName,
        outcome = outcome,
        intervalsCompleted = state.completedWorkIntervals,
        focusMinutes = focusMinutes,
        breachCount = state.breachCount,
        focusScore = focusScore
    )
}

private suspend fun insertPomodoroSummaryEvent(
    db: IronMindDatabase,
    taskEvent: TaskEvent,
    payload: PomodoroSummaryPayload,
    timestamp: Long
) {
    val eventType = when (payload.outcome) {
        "COMPLETE" -> "POMODORO_COMPLETED"
        "BROKEN" -> "POMODORO_BROKEN"
        "EMERGENCY" -> "POMODORO_EMERGENCY_EXIT"
        else -> "POMODORO_COMPLETED"
    }
    val reason = buildString {
        append("INTERVALS=")
        append(payload.intervalsCompleted)
        append(";MINUTES=")
        append(payload.focusMinutes)
        append(";BREACHES=")
        append(payload.breachCount)
        append(";SCORE=")
        append(payload.focusScore)
        append(";OUTCOME=")
        append(payload.outcome)
    }
    db.taskEventDao().insert(
        taskEvent.copy(
            id = 0,
            eventType = eventType,
            timestamp = timestamp,
            reason = reason
        )
    )
}

private fun launchPomodoroSummary(
    context: Context,
    payload: PomodoroSummaryPayload?
) {
    if (payload == null) return
    context.startActivity(
        PomodoroSummaryActivity.createIntent(
            context = context,
            taskName = payload.taskName,
            outcome = payload.outcome,
            intervalsCompleted = payload.intervalsCompleted,
            focusMinutes = payload.focusMinutes,
            breachCount = payload.breachCount,
            focusScore = payload.focusScore
        )
    )
}

private suspend fun resolveCurrentLiveTask(
    db: IronMindDatabase,
    prefs: PrefManager
) = prefs.workLockTaskId
    .takeIf { it != -1 }
    ?.let { db.taskDao().getTaskById(it) }
    ?: db.taskDao()
        .getTasksForDateOnce(prefs.activeTaskDate)
        .firstOrNull { task ->
            task.name == prefs.activeTaskName &&
                !task.isCompleted &&
                !task.isSkipped
        }

private fun clearActiveTaskWindow(prefs: PrefManager) {
    prefs.activeTaskName = ""
    prefs.activeTaskStartTime = ""
    prefs.activeTaskEndTime = ""
    prefs.activeTaskDate = LocalDate.now().toString()
}

private fun completeCurrentMission(context: Context, manager: WorkLockManager) {
    val prefs = PrefManager.getInstance(context)
    val now = System.currentTimeMillis()
    CoroutineScope(Dispatchers.IO).launch {
        val earnedUnlockManager = EarnedUnlockManager(context)
        val db = IronMindDatabase.getDatabase(context)
        val task = resolveCurrentLiveTask(db, prefs) ?: return@launch
        db.taskDao().updateTask(
            task.copy(
                isCompleted = true,
                isDeferred = false,
                isInProgress = false,
                completedAt = now,
                completionSource = "WORK_LOCK",
                lastModified = now,
                syncStatus = "PENDING"
            )
        )
        AlarmScheduler.cancelTaskAlarms(context, task.id, task.name)
        db.taskEventDao().insert(
            TaskEvent(
                taskId = task.id,
                taskName = task.name,
                date = task.date,
                eventType = "COMPLETED",
                timestamp = now,
                oldStartTime = task.startTime,
                oldEndTime = task.endTime,
                newStartTime = task.startTime,
                newEndTime = task.endTime,
                focusScoreSnapshot = task.focusScore,
                reason = "WORK_LOCK_COMPLETE"
            )
        )
        db.taskEventDao().insert(
            TaskEvent(
                taskId = task.id,
                taskName = task.name,
                date = task.date,
                eventType = "WORK_LOCK_COMPLETED",
                timestamp = now,
                oldStartTime = task.startTime,
                oldEndTime = task.endTime,
                newStartTime = task.startTime,
                newEndTime = task.endTime,
                focusScoreSnapshot = task.focusScore,
                reason = "SPRING_PROTOCOL_COMPLETE"
            )
        )
        val pomodoroSummary = resolvePomodoroSummary(
            context = context,
            taskId = task.id,
            taskName = task.name,
            outcome = "COMPLETE"
        )
        if (pomodoroSummary != null) {
            insertPomodoroSummaryEvent(
                db = db,
                taskEvent = TaskEvent(
                    taskId = task.id,
                    taskName = task.name,
                    date = task.date,
                    eventType = "POMODORO_COMPLETED",
                    timestamp = now,
                    oldStartTime = task.startTime,
                    oldEndTime = task.endTime,
                    newStartTime = task.startTime,
                    newEndTime = task.endTime,
                    focusScoreSnapshot = task.focusScore
                ),
                payload = pomodoroSummary,
                timestamp = now
            )
        }
        FocusSessionService.stop(context)
        if (pomodoroSummary == null) {
            prefs.clearPomodoro()
        }
        clearActiveTaskWindow(prefs)
        prefs.clearActiveMissionContextApps()
        earnedUnlockManager.syncTodayFromDatabase()
        manager.endForCompletion()
        launchPomodoroSummary(context, pomodoroSummary)
    }
}

private fun breakCurrentMission(context: Context, manager: WorkLockManager) {
    val prefs = PrefManager.getInstance(context)
    val now = System.currentTimeMillis()
    CoroutineScope(Dispatchers.IO).launch {
        val earnedUnlockManager = EarnedUnlockManager(context)
        val db = IronMindDatabase.getDatabase(context)
        val task = resolveCurrentLiveTask(db, prefs)
        if (task != null) {
                db.taskDao().updateTask(
                    task.copy(
                        isSkipped = true,
                        isDeferred = false,
                        isInProgress = false,
                        skippedAt = now,
                        skipReason = "WORK_LOCK_BREAK",
                        lastModified = now,
                        syncStatus = "PENDING"
                    )
                )
                AlarmScheduler.cancelTaskAlarms(context, task.id, task.name)
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = task.id,
                        taskName = task.name,
                        date = task.date,
                        eventType = "SKIPPED",
                        timestamp = now,
                        oldStartTime = task.startTime,
                        oldEndTime = task.endTime,
                        newStartTime = task.startTime,
                        newEndTime = task.endTime,
                        focusScoreSnapshot = task.focusScore,
                        reason = "WORK_LOCK_BREAK"
                    )
                )
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = task.id,
                        taskName = task.name,
                        date = task.date,
                        eventType = "WORK_LOCK_BROKEN",
                        timestamp = now,
                        oldStartTime = task.startTime,
                        oldEndTime = task.endTime,
                        newStartTime = task.startTime,
                        newEndTime = task.endTime,
                        focusScoreSnapshot = task.focusScore,
                        reason = "SPRING_PROTOCOL_BROKEN"
                    )
                )
                val pomodoroSummary = resolvePomodoroSummary(
                    context = context,
                    taskId = task.id,
                    taskName = task.name,
                    outcome = "BROKEN"
                )
                if (pomodoroSummary != null) {
                    insertPomodoroSummaryEvent(
                        db = db,
                        taskEvent = TaskEvent(
                            taskId = task.id,
                            taskName = task.name,
                            date = task.date,
                            eventType = "POMODORO_BROKEN",
                            timestamp = now,
                            oldStartTime = task.startTime,
                            oldEndTime = task.endTime,
                            newStartTime = task.startTime,
                            newEndTime = task.endTime,
                            focusScoreSnapshot = task.focusScore
                        ),
                        payload = pomodoroSummary,
                        timestamp = now
                    )
                }
                FocusSessionService.stop(context)
                if (pomodoroSummary == null) {
                    prefs.clearPomodoro()
                }
                clearActiveTaskWindow(prefs)
                prefs.clearActiveMissionContextApps()
                earnedUnlockManager.syncTodayFromDatabase()
                manager.breakMissionWithPenalty()
                launchPomodoroSummary(context, pomodoroSummary)
                return@launch
        }
        FocusSessionService.stop(context)
        prefs.clearPomodoro()
        clearActiveTaskWindow(prefs)
        prefs.clearActiveMissionContextApps()
        earnedUnlockManager.syncTodayFromDatabase()
        manager.breakMissionWithPenalty()
    }
}

private fun emergencyExit(context: Context, manager: WorkLockManager) {
    val prefs = PrefManager.getInstance(context)
    CoroutineScope(Dispatchers.IO).launch {
        var pomodoroSummary: PomodoroSummaryPayload? = null
        val db = IronMindDatabase.getDatabase(context)
        val task = resolveCurrentLiveTask(db, prefs)
        if (task != null) {
                db.taskEventDao().insert(
                    TaskEvent(
                        taskId = task.id,
                        taskName = task.name,
                        date = task.date,
                        eventType = "WORK_LOCK_EMERGENCY_EXIT",
                        timestamp = System.currentTimeMillis(),
                        oldStartTime = task.startTime,
                        oldEndTime = task.endTime,
                        newStartTime = task.startTime,
                        newEndTime = task.endTime,
                        focusScoreSnapshot = task.focusScore,
                        reason = "SPRING_PROTOCOL_EMERGENCY_EXIT"
                    )
                )
                pomodoroSummary = resolvePomodoroSummary(
                    context = context,
                    taskId = task.id,
                    taskName = task.name,
                    outcome = "EMERGENCY"
                )
                if (pomodoroSummary != null) {
                    insertPomodoroSummaryEvent(
                        db = db,
                        taskEvent = TaskEvent(
                            taskId = task.id,
                            taskName = task.name,
                            date = task.date,
                            eventType = "POMODORO_EMERGENCY_EXIT",
                            timestamp = System.currentTimeMillis(),
                            oldStartTime = task.startTime,
                            oldEndTime = task.endTime,
                            newStartTime = task.startTime,
                            newEndTime = task.endTime,
                            focusScoreSnapshot = task.focusScore
                        ),
                        payload = pomodoroSummary,
                        timestamp = System.currentTimeMillis()
                    )
                }
        }
        FocusSessionService.stop(context)
        if (pomodoroSummary == null) {
            prefs.clearPomodoro()
        }
        clearActiveTaskWindow(prefs)
        prefs.clearActiveMissionContextApps()
        manager.useEmergencyExit()
        launchPomodoroSummary(context, pomodoroSummary)
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF060608)
@androidx.compose.runtime.Composable
fun WorkLockScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        WorkLockScreenPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun WorkLockScreenPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(
                "🔒 WORK LOCK ACTIVE",
                color = ErrorRed,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black
            )
            androidx.compose.material3.Text(
                "Deep Focus Block · 09:00 – 10:30",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}
