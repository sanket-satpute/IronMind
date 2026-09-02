package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val shimmerColors = listOf(
    Color.White.copy(alpha = 0.03f),
    Color.White.copy(alpha = 0.12f),
    Color.White.copy(alpha = 0.03f)
)

/**
 * Animated shimmer skeleton placeholder for loading states.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    cornerRadius: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 600f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush)
    )
}

/**
 * A pre-built shimmer skeleton for a typical task card layout.
 */
@Composable
fun TaskCardShimmer(modifier: Modifier = Modifier) {
    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Column {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f), height = 16.dp)
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f), height = 12.dp)
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 12.dp)
        }
    }
}

/** Full-screen loading treatment for screens whose data is still being resolved. */
@Composable
fun PremiumScreenShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.42f), height = 18.dp, cornerRadius = 12.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.72f), height = 12.dp, cornerRadius = 8.dp)
        Spacer(modifier = Modifier.height(16.dp))
        TaskCardShimmer()
        TaskCardShimmer()
        ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 160.dp, cornerRadius = 20.dp)
    }
}
