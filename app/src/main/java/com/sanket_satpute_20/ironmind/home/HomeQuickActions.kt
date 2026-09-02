package com.sanket_satpute_20.ironmind.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
//  HomeQuickActions — Premium gamified action cards
//
//  Three density modes:
//    FULL       → gradient icon bg + title + description + neon border
//    COMPACT    → icon + title stacked, glass bg
//    ICON_ONLY  → neon ring orbiting icon circle
// ─────────────────────────────────────────────────────────────────────────────

internal data class HomeQuickActionSpec(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val title: String,
    val description: String,
    val badge: String?,
    val surfaceColor: Color,
    val borderColor: Color,
    val onClick: () -> Unit
)

private enum class HomeQuickActionDensity { ICON_ONLY, COMPACT, FULL }

@Composable
internal fun HomeQuickActionsRow(
    actions: List<HomeQuickActionSpec>,
    modifier: Modifier = Modifier
) {
    if (actions.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = 12.dp
        val cardWidth = (maxWidth - (spacing * (actions.size - 1).coerceAtLeast(0))) / actions.size
        val density = when {
            cardWidth < 100.dp -> HomeQuickActionDensity.ICON_ONLY
            cardWidth < 155.dp -> HomeQuickActionDensity.COMPACT
            else               -> HomeQuickActionDensity.FULL
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            actions.forEach { action ->
                HomeQuickActionCard(
                    action = action,
                    density = density,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HomeQuickActionCard(
    action: HomeQuickActionSpec,
    density: HomeQuickActionDensity,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "qa_card_scale"
    )

    // Ambient glow pulse for the border
    val infiniteTransition = rememberInfiniteTransition(label = "qa_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "qa_glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(action.surfaceColor)
            .drawBehind {
                // Neon border glow
                drawRoundRect(
                    color = action.borderColor.copy(alpha = if (pressed) glowAlpha * 0.9f else glowAlpha * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
                // Top shimmer line
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, action.iconTint.copy(alpha = 0.20f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
                // Radial ambient glow from icon area (top-left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(action.iconTint.copy(alpha = if (pressed) 0.18f else 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.25f, size.height * 0.2f),
                        radius = size.width * 0.65f
                    )
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = action.onClick)
    ) {
        when (density) {
            HomeQuickActionDensity.ICON_ONLY -> IconOnlyContent(action, glowAlpha)
            HomeQuickActionDensity.COMPACT   -> CompactContent(action)
            HomeQuickActionDensity.FULL      -> FullContent(action, glowAlpha)
        }
    }
}

// ─── ICON ONLY ────────────────────────────────────────────────────────────────

@Composable
private fun IconOnlyContent(action: HomeQuickActionSpec, glowAlpha: Float) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        // Orbiting neon ring
        androidx.compose.foundation.Canvas(modifier = Modifier.size(50.dp)) {
            val strokePx = 1.8.dp.toPx()
            val inset = strokePx / 2f
            drawArc(
                color = action.iconTint.copy(alpha = 0.12f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - strokePx, size.height - strokePx),
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, action.iconTint.copy(alpha = glowAlpha * 0.6f), action.iconTint.copy(alpha = glowAlpha))
                ),
                startAngle = -90f, sweepAngle = 260f, useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - strokePx, size.height - strokePx),
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )
        }
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(action.iconBackground, action.iconBackground.copy(alpha = 0.5f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, contentDescription = action.title, tint = action.iconTint, modifier = Modifier.size(20.dp))
        }
    }
}

// ─── COMPACT ─────────────────────────────────────────────────────────────────

@Composable
private fun CompactContent(action: HomeQuickActionSpec) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(action.iconBackground, action.iconBackground.copy(alpha = 0.4f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, contentDescription = action.title, tint = action.iconTint, modifier = Modifier.size(19.dp))
        }
        Text(
            text = action.title.uppercase(),
            color = action.iconTint,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 0.7.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ─── FULL ─────────────────────────────────────────────────────────────────────

@Composable
private fun FullContent(action: HomeQuickActionSpec, glowAlpha: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 118.dp)
            .padding(horizontal = 15.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gradient icon bubble
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    action.iconTint.copy(alpha = 0.30f),
                                    action.iconBackground
                                )
                            )
                        )
                        .drawBehind {
                            drawRoundRect(
                                color = action.iconTint.copy(alpha = glowAlpha * 0.35f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(action.icon, contentDescription = action.title, tint = action.iconTint, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = action.title.uppercase(),
                        color = action.iconTint,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.85.sp,
                        maxLines = 1
                    )
                    action.badge?.let { badge ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(action.iconTint.copy(alpha = 0.14f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                badge.uppercase(),
                                color = action.iconTint,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = action.description,
            color = HomeTextSecondary,
            fontSize = 11.sp,
            lineHeight = 17.sp,
            maxLines = 3
        )
    }
}
