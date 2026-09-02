package com.sanket_satpute_20.ironmind.health

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet

@Composable
fun HealthDashboardScreen(
    onBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground) // Cyberpunk teal-dark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack) { Text("← Disconnect", color = NeonCyan.copy(alpha = 0.7f)) }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.MonitorHeart,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "BIOMETRICS",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("System readiness & physical capacity.", fontSize = 13.sp, color = NeonCyan.copy(alpha = 0.7f))
                }
            }

            Spacer(Modifier.height(36.dp))

            // Main Readiness Score
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(20.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 0.84f },
                            modifier = Modifier.size(160.dp),
                            color = NeonCyan,
                            trackColor = DeepBackground,
                            strokeWidth = 12.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("84%", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                            Text("READINESS", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Bedtime,
                    title = "SLEEP SCORE",
                    value = "7 Hrs 12 Min",
                    status = "OPTIMAL",
                    color = ElectricViolet
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Favorite,
                    title = "HRV",
                    value = "62 ms",
                    status = "STABLE",
                    color = NeonCyan
                )
            }

            Spacer(Modifier.height(32.dp))
            
            Text("DATA SOURCE: DEVICE SENSORS (MOCKED)", fontSize = 10.sp, color = Color.DarkGray, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun MetricCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, status: String, color: Color) {
    Box(
        modifier = modifier
            .background(DeepBackground, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(status, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthDashboardScreenPreview() {
    IronMindTheme { HealthDashboardScreen() }
}