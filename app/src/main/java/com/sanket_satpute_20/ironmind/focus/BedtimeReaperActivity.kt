package com.sanket_satpute_20.ironmind.focus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.focus.WorkStartActivity
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

class BedtimeReaperActivity : ComponentActivity() {

    companion object {
        fun createIntent(context: android.content.Context): android.content.Intent =
            android.content.Intent(context, BedtimeReaperActivity::class.java).apply {
                addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        }

        setContent {
            IronMindTheme {
                com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry {
                    var showShieldShatter by remember { mutableStateOf(false) }
                    
                    BedtimeReaperScreen(
                        onCompleteAll = { firstTask ->
                            if (firstTask != null) {
                                startActivity(
                                    WorkStartActivity.createIntent(
                                        context = this@BedtimeReaperActivity,
                                        taskId = firstTask.id,
                                        taskName = firstTask.name,
                                        newTask = true
                                    )
                                )
                            }
                            finish()
                        },
                        onAcceptPenalty = {
                            val shattered = applyPenalty()
                            if (shattered) {
                                showShieldShatter = true
                            } else {
                                finish()
                            }
                        },
                        onNothingPending = {
                            finish()
                        }
                    )
                    
                    if (showShieldShatter) {
                        com.sanket_satpute_20.ironmind.ui.components.ShieldShatterOverlay(
                            onDismiss = {
                                showShieldShatter = false
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun applyPenalty(): Boolean {
        var shieldShattered = false
        com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(this).playError(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(this).playTemptationBlocked()
        val prefs = PrefManager.getInstance(this)
        com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(this).penalizeXp(50L)
        if (prefs.streakShields > 0) {
            prefs.streakShields = prefs.streakShields - 1
            shieldShattered = true
        } else if (prefs.isStreakInCriticalState) {
            com.sanket_satpute_20.ironmind.gamification.GamificationEngine.getInstance(this).resetStreak()
            prefs.isStreakInCriticalState = false
        } else {
            prefs.isStreakInCriticalState = true
            prefs.streakCriticalTimestamp = System.currentTimeMillis()
        }
        return shieldShattered
    }
}

@Composable
fun BedtimeReaperScreen(
    onCompleteAll: (Task?) -> Unit,
    onAcceptPenalty: () -> Unit,
    onNothingPending: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { IronMindDatabase.getDatabase(context) }
    var pendingTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Skilled Engineer: Adding safety to data fetching
        runCatching {
            pendingTasks = db.taskDao().getPendingDeferredTasks(LocalDate.now().toString())
        }.onFailure {
            pendingTasks = emptyList()
        }
        loaded = true
        if (pendingTasks.isEmpty()) {
            val notifManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notifManager.cancel(9999)
            onNothingPending()
        }
    }

    if (!loaded) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {}
        return
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        ReaperBackground()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "INTEGRITY RECONCILIATION",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 2.sp, fontWeight = FontWeight.Black
                    ),
                    color = ErrorRed
                )
                
                Text(
                    "The day is almost over.",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "You deferred ${pendingTasks.size} tasks today. Leaving them unfinished breaks your word to yourself.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            items(pendingTasks) { task ->
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = GoldXP, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(task.name, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { onCompleteAll(pendingTasks.firstOrNull()) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("I'LL FINISH THEM NOW", fontWeight = FontWeight.Black)
                    }

                    TextButton(
                        onClick = onAcceptPenalty,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GIVE UP & ACCEPT PENALTY (-50 XP)", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReaperBackground() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val moonAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1500, easing = LinearOutSlowInEasing)
    )
    val moonScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing)
    )

    val skullOffset by animateFloatAsState(
        targetValue = if (visible) 0f else -500f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val skullAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .alpha(moonAlpha)
            .scale(moonScale)
        ) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.35f)
            val radius = 400f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ErrorRed.copy(alpha = 0.3f), Color.Transparent),
                    center = centerOffset,
                    radius = radius * 2.5f
                ),
                radius = radius * 2.5f,
                center = centerOffset
            )
            
            drawCircle(
                color = ErrorRed.copy(alpha = 0.15f),
                radius = radius,
                center = centerOffset
            )
        }

        Text(
            text = "☠️",
            fontSize = 180.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 80.dp + skullOffset.dp)
                .alpha(skullAlpha),
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = ErrorRed,
                    blurRadius = 60f
                )
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun BedtimeReaperScreenPreview() {
    IronMindTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            ReaperBackground()
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text("INTEGRITY RECONCILIATION", color = ErrorRed, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(12.dp))
                Text("The day is almost over.", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("You deferred 2 tasks today. Decide how you finish the day.", color = Color.LightGray, textAlign = TextAlign.Center)
            }
        }
    }
}
