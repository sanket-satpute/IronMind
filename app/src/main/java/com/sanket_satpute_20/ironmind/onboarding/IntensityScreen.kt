package com.sanket_satpute_20.ironmind.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private val SuccessGreen = SurfaceElevated
private val ErrorRed = ErrorRed
private val WarningAmber = WarningAmber
private val SurfaceLighter = SurfaceElevated

@Composable
fun IntensityScreen(onNext: (String) -> Unit = {}, onBack: () -> Unit = {}) {
    var showWarning by remember { mutableStateOf(false) }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            containerColor = DeepBackground,
            title = {
                Text("⚠️ HARD MODE", fontWeight = FontWeight.Black, color = ErrorRed, letterSpacing = 1.sp)
            },
            text = {
                Text(
                    "Hard mode is designed to be uncomfortable. \n\n" +
                    "• 5-Minute Locked Breathers\n" +
                    "• Zero-Tolerance App Blocking\n" +
                    "• Accountability SMS to your partner\n\n" +
                    "Choose it only if you want the strongest pressure from the system.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { onNext("HARD") },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CHOOSE HARD", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarning = false }) {
                    Text("NOT NOW", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(56.dp))
                Text(
                    "CALIBRATE INTENSITY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = WarningAmber
                )
                Text(
                    "Choose how strict IronMind should be.",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "This is your final setup step. It controls how strict the focus guard and skip response should feel.",
                    fontSize = 14.sp, color = Color.Gray, lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IntensityPowerCard(
                icon = Icons.Rounded.Shield,
                title = "LOW",
                description = "Gentle nudges. 1-minute reset. Best if you want support without much pressure.",
                color = SuccessGreen,
                onClick = { onNext("LOW") }
            )

            IntensityPowerCard(
                icon = Icons.Rounded.Bolt,
                title = "MEDIUM",
                description = "Balanced rules. 3-minute lockout. Strong enough for most people.",
                color = WarningAmber,
                onClick = { onNext("MEDIUM") }
            )

            IntensityPowerCard(
                icon = Icons.Rounded.Warning,
                title = "HARD",
                description = "Maximum pressure. 5-minute lockout. Best if you want the system to push back hard.",
                color = ErrorRed,
                onClick = { showWarning = true }
            )

            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "You can change this later, but your streak will reset.",
                fontSize = 11.sp,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IntensityPowerCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = SurfaceLighter,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = color
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun IntensityScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        IntensityScreen(onNext = {}, onBack = {})
    }
}