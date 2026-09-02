package com.sanket_satpute_20.ironmind.morninglaunch

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockSoundCatalog
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockSoundController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

private data class MorningStepTransitionData(
    val accent: Color,
    val xpGain: Int,
    val completedTitle: String,
    val nextTitle: String,
    val nextBenefit: String,
    val nextIndex: Int
)

private data class WarriorStepSummary(
    val stepId: String,
    val title: String,
    val actualLabel: String,
    val xpGain: Int
)

@Composable
fun MorningMonkFlow(
    steps: List<MorningLaunchStep>,
    onStepChanged: (Int) -> Unit,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PrefManager.getInstance(context) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var journalText by remember { mutableStateOf("") }
    var transition by remember { mutableStateOf<MorningStepTransitionData?>(null) }

    LaunchedEffect(currentIndex) {
        onStepChanged(currentIndex.coerceAtMost(steps.lastIndex))
    }

    transition?.let { data ->
        MorningStepTransitionScreen(
            accent = data.accent,
            xpGain = data.xpGain,
            completedTitle = data.completedTitle,
            nextTitle = data.nextTitle,
            nextBenefit = data.nextBenefit,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onFinished = {
                transition = null
                currentIndex = data.nextIndex
            }
        )
        return
    }

    if (currentIndex >= steps.size) {
        MorningMonkCompleteScreen(
            reinforcement = MorningPromptRepository.reinforcementForMonk(prefs),
            onComplete = onComplete
        )
        return
    }

    val currentStep = steps[currentIndex]

    when (currentIndex) {
        0 -> MorningBreathingScreen(
            step = currentStep,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = {
                transition = MorningStepTransitionData(
                    accent = NeonCyan,
                    xpGain = 12,
                    completedTitle = "Breathing sealed",
                    nextTitle = "Meditation",
                    nextBenefit = "Stabilizes attention before the day gets noisy.",
                    nextIndex = 1
                )
            }
        )
        1 -> MorningMeditationScreen(
            step = currentStep,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = {
                transition = MorningStepTransitionData(
                    accent = TextPrimary,
                    xpGain = 18,
                    completedTitle = "Meditation settled",
                    nextTitle = "Journaling",
                    nextBenefit = "Turns calm attention into a deliberate first thought.",
                    nextIndex = 2
                )
            }
        )
        else -> MorningJournalScreen(
            step = currentStep,
            initialText = journalText,
            prompt = MorningPromptRepository.promptForToday(MorningLaunchMode.MONK),
            onTextChanged = { journalText = it },
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = { currentIndex = 3 }
        )
    }
}

@Composable
fun MorningWarriorFlow(
    steps: List<MorningLaunchStep>,
    onStepChanged: (Int) -> Unit,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PrefManager.getInstance(context) }
    val manager = remember { MorningLaunchManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var currentIndex by remember { mutableIntStateOf(0) }
    var transition by remember { mutableStateOf<MorningStepTransitionData?>(null) }
    var summaries by remember { mutableStateOf(listOf<WarriorStepSummary>()) }

    LaunchedEffect(currentIndex) {
        onStepChanged(currentIndex.coerceAtMost(steps.lastIndex))
    }

    transition?.let { data ->
        MorningStepTransitionScreen(
            accent = data.accent,
            xpGain = data.xpGain,
            completedTitle = data.completedTitle,
            nextTitle = data.nextTitle,
            nextBenefit = data.nextBenefit,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onFinished = {
                transition = null
                currentIndex = data.nextIndex
            }
        )
        return
    }

    if (currentIndex >= steps.size) {
        WarriorMissionCompleteScreen(
            summaries = summaries,
            reinforcement = MorningPromptRepository.reinforcementForWarrior(prefs),
            onComplete = onComplete
        )
        return
    }

    val currentStep = steps[currentIndex]
    when (currentIndex) {
        0 -> WarriorPushupScreen(
            step = currentStep,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = { actualReps, completed ->
                val xp = if (!completed) 0 else if (actualReps >= WARRIOR_PUSHUP_TARGET) 18 else 12
                summaries = summaries + WarriorStepSummary(
                    stepId = "PUSHUPS",
                    title = "Pushups",
                    actualLabel = if (completed) "$actualReps / $WARRIOR_PUSHUP_TARGET reps" else "Skipped",
                    xpGain = xp
                )
                coroutineScope.launch {
                    manager.saveStepResult(
                        stepId = "PUSHUPS",
                        stepOrder = 0,
                        targetValue = WARRIOR_PUSHUP_TARGET,
                        actualValue = actualReps,
                        unit = "REPS",
                        completed = completed,
                        skipped = !completed
                    )
                }
                transition = MorningStepTransitionData(
                    accent = WarningAmber,
                    xpGain = xp,
                    completedTitle = if (completed) "Pushups logged" else "Pushups skipped",
                    nextTitle = "Run",
                    nextBenefit = "Carry the first surge of effort into forward motion.",
                    nextIndex = 1
                )
            }
        )
        1 -> WarriorRunScreen(
            step = currentStep,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = { actualDuration, completed ->
                val xp = if (!completed) 0 else if (actualDuration >= WARRIOR_RUN_TEST_SECONDS) 24 else 16
                summaries = summaries + WarriorStepSummary(
                    stepId = "RUN",
                    title = "Run",
                    actualLabel = if (completed) "${actualDuration}s logged" else "Skipped",
                    xpGain = xp
                )
                coroutineScope.launch {
                    manager.saveStepResult(
                        stepId = "RUN",
                        stepOrder = 1,
                        targetValue = WARRIOR_RUN_TEST_SECONDS,
                        actualValue = actualDuration,
                        unit = "SECONDS",
                        completed = completed,
                        skipped = !completed
                    )
                }
                transition = MorningStepTransitionData(
                    accent = WarningAmber,
                    xpGain = xp,
                    completedTitle = if (completed) "Run logged" else "Run skipped",
                    nextTitle = "Cold Shower",
                    nextBenefit = "Finish the launch with controlled discomfort.",
                    nextIndex = 2
                )
            }
        )
        else -> WarriorColdShowerScreen(
            step = currentStep,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = { completed ->
                val xp = if (completed) 14 else 0
                summaries = summaries + WarriorStepSummary(
                    stepId = "COLD_SHOWER",
                    title = "Cold Shower",
                    actualLabel = if (completed) "Done" else "Skipped",
                    xpGain = xp
                )
                coroutineScope.launch {
                    manager.saveStepResult(
                        stepId = "COLD_SHOWER",
                        stepOrder = 2,
                        targetValue = 1,
                        actualValue = if (completed) 1 else 0,
                        unit = "BOOLEAN",
                        completed = completed,
                        skipped = !completed
                    )
                }
                currentIndex = 3
            }
        )
    }
}

@Composable
fun MorningBalancedFlow(
    steps: List<MorningLaunchStep>,
    onStepChanged: (Int) -> Unit,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PrefManager.getInstance(context) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var planText by remember { mutableStateOf("") }
    var transition by remember { mutableStateOf<MorningStepTransitionData?>(null) }
    val currentStep = steps.getOrNull(currentIndex)

    LaunchedEffect(currentIndex) {
        onStepChanged(currentIndex.coerceAtMost(steps.lastIndex))
    }

    transition?.let { data ->
        MorningStepTransitionScreen(
            accent = data.accent,
            xpGain = data.xpGain,
            completedTitle = data.completedTitle,
            nextTitle = data.nextTitle,
            nextBenefit = data.nextBenefit,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onFinished = {
                transition = null
                currentIndex = data.nextIndex
            }
        )
        return
    }

    if (currentIndex >= steps.size || currentStep == null) {
        MorningVariantCompleteScreen(
            accent = SuccessGreen,
            title = "BALANCED MORNING SEALED",
            reinforcement = MorningPromptRepository.reinforcementForBalanced(prefs),
            onComplete = onComplete
        )
        return
    }

    when (currentIndex) {
        0 -> MorningBreathingScreen(
            step = currentStep,
            accent = SuccessGreen,
            label = "BALANCED RESET",
            subtitle = "Start with steady breath, not scattered thought.",
            footer = "Bring your system online without force.",
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = {
                transition = MorningStepTransitionData(
                    accent = SuccessGreen,
                    xpGain = 10,
                    completedTitle = "Reset complete",
                    nextTitle = "Grounding",
                    nextBenefit = "Wakes the body gently so the day starts stable, not rigid.",
                    nextIndex = 1
                )
            }
        )
        1 -> MorningActivationScreen(
            step = currentStep,
            accent = TextPrimary,
            label = "BALANCED GROUNDING",
            subtitle = "Loosen the body so the day starts responsive, not stiff.",
            footer = "Lengthen. Reach. Wake the frame.",
            iconRes = R.drawable.ic_ironmind_mark_monochrome,
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = {
                transition = MorningStepTransitionData(
                    accent = TextPrimary,
                    xpGain = 14,
                    completedTitle = "Grounding locked",
                    nextTitle = "Top Three Plan",
                    nextBenefit = "Turns calm energy into a practical plan you can actually follow.",
                    nextIndex = 2
                )
            }
        )
        else -> MorningMissionLockScreen(
            step = currentStep,
            prompt = MorningPromptRepository.promptForToday(MorningLaunchMode.BALANCED),
            initialText = planText,
            onTextChanged = { planText = it },
            actionLabel = "Lock today's top three",
            placeholder = "Write the three things that matter most today...",
            emergencyRemaining = emergencyRemaining,
            onEmergencyExit = onEmergencyExit,
            onDone = { currentIndex = 3 }
        )
    }
}

@Composable
private fun MorningBreathingScreen(
    step: MorningLaunchStep,
    accent: Color = NeonCyan,
    label: String = "MONK BREATHING",
    subtitle: String = "Breathe before the world gets a vote.",
    footer: String = "Inhale. Hold. Exhale. Hold.",
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: () -> Unit
) {
    val totalSeconds = remember(step.durationSeconds) {
        step.durationSeconds
    }
    var secondsLeft by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    val elapsedSeconds = (totalSeconds - secondsLeft).coerceAtLeast(0)
    val breathPhase = remember(elapsedSeconds) { BreathPhase.fromSecond(elapsedSeconds) }
    val animatedScale by animateFloatAsState(
        targetValue = breathPhase.targetScale,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "breath_phase_scale"
    )
    val glowScale by animateFloatAsState(
        targetValue = breathPhase.glowScale,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "breath_phase_glow_scale"
    )
    val progress = if (totalSeconds == 0) 1f else (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    LaunchedEffect(totalSeconds) {
        secondsLeft = totalSeconds
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onDone()
    }

    MorningStepShell(
        accent = accent,
        label = label,
        title = step.title,
        subtitle = subtitle,
        timerLabel = formatLaunchTime(secondsLeft),
        footer = footer,
        emergencyRemaining = emergencyRemaining,
        onEmergencyExit = onEmergencyExit,
        progress = progress,
        actionLabel = null,
        onAction = null
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BreathInstructionBadge(
                accent = accent,
                phase = breathPhase
            )
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier.size(268.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 18.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.07f),
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        color = accent.copy(alpha = 0.90f),
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                            radius = size.minDimension * 0.46f
                        ),
                        radius = size.minDimension * 0.42f
                    )
                    drawCircle(
                        color = accent.copy(alpha = 0.18f),
                        radius = size.minDimension * 0.26f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = size.minDimension * 0.32f,
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f)))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(172.dp)
                        .scale(glowScale)
                        .background(accent.copy(alpha = 0.10f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .scale(animatedScale)
                        .background(accent.copy(alpha = 0.24f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .scale(animatedScale * 0.96f)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = breathPhase.actionLine,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = breathPhase.coachLine,
                color = Color.White.copy(alpha = 0.66f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(12.dp))
            BreathPhaseTrack(
                accent = accent,
                activePhase = breathPhase
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "4-7-8 RESET",
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Fill the belly on the inhale. Hold fully. Release slowly through soft lips.",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MorningActivationScreen(
    step: MorningLaunchStep,
    accent: Color,
    label: String,
    subtitle: String,
    footer: String,
    iconRes: Int,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: () -> Unit
) {
    val totalSeconds = remember(step.durationSeconds) {
        if (step.durationSeconds > 0) step.durationSeconds else 120
    }
    var secondsLeft by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    val pulse = rememberInfiniteTransition(label = "activation")
    val glow by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activation_glow"
    )

    LaunchedEffect(totalSeconds) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onDone()
    }

    MorningStepShell(
        accent = accent,
        label = label,
        title = step.title,
        subtitle = subtitle,
        timerLabel = formatLaunchTime(secondsLeft),
        footer = footer,
        emergencyRemaining = emergencyRemaining,
        onEmergencyExit = onEmergencyExit,
        progress = ((totalSeconds - secondsLeft).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f),
        actionLabel = null,
        onAction = null
    ) {
        Surface(
            color = accent.copy(alpha = 0.08f),
            shape = RoundedCornerShape(34.dp),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .scale(glow)
                        .background(accent.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.10f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Text(
                    text = "Count every rep. Stay moving until the timer dies.",
                    color = Color.White.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun WarriorPushupScreen(
    step: MorningLaunchStep,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: (actualReps: Int, completed: Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var reportMode by remember { mutableStateOf(false) }
    var completedReps by remember { mutableIntStateOf(WARRIOR_PUSHUP_TARGET) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.push_ups))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    val glowMotion = rememberInfiniteTransition(label = "push_glow")
    val glowScale by glowMotion.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "push_glow_scale"
    )
    val heroTarget by animateIntAsState(
        targetValue = if (reportMode) completedReps else WARRIOR_PUSHUP_TARGET,
        animationSpec = tween(320),
        label = "push_target_tick"
    )
    LaunchedEffect(Unit) {
        playWarriorCue(context, WarriorCueProfile.PushupsEnter)
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WarningAmber.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(540f, 220f),
                        radius = 760f
                    )
                )
        )
        Column(modifier = Modifier.fillMaxSize()) {
            WarriorTopBar(phase = "PHASE 1", headline = "PUSHUPS", activeIndex = 0)
            Spacer(Modifier.height(18.dp))
            Text(
                text = if (reportMode) "Lock the real number." else "Hit the first challenge before comfort wakes up.",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val emberY = size.height * 0.82f
                    repeat(8) { index ->
                        val startX = size.width * (index / 8f)
                        drawLine(
                            color = WarningAmber.copy(alpha = 0.08f + (index % 3) * 0.03f),
                            start = Offset(startX, emberY),
                            end = Offset((startX + size.width / 10f).coerceAtMost(size.width), emberY),
                            strokeWidth = 10.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(WarningAmber.copy(alpha = 0.16f), Color.Transparent),
                            radius = size.minDimension * 0.42f
                        ),
                        radius = size.minDimension * 0.36f * glowScale
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 26.dp, vertical = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fire_streak)).value,
                            progress = { animationProgress },
                            modifier = Modifier
                                .size(250.dp)
                                .scale(1.08f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedContent(
                                targetState = reportMode,
                                transitionSpec = {
                                    (fadeIn(tween(220)) + scaleIn(initialScale = 0.96f)) togetherWith
                                        (fadeOut(tween(180)) + scaleOut(targetScale = 1.02f))
                                },
                                label = "pushup_hero_state"
                            ) { inReport ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$heroTarget",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (inReport) 54.sp else 68.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = if (inReport) "REPS LOCKED IN" else "TARGET REPS",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.1.sp
                                    )
                                    Spacer(Modifier.height(18.dp))
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { animationProgress },
                                        modifier = Modifier.size(220.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AnimatedContent(
                        targetState = reportMode,
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(initialScale = 0.98f)) togetherWith
                                (fadeOut(tween(180)) + scaleOut(targetScale = 1.02f))
                        },
                        label = "pushup_panel_state"
                    ) { inReport ->
                        Column {
                            if (!inReport) {
                                WarriorPrimaryButton(
                                    text = "START SET",
                                    accent = WarningAmber,
                                    onClick = {
                                        playWarriorCue(context, WarriorCueProfile.PhaseStart)
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        reportMode = true
                                    }
                                )
                            } else {
                                WarriorCaptureCard(
                                    accent = WarningAmber,
                                    headline = "REPORT YOUR SET",
                                    value = completedReps,
                                    valueLabel = "completed reps",
                                    step = 1,
                                    range = 0..150,
                                    onValueChange = { completedReps = it }
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    WarriorPrimaryButton(
                                        text = "LOCK REPS",
                                        accent = WarningAmber,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            playWarriorCue(context, WarriorCueProfile.PhaseLock)
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDone(completedReps, true)
                                        }
                                    )
                                    WarriorGhostButton(
                                        text = "SKIP",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            playWarriorCue(context, WarriorCueProfile.Skip)
                                            onDone(0, false)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    EmergencyExitButton(
                        remaining = emergencyRemaining,
                        onEmergencyExit = onEmergencyExit
                    )
                }
            }
        }
    }
}

@Composable
private fun WarriorRunScreen(
    step: MorningLaunchStep,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: (actualDuration: Int, completed: Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val totalSeconds = remember(step.durationSeconds) { warriorTestDuration(step.durationSeconds) }
    var secondsLeft by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    var started by remember { mutableStateOf(false) }
    var reportMode by remember { mutableStateOf(false) }
    var actualDuration by remember { mutableIntStateOf(totalSeconds) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.running))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    val progress = if (!started) 0f else ((totalSeconds - secondsLeft).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val heroRunValue by animateIntAsState(
        targetValue = when {
            reportMode -> actualDuration
            started -> secondsLeft
            else -> WARRIOR_RUN_TEST_SECONDS
        },
        animationSpec = tween(300),
        label = "run_hero_tick"
    )
    LaunchedEffect(Unit) {
        playWarriorCue(context, WarriorCueProfile.RunEnter)
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LaunchedEffect(started, totalSeconds) {
        if (!started) return@LaunchedEffect
        secondsLeft = totalSeconds
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        playWarriorCue(context, WarriorCueProfile.ReportReady)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        reportMode = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WarningAmber.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(760f, 260f),
                        radius = 900f
                    )
                )
        )
        Column(modifier = Modifier.fillMaxSize()) {
            WarriorTopBar(phase = "PHASE 2", headline = "RUN", activeIndex = 1)
            Spacer(Modifier.height(18.dp))
            Text(
                text = when {
                    reportMode -> "Return clean. Log what you actually did."
                    started -> "Keep moving until the test window closes."
                    else -> "Build forward momentum before the day gets noisy."
                },
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val y = size.height / 2f
                    repeat(18) { index ->
                        val startX = size.width * (index / 18f)
                        val active = index / 18f <= progress
                        drawLine(
                            color = if (active) WarningAmber else Color.White.copy(alpha = 0.06f),
                            start = Offset(startX, y),
                            end = Offset((startX + size.width / 22f).coerceAtMost(size.width), y),
                            strokeWidth = 9.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 26.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = reportMode,
                            transitionSpec = {
                                (fadeIn(tween(220)) + scaleIn(initialScale = 0.96f)) togetherWith
                                    (fadeOut(tween(180)) + scaleOut(targetScale = 1.02f))
                            },
                            label = "run_hero_state"
                        ) { inReport ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when {
                                        inReport -> "LOG RUN"
                                        started -> formatLaunchTime(heroRunValue)
                                        else -> "$heroRunValue SEC"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 48.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = when {
                                        inReport -> "REPORT ACTUAL TIME"
                                        started -> "WINDOW LIVE"
                                        else -> "TEST TARGET"
                                    },
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.1.sp
                                )
                                Spacer(Modifier.height(14.dp))
                                LottieAnimation(
                                    composition = composition,
                                    progress = { animationProgress },
                                    modifier = Modifier.size(210.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AnimatedContent(
                        targetState = when {
                            reportMode -> "report"
                            started -> "live"
                            else -> "brief"
                        },
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(initialScale = 0.98f)) togetherWith
                                (fadeOut(tween(180)) + scaleOut(targetScale = 1.02f))
                        },
                        label = "run_panel_state"
                    ) { state ->
                        Column {
                            when (state) {
                                "brief" -> {
                                    WarriorPrimaryButton(
                                        text = "START RUN",
                                        accent = WarningAmber,
                                        onClick = {
                                            playWarriorCue(context, WarriorCueProfile.PhaseStart)
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            started = true
                                        }
                                    )
                                }
                                "report" -> {
                                    WarriorCaptureCard(
                                        accent = WarningAmber,
                                        headline = "REPORT YOUR RUN",
                                        value = actualDuration,
                                        valueLabel = "seconds completed",
                                        step = 5,
                                        range = 0..300,
                                        onValueChange = { actualDuration = it }
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        WarriorPrimaryButton(
                                            text = "LOCK RUN",
                                            accent = WarningAmber,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                playWarriorCue(context, WarriorCueProfile.PhaseLock)
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onDone(actualDuration, true)
                                            }
                                        )
                                        WarriorGhostButton(
                                            text = "SKIP",
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                playWarriorCue(context, WarriorCueProfile.Skip)
                                                onDone(0, false)
                                            }
                                        )
                                    }
                                }
                                else -> {
                                    WarriorStatPill("WINDOW", "LIVE", Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    EmergencyExitButton(
                        remaining = emergencyRemaining,
                        onEmergencyExit = onEmergencyExit
                    )
                }
            }
        }
    }
}

@Composable
private fun WarriorColdShowerScreen(
    step: MorningLaunchStep,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: (completed: Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val streakMotion = rememberInfiniteTransition(label = "cold_streaks")
    val shift by streakMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cold_shift"
    )
    var confirmMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playWarriorCue(context, WarriorCueProfile.ColdEnter)
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceDark, DeepBackground)
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(540f, 180f),
                        radius = 760f
                    )
                )
        )
        Column(modifier = Modifier.fillMaxSize()) {
            WarriorTopBar(phase = "PHASE 3", headline = "COLD SHOWER", activeIndex = 2)
            Spacer(Modifier.height(18.dp))
            Text(
                text = if (confirmMode) "Call it clean only if you actually took the cold shower." else "Finish the launch with controlled discomfort. No soft exit.",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val lineCount = 22
                    repeat(lineCount) { index ->
                        val x = size.width * (index / lineCount.toFloat())
                        val offsetFactor = ((index * 0.07f) + shift) % 1f
                        drawLine(
                            color = TextPrimary.copy(alpha = 0.18f + (index % 3) * 0.08f),
                            start = Offset(x, -size.height * 0.12f + offsetFactor * 24f),
                            end = Offset(x + 10.dp.toPx(), size.height * 0.78f + offsetFactor * 36f),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (confirmMode) "SURVIVE THE SHOCK" else "HOLD THE LINE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (confirmMode) "Choose the truth and lock the final phase." else "No comfort deal. Step in, finish it, then confirm.",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!confirmMode) {
                        WarriorPrimaryButton(
                                text = "ENTER THE COLD",
                                accent = NeonCyan,
                                onClick = {
                                    playWarriorCue(context, WarriorCueProfile.PhaseStart)
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    confirmMode = true
                                }
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            WarriorPrimaryButton(
                                text = "SURVIVED",
                                accent = NeonCyan,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    playWarriorCue(context, WarriorCueProfile.PhaseLock)
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDone(true)
                                }
                            )
                            WarriorGhostButton(
                                text = "SKIP PHASE",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    playWarriorCue(context, WarriorCueProfile.Skip)
                                    onDone(false)
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    EmergencyExitButton(
                        remaining = emergencyRemaining,
                        onEmergencyExit = onEmergencyExit
                    )
                }
            }
        }
    }
}

@Composable
private fun MorningDisciplinePrimeScreen(
    step: MorningLaunchStep,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: () -> Unit
) {
    val totalSeconds = remember(step.durationSeconds) {
        if (step.durationSeconds > 0) step.durationSeconds else 120
    }
    var secondsLeft by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    val lines = remember {
        listOf(
            "Move before mood.",
            "Do not negotiate with the first hour.",
            "Momentum is earned in silence."
        )
    }

    LaunchedEffect(totalSeconds) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onDone()
    }

    MorningStepShell(
        accent = WarningAmber,
        label = "WARRIOR PRIME",
        title = step.title,
        subtitle = "Sharpen the line between waking up and drifting.",
        timerLabel = formatLaunchTime(secondsLeft),
        footer = "Stand still. Read it. Mean it.",
        emergencyRemaining = emergencyRemaining,
        onEmergencyExit = onEmergencyExit,
        progress = ((totalSeconds - secondsLeft).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f),
        actionLabel = null,
        onAction = null
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                lines.forEach { line ->
                    Text(
                        text = line,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MorningMissionLockScreen(
    step: MorningLaunchStep,
    prompt: String,
    initialText: String,
    onTextChanged: (String) -> Unit,
    actionLabel: String = "Seal the mission",
    placeholder: String = "Write the first non-negotiable mission for today...",
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: () -> Unit
) {
    var localText by remember(initialText) { mutableStateOf(initialText) }

    MorningStepShell(
        accent = WarningAmber,
        label = "MISSION LOCK",
        title = step.title,
        subtitle = "Choose what the day serves before noise chooses for you.",
        timerLabel = "Decision step",
        footer = "Clarity first. Drift later if you let it.",
        emergencyRemaining = emergencyRemaining,
        onEmergencyExit = onEmergencyExit,
        actionLabel = actionLabel,
        onAction = {
            if (localText.isNotBlank()) {
                onTextChanged(localText)
                onDone()
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PROMPT",
                        color = WarningAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = prompt,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    )
                }
            }

            OutlinedTextField(
                value = localText,
                onValueChange = {
                    localText = it
                    onTextChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = {
                    Text(placeholder, color = Color.White.copy(alpha = 0.35f))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarningAmber,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun WarriorStepShell(
    phase: String,
    title: String,
    subtitle: String,
    accent: Color,
    timerLabel: String,
    progress: Float,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    bottomHint: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(540f, 120f),
                        radius = 720f
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
                    ) {
                        Text(
                            text = phase,
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.3.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = timerLabel,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(10.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accent.copy(alpha = 0.72f), accent)
                                ),
                                RoundedCornerShape(999.dp)
                            )
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 34.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.66f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }

            content()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = bottomHint,
                    color = Color.White.copy(alpha = 0.56f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                EmergencyExitButton(
                    remaining = emergencyRemaining,
                    onEmergencyExit = onEmergencyExit
                )
            }
        }
    }
}

@Composable
private fun WarriorMissionRail(
    activeIndex: Int,
    labels: List<String>
) {
    val glowTransition = rememberInfiniteTransition(label = "warrior_rail_glow")
    val pulse by glowTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warrior_rail_pulse"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val active = index == activeIndex
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (active) (20.dp * pulse) else 14.dp)
                        .background(
                            if (active) WarningAmber else Color.White.copy(alpha = 0.14f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (active) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Black.copy(alpha = 0.84f), CircleShape)
                        )
                    }
                }
                Text(
                    text = label,
                    color = Color.White.copy(alpha = if (active) 0.94f else 0.46f),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun WarriorTopBar(
    phase: String,
    headline: String,
    activeIndex: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text(
                    text = phase,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "WARRIOR PROTOCOL",
                    color = Color.White.copy(alpha = 0.46f),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = headline,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        WarriorMissionRail(
            activeIndex = activeIndex,
            labels = listOf("PUSH", "RUN", "COLD")
        )
    }
}

@Composable
private fun WarriorPrimaryButton(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "warrior_primary_scale"
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun WarriorGhostButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 520f),
        label = "warrior_ghost_scale"
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.06f),
            contentColor = Color.White.copy(alpha = 0.76f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun WarriorStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MorningMeditationScreen(
    step: MorningLaunchStep,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PrefManager.getInstance(context) }
    val soundController = remember { SleepLockSoundController(context) }
    val meditationOptions = remember {
        SleepLockSoundCatalog.sounds.filter { it.id in setOf("night_air", "rain", "brown_noise") }
    }
    val totalSeconds = remember(step.durationSeconds) {
        step.durationSeconds
    }
    var secondsLeft by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    var selectedSoundId by remember {
        mutableStateOf(
            meditationOptions.firstOrNull { soundController.isDownloaded(it.id) }?.id ?: "night_air"
        )
    }
    var isMuted by remember { mutableStateOf(true) }
    val selectedSound = remember(selectedSoundId) { SleepLockSoundCatalog.byId(selectedSoundId) }
    val ambientFile = remember(selectedSoundId) { soundController.getLocalFile(selectedSoundId) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.meditation_monk))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(totalSeconds) {
        secondsLeft = totalSeconds
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onDone()
    }

    LaunchedEffect(selectedSoundId, isMuted, ambientFile?.absolutePath) {
        prefs.sleepLockSelectedSound = selectedSoundId
        prefs.sleepLockSoundEnabled = true
        if (!isMuted && ambientFile?.exists() == true) {
            soundController.play()
        } else {
            soundController.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundController.stop()
        }
    }

    MorningStepShell(
        accent = TextPrimary,
        label = "MONK MEDITATION",
        title = step.title,
        subtitle = "Quiet the first noise before it becomes the whole day.",
        timerLabel = formatLaunchTime(secondsLeft),
        progress = ((totalSeconds - secondsLeft).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f),
        emergencyRemaining = emergencyRemaining,
        onEmergencyExit = onEmergencyExit,
        footer = if (ambientFile?.exists() == true) {
            if (isMuted) "${selectedSound.title} ready • muted" else "${selectedSound.title} flowing"
        } else {
            "${selectedSound.title} not downloaded yet. Download it from Sleep Lock first."
        },
        actionLabel = if (ambientFile?.exists() == true) {
            if (isMuted) "Start ambient"
            else "Mute ambient"
        } else {
            null
        },
        onAction = if (ambientFile?.exists() == true) ({ isMuted = !isMuted }) else null
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(188.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(176.dp)
                                .background(TextPrimary.copy(alpha = 0.08f), CircleShape)
                        )
                        Canvas(modifier = Modifier.size(162.dp)) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = TextPrimary.copy(alpha = 0.18f),
                                radius = size.minDimension * 0.42f
                            )
                        }
                        LottieAnimation(
                            composition = composition,
                            progress = { animationProgress },
                            modifier = Modifier.size(146.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Settle the jaw. Drop the shoulders. Keep the spine tall.",
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        meditationOptions.forEach { option ->
                            val isSelected = option.id == selectedSoundId
                            val isDownloaded = soundController.isDownloaded(option.id)
                            Surface(
                                color = if (isSelected) TextPrimary.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) TextPrimary.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier.gamifiedClick {
                                    selectedSoundId = option.id
                                    isMuted = true
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = option.title,
                                        color = if (isSelected) TextPrimary else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (isDownloaded) "Ready" else "Locked",
                                        color = Color.White.copy(alpha = 0.56f),
                                        fontSize = 10.sp
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
private fun MorningJournalScreen(
    step: MorningLaunchStep,
    initialText: String,
    prompt: String,
    onTextChanged: (String) -> Unit,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onDone: () -> Unit
) {
    var localText by remember(initialText) { mutableStateOf(initialText) }
    val wordCount = remember(localText) {
        localText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val journalState = when {
        wordCount >= 35 -> "DEEP ENTRY"
        wordCount >= 12 -> "CLEAR ENTRY"
        else -> "START WRITING"
    }

    MorningStepShell(
        accent = WarningAmber,
        label = "MONK JOURNAL",
        title = step.title,
        subtitle = "Write before distraction writes the day for you.",
        timerLabel = "Final step",
        footer = "One honest paragraph is enough.",
        emergencyRemaining = emergencyRemaining,
        onEmergencyExit = onEmergencyExit,
        actionLabel = "Seal this entry",
        onAction = {
            if (localText.isNotBlank()) {
                onTextChanged(localText)
                onDone()
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S LINE",
                            color = WarningAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        )
                        Surface(
                            color = WarningAmber.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.20f))
                        ) {
                            Text(
                                text = journalState,
                                color = WarningAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = prompt,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Write it cleanly or speak it aloud, then commit it here.",
                        color = Color.White.copy(alpha = 0.60f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MORNING ENTRY",
                            color = Color.White.copy(alpha = 0.70f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = "$wordCount words",
                            color = WarningAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = localText,
                        onValueChange = {
                            localText = it
                            onTextChanged(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        placeholder = {
                            Text(
                                "Write your first clear thought of the day...",
                                color = Color.White.copy(alpha = 0.35f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarningAmber,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedContainerColor = Color.White.copy(alpha = 0.03f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MorningMonkCompleteScreen(
    reinforcement: String,
    onComplete: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.task_done))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceDark, DeepBackground)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LottieAnimation(
                    composition = composition,
                    progress = { animationProgress },
                    modifier = Modifier.size(160.dp)
                )
                Text(
                    text = "MORNING SEALED",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 1.4.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = reinforcement,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "BEGIN THE DAY",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun MorningStepTransitionScreen(
    accent: Color,
    xpGain: Int,
    completedTitle: String,
    nextTitle: String,
    nextBenefit: String,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onFinished: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebration))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    var secondsLeft by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceDark, DeepBackground)
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(560f, 220f),
                        radius = 720f
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = "NEXT IN ${secondsLeft}s",
                        color = Color.White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(188.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(accent.copy(alpha = 0.10f), CircleShape)
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { animationProgress },
                        modifier = Modifier.size(150.dp)
                    )
                    Surface(
                        color = accent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Text(
                            text = "+$xpGain XP",
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = completedTitle.uppercase(),
                    color = accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.4.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Next: $nextTitle",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = nextBenefit,
                    color = Color.White.copy(alpha = 0.66f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(((3 - secondsLeft) / 3f).coerceIn(0f, 1f))
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accent.copy(alpha = 0.75f), accent)
                                ),
                                RoundedCornerShape(999.dp)
                            )
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Auto-preparing the next step.",
                    color = Color.White.copy(alpha = 0.52f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(14.dp))
                EmergencyExitButton(
                    remaining = emergencyRemaining,
                    onEmergencyExit = onEmergencyExit
                )
            }
        }
    }
}

@Composable
private fun MorningVariantCompleteScreen(
    accent: Color,
    title: String,
    reinforcement: String,
    onComplete: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.task_done))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceDark, DeepBackground)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LottieAnimation(
                    composition = composition,
                    progress = { animationProgress },
                    modifier = Modifier.size(160.dp)
                )
                Text(
                    text = title,
                    color = accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = reinforcement,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "BEGIN THE DAY",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun WarriorMissionCompleteScreen(
    summaries: List<WarriorStepSummary>,
    reinforcement: String,
    onComplete: () -> Unit
) {
    val totalXp = summaries.sumOf { it.xpGain }
    val completedCount = summaries.count { it.xpGain > 0 }
    val animatedTotalXp by animateIntAsState(
        targetValue = totalXp,
        animationSpec = tween(900),
        label = "warrior_total_xp"
    )
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebration))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
        .padding(22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WarningAmber.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(520f, 180f),
                        radius = 860f
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WarriorTopBar(phase = "CLEAR", headline = "MISSION COMPLETE", activeIndex = 2)
                Spacer(Modifier.height(18.dp))
                LottieAnimation(
                    composition = composition,
                    progress = { animationProgress },
                    modifier = Modifier.size(148.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WarriorStatPill(
                        label = "PHASES",
                        value = "$completedCount / 3",
                        modifier = Modifier.weight(1f)
                    )
                    WarriorStatPill(
                        label = "TOTAL XP",
                        value = "+$animatedTotalXp",
                        modifier = Modifier.weight(1f)
                    )
                    WarriorStatPill(
                        label = "STATUS",
                        value = if (completedCount == 3) "SEALED" else "PARTIAL",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        summaries.forEach { summary ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = summary.title.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = summary.actualLabel,
                                        color = Color.White.copy(alpha = 0.62f),
                                        fontSize = 12.sp
                                    )
                                }
                                Surface(
                                    color = if (summary.xpGain > 0) WarningAmber.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.06f),
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (summary.xpGain > 0) WarningAmber.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Text(
                                        text = if (summary.xpGain > 0) "+${summary.xpGain} XP" else "SKIPPED",
                                        color = if (summary.xpGain > 0) WarningAmber else Color.White.copy(alpha = 0.52f),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
                ) {
                    Text(
                        text = reinforcement,
                        color = Color.White.copy(alpha = 0.86f),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                    )
                }
            }

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningAmber,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "ENTER THE DAY",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun WarriorPhaseTransitionScreen(
    accent: Color,
    xpGain: Int,
    completedTitle: String,
    nextTitle: String,
    nextBenefit: String,
    detailLabel: String?,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    onFinished: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(2) }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val animatedXp by animateIntAsState(
        targetValue = xpGain,
        animationSpec = tween(700),
        label = "warrior_transition_xp"
    )
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebration))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(Unit) {
        playWarriorCue(context, WarriorCueProfile.Reward)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
            .padding(22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(520f, 200f),
                        radius = 780f
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                WarriorTopBar(phase = "PHASE CLEAR", headline = nextTitle.uppercase(), activeIndex = -1)
                Spacer(Modifier.height(26.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { animationProgress },
                        modifier = Modifier.size(92.dp)
                    )
                    Column {
                        Text(
                            text = completedTitle.uppercase(),
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (xpGain > 0) "+$animatedXp XP" else "0 XP",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp
                        )
                    }
                }
                if (!detailLabel.isNullOrBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = detailLabel.uppercase(),
                            color = Color.White.copy(alpha = 0.86f),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "NEXT",
                    color = Color.White.copy(alpha = 0.48f),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = nextTitle.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = nextBenefit,
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(((2 - secondsLeft) / 2f).coerceIn(0f, 1f))
                            .height(10.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accent.copy(alpha = 0.8f), accent)
                                ),
                                RoundedCornerShape(999.dp)
                            )
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Auto-deploying next phase.",
                    color = Color.White.copy(alpha = 0.56f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(14.dp))
                EmergencyExitButton(
                    remaining = emergencyRemaining,
                    onEmergencyExit = onEmergencyExit
                )
            }
        }
    }
}

@Composable
private fun WarriorCaptureCard(
    accent: Color,
    headline: String,
    value: Int,
    valueLabel: String,
    step: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    val animatedValue by animateIntAsState(
        targetValue = value.coerceIn(range.first, range.last),
        animationSpec = tween(220),
        label = "warrior_capture_value"
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = headline,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WarriorAdjustButton(
                    label = "-$step",
                    accent = accent,
                    onClick = { onValueChange((value - step).coerceAtLeast(range.first)) }
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedValue",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp
                    )
                    Text(
                        text = valueLabel.uppercase(),
                        color = Color.White.copy(alpha = 0.48f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
                WarriorAdjustButton(
                    label = "+$step",
                    accent = accent,
                    onClick = { onValueChange((value + step).coerceAtMost(range.last)) }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            ((animatedValue - range.first).toFloat() / (range.last - range.first).coerceAtLeast(1)).coerceIn(0f, 1f)
                        )
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(listOf(accent.copy(alpha = 0.68f), accent)),
                            RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun WarriorAdjustButton(
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 540f),
        label = "warrior_adjust_scale"
    )
    Surface(
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.26f))
    ) {
        Text(
            text = label,
            color = accent,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

private enum class WarriorCueProfile {
    PushupsEnter, RunEnter, ColdEnter, PhaseStart, ReportReady, PhaseLock, Skip, Reward
}

private fun playWarriorCue(context: android.content.Context, profile: WarriorCueProfile) {
    val soundManager = com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context)
    when (profile) {
        WarriorCueProfile.PushupsEnter,
        WarriorCueProfile.RunEnter,
        WarriorCueProfile.ColdEnter,
        WarriorCueProfile.PhaseStart -> soundManager.playTimerTick()
        WarriorCueProfile.ReportReady -> soundManager.playTaskComplete()
        WarriorCueProfile.PhaseLock,
        WarriorCueProfile.Skip -> soundManager.playTemptationBlocked()
        WarriorCueProfile.Reward -> soundManager.playStreakMilestone()
    }
}

@Composable
private fun MorningStepShell(
    accent: Color,
    label: String,
    title: String,
    subtitle: String,
    timerLabel: String,
    footer: String,
    emergencyRemaining: Int,
    onEmergencyExit: () -> Unit,
    actionLabel: String?,
        onAction: (() -> Unit)?,
    progress: Float = 0f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceDark, DeepBackground)
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(540f, 180f),
                        radius = 620f
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
                    ) {
                        Text(
                            text = label,
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    if (progress > 0f) {
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Text(
                                text = "${(progress * 100).roundToInt()}%",
                                color = Color.White.copy(alpha = 0.80f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(8.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accent.copy(alpha = 0.75f), accent)
                                    ),
                                    RoundedCornerShape(999.dp)
                                )
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = timerLabel,
                    color = accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                content()
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = footer,
                    color = Color.White.copy(alpha = 0.52f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                EmergencyExitButton(
                    remaining = emergencyRemaining,
                    onEmergencyExit = onEmergencyExit
                )
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = actionLabel,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyExitButton(
    remaining: Int,
    onEmergencyExit: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Use Emergency Exit?",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "This ends Morning Launch immediately and records today as skipped. Remaining this month: $remaining of 5."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onEmergencyExit()
                }) {
                    Text("Use exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Stay here")
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.74f)
        )
    }

    Button(
        onClick = { if (remaining > 0) showDialog = true },
        enabled = remaining > 0,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            contentColor = Color.White.copy(alpha = 0.72f),
            disabledContainerColor = Color.White.copy(alpha = 0.03f),
            disabledContentColor = Color.White.copy(alpha = 0.24f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Text(
            text = if (remaining > 0) "EMERGENCY EXIT • $remaining LEFT" else "EMERGENCY EXIT UNAVAILABLE",
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BreathInstructionBadge(
    accent: Color,
    phase: BreathPhase
) {
    Surface(
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent.copy(alpha = 0.90f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = phase.label,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun BreathPhaseTrack(
    accent: Color,
    activePhase: BreathPhase
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            BreathPhase.INHALE,
            BreathPhase.HOLD_FULL,
            BreathPhase.EXHALE,
            BreathPhase.HOLD_EMPTY
        ).forEach { phase ->
            val isActive = phase == activePhase
            Surface(
                color = if (isActive) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(
                    1.dp,
                    if (isActive) accent.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.08f)
                )
            ) {
                Text(
                    text = phase.shortLabel,
                    color = if (isActive) accent else Color.White.copy(alpha = 0.56f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

private enum class BreathPhase(
    val label: String,
    val shortLabel: String,
    val actionLine: String,
    val coachLine: String,
    val targetScale: Float,
    val glowScale: Float
) {
    INHALE(
        label = "BREATHE IN",
        shortLabel = "IN",
        actionLine = "Take a full, deep breath.",
        coachLine = "Fill the belly first. Expand the ribs and let the inhale travel upward.",
        targetScale = 1.24f,
        glowScale = 1.34f
    ),
    HOLD_FULL(
        label = "HOLD",
        shortLabel = "HOLD",
        actionLine = "Hold the breath at the top.",
        coachLine = "Stay tall and quiet. Keep the chest open without forcing the throat.",
        targetScale = 1.26f,
        glowScale = 1.36f
    ),
    EXHALE(
        label = "RELEASE",
        shortLabel = "OUT",
        actionLine = "Release the breath slowly.",
        coachLine = "Press the air out softly. Let the orb tighten as the body empties.",
        targetScale = 0.70f,
        glowScale = 0.86f
    ),
    HOLD_EMPTY(
        label = "HOLD EMPTY",
        shortLabel = "EMPTY",
        actionLine = "Stay empty for one beat.",
        coachLine = "Rest in the pause. Let the next inhale arrive on its own.",
        targetScale = 0.66f,
        glowScale = 0.82f
    );

    companion object {
        fun fromSecond(elapsedSeconds: Int): BreathPhase {
            val cycleSecond = elapsedSeconds % 15
            return when {
                cycleSecond < 5 -> INHALE
                cycleSecond < 9 -> HOLD_FULL
                cycleSecond < 14 -> EXHALE
                else -> HOLD_EMPTY
            }
        }
    }
}

private object MorningPromptRepository {
    private val monkPrompts = listOf(
        "What kind of man or woman do you become when you protect the first hour instead of spending it?",
        "What matters enough today that it deserves your clean mind before the world touches it?",
        "What are you refusing to let distraction steal from this morning?",
        "What is one disciplined act today that your future self will quietly thank you for?",
        "What truth do you need to face early so the rest of the day can become simpler?"
    )

    fun promptForToday(mode: MorningLaunchMode): String {
        val dateSeed = LocalDate.now().dayOfYear
        return when (mode) {
            MorningLaunchMode.MONK -> monkPrompts[dateSeed % monkPrompts.size]
            MorningLaunchMode.WARRIOR -> warriorPrompts[dateSeed % warriorPrompts.size]
            MorningLaunchMode.BALANCED -> balancedPrompts[dateSeed % balancedPrompts.size]
        }
    }

    fun reinforcementForMonk(prefs: PrefManager): String {
        return when {
            prefs.challengeDaysCompleted >= 21 ->
                "This is no longer a rare morning for you. You built proof, and proof changes what the future is allowed to expect from you."
            prefs.challengeDaysCompleted >= 7 ->
                "A week ago this would have required force. Now it is becoming identity. Keep pushing the standard forward."
            prefs.streakCount >= 10 ->
                "You did not wait to feel ready. You moved first. That is the version of you that can build a serious future."
            else ->
                "You started the day before drift began. Protect enough mornings like this and your life stops looking accidental."
        }
    }

    fun reinforcementForWarrior(prefs: PrefManager): String {
        return when {
            prefs.challengeDaysCompleted >= 21 ->
                "You are not borrowing intensity anymore. You are building a default standard, and the day can feel that before you speak."
            prefs.challengeDaysCompleted >= 7 ->
                "This is what earned momentum looks like. The morning is starting to obey the identity you keep repeating."
            else ->
                "You moved before excuses could form. Keep stacking mornings like that and hesitation stops leading your life."
        }
    }

    fun reinforcementForBalanced(prefs: PrefManager): String {
        return when {
            prefs.challengeDaysCompleted >= 21 ->
                "You are learning how to begin cleanly, not chaotically. That kind of steadiness compounds harder than intensity ever looks like it will."
            prefs.challengeDaysCompleted >= 7 ->
                "The day did not begin scattered today. That is not luck. That is structure starting to hold."
            else ->
                "You gave this day shape before randomness reached it. That is how ordinary mornings become useful."
        }
    }

    private val warriorPrompts = listOf(
        "What is the first action today that proves you are serious instead of merely motivated?",
        "Where does the day need force from you before it starts asking for compromise?",
        "What mission deserves your sharpest hour before the world gets noisy?",
        "What would a disciplined version of you attack first today without hesitation?",
        "What gets stronger today if you stop negotiating with comfort right now?"
    )

    private val balancedPrompts = listOf(
        "What three things would make today feel clean, useful, and complete?",
        "What matters enough today that it deserves calm attention instead of rushed attention?",
        "Where do you need steadiness more than speed today?",
        "What can you decide now so the rest of the day gets lighter?",
        "What would a grounded version of you protect first today?"
    )
}

private fun formatLaunchTime(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val minutes = safe / 60
    val remaining = safe % 60
    return String.format("%02d:%02d", minutes, remaining)
}

private const val WARRIOR_PUSHUP_TARGET = 25
private const val WARRIOR_RUN_TEST_SECONDS = 15

private fun warriorTestDuration(durationSeconds: Int): Int {
    return when {
        durationSeconds <= 0 -> WARRIOR_RUN_TEST_SECONDS
        durationSeconds > WARRIOR_RUN_TEST_SECONDS -> WARRIOR_RUN_TEST_SECONDS
        else -> durationSeconds
    }
}
