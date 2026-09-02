package com.sanket_satpute_20.ironmind.rewards

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun StreakCelebrationScreen(
    streakCount: Int = 0,
    onContinue: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val rotateAnim by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )

    // Milestone label
    val milestone = when {
        streakCount >= 365 -> "LEGENDARY"
        streakCount >= 100 -> "ELITE"
        streakCount >= 30  -> "IRON"
        streakCount >= 14  -> "CONSISTENT"
        streakCount >= 7   -> "BUILDING"
        else               -> "STARTED"
    }
    val milestoneColor = when {
        streakCount >= 100 -> GoldXP
        streakCount >= 30  -> ErrorRed
        else               -> WarningAmber
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBackground, DeepBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Fire emoji with animation
            Text(
                text = "🔥",
                fontSize = 80.sp,
                modifier = Modifier
                    .scale(pulseScale)
                    .rotate(rotateAnim)
            )

            Spacer(Modifier.height(20.dp))

            // Streak number
            Text(
                text = "$streakCount",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = milestoneColor
            )

            Text(
                text = "DAY STREAK",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(16.dp))

            // Milestone badge
            Box(
                modifier = Modifier
                    .background(milestoneColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = milestone,
                    color = milestoneColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Consistency is the only superpower.\nYou have it.",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = milestoneColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "KEEP THE STREAK ALIVE",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StreakCelebrationScreenPreview() {
    IronMindTheme { StreakCelebrationScreen(streakCount = 30) }
}
