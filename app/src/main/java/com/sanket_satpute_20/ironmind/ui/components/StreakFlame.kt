package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.IronRed

/**
 * Animated streak flame counter.
 * Shows a pulsing 🔥 with the streak count.
 */
@Composable
fun StreakFlame(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")

    val scale by infiniteTransition.animateFloat(
        initialValue = if (streakCount > 0) 0.9f else 1.0f,
        targetValue = if (streakCount > 0) 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = if (streakCount > 0) 0.75f else 0.5f,
        targetValue = if (streakCount > 0) 1.0f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (streakCount > 0) "\uD83D\uDD25" else "\uD83D\uDD25\uFE0E", // emoji representation varies, but alpha grey makes it look dead
            fontSize = 22.sp,
            modifier = Modifier
                .scale(scale)
                .graphicsLayer { 
                    this.alpha = alpha 
                    if (streakCount == 0) {
                        // Greyscale effect for dead flame
                        this.colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            androidx.compose.ui.graphics.Color.Gray, 
                            androidx.compose.ui.graphics.BlendMode.SrcIn
                        )
                    }
                }
        )
        Spacer(modifier = Modifier.width(4.dp))
        AnimatedContent(
            targetState = streakCount,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                } else {
                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
            label = "StreakOdometer"
        ) { targetCount ->
            Text(
                text = "$targetCount",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (targetCount > 0) GoldXP else androidx.compose.ui.graphics.Color.Gray,
                    fontSize = 20.sp
                )
            )
        }
    }
}
