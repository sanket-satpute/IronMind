package com.sanket_satpute_20.ironmind.home

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionActivity
import com.sanket_satpute_20.ironmind.crucible.CrucibleManager
import com.sanket_satpute_20.ironmind.crucible.CrucibleRepository
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionStatus
import com.sanket_satpute_20.ironmind.product.CurrentVersionScope
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.ui.components.ProgressRing
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.utils.PermissionHelper
import com.sanket_satpute_20.ironmind.utils.PermissionPriority
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun ProgressScreen(
    viewModel: TaskViewModel,
    onStatsClick: () -> Unit,
    onWeeklyScoreClick: () -> Unit,
    onGraveyardClick: () -> Unit,
    onPermissionCenterClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onIgnitionHistoryClick: () -> Unit,
    onVoiceLogsClick: () -> Unit,
    onVoiceRepClick: () -> Unit,
    onPrepBuilderClick: () -> Unit,
    onCrucibleClick: (String) -> Unit,
    onClubChatClick: () -> Unit,
    onIronCircleClick: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val voiceLogs by viewModel.voiceLogs.collectAsState()
    val ignitionEntry by viewModel.todayIgnitionEntry.collectAsState()
    val ignitionEntries by viewModel.allIgnitionEntries.collectAsState()
    val homePerformanceSnapshot by viewModel.homePerformanceSnapshot.collectAsState()
    val socialOverview by viewModel.socialOverview.collectAsState()
    val configEvents by viewModel.configChangeEvents.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()

    val currentMode = AdaptiveEngine.getCurrentMode(viewModel.prefManager)
    val todayKey = remember { LocalDate.now().toString() }
    val nowDate = remember { LocalDate.now() }
    val tomorrowDate = remember { nowDate.plusDays(1) }

    val careerConfidenceSnapshot = remember(
        tasks, voiceLogs, ignitionEntry, ignitionEntries,
        viewModel.prefManager.lastWeeklyHonestScore,
        viewModel.prefManager.lastWeeklyHonestScoreWeek
    ) {
        buildCareerConfidenceSnapshot(
            tasks = tasks,
            voiceLogs = voiceLogs,
            ignitionEntry = ignitionEntry,
            ignitionEntries = ignitionEntries,
            todayKey = todayKey,
            lastWeeklyHonestScore = viewModel.prefManager.lastWeeklyHonestScore,
            lastWeeklyHonestScoreWeek = viewModel.prefManager.lastWeeklyHonestScoreWeek
        )
    }

    val pomodoroOverview = remember(configEvents, nowDate) {
        buildHomePomodoroOverview(configEvents, nowDate)
    }

    val crucibleManager = remember { CrucibleManager(viewModel.prefManager) }
    val activeCrucible = crucibleManager.getActiveCrucible()
    val seasonalCrucible = CrucibleRepository.getCurrentCrucible()

    val missingCoreRequirements = remember(context) {
        PermissionHelper.getCoreProtectionRequirements(context).filterNot { it.isGranted }
    }
    val topMissingPermissionPriority = missingCoreRequirements.minByOrNull { requirement ->
        when (requirement.priority) {
            PermissionPriority.CRITICAL -> 0
            PermissionPriority.IMPORTANT -> 1
            PermissionPriority.SUPPORTIVE -> 2
        }
    }?.priority
    val canShowPermissionAction = missingCoreRequirements.isNotEmpty()

    val nightDecisionManager = remember { NightDecisionManager(context) }
    val hasTomorrowTasks = remember(tasks, tomorrowDate) {
        tasks.any { task -> !task.isBreakSegment && task.date == tomorrowDate.toString() }
    }
    val tomorrowNightDecisionStatus = remember { nightDecisionManager.statusForDate(tomorrowDate) }
    val canShowEmergencyLeaveAction = viewModel.prefManager.emergencyValveTokens > 0 &&
            !hasTomorrowTasks &&
            tomorrowNightDecisionStatus != NightDecisionStatus.EMERGENCY_LEAVE

    val emergencyCooldownUntil = viewModel.prefManager.emergencyValveCooldownUntil
    val cooldownClock by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val challengeRecentlyTouched = viewModel.prefManager.challengeActive ||
            isRecentDate(viewModel.prefManager.challengeLastCompleted, 10) ||
            isRecentTimestamp(viewModel.prefManager.challengeRunStartedAt, 14)
    val clubRecentlyTouched = viewModel.prefManager.chatUnlocked ||
            viewModel.prefManager.chatMinStreakMet ||
            viewModel.prefManager.clubTrackedPostCount > 0 ||
            viewModel.prefManager.challengeDaysCompleted >= 7
    val shouldShowFiveAmClubQuickAction = CurrentVersionScope.SHOW_ADVANCED_CHALLENGES && (
            challengeRecentlyTouched ||
                clubRecentlyTouched ||
                viewModel.prefManager.ironStatusUnlocked
            )
    val fiveAmClubAccent = when {
        viewModel.prefManager.chatUnlocked -> GoldXP
        viewModel.prefManager.chatMinStreakMet -> TextPrimary
        viewModel.prefManager.challengeActive -> WarningAmber
        else -> TextPrimary
    }
    val fiveAmClubBadge = when {
        viewModel.prefManager.challengeDaysCompleted > 0 -> "Day ${viewModel.prefManager.challengeDaysCompleted}"
        viewModel.prefManager.chatUnlocked -> "Open"
        viewModel.prefManager.chatMinStreakMet -> "Reader"
        viewModel.prefManager.challengeActive -> "Active"
        else -> null
    }
    val fiveAmClubDescription = when {
        viewModel.prefManager.chatUnlocked -> "Morning run and club room."
        viewModel.prefManager.chatMinStreakMet -> "Run progress with room access."
        viewModel.prefManager.challengeActive -> "Today's run is live."
        else -> "Your saved morning hub."
    }

    val focusTasks = tasks.filterNot { it.isBreakSegment }
    val guardNavigation: (() -> Unit) -> Unit = { action -> action() }

    // ── Derive a key stat for the header tagline ────────────────────────────
    val totalDone = homePerformanceSnapshot.doneToday
    val totalMissions = (homePerformanceSnapshot.doneToday + homePerformanceSnapshot.skippedToday + homePerformanceSnapshot.pendingToday).coerceAtLeast(1)
    val completionRate = homePerformanceSnapshot.completionRate

    Box(modifier = Modifier.fillMaxSize().background(HomeCanvas)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Screen header with live stat ─────────────────────────────
            item {
                ProgressScreenHeader(
                    completionRate = completionRate,
                    totalDone = totalDone,
                    totalMissions = totalMissions,
                    currentMode = currentMode
                )
            }

            // ── Career Confidence Card (if feature enabled) ───────────────
            if (CurrentVersionScope.SHOW_CONFIDENCE_AND_VOICE) {
                item {
                    CareerConfidenceCard(
                        snapshot = careerConfidenceSnapshot,
                        onIgnitionClick = {
                            if (careerConfidenceSnapshot.ignitionDone) onIgnitionHistoryClick()
                            else context.startActivity(ConfidenceIgnitionActivity.createIntent(context))
                        },
                        onPrepClick = {
                            val prepTask = careerConfidenceSnapshot.firstPrepTask
                            if (prepTask == null) {
                                onPrepBuilderClick()
                            } else if (prepTask.isInProgress) {
                                openLiveTask(context, prepTask)
                            } else {
                                startCountdownForTask(
                                    context = context,
                                    task = prepTask,
                                    onVoiceRepClick = onVoiceRepClick,
                                    guardNavigation = guardNavigation
                                )
                            }
                        },
                        onVoiceRepClick = {
                            if (careerConfidenceSnapshot.confidenceRepsToday > 0) onVoiceLogsClick()
                            else onVoiceRepClick()
                        },
                        onWeeklyScoreClick = {
                            if (careerConfidenceSnapshot.weeklyScoreFresh && careerConfidenceSnapshot.weeklyScore > 0) onStatsClick()
                            else onWeeklyScoreClick()
                        }
                    )
                }
            }

            // ── Pomodoro analytics (when sessions exist) ──────────────────
            pomodoroOverview?.let { overview ->
                item {
                    GamifiedPomodoroAnalyticsCard(overview = overview)
                }
            }

            // ── Performance read card ─────────────────────────────────────
            item {
                GamifiedPerformanceCard(
                    snapshot = homePerformanceSnapshot,
                    onStatsClick = onStatsClick,
                    onGraveyardClick = onGraveyardClick
                )
            }

            // ── Ecosystem section (quick actions, crucible, social etc.) ──
            HomeSupportSection(
                context = context,
                pomodoroOverview = null, // handled above with gamified card
                homePerformanceSnapshot = homePerformanceSnapshot,
                topMissingPermissionPriority = topMissingPermissionPriority,
                canShowPermissionAction = canShowPermissionAction,
                canShowEmergencyLeaveAction = canShowEmergencyLeaveAction,
                shouldShowFiveAmClubQuickAction = shouldShowFiveAmClubQuickAction,
                fiveAmClubAccent = fiveAmClubAccent,
                fiveAmClubDescription = fiveAmClubDescription,
                fiveAmClubBadge = fiveAmClubBadge,
                prefManager = viewModel.prefManager,
                activeCrucible = activeCrucible.takeIf { CurrentVersionScope.SHOW_ADVANCED_CHALLENGES },
                seasonalCrucible = seasonalCrucible.takeIf { CurrentVersionScope.SHOW_ADVANCED_CHALLENGES },
                crucibleManager = crucibleManager,
                socialOverview = socialOverview.takeIf { CurrentVersionScope.SHOW_IRON_CIRCLE },
                emergencyCooldownUntil = emergencyCooldownUntil,
                cooldownClock = cooldownClock,
                currentLevel = currentLevel,
                onStatsClick = onStatsClick,
                onGraveyardClick = onGraveyardClick,
                onPermissionCenterClick = onPermissionCenterClick,
                onEmergencyLeaveClick = { /* Emergency leave handled from home overlays */ },
                onChallengeClick = onChallengeClick,
                onCrucibleClick = onCrucibleClick,
                onClubChatClick = onClubChatClick,
                onIronCircleClick = onIronCircleClick
            )

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ProgressScreenHeader — Gamified section title with live execution signal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressScreenHeader(
    completionRate: Int,
    totalDone: Int,
    totalMissions: Int,
    currentMode: AppMode
) {
    val modeAccent = when (currentMode.name.uppercase()) {
        "IRON" -> HomeAction
        "FOCUS" -> HomeFocus
        "RECOVER" -> SuccessGreen
        else -> HomeReward
    }
    val infinite = rememberInfiniteTransition(label = "prog_header")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "header_glow"
    )

    Column(modifier = Modifier.padding(top = 48.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Neon accent + title
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .width(3.dp).height(26.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(listOf(modeAccent, modeAccent.copy(alpha = 0.3f)))
                        )
                        .drawBehind {
                            drawRect(color = modeAccent.copy(alpha = glowAlpha * 0.4f))
                        }
                )
                Text(
                    "PROGRESS",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = HomeTextPrimary,
                    letterSpacing = 2.sp
                )
            }
            // Live completion rate badge
            if (totalMissions > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(modeAccent.copy(alpha = 0.13f))
                        .drawBehind {
                            drawRoundRect(
                                color = modeAccent.copy(alpha = glowAlpha * 0.35f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(50f),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(modeAccent.copy(alpha = glowAlpha)))
                        Text(
                            "$totalDone / $totalMissions  ·  $completionRate%",
                            color = modeAccent,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Your execution signals are being tracked in real time.",
            color = HomeTextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  GamifiedPomodoroAnalyticsCard — Chamber pressure stats
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun GamifiedPomodoroAnalyticsCard(overview: HomePomodoroOverview) {
    val infinite = rememberInfiniteTransition(label = "pomo_card")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.25f, targetValue = 0.60f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pomo_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(HomeSurface)
            .drawBehind {
                // Cyan top glow
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(NeonCyan.copy(alpha = glowAlpha * 0.25f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.40f
                    )
                )
                // Top edge highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, NeonCyan.copy(alpha = 0.45f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Neon border
                drawRoundRect(
                    color = NeonCyan.copy(alpha = glowAlpha * 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp).clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.12f))
                        .drawBehind {
                            drawRoundRect(color = NeonCyan.copy(alpha = glowAlpha * 0.3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = Stroke(1.dp.toPx()))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        "CHAMBER PRESSURE",
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        "${overview.started} runs armed · ${overview.completed} sealed",
                        color = HomeTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            // Stat chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PomodoroStatChip("${overview.focusedMinutes}m", "FOCUSED", NeonCyan, Modifier.weight(1f))
                PomodoroStatChip("${overview.broken}", "BROKEN", ErrorRed, Modifier.weight(1f))
                PomodoroStatChip("${overview.averageScore}", "AVG SCORE", HomeReward, Modifier.weight(1f))
            }

            overview.latestTitle?.takeIf { it.isNotBlank() }?.let { title ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.06f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(NeonCyan))
                        Text("Latest: $title", color = HomeTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroStatChip(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(HomeSurfaceRaised)
            .drawBehind {
                drawRoundRect(
                    color = accent.copy(alpha = 0.20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = accent, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = HomeTextSecondary, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.5.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  GamifiedPerformanceCard — Execution read with animated progress bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun GamifiedPerformanceCard(
    snapshot: HomePerformanceSnapshot,
    onStatsClick: () -> Unit,
    onGraveyardClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val done = snapshot.doneToday
    val skipped = snapshot.skippedToday
    val pending = snapshot.pendingToday
    val total = (done + skipped + pending).coerceAtLeast(1)
    val doneFraction = done.toFloat() / total.toFloat()
    val skippedFraction = skipped.toFloat() / total.toFloat()

    val cardAccent = when {
        snapshot.isCleanDay -> SuccessGreen
        pending == 0 -> HomeReward
        skipped > 0 -> ErrorRed.copy(alpha = 0.80f)
        else -> HomeFocus
    }

    val summaryLabel = when {
        done == 0 && skipped == 0 && pending == 0 -> "No missions loaded"
        pending == 0 && skipped == 0 -> "Board secured"
        skipped == 0 && pending > 0 -> "$pending pending · clean so far"
        pending == 0 -> "$skipped skipped · day resolved"
        else -> "$pending pending · $skipped skipped"
    }

    val infinite = rememberInfiniteTransition(label = "perf_card_glow")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.28f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "perf_glow"
    )

    // Animated bar widths
    val animDone by animateFloatAsState(
        targetValue = doneFraction,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "done_anim"
    )
    val animSkipped by animateFloatAsState(
        targetValue = skippedFraction,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "skip_anim"
    )

    var activeLexiconTerm by remember { mutableStateOf<com.sanket_satpute_20.ironmind.ui.components.LexiconTerm?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(HomeSurface)
            .drawBehind {
                // Accent ambient glow from top
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(cardAccent.copy(alpha = glowAlpha * 0.25f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.40f
                    )
                )
                // Top edge highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, cardAccent.copy(alpha = 0.50f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Neon border
                drawRoundRect(
                    color = cardAccent.copy(alpha = glowAlpha * 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {

            // ── Header row ───────────────────────────────────────────────
            val headerInteraction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = headerInteraction, indication = null) { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(cardAccent.copy(alpha = 0.12f))
                        .drawBehind {
                            drawRoundRect(
                                color = cardAccent.copy(alpha = glowAlpha * 0.25f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                                style = Stroke(1.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Analytics, contentDescription = null, tint = cardAccent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PERFORMANCE READ", color = HomeTextPrimary, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                        androidx.compose.material3.IconButton(
                            onClick = { activeLexiconTerm = com.sanket_satpute_20.ironmind.ui.components.LexiconTerm.DAILY_INTEGRITY },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Text("ⓘ", color = HomeTextSecondary, fontSize = 13.sp)
                        }
                    }
                    Text(summaryLabel, color = HomeTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                }
                // Score badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardAccent.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (pending > 0) "$pending" else "${snapshot.completionRate}%", color = cardAccent, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text(if (pending > 0) "OPEN" else "CLEAN", color = HomeTextSecondary, fontWeight = FontWeight.Black, fontSize = 8.sp, letterSpacing = 0.8.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp).clip(CircleShape)
                        .background(HomeSurfaceRaised)
                        .drawBehind {
                            drawCircle(color = HomeOutline, style = Stroke(1.dp.toPx()))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = HomeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Segmented progress bar ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(HomeOutline)
            ) {
                // Done segment
                if (animDone > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animDone)
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(listOf(SuccessGreen.copy(alpha = 0.8f), SuccessGreen))
                            )
                    )
                }
                // Skipped segment (starts after done)
                if (animSkipped > 0f && animDone < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animDone + animSkipped)
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (animDone > 0f) animSkipped / (animDone + animSkipped) else 1f)
                                .height(10.dp)
                                .background(Brush.horizontalGradient(listOf(ErrorRed.copy(alpha = 0.7f), ErrorRed)))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Stat chips row ────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PerformanceLegendChip("DONE", done, SuccessGreen, Modifier.weight(1f))
                PerformanceLegendChip("SKIPPED", skipped, ErrorRed, Modifier.weight(1f))
                PerformanceLegendChip("PENDING", pending, WarningAmber, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Action buttons ────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PerformanceActionButton(
                    label = "OPEN REVIEW",
                    icon = Icons.Rounded.Analytics,
                    color = HomeFocus,
                    modifier = Modifier.weight(1f),
                    onClick = onStatsClick
                )
                if (skipped > 0) {
                    PerformanceActionButton(
                        label = "ARCHIVE",
                        emoji = "📋",
                        color = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onGraveyardClick
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HomeSurfaceRaised)
                            .drawBehind {
                                drawRoundRect(
                                    color = HomeOutline.copy(alpha = 0.5f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                                    style = Stroke(1.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ALL CLEAR TODAY", color = HomeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    }
                }
            }

            // ── Expanded verdict + trend ──────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HomeOutline))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(snapshot.verdict, color = HomeTextSecondary, fontSize = 11.sp, lineHeight = 18.sp)
                    snapshot.trendLabel?.let { trendLabel ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val trendColor = if ((snapshot.recentCompletionRate ?: 0) >= 75) SuccessGreen else WarningAmber
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(trendColor.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
                            Text(
                                trendLabel + snapshot.recentCompletionRate?.let { " · $it% clean" }.orEmpty(),
                                color = trendColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    activeLexiconTerm?.let { term ->
        com.sanket_satpute_20.ironmind.ui.components.IronMindLexiconSheet(
            term = term,
            onDismiss = { activeLexiconTerm = null }
        )
    }
}

@Composable
private fun PerformanceLegendChip(label: String, value: Int, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(HomeSurfaceRaised)
            .drawBehind {
                drawRoundRect(
                    color = accent.copy(alpha = 0.20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", color = accent, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(label, color = HomeTextSecondary, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun PerformanceActionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    emoji: String? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "perf_btn_scale"
    )
    Box(
        modifier = modifier
            .height(42.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (pressed) 0.20f else 0.12f))
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            icon?.let { Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(15.dp)) }
            emoji?.let { Text(it, fontSize = 14.sp) }
            Text(label, color = color, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.4.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E13)
@androidx.compose.runtime.Composable
fun ProgressScreenPreview() {
    if (!androidx.compose.ui.platform.LocalInspectionMode.current) return
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ProgressScreenPreviewContent()
    }
}

@Composable
private fun ProgressScreenPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeCanvas)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressScreenHeader(
            completionRate = 78,
            totalDone = 4,
            totalMissions = 6,
            currentMode = AppMode.IRON
        )
        GamifiedPomodoroAnalyticsCard(
            overview = HomePomodoroOverview(
                started = 3, completed = 2, broken = 1,
                focusedMinutes = 90, averageScore = 82,
                latestTitle = "Deep Work Block"
            )
        )
        GamifiedPerformanceCard(
            snapshot = HomePerformanceSnapshot(
                doneToday = 4, skippedToday = 1, pendingToday = 1,
                completionRate = 80, isCleanDay = false,
                verdict = "You've secured 80% of the board. One skip registered — recover before close.",
                trendLabel = "7-day trend: strong",
                recentCompletionRate = 82
            ),
            onStatsClick = {},
            onGraveyardClick = {}
        )
    }
}
