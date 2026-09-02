package com.sanket_satpute_20.ironmind.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.material3.Text
import com.sanket_satpute_20.ironmind.R
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // ── Animation states ──────────────────────────────────────
    var logoVisible    by remember { mutableStateOf(false) }
    var nameVisible    by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }
    var devVisible     by remember { mutableStateOf(false) }

    // Logo scale + alpha
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        )
    )
    val logoAlpha by animateFloatAsState(
        targetValue  = if (logoVisible) 1f else 0f,
        animationSpec = tween(700)
    )

    // App name slide up
    val nameOffset by animateFloatAsState(
        targetValue  = if (nameVisible) 0f else 40f,
        animationSpec = tween(600, easing = EaseOutCubic)
    )
    val nameAlpha by animateFloatAsState(
        targetValue  = if (nameVisible) 1f else 0f,
        animationSpec = tween(600)
    )

    // Tagline
    val taglineAlpha by animateFloatAsState(
        targetValue  = if (taglineVisible) 1f else 0f,
        animationSpec = tween(500)
    )

    // Developer credit
    val devAlpha by animateFloatAsState(
        targetValue  = if (devVisible) 1f else 0f,
        animationSpec = tween(600)
    )

    // Pulse ring around logo
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.18f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue  = 0.05f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )

    // ── Animation sequence ────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(200);  logoVisible    = true
        delay(500);  nameVisible    = true
        delay(300);  taglineVisible = true
        delay(400);  devVisible     = true
        delay(1500); onSplashComplete()
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            // ── Logo block ───────────────────────────────
            Box(contentAlignment = Alignment.Center) {

                // Outer pulse ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .alpha(if (logoVisible) pulseAlpha else 0f)
                        .background(
                            ErrorRed.copy(alpha = 0.15f),
                            androidx.compose.foundation.shape.CircleShape
                        )
                )

                // Inner pulse ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale * 0.92f)
                        .alpha(if (logoVisible) pulseAlpha * 0.6f else 0f)
                        .background(
                            ErrorRed.copy(alpha = 0.1f),
                            androidx.compose.foundation.shape.CircleShape
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_ironmind_mark),
                    contentDescription = "IronMind Logo",
                    modifier = Modifier
                        .size(124.dp)
                        .padding(4.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── App name ─────────────────────────────────
            Text(
                text     = "IRONMIND",
                fontSize  = 42.sp,
                fontWeight = FontWeight.Black,
                color    = Color.White,
                letterSpacing = 6.sp,
                modifier = Modifier
                    .alpha(nameAlpha)
                    .graphicsLayer { translationY = nameOffset }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Red accent line under name ────────────────
            Box(
                modifier = Modifier
                    .width(if (nameVisible) 80.dp else 0.dp)
                    .height(3.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ErrorRed,
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tagline ──────────────────────────────────
            Text(
                text      = "No excuses. Just execution.",
                fontSize  = 14.sp,
                fontWeight = FontWeight.Normal,
                color     = SurfaceElevated,
                letterSpacing = 1.sp,
                modifier  = Modifier.alpha(taglineAlpha)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // ── Developer credit ──────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(devAlpha)
            ) {
                Text(
                    text      = "Crafted with ⚡ by",
                    fontSize  = 12.sp,
                    color     = SurfaceElevated,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text      = "Sanket Satpute",
                    fontSize  = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color     = ElectricViolet,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text      = "v1.0.0",
                    fontSize  = 11.sp,
                    color     = SurfaceElevated
                )
            }
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun SplashScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        SplashScreen(onSplashComplete = {})
    }
}