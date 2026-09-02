package com.sanket_satpute_20.ironmind.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@Composable
fun SpringControlScreen(
    onBack: () -> Unit = {},
    onOpenMonthlyAudit: () -> Unit = {},
    onOpenTemptationLog: () -> Unit = {}
) {
    var tensionLevel by remember { mutableFloatStateOf(0.7f) }
    var strictMode by remember { mutableStateOf(true) }

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
            TextButton(onClick = onBack) { Text("← Dashboard", color = Color.Gray) }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Build,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "SPRING CONTROL",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("Adjust system tension & strictness.", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(36.dp))

            // Tension Slider Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Speed, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SYSTEM TENSION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningAmber, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(24.dp))
                    Slider(
                        value = tensionLevel,
                        onValueChange = { tensionLevel = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = WarningAmber,
                            activeTrackColor = WarningAmber,
                            inactiveTrackColor = SurfaceElevated
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lenient", color = Color.Gray, fontSize = 11.sp)
                        Text("${(tensionLevel * 100).toInt()}%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Brutal", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Strict Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("App Blocking Strict Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Prevents easy unblocking", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Switch(
                    checked = strictMode,
                    onCheckedChange = { strictMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WarningAmber,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }

            Spacer(Modifier.height(36.dp))
            
            Text("QUICK ACTIONS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onOpenTemptationLog,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Temptation\nLog", textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 16.sp)
                }
                
                OutlinedButton(
                    onClick = onOpenMonthlyAudit,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Monthly\nAudit", textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpringControlScreenPreview() {
    IronMindTheme { SpringControlScreen() }
}