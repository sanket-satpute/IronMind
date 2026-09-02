package com.sanket_satpute_20.ironmind.sleeplock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun SleepLockSetupScreen(
    onBack: () -> Unit = {},
    onSave: (bedtime: Int, wakeTime: Int) -> Unit = { _, _ -> }
) {
    var bedtimeHour by remember { mutableIntStateOf(22) }
    var wakeHour by remember { mutableIntStateOf(5) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBackground, DeepBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = ElectricViolet,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SLEEP LOCK",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Mandatory digital detox window",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Info card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "During the sleep window, your phone's screen will be locked and all apps will be blocked. This is non-negotiable.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // Bedtime selector
            TimeSelector(
                label = "BEDTIME",
                emoji = "🌙",
                hour = bedtimeHour,
                color = ElectricViolet,
                onDecrease = { if (bedtimeHour > 18) bedtimeHour-- },
                onIncrease = { if (bedtimeHour < 23) bedtimeHour++ }
            )

            Spacer(Modifier.height(20.dp))

            // Wake time selector
            TimeSelector(
                label = "WAKE TIME",
                emoji = "☀️",
                hour = wakeHour,
                color = WarningAmber,
                onDecrease = { if (wakeHour > 3) wakeHour-- },
                onIncrease = { if (wakeHour < 9) wakeHour++ }
            )

            Spacer(Modifier.height(32.dp))

            // Duration chip
            val lockDuration = if (wakeHour < bedtimeHour) (24 - bedtimeHour + wakeHour) else (wakeHour - bedtimeHour)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(ElectricViolet.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Lock Duration: ${lockDuration}h",
                        color = ElectricViolet,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onSave(bedtimeHour, wakeHour) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "ACTIVATE SLEEP LOCK",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Sleep is your competitive advantage.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TimeSelector(
    label: String,
    emoji: String,
    hour: Int,
    color: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour < 12) "AM" else "PM"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBackground, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = emoji, fontSize = 24.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = label, fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) {
                    Text("−", fontSize = 22.sp, color = color)
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${displayHour}:00",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(text = amPm, fontSize = 12.sp, color = color)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onIncrease) {
                    Text("+", fontSize = 22.sp, color = color)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SleepLockSetupScreenPreview() {
    IronMindTheme { SleepLockSetupScreen() }
}