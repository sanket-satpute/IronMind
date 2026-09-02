package com.sanket_satpute_20.ironmind.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.gamification.SoundManager
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.ui.components.StreakFlame

// ─────────────────────────────────────────────────────────────────────────────
//  GamifiedHeader  — Premium Redesign
//
//  Visual architecture:
//    ┌──────────────────────────────────────────────────────────┐
//    │  [Avatar Ring / Level Orb]  [Identity Title + Mode Pill] │  [Stats] [Settings]
//    │  Animated neon ring around level number                   │
//    ├──────────────────────────────────────────────────────────┤
//    │  ┌─────────────────────────────────────────────────────┐ │
//    │  │  🔥 streak · ⚡ XP amount · Mode glyph   [ICON BTN]│ │
//    │  └─────────────────────────────────────────────────────┘ │
//    ├──────────────────────────────────────────────────────────┤
//    │  [═══════════════████░░░░░░░░░░░]  XP progress bar      │
//    └──────────────────────────────────────────────────────────┘
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GamifiedHeader(
    level: Int,
    progress: Float,
    totalXp: Long,
    streak: Int,
    identityTitle: String,
    currentMode: AppMode,
    modifier: Modifier = Modifier,
    onGraveyardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    val levelStartXp = com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine.getXpForLevel(level)
    val nextLevelXp = com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine.getXpForLevel(level + 1)
    val currentXP = (totalXp - levelStartXp).coerceAtLeast(0L).toInt()
    val levelRangeXp = (nextLevelXp - levelStartXp).coerceAtLeast(1L).toInt()
    val modeColor = Color(currentMode.color)

    // Infinite pulse for the avatar ring glow
    val infiniteTransition = rememberInfiniteTransition(label = "header_pulse")
    val ringGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_glow"
    )
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1.00f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )

    // Background decorative particle orbit
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // ─── ROW 1: Avatar orb + Identity info + Action buttons ──────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Level Orb with animated neon ring
            PremiumLevelOrb(
                level = level,
                modeColor = modeColor,
                ringGlowAlpha = ringGlowAlpha,
                ringScale = ringScale,
                orbitAngle = orbitAngle
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Identity title + mode badge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = identityTitle,
                    color = HomeTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1
                )
                // Mode badge — glowing pill
                GlowingModeBadge(currentMode = currentMode, modeColor = modeColor, ringGlowAlpha = ringGlowAlpha)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action buttons column
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderUtilityButton(
                    icon = Icons.Rounded.Analytics,
                    contentDescription = "Open statistics",
                    tint = HomeFocus,
                    onClick = onStatsClick
                )
                HeaderUtilityButton(
                    icon = Icons.Rounded.SettingsApplications,
                    contentDescription = "Open settings",
                    tint = HomeTextSecondary,
                    onClick = onSettingsClick
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── ROW 2: Stats glass bar — streak · XP · archive ──────────────────
        StatsGlassBar(
            streak = streak,
            totalXp = totalXp,
            currentMode = currentMode,
            modeColor = modeColor,
            onGraveyardClick = onGraveyardClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ─── ROW 3: XP progress bar ────────────────────────────────────────────
        PremiumXPBar(
            currentXP = currentXP,
            maxXP = levelRangeXp,
            progress = progress,
            level = level,
            modeColor = modeColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Premium Level Orb with animated neon ring + orbit particle
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PremiumLevelOrb(
    level: Int,
    modeColor: Color,
    ringGlowAlpha: Float,
    ringScale: Float,
    orbitAngle: Float
) {
    Box(
        modifier = Modifier.size(62.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer orbit particle (tiny dot circling)
        Canvas(modifier = Modifier.size(62.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val orbitRadius = size.width / 2f - 2.dp.toPx()
            val rad = Math.toRadians(orbitAngle.toDouble())
            val px = (cx + orbitRadius * Math.cos(rad)).toFloat()
            val py = (cy + orbitRadius * Math.sin(rad)).toFloat()
            // Glow halo under orbit dot
            drawCircle(color = modeColor.copy(alpha = 0.22f), radius = 8.dp.toPx(), center = Offset(px, py))
            drawCircle(color = modeColor, radius = 3.dp.toPx(), center = Offset(px, py))
        }

        // Neon ring track
        Canvas(
            modifier = Modifier
                .size(58.dp)
                .scale(ringScale)
        ) {
            val strokePx = 2.5.dp.toPx()
            val diameter = minOf(size.width, size.height) - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)
            // Background track (dim)
            drawArc(
                color = modeColor.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            // Glowing arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        modeColor.copy(alpha = ringGlowAlpha * 0.6f),
                        modeColor.copy(alpha = ringGlowAlpha),
                        modeColor
                    )
                ),
                startAngle = -90f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Core orb
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            modeColor.copy(alpha = 0.28f),
                            HomeSurface
                        )
                    )
                )
                .drawBehind {
                    // Inner glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(modeColor.copy(alpha = ringGlowAlpha * 0.35f), Color.Transparent),
                            radius = size.minDimension * 0.7f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$level",
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = modeColor,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Glowing mode badge pill
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GlowingModeBadge(currentMode: AppMode, modeColor: Color, ringGlowAlpha: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(modeColor.copy(alpha = 0.13f))
            .drawBehind {
                drawRoundRect(
                    color = modeColor.copy(alpha = ringGlowAlpha * 0.22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(50f)
                )
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Blinking status dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(modeColor.copy(alpha = ringGlowAlpha))
            )
            Text(
                text = "${currentMode.emoji} ${currentMode.label.uppercase()}",
                color = modeColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stats glass bar  — streak · XP · archive shortcut
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsGlassBar(
    streak: Int,
    totalXp: Long,
    currentMode: AppMode,
    modeColor: Color,
    onGraveyardClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        HomeSurface,
                        HomeSurfaceRaised,
                        HomeSurface
                    )
                )
            )
            .drawBehind {
                // Subtle top-edge highlight line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Streak section
        StatPill(
            emoji = "🔥",
            value = if (streak > 0) "$streak" else "0",
            label = "STREAK",
            valueColor = com.sanket_satpute_20.ironmind.ui.theme.GoldXP
        )

        // Thin divider
        Box(
            modifier = Modifier
                .height(28.dp)
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )

        // XP section
        StatPill(
            emoji = "⚡",
            value = formatXP(totalXp),
            label = "TOTAL XP",
            valueColor = HomeFocus
        )

        // Thin divider
        Box(
            modifier = Modifier
                .height(28.dp)
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )

        // Archive shortcut — small mode-colored chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(modeColor.copy(alpha = 0.12f))
                .clickable(onClick = onGraveyardClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("📋", fontSize = 13.sp)
                Text(
                    text = "ARCHIVE",
                    color = modeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    emoji: String,
    value: String,
    label: String,
    valueColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Column {
            Text(
                text = value,
                color = valueColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = label,
                color = HomeTextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }
    }
}

private fun formatXP(xp: Long): String = when {
    xp >= 1_000_000 -> "${xp / 1_000_000}M"
    xp >= 1_000     -> "${xp / 1_000}K"
    else             -> "$xp"
}

// ─────────────────────────────────────────────────────────────────────────────
//  Premium XP progress bar with neon glow shimmer
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PremiumXPBar(
    currentXP: Int,
    maxXP: Int,
    progress: Float,
    level: Int,
    modeColor: Color
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) { animatedProgress = progress.coerceIn(0f, 1f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "xp_bar_anim"
    )

    // Shimmer sweep
    val infiniteTransition = rememberInfiniteTransition(label = "xp_shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Level badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HomeFocus.copy(alpha = 0.16f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LVL $level",
                        color = HomeFocus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp
                    )
                }
                Text(
                    text = "NEXT RANK",
                    color = HomeTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "$currentXP / $maxXP XP",
                color = HomeTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Bar track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.07f))
        ) {
            // Filled portion with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedValue.coerceAtLeast(0.02f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                modeColor.copy(alpha = 0.85f),
                                HomeFocus
                            )
                        )
                    )
                    .drawBehind {
                        // Shimmer overlay
                        val shimmerOffset = shimmerX * size.width
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                ),
                                startX = shimmerOffset,
                                endX = shimmerOffset + size.width * 0.4f
                            )
                        )
                        // Glow underneath bar — drawn as a wider, blurred rect
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    HomeFocus.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )

            // Leading glow tip
            if (animatedValue > 0.02f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedValue)
                        .height(10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Utility button — compact icon pill, press-animated
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderUtilityButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btn_press_scale"
    )
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    Surface(
        modifier = modifier
            .size(42.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (!isPreview) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    SoundManager.getInstance(context).playTimerTick()
                }
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        color = if (pressed) HomeSurfaceRaised else HomeSurface,
        border = BorderStroke(
            width = 1.dp,
            brush = if (pressed) {
                Brush.linearGradient(listOf(tint.copy(alpha = 0.5f), tint.copy(alpha = 0.2f)))
            } else {
                Brush.linearGradient(listOf(HomeOutline, HomeOutline))
            }
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (pressed) tint else HomeTextSecondary.copy(alpha = 0.85f),
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E13)
@androidx.compose.runtime.Composable
fun GamifiedHeaderPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        GamifiedHeader(
            level = 5,
            progress = 0.7f,
            totalXp = 3_240,
            streak = 12,
            identityTitle = "Iron Operator",
            currentMode = AppMode.IRON,
            modifier = Modifier.padding(16.dp),
            onGraveyardClick = {},
            onSettingsClick = {},
            onStatsClick = {}
        )
    }
}
