package com.sanket_satpute_20.ironmind.focus

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

private const val ESCAPE_WINDOW_SECONDS = 60

class BlackHoleActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var vibrator: Vibrator? = null
    
    private val _energy = mutableStateOf(0f)
    
    private var lastUpdate: Long = 0
    private var lastX = 0f; private var lastY = 0f; private var lastZ = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        onBackPressedDispatcher.addCallback(this) { performHomeExit() }

        setContent {
            IronMindTheme {
                com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry {
                    var energyProgress by rememberSaveable { _energy }
                    var showExitDialog by remember { mutableStateOf(false) }
                    
                    Box {
                        BlackHoleVortexScreen(
                            energy = energyProgress,
                            onEmergencyClick = { showExitDialog = true },
                            onComplete = {
                                val prefs = PrefManager.getInstance(this@BlackHoleActivity)
                                prefs.isBlackHoleActive = false
                                finish()
                            }
                        )

                        if (showExitDialog) {
                            EmergencyBreakDialog(
                                onConfirm = {
                                    breakVoid()
                                    finish()
                                },
                                onDismiss = { showExitDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun performHomeExit() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    private fun breakVoid() {
        val prefs = PrefManager.getInstance(this)
        // Severe penalty for breaking ultra-deep work
        com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(this).penalizeXp(100L)
        if (prefs.isStreakInCriticalState) {
            com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(this).resetStreak()
            prefs.isStreakInCriticalState = false
        } else {
            prefs.isStreakInCriticalState = true
            prefs.streakCriticalTimestamp = System.currentTimeMillis()
        }
        prefs.isBlackHoleActive = false
        performHomeExit()
    }

    override fun onStart() {
        super.onStart()
        sensorManager.unregisterListener(this) 
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val curTime = System.currentTimeMillis()
        if ((curTime - lastUpdate) > 80) {
            val diffTime = curTime - lastUpdate
            lastUpdate = curTime

            val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
            val speed = sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) / diffTime * 10000
            
            if (speed > 900) { 
                _energy.value = (_energy.value + 1.5f).coerceAtMost(100f)
                triggerHapticFeedback(_energy.value)
            }
            lastX = x; lastY = y; lastZ = z
        }
    }

    private fun triggerHapticFeedback(energy: Float) {
        val intensity = (energy / 100f * 255).toInt().coerceIn(10, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, intensity))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun EmergencyBreakDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        title = { Text("END THE VOID RUN?", fontWeight = FontWeight.Black, color = ErrorRed) },
        text = {
            Text(
                "This will end the Black Hole run immediately.\n\nPenalty: -100 XP\nEffect: Streak reset to 0\n\nUse this only if you need to shut the run down for real.",
                color = Color.White.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                Text("END RUN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("STAY IN THE VOID", color = Color.Gray) }
        }
    )
}

@Composable
fun BlackHoleVortexScreen(
    energy: Float,
    onEmergencyClick: () -> Unit,
    onComplete: () -> Unit
) {
    val progress = energy / 100f
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    var escapeSecondsRemaining by rememberSaveable {
        mutableIntStateOf(if (isPreview) 37 else ESCAPE_WINDOW_SECONDS)
    }
    val eventHorizonReached = escapeSecondsRemaining <= 0
    val infiniteTransition = rememberInfiniteTransition(label = "vortex")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(energy) {
        if (energy >= 100f) { delay(800); onComplete() }
    }

    LaunchedEffect(isPreview, energy >= 100f) {
        if (!isPreview && energy < 100f) {
            while (escapeSecondsRemaining > 0 && energy < 100f) {
                delay(1_000L)
                escapeSecondsRemaining -= 1
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        
        Box(
            modifier = Modifier
                .size(400.dp)
                .rotate(rotation)
                .scale(pulseScale + (progress * 0.2f) + if (eventHorizonReached) 0.05f else 0f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().blur(if (progress > 0.5f) 10.dp else 0.dp)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.1f + (progress * 0.5f)), Color.Transparent)
                    ),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = 80f)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(212.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { escapeSecondsRemaining / ESCAPE_WINDOW_SECONDS.toFloat() },
                modifier = Modifier.fillMaxSize(),
                color = if (eventHorizonReached) ErrorRed else Color.White.copy(alpha = 0.72f),
                trackColor = Color.White.copy(alpha = 0.07f),
                strokeWidth = 2.dp
            )
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Color.Black, CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.1f + (progress * 0.9f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (progress > 0) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        color = Color.White,
                        trackColor = Color.Transparent,
                        strokeWidth = 4.dp
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(64.dp))
                Text(
                    "SINGULARITY ACTIVE",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 6.sp, fontWeight = FontWeight.Black),
                    color = Color.White.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "THE BLACK HOLE",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 48.dp)) {
                Text(
                    when {
                        eventHorizonReached -> "EVENT HORIZON LOCKED"
                        progress < 0.3f -> "SHAKE VIGOROUSLY"
                        else -> "BREAKING EVENT HORIZON"
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (eventHorizonReached) ErrorRed else Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (eventHorizonReached) "ESCAPE WINDOW CLOSED · KEEP GENERATING ENERGY" else "ESCAPE WINDOW  ${formatEscapeCountdown(escapeSecondsRemaining)}",
                    color = if (eventHorizonReached) ErrorRed.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.58f),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Escape requires physical energy generation.",
                    color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = onEmergencyClick) {
                    Text("EMERGENCY EXIT", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF000000)
@androidx.compose.runtime.Composable
fun BlackHoleVortexScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        BlackHoleVortexScreen(
            energy = 72f,
            onEmergencyClick = {},
            onComplete = {}
        )
    }
}

private fun formatEscapeCountdown(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(safeSeconds / 60, safeSeconds % 60)
}
