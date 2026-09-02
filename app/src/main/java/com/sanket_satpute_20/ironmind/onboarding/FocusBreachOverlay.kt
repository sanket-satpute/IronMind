package com.sanket_satpute_20.ironmind.onboarding

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.components.AppIconImage
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@Composable
fun FocusBreachOverlay(
    blockedItem: String,
    onReturn: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    
    // Lock the back button — users cannot escape the breach screen by just swiping back
    BackHandler(enabled = true) {
        // Do nothing — force them to use the "BACK TO REALITY" button
    }

    // Haptic feedback to emphasize the breach
    if (!isPreview) {
        val vibrator = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }
        LaunchedEffect(Unit) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "FOCUS BREACH",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "You tried to open $blockedItem during an active focus session.",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onReturn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "BACK TO REALITY",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EarnedUnlockBreachOverlay(
    blockedPackage: String,
    blockedAppName: String,
    remainingCount: Int,
    completedCount: Int,
    totalCount: Int,
    onReturn: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 760

    BackHandler(enabled = true) {}

    if (!isPreview) {
        val vibrator = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }
        LaunchedEffect(Unit) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 140), -1))
        }
    }

    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val chamberGlow = rememberInfiniteTransition(label = "earned_unlock_glow")
    val glowAlpha by chamberGlow.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepBackground, DeepBackground, DeepBackground)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ElectricViolet.copy(alpha = glowAlpha), Color.Transparent),
                            radius = 900f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = if (isCompactHeight) 14.dp else 24.dp,
                        bottom = if (isCompactHeight) 14.dp else 20.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                    color = NeonCyan.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.22f))
                ) {
                    Text(
                        "APP SEALED FOR TODAY",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 12.dp else 20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EarnedUnlockStatChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Lock,
                        label = if (remainingCount == 1) "1 MISSION LEFT" else "$remainingCount MISSIONS LEFT",
                        accent = WarningAmber
                    )
                    EarnedUnlockStatChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.CheckCircle,
                        label = "$completedCount CLEARED",
                        accent = SuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 18.dp else 28.dp))

                Box(
                    modifier = Modifier
                        .size(if (isCompactHeight) 204.dp else 228.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.025f))
                        .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompactHeight) 164.dp else 184.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .border(1.dp, NeonCyan.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(if (isCompactHeight) 64.dp else 74.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .padding(13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (blockedPackage.isNotBlank()) {
                                    AppIconImage(packageName = blockedPackage, modifier = Modifier.fillMaxSize())
                                } else {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "SEALED",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                blockedAppName,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = if (isCompactHeight) 18.sp else 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 22.dp))

                Text(
                    "Finish the board.",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (isCompactHeight) 24.sp else 28.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Then $blockedAppName opens for today.",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 16.dp else 26.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.045f),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TODAY'S BOARD",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            AnimatedContent(targetState = "$completedCount/$totalCount", label = "progress_count") { label ->
                                Text(
                                    label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.07f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NeonCyan, TextPrimary)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (remainingCount == 1) "One last mission unlocks this app."
                            else "Clear the remaining missions to release it.",
                            color = Color.White.copy(alpha = 0.56f),
                            fontSize = 12.sp
                        )
                    }
                }
                } // End of scrollable column

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = remainingCount > 0,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Text(
                            "DISTRACTION APPS STAY SEALED UNTIL RESOLUTION",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Text(
                        "RETURN. COMPLETE. UNLOCK.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onReturn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                ) {
                    Text(
                        "GO BACK TO WORK",
                        color = DeepBackground,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EarnedUnlockStatChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun FocusBreachOverlayPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        FocusBreachOverlay(blockedItem = "YouTube", onReturn = {})
    }
}
