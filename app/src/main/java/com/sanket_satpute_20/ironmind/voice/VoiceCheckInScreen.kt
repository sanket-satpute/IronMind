package com.sanket_satpute_20.ironmind.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.VoiceLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private enum class ConfidenceVoiceState {
    READY, RECORDING, PLAYBACK, RATING, SAVING, FINISHED, ERROR
}

private const val MIN_RECOMMENDED_SECONDS = 60
private const val MAX_RECORDING_SECONDS = 90

@Composable
fun VoiceCheckInScreen(
    taskName: String,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recorder = remember { VoiceRecorder(context) }
    val player = remember { VoicePlayer() }

    var state by remember { mutableStateOf(ConfidenceVoiceState.READY) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var clarityScore by remember { mutableIntStateOf(0) }
    var confidenceScore by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf("") }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var playedBackOnce by remember { mutableStateOf(false) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (!granted) {
            errorMessage = "Mic permission is required."
            state = ConfidenceVoiceState.ERROR
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.stopPlayback()
            if (recorder.isCurrentlyRecording()) {
                recorder.cancelRecording()
            }
        }
    }

    LaunchedEffect(state) {
        if (state == ConfidenceVoiceState.RECORDING) {
            while (state == ConfidenceVoiceState.RECORDING && recordingSeconds < MAX_RECORDING_SECONDS) {
                delay(1000L)
                recordingSeconds++
            }
            if (state == ConfidenceVoiceState.RECORDING) {
                recordedFile = recorder.stopRecording()
                state = if (recordedFile != null) ConfidenceVoiceState.PLAYBACK else ConfidenceVoiceState.ERROR
                if (recordedFile == null) {
                    errorMessage = "Recording could not be saved."
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DeepBackground,
                        DeepBackground,
                        DeepBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = SurfaceDark.copy(alpha = 0.94f),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.18f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("VOICE REP", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    when (state) {
                        ConfidenceVoiceState.READY -> "Say one clear thing about what you built, learned, or will do next."
                        ConfidenceVoiceState.RECORDING -> "Keep going. The point is reps, not perfection."
                        ConfidenceVoiceState.PLAYBACK -> "Listen back once. Hear your real voice without overthinking it."
                        ConfidenceVoiceState.RATING -> "Give this rep a quick honest read."
                        ConfidenceVoiceState.SAVING -> "Saving today's rep."
                        ConfidenceVoiceState.FINISHED -> "Today's voice rep is complete."
                        ConfidenceVoiceState.ERROR -> errorMessage.ifBlank { "Something interrupted the voice log." }
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    taskName.ifBlank { "Confidence Reflection" },
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                when (state) {
                    ConfidenceVoiceState.READY -> {
                        PromptCard()
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    errorMessage = ""
                                    clarityScore = 0
                                    confidenceScore = 0
                                    playedBackOnce = false
                                    recordingSeconds = 0
                                    recordedFile = null
                                    recorder.startRecording(
                                        taskName = taskName.ifBlank { "confidence_log" },
                                        maxDurationMs = MAX_RECORDING_SECONDS * 1000
                                    )
                                    state = ConfidenceVoiceState.RECORDING
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("START VOICE REP", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }

                    ConfidenceVoiceState.RECORDING -> {
                        RecordingDial(seconds = recordingSeconds)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            if (recordingSeconds < MIN_RECOMMENDED_SECONDS) {
                                "Aim for at least $MIN_RECOMMENDED_SECONDS seconds before you stop."
                            } else {
                                "Good. Keep your pace steady or stop whenever you're done."
                            },
                            color = Color.White.copy(alpha = 0.64f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                recordedFile = recorder.stopRecording()
                                state = if (recordedFile != null) ConfidenceVoiceState.PLAYBACK else ConfidenceVoiceState.ERROR
                                if (recordedFile == null) errorMessage = "Recording could not be saved."
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(Icons.Rounded.Stop, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("STOP AND LISTEN", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }

                    ConfidenceVoiceState.PLAYBACK -> {
                        PlaybackCard(
                            hasPlayedBackOnce = playedBackOnce,
                            onPlay = {
                                val path = recordedFile?.absolutePath ?: return@PlaybackCard
                                player.play(path) {
                                    playedBackOnce = true
                                }
                            },
                            onStop = { player.stopPlayback() }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { state = ConfidenceVoiceState.RATING },
                            enabled = playedBackOnce,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("CHECK THIS REP", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }

                    ConfidenceVoiceState.RATING -> {
                        RatingSection(
                            title = "Clarity",
                            subtitle = "Could someone follow your explanation?",
                            score = clarityScore,
                            onSelect = { clarityScore = it }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        RatingSection(
                            title = "Confidence",
                            subtitle = "Did you sound steady and believable?",
                            score = confidenceScore,
                            onSelect = { confidenceScore = it }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                state = ConfidenceVoiceState.SAVING
                                scope.launch(Dispatchers.IO) {
                                    val today = LocalDate.now()
                                    val month = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                                    IronMindDatabase.getDatabase(context).voiceLogDao().insert(
                                        VoiceLog(
                                            taskName = taskName.ifBlank { "Confidence Reflection" },
                                            date = today.toString(),
                                            month = month,
                                            filePath = recordedFile?.absolutePath ?: "",
                                            durationSeconds = recordingSeconds,
                                            timestamp = System.currentTimeMillis(),
                                            entryType = "CONFIDENCE",
                                            promptType = "DAILY_REFLECTION",
                                            clarityScore = clarityScore,
                                            confidenceScore = confidenceScore,
                                            playbackCompleted = playedBackOnce
                                        )
                                    )
                                    launch(Dispatchers.Main) { state = ConfidenceVoiceState.FINISHED }
                                }
                            },
                            enabled = clarityScore > 0 && confidenceScore > 0,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("SAVE THIS REP", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }

                    ConfidenceVoiceState.SAVING -> {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(64.dp))
                    }

                    ConfidenceVoiceState.FINISHED -> {
                        FinishCard(
                            clarityScore = clarityScore,
                            confidenceScore = confidenceScore,
                            onComplete = onComplete
                        )
                    }

                    ConfidenceVoiceState.ERROR -> {
                        Text(errorMessage, color = WarningAmber, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { state = ConfidenceVoiceState.READY }) {
                            Text("TRY AGAIN")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onComplete) {
                            Text("Back", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptCard() {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("TODAY'S REP", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("What did you build, learn, or understand better today?", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Keep it simple. One clear explanation is enough.", color = Color.White.copy(alpha = 0.58f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun RecordingDial(seconds: Int) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { seconds / MAX_RECORDING_SECONDS.toFloat() },
            modifier = Modifier.size(128.dp),
            color = ErrorRed,
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeWidth = 8.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(String.format("%02d", seconds), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("seconds", color = Color.White.copy(alpha = 0.56f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun PlaybackCard(
    hasPlayedBackOnce: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Listen once before you score it.", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (hasPlayedBackOnce) "Good. You heard the rep clearly."
                else "Just listen for clarity and steadiness. No need to overjudge it.",
                color = Color.White.copy(alpha = 0.58f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPlay, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (hasPlayedBackOnce) "PLAY AGAIN" else "PLAY", color = Color.Black, fontWeight = FontWeight.Black)
                }
                OutlinedButton(onClick = onStop) {
                    Icon(Icons.Rounded.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STOP")
                }
            }
        }
    }
}

@Composable
private fun RatingSection(
    title: String,
    subtitle: String,
    score: Int,
    onSelect: (Int) -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title.uppercase(), color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                (1..5).forEach { value ->
                    val selected = score == value
                    Surface(
                        onClick = { onSelect(value) },
                        color = if (selected) NeonCyan else Color.White.copy(alpha = 0.04f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, if (selected) Color.Transparent else Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                value.toString(),
                                color = if (selected) Color.Black else Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishCard(
    clarityScore: Int,
    confidenceScore: Int,
    onComplete: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Rep saved.", color = SuccessGreen, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Clarity $clarityScore/5 • Confidence $confidenceScore/5",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "One honest rep every day is enough to change how you sound in interviews.",
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
        ) {
            Text("DONE", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun VoiceCheckInScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        VoiceCheckInScreen(taskName = "Mission", onComplete = {})
    }
}