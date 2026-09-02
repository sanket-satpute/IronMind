package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class PomodoroSummaryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val taskName = intent.getStringExtra(EXTRA_TASK_NAME).orEmpty()
        val outcome = intent.getStringExtra(EXTRA_OUTCOME).orEmpty()
        val intervalsCompleted = intent.getIntExtra(EXTRA_INTERVALS_COMPLETED, 0)
        val focusMinutes = intent.getIntExtra(EXTRA_FOCUS_MINUTES, 0)
        val breachCount = intent.getIntExtra(EXTRA_BREACH_COUNT, 0)
        val focusScore = intent.getIntExtra(EXTRA_FOCUS_SCORE, 0)
        val source = intent.getStringExtra(EXTRA_SOURCE).orEmpty()
        val windowLabel = intent.getStringExtra(EXTRA_WINDOW).orEmpty()

        setContent {
            IronMindTheme {
                PomodoroSummaryScreen(
                    taskName = taskName,
                    outcome = outcome,
                    intervalsCompleted = intervalsCompleted,
                    focusMinutes = focusMinutes,
                    breachCount = breachCount,
                    focusScore = focusScore,
                    source = source,
                    windowLabel = windowLabel,
                    onClose = {
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                                )
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        private const val EXTRA_TASK_NAME = "task_name"
        private const val EXTRA_OUTCOME = "outcome"
        private const val EXTRA_INTERVALS_COMPLETED = "intervals_completed"
        private const val EXTRA_FOCUS_MINUTES = "focus_minutes"
        private const val EXTRA_BREACH_COUNT = "breach_count"
        private const val EXTRA_FOCUS_SCORE = "focus_score"
        private const val EXTRA_SOURCE = "source"
        private const val EXTRA_WINDOW = "window"

        fun createIntent(
            context: Context,
            taskName: String,
            outcome: String,
            intervalsCompleted: Int,
            focusMinutes: Int,
            breachCount: Int,
            focusScore: Int,
            source: String = "TASK",
            windowLabel: String = ""
        ): Intent {
            return Intent(context, PomodoroSummaryActivity::class.java).apply {
                putExtra(EXTRA_TASK_NAME, taskName)
                putExtra(EXTRA_OUTCOME, outcome)
                putExtra(EXTRA_INTERVALS_COMPLETED, intervalsCompleted)
                putExtra(EXTRA_FOCUS_MINUTES, focusMinutes)
                putExtra(EXTRA_BREACH_COUNT, breachCount)
                putExtra(EXTRA_FOCUS_SCORE, focusScore)
                putExtra(EXTRA_SOURCE, source)
                putExtra(EXTRA_WINDOW, windowLabel)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
        }
    }
}

@Composable
private fun PomodoroSummaryScreen(
    taskName: String,
    outcome: String,
    intervalsCompleted: Int,
    focusMinutes: Int,
    breachCount: Int,
    focusScore: Int,
    source: String,
    windowLabel: String,
    onClose: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pomodoro_summary_motion")
    val accent = when (outcome) {
        "COMPLETE" -> SuccessGreen
        "BROKEN" -> WarningAmber
        "EMERGENCY" -> WarningAmber
        else -> NeonCyan
    }
    val outcomeLabel = when (outcome) {
        "COMPLETE" -> "SESSION COMPLETE"
        "BROKEN" -> "SESSION BROKEN"
        "EMERGENCY" -> "EMERGENCY EXIT"
        else -> "SESSION CLOSED"
    }
    var secondsLeft by remember { mutableIntStateOf(6) }
    val pulseScale by animateFloatAsState(
        targetValue = if (secondsLeft % 2 == 0) 1.02f else 1f,
        animationSpec = tween(500),
        label = "pomodoro_summary_pulse"
    )
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pomodoro_summary_badge_pulse"
    )
    val outcomeDetail = when (outcome) {
        "COMPLETE" -> "Run sealed. The chamber held to the end."
        "BROKEN" -> "Focus cracked before the window closed."
        "EMERGENCY" -> "Run terminated under emergency pressure."
        else -> "Session closed."
    }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.36f))
                ) {
                    Text(
                        text = outcomeLabel,
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier
                            .scale(badgePulse)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Text(
                    text = if (taskName.isBlank()) "FOCUS SESSION" else taskName.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    modifier = Modifier.scale(pulseScale)
                )

                if (source.isNotBlank() || windowLabel.isNotBlank()) {
                    Text(
                        text = buildString {
                            if (source.equals("HOME", ignoreCase = true)) {
                                append("HOME CHAMBER")
                            } else if (source.isNotBlank()) {
                                append("TASK CHAMBER")
                            }
                            if (windowLabel.isNotBlank()) {
                                if (isNotBlank()) append(" · ")
                                append(windowLabel)
                            }
                        },
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = outcomeDetail,
                    color = Color.White.copy(alpha = 0.74f),
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PomodoroMetricRow(
                            label = "INTERVALS CLEARED",
                            value = intervalsCompleted.toString(),
                            accent = accent
                        )
                        PomodoroMetricRow(
                            label = "FOCUSED MINUTES",
                            value = focusMinutes.toString(),
                            accent = accent
                        )
                        PomodoroMetricRow(
                            label = "BREACH ATTEMPTS",
                            value = breachCount.toString(),
                            accent = accent
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = accent.copy(alpha = 0.11f),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.32f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "FOCUS SCORE",
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = focusScore.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 52.sp
                        )
                        Text(
                            text = when {
                                focusScore >= 90 -> "Clean pressure. Keep the rhythm."
                                focusScore >= 70 -> "Solid session. Breach resistance held."
                                else -> "Focus cracked. Next round needs harder discipline."
                            },
                            color = Color.White.copy(alpha = 0.84f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUTO CLOSE",
                                color = Color.White.copy(alpha = 0.56f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Returning to the board in $secondsLeft s",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    color = accent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = secondsLeft.toString(),
                                color = accent,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "RETURN TO IRONMIND",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PomodoroMetricRow(
    label: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.56f),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.9.sp
            )
            Spacer(modifier = Modifier.height(1.dp))
        }
        Text(
            text = value,
            color = accent,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp
        )
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun PomodoroSummaryPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PomodoroSummaryPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun PomodoroSummaryPreviewContent() {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.Text(
            "⏱ SESSION COMPLETE",
            color = ErrorRed,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black
        )
        androidx.compose.material3.Text(
            "Deep Work Block",
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 22.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(8.dp))
        androidx.compose.material3.Text("4 intervals · 100 min · Focus Score: 87", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
    }
}