package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.alarm.AlarmReceiver
import com.sanket_satpute_20.ironmind.apps.AppClassificationRepository
import com.sanket_satpute_20.ironmind.apps.ClassifiedApp
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.mission.MissionExecutionResult
import com.sanket_satpute_20.ironmind.mission.MissionExecutionService
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine
import com.sanket_satpute_20.ironmind.ui.components.AppIconImage
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class WorkStartActivity : ComponentActivity() {

    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskName = intent.getStringExtra(AlarmReceiver.EXTRA_TASK_NAME) ?: "Classified Task"
        val taskId = intent.getIntExtra(AlarmReceiver.EXTRA_TASK_ID, -1)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        onBackPressedDispatcher.addCallback(this) { performHomeExit() }
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContent {
            IronMindTheme {
                var showAbortConfirm by remember { mutableStateOf(false) }
                val classificationRepository = remember { AppClassificationRepository.getInstance(applicationContext) }
                var contextCandidates by remember { mutableStateOf<List<ClassifiedApp>>(emptyList()) }
                var selectedContextApps by remember { mutableStateOf<Set<String>>(emptySet()) }
                var requiredPackage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(taskId) {
                    if (taskId != -1) {
                        val task = IronMindDatabase.getDatabase(applicationContext).taskDao().getTaskById(taskId)
                        requiredPackage = task?.packageName
                    }
                    val contextApps = classificationRepository.getContextApps()
                    contextCandidates = contextApps
                    selectedContextApps = requiredPackage
                        ?.takeIf { packageName -> contextApps.any { it.packageName == packageName } }
                        ?.let { setOf(it) }
                        ?: emptySet()
                }

                var showShieldShatter by remember { mutableStateOf(false) }

                Box {
                    MissionBriefingScreen(
                        taskName = taskName,
                        taskId = taskId,
                        contextCandidates = contextCandidates,
                        selectedContextApps = selectedContextApps,
                        onToggleContextApp = { packageName ->
                            selectedContextApps = selectedContextApps.toMutableSet().also { current ->
                                if (!current.add(packageName)) {
                                    current.remove(packageName)
                                }
                            }.toSet()
                        },
                        onStart = {
                            startFocusSession(taskId, taskName, selectedContextApps)
                            finish()
                        },
                        onDefer = {
                            deferTask(taskId)
                            finish()
                        },
                        onAbort = { showAbortConfirm = true },
                        onPulse = { triggerHeartbeat() }
                    )

                    if (showAbortConfirm) {
                        AbortConfirmationDialog(
                            onConfirm = {
                                showAbortConfirm = false
                                val shattered = abortTask(taskId)
                                if (shattered) {
                                    showShieldShatter = true
                                    com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(this@WorkStartActivity).playError(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(this@WorkStartActivity).playTemptationBlocked()
                                } else {
                                    finish()
                                }
                            },
                            onDismiss = { showAbortConfirm = false }
                        )
                    }
                    
                    if (showShieldShatter) {
                        com.sanket_satpute_20.ironmind.ui.components.ShieldShatterOverlay(
                            onDismiss = {
                                showShieldShatter = false
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun triggerHeartbeat() {
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(this).playSuccess(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(this).playTaskComplete()
    }

    private fun performHomeExit() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    private fun startFocusSession(taskId: Int, taskName: String, selectedContextApps: Set<String>) {
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(this).playSuccess(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(this).playTaskComplete()
        if (taskId == -1) return
        val prefs = PrefManager.getInstance(this)
        val missionExecutionService = MissionExecutionService(this)
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = missionExecutionService.startMission(taskId = taskId, allowedPackages = selectedContextApps)) {
                is MissionExecutionResult.Started -> {
                    prefs.totalXp += 10
                    launch(Dispatchers.Main) {
                        startActivity(
                            WorkLockActivity.createIntent(
                                context = this@WorkStartActivity,
                                taskId = result.task.id,
                                taskName = result.task.name,
                                taskDate = result.task.date,
                                taskStartTime = result.task.startTime,
                                taskEndTime = result.task.endTime
                            )
                        )
                    }
                }
                is MissionExecutionResult.AlreadyActive -> {
                    launch(Dispatchers.Main) {
                        startActivity(
                            WorkLockActivity.createIntent(
                                context = this@WorkStartActivity,
                                taskId = result.task.id,
                                taskName = result.task.name,
                                taskDate = result.task.date,
                                taskStartTime = result.task.startTime,
                                taskEndTime = result.task.endTime
                            )
                        )
                    }
                }
                is MissionExecutionResult.InvalidWindow -> {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@WorkStartActivity, result.reason, Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@WorkStartActivity, "Unable to start this mission.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun deferTask(taskId: Int) {
        if (taskId == -1) return
        val prefs = PrefManager.getInstance(this)
        prefs.clearActiveMissionContextApps()
        prefs.clearWorkLock()
        prefs.clearPomodoro()
        sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(packageName) })
        com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(this).penalizeXp(5L)
        val missionExecutionService = MissionExecutionService(this)
        CoroutineScope(Dispatchers.IO).launch {
            missionExecutionService.deferMission(taskId = taskId, reason = "MISSION_BRIEFING_DEFER")
        }
    }

    private fun abortTask(taskId: Int): Boolean {
        var shieldShattered = false
        if (taskId == -1) return shieldShattered
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(this).playError(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(this).playTemptationBlocked()
        val prefs = PrefManager.getInstance(this)
        prefs.clearActiveMissionContextApps()
        prefs.clearWorkLock()
        prefs.clearPomodoro()
        sendBroadcast(Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(packageName) })
        com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(this).penalizeXp(25L)
        if (prefs.streakShields > 0) {
            prefs.streakShields = prefs.streakShields - 1
            shieldShattered = true
        } else if (prefs.isStreakInCriticalState) {
            com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(this).resetStreak()
            prefs.isStreakInCriticalState = false
        } else {
            prefs.isStreakInCriticalState = true
            prefs.streakCriticalTimestamp = System.currentTimeMillis()
        }
        val missionExecutionService = MissionExecutionService(this)
        CoroutineScope(Dispatchers.IO).launch {
            missionExecutionService.skipMission(taskId = taskId, skipReason = "MISSION_ABORT")
        }
        return shieldShattered
    }

    companion object {
        fun createIntent(
            context: Context,
            taskId: Int,
            taskName: String,
            newTask: Boolean = false
        ): Intent {
            return Intent(context, WorkStartActivity::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_TASK_NAME, taskName)
                putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
                if (newTask) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }
    }
}

@Composable
fun AbortConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        title = { Text("CONFIRM ABORT", fontWeight = FontWeight.Black, color = ErrorRed) },
        text = {
            Text(
                "Choosing to abort this mission will break your streak and cost 25 XP.\n\nAre you sure you want to surrender?",
                color = Color.White.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                Text("SURRENDER", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("GO BACK", color = Color.Gray) }
        }
    )
}

@Composable
fun MissionBriefingScreen(
    taskName: String,
    taskId: Int,
    contextCandidates: List<ClassifiedApp>,
    selectedContextApps: Set<String>,
    onToggleContextApp: (String) -> Unit,
    onStart: () -> Unit,
    onDefer: () -> Unit,
    onAbort: () -> Unit,
    onPulse: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { IronMindDatabase.getDatabase(context) }
    var importanceRank by remember { mutableIntStateOf(3) }
    
    LaunchedEffect(taskId) {
        if (taskId != -1) {
            val task = db.taskDao().getTaskById(taskId)
            importanceRank = task?.importanceRank ?: 3
        }
    }

    val prefs = remember { PrefManager.getInstance(context) }
    val userType = AdaptiveEngine.getCurrentType(prefs)
    val identityTitle = IdentityLevelEngine.getIdentityTitle(userType, prefs.currentIdentityLevel)
    val hasContextChoice = contextCandidates.size > 1

    val infiniteTransition = rememberInfiniteTransition(label = "mission")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse"
    )
    val scrollState = rememberScrollState()

    LaunchedEffect(pulse) {
        if (pulse > 1.04f) onPulse()
    }

    val rankColor = when(importanceRank) {
        1 -> GoldXP // CRITICAL
        2 -> NeonCyan // VITAL
        else -> Color.White // GROWTH
    }

    val rankLabel = when(importanceRank) {
        1 -> "CRITICAL MISSION"
        2 -> "VITAL MISSION"
        else -> "GROWTH MISSION"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val compact = maxWidth < 380.dp || maxHeight < 760.dp
        val horizontalPadding = if (compact) 20.dp else 32.dp
        val headerTop = if (compact) 20.dp else 48.dp
        val actionRingSize = if (compact) 116.dp else 140.dp
        val actionButtonSize = if (compact) 84.dp else 100.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 18.dp else 24.dp)
        ) {
            Spacer(modifier = Modifier.height(headerTop))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = rankColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        rankLabel,
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black),
                        color = rankColor,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    taskName.uppercase(),
                    style = if (compact) {
                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    } else {
                        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    },
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (hasContextChoice) "Choose your mission context, then start." else "This mission is ready to begin now.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            MissionArmingCard(
                hasContextChoice = hasContextChoice,
                selectedContextCount = selectedContextApps.size
            )

            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulse)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("IDENTITY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(
                        identityTitle.uppercase(),
                        fontSize = if (compact) 20.sp else 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Level ${prefs.currentIdentityLevel}", fontSize = 12.sp, color = GoldXP, fontWeight = FontWeight.Bold)
                }
            }

            if (contextCandidates.isNotEmpty()) {
                ContextMissionCard(
                    contextCandidates = contextCandidates,
                    selectedContextApps = selectedContextApps,
                    onToggleContextApp = onToggleContextApp,
                    compact = compact
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(actionRingSize)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color.White.copy(alpha = 0.05f), style = Stroke(width = 2.dp.toPx()))
                        drawArc(
                            brush = Brush.sweepGradient(listOf(SurfaceElevated, Color.Transparent)),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                    Button(
                        onClick = onStart,
                        modifier = Modifier.size(actionButtonSize),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(if (compact) 34.dp else 40.dp))
                    }
                }

                Text(
                    "START FOCUS",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    if (hasContextChoice) "Tap once to arm the session and carry only the apps you selected." else "Tap once to arm the session and move straight into protected work.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            MissionSecondaryActions(
                compact = compact,
                onDefer = onDefer,
                onAbort = onAbort
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MissionArmingCard(
    hasContextChoice: Boolean,
    selectedContextCount: Int
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "ON START",
                fontSize = 10.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
            MissionArmingLine("Focus timer starts")
            MissionArmingLine("Spring seals if work lock is enabled")
            MissionArmingLine(
                if (hasContextChoice) {
                    if (selectedContextCount > 0) "$selectedContextCount context app${if (selectedContextCount == 1) "" else "s"} stay allowed"
                    else "Only the selected context apps stay allowed"
                } else {
                    "Mission boundary arms immediately"
                }
            )
        }
    }
}

@Composable
private fun MissionArmingLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(SuccessGreen, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MissionSecondaryActions(
    compact: Boolean,
    onDefer: () -> Unit,
    onAbort: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.035f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "OTHER OPTIONS",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MissionExitCard(
                        icon = Icons.Rounded.History,
                        iconTint = Color.Gray,
                        title = "DEFER",
                        subtitle = "Push this window and take -5 XP",
                        subtitleColor = Color.Gray,
                        borderColor = Color.Transparent,
                        surfaceColor = Color.White.copy(alpha = 0.05f),
                        onClick = onDefer
                    )
                    MissionExitCard(
                        icon = Icons.Rounded.Close,
                        iconTint = ErrorRed,
                        title = "ABORT",
                        subtitle = "Fail this mission and cut the streak",
                        subtitleColor = WarningAmber,
                        borderColor = ErrorRed.copy(alpha = 0.22f),
                        surfaceColor = ErrorRed.copy(alpha = 0.08f),
                        onClick = onAbort
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MissionExitCard(
                        icon = Icons.Rounded.History,
                        iconTint = Color.Gray,
                        title = "DEFER",
                        subtitle = "Push this window and take -5 XP",
                        subtitleColor = Color.Gray,
                        borderColor = Color.Transparent,
                        surfaceColor = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.weight(1f),
                        onClick = onDefer
                    )
                    MissionExitCard(
                        icon = Icons.Rounded.Close,
                        iconTint = ErrorRed,
                        title = "ABORT",
                        subtitle = "Fail this mission and cut the streak",
                        subtitleColor = WarningAmber,
                        borderColor = ErrorRed.copy(alpha = 0.22f),
                        surfaceColor = ErrorRed.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f),
                        onClick = onAbort
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionExitCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    borderColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = surfaceColor,
        shape = RoundedCornerShape(16.dp),
        border = if (borderColor == Color.Transparent) null else BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(subtitle, fontSize = 10.sp, color = subtitleColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ContextMissionCard(
    contextCandidates: List<ClassifiedApp>,
    selectedContextApps: Set<String>,
    onToggleContextApp: (String) -> Unit,
    compact: Boolean
) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Language, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "MISSION CONTEXT",
                    color = ElectricViolet,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Select only the apps you genuinely need for this task.",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = if (compact) 12.sp else 13.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(contextCandidates, key = { it.packageName }) { app ->
                    val selected = app.packageName in selectedContextApps
                    Surface(
                        onClick = { onToggleContextApp(app.packageName) },
                        color = if (selected) ElectricViolet.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, if (selected) ElectricViolet else Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = if (compact) 10.dp else 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AppIconImage(packageName = app.packageName, modifier = Modifier.size(40.dp))
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(ElectricViolet, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                app.appName,
                                color = Color.White,
                                fontSize = if (compact) 10.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                if (selected) "ALLOWED" else "BLOCKED",
                                color = if (selected) ElectricViolet else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun WorkStartActivityPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        WorkStartPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun WorkStartPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            modifier = androidx.compose.ui.Modifier.padding(24.dp)
        ) {
            androidx.compose.material3.Text(
                "🎯 READY TO LOCK IN?",
                color = GoldXP,
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black
            )
            androidx.compose.material3.Text(
                "Deep Work Block",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.material3.Text(
                "09:00 – 10:30",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}