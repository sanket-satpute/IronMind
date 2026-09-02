package com.sanket_satpute_20.ironmind.home

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionActivity
import com.sanket_satpute_20.ironmind.context.ModeStateEngine
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.integrity.IronStrictnessProfile
import com.sanket_satpute_20.ironmind.psychology.UserType
import java.time.LocalTime
import java.util.Locale
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

internal data class CareerConfidenceSnapshot(
    val ignitionDone: Boolean,
    val ignitionRecoverable: Boolean,
    val ignitionCue: String?,
    val ignitionStreak: Int,
    val ignitionCompletedLast7Days: Int,
    val ignitionRecentDays: List<Boolean>,
    val dailyActionDone: Boolean,
    val dailyActionCue: String?,
    val prepMissionsToday: Int,
    val firstPrepTask: Task?,
    val confidenceRepsToday: Int,
    val weeklyScore: Int,
    val weeklyScoreFresh: Boolean
)

private data class CareerConfidenceLaneState(
    val label: String,
    val detail: String,
    val accent: Color
)

private data class CareerConfidencePrimaryAction(
    val label: String,
    val accent: Color,
    val onClick: () -> Unit
)

internal fun LazyListScope.HomeFrictionlessSection(
    doNowMission: HomeMissionItem?,
    showSchedule: Boolean,
    onToggleSchedule: () -> Unit,
    recoveryBudget: Int,
    isRecoveryDayActive: Boolean,
    onDeclareRecoveryDayClick: () -> Unit,
    ironStrictnessProfile: IronStrictnessProfile?,
    tasks: List<Task>,
    homeMissionItems: List<HomeMissionItem>,
    currentTime: LocalTime,
    userType: UserType,
    allDone: Boolean,
    resolvedExpanded: Boolean,
    onResolvedExpandedChange: (Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    onDone: (Task) -> Unit,
    onSkip: (Task) -> Unit,
    onDoneChain: (String) -> Unit,
    onSkipChain: (String) -> Unit,
    onStartCountdown: (Task) -> Unit,
    onStartPomodoro: (Task) -> Unit,
    onOpenLiveTask: (Task) -> Unit,
    onStartQuickFocus: () -> Unit
) {
    item {
        HomeDailyCommandCenter(
            doNowMission = doNowMission,
            tasks = tasks,
            allDone = allDone,
            isRecoveryDayActive = isRecoveryDayActive,
            onPrimaryAction = {
                when {
                    isRecoveryDayActive -> onDeclareRecoveryDayClick()
                    allDone -> if (!showSchedule) onToggleSchedule()
                    doNowMission?.task?.isInProgress == true -> onOpenLiveTask(doNowMission.task)
                    doNowMission?.task?.focusModeEnabled == true -> onStartPomodoro(doNowMission.task)
                    doNowMission != null -> onStartCountdown(doNowMission.task)
                    else -> onStartQuickFocus()
                }
            },
            onAddMission = onAddTaskClick,
            onQuickFocus = onStartQuickFocus
        )
    }

    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .gamifiedClick { onToggleSchedule() }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (showSchedule) "TODAY'S MISSIONS ARE OPEN" else "SHOW TODAY'S MISSIONS",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.Icon(
                if (showSchedule) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (ironStrictnessProfile?.activeToday == true && showSchedule) {
        item {
            IntegrityPressureCard(
                punishmentBonusSeconds = ironStrictnessProfile.punishmentSecondsBonus,
                cleanUnlockRequired = ironStrictnessProfile.requireCleanEarnedUnlock,
                springPenaltyBonus = ironStrictnessProfile.workLockPenaltyBonus,
                emergencyExitReduction = ironStrictnessProfile.emergencyExitReduction
            )
        }
    }

    if (showSchedule) {
        HomeMissionBoardSection(
            tasks = tasks,
            homeMissionItems = homeMissionItems,
            currentTime = currentTime,
            userType = userType,
            allDone = allDone,
            resolvedExpanded = resolvedExpanded,
            onResolvedExpandedChange = onResolvedExpandedChange,
            highlightedTaskId = doNowMission?.task?.id,
            onAddTaskClick = onAddTaskClick,
            onDone = onDone,
            onSkip = onSkip,
            onDoneChain = onDoneChain,
            onSkipChain = onSkipChain,
            onStartCountdown = onStartCountdown,
            onStartPomodoro = onStartPomodoro,
            onOpenLiveTask = onOpenLiveTask
        )
    }
    return

    item {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Massive Frictionless Button
        val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseScale"
        )

        if (isRecoveryDayActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonCyan.copy(alpha = 0.6f), SurfaceElevated.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Bolt, // Replace with appropriate icon
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "RECOVERY DAY ACTIVE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val (actionLabel, onClick) = if (doNowMission != null) {
                val label = when {
                    doNowMission.task.isInProgress -> "OPEN: ${doNowMission.displayName}"
                    doNowMission.task.focusModeEnabled -> "START: ${doNowMission.displayName}"
                    else -> "START: ${doNowMission.displayName}"
                }
                val click = {
                    when {
                        doNowMission.task.isInProgress -> onOpenLiveTask(doNowMission.task)
                        doNowMission.task.focusModeEnabled -> onStartPomodoro(doNowMission.task)
                        else -> onStartCountdown(doNowMission.task)
                    }
                }
                label to click
            } else {
                "START QUICK FOCUS" to onStartQuickFocus
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(SuccessGreen.copy(alpha = 0.8f), SuccessGreen.copy(alpha = 0.4f))
                        )
                    )
                    .gamifiedClick { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        actionLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Toggle Schedule Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .gamifiedClick { onToggleSchedule() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (showSchedule) "HIDE SCHEDULE" else "VIEW SCHEDULE",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.Icon(
                imageVector = if (showSchedule) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        if (!isRecoveryDayActive) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .gamifiedClick { onDeclareRecoveryDayClick() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.Bolt, // Consider using a 'healing' or 'rest' icon
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "DECLARE RECOVERY ($recoveryBudget LEFT)",
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    if (ironStrictnessProfile?.activeToday == true && showSchedule) {
        item {
            IntegrityPressureCard(
                punishmentBonusSeconds = ironStrictnessProfile.punishmentSecondsBonus,
                cleanUnlockRequired = ironStrictnessProfile.requireCleanEarnedUnlock,
                springPenaltyBonus = ironStrictnessProfile.workLockPenaltyBonus,
                emergencyExitReduction = ironStrictnessProfile.emergencyExitReduction
            )
        }
    }

    if (showSchedule) {
        HomeMissionBoardSection(
            tasks = tasks,
            homeMissionItems = homeMissionItems,
            currentTime = currentTime,
            userType = userType,
            allDone = allDone,
            resolvedExpanded = resolvedExpanded,
            onResolvedExpandedChange = onResolvedExpandedChange,
            highlightedTaskId = doNowMission?.task?.id,
            onAddTaskClick = onAddTaskClick,
            onDone = onDone,
            onSkip = onSkip,
            onDoneChain = onDoneChain,
            onSkipChain = onSkipChain,
            onStartCountdown = onStartCountdown,
            onStartPomodoro = onStartPomodoro,
            onOpenLiveTask = onOpenLiveTask
        )
    }
}

@Composable
internal fun CareerConfidenceCard(
    snapshot: CareerConfidenceSnapshot,
    onIgnitionClick: () -> Unit,
    onPrepClick: () -> Unit,
    onVoiceRepClick: () -> Unit,
    onWeeklyScoreClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val laneState = remember(
        snapshot.ignitionDone,
        snapshot.ignitionRecoverable,
        snapshot.dailyActionDone,
        snapshot.firstPrepTask,
        snapshot.confidenceRepsToday,
        snapshot.weeklyScore,
        snapshot.weeklyScoreFresh
    ) {
        deriveCareerConfidenceLaneState(
            ignitionDone = snapshot.ignitionDone,
            ignitionRecoverable = snapshot.ignitionRecoverable,
            dailyActionDone = snapshot.dailyActionDone,
            hasPrepMission = snapshot.firstPrepTask != null,
            prepMissionLive = snapshot.firstPrepTask?.isInProgress == true,
            confidenceRepsToday = snapshot.confidenceRepsToday,
            weeklyScore = snapshot.weeklyScore,
            weeklyScoreFresh = snapshot.weeklyScoreFresh
        )
    }
    val primaryAction = remember(
        snapshot.ignitionDone,
        snapshot.ignitionRecoverable,
        snapshot.firstPrepTask,
        snapshot.confidenceRepsToday,
        snapshot.weeklyScore,
        snapshot.weeklyScoreFresh
    ) {
        when {
            snapshot.ignitionRecoverable -> CareerConfidencePrimaryAction(
                label = "RECOVER IGNITION",
                accent = WarningAmber,
                onClick = onIgnitionClick
            )
            !snapshot.ignitionDone -> CareerConfidencePrimaryAction(
                label = "OPEN IGNITION",
                accent = TextPrimary,
                onClick = onIgnitionClick
            )
            snapshot.firstPrepTask == null -> CareerConfidencePrimaryAction(
                label = "OPEN PREP",
                accent = TextPrimary,
                onClick = onPrepClick
            )
            snapshot.confidenceRepsToday <= 0 -> CareerConfidencePrimaryAction(
                label = "RECORD VOICE REP",
                accent = WarningAmber,
                onClick = onVoiceRepClick
            )
            else -> CareerConfidencePrimaryAction(
                label = "OPEN WEEKLY REVIEW",
                accent = SuccessGreen,
                onClick = onWeeklyScoreClick
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.18f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(SurfaceDark, DeepBackground, DeepBackground)
                    )
                )
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                laneState.accent.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gamifiedClick { expanded = !expanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(TextPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(laneState.accent.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(laneState.accent, CircleShape)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "CAREER CONFIDENCE",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                laneState.label,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(laneState.accent.copy(alpha = 0.9f))
                            )
                        }
                    }
                    Surface(
                        color = SurfaceDark,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, laneState.accent.copy(alpha = 0.28f))
                    ) {
                        Text(
                            laneState.label.uppercase(Locale.ROOT),
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            color = laneState.accent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    laneState.detail,
                    color = Color.White.copy(alpha = 0.74f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Surface(
                            color = Color.White.copy(alpha = 0.035f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    "STATUS RAIL",
                                    color = Color.White.copy(alpha = 0.42f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CareerConfidenceProgressStrip(
                                    ignitionDone = snapshot.ignitionDone,
                                    dailyActionDone = snapshot.dailyActionDone,
                                    prepReady = snapshot.firstPrepTask != null,
                                    voiceDone = snapshot.confidenceRepsToday > 0,
                                    weeklyDone = snapshot.weeklyScoreFresh && snapshot.weeklyScore > 0
                                )
                            }
                        }
                        val leadCue = snapshot.ignitionCue ?: snapshot.dailyActionCue
                        leadCue?.let { cue ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = SurfaceDark,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.12f))
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(laneState.accent)
                                        )
                                        Text(
                                            "TODAY'S EDGE",
                                            color = TextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.9.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        cue,
                                        color = Color.White.copy(alpha = 0.88f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 18.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = primaryAction.onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryAction.accent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        primaryAction.label,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onWeeklyScoreClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.18f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text("OPEN WEEKLY REVIEW", fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 0.4.sp)
                }
            }
        }
    }
}

private fun deriveCareerConfidenceLaneState(
    ignitionDone: Boolean,
    ignitionRecoverable: Boolean,
    dailyActionDone: Boolean,
    hasPrepMission: Boolean,
    prepMissionLive: Boolean,
    confidenceRepsToday: Int,
    weeklyScore: Int,
    weeklyScoreFresh: Boolean
): CareerConfidenceLaneState {
    return when {
        ignitionDone && dailyActionDone && hasPrepMission && confidenceRepsToday > 0 && weeklyScoreFresh && weeklyScore > 0 ->
            CareerConfidenceLaneState(
                label = "Locked In",
                detail = "Proof is on the board.",
                accent = SuccessGreen
            )
        ignitionRecoverable ->
            CareerConfidenceLaneState(
                label = "Recover Today",
                detail = "Recover the morning first.",
                accent = WarningAmber
            )
        ignitionDone || prepMissionLive || confidenceRepsToday > 0 || dailyActionDone ->
            CareerConfidenceLaneState(
                label = "In Motion",
                detail = "Keep stacking proof.",
                accent = WarningAmber
            )
        else ->
            CareerConfidenceLaneState(
                label = "Open",
                detail = "Start with ignition.",
                accent = TextPrimary
            )
    }
}

@Composable
private fun CareerConfidenceProgressStrip(
    ignitionDone: Boolean,
    dailyActionDone: Boolean,
    prepReady: Boolean,
    voiceDone: Boolean,
    weeklyDone: Boolean
) {
    val score = listOf(ignitionDone, dailyActionDone, prepReady, voiceDone, weeklyDone).count { it }
    val progress = (score / 5f).coerceIn(0f, 1f)
    
    val infiniteTransition = rememberInfiniteTransition(label = "meter_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("IGNITE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (ignitionDone) SuccessGreen else Color.Gray)
            Text("1%", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (dailyActionDone) SuccessGreen else Color.Gray)
            Text("PREP", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (prepReady) TextPrimary else Color.Gray)
            Text("VOICE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (voiceDone) WarningAmber else Color.Gray)
            Text("REVIEW", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (weeklyDone) ElectricViolet else Color.Gray)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            // Zones
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(SuccessGreen.copy(alpha = 0.1f)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(SuccessGreen.copy(alpha = 0.15f)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(TextPrimary.copy(alpha = 0.1f)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(WarningAmber.copy(alpha = 0.1f)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(ElectricViolet.copy(alpha = 0.1f)))
            }
            
            // Animated Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(SuccessGreen, SuccessGreen, TextPrimary, WarningAmber, ElectricViolet)
                        )
                    )
                    .scale(scaleX = 1f, scaleY = pulseAlpha) // slight vertical breathing
            )
        }
    }
}

// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun CareerConfidenceCardPreview() {
    val snapshot = CareerConfidenceSnapshot(
        ignitionDone = true,
        ignitionRecoverable = false,
        ignitionCue = null,
        ignitionStreak = 4,
        ignitionCompletedLast7Days = 5,
        ignitionRecentDays = listOf(true, true, false, true, true, true, false),
        dailyActionDone = true,
        dailyActionCue = null,
        prepMissionsToday = 1,
        firstPrepTask = null,
        confidenceRepsToday = 2,
        weeklyScore = 82,
        weeklyScoreFresh = true
    )
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        CareerConfidenceCard(
            snapshot = snapshot,
            onIgnitionClick = {},
            onPrepClick = {},
            onVoiceRepClick = {},
            onWeeklyScoreClick = {}
        )
    }
}
