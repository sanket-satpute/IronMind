package com.sanket_satpute_20.ironmind.integrity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.focus.EmergencyValveScreen
import kotlinx.coroutines.launch
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class NeutralBlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        val prefs = PrefManager.getInstance(this)
        
        val blockedAppName = runCatching {
            val info = packageManager.getApplicationInfo(blockedPackage, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault("Distraction")

        onBackPressedDispatcher.addCallback(this) {
            goHome()
        }

        setContent {
            IronMindTheme {
                var showEmergency by remember { mutableStateOf(false) }
                var showRecoveryDialog by remember { mutableStateOf(false) }
                if (showEmergency) {
                    EmergencyValveScreen(
                        onDismiss = { showEmergency = false },
                        onFiveMinuteReset = { goHome() },
                        onDecisionDelay = { goHome() }
                    )
                } else {
                    NeutralBlockScreen(
                        blockedAppName = blockedAppName,
                        onResumeFocus = { goHome() },
                        onPlannedBreak = { startBreakAndGoHome() },
                        onRecovery = { showRecoveryDialog = true },
                        onEmergency = { showEmergency = true }
                    )
                }

                if (showRecoveryDialog) {
                    AlertDialog(
                        onDismissRequest = { showRecoveryDialog = false },
                        title = { Text("Declare Recovery Day?", color = Color.White) },
                        text = { Text("Activating this will disable all penalties and streak breaks for today. Take the day to rest guilt-free.", color = Color.LightGray) },
                        confirmButton = {
                            TextButton(onClick = {
                                if (prefs.recoveryDaysBudget > 0) {
                                    prefs.recoveryDaysBudget -= 1
                                    prefs.isRecoveryDayActive = true
                                }
                                showRecoveryDialog = false
                                goHome()
                            }) {
                                Text("ACTIVATE", color = SuccessGreen)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRecoveryDialog = false }) {
                                Text("CANCEL", color = Color.Gray)
                            }
                        },
                        containerColor = SurfaceElevated,
                        titleContentColor = Color.White,
                        textContentColor = Color.LightGray
                    )
                }
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    private fun startBreakAndGoHome() {
        // Here we could directly launch a Pomodoro chamber break, but for now we just 
        // redirect them home so they can manage their breaks appropriately.
        // Or if Pomodoro is active, maybe trigger its break phase?
        // Simple fallback for Phase 2: send them to the main app interface.
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "BLOCKED_PACKAGE"
    }
}

@Composable
fun NeutralBlockScreen(
    blockedAppName: String,
    onResumeFocus: () -> Unit,
    onPlannedBreak: () -> Unit,
    onRecovery: () -> Unit,
    onEmergency: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Access Blocked",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$blockedAppName is restricted during an active focus session.",
                color = Color.LightGray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            BlockOptionButton(
                title = "Resume Focus",
                subtitle = "Close this app and continue working.",
                icon = Icons.Rounded.PlayArrow,
                color = SuccessGreen,
                onClick = onResumeFocus
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BlockOptionButton(
                title = "Planned Break",
                subtitle = "Step away for 5-15 minutes.",
                icon = Icons.Rounded.Coffee,
                color = WarningAmber,
                onClick = onPlannedBreak
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BlockOptionButton(
                title = "Recovery Adjustment",
                subtitle = "Declare today a recovery day to disable tracking.",
                icon = Icons.Rounded.Healing,
                color = ElectricViolet,
                onClick = onRecovery
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BlockOptionButton(
                title = "Emergency Exit",
                subtitle = "Trigger the emergency valve cooldown.",
                icon = Icons.Rounded.Warning,
                color = ErrorRed,
                onClick = onEmergency
            )
        }
    }
}

@Composable
fun BlockOptionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun NeutralBlockScreenPreview() {
    IronMindTheme {
        NeutralBlockScreen(
            blockedAppName = "Instagram",
            onResumeFocus = {}, onPlannedBreak = {}, onRecovery = {}, onEmergency = {}
        )
    }
}
