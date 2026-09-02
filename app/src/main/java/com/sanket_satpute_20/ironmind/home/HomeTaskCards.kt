package com.sanket_satpute_20.ironmind.home

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.focus.getFocusColor
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
//  TaskCardPremium — Gamified mission card
//
//  Visual anatomy:
//  ┌─┬──────────────────────────────────────────────────────┐
//  │█│  [STATE PILL 🟢 LIVE NOW]  [supporting label]        │
//  │█│  ──────────────────────────────────────────          │
//  │█│  [TimeRail │  Task Name ●live dot]                   │
//  │█│             [tag chips row]                          │
//  │█│             [meta: type · duration]                  │
//  │█│             [Daily cue card if any]                  │
//  │█│             [Focus score chip if done]               │
//  │█│  [Launch actions row]                                │
//  │█│  [Resolution rail]                                   │
//  └─┴──────────────────────────────────────────────────────┘
//   ↑ left-edge accent bar (state color)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCardPremium(
    missionItem: HomeMissionItem,
    index: Int = 0,
    currentTime: LocalTime,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onStartCountdown: () -> Unit,
    onStartPomodoro: () -> Unit,
    onOpenLiveTask: () -> Unit,
    onDoneLinkedTask: ((Task) -> Unit)? = null,
    onSkipLinkedTask: ((Task) -> Unit)? = null,
    onDoneChain: (() -> Unit)? = null,
    onSkipChain: (() -> Unit)? = null
) {
    val task = missionItem.task
    val taskStart = parseHomeTime(task.startTime)
    val taskEnd = parseHomeTime(task.endTime)
    val minutesUntilStart = taskStart?.let { ChronoUnit.MINUTES.between(currentTime, it) } ?: Long.MAX_VALUE
    val isInProgress = missionItem.state == HomeMissionState.LIVE_NOW
    val isResolvedMission = missionItem.state == HomeMissionState.COMPLETED || missionItem.state == HomeMissionState.SKIPPED
    val liveRemainingMinutes = if (taskEnd != null && isInProgress) {
        ChronoUnit.MINUTES.between(currentTime, taskEnd).coerceAtLeast(0)
    } else null
    val showStartButton = !task.isCompleted && !task.isSkipped && minutesUntilStart in 0..10
    val showLaunchActions = !task.isCompleted && !task.isSkipped && !task.isBreakSegment
    val isDailyMicroAction = task.origin == DAILY_ONE_PERCENT_ORIGIN

    // ── Animated border thickness for LIVE_NOW ──────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "task_glow")
    val borderThickness by infiniteTransition.animateValue(
        initialValue = 1.dp, targetValue = 2.2.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "border_anim"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f, targetValue = 0.70f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_alpha"
    )

    // ── State-derived colors ────────────────────────────────────────────────
    val stateColor = when (missionItem.state) {
        HomeMissionState.LIVE_NOW    -> SuccessGreen
        HomeMissionState.UP_NEXT     -> WarningAmber
        HomeMissionState.LATER_TODAY -> HomeFocus
        HomeMissionState.OVERDUE     -> ErrorRed
        HomeMissionState.COMPLETED   -> SuccessGreen
        HomeMissionState.SKIPPED     -> ErrorRed.copy(alpha = 0.7f)
    }
    val cardBg = when (missionItem.state) {
        HomeMissionState.COMPLETED -> SuccessGreen.copy(alpha = 0.06f)
        HomeMissionState.SKIPPED   -> HomeSurface.copy(alpha = 0.7f)
        HomeMissionState.OVERDUE   -> ErrorRed.copy(alpha = 0.04f)
        HomeMissionState.LIVE_NOW  -> SuccessGreen.copy(alpha = 0.05f)
        else -> HomeSurface
    }

    // ── Metadata labels ─────────────────────────────────────────────────────
    val segmentChip = when {
        task.isBreakSegment -> "RECOVERY GAP"
        task.segmentCount > 1 && task.segmentIndex > 0 -> "PART ${task.segmentIndex}/${task.segmentCount}"
        else -> null
    }
    val taskTypeLabel = when (task.taskType.uppercase(Locale.ROOT)) {
        "STUDY" -> "Study"; "CODE" -> "Code"; "DESIGN" -> "Design"; "READ" -> "Read"
        "PHYSICAL" -> "Physical"; "ADMIN" -> "Admin"; "DSA" -> "DSA"
        "SYSTEM_DESIGN" -> "System Design"; "BEHAVIORAL" -> "Behavioral"
        "PROJECT_REVIEW" -> "Project Review"; "APP_INVEST" -> "App Invest"
        "DEEP_WORK" -> "Deep Work"; else -> "Mission"
    }
    val durationLabel = if (taskStart != null && taskEnd != null) {
        "${ChronoUnit.MINUTES.between(taskStart, taskEnd).coerceAtLeast(0)}m block"
    } else "Scheduled block"
    val isGroupedChain = missionItem.linkedTasks.size > 1
    val unresolvedLinkedCount = missionItem.linkedTasks.count { !it.isBreakSegment && !it.isCompleted && !it.isSkipped }
    val showChainLevelResolution = isGroupedChain && unresolvedLinkedCount > 1 && onDoneChain != null && onSkipChain != null
    val showTaskResolutionActions = showLaunchActions && !showChainLevelResolution
    val dailyActionCue = task.focusNotes.takeIf { isDailyMicroAction && it.isNotBlank() }
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    val dailyActionMode = if (isDailyMicroAction) {
        when (task.taskType.uppercase(Locale.ROOT)) {
            "BEHAVIORAL", "PROJECT_REVIEW" -> "VOICE_REP"
            "DSA" -> "PREP_FOCUS"
            "SYSTEM_DESIGN" -> if (task.focusModeEnabled) "PREP_FOCUS" else "VOICE_REP"
            "PHYSICAL" -> "PHYSICAL_RESET"
            else -> null
        }
    } else null
    val dailyActionLabel = when (dailyActionMode) {
        "VOICE_REP" -> "VOICE REP"; "PREP_FOCUS" -> "PREP REP"; "PHYSICAL_RESET" -> "RESET REP"; else -> "DAILY REP"
    }
    val dailyActionCueLabel = when (dailyActionMode) {
        "VOICE_REP" -> "VOICE CUE"; "PREP_FOCUS" -> "PREP CUE"; "PHYSICAL_RESET" -> "RESET CUE"; else -> "REP CUE"
    }
    val showPomodoroSecondary = !isInProgress && dailyActionMode == null
    val chainToggleKey = task.parentMissionId.ifBlank { "single_${task.id}" }
    var chainExpanded by rememberSaveable(chainToggleKey) { mutableStateOf(false) }

    // ── Depth perspective effect ────────────────────────────────────────────
    val depthScale = 1f - (index * 0.018f).coerceAtMost(0.10f)
    val depthOffsetY = (index * -7).dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = depthScale; scaleY = depthScale
                translationY = depthOffsetY.toPx()
                alpha = 1f - (index * 0.07f).coerceAtMost(0.28f)
            }
    ) {
        val isCompactCard = maxWidth < 360.dp
        val isVeryCompactCard = maxWidth < 330.dp
        val titleMaxLines = if (isDailyMicroAction || isCompactCard) 2 else 1
        val showMetaLine = !isVeryCompactCard
        val showSupportingLabel = !isCompactCard
        val showGroupBadge = missionItem.groupBadge != null && !isVeryCompactCard
        val showSegmentChip = segmentChip != null && !isCompactCard
        val compactActionLabels = isCompactCard

        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        if (onDoneChain != null && showChainLevelResolution) onDoneChain() else onDone()
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        if (onSkipChain != null && showChainLevelResolution) onSkipChain() else onSkip()
                        false
                    }
                    else -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = showTaskResolutionActions,
            enableDismissFromEndToStart = showTaskResolutionActions,
            backgroundContent = {
                // Rich swipe reveal background
                val (bgColor, alignment, icon) = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Triple(SuccessGreen, Alignment.CenterStart, Icons.Rounded.CheckCircle)
                    SwipeToDismissBoxValue.EndToStart -> Triple(ErrorRed, Alignment.CenterEnd, Icons.Rounded.Cancel)
                    else -> Triple(Color.Transparent, Alignment.Center, null)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd)
                                    listOf(bgColor.copy(alpha = 0.60f), Color.Transparent)
                                else
                                    listOf(Color.Transparent, bgColor.copy(alpha = 0.60f))
                            )
                        ),
                    contentAlignment = alignment
                ) {
                    icon?.let {
                        Icon(
                            it, contentDescription = null, tint = Color.White,
                            modifier = Modifier.size(34.dp).padding(
                                start = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) 16.dp else 0.dp,
                                end = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 16.dp else 0.dp
                            )
                        )
                    }
                }
            },
            content = {
                // ── Card shell ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(cardBg)
                        .drawBehind {
                            // Left-edge accent bar
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(stateColor, stateColor.copy(alpha = 0.25f))
                                ),
                                topLeft = Offset(0f, 8.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(3.5.dp.toPx(), size.height - 16.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                            )
                            // Animated border for active states
                            if (missionItem.state == HomeMissionState.LIVE_NOW || missionItem.state == HomeMissionState.OVERDUE) {
                                drawRoundRect(
                                    color = stateColor.copy(alpha = glowAlpha * 0.55f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = borderThickness.toPx()
                                    )
                                )
                                // Ambient glow halo (top edge)
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(stateColor.copy(alpha = glowAlpha * 0.20f), Color.Transparent),
                                        startY = 0f, endY = size.height * 0.35f
                                    )
                                )
                            } else if (missionItem.state == HomeMissionState.UP_NEXT) {
                                drawRoundRect(
                                    color = stateColor.copy(alpha = 0.28f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                                )
                            } else {
                                drawRoundRect(
                                    color = HomeOutline.copy(alpha = 0.55f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 20.dp, // extra space for accent bar
                            end = if (isCompactCard) 16.dp else 18.dp,
                            top = if (isResolvedMission) 14.dp else 16.dp,
                            bottom = if (isResolvedMission) 14.dp else 16.dp
                        )
                    ) {
                        // State row
                        TaskCardStateRow(
                            stateLabel = missionItem.stateLabel,
                            stateColor = stateColor,
                            supportingLabel = if (showSupportingLabel) {
                                if (isInProgress && liveRemainingMinutes != null) "${liveRemainingMinutes.toInt()} min left"
                                else missionItem.supportingLabel
                            } else null
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Live / overdue context banner
                        if (missionItem.state == HomeMissionState.LIVE_NOW || missionItem.state == HomeMissionState.OVERDUE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(stateColor.copy(alpha = 0.08f))
                                    .drawBehind {
                                        drawLine(
                                            color = stateColor.copy(alpha = 0.25f),
                                            start = Offset(0f, 0f), end = Offset(0f, size.height),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(stateColor.copy(alpha = glowAlpha)))
                                    Text(
                                        when (missionItem.state) {
                                            HomeMissionState.LIVE_NOW -> "Active now. Keep this window clean until it closes."
                                            HomeMissionState.OVERDUE -> "This block slipped. Recover it now or make the miss explicit."
                                            else -> ""
                                        },
                                        color = Color.White.copy(alpha = 0.80f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Main content row
                        Row(verticalAlignment = Alignment.Top) {
                            TaskScheduleRail(
                                startTime = task.startTime,
                                endTime = task.endTime,
                                isResolvedMission = isResolvedMission,
                                isSkipped = task.isSkipped,
                                compact = isVeryCompactCard
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {

                                // Task name row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        missionItem.displayName,
                                        fontSize = if (isResolvedMission) 14.sp else 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            task.isSkipped -> HomeTextSecondary.copy(alpha = 0.7f)
                                            isResolvedMission -> HomeTextSecondary.copy(alpha = 0.85f)
                                            else -> HomeTextPrimary
                                        },
                                        textDecoration = if (task.isSkipped) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = titleMaxLines,
                                        lineHeight = if (titleMaxLines > 1) 22.sp else 20.sp,
                                        letterSpacing = (-0.2).sp,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (isInProgress) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Pulsing live dot
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(SuccessGreen.copy(alpha = glowAlpha))
                                        )
                                    }
                                }

                                // Tag chips
                                if (showGroupBadge || showSegmentChip || isDailyMicroAction) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (showGroupBadge) {
                                            missionItem.groupBadge?.let { badge ->
                                                TagChip(badge, SurfaceElevated, TextPrimary)
                                            }
                                        }
                                        if (showSegmentChip) {
                                            segmentChip?.let { chip ->
                                                TagChip(
                                                    chip,
                                                    if (task.isBreakSegment) SurfaceElevated else stateColor.copy(alpha = 0.12f),
                                                    if (task.isBreakSegment) SuccessGreen else stateColor
                                                )
                                            }
                                        }
                                        if (isDailyMicroAction) {
                                            TagChip(dailyActionLabel, NeonCyan.copy(alpha = 0.12f), NeonCyan)
                                        }
                                    }
                                }

                                // Meta line
                                if (showMetaLine) {
                                    Text(
                                        if (isDailyMicroAction) "$taskTypeLabel · daily rep" else "$taskTypeLabel · $durationLabel",
                                        color = HomeTextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }

                                // Daily cue card
                                dailyActionCue?.let { cue ->
                                    TaskDailyCueCard(cueLabel = dailyActionCueLabel, cue = cue, compact = isCompactCard)
                                }

                                // Resolution states
                                when {
                                    task.isCompleted && task.focusScore > 0 -> {
                                        FocusScoreChip(score = task.focusScore)
                                    }
                                    isResolvedMission -> {
                                        Text(
                                            if (missionItem.state == HomeMissionState.COMPLETED) "Archived as proof." else "Archived as compromise.",
                                            color = HomeTextSecondary.copy(alpha = 0.6f),
                                            fontSize = 10.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }

                            // Resolved icon (right side)
                            if (task.isCompleted || task.isSkipped) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = if (task.isCompleted) SuccessGreen else ErrorRed.copy(alpha = 0.45f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Chain progress card
                        if (isGroupedChain) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TaskChainProgressCard(
                                linkedTasks = missionItem.linkedTasks,
                                isCompactCard = isCompactCard,
                                showChainLevelResolution = showChainLevelResolution,
                                chainExpanded = chainExpanded,
                                onToggle = { chainExpanded = !chainExpanded },
                                onDoneLinkedTask = onDoneLinkedTask,
                                onSkipLinkedTask = onSkipLinkedTask
                            )
                        }

                        // Chain resolution
                        if (showChainLevelResolution) {
                            Spacer(modifier = Modifier.height(12.dp))
                            ChainResolutionActions(isCompactCard = isCompactCard, onDoneChain = onDoneChain!!, onSkipChain = onSkipChain!!)
                        }

                        // Launch actions
                        if (showLaunchActions) {
                            Spacer(modifier = Modifier.height(14.dp))
                            TaskLaunchActions(
                                isDailyMicroAction = isDailyMicroAction,
                                isInProgress = isInProgress,
                                isVeryCompactCard = isVeryCompactCard,
                                dailyActionMode = dailyActionMode,
                                showStartButton = showStartButton,
                                compactActionLabels = compactActionLabels,
                                showChainLevelResolution = showChainLevelResolution,
                                showPomodoroSecondary = showPomodoroSecondary,
                                onOpenLiveTask = onOpenLiveTask,
                                onStartCountdown = onStartCountdown,
                                onStartPomodoro = onStartPomodoro
                            )
                            if (showTaskResolutionActions) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TaskResolutionRail(
                                    isDailyMicroAction = isDailyMicroAction,
                                    compactActionLabels = compactActionLabels,
                                    onDone = onDone,
                                    onSkip = onSkip
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Tag chip — small rounded pill
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TagChip(label: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(label, color = textColor, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 0.5.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Focus score chip — neon bolt + color-coded score
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FocusScoreChip(score: Int) {
    val color = getFocusColor(score)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = 0.25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
            Text("FOCUS: $score", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────

private fun mockMissionItem() = HomeMissionItem(
    task = com.sanket_satpute_20.ironmind.data.Task(
        id = 1, name = "Deep Focus Block",
        startTime = "09:00", endTime = "10:30", isCompleted = false
    ),
    state = HomeMissionState.LIVE_NOW,
    stateLabel = "LIVE NOW",
    supportingLabel = "Execution window is open"
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E13)
@androidx.compose.runtime.Composable
fun TaskCardPremiumPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        TaskCardPremium(
            missionItem = mockMissionItem(),
            currentTime = java.time.LocalTime.of(9, 30),
            onDone = {}, onSkip = {},
            onStartCountdown = {}, onStartPomodoro = {}, onOpenLiveTask = {}
        )
    }
}
