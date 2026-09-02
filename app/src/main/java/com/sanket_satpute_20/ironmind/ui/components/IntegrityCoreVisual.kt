package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

@Composable
fun IntegrityCoreVisual(
    streak: Int,
    shields: Int,
    isCritical: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_pulse")
    
    // Core pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isCritical) 500 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Core pulsing alpha
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isCritical) 500 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val coreColor = if (isCritical) Color.Red else SuccessGreen // Green for healthy, Red for critical
    val shieldColor = NeonCyan // Light blue for shields

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
    ) {
        // The visual canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.height / 3

            // Draw outer glow (pulsing)
            drawCircle(
                color = coreColor.copy(alpha = pulseAlpha),
                radius = baseRadius * pulseScale,
                center = center
            )

            // Draw solid inner core
            drawCircle(
                color = coreColor,
                radius = baseRadius * 0.7f,
                center = center
            )

            // Draw orbiting shields
            if (shields > 0) {
                val shieldRadius = baseRadius * 1.3f
                drawCircle(
                    color = shieldColor.copy(alpha = 0.5f),
                    radius = shieldRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
                
                // Draw shield nodes based on count (max 3 for visual cleanliness)
                val visualShields = shields.coerceAtMost(3)
                for (i in 0 until visualShields) {
                    val angle = (i * (360f / visualShields)) * (Math.PI / 180f)
                    val x = center.x + shieldRadius * kotlin.math.cos(angle).toFloat()
                    val y = center.y + shieldRadius * kotlin.math.sin(angle).toFloat()
                    drawCircle(
                        color = shieldColor,
                        radius = 8.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Overlay the raw quantitative data on top
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$streak",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "DAY STREAK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )
            if (shields > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🛡️ $shields",
                    fontSize = 14.sp,
                    color = shieldColor,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isCritical) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CRITICAL",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun IntegrityCoreVisualPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        IntegrityCoreVisual(streak = 5, shields = 2, isCritical = false)
    }
}