package com.sanket_satpute_20.ironmind.identity

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

@Composable
fun IdentityFlashOverlay(
    taskName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // We mock the context access in previews to avoid crashes
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val identity = if (isPreview) "I am an elite finisher." else remember { getRandomIdentity(context) }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(3200L) // Show for 3.2 seconds
        visible = false
        delay(500L)  // Wait for fade animation
        onDismiss()
    }

    val glowAlpha by animateFloatAsState(
        targetValue = if (visible) 0.6f else 0f,
        animationSpec = tween(800, easing = LinearOutSlowInEasing),
        label = "glow"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + scaleIn(tween(600, easing = EaseOutElastic)),
        exit = fadeOut(tween(500)) + scaleOut(tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBackground.copy(alpha = 0.9f)), // Darker overlay 90% opacity
            contentAlignment = Alignment.Center
        ) {
            // Background ambient glow
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .scale(1.5f)
                    .blur(64.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SuccessGreen.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "IDENTITY SECURED",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = SuccessGreen
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "+1",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Vote cast for the person\nyou are becoming.",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                if (identity != null) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            "\"$identity\"",
                            fontSize = 16.sp,
                            color = SuccessGreen,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IdentityFlashOverlayPreview() {
    IronMindTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            IdentityFlashOverlay(taskName = "Morning Run", onDismiss = {})
        }
    }
}