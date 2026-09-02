package com.sanket_satpute_20.ironmind.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class NudgeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val blockedItem = intent.getStringExtra(EXTRA_BLOCKED_ITEM) ?: "Distraction"
        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()

        // Disable standard back button during nudge
        onBackPressedDispatcher.addCallback(this) {
            returnHome()
        }

        setContent {
            IronMindTheme {
                NudgeOverlay(
                    blockedItem = blockedItem,
                    onReturn = { returnHome() },
                    onProceed = {
                        val broadcastIntent = Intent("com.ironmind.WHITELIST_PACKAGE").apply {
                            putExtra("package_name", blockedPackage)
                            // Important: Use explicit component to target our service for security and to avoid export issues
                            setPackage(packageName)
                        }
                        sendBroadcast(broadcastIntent)
                        finish() // Let them proceed to the app
                    }
                )
            }
        }
    }

    private fun returnHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_ITEM = "BLOCKED_ITEM"
        const val EXTRA_BLOCKED_PACKAGE = "BLOCKED_PACKAGE"

        fun createIntent(context: android.content.Context, blockedItem: String, blockedPackage: String): Intent {
            return Intent(context, NudgeActivity::class.java).apply {
                putExtra(EXTRA_BLOCKED_ITEM, blockedItem)
                putExtra(EXTRA_BLOCKED_PACKAGE, blockedPackage)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NudgeOverlay(
    blockedItem: String,
    onReturn: () -> Unit,
    onProceed: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBackground.copy(alpha = 0.93f) // Translucent deep black/blue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "MILD FRICTION",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Take a breath. Do you really need to open $blockedItem right now?",
                color = Color.LightGray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Primary action: Return Home
            Button(
                onClick = onReturn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "CLOSE DISTRACTION",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary action: Proceed Anyway (disabled until timer ends)
            val alpha by animateFloatAsState(targetValue = if (timeLeft == 0) 1f else 0.5f, animationSpec = tween(500), label = "alpha")
            
            OutlinedButton(
                onClick = onProceed,
                enabled = timeLeft == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray,
                    disabledContentColor = Color.DarkGray
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (timeLeft == 0) Color.Gray else Color.DarkGray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                AnimatedContent(targetState = timeLeft, label = "timer") { time ->
                    if (time > 0) {
                        Text(
                            "Wait ${time}s...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            "PROCEED ANYWAY",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun NudgeOverlayPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        NudgeOverlay(blockedItem = "Instagram", onReturn = {}, onProceed = {})
    }
}
