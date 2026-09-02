package com.sanket_satpute_20.ironmind.punishment

import android.app.NotificationManager
import android.content.*
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppCopy
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.settings.getDefaultSmsPackage
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class PunishmentActivity : ComponentActivity() {

    companion object {
        var isInForeground = false
        var emergencyAppLaunching = false
    }

    private var notifManager: NotificationManager? = null
    private var previousDndMode = NotificationManager.INTERRUPTION_FILTER_ALL
    private var punishmentEndedReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PrefManager.getInstance(this)
        val currentMode = AdaptiveEngine.getCurrentMode(prefs)

        if (!AdaptiveEngine.shouldShowPunishmentScreen(currentMode)) {
            finish()
            return
        }

        setupWindowFlags()
        enableDND()
        blockBackButton()

        val punishmentDuration = AdaptiveEngine.getPunishmentDurationSeconds(currentMode, prefs)

        if (!PunishmentForegroundService.isRunning) {
            PunishmentForegroundService.start(this, punishmentDuration * 1000)
        }

        punishmentEndedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Timer ended, but we don't finish yet. 
                // The UI will switch to Redemption mode.
            }
        }
        registerReceiver(
            punishmentEndedReceiver,
            IntentFilter("PUNISHMENT_ENDED"),
            RECEIVER_NOT_EXPORTED
        )

        setContent {
            IronMindTheme {
                PunishmentScreen(
                    onExit = {
                        PunishmentForegroundService.stop(applicationContext)
                        restoreDND()
                        finish()
                    }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (emergencyAppLaunching) {
            emergencyAppLaunching = false
            return
        }
        if (PunishmentForegroundService.isRunning) {
            startActivity(
                Intent(this, PunishmentActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        isInForeground = true
        emergencyAppLaunching = false
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    private fun setupWindowFlags() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }

    private fun enableDND() {
        notifManager = getSystemService(NotificationManager::class.java)
        if (notifManager?.isNotificationPolicyAccessGranted == true) {
            previousDndMode = notifManager?.currentInterruptionFilter
                ?: NotificationManager.INTERRUPTION_FILTER_ALL
            notifManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    fun restoreDND() {
        if (notifManager?.isNotificationPolicyAccessGranted == true) {
            notifManager?.setInterruptionFilter(previousDndMode)
        }
    }

    private fun blockBackButton() {
        onBackPressedDispatcher.addCallback(this) { /* blocked */ }
    }

    override fun onDestroy() {
        super.onDestroy()
        isInForeground = false
        runCatching { unregisterReceiver(punishmentEndedReceiver) }
    }
}

@Composable
fun PunishmentScreen(onExit: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isPreview = LocalInspectionMode.current
    
    var timeLeft by remember { mutableStateOf(if (isPreview) 180 else PunishmentForegroundService.remainingSeconds) }
    val isTimerDone = timeLeft <= 0 && (isPreview || PunishmentForegroundService.endTimeMillis > 0)
    var showBypassUI by remember { mutableStateOf(false) }

    val prefs = if (isPreview) null else runCatching { PrefManager.getInstance(context) }.getOrNull()
    val mode = prefs?.let { AdaptiveEngine.getCurrentMode(it) } ?: AppMode.IRON
    val contract = remember { Triple(prefs?.userName ?: "Alex", prefs?.contractGoal ?: "Start business", prefs?.contractReason ?: "Freedom") }
    val identity = remember { prefs?.identityStatements?.randomOrNull() ?: "I am relentless." }

    if (!isPreview) {
        LaunchedEffect(Unit) {
            while (true) {
                timeLeft = PunishmentForegroundService.remainingSeconds
                delay(500L)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // --- IDENTITY & CONTRACT ---
                Crossfade(targetState = isTimerDone || showBypassUI, label = "Header") { done ->
                    if (!done) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IdentitySurface(identity)
                            Spacer(modifier = Modifier.height(16.dp))
                            ContractSurface(contract)
                        }
                    } else if (isTimerDone) {
                        Text(
                            "TIME EXPIRED. EARN YOUR RETURN.",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.sp, fontWeight = FontWeight.Black
                            ),
                            color = SuccessGreen
                        )
                    } else {
                        Text(
                            "EMERGENCY BYPASS PROTOCOL",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.sp, fontWeight = FontWeight.Black
                            ),
                            color = WarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- MAIN INTERACTION (PUNISHMENT OR REDEMPTION) ---
                if (showBypassUI && !isTimerDone) {
                    EmergencyBypassUI(
                        onCancel = { showBypassUI = false },
                        onUnlock = onExit
                    )
                } else if (!isTimerDone) {
                    CountdownUI(
                        timeLeft = timeLeft,
                        mode = mode,
                        onBypassClick = { showBypassUI = true }
                    )
                } else {
                    RedemptionUI(mode, onExit)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- EMERGENCY BAR ---
            EmergencyAppsBar(context = context, userEmergencyApps = prefs?.emergencyApps?.toList() ?: emptyList())
        }
    }
}

@Composable
fun CountdownUI(timeLeft: Long, mode: AppMode, onBypassClick: () -> Unit) {
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("💀", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            AppCopy.skipConsequenceTitle.forMode(mode).uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black, letterSpacing = 4.sp
            ),
            color = ErrorRed
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            String.format("%02d:%02d", minutes, seconds),
            fontSize = 84.sp, fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Lockout Active. Sit with the choice you made.",
            fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(onClick = onBypassClick) {
            Text("EMERGENCY BYPASS", color = Color.Gray.copy(alpha = 0.5f), fontSize = 12.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun RedemptionUI(mode: AppMode, onExit: () -> Unit) {
    if (mode == AppMode.IRON) {
        BoxBreathingTask(onComplete = onExit)
    } else {
        ReflectionTask(onComplete = onExit)
    }
}

@Composable
fun BoxBreathingTask(onComplete: () -> Unit) {
    var phase by remember { mutableStateOf("Inhale") }
    var cycleCount by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    val totalCycles = 4

    LaunchedEffect(Unit) {
        while (cycleCount < totalCycles) {
            phase = "Inhale"; for (i in 1..40) { delay(100); progress = i / 40f }; progress = 1f
            phase = "Hold";   delay(4000)
            phase = "Exhale"; for (i in 1..40) { delay(100); progress = 1f - (i / 40f) }; progress = 0f
            phase = "Hold";   delay(4000)
            cycleCount++
        }
        onComplete()
    }

    val scale by animateFloatAsState(targetValue = if (phase == "Inhale" || phase == "Hold" && progress == 1f) 1.5f else 1f, label = "Scale")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("BOX BREATHING", fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = SuccessGreen)
        Text("Cycle $cycleCount / $totalCycles", color = Color.Gray, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(contentAlignment = Alignment.Center) {
            // Animated Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(SuccessGreen.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(2.dp, SuccessGreen)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(phase, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Text("Regain focus. Breathe with the circle.", textAlign = TextAlign.Center, color = Color.Gray)
    }
}

@Composable
fun ReflectionTask(onComplete: () -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("MINDFUL RESET", fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = NeonCyan)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("What will you do differently tomorrow?", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = SurfaceElevated
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onComplete,
            enabled = text.length > 10,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("FINISH RESET", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun IdentitySurface(identity: String?) {
    identity?.let {
        Surface(color = DeepBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("IDENTITY CHECK:", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                Text("\"$it\"", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
fun ContractSurface(contract: Triple<String, String, String>) {
    Surface(color = DeepBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("YOU PROMISED:", fontSize = 10.sp, color = GoldXP, fontWeight = FontWeight.Bold)
            Text("\"${contract.third}\"", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}

@Composable
fun EmergencyAppsBar(context: Context, userEmergencyApps: List<String>) {
    Surface(color = DeepBackground, shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EmergencyButton(label = "Call", icon = Icons.Rounded.Call, onClick = {
                PunishmentActivity.emergencyAppLaunching = true
                context.startActivity(Intent(Intent.ACTION_DIAL).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            })
            EmergencyButton(label = "SMS", icon = Icons.AutoMirrored.Rounded.Message, onClick = {
                PunishmentActivity.emergencyAppLaunching = true
                val smsIntent = Intent(Intent.ACTION_MAIN).apply {
                    setPackage(getDefaultSmsPackage(context)); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(smsIntent) }
            })
            userEmergencyApps.take(2).forEach { pkg ->
                EmergencyAppIcon(packageName = pkg, context = context)
            }
        }
    }
}

@Composable
fun EmergencyButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp).background(SurfaceElevated, CircleShape)) {
        Icon(icon, contentDescription = label, tint = SuccessGreen, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun EmergencyAppIcon(packageName: String, context: Context) {
    val drawable = remember(packageName) { runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull() }
    IconButton(
        onClick = {
            PunishmentActivity.emergencyAppLaunching = true
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent != null) context.startActivity(intent)
        },
        modifier = Modifier.size(48.dp).background(SurfaceElevated, CircleShape)
    ) {
        if (drawable != null) {
            Image(bitmap = drawable.toBitmap(48, 48).asImageBitmap(), contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}
@Composable
fun EmergencyBypassUI(onCancel: () -> Unit, onUnlock: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val pledge = "I acknowledge that I am breaking my commitment. I am choosing immediate comfort over long-term growth. If this is a true emergency, I accept the bypass. If this is weakness, I accept the damage to my integrity. I will return stronger, or I will not return at all."
    
    val normalizedPledge = pledge.replace(Regex("[^a-zA-Z0-9 ]"), "").lowercase().replace(Regex("\\s+"), " ").trim()
    val normalizedText = text.replace(Regex("[^a-zA-Z0-9 ]"), "").lowercase().replace(Regex("\\s+"), " ").trim()
    
    val isMatch = normalizedText == normalizedPledge
    val wordCount = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
    val totalWords = 49

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "TYPE THE PLEDGE EXACTLY TO BYPASS",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = WarningAmber
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = Color.DarkGray.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                pledge,
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = WarningAmber,
                unfocusedIndicatorColor = Color.Gray
            ),
            placeholder = { Text("Type here...", color = Color.Gray) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("CANCEL", color = Color.Gray)
            }
            Text(
                "$wordCount / $totalWords words",
                color = if (wordCount >= totalWords && !isMatch) Color.Red else if (isMatch) SuccessGreen else Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onUnlock,
            enabled = isMatch,
            colors = ButtonDefaults.buttonColors(
                containerColor = WarningAmber,
                contentColor = Color.Black,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("UNLOCK DEVICE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PunishmentScreenPreview() {
    IronMindTheme {
        PunishmentScreen(onExit = {})
    }
}
