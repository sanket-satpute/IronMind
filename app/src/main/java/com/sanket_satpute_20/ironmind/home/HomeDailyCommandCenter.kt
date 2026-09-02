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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

// ─────────────────────────────────────────────────────────────────────────────
//  HomeDailyCommandCenter — Premium Command Deck Hero Card
//
//  ┌──────────────────────────────────────────────────────┐
//  │ accent gradient top glow                             │
//  │  [Completion Ring 62dp]  NEXT MISSION                │
//  │                          2 / 5 missions complete     │
//  │  Supporting context text                             │
//  │  [══════ PRIMARY CTA BUTTON ══════] pulsing gradient │
//  │  [ADD MISSION]         [QUICK FOCUS]                 │
//  └──────────────────────────────────────────────────────┘
// ─────────────────────────────────────────────────────────────────────────────

/** The actionable, above-the-fold status for the user's current day. */
@Composable
internal fun HomeDailyCommandCenter(
    doNowMission: HomeMissionItem?,
    tasks: List<Task>,
    allDone: Boolean,
    isRecoveryDayActive: Boolean,
    onPrimaryAction: () -> Unit,
    onAddMission: () -> Unit,
    onQuickFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusTasks = tasks.filterNot { it.isBreakSegment }
    val completedCount = focusTasks.count { it.isCompleted }
    val pendingCount = focusTasks.count { !it.isCompleted && !it.isSkipped }
    val totalCount = focusTasks.size
    val completion = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val isLive = doNowMission?.task?.isInProgress == true

    val accent = when {
        isRecoveryDayActive -> HomeFocus
        allDone -> HomeReward
        doNowMission?.state == HomeMissionState.OVERDUE -> ErrorRed
        isLive -> SuccessGreen
        else -> HomeAction
    }

    val title = when {
        isRecoveryDayActive -> "RECOVERY DAY ACTIVE"
        allDone -> "DAY SECURED"
        doNowMission != null -> if (isLive) "MISSION LIVE" else "NEXT MISSION"
        else -> "PLAN THE DAY"
    }
    val primaryLabel = when {
        isRecoveryDayActive -> "PROTECT YOUR RECOVERY"
        allDone -> "REVIEW TODAY'S PROOF"
        doNowMission == null -> "START QUICK FOCUS"
        isLive -> "OPEN ${doNowMission.displayName}"
        doNowMission.task.focusModeEnabled -> "START ${doNowMission.displayName}"
        else -> "BEGIN ${doNowMission.displayName}"
    }
    val supporting = when {
        isRecoveryDayActive -> "Penalties and blockers are paused. Rest deliberately."
        allDone -> "$completedCount of $totalCount missions secured. Keep the streak clean."
        doNowMission != null -> "$pendingCount mission${if (pendingCount == 1) "" else "s"} remain · ${doNowMission.displayName} is your best next move."
        else -> "Add your first mission, or enter a focus run without planning one."
    }

    // Pulse animation for live / active state
    val transition = rememberInfiniteTransition(label = "commandPulse")
    val primaryScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (doNowMission != null && !isRecoveryDayActive && !allDone) 1.013f else 1f,
        animationSpec = infiniteRepeatable(tween(1_600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "commandPrimaryScale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.25f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1_400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "card_glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(HomeSurface)
            .drawBehind {
                // Full-card ambient glow (accent colour radiating from top edge)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = glowAlpha * 0.55f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.45f
                    )
                )
                // Top highlight line
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, accent.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Status row ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                CommandCompletionRing(
                    progress = completion,
                    color = accent,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    allDone = allDone
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Title chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accent.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isLive || (doNowMission?.state == HomeMissionState.LIVE_NOW)) {
                                Box(
                                    modifier = Modifier.size(6.dp).clip(CircleShape)
                                        .background(accent.copy(alpha = glowAlpha + 0.4f))
                                )
                            }
                            Text(
                                title,
                                color = accent,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 1.4.sp
                            )
                        }
                    }
                    Text(
                        if (totalCount == 0) "No missions assigned yet"
                        else "$completedCount / $totalCount missions complete",
                        color = HomeTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
                if (allDone) {
                    Icon(
                        Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = HomeReward,
                        modifier = Modifier.size(30.dp).graphicsLayer { rotationZ = -5f }
                    )
                }
            }

            // ── Supporting text ────────────────────────────────────────────
            Text(
                supporting,
                color = HomeTextSecondary,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )

            // ── Primary CTA ────────────────────────────────────────────────
            CommandPrimaryButton(
                label = primaryLabel,
                accent = accent,
                allDone = allDone,
                pulseScale = primaryScale,
                onClick = onPrimaryAction
            )

            // ── Secondary row ──────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CommandSecondaryButton(
                    label = "ADD MISSION",
                    icon = Icons.Rounded.Add,
                    color = HomeFocus,
                    modifier = Modifier.weight(1f).height(48.dp),
                    onClick = onAddMission
                )
                CommandSecondaryButton(
                    label = "QUICK FOCUS",
                    icon = Icons.Rounded.PlayArrow,
                    color = HomeTextPrimary,
                    modifier = Modifier.weight(1f).height(48.dp),
                    onClick = onQuickFocus
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Completion ring — animated Canvas arc
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CommandCompletionRing(
    progress: Float,
    color: Color,
    completedCount: Int,
    totalCount: Int,
    allDone: Boolean
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) { animatedProgress = progress }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "ring_anim"
    )
    val infinite = rememberInfiniteTransition(label = "ring_glow")
    val glowRadius by infinite.animateFloat(
        initialValue = 0.8f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_glow_radius"
    )

    Box(modifier = Modifier.size(62.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(62.dp)) {
            val strokePx = 5.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(inset, inset)

            // Background glow
            drawCircle(color = color.copy(alpha = 0.10f * glowRadius), radius = size.minDimension / 2f)

            // Track
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            // Progress arc
            if (animatedValue > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Transparent, color.copy(alpha = 0.6f), color),
                        center = Offset(size.width / 2f, size.height / 2f)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedValue,
                    useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        if (allDone) {
            Text("✓", color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
        } else {
            Text(
                "${(progress * 100).toInt()}%",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Primary CTA button with gradient fill + press spring
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CommandPrimaryButton(
    label: String,
    accent: Color,
    allDone: Boolean,
    pulseScale: Float,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else pulseScale,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "cta_press_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(accent, accent.copy(alpha = 0.75f))
                )
            )
            .drawBehind {
                // Bottom glow shadow
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, accent.copy(alpha = 0.30f)),
                        startY = size.height * 0.6f, endY = size.height
                    )
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                if (allDone) Icons.Rounded.CheckCircle else Icons.Rounded.Bolt,
                contentDescription = null,
                tint = HomeCanvas,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                color = HomeCanvas,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.4.sp,
                maxLines = 1
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Secondary outlined button — glassmorphic neon border
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CommandSecondaryButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "sec_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(HomeSurfaceRaised)
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = if (pressed) 0.50f else 0.25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(17.dp))
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun HomeDailyCommandCenterEmptyPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HomeDailyCommandCenter(null, emptyList(), false, false, {}, {}, {}, Modifier.padding(16.dp))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun HomeDailyCommandCenterMissionPreview() {
    val task = Task(id = 1, name = "Ship the project brief", startTime = "10:00", endTime = "11:30", focusModeEnabled = true)
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HomeDailyCommandCenter(
            doNowMission = HomeMissionItem(task, HomeMissionState.UP_NEXT, "UP NEXT"),
            tasks = listOf(task, Task(id = 2, name = "Workout", isCompleted = true)),
            allDone = false, isRecoveryDayActive = false,
            onPrimaryAction = {}, onAddMission = {}, onQuickFocus = {}, modifier = Modifier.padding(16.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun HomeDailyCommandCenterCompletePreview() {
    val task = Task(id = 1, name = "Deep work", isCompleted = true)
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HomeDailyCommandCenter(null, listOf(task), true, false, {}, {}, {}, Modifier.padding(16.dp))
    }
}
