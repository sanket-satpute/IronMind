package com.sanket_satpute_20.ironmind.crucible

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun CrucibleScreen(
    crucibleId: String = "hard_mode",
    onAbort: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    var showAbortWarning by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
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
                    "ACTIVE CRUCIBLE",
                    color = ErrorRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Phase 1: Endure",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            // Central pulsing timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale)
                        .background(ErrorRed.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, ErrorRed.copy(alpha = 0.2f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(DeepBackground, CircleShape)
                        .border(2.dp, ErrorRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "01:45:22",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "REMAINING",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showAbortWarning) {
                    Text(
                        "WARNING: Aborting now will result in severe streak penalties and a permanent mark on your history.",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { showAbortWarning = false },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                        ) {
                            Text("STAY", color = Color.White)
                        }
                        Button(
                            onClick = onAbort,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("ABORT", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showAbortWarning = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                    ) {
                        Text("ABORT CRUCIBLE", letterSpacing = 2.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onComplete) {
                    Text("[DEV] Trigger Complete", color = Color.DarkGray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrucibleScreenPreview() {
    IronMindTheme { CrucibleScreen() }
}