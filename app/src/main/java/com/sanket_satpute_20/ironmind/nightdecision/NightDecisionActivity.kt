package com.sanket_satpute_20.ironmind.nightdecision

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.integrity.IntegrityShieldViewModel
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockActivity
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.utils.PermissionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

class NightDecisionActivity : ComponentActivity() {

    companion object {
        private const val ACTION_FINISH = "com.sanket_satpute_20.ironmind.nightdecision.FINISH"

        fun createIntent(context: Context): Intent =
            Intent(context, NightDecisionActivity::class.java).apply {
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
    private var plannerHandoffInProgress = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = NightDecisionManager(this)
        manager.reconcileRuntimeState()
        if (!PrefManager.getInstance(this).nightDecisionActive) {
            finish()
            return
        }
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
        
        setContent {
            val ritualViewModel: NightRitualViewModel = viewModel()
            val ritualState by ritualViewModel.uiState.collectAsState()

            IronMindTheme {
                com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry {
                    Crossfade(targetState = ritualState, label = "night_ritual_crossfade") { state ->
                    when (state) {
                        is NightRitualState.Loading -> {
                            com.sanket_satpute_20.ironmind.ui.components.PremiumScreenShimmer(
                                modifier = Modifier.fillMaxSize().background(DeepBackground)
                            )
                        }
                        is NightRitualState.Finished -> {
                            LaunchedEffect(Unit) {
                                ritualViewModel.resetRitual()
                                finish()
                            }
                        }
                        is NightRitualState.Active -> {
                            NightRitualGateScreen(
                                state = state,
                                viewModel = ritualViewModel,
                                onPlanTomorrow = {
                                    plannerHandoffInProgress = true
                                },
                                onClose = { finish() },
                                permissionLauncher = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                            )
                        }
                    }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        plannerHandoffInProgress = false
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val prefs = PrefManager.getInstance(this)
        if (!isFinishing && prefs.nightDecisionActive && !plannerHandoffInProgress) {
            startActivity(createIntent(this))
        }
    }

    override fun onStop() {
        super.onStop()
        val prefs = PrefManager.getInstance(this)
        if (!isFinishing && !isChangingConfigurations && prefs.nightDecisionActive && !plannerHandoffInProgress) {
            startActivity(createIntent(this))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(finishReceiver) }
    }

    private fun setupWindowFlags() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }
}

@Composable
private fun NightRitualGateScreen(
    state: NightRitualState.Active,
    viewModel: NightRitualViewModel,
    onPlanTomorrow: () -> Unit,
    onClose: () -> Unit,
    permissionLauncher: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val manager = remember { NightDecisionManager(context) }
    val prefs = remember { PrefManager.getInstance(context) }
    val targetDate = remember { manager.resolveTargetDate() }
    val scope = rememberCoroutineScope()
    
    var showHolidayDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var leaveReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!prefs.nightDecisionActive) {
            manager.startDecision(source = "MANUAL_GATE")
        }
    }

    LaunchedEffect(state.currentStep) {
        if (state.currentStep == NightRitualStep.HANDOFF) {
            viewModel.completeRitual()
            if (state.sleepLockEnabled && PermissionHelper.canDrawOverlays(context)) {
                context.startActivity(SleepLockActivity.createIntent(context))
            } else {
                NightDecisionAlarmReceiver.dismissNotification(context)
                onClose()
            }
        }
    }

    fun finishHoliday() {
        scope.launch {
            clearNightDecisionTasksWithHistory(context, targetDate, "NIGHT_DECISION_HOLIDAY")
            manager.markHoliday()
            NightDecisionAlarmReceiver.dismissNotification(context)
            NightDecisionScheduler.schedule(context)
            viewModel.completePlanning() // Moves to Handoff
        }
    }

    fun finishEmergencyLeave() {
        scope.launch {
            clearNightDecisionTasksWithHistory(context, targetDate, "NIGHT_DECISION_EMERGENCY_LEAVE")
            manager.markEmergencyLeave(leaveReason.trim().ifBlank { null })
            NightDecisionAlarmReceiver.dismissNotification(context)
            NightDecisionScheduler.schedule(context)
            viewModel.completePlanning() // Moves to Handoff
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBackground
    ) {
        val midnightGlow = rememberInfiniteTransition(label = "night_glow")
        val glow by midnightGlow.animateFloat(
            initialValue = 0.88f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "night_glow_value"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SurfaceDark, DeepBackground, DeepBackground)
                    )
                )
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size((320.dp * glow))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(ElectricViolet.copy(alpha = 0.2f), SurfaceElevated.copy(alpha = 0.07f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    NightDecisionTopChip(
                        label = "EVENING CHECKPOINT",
                        accent = TextPrimary
                    )

                    when (state.currentStep) {
                        NightRitualStep.SHIELD_RECAP -> {
                            ShieldRecapStep(onContinue = { viewModel.completeShieldRecap() })
                        }
                        NightRitualStep.REFLECTION -> {
                            ReflectionStep(
                                state = state,
                                viewModel = viewModel,
                                permissionLauncher = permissionLauncher
                            )
                        }
                        NightRitualStep.RECOVERY_RECOMMENDATION -> {
                            RecoveryRecommendationStep(
                                state = state,
                                viewModel = viewModel
                            )
                        }
                        NightRitualStep.PLANNING, NightRitualStep.HANDOFF -> {
                            PlanningStepHeader(targetDate = targetDate)
                        }
                    }
                }

                if (state.currentStep == NightRitualStep.PLANNING) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        NightDecisionChoiceCard(
                            eyebrow = "PLAN",
                            title = "BUILD TOMORROW",
                            subtitle = "Set tomorrow's board now. Up to 6 tasks.",
                            accent = ElectricViolet,
                            highlighted = true,
                            onClick = {
                                onPlanTomorrow()
                                NightDecisionAlarmReceiver.dismissNotification(context)
                                val route = "task_builder?date=${targetDate}"
                                context.startActivity(MainActivity.createRouteIntent(context, route))
                                viewModel.completePlanning() // Complete planning, ready for handoff when returns
                            }
                        )
                        NightDecisionChoiceCard(
                            eyebrow = "REST",
                            title = "MARK A HOLIDAY",
                            subtitle = "Keep tomorrow clear. No task pressure.",
                            accent = NeonCyan,
                            highlighted = false,
                            onClick = { showHolidayDialog = true }
                        )
                        NightDecisionChoiceCard(
                            eyebrow = "SAFETY",
                            title = "EMERGENCY LEAVE",
                            subtitle = "Leave tomorrow open when something real needs the space.",
                            accent = WarningAmber,
                            highlighted = false,
                            onClick = { showEmergencyDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Dialogs for Holiday and Emergency Leave
    if (showHolidayDialog) {
        AlertDialog(
            onDismissRequest = { showHolidayDialog = false },
            title = { Text("Set tomorrow as a holiday?", fontWeight = FontWeight.Bold) },
            text = { Text("Tomorrow will stay clear. Existing tasks for that day will be removed.") },
            confirmButton = {
                Button(
                    onClick = { showHolidayDialog = false; finishHoliday() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepBackground)
                ) { Text("Set Holiday") }
            },
            dismissButton = {
                TextButton(onClick = { showHolidayDialog = false }) { Text("Cancel") }
            },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary
        )
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Take emergency leave tomorrow?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Tomorrow will stay open, planned tasks will be cleared, and ${NightDecisionManager.EMERGENCY_LEAVE_XP_COST} XP will be deducted.",
                        color = TextPrimary
                    )
                    TextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it.take(120) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        placeholder = { Text("Reason (optional)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = ElectricViolet,
                            unfocusedPlaceholderColor = ElectricViolet
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEmergencyDialog = false; finishEmergencyLeave() },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = DeepBackground)
                ) { Text("Use Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) { Text("Cancel") }
            },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary
        )
    }
}

@Composable
private fun ShieldRecapStep(onContinue: () -> Unit) {
    val integrityShieldViewModel: IntegrityShieldViewModel = viewModel()
    val shieldUiState by integrityShieldViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        delay(300)
        integrityShieldViewModel.sealDay()
    }

    com.sanket_satpute_20.ironmind.ui.components.IntegrityShield(
        integrityLevel = shieldUiState.integrityLevel,
        mode = shieldUiState.mode,
        event = shieldUiState.currentEvent,
        eventId = shieldUiState.currentEventId,
        onEventHandled = integrityShieldViewModel::onEventHandled,
        modifier = Modifier.size(160.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val isGoodDay = shieldUiState.integrityLevel >= 0.6f
        val context = androidx.compose.ui.platform.LocalContext.current
        LaunchedEffect(isGoodDay) {
            if (!isGoodDay) {
                com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logRoughRecapViewed()
                val pref = com.sanket_satpute_20.ironmind.data.PrefManager.getInstance(context)
                if (pref.lastRelapseTimestamp == 0L) {
                    pref.lastRelapseTimestamp = System.currentTimeMillis()
                }
            }
        }
        
        Text(
            text = if (isGoodDay) "The Day is Sealed" else "Rough Day Reforge",
            color = if (isGoodDay) NeonCyan else WarningAmber,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isGoodDay) "Your focus held strong." else "Tomorrow is a clean slate. Let it go.",
            color = ElectricViolet,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    Spacer(modifier = Modifier.height(30.dp))
    
    Button(
        onClick = onContinue,
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReflectionStep(
    state: NightRitualState.Active,
    viewModel: NightRitualViewModel,
    permissionLauncher: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceHelper = remember { VoiceRecognizerHelper(context) }
    val isListening by voiceHelper.isListening.collectAsState()
    val speechText by voiceHelper.speechText.collectAsState()
    val errorMsg by voiceHelper.errorMsg.collectAsState()
    
    var reflection by remember { mutableStateOf("") }
    
    LaunchedEffect(speechText) {
        if (speechText.isNotBlank()) {
            reflection = speechText
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { voiceHelper.destroy() }
    }

    Text(
        text = "What is one thing you are proud of today?",
        color = TextPrimary,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))
    
    OutlinedTextField(
        value = reflection,
        onValueChange = { reflection = it },
        placeholder = { Text("I learned how to...", color = ElectricViolet) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = ElectricViolet,
            unfocusedBorderColor = SurfaceElevated
        ),
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )

    if (errorMsg != null) {
        Text(text = errorMsg ?: "", color = Color.Red, fontSize = 12.sp)
    }
    
    Spacer(modifier = Modifier.height(10.dp))
    
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    if (isListening) voiceHelper.stopListening() else voiceHelper.startListening()
                } else {
                    permissionLauncher()
                }
            },
            modifier = Modifier.background(SurfaceElevated, CircleShape)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = "Voice Input",
                tint = if (isListening) Color.Red else Color.White
            )
        }
        
        Button(
            onClick = { viewModel.saveReflection(reflection) },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
            modifier = Modifier.weight(1f)
        ) {
            Text("Save & Continue", color = DeepBackground, fontWeight = FontWeight.Bold)
        }
    }
    
    TextButton(onClick = { viewModel.skipReflection() }) {
        Text("Skip", color = ElectricViolet)
    }
}

@Composable
private fun RecoveryRecommendationStep(
    state: NightRitualState.Active,
    viewModel: NightRitualViewModel
) {
    Text(
        text = "You've had ${state.strugglingDays} rough days in a row.",
        color = WarningAmber,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "Burnout is real. We highly recommend marking tomorrow as a Recovery Day to rest without penalty.",
        color = ElectricViolet,
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(30.dp))
    
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = { viewModel.acceptRecoveryRecommendation() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Accept Recovery Day", color = DeepBackground, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { viewModel.declineRecoveryRecommendation() },
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I'll push through", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlanningStepHeader(targetDate: LocalDate) {
    Text(
        text = "Set tomorrow, then let the night go quiet.",
        color = TextPrimary,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
    )
    Text(
        text = formattedTargetDate(targetDate),
        color = ElectricViolet,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(20.dp))
    NightDecisionStatusRail(targetDate = targetDate)
}

@Composable
private fun NightDecisionTopChip(label: String, accent: Color) {
    Row(
        modifier = Modifier
            .background(color = TextPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(accent, CircleShape))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun NightDecisionStatusRail(targetDate: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NightDecisionStatPill("CHECK-IN", "30 MIN BEFORE BED")
        NightDecisionStatPill("TOMORROW", targetDate.dayOfWeek.name.take(3))
    }
}

@Composable
private fun RowScope.NightDecisionStatPill(title: String, value: String) {
    Surface(
        modifier = Modifier.weight(1f),
        color = SurfaceElevated.copy(alpha = 0.08f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.11f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, color = ElectricViolet, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NightDecisionChoiceCard(
    eyebrow: String, title: String, subtitle: String, accent: Color, highlighted: Boolean, onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) 0.985f else 1f
    val borderColor by animateColorAsState(targetValue = if (highlighted) accent.copy(alpha = 0.5f) else TextPrimary.copy(alpha = 0.1f), label = "border")
    val containerColor by animateColorAsState(targetValue = if (highlighted) SurfaceElevated else SurfaceDark, label = "bg")

    Surface(
        modifier = Modifier.fillMaxWidth().scale(scale).clickable(interactionSource = interaction, indication = null, onClick = onClick),
        color = containerColor, shape = RoundedCornerShape(30.dp), border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(eyebrow, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = ElectricViolet, fontSize = 13.sp, lineHeight = 18.sp)
            }
            Box(
                modifier = Modifier.size(42.dp).background(color = accent.copy(alpha = if (highlighted) 0.2f else 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(">", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

private fun formattedTargetDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
}


// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF05050A)
@androidx.compose.runtime.Composable
fun NightDecisionActivityPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        NightDecisionPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun NightDecisionPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            modifier = androidx.compose.ui.Modifier.padding(32.dp)
        ) {
            androidx.compose.material3.Text(
                "🌙 Night Decision",
                color = ElectricViolet,
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black
            )
            androidx.compose.material3.Text(
                "FRIDAY, 22 AUG",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                letterSpacing = 3.sp
            )
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(24.dp))
            listOf("📋 Plan Tomorrow", "🏖 Take a Rest Day", "🚨 Emergency Leave").forEach { choice ->
                androidx.compose.material3.OutlinedButton(
                    onClick = {},
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceElevated)
                ) {
                    androidx.compose.material3.Text(choice, color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
