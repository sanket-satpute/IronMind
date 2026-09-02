package com.sanket_satpute_20.ironmind.psychology

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun ModeSwitcherScreen(onSaved: () -> Unit = {}, onCancel: () -> Unit = {}) {
    val context     = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    val currentMode = AdaptiveEngine.getCurrentMode(prefs)
    var selectedMode by remember { mutableStateOf(currentMode) }
    var showWarning by remember { mutableStateOf(false) }
    var showIronCooldownWarning by remember { mutableStateOf(false) }

    // Check if Iron Mode cooldown is active (24-hour reflection period)
    val ironCooldownActive = remember {
        val lastSwitch = prefs.lastModeSwitchTimestamp
        lastSwitch > 0 && System.currentTimeMillis() - lastSwitch < 24 * 60 * 60 * 1000L
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        Text("Select Your App Mode", fontSize = 26.sp,
            fontWeight = FontWeight.Black, color = Color.White)
        Text(
            "Each mode changes the app rules and consequences.",
            fontSize = 14.sp, color = Color.Gray, lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Mode selection cards
        AppMode.values().forEach { mode ->
            ModeCard(mode, selectedMode, onClick = { selectedMode = it })
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedMode == AppMode.IRON && currentMode != AppMode.IRON && ironCooldownActive) {
                    showIronCooldownWarning = true
                } else {
                    showWarning = true
                }
            },
            enabled = selectedMode != currentMode,
            colors  = ButtonDefaults.buttonColors(
                containerColor = if (selectedMode != currentMode)
                    Color(selectedMode.color) else SurfaceDark,
                disabledContainerColor = SurfaceDark
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Text(
                (if (selectedMode != currentMode)
                    "Switch to ${selectedMode.label} →"
                else
                    "Select a different mode to save") as String,
                fontWeight = FontWeight.Black,
                color      = if (selectedMode != currentMode)
                    Color.Black else SurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Final confirmation dialog
    if (showWarning) {
        val currentPunishment = AdaptiveEngine.getPunishmentDurationSeconds(currentMode)
        val newPunishment     = AdaptiveEngine.getPunishmentDurationSeconds(selectedMode)

        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(selectedMode.color).copy(alpha = 0.1f),
            title = {
                Text("⚠️ Confirm Switch", color = Color(selectedMode.color),
                    fontWeight = FontWeight.Black)
            },
            text = {
                Column {
                    Text(
                        "You are switching from ${currentMode.label} to ${selectedMode.label}.",
                        color = Color.White, lineHeight = 24.sp, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Punishment for skipping will change from " +
                        "${formatSeconds(currentPunishment)} to ${formatSeconds(newPunishment)}.",
                        color = Color.Gray, lineHeight = 22.sp, fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        prefs.lastModeSwitchTimestamp = System.currentTimeMillis()
                        AdaptiveEngine.switchMode(
                            prefs = prefs,
                            newMode = selectedMode,
                            isManual = true,
                            context = context,
                            previousMode = currentMode.name,
                            source = "MODE_SWITCHER"
                        )
                        onSaved()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(selectedMode.color)
                    )
                ) {
                    Text("Switch to ${selectedMode.label}", fontWeight = FontWeight.Bold,
                        color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarning = false; selectedMode = currentMode }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Iron Mode cooldown warning — prevents impulsive upgrade
    if (showIronCooldownWarning) {
        AlertDialog(
            onDismissRequest = { showIronCooldownWarning = false },
            containerColor = DeepBackground,
            title = {
                Text("🛑 Reflection Period Active", color = ErrorRed,
                    fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "Iron Mode is the strictest level — it increases punishment severity and lowers tolerance for breaches.\n\n" +
                    "A 24-hour reflection period is required before upgrading to Iron Mode. " +
                    "This prevents impulsive mode changes you may regret.\n\n" +
                    "Come back tomorrow if you still want Iron Mode.",
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 22.sp,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showIronCooldownWarning = false; selectedMode = currentMode }) {
                    Text("I understand", color = ErrorRed)
                }
            }
        )
    }
}

@Composable
fun ModeCard(mode: AppMode, selected: AppMode, onClick: (AppMode) -> Unit) {
    val isSelected = mode == selected
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Surface(
        color = if (isSelected) Color(mode.color).copy(alpha = 0.1f) else DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(mode.color) else SurfaceDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .gamifiedClick { onClick(mode) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(mode.emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mode.label, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(mode.color) else Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    mode.description,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.75f)
                            else SurfaceElevated,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

fun formatSeconds(seconds: Long): String {
    return when {
        seconds == 0L -> "0 seconds (no punishment)"
        seconds < 60 -> "$seconds seconds"
        else -> "${seconds / 60} minutes"
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ModeSwitcherScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ModeSwitcherScreen(onSaved = {}, onCancel = {})
    }
}