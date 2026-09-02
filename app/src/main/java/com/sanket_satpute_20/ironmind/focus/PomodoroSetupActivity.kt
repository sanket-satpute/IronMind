package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.components.RotaryDualRingPicker
import com.sanket_satpute_20.ironmind.gamification.SoundManager
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class PomodoroSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialTitle = intent.getStringExtra(EXTRA_INITIAL_TITLE).orEmpty()
        setContent {
            IronMindTheme {
                PomodoroSetupScreen(
                    initialTitle = initialTitle,
                    onBack = { finish() },
                    onStart = { config ->
                        PomodoroEngine(this).startForSession(config)
                        startActivity(PomodoroChamberActivity.createIntent(this))
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        private const val EXTRA_INITIAL_TITLE = "extra_initial_title"

        fun createIntent(
            context: Context,
            initialTitle: String = ""
        ): Intent =
            Intent(context, PomodoroSetupActivity::class.java).apply {
                putExtra(EXTRA_INITIAL_TITLE, initialTitle)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
    }
}

@Composable
private fun PomodoroSetupScreen(
    initialTitle: String,
    onBack: () -> Unit,
    onStart: (PomodoroSessionConfig) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    var title by rememberSaveable(initialTitle) { mutableStateOf(initialTitle) }
    var selectedPreset by rememberSaveable { mutableStateOf(PomodoroPreset.CLASSIC_25_5) }
    var totalDurationMinutes by rememberSaveable { mutableIntStateOf(90) }
    val scrollState = rememberScrollState()
    val durationHours = totalDurationMinutes / 60
    val durationMinuteBucket = totalDurationMinutes % 60

    val runBreakdown = remember(totalDurationMinutes, selectedPreset) {
        estimatePomodoroBreakdown(totalDurationMinutes, selectedPreset)
    }
    val finishTimeLabel = remember(totalDurationMinutes) {
        LocalTime.now().plusMinutes(totalDurationMinutes.toLong()).format(DateTimeFormatter.ofPattern("h:mm a"))
    }
    val runClass = remember(totalDurationMinutes) {
        when {
            totalDurationMinutes <= 30 -> "Quick Strike"
            totalDurationMinutes <= 60 -> "Standard Run"
            totalDurationMinutes <= 120 -> "Deep Dive"
            else -> "Endurance Chamber"
        }
    }
    val runIntensityColor = remember(totalDurationMinutes) {
        when {
            totalDurationMinutes <= 30 -> NeonCyan
            totalDurationMinutes <= 90 -> WarningAmber
            else -> WarningAmber
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
    ) {
        val compactLayout = maxWidth < 380.dp
        val heroSize = when {
            maxWidth < 360.dp -> 212.dp
            maxWidth < 420.dp -> 240.dp
            else -> 280.dp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 156.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = ElectricViolet.copy(alpha = 0.10f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.24f))
            ) {
                Text(
                    text = "POMODORO CHAMBER",
                    color = ElectricViolet,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.3.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }

            Text(
                text = "Start A Focus Run",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = if (compactLayout) 28.sp else 32.sp,
                lineHeight = if (compactLayout) 31.sp else 34.sp
            )

            Text(
                text = "Pick the duration, choose the rhythm, and go straight into the chamber.",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            ChamberStatusBanner(
                runClass = runClass,
                accent = runIntensityColor,
                finishTimeLabel = finishTimeLabel,
                focusMinutes = runBreakdown.focusMinutes
            )

            ChamberHeroCard(
                durationMinutes = totalDurationMinutes,
                breakdown = runBreakdown,
                durationHours = durationHours,
                durationMinuteBucket = durationMinuteBucket,
                heroSize = heroSize,
                runClass = runClass,
                runIntensityColor = runIntensityColor,
                onDialStep = {
                    soundManager.playTimerTick()
                },
                onHoursChange = { hours ->
                    totalDurationMinutes = normalizeDurationMinutes(hours * 60 + durationMinuteBucket)
                },
                onMinutesChange = { minutes ->
                    totalDurationMinutes = normalizeDurationMinutes(durationHours * 60 + minutes)
                }
            )

            ChamberForecastStrip(
                finishTimeLabel = finishTimeLabel,
                breakdown = runBreakdown,
                totalDurationMinutes = totalDurationMinutes
            )

            QuickDurationRow(
                selectedMinutes = totalDurationMinutes,
                onSelect = { totalDurationMinutes = it }
            )

            PresetSelectorRow(
                selectedPreset = selectedPreset,
                onPresetSelected = { selectedPreset = it }
            )

            MissionTagCard(
                title = title,
                onTitleChange = { title = it }
            )

            PomodoroSetupSummaryCard(
                durationMinutes = totalDurationMinutes,
                breakdown = runBreakdown,
                preset = selectedPreset,
                runClass = runClass
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = DeepBackground.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ready: ${runClass.lowercase()} · ends $finishTimeLabel",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Button(
                    onClick = {
                        val safeTitle = title.trim().ifBlank { "Some Work" }
                        val now = System.currentTimeMillis()
                        val hardEnd = now + totalDurationMinutes * 60_000L
                        onStart(
                            PomodoroSessionConfig(
                                sessionId = "home-$now",
                                source = PomodoroSessionSource.HOME,
                                taskId = null,
                                title = safeTitle,
                                date = LocalDate.now().toString(),
                                startTime = "NOW",
                                endTime = formatDurationLabel(totalDurationMinutes),
                                totalDurationMinutes = totalDurationMinutes,
                                preset = selectedPreset,
                                sessionHardEndsAt = hardEnd
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricViolet,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "START TIMER",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.9.sp,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Back",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChamberHeroCard(
    durationMinutes: Int,
    breakdown: PomodoroRunBreakdown,
    durationHours: Int,
    durationMinuteBucket: Int,
    heroSize: Dp,
    runClass: String,
    runIntensityColor: Color,
    onDialStep: () -> Unit,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit
) {
    val animatedDuration by animateFloatAsState(
        targetValue = durationMinutes.toFloat(),
        animationSpec = tween(500),
        label = "pomodoro_setup_duration"
    )
    val durationProgress = (animatedDuration / 300f).coerceIn(0f, 1f)
    val glowScale by animateFloatAsState(
        targetValue = 0.96f + (durationProgress * 0.08f),
        animationSpec = tween(500),
        label = "pomodoro_setup_glow_scale"
    )
    val orbitMotion = rememberInfiniteTransition(label = "pomodoro_setup_orbit")
    val orbitSweep by orbitMotion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((5200 - (durationProgress * 1800f)).toInt().coerceAtLeast(2400)),
            repeatMode = RepeatMode.Restart
        ),
        label = "pomodoro_setup_orbit_sweep"
    )
    val auraPulse by orbitMotion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pomodoro_setup_aura_pulse"
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(heroSize)
                    .scale(glowScale * auraPulse),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val strokeOuter = 10.dp.toPx()
                    val strokeInner = 5.dp.toPx()
                    val diameter = size.minDimension - strokeOuter
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                ElectricViolet.copy(alpha = 0.10f + (durationProgress * 0.22f)),
                                WarningAmber.copy(alpha = 0.04f + (durationProgress * 0.12f)),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / (2.4f - (durationProgress * 0.35f))
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.06f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                ElectricViolet.copy(alpha = 0.18f),
                                WarningAmber.copy(alpha = 0.9f),
                                NeonCyan.copy(alpha = 0.72f),
                                ElectricViolet.copy(alpha = 0.18f)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = 50f + (280f * durationProgress),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                    )
                    rotate(orbitSweep, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    WarningAmber.copy(alpha = 0.15f),
                                    WarningAmber.copy(alpha = 0.92f),
                                    Color.Transparent
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = 28f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                        )
                    }
                }

                DualRingDurationPicker(
                    modifier = Modifier.size(heroSize * 0.9f),
                    hour = durationHours,
                    minute = durationMinuteBucket,
                    onDialStep = {
                        onDialStep()
                    },
                    onHourChange = onHoursChange,
                    onMinuteChange = onMinutesChange
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = runClass.uppercase(),
                    color = runIntensityColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "${breakdown.workIntervals} focus bursts armed",
                    color = Color.White.copy(alpha = 0.62f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroMetricPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Bolt,
                    label = "FOCUS",
                    value = "${breakdown.focusMinutes}M",
                    accent = WarningAmber
                )
                HeroMetricPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.NightsStay,
                    label = "RESET",
                    value = "${breakdown.recoveryMinutes}M",
                    accent = NeonCyan
                )
                HeroMetricPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.RocketLaunch,
                    label = "PUSHES",
                    value = "${breakdown.workIntervals}",
                    accent = ElectricViolet
                )
            }
        }
    }
}

@Composable
private fun ChamberStatusBanner(
    runClass: String,
    accent: Color,
    finishTimeLabel: String,
    focusMinutes: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.RocketLaunch,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = runClass.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$focusMinutes focused minutes projected · chamber clears at $finishTimeLabel",
                    color = Color.White.copy(alpha = 0.66f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun ChamberForecastStrip(
    finishTimeLabel: String,
    breakdown: PomodoroRunBreakdown,
    totalDurationMinutes: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ForecastPill(label = "Focus", value = "${breakdown.focusMinutes}m", accent = WarningAmber)
            ForecastPill(label = "Recovery", value = "${breakdown.recoveryMinutes}m", accent = NeonCyan)
            ForecastPill(label = "Bursts", value = breakdown.workIntervals.toString(), accent = ElectricViolet)
            ForecastPill(label = "Finish", value = finishTimeLabel, accent = WarningAmber)
            ForecastPill(label = "Run", value = formatDurationLabel(totalDurationMinutes), accent = NeonCyan)
        }
    }
}

@Composable
private fun ForecastPill(
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MissionTagCard(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SESSION LABEL · OPTIONAL",
                color = Color.White.copy(alpha = 0.66f),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.1.sp
            )
            Text(
                text = "Leave this blank if you just want to start quickly.",
                color = Color.White.copy(alpha = 0.52f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Optional label") },
                placeholder = { Text("Deep Work / Study Sprint / Admin Cleanup") },
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

@Composable
private fun DualRingDurationPicker(
    modifier: Modifier = Modifier,
    hour: Int,
    minute: Int,
    onDialStep: () -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    var localHour by remember { mutableIntStateOf(hour) }
    var localMinute by remember { mutableIntStateOf(minute) }
    LaunchedEffect(hour) {
        localHour = hour
    }
    LaunchedEffect(minute) {
        localMinute = minute
    }

    RotaryDualRingPicker(
        modifier = modifier,
        primaryValue = localHour,
        secondaryValue = localMinute,
        primaryMaxValue = 4,
        secondaryMaxValue = 55,
        primaryStepDegrees = 72f,
        secondaryStepDegrees = 30f,
        primaryStepValue = 1,
        secondaryStepValue = 5,
        primaryAccent = ElectricViolet,
        secondaryAccent = NeonCyan,
        primaryMarkerCount = 60,
        secondaryMarkerCount = 12,
        primaryStrongMarkerEvery = 12,
        primaryGearStyle = true,
        primaryWrap = false,
        secondaryWrap = false,
        onPrimaryValueChange = { updated ->
            localHour = updated
            onHourChange(updated)
            onDialStep()
        },
        onSecondaryValueChange = { updated ->
            localMinute = updated
            onMinuteChange(updated)
            onDialStep()
        }
    ) { _, displayHour, displayMinute ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${displayHour}:${"%02d".format(displayMinute)}",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 42.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun HeroMetricPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun QuickDurationRow(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "RUN LENGTH",
            color = Color.White.copy(alpha = 0.62f),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(30, 45, 60, 90, 120, 150).forEach { minutes ->
                DurationChip(
                    minutes = minutes,
                    selected = selectedMinutes == minutes,
                    onClick = { onSelect(minutes) }
                )
            }
        }
    }
}

@Composable
private fun DurationChip(
    minutes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) ElectricViolet.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (selected) ElectricViolet.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatDurationLabel(minutes),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = when {
                    minutes <= 30 -> "Quick"
                    minutes <= 60 -> "Balanced"
                    minutes <= 120 -> "Deep"
                    else -> "Endurance"
                },
                color = Color.White.copy(alpha = 0.56f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun PresetSelectorRow(
    selectedPreset: PomodoroPreset,
    onPresetSelected: (PomodoroPreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "COMBAT STYLE",
            color = Color.White.copy(alpha = 0.62f),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PomodoroPresetChip(
                label = "Classic",
                detail = "Steady cadence",
                selected = selectedPreset == PomodoroPreset.CLASSIC_25_5,
                onClick = { onPresetSelected(PomodoroPreset.CLASSIC_25_5) }
            )
            PomodoroPresetChip(
                label = "Deep Work",
                detail = "Long pressure",
                selected = selectedPreset == PomodoroPreset.DEEP_50_10,
                onClick = { onPresetSelected(PomodoroPreset.DEEP_50_10) }
            )
            PomodoroPresetChip(
                label = "Quick Strike",
                detail = "Fast cycle",
                selected = selectedPreset == PomodoroPreset.QUICK_15_5,
                onClick = { onPresetSelected(PomodoroPreset.QUICK_15_5) }
            )
        }
    }
}

@Composable
private fun PomodoroPresetChip(
    label: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) ElectricViolet.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (selected) ElectricViolet.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 110.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail,
                color = if (selected) ElectricViolet else Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun PomodoroSetupSummaryCard(
    durationMinutes: Int,
    breakdown: PomodoroRunBreakdown,
    preset: PomodoroPreset,
    runClass: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "RUN READ",
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "$runClass · ${formatDurationLabel(durationMinutes)}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = "${breakdown.focusMinutes} focus minutes across ${breakdown.workIntervals} work bursts. Break windows total ${breakdown.recoveryMinutes} minutes on the ${preset.workMinutes}/${preset.shortBreakMinutes} rhythm.",
                color = Color.White.copy(alpha = 0.74f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

private data class PomodoroRunBreakdown(
    val workIntervals: Int,
    val focusMinutes: Int,
    val recoveryMinutes: Int
)

private fun estimatePomodoroBreakdown(
    durationMinutes: Int,
    preset: PomodoroPreset
): PomodoroRunBreakdown {
    if (durationMinutes <= 0) return PomodoroRunBreakdown(0, 0, 0)
    var remaining = durationMinutes
    var workIntervals = 0
    var focusMinutes = 0
    var recoveryMinutes = 0
    while (remaining > 0) {
        val workSlice = minOf(remaining, preset.workMinutes)
        focusMinutes += workSlice
        remaining -= workSlice
        workIntervals += 1
        if (remaining <= 0) break
        val breakSlice = if (workIntervals % preset.cyclesBeforeLongBreak == 0) {
            preset.longBreakMinutes
        } else {
            preset.shortBreakMinutes
        }
        val actualBreak = minOf(remaining, breakSlice)
        recoveryMinutes += actualBreak
        remaining -= actualBreak
    }
    return PomodoroRunBreakdown(workIntervals, focusMinutes, recoveryMinutes)
}

private fun normalizeDurationMinutes(total: Int): Int {
    return when {
        total <= 15 -> 15
        total >= 295 -> 300
        else -> {
            val snapped = ((total + 2) / 5) * 5
            if (snapped == 0) 15 else snapped
        }
    }
}

private fun formatDurationLabel(totalMinutes: Int): String {
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun PomodoroSetupScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PomodoroSetupPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun PomodoroSetupPreviewContent() {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(20.dp)
    ) {
        androidx.compose.material3.Text(
            "⚙️ POMODORO SETUP",
            color = ErrorRed,
            fontSize = 22.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black
        )
        androidx.compose.material3.Text(
            "Deep Work Block",
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 18.sp
        )
        androidx.compose.material3.Text(
            "Focus: 25 min · Break: 5 min · 4 rounds",
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
        androidx.compose.material3.Button(
            onClick = {},
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = ErrorRed
            )
        ) {
            androidx.compose.material3.Text("Start Session", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}
