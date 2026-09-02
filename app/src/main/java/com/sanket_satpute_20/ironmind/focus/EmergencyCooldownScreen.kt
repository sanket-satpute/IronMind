package com.sanket_satpute_20.ironmind.focus

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan

@Composable
fun EmergencyCooldownScreen(
    mode: String = "reset",
    onReturnToMission: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "breath"
    )

    var secondsRemaining by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground) // Deep calm blue/black
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "EMERGENCY COOLDOWN",
                    color = NeonCyan,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Do not act on impulse.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }

            // Breathing visualizer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(breathScale)
                        .background(NeonCyan.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), CircleShape)
                )
                Text(
                    if (breathScale > 1.0f) "INHALE" else "EXHALE",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "00:${secondsRemaining.toString().padStart(2, '0')}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(Modifier.height(24.dp))
                
                if (secondsRemaining == 0) {
                    Button(
                        onClick = onReturnToMission,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("RETURN TO MISSION", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onBackToHome,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("ABORT EVERYTHING", letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmergencyCooldownScreenPreview() {
    IronMindTheme { EmergencyCooldownScreen() }
}