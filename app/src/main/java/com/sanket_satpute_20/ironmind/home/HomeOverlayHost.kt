package com.sanket_satpute_20.ironmind.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sanket_satpute_20.ironmind.artifact.ArtifactUnlockOverlay
import com.sanket_satpute_20.ironmind.bossmode.BossModeScreen
import com.sanket_satpute_20.ironmind.bossmode.BossModeUpgrade
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.identity.IdentityLevelUpOverlay
import com.sanket_satpute_20.ironmind.psychology.EnergyCheckInOverlay
import com.sanket_satpute_20.ironmind.psychology.UserType
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
internal fun HomeOverlayHost(
    prefManager: PrefManager,
    userType: UserType,
    bossModeTrigger: Boolean,
    onDismissBossModeTrigger: () -> Unit,
    pendingNavigationAction: (() -> Unit)?,
    onDismissPendingNavigation: () -> Unit,
    onConfirmPendingNavigation: (() -> Unit) -> Unit,
    showEmergencyLeaveDialog: Boolean,
    emergencyLeaveReason: String,
    onEmergencyLeaveReasonChange: (String) -> Unit,
    onDismissEmergencyLeaveDialog: () -> Unit,
    onConfirmEmergencyLeave: () -> Unit,
    showEnergyCheckIn: Boolean,
    onEnergyCheckInComplete: () -> Unit,
    levelUpEvent: Int?,
    onDismissLevelUp: () -> Unit,
    newArtifactEvent: String?,
    onDismissArtifact: () -> Unit,
    bossModeEvent: BossModeUpgrade?,
    onDismissBossModeUpgrade: () -> Unit
) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) return

    // Priority-ordered overlay: show only the highest-priority active overlay at a time.
    // When dismissed, the next one auto-shows.
    val activeOverlay = when {
        pendingNavigationAction != null -> "pending_nav"
        showEmergencyLeaveDialog -> "emergency_leave"
        showEnergyCheckIn -> "energy_checkin"
        bossModeTrigger -> "boss_trigger"
        levelUpEvent != null -> "level_up"
        newArtifactEvent != null -> "artifact"
        bossModeEvent != null -> "boss_upgrade"
        else -> null
    }

    if (activeOverlay == "boss_trigger") {
        AlertDialog(
            onDismissRequest = onDismissBossModeTrigger,
            containerColor = DeepBackground,
            title = {
                Text(
                    "BOSS MODE ACTIVATED",
                    color = ErrorRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    "Perfect week detected. Your tasks are now 15% harder. Keep pushing your edge.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissBossModeTrigger) {
                    Text("UNDERSTOOD", color = ErrorRed)
                }
            }
        )
    }

    if (activeOverlay == "pending_nav") {
        val action = pendingNavigationAction!!
        AlertDialog(
            onDismissRequest = onDismissPendingNavigation,
            containerColor = SurfaceLighter,
            title = { Text("ACTIVE FOCUS SESSION", fontWeight = FontWeight.Black, color = WarningAmber) },
            text = {
                Text(
                    "You are currently in a focus session. Leaving now will interrupt your current focus. Leave anyway?",
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmPendingNavigation(action) },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                ) {
                    Text("LEAVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPendingNavigation) {
                    Text("STAY FOCUSED", color = Color.White)
                }
            }
        )
    }

    if (activeOverlay == "emergency_leave") {
        AlertDialog(
            onDismissRequest = onDismissEmergencyLeaveDialog,
            containerColor = SurfaceLighter,
            title = {
                Text("EMERGENCY LEAVE TOMORROW", fontWeight = FontWeight.Black, color = WarningAmber)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "This will clear tomorrow's missions, mark the day as emergency leave, and deduct ${com.sanket_satpute_20.ironmind.nightdecision.NightDecisionManager.EMERGENCY_LEAVE_XP_COST} XP.",
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = emergencyLeaveReason,
                        onValueChange = { onEmergencyLeaveReasonChange(it.take(120)) },
                        placeholder = { Text("Reason (optional)") },
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarningAmber,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmEmergencyLeave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningAmber,
                        contentColor = Color.Black
                    )
                ) {
                    Text("TAKE LEAVE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEmergencyLeaveDialog) {
                    Text("CANCEL", color = Color.White)
                }
            }
        )
    }

    if (activeOverlay == "energy_checkin") {
        EnergyCheckInOverlay(
            prefs = prefManager,
            onComplete = onEnergyCheckInComplete
        )
    }

    if (activeOverlay == "level_up") {
        val newLevel = levelUpEvent!!
        IdentityLevelUpOverlay(
            newLevel = newLevel,
            userType = userType,
            onDismiss = onDismissLevelUp
        )
    }

    if (activeOverlay == "artifact") {
        val artifactId = newArtifactEvent!!
        ArtifactUnlockOverlay(
            artifactId = artifactId,
            onDismiss = onDismissArtifact
        )
    }

    if (activeOverlay == "boss_upgrade") {
        val upgrade = bossModeEvent!!
        Dialog(
            onDismissRequest = onDismissBossModeUpgrade,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BossModeScreen(upgrade = upgrade, onAccept = onDismissBossModeUpgrade)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun HomeOverlayHostPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        AlertDialog(
            onDismissRequest = {},
            containerColor = DeepBackground,
            title = { Text("ACTIVE FOCUS SESSION", fontWeight = FontWeight.Black, color = WarningAmber) },
            text = { Text("Leaving now will interrupt your focus session. Leave anyway?", color = Color.Gray) },
            confirmButton = {
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)) {
                    Text("LEAVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = {}) { Text("STAY FOCUSED", color = Color.White) } }
        )
    }
}
