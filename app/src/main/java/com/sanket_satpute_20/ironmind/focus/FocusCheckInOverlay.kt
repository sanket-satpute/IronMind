package com.sanket_satpute_20.ironmind.focus

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.components.ProgressRing

@Composable
fun FocusCheckInOverlay(
    taskName: String,
    onScoreSelected: (Int) -> Unit,
    onTimeout: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(10) }
    var selectedScore by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && selectedScore == 0) {
            delay(1000L)
            timeLeft--
        }
        if (selectedScore == 0) onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground.copy(alpha = 0.93f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🧠", fontSize = 48.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "How focused were you?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                taskName,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Score labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Distracted", fontSize = 11.sp, color = Color.Gray)
                Text("Locked In", fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5 score buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (1..5).forEach { score ->
                    ScoreButton(
                        score = score,
                        isSelected = selectedScore == score,
                        onClick = {
                            selectedScore = score
                            onScoreSelected(score)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Score description
            if (selectedScore > 0) {
                Text(
                    getFocusLabel(selectedScore),
                    fontSize = 15.sp,
                    color = getFocusColor(selectedScore),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            } else {
                // Countdown
                Text(
                    "Auto-skipping in $timeLeft...",
                    fontSize = 13.sp,
                    color = SurfaceElevated
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar draining as time runs out
            if (selectedScore == 0) {
                ProgressRing(
                    progress = timeLeft / 10f,
                    size = 42.dp,
                    strokeWidth = 4.dp,
                    progressColor = WarningAmber,
                    trackColor = SurfaceElevated,
                    label = timeLeft.toString()
                )
            }
        }
    }
}

@Composable
fun ScoreButton(score: Int, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val bgColor = if (isSelected) getFocusColor(score) else SurfaceDark
    val textColor = if (isSelected) Color.Black else Color.Gray

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .background(bgColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$score",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}

fun getFocusColor(score: Int): Color = when (score) {
    1 -> ErrorRed
    2 -> WarningAmber
    3 -> GoldXP
    4 -> WarningAmber
    5 -> SuccessGreen
    else -> Color.Gray
}

fun getFocusLabel(score: Int): String = when (score) {
    1 -> "Completely distracted — barely showed up"
    2 -> "Mostly distracted — kept losing focus"
    3 -> "Mixed — some good moments, some drift"
    4 -> "Mostly sharp — minor distractions"
    5 -> "Locked in — pure, unbroken focus"
    else -> ""
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun FocusCheckInOverlayPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        FocusCheckInOverlay(
            taskName = "Deep Work Session",
            onScoreSelected = {},
            onTimeout = {}
        )
    }
}
