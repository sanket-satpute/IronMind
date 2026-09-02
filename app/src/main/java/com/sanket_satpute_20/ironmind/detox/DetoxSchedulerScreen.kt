package com.sanket_satpute_20.ironmind.detox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhonelinkErase
import androidx.compose.material.icons.rounded.Shield
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
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

private val detoxWindows = listOf(
    "Morning Block" to "6:00 AM – 9:00 AM",
    "Deep Work" to "9:00 AM – 12:00 PM",
    "Post-Lunch" to "1:00 PM – 3:00 PM",
    "Evening Detox" to "7:00 PM – 10:00 PM",
    "Full Day Detox" to "12:00 AM – 11:59 PM"
)

@Composable
fun DetoxSchedulerScreen(
    onSave: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val enabledWindows = remember { mutableStateMapOf<String, Boolean>() }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

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
                    Icons.Rounded.PhonelinkErase,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "DETOX SCHEDULER",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("Block distractions on a schedule", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Day picker
            Text(
                "ACTIVE DAYS",
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                days.forEach { day ->
                    val isSelected = day in selectedDays
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                if (isSelected) SuccessGreen.copy(alpha = 0.2f) else DeepBackground,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                selectedDays = if (day in selectedDays)
                                    selectedDays - day
                                else
                                    selectedDays + day
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                day,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SuccessGreen else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Time windows
            Text("DETOX WINDOWS", fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))

            detoxWindows.forEach { (name, time) ->
                val enabled = enabledWindows[name] ?: false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(DeepBackground, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = if (enabled) SuccessGreen else SurfaceElevated,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(time, fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabledWindows[name] = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen,
                            uncheckedTrackColor = SurfaceElevated
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val anyEnabled = enabledWindows.values.any { it }

            Button(
                onClick = { if (anyEnabled) onSave() },
                enabled = anyEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    disabledContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("SAVE DETOX SCHEDULE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetoxSchedulerScreenPreview() {
    IronMindTheme { DetoxSchedulerScreen() }
}
