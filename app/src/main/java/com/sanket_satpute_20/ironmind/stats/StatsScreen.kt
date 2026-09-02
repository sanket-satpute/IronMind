package com.sanket_satpute_20.ironmind.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.ChallengeDayRecord
import com.sanket_satpute_20.ironmind.data.ChallengeEvent
import com.sanket_satpute_20.ironmind.data.ClubChatEvent
import com.sanket_satpute_20.ironmind.data.ConfigChangeEvent
import com.sanket_satpute_20.ironmind.data.ConfidenceIgnitionEntry
import com.sanket_satpute_20.ironmind.data.CrucibleDayRecord
import com.sanket_satpute_20.ironmind.data.CrucibleEvent
import com.sanket_satpute_20.ironmind.data.DailyCheckIn
import com.sanket_satpute_20.ironmind.data.EmergencyValveEvent
import com.sanket_satpute_20.ironmind.data.FocusSession
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionActivity
import com.sanket_satpute_20.ironmind.focus.WorkLockActivity
import com.sanket_satpute_20.ironmind.focus.WorkStartActivity
import com.sanket_satpute_20.ironmind.ui.components.ProgressRing
import com.sanket_satpute_20.ironmind.home.DAILY_ONE_PERCENT_ORIGIN
import com.sanket_satpute_20.ironmind.home.TaskViewModel
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

// --- Premium Design Palette ---
val SuccessGreen = SurfaceElevated
val SuccessGreenLight = SuccessGreen
val ErrorRed = ErrorRed
val ErrorRedLight = ErrorRed
val WarningAmber = WarningAmber
val SurfaceDark = DeepBackground
val SurfaceLighter = SurfaceElevated
val TextSecondary = ElectricViolet

private data class SplitMissionSummary(
    val chainsSeen: Int,
    val fullySecuredChains: Int,
    val compromisedChains: Int,
    val activeChains: Int,
    val averagePartsPerChain: Int,
    val longestChain: Int,
    val mostCommonSpacing: String
)

private data class PerformanceSummary(
    val headline: String,
    val body: String,
    val accent: Color
)

private data class RecentPerformanceWindow(
    val done: Int,
    val skipped: Int,
    val completionRate: Float,
    val dailyBreakdown: List<RecentDayPerformance>
)

private data class PatternSpotlight(
    val title: String,
    val summary: String,
    val accent: Color
)

private data class RecentDayPerformance(
    val label: String,
    val done: Int,
    val skipped: Int
)

private data class CareerConfidenceSummary(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit = {},
    onArtifactsClick: () -> Unit = {},
    onMonthlyAuditClick: () -> Unit = {},
    onAutopsyClick: () -> Unit = {},
    onHealthCorrelationClick: () -> Unit = {},
    onWeeklyReportClick: () -> Unit = {},
    onWeeklyScoreClick: () -> Unit = {},
    onIgnitionHistoryClick: () -> Unit = {},
    onVoiceLogsClick: () -> Unit = {},
    onVoiceRepClick: () -> Unit = {},
    onPrepBuilderClick: () -> Unit = {},
    viewModel: TaskViewModel? = if (androidx.compose.ui.platform.LocalInspectionMode.current) null else viewModel()
) {
    val context = LocalContext.current
    val prefs = if (viewModel == null) com.sanket_satpute_20.ironmind.data.PrefManager.getInstance(context) else viewModel.prefManager
    val db = remember { IronMindDatabase.getDatabase(context) }
    
    // Core data streams
    val evidence by (viewModel?.identityEvidence ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())).collectAsState()
    val emergencyValveEvents by db.emergencyValveEventDao().getAllEvents().collectAsState(initial = emptyList())
    val challengeEvents by db.challengeEventDao().getAll().collectAsState(initial = emptyList())
    val challengeDayRecords by db.challengeDayRecordDao().getAll().collectAsState(initial = emptyList())
    val crucibleEvents by db.crucibleEventDao().getAll().collectAsState(initial = emptyList())
    val crucibleDayRecords by db.crucibleDayRecordDao().getAll().collectAsState(initial = emptyList())
    val clubChatEvents by db.clubChatEventDao().getAll().collectAsState(initial = emptyList())
    val allTasks by db.taskDao().getAllTasks().collectAsState(initial = emptyList())
    val focusSessions by db.focusSessionDao().getAllSessions().collectAsState(initial = emptyList())
    val dailyCheckIns by db.dailyCheckInDao().getAll().collectAsState(initial = emptyList())
    val voiceLogs by db.voiceLogDao().getAllLogs().collectAsState(initial = emptyList())
    val ignitionEntry by db.confidenceIgnitionDao().observeEntry(LocalDate.now().toString()).collectAsState(initial = null)
    val ignitionEntries by db.confidenceIgnitionDao().getAll().collectAsState(initial = emptyList())
    val configChangeEvents by db.configChangeEventDao().getAll().collectAsState(initial = emptyList())
    val emergencyValveSummary = remember(emergencyValveEvents) {
        buildEmergencyValveSummary(emergencyValveEvents)
    }
    val systemSignalsSummary = remember(focusSessions, dailyCheckIns, configChangeEvents) {
        buildSystemSignalsSummary(focusSessions, dailyCheckIns, configChangeEvents)
    }
    val challengeSummary = remember(challengeEvents, challengeDayRecords) {
        buildChallengeSummary(challengeEvents, challengeDayRecords)
    }
    val crucibleSummary = remember(crucibleEvents, crucibleDayRecords) {
        buildCrucibleSummary(crucibleEvents, crucibleDayRecords)
    }
    val clubChatSummary = remember(clubChatEvents) {
        buildClubChatSummary(clubChatEvents)
    }
    val splitMissionSummary = remember(allTasks) {
        buildSplitMissionSummary(allTasks)
    }
    val focusTasks = remember(allTasks) {
        allTasks.filterNot { it.isBreakSegment }
    }
    val streak = remember(allTasks) { prefs.streakCount }
    val totalDone = remember(focusTasks) { focusTasks.count { it.isCompleted } }
    val totalSkipped = remember(focusTasks) { focusTasks.count { it.isSkipped } }
    val total = totalDone + totalSkipped
    val completionRate = if (total > 0) (totalDone * 100f / total) else 0f
    val performanceSummary = remember(streak, totalDone, totalSkipped, completionRate) {
        buildPerformanceSummary(streak, totalDone, totalSkipped, completionRate)
    }
    val recentWindow = remember(focusTasks) {
        buildRecentPerformanceWindow(focusTasks)
    }
    val patternSpotlight = remember(
        emergencyValveSummary,
        systemSignalsSummary,
        challengeSummary,
        splitMissionSummary
    ) {
        buildPatternSpotlight(
            emergencyValveSummary = emergencyValveSummary,
            systemSignalsSummary = systemSignalsSummary,
            challengeSummary = challengeSummary,
            splitMissionSummary = splitMissionSummary
        )
    }
    val todayKey = remember { LocalDate.now().toString() }
    val todayFocusTasks = remember(focusTasks, todayKey) {
        focusTasks.filter { it.date == todayKey }
    }
    val todayDone = remember(todayFocusTasks) { todayFocusTasks.count { it.isCompleted } }
    val todaySkipped = remember(todayFocusTasks) { todayFocusTasks.count { it.isSkipped } }
    val todayPending = remember(todayFocusTasks) {
        todayFocusTasks.count { !it.isCompleted && !it.isSkipped }
    }
    val careerConfidenceSummary = remember(allTasks, voiceLogs, ignitionEntry, ignitionEntries, prefs.lastWeeklyHonestScore, prefs.lastWeeklyHonestScoreWeek) {
        buildCareerConfidenceSummary(
            tasks = allTasks,
            voiceLogs = voiceLogs,
            ignitionEntries = ignitionEntries,
            ignitionDone = ignitionEntry?.completed == true,
            ignitionRecoverable = ignitionEntry?.completed != true && java.time.LocalTime.now().isAfter(java.time.LocalTime.NOON),
            ignitionCue = ignitionEntry?.let { entry ->
                runCatching {
                    com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionLibrary
                        .couragePromptById(entry.couragePromptId)
                        .cue
                }.getOrNull()
            },
            weeklyScore = prefs.lastWeeklyHonestScore,
            weeklyScoreWeek = prefs.lastWeeklyHonestScoreWeek
        )
    }

    // Fire animation
    val fireComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.fire_streak)
    )
    val fireProgress by animateLottieCompositionAsState(
        fireComposition, iterations = LottieConstants.IterateForever
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("Stats", 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SectionHeader(
                    title = "TODAY",
                    subtitle = "Read today fast. Open deep review only if something needs explaining.",
                    color = Color.White
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                TodaySnapshotCard(
                    done = todayDone,
                    skipped = todaySkipped,
                    pending = todayPending
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                CareerConfidenceSummaryCard(
                    summary = careerConfidenceSummary,
                    onIgnitionClick = {
                        if (careerConfidenceSummary.ignitionDone) onIgnitionHistoryClick()
                        else context.startActivity(ConfidenceIgnitionActivity.createIntent(context))
                    },
                    onPrepClick = {
                        val prepTask = careerConfidenceSummary.firstPrepTask
                        if (prepTask == null) {
                            onPrepBuilderClick()
                        } else if (prepTask.isInProgress) {
                            context.startActivity(
                                WorkLockActivity.createIntent(
                                    context = context,
                                    taskId = prepTask.id,
                                    taskName = prepTask.name,
                                    taskDate = prepTask.date,
                                    taskStartTime = prepTask.startTime,
                                    taskEndTime = prepTask.endTime
                                )
                            )
                        } else {
                            context.startActivity(
                                WorkStartActivity.createIntent(
                                    context = context,
                                    taskId = prepTask.id,
                                    taskName = prepTask.name
                                )
                            )
                        }
                    },
                    onVoiceRepClick = {
                        if (careerConfidenceSummary.confidenceRepsToday > 0) {
                            onVoiceLogsClick()
                        } else {
                            onVoiceRepClick()
                        }
                    },
                    onWeeklyScoreClick = {
                        if (careerConfidenceSummary.weeklyScoreFresh && careerConfidenceSummary.weeklyScore > 0) {
                            onWeeklyReportClick()
                        } else {
                            onWeeklyScoreClick()
                        }
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactHighlightCard(
                        title = "STREAK",
                        value = streak.toString(),
                        note = if (streak > 0) "Current run" else "No active run",
                        accent = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    CompactHighlightCard(
                        title = "CLOSE RATE",
                        value = "${completionRate.toInt()}%",
                        note = if (total > 0) "All-time" else "Waiting for proof",
                        accent = when {
                            completionRate >= 80f -> SuccessGreenLight
                            completionRate >= 50f -> WarningAmber
                            else -> ErrorRedLight
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                SectionHeader(
                    title = "TREND",
                    subtitle = "Keep the short read compact. Save deeper analysis for the review console.",
                    color = TextPrimary
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { RecentPerformanceCard(summary = recentWindow) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { PerformancePulseCard(summary = performanceSummary) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                TrendScoreboardRow(
                    totalDone = totalDone,
                    totalSkipped = totalSkipped,
                    focusMinutes = systemSignalsSummary?.focusMinutesLast7Days ?: 0,
                    completedSessions = systemSignalsSummary?.completedSessionsLast7Days ?: 0
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                SectionHeader(
                    title = "PATTERN",
                    subtitle = "One strongest signal first. Don't turn a quick read into a dashboard maze.",
                    color = WarningAmber
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            patternSpotlight?.let { spotlight ->
                item { PatternSpotlightCard(spotlight = spotlight) }
            } ?: item { EmptyDataState() }

            item { Spacer(modifier = Modifier.height(28.dp)) }

            item {
                SectionHeader(
                    title = "DEEP REVIEW",
                    subtitle = "Use these when you need diagnosis, weekly review, or recovery insight beyond the short read.",
                    color = TextSecondary
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                ReviewToolsGrid(
                    unlockedCount = prefs.unlockedArtifactIds.size,
                    onHealthCorrelationClick = onHealthCorrelationClick,
                    onAutopsyClick = onAutopsyClick,
                    onMonthlyAuditClick = onMonthlyAuditClick,
                    onVoiceLogsClick = onVoiceLogsClick,
                    onArtifactsClick = onArtifactsClick,
                    onWeeklyReportClick = onWeeklyReportClick
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun CareerConfidenceSummaryCard(
    summary: CareerConfidenceSummary,
    onIgnitionClick: () -> Unit,
    onPrepClick: () -> Unit,
    onVoiceRepClick: () -> Unit,
    onWeeklyScoreClick: () -> Unit
) {
    val laneState = remember(
        summary.ignitionDone,
        summary.ignitionRecoverable,
        summary.dailyActionDone,
        summary.firstPrepTask,
        summary.confidenceRepsToday,
        summary.weeklyScore,
        summary.weeklyScoreFresh
    ) {
        deriveCareerConfidenceLaneState(
            ignitionDone = summary.ignitionDone,
            ignitionRecoverable = summary.ignitionRecoverable,
            dailyActionDone = summary.dailyActionDone,
            hasPrepMission = summary.firstPrepTask != null,
            prepMissionLive = summary.firstPrepTask?.isInProgress == true,
            confidenceRepsToday = summary.confidenceRepsToday,
            weeklyScore = summary.weeklyScore,
            weeklyScoreFresh = summary.weeklyScoreFresh
        )
    }
    val primaryAction = remember(
        summary.ignitionDone,
        summary.ignitionRecoverable,
        summary.firstPrepTask,
        summary.confidenceRepsToday,
        summary.weeklyScore,
        summary.weeklyScoreFresh
    ) {
        when {
            summary.ignitionRecoverable -> CareerConfidencePrimaryAction(
                label = "RECOVER IGNITION",
                accent = WarningAmber,
                onClick = onIgnitionClick
            )
            !summary.ignitionDone -> CareerConfidencePrimaryAction(
                label = "OPEN IGNITION",
                accent = TextPrimary,
                onClick = onIgnitionClick
            )
            summary.firstPrepTask == null -> CareerConfidencePrimaryAction(
                label = "OPEN PREP",
                accent = TextPrimary,
                onClick = onPrepClick
            )
            summary.confidenceRepsToday <= 0 -> CareerConfidencePrimaryAction(
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
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth(),
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
                            laneState.label.uppercase(java.util.Locale.ROOT),
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
                            ignitionDone = summary.ignitionDone,
                            dailyActionDone = summary.dailyActionDone,
                            prepReady = summary.firstPrepTask != null,
                            voiceDone = summary.confidenceRepsToday > 0,
                            weeklyDone = summary.weeklyScoreFresh && summary.weeklyScore > 0
                        )
                    }
                }
                val leadCue = summary.ignitionCue ?: summary.dailyActionCue
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
                accent = SuccessGreenLight
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactConfidenceProgressNode("Ignite", ignitionDone, SuccessGreen, Modifier.weight(1f))
        CompactConfidenceProgressNode("1%", dailyActionDone, SuccessGreenLight, Modifier.weight(1f))
        CompactConfidenceProgressNode("Prep", prepReady, TextPrimary, Modifier.weight(1f))
        CompactConfidenceProgressNode("Voice", voiceDone, WarningAmber, Modifier.weight(1f))
        CompactConfidenceProgressNode("Review", weeklyDone, ElectricViolet, Modifier.weight(1f))
    }
}

@Composable
private fun CompactConfidenceProgressNode(
    label: String,
    complete: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (complete) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (complete) accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (complete) accent else SurfaceElevated, CircleShape)
            )
            Text(
                label,
                color = if (complete) Color.White else ElectricViolet,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun HealthCorrelationCard(onClick: () -> Unit) {
    ReflectionToolCard(
        title = "SLEEP CORRELATION",
        subtitle = "Read rest against output",
        icon = Icons.Rounded.HealthAndSafety,
        accent = NeonCyan,
        background = SurfaceDark,
        badges = listOf("SLEEP", "FOCUS"),
        onClick = onClick
    )
}

@Composable
fun ReviewConsoleCard(onClick: () -> Unit) {
    ReflectionToolCard(
        title = "REVIEW CONSOLE",
        subtitle = "Debrief, campaign, and season in one board",
        icon = Icons.Rounded.Analytics,
        accent = WarningAmber,
        background = DeepBackground,
        badges = listOf("YESTERDAY", "WEEKLY"),
        onClick = onClick
    )
}

@Composable
fun VoiceJournalsCard(onClick: () -> Unit) {
    ReflectionToolCard(
        title = "VOICE JOURNALS",
        subtitle = "Capture pressure before it fades",
        icon = Icons.Rounded.Mic,
        accent = ElectricViolet,
        background = DeepBackground,
        badges = listOf("VOICE", "PRESSURE"),
        onClick = onClick
    )
}

@Composable
fun PremiumBarChart(
    done: Int,
    skipped: Int,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(done, skipped, 1).toFloat()
    var startAnimation by remember { mutableStateOf(false) }
    
    val doneHeightProgress by animateFloatAsState(
        targetValue = if (startAnimation) done / maxVal else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "doneHeight"
    )
    
    val skippedHeightProgress by animateFloatAsState(
        targetValue = if (startAnimation) skipped / maxVal else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "skippedHeight"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Surface(
        color = SurfaceLighter,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 60.dp.toPx()
                    val spacing = (width - (barWidth * 2)) / 3

                    // Draw Grid Lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = height - (i * height / gridLines)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Done Bar
                    val doneX = spacing
                    val doneBarHeight = height * doneHeightProgress
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(SuccessGreenLight, SuccessGreen)
                        ),
                        topLeft = Offset(doneX, height - doneBarHeight),
                        size = Size(barWidth, doneBarHeight),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )

                    // Skipped Bar
                    val skipX = spacing * 2 + barWidth
                    val skipBarHeight = height * skippedHeightProgress
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(ErrorRedLight, ErrorRed)
                        ),
                        topLeft = Offset(skipX, height - skipBarHeight),
                        size = Size(barWidth, skipBarHeight),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                    
                    // Value Labels
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 14.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        
                        if (doneHeightProgress > 0.1f) {
                            canvas.nativeCanvas.drawText(
                                done.toString(),
                                doneX + barWidth / 2,
                                height - doneBarHeight - 10.dp.toPx(),
                                paint
                            )
                        }
                        
                        if (skippedHeightProgress > 0.1f) {
                            canvas.nativeCanvas.drawText(
                                skipped.toString(),
                                skipX + barWidth / 2,
                                height - skipBarHeight - 10.dp.toPx(),
                                paint
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                ChartLabel("DONE", SuccessGreenLight, Modifier.weight(1f))
                ChartLabel("SKIPPED", ErrorRedLight, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ChartLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
    }
}

@Composable
fun ArtifactsAccessCard(unlockedCount: Int, onClick: () -> Unit) {
    ReflectionToolCard(
        title = "LEGENDARY ARTIFACTS",
        subtitle = "$unlockedCount unlocked relics",
        icon = Icons.Rounded.WorkspacePremium,
        accent = GoldXP,
        background = DeepBackground,
        badges = listOf("RELICS", "PROOF"),
        onClick = onClick
    )
}

@Composable
private fun ReflectionToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    background: Color,
    badges: List<String> = emptyList(),
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = background,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.9.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.56f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                if (badges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        badges.take(2).forEach { badge ->
                            Surface(
                                color = accent.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
                            ) {
                                Text(
                                    badge,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = accent.copy(alpha = 0.86f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun IdentityEvidenceList(tasks: List<Task>, userName: String) {
    val name = userName.ifEmpty { "You" }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tasks.take(3).forEach { task ->
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Evidence that $name is a person who follows through on ${task.name}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun HeroStreakCard(streak: Int, composition: LottieComposition?, progress: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepBackground, Color.Black)
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("CURRENT STREAK", 
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$streak DAYS",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (streak >= 7) "UNSTOPPABLE FORCE ⚡" else "REACH 7 DAYS TO UNLOCK IRON STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    color: Color
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TodaySnapshotCard(
    done: Int,
    skipped: Int,
    pending: Int
) {
    val total = done + skipped + pending
    val accent = when {
        total == 0 -> TextPrimary
        pending == 0 && skipped == 0 -> SuccessGreenLight
        pending == 0 -> WarningAmber
        else -> WarningAmber
    }
    val headline = when {
        total == 0 -> "No missions loaded for today"
        pending == 0 && skipped == 0 -> "Clean day secured"
        pending == 0 -> "Day resolved"
        done == 0 && skipped == 0 -> "Board is waiting"
        else -> "$pending still open"
    }
    val body = when {
        total == 0 -> "As soon as today's missions exist, this screen will summarize them here."
        pending == 0 && skipped == 0 -> "Everything scheduled for today has been completed."
        pending == 0 -> "$done done and $skipped skipped. Nothing active is left on the board."
        else -> "$done done, $skipped skipped, and $pending still need a finish or a decision."
    }

    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "LIVE BOARD",
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                headline,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                body,
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSmallCard(
                    label = "DONE",
                    value = done.toString(),
                    color = SuccessGreenLight,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "SKIPPED",
                    value = skipped.toString(),
                    color = ErrorRedLight,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "OPEN",
                    value = pending.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PerformancePulseCard(summary: PerformanceSummary) {
    Surface(
        color = SurfaceLighter,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, summary.accent.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "RECENT READ",
                color = summary.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                summary.headline,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                summary.body,
                color = Color.White.copy(alpha = 0.74f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun PatternSubsectionHeader(
    title: String,
    subtitle: String,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                title,
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RecentPerformanceCard(summary: RecentPerformanceWindow) {
    val completionColor = when {
        summary.done + summary.skipped == 0 -> TextPrimary
        summary.completionRate >= 0.8f -> SuccessGreenLight
        summary.completionRate >= 0.5f -> WarningAmber
        else -> ErrorRedLight
    }
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "LAST 7 DAYS",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (summary.done + summary.skipped > 0) "${summary.completionRate.toInt()}% close rate recently"
                    else "No resolved missions in the last 7 days yet",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (summary.done + summary.skipped > 0) "${summary.done} secured / ${summary.skipped} buried in the recent window"
                    else "This card will wake up as soon as the recent window has real mission data.",
                    color = Color.White.copy(alpha = 0.64f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProgressRing(
                    progress = summary.completionRate.coerceIn(0f, 1f),
                    size = 72.dp,
                    strokeWidth = 7.dp,
                    progressColor = completionColor,
                    label = "${(summary.completionRate.coerceIn(0f, 1f) * 100).toInt()}%"
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            SevenDayTrendStrip(days = summary.dailyBreakdown)
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniMetricChip(
                    label = "DONE",
                    value = summary.done.toString(),
                    accent = SuccessGreenLight,
                    modifier = Modifier.weight(1f)
                )
                MiniMetricChip(
                    label = "SKIPPED",
                    value = summary.skipped.toString(),
                    accent = ErrorRedLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SevenDayTrendStrip(days: List<RecentDayPerformance>) {
    val maxResolved = days.maxOfOrNull { it.done + it.skipped }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val resolved = day.done + day.skipped
            val ratio = if (resolved == 0) 0.16f else resolved.toFloat() / maxResolved.toFloat()
            val barColor = when {
                resolved == 0 -> Color.White.copy(alpha = 0.12f)
                day.skipped == 0 -> SuccessGreenLight
                day.done >= day.skipped -> WarningAmber
                else -> ErrorRedLight
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        barColor.copy(alpha = 0.45f),
                                        barColor
                                    )
                                )
                            )
                    )
                }
                Text(
                    text = day.label,
                    color = Color.White.copy(alpha = 0.56f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TimeframeBadgeRow(labels: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEach { label ->
            Surface(
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
fun PremiumCompletionCard(rate: Float) {
    Surface(
        color = SurfaceLighter,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "${rate.toInt()}%",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = when {
                        rate >= 80 -> SuccessGreen
                        rate >= 50 -> WarningAmber
                        else -> ErrorRed
                    }
                )
                Text(
                    "ALL-TIME CLOSE RATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProgressRing(
                    progress = rate / 100f,
                    size = 84.dp,
                    strokeWidth = 8.dp,
                    progressColor = when {
                        rate >= 80 -> SuccessGreen
                        rate >= 50 -> WarningAmber
                        else -> ErrorRed
                    },
                    label = "${rate.toInt()}%"
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                when {
                    rate >= 80 -> "You are closing most resolved missions cleanly."
                    rate >= 50 -> "The baseline is workable, but skips are still eating into the board."
                    else -> "Completion is getting buried by skips. This needs a reset, not more pressure."
                },
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun StatSmallCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceLighter,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.14f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = color)
        }
    }
}

@Composable
private fun MiniMetricChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                value,
                color = accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun EmptyDataState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("NO DATA COLLECTED YET", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
private fun CompactHighlightCard(
    title: String,
    value: String,
    note: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceLighter,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.9.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                note,
                color = Color.White.copy(alpha = 0.58f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun TrendScoreboardRow(
    totalDone: Int,
    totalSkipped: Int,
    focusMinutes: Int,
    completedSessions: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatSmallCard(
            label = "DONE",
            value = totalDone.toString(),
            color = SuccessGreenLight,
            modifier = Modifier.weight(1f)
        )
        StatSmallCard(
            label = "SKIPPED",
            value = totalSkipped.toString(),
            color = ErrorRedLight,
            modifier = Modifier.weight(1f)
        )
        StatSmallCard(
            label = "FOCUS",
            value = if (focusMinutes > 0) "${focusMinutes}m" else completedSessions.toString(),
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PatternSpotlightCard(
    spotlight: PatternSpotlight
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, spotlight.accent.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(
                        spotlight.accent.copy(alpha = 0.16f),
                        SurfaceDark,
                        DeepBackground
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SPOTLIGHT",
                        color = spotlight.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        color = spotlight.accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            "MAIN SIGNAL",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.7.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    spotlight.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 25.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    spotlight.summary,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        spotlight.accent.copy(alpha = 0.7f),
                                        spotlight.accent
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewToolsGrid(
    unlockedCount: Int,
    onHealthCorrelationClick: () -> Unit,
    onAutopsyClick: () -> Unit,
    onMonthlyAuditClick: () -> Unit,
    onVoiceLogsClick: () -> Unit,
    onArtifactsClick: () -> Unit,
    onWeeklyReportClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { ReviewConsoleCard(onClick = onAutopsyClick) }
            Box(modifier = Modifier.weight(1f)) { VoiceJournalsCard(onClick = onVoiceLogsClick) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { HealthCorrelationCard(onClick = onHealthCorrelationClick) }
            Box(modifier = Modifier.weight(1f)) {
                ArtifactsAccessCard(
                    unlockedCount = unlockedCount,
                    onClick = onArtifactsClick
                )
            }
        }
    }
}

private fun buildPerformanceSummary(
    streak: Int,
    totalDone: Int,
    totalSkipped: Int,
    completionRate: Float
): PerformanceSummary {
    return when {
        totalDone == 0 && totalSkipped == 0 -> PerformanceSummary(
            headline = "No execution proof yet",
            body = "The board has not produced enough resolved missions to judge your pattern. Secure a few missions before looking for deeper meaning.",
            accent = WarningAmber
        )
        completionRate >= 85f && streak >= 7 -> PerformanceSummary(
            headline = "Momentum is holding at a high standard",
            body = "Your close rate is elite and the streak is reinforcing it. Protect the first mission of each day and avoid unnecessary complexity.",
            accent = SuccessGreenLight
        )
        completionRate >= 70f -> PerformanceSummary(
            headline = "Strong follow-through with room to tighten",
            body = "You are converting more promises than you bury. The next gain comes from reducing skips, not increasing board size.",
            accent = SuccessGreen
        )
        totalSkipped > totalDone -> PerformanceSummary(
            headline = "Skip pressure is outrunning execution",
            body = "Too many promises are being buried compared to what gets secured. Rebuild smaller, earlier, and more realistically.",
            accent = ErrorRedLight
        )
        else -> PerformanceSummary(
            headline = "The board is still recoverable",
            body = "Execution is mixed, not lost. Reduce friction, protect your highest-value mission earlier, and keep the next few days cleaner than the last few.",
            accent = WarningAmber
        )
    }
}

private fun buildPatternSpotlight(
    emergencyValveSummary: EmergencyValveSummary?,
    systemSignalsSummary: SystemSignalsSummary?,
    challengeSummary: ChallengeSummary?,
    splitMissionSummary: SplitMissionSummary?
): PatternSpotlight? {
    if (emergencyValveSummary != null && emergencyValveSummary.totalUsesLast7Days > 0) {
        val topTrigger = emergencyValveSummary.topTriggerLabel ?: "Unknown trigger"
        return PatternSpotlight(
            title = "Emergency pressure is active",
            summary = "${emergencyValveSummary.totalUsesLast7Days} valve uses in the last 7 days. Most common trigger: $topTrigger. Return-to-mission rate is ${emergencyValveSummary.returnToMissionRate}%.",
            accent = NeonCyan
        )
    }
    if (systemSignalsSummary != null && systemSignalsSummary.sleepLockEmergencyExitsLast30Days > 0) {
        return PatternSpotlight(
            title = "Night boundary is under pressure",
            summary = "${systemSignalsSummary.sleepLockEmergencyExitsLast30Days} night emergency exits were used in the last 30 days. That usually means the sleep window is colliding with real life or needs a cleaner boundary.",
            accent = WarningAmber
        )
    }
    if (challengeSummary != null && challengeSummary.verifiedMorningsLast30Days > 0) {
        return PatternSpotlight(
            title = "Morning discipline is producing real proof",
            summary = "${challengeSummary.verifiedMorningsLast30Days} verified mornings landed in the last 30 days. Best run so far is ${challengeSummary.bestRunLength} days.",
            accent = GoldXP
        )
    }
    if (splitMissionSummary != null && splitMissionSummary.chainsSeen > 0) {
        return PatternSpotlight(
            title = "Split missions are shaping execution",
            summary = "${splitMissionSummary.fullySecuredChains} chains were fully cleared, ${splitMissionSummary.compromisedChains} were compromised, and the average chain size is ${splitMissionSummary.averagePartsPerChain} parts.",
            accent = TextPrimary
        )
    }
    return null
}

private fun buildRecentPerformanceWindow(tasks: List<Task>): RecentPerformanceWindow {
    val today = LocalDate.now()
    val recentTasks = tasks.filter { task ->
        runCatching {
            val taskDate = LocalDate.parse(task.date)
            !taskDate.isBefore(today.minusDays(6))
        }.getOrDefault(false)
    }
    val done = recentTasks.count { it.isCompleted }
    val skipped = recentTasks.count { it.isSkipped }
    val resolved = done + skipped
    val completionRate = if (resolved > 0) (done * 100f / resolved) else 0f
    val dailyBreakdown = (6L downTo 0L).map { offset ->
        val date = today.minusDays(offset)
        val dayTasks = recentTasks.filter { it.date == date.toString() }
        RecentDayPerformance(
            label = date.dayOfWeek.name.take(3),
            done = dayTasks.count { it.isCompleted },
            skipped = dayTasks.count { it.isSkipped }
        )
    }
    return RecentPerformanceWindow(
        done = done,
        skipped = skipped,
        completionRate = completionRate,
        dailyBreakdown = dailyBreakdown
    )
}

private fun buildCareerConfidenceSummary(
    tasks: List<Task>,
    voiceLogs: List<com.sanket_satpute_20.ironmind.data.VoiceLog>,
    ignitionEntries: List<ConfidenceIgnitionEntry>,
    ignitionDone: Boolean,
    ignitionRecoverable: Boolean,
    ignitionCue: String?,
    weeklyScore: Int,
    weeklyScoreWeek: String
): CareerConfidenceSummary {
    val today = LocalDate.now()
    val todayKey = today.toString()
    val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
    val todayTasks = tasks.filter { it.date == todayKey && !it.isBreakSegment }
    val ignitionByDate = ignitionEntries.associateBy { it.date }
    val ignitionRecentDays = (6L downTo 0L).map { offset ->
        val date = today.minusDays(offset).toString()
        ignitionByDate[date]?.completed == true
    }
    var ignitionStreak = 0
    var cursor = today
    var checkedDays = 0
    while (checkedDays < 30 && ignitionByDate[cursor.toString()]?.completed == true) {
        ignitionStreak++
        cursor = cursor.minusDays(1)
        checkedDays++
    }
    return CareerConfidenceSummary(
        ignitionDone = ignitionDone,
        ignitionRecoverable = ignitionRecoverable,
        ignitionCue = ignitionCue,
        ignitionStreak = ignitionStreak,
        ignitionCompletedLast7Days = ignitionRecentDays.count { it },
        ignitionRecentDays = ignitionRecentDays,
        dailyActionDone = todayTasks.firstOrNull { it.origin == DAILY_ONE_PERCENT_ORIGIN }?.isCompleted == true,
        dailyActionCue = todayTasks.firstOrNull { it.origin == DAILY_ONE_PERCENT_ORIGIN }?.focusNotes?.takeIf { it.isNotBlank() },
        firstPrepTask = todayTasks
            .filter { it.taskType.uppercase() in setOf("DSA", "SYSTEM_DESIGN", "BEHAVIORAL", "PROJECT_REVIEW") }
            .sortedWith(
                compareBy<Task> { !it.isInProgress }
                    .thenBy { it.isCompleted || it.isSkipped }
                    .thenBy { it.startTime }
            )
            .firstOrNull(),
        prepMissionsToday = todayTasks.count {
            it.taskType.uppercase() in setOf("DSA", "SYSTEM_DESIGN", "BEHAVIORAL", "PROJECT_REVIEW")
        },
        confidenceRepsToday = voiceLogs.count { it.date == todayKey && it.entryType == "CONFIDENCE" },
        weeklyScore = weeklyScore,
        weeklyScoreFresh = weeklyScoreWeek == currentWeekStart
    )
}

private data class EmergencyValveSummary(
    val totalUsesLast7Days: Int,
    val completedUsesLast7Days: Int,
    val abandonedUsesLast7Days: Int,
    val returnToMissionRate: Int,
    val abandonRate: Int,
    val topTriggerLabel: String?,
    val topRecoveryActionLabel: String?,
    val topMissionContextLabel: String?,
    val topTimeWindowLabel: String?,
    val topTriggerBreakdown: List<Pair<String, Int>>,
    val topActionBreakdown: List<Pair<String, Int>>
)

private data class SystemSignalsSummary(
    val focusMinutesLast7Days: Int,
    val completedSessionsLast7Days: Int,
    val avgEnergyLast7Days: Int?,
    val avgStressLast7Days: Int?,
    val topMoodLast7Days: String?,
    val configChangesLast7Days: Int,
    val topConfigChangeLabel: String?,
    val sleepLockEmergencyExitsLast30Days: Int
)

private data class ChallengeSummary(
    val verifiedMorningsLast30Days: Int,
    val totalRunStarts: Int,
    val totalResets: Int,
    val bestRunLength: Int,
    val topRoutine: String?,
    val totalCompletedRuns: Int,
    val strongestWeekday: String?,
    val weekdayBreakdown: List<Pair<String, Int>>
)

private data class CrucibleSummary(
    val verifiedDaysLast30Days: Int,
    val totalRunsStarted: Int,
    val totalRunsFailed: Int,
    val totalRunsCompleted: Int,
    val bestRunLength: Int,
    val topCrucibleTitle: String?,
    val topPenaltyLabel: String?,
    val weekdayBreakdown: List<Pair<String, Int>>
)

private data class ClubChatSummary(
    val opensLast30Days: Int,
    val readerUnlocks: Int,
    val fullUnlocks: Int,
    val trackedPostsLast30Days: Int,
    val firstPostPromptsShown: Int,
    val topMembershipTier: String?,
    val latestEventLabel: String?
)

private fun buildEmergencyValveSummary(events: List<EmergencyValveEvent>): EmergencyValveSummary? {
    if (events.isEmpty()) return null

    val today = LocalDate.now()
    val recentEvents = events.filter { event ->
        val eventDate = Instant.ofEpochMilli(event.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        !eventDate.isBefore(today.minusDays(6))
    }

    if (recentEvents.isEmpty()) return null

    val completedEvents = recentEvents.filter { it.completed }
    val abandonedEvents = recentEvents.filter { !it.completed || it.selectedRecoveryAction == "ABANDONED_RESET" }
    val returnToMissionCount = completedEvents.count {
        normalizeEmergencyValveActionLabel(it.selectedRecoveryAction) == "Return To Mission"
    }
    val topTrigger = recentEvents
        .groupingBy { humanizeEmergencyValveLabel(it.triggerReason) }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
    val topAction = completedEvents
        .groupingBy { humanizeEmergencyValveLabel(it.selectedRecoveryAction) }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
    val topMissionContext = recentEvents
        .filter { it.activeTaskName.isNotBlank() }
        .groupingBy { it.activeTaskName }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
    val topTimeWindow = recentEvents
        .groupingBy { emergencyValveTimeWindow(it.timestamp) }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
    val triggerBreakdown = recentEvents
        .groupingBy { humanizeEmergencyValveLabel(it.triggerReason) }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(3)
        .map { it.key to it.value }
    val actionBreakdown = completedEvents
        .groupingBy { humanizeEmergencyValveLabel(it.selectedRecoveryAction) }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(3)
        .map { it.key to it.value }

    return EmergencyValveSummary(
        totalUsesLast7Days = recentEvents.size,
        completedUsesLast7Days = completedEvents.size,
        abandonedUsesLast7Days = abandonedEvents.size,
        returnToMissionRate = if (completedEvents.isNotEmpty()) (returnToMissionCount * 100) / completedEvents.size else 0,
        abandonRate = (abandonedEvents.size * 100) / recentEvents.size,
        topTriggerLabel = topTrigger,
        topRecoveryActionLabel = topAction,
        topMissionContextLabel = topMissionContext,
        topTimeWindowLabel = topTimeWindow,
        topTriggerBreakdown = triggerBreakdown,
        topActionBreakdown = actionBreakdown
    )
}

private fun humanizeEmergencyValveLabel(value: String): String {
    if (value.isBlank()) return "Unknown"
    if (value.contains(":")) {
        val prefix = value.substringBefore(":")
        return when (prefix) {
            "OPEN_EMERGENCY_APP" -> "Open Emergency App"
            else -> humanizeEmergencyValveLabel(prefix)
        }
    }
    return value
        .lowercase()
        .split('_')
        .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
}

private fun buildSystemSignalsSummary(
    focusSessions: List<FocusSession>,
    dailyCheckIns: List<DailyCheckIn>,
    configChanges: List<ConfigChangeEvent>
): SystemSignalsSummary? {
    val cutoff = LocalDate.now().minusDays(6)
    fun inWindow(timestamp: Long): Boolean {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return !date.isBefore(cutoff)
    }

    val weeklySessions = focusSessions.filter { it.endTimestamp != null && inWindow(it.startTimestamp) }
    val weeklyCheckIns = dailyCheckIns.filter { inWindow(it.timestamp) }
    val weeklyConfigChanges = configChanges.filter { inWindow(it.timestamp) }

    if (weeklySessions.isEmpty() && weeklyCheckIns.isEmpty() && weeklyConfigChanges.isEmpty()) return null

    return SystemSignalsSummary(
        focusMinutesLast7Days = weeklySessions.sumOf { it.actualDurationMinutes },
        completedSessionsLast7Days = weeklySessions.count { it.completed },
        avgEnergyLast7Days = weeklyCheckIns.takeIf { it.isNotEmpty() }?.let { it.map { item -> item.energyScore }.average().toInt() },
        avgStressLast7Days = weeklyCheckIns.takeIf { it.isNotEmpty() }?.let { it.map { item -> item.stressScore }.average().toInt() },
        topMoodLast7Days = weeklyCheckIns.groupingBy { it.moodWord.ifBlank { "Unknown" } }.eachCount().maxByOrNull { it.value }?.key,
        configChangesLast7Days = weeklyConfigChanges.size,
        topConfigChangeLabel = weeklyConfigChanges
            .groupingBy { it.configType.lowercase().replace('_', ' ').replaceFirstChar { ch -> ch.uppercase() } }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key,
        sleepLockEmergencyExitsLast30Days = configChanges.count { it.configType == "SLEEP_LOCK_EMERGENCY_EXIT" }
    )
}

private fun buildChallengeSummary(
    challengeEvents: List<ChallengeEvent>,
    challengeDayRecords: List<ChallengeDayRecord>
): ChallengeSummary? {
    if (challengeEvents.isEmpty() && challengeDayRecords.isEmpty()) return null

    val cutoff = LocalDate.now().minusDays(29)
    fun inWindow(dateString: String): Boolean {
        val date = runCatching { LocalDate.parse(dateString) }.getOrNull() ?: return false
        return !date.isBefore(cutoff)
    }

    val recentRecords = challengeDayRecords.filter { inWindow(it.date) }
    val bestRunLength = challengeDayRecords
        .groupBy { it.runId }
        .maxOfOrNull { (_, records) -> records.maxOfOrNull { it.dayNumber } ?: 0 }
        ?: 0
    val topRoutine = recentRecords
        .groupingBy {
            when (it.routineType) {
                "MONK" -> "Monk Mode"
                "WARRIOR" -> "Warrior Mode"
                "BALANCED" -> "Balanced Mode"
                else -> it.routineType.ifBlank { "Unknown" }
            }
        }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
    val weekdayBreakdown = recentRecords
        .groupingBy {
            runCatching { LocalDate.parse(it.date).dayOfWeek.name.lowercase().replaceFirstChar { ch -> ch.uppercase() } }
                .getOrDefault("Unknown")
        }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(7)
        .map { it.key to it.value }

    return ChallengeSummary(
        verifiedMorningsLast30Days = recentRecords.count { it.completed },
        totalRunStarts = challengeEvents.count { it.eventType == "STARTED" },
        totalResets = challengeEvents.count { it.eventType == "RESET" },
        bestRunLength = bestRunLength,
        topRoutine = topRoutine,
        totalCompletedRuns = challengeEvents.count { it.eventType == "COMPLETED_CHALLENGE" },
        strongestWeekday = weekdayBreakdown.maxByOrNull { it.second }?.first,
        weekdayBreakdown = weekdayBreakdown
    )
}

private fun buildCrucibleSummary(
    crucibleEvents: List<CrucibleEvent>,
    crucibleDayRecords: List<CrucibleDayRecord>
): CrucibleSummary? {
    if (crucibleEvents.isEmpty() && crucibleDayRecords.isEmpty()) return null

    val cutoff = LocalDate.now().minusDays(29)
    fun inWindow(dateString: String): Boolean {
        val date = runCatching { LocalDate.parse(dateString) }.getOrNull() ?: return false
        return !date.isBefore(cutoff)
    }

    val recentRecords = crucibleDayRecords.filter { inWindow(it.date) }
    val recentEvents = crucibleEvents.filter { inWindow(it.date) }
    val bestRunLength = crucibleDayRecords
        .groupBy { it.runId }
        .maxOfOrNull { (_, records) -> records.maxOfOrNull { it.dayNumber } ?: 0 }
        ?: 0
    val weekdayBreakdown = recentRecords
        .groupingBy {
            runCatching { LocalDate.parse(it.date).dayOfWeek.name.lowercase().replaceFirstChar { ch -> ch.uppercase() } }
                .getOrDefault("Unknown")
        }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(7)
        .map { it.key to it.value }

    return CrucibleSummary(
        verifiedDaysLast30Days = recentRecords.count { it.completed },
        totalRunsStarted = crucibleEvents.count { it.eventType == "STARTED" },
        totalRunsFailed = crucibleEvents.count { it.eventType == "FAILED" },
        totalRunsCompleted = crucibleEvents.count { it.eventType == "COMPLETED_CRUCIBLE" },
        bestRunLength = bestRunLength,
        topCrucibleTitle = recentEvents
            .groupingBy { it.crucibleTitle.ifBlank { "Unknown Crucible" } }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key,
        topPenaltyLabel = recentEvents
            .map { it.penaltyType }
            .filter { it.isNotBlank() }
            .groupingBy { it.lowercase().replace('_', ' ').replaceFirstChar { ch -> ch.uppercase() } }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key,
        weekdayBreakdown = weekdayBreakdown
    )
}

private fun buildClubChatSummary(events: List<ClubChatEvent>): ClubChatSummary? {
    if (events.isEmpty()) return null

    val cutoff = LocalDate.now().minusDays(29)
    val recentEvents = events.filter { event ->
        val date = runCatching { LocalDate.parse(event.date) }.getOrNull() ?: return@filter false
        !date.isBefore(cutoff)
    }
    if (recentEvents.isEmpty()) return null

    return ClubChatSummary(
        opensLast30Days = recentEvents.count { it.eventType == "CLUB_OPENED" },
        readerUnlocks = recentEvents.count { it.eventType == "READER_ACCESS_UNLOCKED" },
        fullUnlocks = recentEvents.count { it.eventType == "FULL_MEMBER_UNLOCKED" },
        trackedPostsLast30Days = recentEvents
            .filter { it.eventType == "MESSAGE_POSTED" }
            .sumOf { it.postCountDelta.coerceAtLeast(0) },
        firstPostPromptsShown = recentEvents.count { it.eventType == "FIRST_POST_PROMPT_SHOWN" },
        topMembershipTier = recentEvents
            .map { it.membershipTier }
            .filter { it.isNotBlank() }
            .groupingBy { humanizeClubMembershipTier(it) }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key,
        latestEventLabel = recentEvents.maxByOrNull { it.timestamp }?.let {
            humanizeClubEventType(it.eventType)
        }
    )
}

private fun buildSplitMissionSummary(tasks: List<Task>): SplitMissionSummary? {
    if (tasks.isEmpty()) return null

    val cutoff = LocalDate.now().minusDays(29)
    val recentTasks = tasks.filter { task ->
        val date = runCatching { LocalDate.parse(task.date) }.getOrNull() ?: return@filter false
        !date.isBefore(cutoff)
    }

    val chains = recentTasks
        .filter { it.parentMissionId.isNotBlank() && it.segmentCount > 1 }
        .groupBy { it.parentMissionId }
        .values

    if (chains.isEmpty()) return null

    val focusChains = chains.map { chain -> chain.sortedBy { it.startTime }.filterNot { it.isBreakSegment } }
        .filter { it.isNotEmpty() }
    if (focusChains.isEmpty()) return null

    val fullySecured = focusChains.count { chain -> chain.all { it.isCompleted } && chain.none { it.isSkipped } }
    val compromised = focusChains.count { chain -> chain.any { it.isSkipped } }
    val active = focusChains.count { chain -> chain.any { !it.isCompleted && !it.isSkipped } }
    val averageParts = focusChains.map { it.size }.average().toInt().coerceAtLeast(1)
    val longestChain = focusChains.maxOfOrNull { it.size } ?: 0

    val mostCommonSpacing = chains
        .mapNotNull { chain ->
            val ordered = chain.sortedBy { it.startTime }
            val breakTask = ordered.firstOrNull { it.isBreakSegment }
            if (breakTask != null) {
                val breakStart = runCatching { java.time.LocalTime.parse(breakTask.startTime) }.getOrNull()
                val breakEnd = runCatching { java.time.LocalTime.parse(breakTask.endTime) }.getOrNull()
                if (breakStart != null && breakEnd != null) {
                    val minutes = java.time.Duration.between(breakStart, breakEnd).toMinutes().toInt()
                    "Recovery gap ${minutes}m"
                } else null
            } else {
                val focus = ordered.filterNot { it.isBreakSegment }
                if (focus.size >= 2) {
                    val firstEnd = runCatching { java.time.LocalTime.parse(focus[0].endTime) }.getOrNull()
                    val secondStart = runCatching { java.time.LocalTime.parse(focus[1].startTime) }.getOrNull()
                    if (firstEnd != null && secondStart != null) {
                        val minutes = java.time.Duration.between(firstEnd, secondStart).toMinutes().toInt()
                        if (minutes % 60 == 0) "After delay ${minutes / 60}h" else "After delay ${minutes}m"
                    } else null
                } else null
            }
        }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?: "No repeated spacing pattern yet"

    return SplitMissionSummary(
        chainsSeen = focusChains.size,
        fullySecuredChains = fullySecured,
        compromisedChains = compromised,
        activeChains = active,
        averagePartsPerChain = averageParts,
        longestChain = longestChain,
        mostCommonSpacing = mostCommonSpacing
    )
}

private fun humanizeClubMembershipTier(value: String): String {
    return when (value) {
        "FULL_MEMBER" -> "Full Member"
        "READER" -> "Reader"
        else -> value.lowercase().replace('_', ' ').replaceFirstChar { ch -> ch.uppercase() }
    }
}

private fun humanizeClubEventType(value: String): String {
    return when (value) {
        "CLUB_OPENED" -> "Club opened"
        "READER_ACCESS_UNLOCKED" -> "Reader access earned"
        "FULL_MEMBER_UNLOCKED" -> "Full member unlocked"
        "FIRST_POST_PROMPT_SHOWN" -> "First-post prompt shown"
        "MESSAGE_POSTED" -> "Post activity tracked"
        else -> value.lowercase().replace('_', ' ').replaceFirstChar { ch -> ch.uppercase() }
    }
}

private fun normalizeEmergencyValveActionLabel(value: String): String {
    return when {
        value.startsWith("OPEN_EMERGENCY_APP") -> "Open Emergency App"
        else -> humanizeEmergencyValveLabel(value)
    }
}

private fun emergencyValveTimeWindow(timestamp: Long): String {
    val hour = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).hour
    return when (hour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..21 -> "Evening"
        else -> "Night"
    }
}

@Composable
private fun EmergencyValveInsightsCard(summary: EmergencyValveSummary) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSmallCard(
                    label = "VALVE USES",
                    value = summary.totalUsesLast7Days.toString(),
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "COMPLETED",
                    value = summary.completedUsesLast7Days.toString(),
                    color = SuccessGreenLight,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "ABANDONED",
                    value = summary.abandonedUsesLast7Days.toString(),
                    color = ErrorRedLight,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            summary.topTriggerLabel?.let { trigger ->
                Text(
                    "Most common trigger: $trigger",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                "Return-to-mission rate: ${summary.returnToMissionRate}%",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Abandon rate: ${summary.abandonRate}%",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = summary.topRecoveryActionLabel?.let { "Most chosen reset path: $it" }
                    ?: "No completed reset path recorded yet this week.",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            summary.topMissionContextLabel?.let { mission ->
                Text(
                    "Most pressured mission context: $mission",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            summary.topTimeWindowLabel?.let { window ->
                Text(
                    "Most common pressure window: $window",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            BreakdownSection(
                title = "TOP TRIGGERS",
                entries = summary.topTriggerBreakdown,
                accentColor = NeonCyan
            )
            Spacer(modifier = Modifier.height(12.dp))
            BreakdownSection(
                title = "TOP RECOVERY PATHS",
                entries = summary.topActionBreakdown,
                accentColor = SuccessGreenLight
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Use this to spot whether Reset Protocol helps you re-enter the mission or mostly buys time before the next clean decision.",
                color = ElectricViolet,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SystemSignalsCard(summary: SystemSignalsSummary) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSmallCard(
                    label = "FOCUS MIN",
                    value = summary.focusMinutesLast7Days.toString(),
                    color = SuccessGreenLight,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "SESSIONS",
                    value = summary.completedSessionsLast7Days.toString(),
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "RULE SHIFTS",
                    value = summary.configChangesLast7Days.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Check-in baseline: energy ${summary.avgEnergyLast7Days ?: "-"} / stress ${summary.avgStressLast7Days ?: "-"}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                buildString {
                    summary.topMoodLast7Days?.let { append("Top mood: $it") }
                    if (summary.topMoodLast7Days != null && summary.topConfigChangeLabel != null) append(" · ")
                    summary.topConfigChangeLabel?.let { append("Most changed rule: $it") }
                }.ifBlank { "Focus sessions, check-ins, and rule changes will appear here as the system learns your patterns." },
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            if (summary.sleepLockEmergencyExitsLast30Days > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Night emergency exits in the last 30 days: ${summary.sleepLockEmergencyExitsLast30Days}",
                    color = WarningAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Repeated night exits usually mean the bedtime boundary is clashing with real life or the sleep window is set too aggressively.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ChallengeHistoryCard(summary: ChallengeSummary) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GoldXP.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSmallCard(
                    label = "30D MORNINGS",
                    value = summary.verifiedMorningsLast30Days.toString(),
                    color = GoldXP,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "BEST RUN",
                    value = summary.bestRunLength.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "RESETS",
                    value = summary.totalResets.toString(),
                    color = ErrorRedLight,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Challenge runs started: ${summary.totalRunStarts} · Completed runs: ${summary.totalCompletedRuns}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                summary.topRoutine?.let { "Most used morning stack: $it" }
                    ?: "Routine preference will appear once more verified mornings are recorded.",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            summary.strongestWeekday?.let { weekday ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Strongest 5 AM day: $weekday",
                    color = WarningAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (summary.weekdayBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                ChallengeWeekdayTrend(summary.weekdayBreakdown)
            }
        }
    }
}

@Composable
private fun ChallengeWeekdayTrend(entries: List<Pair<String, Int>>) {
    val maxCount = entries.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "30-DAY MORNING TREND",
            color = GoldXP,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        entries.forEach { (label, count) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = Color.White, fontSize = 12.sp)
                    Text(count.toString(), color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(count.toFloat() / maxCount.toFloat())
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(WarningAmber, WarningAmber)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun CrucibleHistoryCard(summary: CrucibleSummary) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSmallCard(
                    label = "30D DAYS",
                    value = summary.verifiedDaysLast30Days.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "BEST RUN",
                    value = summary.bestRunLength.toString(),
                    color = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "FAILS",
                    value = summary.totalRunsFailed.toString(),
                    color = ErrorRedLight,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Runs started: ${summary.totalRunsStarted} · Full clears: ${summary.totalRunsCompleted}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                buildString {
                    summary.topCrucibleTitle?.let { append("Most active crucible: $it") }
                    if (summary.topCrucibleTitle != null && summary.topPenaltyLabel != null) append(" · ")
                    summary.topPenaltyLabel?.let { append("Most carried penalty: $it") }
                }.ifBlank { "Crucible history will appear here as seasonal runs are completed." },
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            if (summary.weekdayBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                ChallengeWeekdayTrend(summary.weekdayBreakdown)
            }
        }
    }
}

@Composable
private fun ClubChatAnalyticsCard(summary: ClubChatSummary) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSmallCard(
                    label = "30D OPENS",
                    value = summary.opensLast30Days.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "POSTS",
                    value = summary.trackedPostsLast30Days.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "FULL UNLOCKS",
                    value = summary.fullUnlocks.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Reader unlocks: ${summary.readerUnlocks} · First-post prompts: ${summary.firstPostPromptsShown}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                buildString {
                    summary.topMembershipTier?.let { append("Most active membership state: $it") }
                    if (summary.topMembershipTier != null && summary.latestEventLabel != null) append(" · ")
                    summary.latestEventLabel?.let { append("Latest signal: $it") }
                }.ifBlank { "Club unlocks, opens, and post activity will surface here as the room becomes part of your 5 AM discipline loop." },
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SplitMissionAnalyticsCard(summary: SplitMissionSummary) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSmallCard(
                    label = "30D CHAINS",
                    value = summary.chainsSeen.toString(),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "FULL CLEARS",
                    value = summary.fullySecuredChains.toString(),
                    color = SuccessGreenLight,
                    modifier = Modifier.weight(1f)
                )
                StatSmallCard(
                    label = "ACTIVE",
                    value = summary.activeChains.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Compromised chains: ${summary.compromisedChains} · Avg parts: ${summary.averagePartsPerChain} · Longest chain: ${summary.longestChain}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Most common split spacing: ${summary.mostCommonSpacing}",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun BreakdownSection(
    title: String,
    entries: List<Pair<String, Int>>,
    accentColor: Color
) {
    if (entries.isEmpty()) return

    Text(
        title,
        color = accentColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    entries.forEach { (label, count) ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                count.toString(),
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun StatsScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        StatsScreen()
    }
}
