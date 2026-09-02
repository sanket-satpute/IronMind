package com.sanket_satpute_20.ironmind.psychology

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun EnergyCheckInOverlay(
    prefs: PrefManager,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var energyScore by remember { mutableStateOf(3f) }
    val recommendedMode = AdaptiveEngine.recommendModeBasedOnEnergy(energyScore.toInt())
    val userName = prefs.userName.ifEmpty { "Soldier" }

    Dialog(
        onDismissRequest = { /* No dismiss allowed - must check in */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "MORNING READINESS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "How is your battery today, $userName?",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // --- BATTERY SLIDER ---
                BatterySlider(
                    value = energyScore,
                    onValueChange = { energyScore = it }
                )

                Spacer(modifier = Modifier.height(48.dp))

                // --- RECOMMENDATION CARD ---
                RecommendationCard(recommendedMode)

                Spacer(modifier = Modifier.height(64.dp))

                Button(
                    onClick = {
                        val previousMode = prefs.appMode
                        prefs.lastEnergyScore = energyScore.toInt()
                        prefs.lastCheckinDate = LocalDate.now().toString()
                        HistoryRecorder.recordDailyCheckIn(
                            context = context,
                            energyScore = energyScore.toInt(),
                            stressScore = prefs.lastStressScore,
                            moodWord = prefs.lastMoodWord,
                            source = "ENERGY_CHECK_IN"
                        )
                        AdaptiveEngine.switchMode(
                            context = context,
                            prefs = prefs,
                            newMode = recommendedMode,
                            previousMode = previousMode,
                            source = "ENERGY_CHECK_IN"
                        )
                        onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(recommendedMode.color)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "SET MODE: ${recommendedMode.label.uppercase()}",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BatterySlider(value: Float, onValueChange: (Float) -> Unit) {
    val color by animateColorAsState(
        targetValue = when {
            value <= 2 -> ErrorRed
            value <= 3 -> WarningAmber
            else -> SurfaceElevated
        }
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .padding(8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Fill level
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value / 5f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(color, color.copy(alpha = 0.6f))
                        )
                    )
            )
            
            Icon(
                Icons.Rounded.Bolt,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp).align(Alignment.Center)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = SurfaceElevated
            ),
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
fun RecommendationCard(mode: AppMode) {
    Surface(
        color = Color(mode.color).copy(alpha = 0.1f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(mode.color).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(mode.emoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    "SUGGESTED: ${mode.label}",
                    fontWeight = FontWeight.Black,
                    color = Color(mode.color),
                    fontSize = 14.sp
                )
                Text(
                    mode.description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun EnergyCheckInOverlayPreview() {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .background(DeepBackground),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "⚡ Energy Check-In",
                    color = GoldXP,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}