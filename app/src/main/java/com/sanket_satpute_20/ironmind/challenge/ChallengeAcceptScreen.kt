package com.sanket_satpute_20.ironmind.challenge

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan

@Composable
fun ChallengeAcceptScreen(
    challengeId: String = "75_hard",
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBackground, SurfaceDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Cinematic Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(NeonCyan.copy(alpha = 0.1f), CircleShape)
                    .border(2.dp, NeonCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.SelfImprovement,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "PROJECT 75",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "A 75-day tactical mental toughness program. Not a fitness program. A transformative challenge.",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(48.dp))

            // Rules List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "THE RULES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 2.sp
                )
                
                ChallengeRuleRow(icon = Icons.Rounded.DirectionsRun, text = "Two 45-minute workouts (one outdoors)")
                ChallengeRuleRow(icon = Icons.Rounded.Check, text = "Follow a diet. No cheat meals. No alcohol.")
                ChallengeRuleRow(icon = Icons.Rounded.Check, text = "Drink 1 gallon of water.")
                ChallengeRuleRow(icon = Icons.Rounded.Check, text = "Read 10 pages of non-fiction.")
                ChallengeRuleRow(icon = Icons.Rounded.Timer, text = "If you miss ANY rule, you restart at Day 1.")
            }

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("COWER", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ACCEPT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChallengeRuleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ChallengeAcceptScreenPreview() {
    IronMindTheme { ChallengeAcceptScreen() }
}