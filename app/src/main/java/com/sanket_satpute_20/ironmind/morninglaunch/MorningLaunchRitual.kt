package com.sanket_satpute_20.ironmind.morninglaunch

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.components.HapticPattern
import com.sanket_satpute_20.ironmind.ui.components.IntegrityShield
import com.sanket_satpute_20.ironmind.ui.components.ShieldEvent
import com.sanket_satpute_20.ironmind.ui.components.ShieldMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground

@Composable
fun MorningLaunchRecapScreen(
    yesterdayFillLevel: Float,
    yesterdayCracks: Int,
    mode: ShieldMode,
    onContinue: () -> Unit
) {
    var event by remember { mutableStateOf<ShieldEvent?>(null) }
    var shieldReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        shieldReady = true
        // The cracks render deterministically based on yesterdayCracks. 
        // We can just show them instantly.
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "YESTERDAY'S SHIELD",
            color = mode.primary,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
        
        Spacer(Modifier.height(32.dp))

        if (shieldReady) {
            IntegrityShield(
                integrityLevel = yesterdayFillLevel,
                mode = mode,
                event = event,
                onEventHandled = { event = null },
                modifier = Modifier.size(240.dp),
                initialCrackCount = yesterdayCracks
            )
        } else {
            Spacer(Modifier.size(240.dp))
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text = if (yesterdayCracks == 0 && yesterdayFillLevel > 0.8f) "Flawless" else if (yesterdayCracks > 3) "Fractured but Held" else "Stable",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = mode.secondary)
        ) {
            Text("PROCEED TO TODAY", color = Color.White)
        }
    }
}

@Composable
fun MorningLaunchOathScreen(
    mode: ShieldMode,
    level: Int,
    onOathSworn: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.sanket_satpute_20.ironmind.data.PrefManager.getInstance(context) }
    val reduceMotion = prefs.reduceMotionEnabled

    var holdProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isHolding by remember { mutableStateOf(false) }
    
    val isHighLevel = level >= 10
    var typedOath by remember { mutableStateOf("") }
    val requiredOath = "I commit to my tasks. I accept the consequences of failure."
    val isMatch = typedOath.trim().equals(requiredOath.trim(), ignoreCase = true)

    LaunchedEffect(isHolding) {
        if (isHolding) {
            if (reduceMotion) {
                // Instantly complete for reduced motion users
                holdProgress.snapTo(1f)
                onOathSworn()
            } else {
                holdProgress.animateTo(1f, tween(1500))
                if (holdProgress.value == 1f) {
                    onOathSworn()
                }
            }
        } else {
            if (!reduceMotion) {
                holdProgress.animateTo(0f, tween(300))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TODAY'S OATH",
            color = mode.primary,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
        
        Spacer(Modifier.height(32.dp))

        Text(
            text = "I commit to my tasks.\nI accept the consequences of failure.",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(64.dp))

        if (isHighLevel) {
            Text(
                text = "TYPE THE OATH EXACTLY",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = typedOath,
                onValueChange = { typedOath = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = mode.primary,
                    unfocusedIndicatorColor = Color.DarkGray
                ),
                placeholder = { Text("Type here...", color = Color.DarkGray) }
            )
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onOathSworn,
                enabled = isMatch,
                colors = ButtonDefaults.buttonColors(containerColor = mode.secondary),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("SWEAR OATH", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            if (reduceMotion) {
                // Static button for reduced motion users
                Button(
                    onClick = onOathSworn,
                    colors = ButtonDefaults.buttonColors(containerColor = mode.primary.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth(0.8f).height(64.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                ) {
                    Text("CONFIRM OATH", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(mode.secondary.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isHolding = true
                                    tryAwaitRelease()
                                    isHolding = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { holdProgress.value },
                        modifier = Modifier.fillMaxSize(),
                        color = mode.primary,
                        strokeWidth = 8.dp,
                        trackColor = Color.Transparent
                    )
                    Text("HOLD", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MorningLaunchModeConfirmScreen(
    currentMode: ShieldMode,
    recommendationReason: String? = null,
    onModeConfirmed: (ShieldMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    
    // We simulate the "temperature dial" by just showing a picker for now. 
    // In a full implementation this would be a custom draggable dial.
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CONFIRM OPERATING MODE",
            color = selectedMode.primary,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
        
        if (recommendationReason != null) {
            Spacer(Modifier.height(16.dp))
            Surface(
                color = selectedMode.primary.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedMode.primary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = recommendationReason,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShieldMode.entries.forEach { mode ->
                Button(
                    onClick = { selectedMode = mode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == mode) mode.primary else Color.DarkGray
                    )
                ) {
                    Text(mode.name, color = Color.White)
                }
            }
        }
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = { onModeConfirmed(selectedMode) },
            colors = ButtonDefaults.buttonColors(containerColor = selectedMode.secondary)
        ) {
            Text("UNLOCK", color = Color.White)
        }
    }
}

@Composable
fun MorningLaunchTaskCommitmentScreen(
    mode: ShieldMode,
    tasks: List<com.sanket_satpute_20.ironmind.data.Task>,
    selectedTaskIds: List<Long>,
    onTaskSelected: (Long) -> Unit,
    onCommit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TODAY'S COMMITMENTS",
            color = mode.primary,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Select up to 3 priority tasks.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(32.dp))
        
        if (tasks.isEmpty()) {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            ) {
                Text(
                    text = "No tasks found for today.\nYou can add tasks later from the Home screen.",
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                    fontSize = 14.sp
                )
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    val isSelected = selectedTaskIds.contains(task.id.toLong())
                    Surface(
                        color = if (isSelected) mode.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) mode.primary else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().gamifiedClick { onTaskSelected(task.id.toLong()) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onTaskSelected(task.id.toLong()) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = mode.primary,
                                    unselectedColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = task.name.ifEmpty { "Unnamed Task" },
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = onCommit,
            colors = ButtonDefaults.buttonColors(containerColor = mode.secondary),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = if (selectedTaskIds.isEmpty()) "CONTINUE WITHOUT TASKS" else "COMMIT TO ${selectedTaskIds.size} TASKS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun MorningLaunchTaskCommitmentScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        MorningLaunchTaskCommitmentScreen(
            mode = com.sanket_satpute_20.ironmind.ui.components.ShieldMode.IRON,
            tasks = listOf(
                com.sanket_satpute_20.ironmind.data.Task(id = 1, name = "Deep Work Session"),
                com.sanket_satpute_20.ironmind.data.Task(id = 2, name = "Workout"),
                com.sanket_satpute_20.ironmind.data.Task(id = 3, name = "Read 10 pages")
            ),
            selectedTaskIds = listOf(1L),
            onTaskSelected = {},
            onCommit = {}
        )
    }
}
