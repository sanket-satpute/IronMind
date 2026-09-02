package com.sanket_satpute_20.ironmind.focus

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.EmergencyValveEvent
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

enum class ValveState { EMPTY, TRIGGER, BREATHING, CONTRACT, SUCCESS }
enum class BreathPhase { IN, HOLD, OUT }

private data class EmergencyAppChoice(
    val packageName: String,
    val label: String
)

private data class EmergencyTriggerOption(
    val key: String,
    val title: String,
    val description: String
)

private data class EmergencyInterventionProfile(
    val triggerKey: String,
    val breathingIntro: String,
    val breathingOutro: String,
    val contractHeading: String,
    val contractFallback: String,
    val completionLabel: String,
    val successTitle: String,
    val successMessage: String
)

private val emergencyTriggerOptions = listOf(
    EmergencyTriggerOption("URGE", "Urge Spike", "You feel pulled toward a bad decision right now."),
    EmergencyTriggerOption("PANIC", "Panic", "Your body feels flooded and you need to steady yourself."),
    EmergencyTriggerOption("EXHAUSTION", "Exhaustion", "You feel depleted and close to shutting down."),
    EmergencyTriggerOption("DISTRACTION_SPIRAL", "Distraction Spiral", "Your focus is slipping and you are getting dragged sideways."),
    EmergencyTriggerOption("WANT_TO_QUIT", "Want To Quit", "You want to abandon the mission entirely.")
)

private fun interventionProfileFor(triggerKey: String): EmergencyInterventionProfile {
    return when (triggerKey) {
        "URGE" -> EmergencyInterventionProfile(
            triggerKey = triggerKey,
            breathingIntro = "URGE DETECTED",
            breathingOutro = "Delay the impulse until it loses authority.",
            contractHeading = "OUTLAST THE PULL",
            contractFallback = "An urge is not an order. Buy time until your system calms down.",
            completionLabel = "THE URGE PASSES",
            successTitle = "URGE BROKEN",
            successMessage = "You created distance between feeling and action. Choose the next move before the impulse regroups."
        )
        "PANIC" -> EmergencyInterventionProfile(
            triggerKey = triggerKey,
            breathingIntro = "NERVOUS SYSTEM FIRST",
            breathingOutro = "Stabilize the body before you negotiate with the mind.",
            contractHeading = "REGAIN CONTROL",
            contractFallback = "You do not need a perfect plan right now. You only need one steady breath at a time.",
            completionLabel = "I AM STEADYING",
            successTitle = "SYSTEM STABILIZED",
            successMessage = "Your body has started to come back online. Choose a steady next step instead of reacting blindly."
        )
        "EXHAUSTION" -> EmergencyInterventionProfile(
            triggerKey = triggerKey,
            breathingIntro = "LOW POWER MODE",
            breathingOutro = "We are not chasing intensity. We are restoring enough control to act wisely.",
            contractHeading = "PROTECT THE CORE",
            contractFallback = "Exhaustion makes every exit look reasonable. Hold the line until clarity returns.",
            completionLabel = "I WILL HOLD",
            successTitle = "ENERGY REGAINED",
            successMessage = "Not fully restored, but steady enough to choose your next move instead of collapsing into the wrong one."
        )
        "DISTRACTION_SPIRAL" -> EmergencyInterventionProfile(
            triggerKey = triggerKey,
            breathingIntro = "FOCUS COLLAPSE DETECTED",
            breathingOutro = "Interrupt the drift before it becomes a full retreat.",
            contractHeading = "LOCK BACK IN",
            contractFallback = "Your attention is scattering. Narrow the next step until it becomes easy to re-enter.",
            completionLabel = "BACK ON TARGET",
            successTitle = "COURSE CORRECTED",
            successMessage = "The spiral has been interrupted. Pick a deliberate next move before momentum leaks out again."
        )
        "WANT_TO_QUIT" -> EmergencyInterventionProfile(
            triggerKey = triggerKey,
            breathingIntro = "DO NOT DECIDE WHILE WEAK",
            breathingOutro = "Quitting feels clean in the crash. Breathe until the decision stops lying to you.",
            contractHeading = "REMEMBER WHO YOU SAID YOU'D BE",
            contractFallback = "The urge to quit is usually a request for relief, not proof that the mission is wrong.",
            completionLabel = "I STAY IN IT",
            successTitle = "QUIT LOOP BROKEN",
            successMessage = "You refused to make a permanent decision inside a temporary collapse. Now take the next honest step."
        )
        else -> EmergencyInterventionProfile(
            triggerKey = triggerKey,
            breathingIntro = "SYSTEM RESET",
            breathingOutro = "Steady the system before the next decision.",
            contractHeading = "HOLD THE LINE",
            contractFallback = "Delay the bad decision long enough to regain control.",
            completionLabel = "I WILL NOT BREAK",
            successTitle = "SYSTEM RESET",
            successMessage = "Impulse neutralized. Choose the next deliberate action."
        )
    }
}

@Composable
fun EmergencyValveScreen(
    onDismiss: () -> Unit,
    onFiveMinuteReset: () -> Unit = {},
    onDecisionDelay: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { PrefManager.getInstance(context) }
    val db = remember { IronMindDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val hasContract = prefs.contractSigned &&
        prefs.contractGoal.isNotBlank() &&
        prefs.contractReason.isNotBlank()
    val today = remember { LocalDate.now().toString() }
    val focusSessionActive = prefs.activeTaskDate == today && prefs.activeTaskName.isNotBlank()
    var currentState by remember {
        mutableStateOf(if (prefs.emergencyValveTokens > 0) ValveState.TRIGGER else ValveState.EMPTY)
    }
    var showExitConfirm by remember { mutableStateOf(false) }
    var showEmergencyAppPicker by remember { mutableStateOf(false) }
    var selectedTrigger by remember { mutableStateOf("UNSPECIFIED") }
    val interventionProfile = remember(selectedTrigger) { interventionProfileFor(selectedTrigger) }
    val emergencyAppChoices = remember(prefs.emergencyApps) {
        prefs.emergencyApps
            .mapNotNull { packageName ->
                runCatching {
                    val label = context.packageManager.getApplicationLabel(
                        context.packageManager.getApplicationInfo(packageName, 0)
                    ).toString()
                    EmergencyAppChoice(packageName = packageName, label = label)
                }.getOrNull()
            }
            .sortedBy { it.label }
    }

    BackHandler(enabled = currentState != ValveState.SUCCESS && currentState != ValveState.EMPTY) {
        showExitConfirm = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepBackground, Color.Black, DeepBackground)
                )
            )
    ) {
        if (currentState != ValveState.SUCCESS) {
            IconButton(
                onClick = {
                    if (currentState == ValveState.EMPTY) onDismiss() else showExitConfirm = true
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Abort", tint = Color.Gray)
            }
        }

        if (currentState != ValveState.EMPTY) {
            ValveStatusHeader(
                tokens = prefs.emergencyValveTokens,
                stageLabel = when (currentState) {
                    ValveState.TRIGGER -> "SIGNAL"
                    ValveState.BREATHING -> "REGULATE"
                    ValveState.CONTRACT -> "RECALL"
                    ValveState.SUCCESS -> "RECOVER"
                    ValveState.EMPTY -> ""
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp)
            )
        }

        AnimatedContent(
            targetState = currentState,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) + slideInVertically { it / 12 } togetherWith
                    fadeOut(animationSpec = tween(180)) + slideOutVertically { -it / 18 }
            },
            label = "valve_state"
        ) { state ->
            when (state) {
                ValveState.EMPTY -> NoTokensPhaseUI(onDismiss = onDismiss)
                ValveState.TRIGGER -> TriggerSelectionPhaseUI(
                    activeTaskName = if (focusSessionActive) prefs.activeTaskName else "",
                    onTriggerSelected = { trigger ->
                        selectedTrigger = trigger
                        currentState = ValveState.BREATHING
                    }
                )
                ValveState.BREATHING -> BreathingPhaseUI(
                    profile = interventionProfile,
                    onFinished = { currentState = ValveState.CONTRACT }
                )
                ValveState.CONTRACT -> ContractPhaseUI(
                    profile = interventionProfile,
                    goal = prefs.contractGoal,
                    reason = prefs.contractReason,
                    signature = prefs.contractSignature,
                    hasContract = hasContract,
                    onComplete = {
                        prefs.emergencyValveTokens = (prefs.emergencyValveTokens - 1).coerceAtLeast(0)
                        currentState = ValveState.SUCCESS
                    }
                )
                ValveState.SUCCESS -> SuccessPhaseUI(
                    profile = interventionProfile,
                    focusSessionActive = focusSessionActive,
                    activeTaskName = if (focusSessionActive) prefs.activeTaskName else "",
                    hasEmergencyApps = emergencyAppChoices.isNotEmpty(),
                    onActionSelected = { action ->
                        applyEmergencyCooldown(context, prefs, action)
                        cooldownConfirmationFor(action)?.let { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                        scope.launch {
                            db.emergencyValveEventDao().insert(
                                createEmergencyValveEvent(
                                    prefs = prefs,
                                    triggerReason = selectedTrigger,
                                    tokenConsumed = true,
                                    selectedRecoveryAction = action,
                                    completed = true
                                )
                            )
                        }
                    },
                    onOpenEmergencyApp = {
                        when {
                            emergencyAppChoices.isEmpty() -> Unit
                            emergencyAppChoices.size == 1 -> {
                                val selectedApp = emergencyAppChoices.first()
                                onEmergencyAppSelected(
                                    context = context,
                                    prefs = prefs,
                                    db = db,
                                    scope = scope,
                                    triggerReason = selectedTrigger,
                                    appChoice = selectedApp,
                                    onDismiss = onDismiss
                                )
                            }
                            else -> showEmergencyAppPicker = true
                        }
                    },
                    onFiveMinuteReset = onFiveMinuteReset,
                    onDecisionDelay = onDecisionDelay,
                    onDismiss = onDismiss
                )
            }
        }

        if (showEmergencyAppPicker) {
            AlertDialog(
                onDismissRequest = { showEmergencyAppPicker = false },
                containerColor = DeepBackground,
                title = {
                    Text(
                        "CHOOSE EMERGENCY APP",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Pick the app you actually need. The shield stays active while blocked apps remain sealed.",
                            color = Color.Gray
                        )
                        emergencyAppChoices.forEach { appChoice ->
                            Surface(
                                onClick = {
                                    showEmergencyAppPicker = false
                                    onEmergencyAppSelected(
                                        context = context,
                                        prefs = prefs,
                                        db = db,
                                        scope = scope,
                                        triggerReason = selectedTrigger,
                                        appChoice = appChoice,
                                        onDismiss = onDismiss
                                    )
                                },
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Text(
                                    text = appChoice.label,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showEmergencyAppPicker = false }) {
                        Text("CANCEL", color = Color.White)
                    }
                }
            )
        }

        if (showExitConfirm) {
            AlertDialog(
                onDismissRequest = { showExitConfirm = false },
                containerColor = DeepBackground,
                icon = {
                    Icon(
                        Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = WarningAmber
                    )
                },
                title = {
                    Text(
                        "ABANDON RESET?",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Text(
                        "Leaving now will end the rescue flow before it has a chance to steady you.",
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                db.emergencyValveEventDao().insert(
                                    createEmergencyValveEvent(
                                        prefs = prefs,
                                        triggerReason = selectedTrigger,
                                        tokenConsumed = false,
                                        selectedRecoveryAction = "ABANDONED_RESET",
                                        completed = false
                                    )
                                )
                            }
                            showExitConfirm = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("LEAVE ANYWAY", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirm = false }) {
                        Text("STAY IN RESET", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
private fun ValveStatusHeader(
    tokens: Int,
    stageLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "RESET PROTOCOL",
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                "Intercept. Stabilize. Choose cleanly.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stageLabel, color = WarningAmber, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("$tokens", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun NoTokensPhaseUI(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("0", color = ErrorRed, fontSize = 72.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "NO VALVE TOKENS LEFT",
            color = Color.White,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "You have used all 3 rescue tokens for this month. The valve will reset next month.\n\nYou can earn bonus tokens by maintaining 3+ consecutive days with 85%+ completion.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("BACK TO HOME", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TriggerSelectionPhaseUI(
    activeTaskName: String,
    onTriggerSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            "WHAT HIT YOU?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Name the pressure first. The right reset starts with an honest signal.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        if (activeTaskName.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = DeepBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "ACTIVE MISSION: $activeTaskName",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        emergencyTriggerOptions.forEach { option ->
            TriggerOptionCard(
                option = option,
                onClick = { onTriggerSelected(option.key) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TriggerOptionCard(
    option: EmergencyTriggerOption,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                option.title.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                option.description,
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun BreathingPhaseUI(
    profile: EmergencyInterventionProfile,
    onFinished: () -> Unit
) {
    var cycleCount by remember { mutableIntStateOf(0) }
    var currentPhase by remember { mutableStateOf(BreathPhase.IN) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (currentPhase == BreathPhase.IN) 0.6f else if (currentPhase == BreathPhase.OUT) 1.2f else 1.2f,
        targetValue = if (currentPhase == BreathPhase.IN) 1.2f else if (currentPhase == BreathPhase.OUT) 0.6f else 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        while (cycleCount < 3) {
            currentPhase = BreathPhase.IN; delay(4000)
            currentPhase = BreathPhase.HOLD; delay(4000)
            currentPhase = BreathPhase.OUT; delay(4000)
            cycleCount++
        }
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            profile.breathingIntro,
            color = WarningAmber,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = when(currentPhase) {
                BreathPhase.IN -> "BREATHE IN"
                BreathPhase.HOLD -> "HOLD"
                BreathPhase.OUT -> "BREATHE OUT"
            },
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 4.sp),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(64.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(240.dp)) {
                drawCircle(color = Color.White.copy(alpha = 0.1f), style = Stroke(2.dp.toPx()))
            }
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .background(
                        color = when(currentPhase) {
                            BreathPhase.IN -> NeonCyan
                            BreathPhase.HOLD -> SuccessGreen
                            BreathPhase.OUT -> ErrorRed
                        }.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
            Text(text = cycleCount.toString(), color = Color.White.copy(alpha = 0.5f), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        Text("Calibrating Human System... ${3 - cycleCount} cycles left", color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        Text(profile.breathingOutro, color = Color.DarkGray, textAlign = TextAlign.Center, lineHeight = 20.sp)
    }
}

@Composable
private fun ContractPhaseUI(
    profile: EmergencyInterventionProfile,
    goal: String,
    reason: String,
    signature: String,
    hasContract: Boolean,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            if (hasContract) profile.contractHeading else "HOLD THE LINE",
            color = WarningAmber,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (hasContract) {
            Text(
                text = "\"$goal\"",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = reason,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        } else {
            Text(
                text = "You have not signed your commitment contract yet. This reset will still help you slow down, but it will be stronger once your vow is in place.",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text(
                    text = profile.contractFallback,
                    modifier = Modifier.padding(20.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        if (hasContract && signature.isNotEmpty()) {
            Text("SIGNED BY", fontSize = 10.sp, color = Color.DarkGray)
            Text(signature.uppercase(), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(80.dp))
        } else {
            Text(
                "Complete your contract later to make this valve more personal and harder to ignore.",
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(80.dp))
        }

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(profile.completionLabel, color = Color.Black, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun SuccessPhaseUI(
    profile: EmergencyInterventionProfile,
    focusSessionActive: Boolean,
    activeTaskName: String,
    hasEmergencyApps: Boolean,
    onActionSelected: (String) -> Unit,
    onOpenEmergencyApp: () -> Unit,
    onFiveMinuteReset: () -> Unit,
    onDecisionDelay: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(profile.successTitle, color = SuccessGreen, fontWeight = FontWeight.Black, letterSpacing = 4.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(profile.successMessage, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 22.sp)
        if (focusSessionActive && activeTaskName.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = DeepBackground,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "MISSION WAITING: $activeTaskName",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = SuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "RECOVERY SHIELD OPTIONS",
                    color = WarningAmber,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Time-buying actions keep blocked apps sealed but soften punishment while you reset. Return to mission clears the shield immediately.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
        OutcomeActionButton(
            text = if (focusSessionActive) "RETURN TO MISSION" else "BACK TO HOME",
            containerColor = SuccessGreen,
            contentColor = Color.Black,
            onClick = {
                onActionSelected(if (focusSessionActive) "RETURN_TO_MISSION" else "BACK_TO_HOME")
                onDismiss()
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutcomeActionButton(
            text = "TAKE 5-MIN RESET",
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = Color.White,
            onClick = {
                onActionSelected("FIVE_MIN_RESET")
                onFiveMinuteReset()
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutcomeActionButton(
            text = "DELAY DECISION 10 MIN",
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = Color.White,
            onClick = {
                onActionSelected("DELAY_DECISION_10_MIN")
                onDecisionDelay()
            }
        )
        if (hasEmergencyApps) {
            Spacer(modifier = Modifier.height(12.dp))
            OutcomeActionButton(
                text = "OPEN EMERGENCY APP",
                containerColor = SurfaceElevated.copy(alpha = 0.75f),
                contentColor = Color.White,
                onClick = {
                    onActionSelected("OPEN_EMERGENCY_APP")
                    onOpenEmergencyApp()
                    onDismiss()
                }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun OutcomeActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

private fun createEmergencyValveEvent(
    prefs: PrefManager,
    triggerReason: String,
    tokenConsumed: Boolean,
    selectedRecoveryAction: String,
    completed: Boolean
): EmergencyValveEvent {
    val today = LocalDate.now().toString()
    val focusSessionActive = prefs.activeTaskDate == today && prefs.activeTaskName.isNotBlank()

    return EmergencyValveEvent(
        timestamp = System.currentTimeMillis(),
        date = today,
        triggerReason = triggerReason,
        activeTaskName = if (focusSessionActive) prefs.activeTaskName else "",
        focusSessionActive = focusSessionActive,
        tokenConsumed = tokenConsumed,
        tokensRemainingAfter = prefs.emergencyValveTokens,
        selectedRecoveryAction = selectedRecoveryAction,
        completed = completed
    )
}

private fun applyEmergencyCooldown(
    context: android.content.Context,
    prefs: PrefManager,
    action: String
) {
    val duration = EmergencyCooldownPolicy.durationForAction(action)
    if (duration != null) {
        prefs.activateEmergencyValveCooldown(duration, context)
    } else {
        prefs.clearEmergencyValveCooldown(context)
    }
}

private fun cooldownConfirmationFor(action: String): String? {
    return EmergencyCooldownPolicy.confirmationForAction(action)
}

private fun onEmergencyAppSelected(
    context: android.content.Context,
    prefs: PrefManager,
    db: IronMindDatabase,
    scope: CoroutineScope,
    triggerReason: String,
    appChoice: EmergencyAppChoice,
    onDismiss: () -> Unit
) {
    applyEmergencyCooldown(context, prefs, "OPEN_EMERGENCY_APP")
    Toast.makeText(context, EmergencyCooldownPolicy.confirmationForAction("OPEN_EMERGENCY_APP"), Toast.LENGTH_SHORT).show()
    scope.launch {
        db.emergencyValveEventDao().insert(
            createEmergencyValveEvent(
                prefs = prefs,
                triggerReason = triggerReason,
                tokenConsumed = true,
                selectedRecoveryAction = "OPEN_EMERGENCY_APP:${appChoice.label}",
                completed = true
            )
        )
    }
    val intent = context.packageManager.getLaunchIntentForPackage(appChoice.packageName)
    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    if (intent != null) {
        context.startActivity(intent)
    }
    onDismiss()
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun EmergencyValveScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        EmergencyValveScreen(
            onDismiss = {}
        )
    }
}
