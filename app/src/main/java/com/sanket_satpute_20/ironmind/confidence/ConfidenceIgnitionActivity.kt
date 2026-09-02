package com.sanket_satpute_20.ironmind.confidence

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.VoiceLog
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchActivity
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.voice.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private const val MAX_IGNITION_RECORD_SECONDS = 45

class ConfidenceIgnitionActivity : ComponentActivity() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, ConfidenceIgnitionActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
    }

    private val viewModel by viewModels<ConfidenceIgnitionViewModel>()
    private var advancing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        onBackPressedDispatcher.addCallback(this) { }

        setContent {
            IronMindTheme {
                ConfidenceIgnitionRoute(
                    viewModel = viewModel,
                    onDone = {
                        advancing = true
                        startActivity(MorningLaunchActivity.createIntent(this))
                        finish()
                    }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isFinishing && !advancing) startActivity(createIntent(this))
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing && !isChangingConfigurations && !advancing) {
            startActivity(createIntent(this))
        }
    }
}

@Composable
private fun ConfidenceIgnitionRoute(
    viewModel: ConfidenceIgnitionViewModel,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val recoveryMode = remember { LocalTime.now().isAfter(LocalTime.NOON) }

    if (uiState.currentStep == ConfidenceIgnitionStep.COMPLETE) {
        ConfidenceIgnitionCompleteScreen(
            line = uiState.line.text,
            courageCue = uiState.couragePrompt.cue,
            recoveryMode = recoveryMode,
            onDone = onDone
        )
        return
    }

    ConfidenceIgnitionScreen(
        uiState = uiState,
        recoveryMode = recoveryMode,
        isSaving = isSaving,
        onBreathDone = viewModel::markBreathDone,
        onVoiceDone = viewModel::markVoiceRecorded,
        onMoveDone = viewModel::markMoveDone,
        onCourageAccepted = viewModel::markCourageAccepted
    )
}

@Composable
private fun ConfidenceIgnitionScreen(
    uiState: ConfidenceIgnitionUiState,
    recoveryMode: Boolean,
    isSaving: Boolean,
    onBreathDone: () -> Unit,
    onVoiceDone: () -> Unit,
    onMoveDone: () -> Unit,
    onCourageAccepted: () -> Unit
) {
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = if (recoveryMode) WarningAmber.copy(alpha = 0.14f) else NeonCyan.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, if (recoveryMode) WarningAmber.copy(alpha = 0.3f) else NeonCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (recoveryMode) "RECOVERY IGNITION" else "CONFIDENCE IGNITION",
                        color = if (recoveryMode) WarningAmber else NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Text(
                    text = uiState.line.text,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    lineHeight = 34.sp
                )
                Text(
                    text = if (recoveryMode) "Reset. Reclaim." else "Steady. Speak. Edge.",
                    color = Color.White.copy(alpha = 0.66f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                IgnitionProgressRow(step = uiState.currentStep)
            }

            when (uiState.currentStep) {
                ConfidenceIgnitionStep.BREATHE -> {
                    IgnitionStepCard(
                        stepLabel = "BREATHE",
                        accent = NeonCyan,
                        icon = Icons.Rounded.SelfImprovement,
                        title = if (recoveryMode) "Reset your breathing." else "Take 3 slow breaths.",
                        body = if (recoveryMode) "Slow down first." else "Stand tall."
                    ) {
                        Button(
                            onClick = onBreathDone,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("3 BREATHS DONE", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                ConfidenceIgnitionStep.SPEAK -> {
                    VoiceIgnitionStep(
                        prompt = uiState.voicePrompt,
                        isSaving = isSaving,
                        onVoiceSaved = onVoiceDone
                    )
                }

                ConfidenceIgnitionStep.MOVE -> {
                    IgnitionStepCard(
                        stepLabel = "MOVE",
                        accent = SuccessGreen,
                        icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                        title = uiState.movePrompt.title,
                        body = uiState.movePrompt.cue
                    ) {
                        Button(
                            onClick = onMoveDone,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("MOVE COMPLETE", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                ConfidenceIgnitionStep.COURAGE -> {
                    IgnitionStepCard(
                        stepLabel = "EDGE",
                        accent = WarningAmber,
                        icon = Icons.Rounded.Bolt,
                        title = uiState.couragePrompt.title,
                        body = uiState.couragePrompt.cue
                    ) {
                        Button(
                            onClick = onCourageAccepted,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                        ) {
                            Text("I'LL FACE THIS TODAY", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                ConfidenceIgnitionStep.COMPLETE -> Unit
            }
        }
    }
}

@Composable
private fun VoiceIgnitionStep(
    prompt: IgnitionVoicePrompt,
    isSaving: Boolean,
    onVoiceSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recorder = remember { VoiceRecorder(context) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf("") }
    var recordedFile by remember { mutableStateOf<File?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (!granted) {
            errorMessage = "Mic permission is required for the morning voice rep."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (recorder.isCurrentlyRecording()) {
                recorder.cancelRecording()
            }
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingSeconds < MAX_IGNITION_RECORD_SECONDS) {
                delay(1000L)
                recordingSeconds++
            }
            if (isRecording) {
                recordedFile = recorder.stopRecording()
                isRecording = false
            }
        }
    }

    IgnitionStepCard(
        stepLabel = "VOICE",
        accent = NeonCyan,
        icon = Icons.Rounded.RecordVoiceOver,
        title = prompt.title,
        body = prompt.cue
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        progress = { recordingSeconds / MAX_IGNITION_RECORD_SECONDS.toFloat() },
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 8.dp,
                        color = NeonCyan,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "$recordingSeconds s",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    recordedFile = recorder.stopRecording()
                    isRecording = false
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("STOP REP", color = Color.Black, fontWeight = FontWeight.Black)
            }
        } else if (recordedFile == null) {
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = WarningAmber,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(12.dp))
            }
            Button(
                onClick = {
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        errorMessage = ""
                        recordingSeconds = 0
                        recorder.startRecording(
                            taskName = "confidence_ignition",
                            maxDurationMs = MAX_IGNITION_RECORD_SECONDS * 1000
                        )
                        isRecording = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.weight(1f, fill = false))
                Text("START VOICE REP", color = Color.Black, fontWeight = FontWeight.Black)
            }
            if (!hasPermission || errorMessage.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Fallback",
                            color = WarningAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Say it out loud. Keep the reset.",
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = onVoiceSaved,
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                        ) {
                            Text("I SPOKE IT ALOUD", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Rep captured. Lock it.",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    val file = recordedFile ?: return@Button
                    scope.launch(Dispatchers.IO) {
                        val today = java.time.LocalDate.now()
                        val month = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                        IronMindDatabase.getDatabase(context).voiceLogDao().insert(
                            VoiceLog(
                                taskName = "Confidence Ignition",
                                date = today.toString(),
                                month = month,
                                filePath = file.absolutePath,
                                durationSeconds = recordingSeconds,
                                timestamp = System.currentTimeMillis(),
                                entryType = "CONFIDENCE_IGNITION",
                                promptType = prompt.id,
                                clarityScore = 0,
                                confidenceScore = 0,
                                playbackCompleted = false
                            )
                        )
                        launch(Dispatchers.Main) {
                            onVoiceSaved()
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.weight(1f, fill = false))
                    Text("SAVE REP", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun IgnitionStepCard(
    stepLabel: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.size(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    text = stepLabel,
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.1.sp
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                lineHeight = 34.sp
            )
            Text(
                text = body,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            content()
        }
    }
}

@Composable
private fun IgnitionProgressRow(step: ConfidenceIgnitionStep) {
    val items = listOf(
        ConfidenceIgnitionStep.BREATHE to "Breathe",
        ConfidenceIgnitionStep.SPEAK to "Speak",
        ConfidenceIgnitionStep.MOVE to "Move",
        ConfidenceIgnitionStep.COURAGE to "Edge"
    )
    val activeIndex = when (step) {
        ConfidenceIgnitionStep.BREATHE -> 0
        ConfidenceIgnitionStep.SPEAK -> 1
        ConfidenceIgnitionStep.MOVE -> 2
        ConfidenceIgnitionStep.COURAGE -> 3
        ConfidenceIgnitionStep.COMPLETE -> 4
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            val complete = index < activeIndex || step == ConfidenceIgnitionStep.COMPLETE
            val active = index == activeIndex && step != ConfidenceIgnitionStep.COMPLETE
            val color = when {
                complete -> SuccessGreen
                active -> NeonCyan
                else -> Color.White.copy(alpha = 0.14f)
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = color.copy(alpha = if (active || complete) 0.18f else 0.08f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, color.copy(alpha = if (active || complete) 0.45f else 0.18f))
            ) {
                Text(
                    text = item.second.uppercase(),
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    color = if (active || complete) color else Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ConfidenceIgnitionCompleteScreen(
    line: String,
    courageCue: String,
    recoveryMode: Boolean,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.16f),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Waves, contentDescription = null, tint = SuccessGreen)
                    }
                }
                Text(
                    text = if (recoveryMode) "Recovery locked." else "Proof collected.",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
                Text(
                    text = if (recoveryMode) "Day recovered." else "Day armed.",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Text(
                    text = courageCue,
                    color = Color.White.copy(alpha = 0.74f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text(if (recoveryMode) "RETURN TO THE DAY" else "ENTER THE DAY", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun ConfidenceIgnitionScreenPreview() {
    IronMindTheme {
        ConfidenceIgnitionScreen(
            uiState = ConfidenceIgnitionUiState(), recoveryMode = false, isSaving = false,
            onBreathDone = {}, onVoiceDone = {}, onMoveDone = {}, onCourageAccepted = {}
        )
    }
}
