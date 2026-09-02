package com.sanket_satpute_20.ironmind.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IconGlow(
    color: Color,
    radius: Dp = 8.dp,
    pulsing: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "IconGlowPulse")
    val alphaMultiplier by infiniteTransition.animateFloat(
        initialValue = if (pulsing) 0.5f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        this.color = color.copy(alpha = alphaMultiplier).toArgb()
        maskFilter = BlurMaskFilter(radius.value, BlurMaskFilter.Blur.NORMAL)
    }

    Box(
        modifier = modifier.drawBehind {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawCircle(
                    size.width / 2,
                    size.height / 2,
                    size.width / 2,
                    paint
                )
            }
        }
    ) {
        content()
    }
}
