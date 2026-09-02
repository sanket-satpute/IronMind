package com.sanket_satpute_20.ironmind.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun DebugEventScreen(
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground) // Extremely dark terminal background
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = SuccessGreen)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Rounded.BugReport, contentDescription = null, tint = SuccessGreen)
            Spacer(Modifier.width(8.dp))
            Text(
                "SYS.DEBUG_CONSOLE",
                color = SuccessGreen,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        // Terminal Warning
        Surface(
            color = DeepBackground,
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "WARNING: DO NOT USE THESE COMMANDS UNLESS INSTRUCTED. CAN CORRUPT STREAK DATA.",
                color = Color.Red,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Commands
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                DebugCommandCard(
                    title = "TRIGGER_BEDTIME_NOW()",
                    description = "Force launches the SleepLock sequence.",
                    icon = Icons.Rounded.FastForward,
                    color = NeonCyan
                )
            }
            item {
                DebugCommandCard(
                    title = "SIMULATE_END_OF_DAY()",
                    description = "Runs the midnight cron job immediately to calculate streaks.",
                    icon = Icons.Rounded.SkipNext,
                    color = WarningAmber
                )
            }
            item {
                DebugCommandCard(
                    title = "NUKE_LOCAL_DB()",
                    description = "Wipes all tasks, history, and preferences. Cannot be undone.",
                    icon = Icons.Rounded.DeleteForever,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun DebugCommandCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        color = DeepBackground,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, color),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("EXEC", color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DebugEventScreenPreview() {
    IronMindTheme {
        DebugEventScreen()
    }
}