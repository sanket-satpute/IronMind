package com.sanket_satpute_20.ironmind.autopsy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun MonthlyAuditScreen(onBack: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack) { Text("← Back", color = Color.Gray) }
            
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Assessment,
                    contentDescription = null,
                    tint = ElectricViolet,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "MONTHLY AUDIT",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("Brutal honesty. Real metrics.", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Main stats grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AuditStatCard(
                    modifier = Modifier.weight(1f),
                    label = "COMPLETION",
                    value = "87%",
                    trend = "+4%",
                    color = SuccessGreen
                )
                AuditStatCard(
                    modifier = Modifier.weight(1f),
                    label = "SKIPS",
                    value = "12",
                    trend = "-3",
                    color = ErrorRed
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("CONSISTENCY HEATMAP", fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(12.dp))

            // Fake heatmap UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { week ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(7) { day ->
                                val active = (Math.random() > 0.3)
                                val intensity = if (active) (0.4f + Math.random() * 0.6f).toFloat() else 0.1f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(
                                            if (active) SuccessGreen.copy(alpha = intensity)
                                            else SurfaceElevated,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Highlights
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("BIGGEST WIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("14-Day Perfect Streak", color = Color.White, fontSize = 13.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("BIGGEST FAILURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("Missed morning workout 3x", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("CLOSE AUDIT", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun AuditStatCard(modifier: Modifier = Modifier, label: String, value: String, trend: String, color: Color) {
    Box(
        modifier = modifier
            .background(DeepBackground, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("$trend vs last month", fontSize = 11.sp, color = color)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthlyAuditScreenPreview() {
    IronMindTheme { MonthlyAuditScreen() }
}
