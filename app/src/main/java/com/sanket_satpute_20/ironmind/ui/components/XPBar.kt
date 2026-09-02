package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceGlass
import com.sanket_satpute_20.ironmind.ui.theme.TextMuted
import com.sanket_satpute_20.ironmind.ui.theme.TextSecondary

/**
 * Glowing animated XP progress bar.
 * Shows current XP and level with neon glow fill animation.
 */
@Composable
fun XPBar(
    currentXP: Int,
    maxXP: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (maxXP > 0) currentXP.toFloat() / maxXP else 0f
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) { animatedProgress = progress }

    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow),
        label = "XPBarAnimation"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LVL $level",
                style = MaterialTheme.typography.labelLarge,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$currentXP / $maxXP XP",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceGlass)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedValue)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NeonCyan, GoldXP)
                        )
                    )
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = NeonCyan,
                        spotColor = NeonCyan
                    )
            )
        }
    }
}
