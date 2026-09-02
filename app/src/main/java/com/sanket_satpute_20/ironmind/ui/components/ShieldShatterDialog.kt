package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun ShieldShatterOverlay(
    onDismiss: () -> Unit
) {
    var shatterTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        shatterTriggered = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                ShatteringShieldGraphic(isShattered = shatterTriggered)
            }

            Text(
                text = "SHIELD SHATTERED",
                color = WarningAmber,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "You breached a critical protocol.\nYour streak was about to die.",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your Streak Shield absorbed the fatal blow.\nYou have no shields remaining.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceElevated,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "ACKNOWLEDGE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun ShatteringShieldGraphic(isShattered: Boolean) {
    val transition = updateTransition(targetState = isShattered, label = "shatter_transition")
    
    val explosionProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "explosion"
    ) { shattered ->
        if (shattered) 1f else 0f
    }
    
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val shards = remember {
        List(12) {
            val angle = Random.nextFloat() * 360f
            val distance = Random.nextFloat() * 100f + 50f
            val rotation = Random.nextFloat() * 360f
            val size = Random.nextFloat() * 15f + 10f
            ShardData(angle, distance, rotation, size)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val shieldSize = size.width * 0.6f
        
        if (explosionProgress < 1f) {
            withTransform({
                val currentScale = if (isShattered) 1f + explosionProgress * 0.5f else pulse
                scale(scaleX = currentScale, scaleY = currentScale)
                val alpha = (1f - explosionProgress * 1.5f).coerceIn(0f, 1f)
                translate(
                    left = if (isShattered) Random.nextFloat() * 10f - 5f else 0f,
                    top = if (isShattered) Random.nextFloat() * 10f - 5f else 0f
                )
            }) {
                val path = Path().apply {
                    moveTo(center.x, center.y - shieldSize * 0.5f)
                    lineTo(center.x + shieldSize * 0.45f, center.y - shieldSize * 0.4f)
                    lineTo(center.x + shieldSize * 0.45f, center.y + shieldSize * 0.1f)
                    quadraticBezierTo(
                        center.x + shieldSize * 0.3f, center.y + shieldSize * 0.5f,
                        center.x, center.y + shieldSize * 0.6f
                    )
                    quadraticBezierTo(
                        center.x - shieldSize * 0.3f, center.y + shieldSize * 0.5f,
                        center.x - shieldSize * 0.45f, center.y + shieldSize * 0.1f
                    )
                    lineTo(center.x - shieldSize * 0.45f, center.y - shieldSize * 0.4f)
                    close()
                }
                val alpha = (1f - explosionProgress * 1.5f).coerceIn(0f, 1f)
                drawPath(
                    path = path,
                    color = WarningAmber.copy(alpha = alpha * 0.2f)
                )
                drawPath(
                    path = path,
                    color = WarningAmber.copy(alpha = alpha),
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
        
        if (explosionProgress > 0f) {
            shards.forEach { shard ->
                val currentDist = shard.targetDistance * explosionProgress
                val x = center.x + Math.cos(Math.toRadians(shard.angle.toDouble())).toFloat() * currentDist
                val y = center.y + Math.sin(Math.toRadians(shard.angle.toDouble())).toFloat() * currentDist
                val alpha = (1f - explosionProgress).coerceIn(0f, 1f)
                
                withTransform({
                    translate(left = x, top = y)
                    rotate(shard.rotation * explosionProgress)
                }) {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, -shard.size)
                            lineTo(shard.size, shard.size)
                            lineTo(-shard.size, shard.size)
                            close()
                        },
                        color = WarningAmber.copy(alpha = alpha),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }
        }
    }
}

private data class ShardData(
    val angle: Float,
    val targetDistance: Float,
    val rotation: Float,
    val size: Float
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ShieldShatterDialogPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ShieldShatterOverlay(onDismiss = {})
    }
}
