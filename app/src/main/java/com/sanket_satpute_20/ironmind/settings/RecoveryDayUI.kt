package com.sanket_satpute_20.ironmind.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.text.SimpleDateFormat
import java.util.*
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.components.PremiumPillButton

@Composable
fun RecoveryDaySettingsCard() {
    val context = LocalContext.current
    val pref = remember { PrefManager.getInstance(context) }
    
    // YYYY-MM format
    val currentMonth = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }
    
    var isUsedThisMonth by remember { mutableStateOf(pref.lastRecoveryDayMonth == currentMonth) }
    var isActive by remember { mutableStateOf(pref.isRecoveryDayActive) }

    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Healing,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "RECOVERY DAY",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Life happens. Illness, emergencies, or severe burnout. You are allowed ONE recovery day per month where all penalties, blockers, and bedtimes are paused. Use it wisely.",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isActive) {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "RECOVERY DAY IS ACTIVE TODAY",
                        color = NeonCyan,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isUsedThisMonth) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "USED THIS MONTH",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        pref.lastRecoveryDayMonth = currentMonth
                        pref.isRecoveryDayActive = true
                        pref.recoveryDayTimestamp = System.currentTimeMillis()
                        isActive = true
                        isUsedThisMonth = true
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("TRIGGER RECOVERY DAY", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun RecoveryDayOverlay(
    remainingDays: Int,
    onActivate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.padding(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.32f))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(color = NeonCyan.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
                    Text(
                        "RECOVERY PROTOCOL",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
                Icon(Icons.Rounded.Healing, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(52.dp))
                Text("Declare Recovery Day", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(
                    "You have $remainingDays recovery ${if (remainingDays == 1) "day" else "days"} remaining. Today's penalties, blockers, and streak pressure will stand down so you can reset without guilt.",
                    color = Color.White.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                PremiumPillButton(
                    label = if (remainingDays > 0) "ACTIVATE RECOVERY" else "NO DAYS REMAINING",
                    onClick = onActivate,
                    enabled = remainingDays > 0,
                    containerColor = NeonCyan,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = onDismiss) { Text("NOT TODAY", color = Color.Gray, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun RecoveryDayOverlayPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        RecoveryDayOverlay(remainingDays = 1, onActivate = {}, onDismiss = {})
    }
}

