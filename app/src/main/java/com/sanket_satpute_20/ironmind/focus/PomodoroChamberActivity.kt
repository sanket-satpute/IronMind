package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.gamification.SoundManager
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.sanket_satpute_20.ironmind.failure.MissionFailureContext
import com.sanket_satpute_20.ironmind.mission.MissionExecutionService
import com.sanket_satpute_20.ironmind.mission.MissionExecutionResult
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

class PomodoroChamberActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContent {
            IronMindTheme {
                com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry {
                    PomodoroChamberRoute(
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, PomodoroChamberActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
    }
}

@Composable
private fun PomodoroChamberRoute(
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptics = LocalHapticFeedback.current
    val engine = remember { PomodoroEngine(context) }
    val soundManager = remember { SoundManager.getInstance(context) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var sessionState by remember { mutableStateOf(engine.currentState()) }
    var completionCeremony by remember { mutableStateOf<PomodoroCeremonyState?>(null) }

    LaunchedEffect(Unit) {
        var lastPhase = sessionState.phase
        var lastCountdownSecond = -1L
        var primed = false
        snapshotLoop@ while (true) {
            delay(1_000)
            nowMillis = System.currentTimeMillis()
            val advanced = engine.advanceIfNeeded(nowMillis)
            sessionState = advanced
            if (!primed) {
                lastPhase = advanced.phase
                primed = true
            } else if (advanced.phase != lastPhase) {
                when (advanced.phase) {
                    PomodoroPhase.WORK -> {
                        soundManager.playTimerTick()
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    PomodoroPhase.SHORT_BREAK,
                    PomodoroPhase.LONG_BREAK -> {
                        soundManager.playTaskComplete()
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    else -> Unit
                }
                lastPhase = advanced.phase
            }
            if (!advanced.active) {
                onClose()
                break@snapshotLoop
            }
            val remainingSeconds = ((advanced.phaseEndsAt - nowMillis).coerceAtLeast(0L) + 999L) / 1_000L
            if (remainingSeconds in 1L..10L && remainingSeconds != lastCountdownSecond) {
                soundManager.playTimerTick()
                lastCountdownSecond = remainingSeconds
            } else if (remainingSeconds > 10L) {
                lastCountdownSecond = -1L
            }
            if (advanced.sessionHardEndsAt > 0L && nowMillis >= advanced.sessionHardEndsAt) {
                val ceremonyState = buildCeremonyState(advanced, "COMPLETE")
                completionCeremony = ceremonyState
                soundManager.playTaskComplete()
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(1800)
                finishSession(context, engine, advanced, "COMPLETE", ceremonyState.focusMinutes, ceremonyState.focusScore, onClose)
                break@snapshotLoop
            }
        }
    }

    LaunchedEffect(sessionState.sessionId) {
        if (sessionState.active && sessionState.source == PomodoroSessionSource.HOME && sessionState.sessionId.isNotBlank()) {
            HistoryRecorder.recordConfigChange(
                context = context,
                configType = "POMODORO_HOME_STARTED",
                oldValue = "",
                newValue = pomodoroSessionPayload(
                    state = sessionState,
                    outcome = "STARTED",
                    focusMinutes = 0,
                    focusScore = 0
                ),
                sourceScreen = "POMODORO_CHAMBER"
            )
        }
    }

    PomodoroChamberScreen(
        state = sessionState,
        nowMillis = nowMillis,
        completionCeremony = completionCeremony,
        onEndRun = {
            val ceremonyState = buildCeremonyState(sessionState, "BROKEN")
            completionCeremony = ceremonyState
            soundManager.playTemptationBlocked()
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    )

    LaunchedEffect(completionCeremony) {
        val ceremony = completionCeremony ?: return@LaunchedEffect
        delay(1600)
        finishSession(context, engine, sessionState, ceremony.outcome, ceremony.focusMinutes, ceremony.focusScore, onClose)
    }
}

private data class ImplosionParticle(
    val startRadius: Float,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val delay: Float
)

@Composable
private fun PomodoroChamberScreen(
    state: PomodoroSessionState,
    nowMillis: Long,
    completionCeremony: PomodoroCeremonyState?,
    onEndRun: () -> Unit
) {
    val isBreak = state.isBreakPhase
    val infiniteTransition = rememberInfiniteTransition(label = "pomodoro_chamber_motion")
    val accent = when (state.phase) {
        PomodoroPhase.WORK -> ErrorRed
        PomodoroPhase.SHORT_BREAK -> NeonCyan
        PomodoroPhase.LONG_BREAK -> NeonCyan
        else -> WarningAmber
    }
    val background = when (state.phase) {
        PomodoroPhase.WORK -> listOf(DeepBackground, DeepBackground, DeepBackground)
        PomodoroPhase.SHORT_BREAK -> listOf(DeepBackground, SurfaceElevated, DeepBackground)
        PomodoroPhase.LONG_BREAK -> listOf(DeepBackground, SurfaceDark, DeepBackground)
        else -> listOf(DeepBackground, DeepBackground, DeepBackground)
    }
    val phaseRemainingMillis = (state.phaseEndsAt - nowMillis).coerceAtLeast(0L)
    val sessionRemainingMillis = if (state.sessionHardEndsAt > 0L) {
        (state.sessionHardEndsAt - nowMillis).coerceAtLeast(0L)
    } else {
        phaseRemainingMillis
    }
    val phaseDurationMillis = when (state.phase) {
        PomodoroPhase.WORK -> state.preset.workMinutes * 60_000L
        PomodoroPhase.SHORT_BREAK -> state.preset.shortBreakMinutes * 60_000L
        PomodoroPhase.LONG_BREAK -> state.preset.longBreakMinutes * 60_000L
        else -> 1L
    }.coerceAtLeast(1L)
    val progress = (phaseRemainingMillis.toFloat() / phaseDurationMillis.toFloat()).coerceIn(0f, 1f)
    val progressSweep by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "pomodoro_chamber_progress"
    )
    val chamberPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pomodoro_chamber_pulse"
    )
    val orbitSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBreak) 9000 else 5200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pomodoro_chamber_orbit"
    )
    val implosionTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pomodoro_chamber_implosion"
    )
    val particles = remember {
        List(80) {
            ImplosionParticle(
                startRadius = 1.0f + (Math.random().toFloat() * 0.5f),
                angle = Math.random().toFloat() * 360f,
                speed = 0.5f + (Math.random().toFloat() * 1.5f),
                size = 1f + (Math.random().toFloat() * 3f),
                delay = Math.random().toFloat() * 100f
            )
        }
    }
    val haloDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBreak) 14000 else 11000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pomodoro_halo_drift"
    )
    val shardPulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBreak) 3600 else 2800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pomodoro_shard_pulse"
    )
    val phaseTitle = when (state.phase) {
        PomodoroPhase.WORK -> "FOCUS PRESSURE"
        PomodoroPhase.SHORT_BREAK -> "BREAK UNLOCKED"
        PomodoroPhase.LONG_BREAK -> "LONG BREAK OPEN"
        else -> "RUN ACTIVE"
    }
    val phaseSupport = when (state.phase) {
        PomodoroPhase.WORK -> "VOID SEALED"
        PomodoroPhase.SHORT_BREAK -> "SIGNAL OPEN"
        PomodoroPhase.LONG_BREAK -> "RECOVER CLEAN"
        else -> "RUN ACTIVE"
    }
    val totalRunProgress = if (state.sessionHardEndsAt > 0L && state.startedAt > 0L) {
        val total = (state.sessionHardEndsAt - state.startedAt).coerceAtLeast(1L)
        1f - (sessionRemainingMillis.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        1f - progressSweep
    }
    val timeParts = formatDialParts(phaseRemainingMillis)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(background))
            .padding(20.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val upperOrbColor = lerp(
                accent.copy(alpha = 0.08f),
                TextPrimary.copy(alpha = 0.05f),
                haloDrift
            )
            val lowerOrbColor = lerp(
                accent.copy(alpha = 0.05f),
                TextPrimary.copy(alpha = if (isBreak) 0.08f else 0.04f),
                haloDrift
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(upperOrbColor, Color.Transparent)
                ),
                radius = size.minDimension * (0.46f + haloDrift * 0.08f),
                center = Offset(size.width * 0.78f, size.height * 0.18f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(lowerOrbColor, Color.Transparent)
                ),
                radius = size.minDimension * (0.38f + (1f - haloDrift) * 0.07f),
                center = Offset(size.width * 0.18f, size.height * 0.82f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChamberStatusPill(
                        text = "CHAMBER LIVE",
                        accent = accent
                    )
                    ChamberStatusPill(
                        text = if (state.source == PomodoroSessionSource.HOME) "HOME RUN" else "TASK RUN",
                        accent = Color.White.copy(alpha = 0.88f),
                        filled = false
                    )
                }

                Text(
                    text = state.title.ifBlank { "Some Work" }.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    lineHeight = 33.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChamberStatusPill(
                        text = phaseTitle,
                        accent = accent
                    )
                    Text(
                        text = phaseSupport,
                        color = Color.White.copy(alpha = 0.54f),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(352.dp)
                        .scale(chamberPulse),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 22.dp.toPx()
                        val outerStroke = 30.dp.toPx()
                        val markerRadius = size.minDimension / 2.34f
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    accent.copy(alpha = 0.10f + (totalRunProgress * 0.08f)),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 1.65f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    accent.copy(alpha = if (isBreak) 0.14f + (totalRunProgress * 0.08f) else 0.18f + (totalRunProgress * 0.14f)),
                                    WarningAmber.copy(alpha = 0.04f + (totalRunProgress * 0.08f)),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / (2.18f - (totalRunProgress * 0.24f))
                        )
                        drawArc(
                            color = Color.White.copy(alpha = 0.04f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                            size = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(stroke / 2f, stroke / 2f),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(accent.copy(alpha = 0.5f), accent, accent.copy(alpha = 0.5f))
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * progressSweep,
                            useCenter = false,
                            topLeft = Offset(stroke / 2f, stroke / 2f),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        rotate(orbitSweep) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(Color.Transparent, accent.copy(alpha = 0.85f), Color.Transparent)
                                ),
                                startAngle = -36f,
                                sweepAngle = 72f,
                                useCenter = false,
                                topLeft = Offset(outerStroke / 2f, outerStroke / 2f),
                                size = Size(size.width - outerStroke, size.height - outerStroke),
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        rotate(-orbitSweep * 0.65f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(Color.Transparent, Color.White.copy(alpha = 0.22f), Color.Transparent)
                                ),
                                startAngle = -18f,
                                sweepAngle = 34f,
                                useCenter = false,
                                topLeft = Offset(46.dp.toPx(), 46.dp.toPx()),
                                size = Size(size.width - 92.dp.toPx(), size.height - 92.dp.toPx()),
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        
                        // Particle Implosion
                        particles.forEach { p ->
                            val progress = ((implosionTime * p.speed + p.delay) % 100f) / 100f
                            val currentRadius = size.minDimension * p.startRadius * (1f - progress)
                            val currentAngle = Math.toRadians((p.angle + progress * 180f).toDouble())
                            
                            val px = center.x + kotlin.math.cos(currentAngle).toFloat() * currentRadius
                            val py = center.y + kotlin.math.sin(currentAngle).toFloat() * currentRadius
                            
                            val alpha = if (progress < 0.1f) {
                                progress / 0.1f
                            } else if (progress > 0.8f) {
                                (1f - progress) / 0.2f
                            } else {
                                1f
                            }
                            
                            drawCircle(
                                color = accent.copy(alpha = alpha * 0.6f),
                                radius = p.size.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }

                        repeat(12) { index ->
                            val angle = Math.toRadians((index * 30f - 90f).toDouble())
                            val dotCenter = Offset(
                                x = center.x + kotlin.math.cos(angle).toFloat() * markerRadius,
                                y = center.y + kotlin.math.sin(angle).toFloat() * markerRadius
                            )
                            drawCircle(
                                color = if (index < state.completedWorkIntervals.coerceAtMost(12)) {
                                    accent.copy(alpha = 0.95f)
                                } else {
                                    Color.White.copy(alpha = 0.12f)
                                },
                                radius = if (index < state.completedWorkIntervals.coerceAtMost(12)) 4.5.dp.toPx() else 3.dp.toPx(),
                                center = dotCenter
                            )
                        }
                        drawCircle(
                            color = Color.White.copy(alpha = 0.03f),
                            radius = size.minDimension / 3.6f
                        )
                        rotate(45f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color.Transparent,
                                        accent.copy(alpha = 0.26f * shardPulse),
                                        Color.Transparent
                                    )
                                ),
                                startAngle = -22f,
                                sweepAngle = 44f,
                                useCenter = false,
                                topLeft = Offset(78.dp.toPx(), 78.dp.toPx()),
                                size = Size(size.width - 156.dp.toPx(), size.height - 156.dp.toPx()),
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.035f),
                        shape = RoundedCornerShape(44.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(44.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.05f),
                                            Color.Transparent,
                                            accent.copy(alpha = 0.04f)
                                        )
                                    )
                                )
                                .padding(horizontal = 28.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (state.phase) {
                                    PomodoroPhase.WORK -> "FOCUS"
                                    PomodoroPhase.SHORT_BREAK -> "BREAK"
                                    PomodoroPhase.LONG_BREAK -> "LONG BREAK"
                                    else -> "RUN"
                                },
                                color = accent,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.8.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ChamberDialTimeDisplay(
                                major = timeParts.first,
                                minor = timeParts.second,
                                accent = accent
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ChamberStatusPill(
                                text = if (isBreak) "UNTIL FOCUS RETURNS" else "UNTIL BREAK UNLOCKS",
                                accent = Color.White.copy(alpha = 0.78f),
                                filled = false
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            IntervalNodeRow(
                                completed = state.completedWorkIntervals,
                                activeIndex = state.intervalIndex,
                                accent = accent
                            )
                        }
                    }
                }

                completionCeremony?.let { ceremony ->
                    CompletionCeremonyCard(ceremony = ceremony, accent = accent)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChamberMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "RUN LEFT",
                        value = formatMillis(sessionRemainingMillis),
                        accent = accent
                    )
                    ChamberMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "CYCLE",
                        value = "${state.intervalIndex}",
                        accent = accent
                    )
                    ChamberMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "BREACH",
                        value = "${state.breachCount}",
                        accent = accent
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildSessionWindowLabel(state),
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isBreak) "RESET WINDOW" else "LOCKED RUN",
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.9.sp
                        )
                    }
                }
            }
            } // end of scrollable column

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onEndRun,
                    enabled = completionCeremony == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent.copy(alpha = 0.22f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "END RUN",
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ChamberStatusPill(
    text: String,
    accent: Color,
    filled: Boolean = true
) {
    Surface(
        color = if (filled) accent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (filled) 0.12f else 0.18f))
    ) {
        Text(
            text = text,
            color = accent,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.08.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun IntervalNodeRow(
    completed: Int,
    activeIndex: Int,
    accent: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val cycleNumber = index + 1
            val isCompleted = cycleNumber <= completed
            val isActive = cycleNumber == activeIndex
            Surface(
                color = when {
                    isCompleted -> accent.copy(alpha = 0.22f)
                    isActive -> Color.White.copy(alpha = 0.12f)
                    else -> Color.White.copy(alpha = 0.05f)
                },
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(
                    1.dp,
                    when {
                        isCompleted -> accent.copy(alpha = 0.7f)
                        isActive -> Color.White.copy(alpha = 0.22f)
                        else -> Color.White.copy(alpha = 0.08f)
                    }
                )
            ) {
                Text(
                    text = cycleNumber.toString(),
                    color = if (isCompleted) accent else Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun ChamberMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.56f),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChamberDialTimeDisplay(
    major: String,
    minor: String,
    accent: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChamberDialTile(
            value = major,
            suffix = "MIN",
            accent = accent
        )
        Text(
            text = ":",
            color = Color.White.copy(alpha = 0.52f),
            fontWeight = FontWeight.Black,
            fontSize = 38.sp
        )
        ChamberDialTile(
            value = minor,
            suffix = "SEC",
            accent = Color.White.copy(alpha = 0.82f)
        )
    }
}

@Composable
private fun ChamberDialTile(
    value: String,
    suffix: String,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = suffix,
                color = accent.copy(alpha = 0.92f),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 40.sp
            )
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatDialParts(millis: Long): Pair<String, String> {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d".format(minutes) to "%02d".format(seconds)
}

private data class PomodoroCeremonyState(
    val outcome: String,
    val title: String,
    val detail: String,
    val focusMinutes: Int,
    val focusScore: Int
)

private fun buildCeremonyState(
    state: PomodoroSessionState,
    outcome: String
): PomodoroCeremonyState {
    val focusMinutes = state.completedWorkIntervals * state.preset.workMinutes
    val penalty = if (outcome == "COMPLETE") 0 else 15
    val focusScore = (100 - (state.breachCount * 20) - penalty).coerceIn(0, 100)
    return PomodoroCeremonyState(
        outcome = outcome,
        title = when (outcome) {
            "COMPLETE" -> "RUN SEALED"
            "EMERGENCY" -> "RUN ABORTED"
            else -> "RUN BROKEN"
        },
        detail = when (outcome) {
            "COMPLETE" -> "The chamber held. Launching summary."
            "EMERGENCY" -> "Emergency pressure forced a shutdown."
            else -> "Focus cracked before the window closed."
        },
        focusMinutes = focusMinutes,
        focusScore = focusScore
    )
}

private fun finishSession(
    context: Context,
    engine: PomodoroEngine,
    state: PomodoroSessionState,
    outcome: String,
    focusMinutes: Int,
    focusScore: Int,
    onClose: () -> Unit
) {
    if (state.source == PomodoroSessionSource.TASK) {
        CoroutineScope(Dispatchers.IO).launch {
            if (outcome == "COMPLETE") {
                val res = MissionExecutionService(context).completeMission(
                    taskId = state.taskId!!,
                    completionSource = "POMODORO_COMPLETE",
                    eventReason = "POMODORO_COMPLETED"
                )
                if (res is MissionExecutionResult.Completed) {
                    context.startActivity(
                        PomodoroSummaryActivity.createIntent(
                            context = context,
                            taskName = state.title.ifBlank { "Some Work" },
                            outcome = outcome,
                            intervalsCompleted = res.pomodoroSummary?.intervalsCompleted ?: state.completedWorkIntervals,
                            focusMinutes = res.pomodoroSummary?.focusMinutes ?: focusMinutes,
                            breachCount = res.pomodoroSummary?.breachCount ?: state.breachCount,
                            focusScore = res.pomodoroSummary?.focusScore ?: focusScore,
                            source = state.source.name,
                            windowLabel = buildSessionWindowLabel(state)
                        )
                    )
                }
            } else if (outcome == "EMERGENCY") {
                val finalState = engine.breakSession()
                context.startActivity(
                    PomodoroSummaryActivity.createIntent(
                        context = context,
                        taskName = finalState.title.ifBlank { "Some Work" },
                        outcome = outcome,
                        intervalsCompleted = finalState.completedWorkIntervals,
                        focusMinutes = focusMinutes,
                        breachCount = finalState.breachCount,
                        focusScore = focusScore,
                        source = finalState.source.name,
                        windowLabel = buildSessionWindowLabel(finalState)
                    )
                )
            } else {
                val res = MissionExecutionService(context).skipMission(
                    taskId = state.taskId!!,
                    skipReason = "POMODORO_BROKEN",
                    eventReason = "POMODORO_BROKEN",
                    failureContext = MissionFailureContext.POMODORO_BREAK
                )
                if (res is MissionExecutionResult.Skipped) {
                    context.startActivity(
                        PomodoroSummaryActivity.createIntent(
                            context = context,
                            taskName = state.title.ifBlank { "Some Work" },
                            outcome = outcome,
                            intervalsCompleted = res.pomodoroSummary?.intervalsCompleted ?: state.completedWorkIntervals,
                            focusMinutes = res.pomodoroSummary?.focusMinutes ?: focusMinutes,
                            breachCount = res.pomodoroSummary?.breachCount ?: state.breachCount,
                            focusScore = res.pomodoroSummary?.focusScore ?: focusScore,
                            source = state.source.name,
                            windowLabel = buildSessionWindowLabel(state)
                        )
                    )
                }
            }
        }
    } else {
        val finalState = when (outcome) {
            "COMPLETE" -> engine.completeSession()
            else -> engine.breakSession()
        }
        HistoryRecorder.recordConfigChange(
            context = context,
            configType = when (outcome) {
                "COMPLETE" -> "POMODORO_HOME_COMPLETED"
                "EMERGENCY" -> "POMODORO_HOME_EMERGENCY_EXIT"
                else -> "POMODORO_HOME_BROKEN"
            },
            oldValue = "",
            newValue = pomodoroSessionPayload(
                state = finalState,
                outcome = outcome,
                focusMinutes = focusMinutes,
                focusScore = focusScore
            ),
            sourceScreen = "POMODORO_CHAMBER"
        )
        context.startActivity(
            PomodoroSummaryActivity.createIntent(
                context = context,
                taskName = finalState.title.ifBlank { "Some Work" },
                outcome = outcome,
                intervalsCompleted = finalState.completedWorkIntervals,
                focusMinutes = focusMinutes,
                breachCount = finalState.breachCount,
                focusScore = focusScore,
                source = finalState.source.name,
                windowLabel = buildSessionWindowLabel(finalState)
            )
        )
    }
    onClose()
}

@Composable
private fun CompletionCeremonyCard(
    ceremony: PomodoroCeremonyState,
    accent: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ceremony.title,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ceremony.detail,
                color = Color.White.copy(alpha = 0.84f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${ceremony.focusMinutes} MIN · SCORE ${ceremony.focusScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

private fun pomodoroSessionPayload(
    state: PomodoroSessionState,
    outcome: String,
    focusMinutes: Int,
    focusScore: Int
): List<String> {
    return listOf(
        "SESSION_ID=${state.sessionId}",
        "TITLE=${state.title.ifBlank { "Some Work" }}",
        "SOURCE=${state.source.name}",
        "START=${state.startTime}",
        "END=${state.endTime}",
        "DURATION=${state.totalDurationMinutes}",
        "INTERVALS=${state.completedWorkIntervals}",
        "MINUTES=$focusMinutes",
        "BREACHES=${state.breachCount}",
        "SCORE=$focusScore",
        "OUTCOME=$outcome"
    )
}

private fun buildSessionWindowLabel(state: PomodoroSessionState): String {
    return if (state.source == PomodoroSessionSource.HOME && state.totalDurationMinutes > 0) {
        "${formatChamberDurationLabel(state.totalDurationMinutes)} RUN"
    } else {
        "${state.startTime} -> ${state.endTime}"
    }
}

private fun formatChamberDurationLabel(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}H ${minutes}M"
        hours > 0 -> "${hours}H"
        else -> "${minutes}M"
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF020205)
@androidx.compose.runtime.Composable
fun PomodoroChamberPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PomodoroChamberPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun PomodoroChamberPreviewContent() {
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
                "🔥 FOCUS MODE",
                color = ErrorRed,
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                letterSpacing = 4.sp
            )
            androidx.compose.material3.Text(
                "24:37",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 64.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black
            )
            androidx.compose.material3.Text(
                "Deep Work Block · Round 2/4",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}
