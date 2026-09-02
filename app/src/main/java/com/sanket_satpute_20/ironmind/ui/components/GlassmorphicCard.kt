package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.sanket_satpute_20.ironmind.ui.theme.BorderGlass
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceGlass
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceGlassDark
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

/**
 * Premium glassmorphism card with optional neon glow effect.
 * The foundational card component for all of IronMind's UI surfaces.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    padding: Dp = 16.dp,
    backgroundColor: Color? = null,
    elevation: Dp = 6.dp,
    glowColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // Shadow/glow layer — uses spotColor for the neon effect
    val shadowModifier = if (glowColor != null) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = glowColor.copy(alpha = 0.3f),
            spotColor = glowColor.copy(alpha = 0.6f)
        )
    } else {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = DeepBackground.copy(alpha = 0.72f),
            spotColor = DeepBackground.copy(alpha = 0.48f)
        )
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(
                if (backgroundColor != null) {
                    Brush.linearGradient(
                        listOf(
                            backgroundColor.copy(alpha = 0.25f),
                            backgroundColor.copy(alpha = 0.12f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(SurfaceGlass, SurfaceGlassDark)
                    )
                }
            )
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.linearGradient(
                        colors = listOf(
                            TextPrimary.copy(alpha = 0.20f),
                            TextPrimary.copy(alpha = 0.05f)
                        )
                    )
                ),
                shape
            )
            .padding(padding),
        content = content
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun GlassmorphicCardPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        GlassmorphicCard(glowColor = NeonCyan) {
            Text("GLASS SURFACE", color = TextPrimary)
        }
    }
}
