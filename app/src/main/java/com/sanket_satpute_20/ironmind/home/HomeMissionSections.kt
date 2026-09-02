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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.sanket_satpute_20.ironmind.gamification.LocalHapticsManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.IronRed
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
//  Section header — neon accent bar + title + count badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun HomeAuxSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 0.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, HomeOutline.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            title,
            color = HomeTextPrimary.copy(alpha = 0.72f),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(subtitle, color = HomeTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
fun MissionSectionHeader(
    title: String,
    subtitle: String,
    accent: Color,
    count: Int,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    val infinite = rememberInfiniteTransition(label = "msection_glow")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "mheader_glow"
    )

    Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent.copy(alpha = glowAlpha))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.6.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Count badge with glow
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f))
                    .drawBehind {
                        drawCircle(color = accent.copy(alpha = glowAlpha * 0.20f))
                    }
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    count.toString(),
                    color = accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (trailingText != null && onTrailingClick != null) {
                TextButton(
                    onClick = onTrailingClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        trailingText,
                        color = accent.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(subtitle, color = HomeTextSecondary, fontSize = 11.sp, lineHeight = 17.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Resolved mission summary card — glassmorphic with trophy + counts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ResolvedMissionSummaryCard(completedCount: Int, skippedCount: Int, onExpand: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "resolved_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(HomeSurface)
            .drawBehind {
                // Top accent highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, SuccessGreen.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onExpand)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Trophy icon bg
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏆", fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "ARCHIVED TODAY",
                    color = HomeTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallStatusChip("$completedCount SECURED", SuccessGreen)
                    SmallStatusChip("$skippedCount BURIED", ErrorRed)
                }
            }
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = HomeTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SmallStatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Action icon button — glass circle with neon tint
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ActionIconButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "action_icon_scale"
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = if (pressed) 0.25f else 0.14f))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TaskCardStateRow — state pill + supporting label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TaskCardStateRow(stateLabel: String, stateColor: Color, supportingLabel: String?) {
    val isLive = stateLabel.contains("LIVE") || stateLabel.contains("NOW")
    val infinite = rememberInfiniteTransition(label = "state_dot")
    val dotAlpha by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot_alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // State pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(stateColor.copy(alpha = 0.15f))
                .drawBehind {
                    if (isLive) {
                        drawRoundRect(
                            color = stateColor.copy(alpha = dotAlpha * 0.3f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(50f)
                        )
                    }
                }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(stateColor.copy(alpha = dotAlpha))
                    )
                }
                Text(
                    stateLabel,
                    color = stateColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        supportingLabel?.let { label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    label,
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TaskScheduleRail — time column with state accent line
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TaskScheduleRail(
    startTime: String,
    endTime: String,
    isResolvedMission: Boolean,
    isSkipped: Boolean,
    compact: Boolean
) {
    val railColor = when {
        isSkipped -> ErrorRed.copy(alpha = 0.4f)
        isResolvedMission -> SuccessGreen.copy(alpha = 0.5f)
        else -> HomeFocus.copy(alpha = 0.6f)
    }

    Box(modifier = Modifier.width(if (compact) 72.dp else 82.dp)) {
        // Left accent line
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(railColor, railColor.copy(alpha = 0.15f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 2.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                startTime,
                style = if (isResolvedMission)
                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSkipped) HomeTextSecondary.copy(alpha = 0.6f) else HomeTextPrimary,
                maxLines = 1
            )
            Text(
                if (isResolvedMission) "until $endTime" else endTime,
                style = MaterialTheme.typography.labelSmall,
                color = HomeTextSecondary,
                maxLines = 1
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TaskDailyCueCard — glassmorphic cue with cyan glow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TaskDailyCueCard(cueLabel: String, cue: String, compact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(NeonCyan.copy(alpha = 0.07f), HomeSurface)
                )
            )
            .drawBehind {
                // Cyan glow border top
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, NeonCyan.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                )
                Text(
                    cueLabel,
                    color = NeonCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.9.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                cue,
                color = Color.White.copy(alpha = 0.84f),
                fontSize = 11.sp,
                lineHeight = 17.sp,
                maxLines = if (compact) 3 else 2
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TaskResolutionRail — MARK DONE / SKIP TASK row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TaskResolutionRail(
    isDailyMicroAction: Boolean,
    compactActionLabels: Boolean,
    onDone: () -> Unit,
    onSkip: () -> Unit
) {
    val haptic = LocalHapticsManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // DONE button — green glass pill
        ResolutionButton(
            label = if (compactActionLabels) "DONE" else "MARK DONE",
            icon = Icons.Rounded.CheckCircle,
            color = SuccessGreen,
            modifier = Modifier.weight(1f),
            onClick = { haptic.playSuccess(); onDone() }
        )

        if (!isDailyMicroAction) {
            // SKIP button — red ghost pill
            ResolutionButton(
                label = if (compactActionLabels) "SKIP" else "SKIP TASK",
                icon = Icons.Rounded.Cancel,
                color = ErrorRed,
                modifier = Modifier.weight(1f),
                onClick = { haptic.playError(); onSkip() }
            )
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "PROTECTED",
                    color = HomeTextSecondary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.7.sp
                )
            }
        }
    }
}

@Composable
private fun ResolutionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "res_btn_scale"
    )
    Box(
        modifier = modifier
            .height(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = if (pressed) 0.22f else 0.12f))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(label, color = color, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TaskLaunchActions — START TASK / POMODORO buttons
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TaskLaunchActions(
    isDailyMicroAction: Boolean,
    isInProgress: Boolean,
    isVeryCompactCard: Boolean,
    dailyActionMode: String?,
    showStartButton: Boolean,
    compactActionLabels: Boolean,
    showChainLevelResolution: Boolean,
    showPomodoroSecondary: Boolean,
    onOpenLiveTask: () -> Unit,
    onStartCountdown: () -> Unit,
    onStartPomodoro: () -> Unit
) {
    val hapticLaunch = LocalHapticsManager.current
    val primaryColor = when {
        isInProgress -> SuccessGreen
        showStartButton -> WarningAmber
        else -> HomeFocus
    }
    val primaryContentColor = when {
        isInProgress || showStartButton -> Color.Black
        else -> HomeCanvas
    }

    if (isDailyMicroAction) {
        GradientLaunchButton(
            label = when {
                isInProgress && isVeryCompactCard -> "LIVE"
                isInProgress -> "OPEN LIVE TASK"
                dailyActionMode == "VOICE_REP" && isVeryCompactCard -> "REP"
                dailyActionMode == "VOICE_REP" -> "RECORD REP"
                dailyActionMode == "PREP_FOCUS" && isVeryCompactCard -> "PREP"
                dailyActionMode == "PREP_FOCUS" -> "START PREP"
                dailyActionMode == "PHYSICAL_RESET" && isVeryCompactCard -> "RESET"
                dailyActionMode == "PHYSICAL_RESET" -> "START RESET"
                isVeryCompactCard -> "START"
                else -> "START TASK"
            },
            primaryColor = primaryColor,
            contentColor = primaryContentColor,
            icon = if (isInProgress) Icons.Rounded.PlayArrow else Icons.Rounded.Bolt,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            onClick = { hapticLaunch.playError(); if (isInProgress) onOpenLiveTask() else onStartCountdown() }
        )
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientLaunchButton(
                label = when {
                    isInProgress && isVeryCompactCard -> "LIVE"
                    isInProgress -> "OPEN LIVE TASK"
                    isVeryCompactCard -> "START"
                    else -> "START TASK"
                },
                primaryColor = primaryColor,
                contentColor = primaryContentColor,
                icon = if (isInProgress) Icons.Rounded.PlayArrow else Icons.Rounded.Bolt,
                modifier = Modifier.weight(1f).height(50.dp),
                onClick = { hapticLaunch.playError(); if (isInProgress) onOpenLiveTask() else onStartCountdown() }
            )
            if (showPomodoroSecondary) {
                PomodoroButton(
                    compact = compactActionLabels || showChainLevelResolution,
                    onClick = onStartPomodoro
                )
            }
        }
    }
}

@Composable
private fun GradientLaunchButton(
    label: String,
    primaryColor: Color,
    contentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "launch_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(primaryColor, primaryColor.copy(alpha = 0.75f))
                )
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Text(label, color = contentColor, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 0.4.sp)
        }
    }
}

@Composable
private fun PomodoroButton(compact: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "pomo_scale"
    )
    Box(
        modifier = Modifier
            .widthIn(min = if (compact) 50.dp else 88.dp)
            .height(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(ElectricViolet.copy(alpha = if (pressed) 0.18f else 0.10f))
            .drawBehind {
                drawRoundRect(
                    color = ElectricViolet.copy(alpha = 0.28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(16.dp))
            if (!compact) {
                Text("POMODORO", color = ElectricViolet, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TaskChainProgressCard — expandable chain view
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TaskChainProgressCard(
    linkedTasks: List<Task>,
    isCompactCard: Boolean,
    showChainLevelResolution: Boolean,
    chainExpanded: Boolean,
    onToggle: () -> Unit,
    onDoneLinkedTask: ((Task) -> Unit)?,
    onSkipLinkedTask: ((Task) -> Unit)?
) {
    val unresolvedLinkedCount = linkedTasks.count { !it.isBreakSegment && !it.isCompleted && !it.isSkipped }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "chain_card_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(HomeSurface)
            .drawBehind {
                drawLine(
                    color = HomeFocus.copy(alpha = 0.18f),
                    start = Offset(0f, 0f), end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CHAIN PROGRESS", color = HomeTextPrimary, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(HomeFocus.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text("$unresolvedLinkedCount open", color = HomeFocus, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
            Icon(
                if (chainExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = HomeTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(
            visible = chainExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HomeOutline))
                Spacer(modifier = Modifier.height(10.dp))
                linkedTasks
                    .sortedBy { parseHomeTime(it.startTime) ?: LocalTime.MAX }
                    .forEach { linkedTask ->
                        val linkedChipColor = when {
                            linkedTask.isCompleted -> SuccessGreen
                            linkedTask.isSkipped -> ErrorRed
                            linkedTask.isBreakSegment -> SuccessGreen
                            else -> HomeFocus
                        }
                        val linkedStatusLabel = when {
                            linkedTask.isCompleted -> "DONE"
                            linkedTask.isSkipped -> "SKIPPED"
                            linkedTask.isBreakSegment -> "BREAK"
                            else -> "PENDING"
                        }
                        val showLinkedTaskActions = !linkedTask.isBreakSegment && !linkedTask.isCompleted &&
                            !linkedTask.isSkipped && !showChainLevelResolution

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(6.dp).clip(CircleShape)
                                        .background(linkedChipColor)
                                )
                                Column {
                                    Text(linkedTask.name, color = HomeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text("${linkedTask.startTime} – ${linkedTask.endTime}", color = HomeTextSecondary, fontSize = 10.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(linkedChipColor.copy(alpha = 0.14f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(linkedStatusLabel, color = linkedChipColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                                if (showLinkedTaskActions) {
                                    ActionIconButton(Icons.Rounded.CheckCircle, SuccessGreen) { onDoneLinkedTask?.invoke(linkedTask) }
                                    ActionIconButton(Icons.Rounded.Cancel, ErrorRed.copy(alpha = 0.6f)) { onSkipLinkedTask?.invoke(linkedTask) }
                                }
                            }
                        }
                    }
            }
        }

        if (!chainExpanded) {
            Text(
                "${linkedTasks.count { !it.isBreakSegment }} focus parts, $unresolvedLinkedCount unresolved.",
                color = HomeTextSecondary,
                fontSize = 11.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ChainResolutionActions — complete chain / skip chain
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ChainResolutionActions(
    isCompactCard: Boolean,
    onDoneChain: () -> Unit,
    onSkipChain: () -> Unit
) {
    val arrangement = if (isCompactCard) Arrangement.spacedBy(10.dp) else Arrangement.spacedBy(10.dp)
    if (isCompactCard) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ChainActionButton("COMPLETE CHAIN", Icons.Rounded.CheckCircle, SuccessGreen, Modifier.fillMaxWidth(), onDoneChain)
            ChainActionButton("SKIP CHAIN", Icons.Rounded.Cancel, ErrorRed, Modifier.fillMaxWidth(), onSkipChain)
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = arrangement) {
            ChainActionButton("COMPLETE CHAIN", Icons.Rounded.CheckCircle, SuccessGreen, Modifier.weight(1f), onDoneChain)
            ChainActionButton("SKIP CHAIN", Icons.Rounded.Cancel, ErrorRed, Modifier.weight(1f), onSkipChain)
        }
    }
}

@Composable
private fun ChainActionButton(label: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "chain_btn_scale"
    )
    Box(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (pressed) 0.18f else 0.10f))
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Text(label, color = color, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Utility helpers — used across files
// ─────────────────────────────────────────────────────────────────────────────

internal fun parseHomeTime(timeStr: String): LocalTime? {
    val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
    for (format in formats) {
        runCatching {
            return LocalTime.parse(timeStr.uppercase(), DateTimeFormatter.ofPattern(format, Locale.US))
        }
    }
    return null
}

internal fun isRecentDate(rawDate: String, days: Long): Boolean {
    if (rawDate.isBlank()) return false
    return runCatching {
        val recordedDate = LocalDate.parse(rawDate)
        !recordedDate.isBefore(LocalDate.now().minusDays(days))
    }.getOrDefault(false)
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun MissionSectionHeaderPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        MissionSectionHeader(
            title = "ACTIVE MISSIONS",
            subtitle = "3 tasks remaining",
            accent = IronRed,
            count = 3
        )
    }
}