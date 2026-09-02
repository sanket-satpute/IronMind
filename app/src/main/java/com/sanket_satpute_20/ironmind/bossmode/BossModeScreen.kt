package com.sanket_satpute_20.ironmind.bossmode

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Warning
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
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun BossModeScreen(
    upgrade: BossModeUpgrade = BossModeUpgrade("Strict Device Lock", "10:00", "11:00", 3600, 1),
    onAccept: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    var sliderValue by remember { mutableStateOf(0f) }
    val isConfirmed = sliderValue > 0.9f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground) // Deep red-black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                TextButton(onClick = onBack) {
                    Text("← Retreat", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Warning Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulse)
                    .background(ErrorRed.copy(alpha = 0.15f), RoundedCornerShape(40.dp))
                    .border(2.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PriorityHigh, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "BOSS MODE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ErrorRed,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "This is the point of no return. Boss Mode fundamentally changes how IronMind operates. Once activated, you cannot easily disable it.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(48.dp))

            // Warning Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(16.dp))
                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PENALTIES & RESTRICTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("• ${upgrade.upgradedTaskName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("No app uninstalls allowed. Ever.", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("• Severe Streak Penalties", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Missing a task costs 3x the streak days.", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("• Forced Autopsies", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("You cannot use your phone until you write a 50-word failure explanation.", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                "SLIDE TO ACTIVATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(16.dp))

            // Custom Slider implementation
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = SliderDefaults.colors(
                    thumbColor = ErrorRed,
                    activeTrackColor = ErrorRed,
                    inactiveTrackColor = DeepBackground
                )
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAccept,
                enabled = isConfirmed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    disabledContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isConfirmed) "ACTIVATE BOSS MODE" else "CONFIRM INTENT FIRST",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 14.sp,
                    color = if (isConfirmed) Color.White else Color.Gray
                )
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BossModeScreenPreview() {
    IronMindTheme { BossModeScreen() }
}