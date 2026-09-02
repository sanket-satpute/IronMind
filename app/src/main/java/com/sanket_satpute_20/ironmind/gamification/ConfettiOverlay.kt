package com.sanket_satpute_20.ironmind.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var rotation: Float,
    var rotationSpeed: Float
)

@Composable
fun ConfettiOverlay(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    var particles by remember { mutableStateOf(emptyList<ConfettiParticle>()) }
    val colors = listOf(
        ElectricViolet, // ContextPurple
        SuccessGreen, // SuccessGreen
        WarningAmber, // WarningAmber
        NeonCyan, // Blue
        ErrorRed  // Pink
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            val newParticles = List(100) {
                val angle = Random.nextDouble(PI / 4, 3 * PI / 4)
                val speed = Random.nextDouble(20.0, 60.0)
                ConfettiParticle(
                    x = 500f, // Will be updated to center later in draw
                    y = 1500f,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = -(sin(angle) * speed).toFloat(),
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 8f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 20f - 10f
                )
            }
            particles = newParticles

            // Animation loop
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 3000) {
                withFrameNanos { 
                    particles = particles.map { p ->
                        p.copy(
                            x = p.x + p.vx,
                            y = p.y + p.vy,
                            vy = p.vy + 1.5f, // Gravity
                            vx = p.vx * 0.98f, // Air resistance
                            rotation = p.rotation + p.rotationSpeed
                        )
                    }
                }
            }
            particles = emptyList()
            onAnimationEnd()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val startY = size.height * 0.8f

        particles.forEach { particle ->
            // Shift initial spawn to center for the first frame if needed
            val drawX = if (particle.y == 1500f) centerX else particle.x
            val drawY = if (particle.y == 1500f) startY else particle.y

            withTransform({
                translate(drawX, drawY)
                rotate(particle.rotation)
            }) {
                drawRect(
                    color = particle.color,
                    size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun ConfettiOverlayPreview() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.sanket_satpute_20.ironmind.ui.theme.DeepBackground)
    ) {
        ConfettiOverlay(isVisible = true, onAnimationEnd = {})
    }
}
