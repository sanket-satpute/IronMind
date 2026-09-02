package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun EnergyCoreUI(
    progress: Float,
    modifier: Modifier,
    remainingLabel: String,
    accent: Color,
    phaseLabel: String
) {
    // Dynamic pulse based on remaining progress
    val isCritical = progress < 0.15f
    val pulseDuration = if (isCritical) 300 else 1500

    val infiniteTransition = rememberInfiniteTransition(label = "CorePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulseAnim"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCritical) 2000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CoreRotationAnim"
    )
    val shakeOffset = if (isCritical) Random.nextFloat() * 4f - 2f else 0f

    val coreColor = if (isCritical) ErrorRed else accent

    Box(modifier = modifier.offset(x = shakeOffset.dp, y = shakeOffset.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 24.dp.toPx()
            val radius = (size.minDimension / 2f) - stroke
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer Glow Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreColor.copy(alpha = 0.3f * pulseScale), Color.Transparent),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f,
                center = center
            )

            // The Core HP Track
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = radius,
                center = center,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Dynamic Core HP Ring
            drawArc(
                color = coreColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Pulsing Inner Reactor
            drawCircle(
                color = coreColor.copy(alpha = 0.15f * pulseScale),
                radius = radius - stroke * 1.5f,
                center = center
            )
            
            // Rotating Energy Particles (simulated with dashed arc)
            drawArc(
                color = coreColor.copy(alpha = 0.6f),
                startAngle = rotation,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - (radius - stroke * 0.5f), center.y - (radius - stroke * 0.5f)),
                size = Size((radius - stroke * 0.5f) * 2f, (radius - stroke * 0.5f) * 2f),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = phaseLabel,
                color = coreColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = remainingLabel,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp
            )
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun EnergyCoreUIPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        EnergyCoreUI(progress = 0.5f, modifier = androidx.compose.ui.Modifier, remainingLabel = "50%", accent = androidx.compose.ui.graphics.Color.Cyan, phaseLabel = "Phase 1")
    }
}