package com.sanket_satpute_20.ironmind.confidence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.ConfidenceIgnitionEntry
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.VoiceLog
import java.time.LocalTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidenceIgnitionHistoryScreen(
    onBack: () -> Unit,
    onOpenVoiceTrack: () -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    
    val db = if (isPreview) null else remember { IronMindDatabase.getDatabase(context) }
    
    val entries by if (isPreview) {
        remember { mutableStateOf(emptyList<ConfidenceIgnitionEntry>()) }
    } else {
        db!!.confidenceIgnitionDao().getAll().collectAsState(initial = emptyList())
    }
    
    val voiceLogs by if (isPreview) {
        remember { mutableStateOf(emptyList<com.sanket_satpute_20.ironmind.data.VoiceLog>()) }
    } else {
        db!!.voiceLogDao().getAllLogs().collectAsState(initial = emptyList())
    }
    
    val ignitionVoiceLogs = remember(voiceLogs) {
        voiceLogs.filter { it.entryType == "CONFIDENCE_IGNITION" }
    }
    val todayKey = remember { LocalDate.now().toString() }
    val todayEntry = remember(entries) { entries.firstOrNull { it.date == todayKey } }
    val completedCount = remember(entries) { entries.count { it.completed } }
    val streak = remember(entries) { ignitionStreak(entries) }
    val recent7 = remember(entries) { ignitionRecentDays(entries, 7).count { it } }
    val completionRate30 = remember(entries) { ignitionRecentDays(entries, 30).count { it } }
    val mostCommonEdge = remember(entries) { ignitionMostCommonEdge(entries) }
    val missedLast30 = (30 - completionRate30).coerceAtLeast(0)

    Scaffold(
        containerColor = DeepBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Ignition History",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DeepBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                IgnitionHistoryHero(
                    completedCount = completedCount,
                    streak = streak,
                    recent7 = recent7,
                    voiceReps = ignitionVoiceLogs.size,
                    completionRate30 = completionRate30,
                    mostCommonEdge = mostCommonEdge
                )
            }
            item {
                IgnitionTodayActionCard(
                    todayEntry = todayEntry,
                    missedLast30 = missedLast30,
                    onResume = {
                        context.startActivity(ConfidenceIgnitionActivity.createIntent(context))
                    }
                )
            }
            item {
                IgnitionVoiceTrackCard(
                    voiceLogs = ignitionVoiceLogs,
                    onOpenVoiceTrack = onOpenVoiceTrack
                )
            }
            item {
                IgnitionSectionHeader(
                    title = "Last 30 Days",
                    subtitle = "Read the streak, the missed days, and the courage edges you were training."
                )
            }
            if (entries.isEmpty()) {
                item { IgnitionEmptyState() }
            } else {
                items(entries.take(30)) { entry ->
                    val voiceLog = ignitionVoiceLogs.firstOrNull { it.date == entry.date }
                    IgnitionHistoryRow(entry = entry, voiceLog = voiceLog)
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun IgnitionHistoryHero(
    completedCount: Int,
    streak: Int,
    recent7: Int,
    voiceReps: Int,
    completionRate30: Int,
    mostCommonEdge: String?
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(SurfaceDark, DeepBackground)
                )
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "CONFIDENCE IGNITION",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Morning proof, not morning hype.",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Track whether you reset the body, used your voice, and picked one edge before the day got noisy.",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                mostCommonEdge?.let { edge ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Text(
                            text = "Most trained edge: $edge",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IgnitionStatPill("Streak", streak.toString(), SuccessGreen, Modifier.weight(1f))
                    IgnitionStatPill("7 Days", "$recent7/7", TextPrimary, Modifier.weight(1f))
                    IgnitionStatPill("Voice", voiceReps.toString(), WarningAmber, Modifier.weight(1f))
                    IgnitionStatPill("30 Days", "$completionRate30/30", ElectricViolet, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Total completed ignitions: $completedCount",
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun IgnitionTodayActionCard(
    todayEntry: ConfidenceIgnitionEntry?,
    missedLast30: Int,
    onResume: () -> Unit
) {
    val completed = todayEntry?.completed == true
    val recoveryMode = !completed && LocalTime.now().isAfter(LocalTime.NOON)
    val accent = when {
        completed -> SuccessGreen
        recoveryMode -> WarningAmber
        else -> TextPrimary
    }
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "TODAY",
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = when {
                    completed -> "Today is already armed."
                    recoveryMode -> "Recover today's ignition."
                    else -> "Open today's ignition."
                },
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = when {
                    completed -> "You already have morning proof on the board. Protect the edge you picked."
                    recoveryMode -> "The morning window slipped, but this day still counts if you reset now."
                    else -> "Lock body, voice, and one courage edge before the day gets noisy."
                },
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Text(
                text = if (missedLast30 > 0) {
                    "$missedLast30 missed morning${if (missedLast30 == 1) "" else "s"} in the last 30 days."
                } else {
                    "No missed mornings in the last 30 days."
                },
                color = Color.White.copy(alpha = 0.52f),
                fontSize = 11.sp
            )
            if (!completed) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text(
                        text = if (recoveryMode) "RECOVER IGNITION" else "OPEN IGNITION",
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun IgnitionVoiceTrackCard(
    voiceLogs: List<VoiceLog>,
    onOpenVoiceTrack: () -> Unit
) {
    val recent7 = remember(voiceLogs) {
        val cutoff = LocalDate.now().minusDays(6)
        voiceLogs.count { runCatching { LocalDate.parse(it.date) >= cutoff }.getOrDefault(false) }
    }
    val avgDuration = remember(voiceLogs) {
        voiceLogs.map { it.durationSeconds }.average().takeIf { !it.isNaN() } ?: 0.0
    }
    val topPrompt = remember(voiceLogs) {
        voiceLogs
            .filter { it.promptType.isNotBlank() }
            .groupingBy {
                runCatching { ConfidenceIgnitionLibrary.voicePromptById(it.promptType).title }.getOrDefault("Voice rep")
            }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "VOICE TRACK",
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Morning reps deserve their own growth lane.",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (voiceLogs.isEmpty()) {
                    "Once you save ignition voice reps, this lane becomes your cleanest proof of speaking consistency."
                } else {
                    "You have ${voiceLogs.size} ignition reps saved, $recent7 in the last 7 days, averaging ${avgDuration.toInt()} seconds."
                },
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            topPrompt?.let {
                Text(
                    text = "Most repeated prompt: $it",
                    color = WarningAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = onOpenVoiceTrack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextPrimary)
            ) {
                Text("OPEN IGNITION VOICE TRACK", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun IgnitionStatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(label.uppercase(Locale.ROOT), color = Color.White.copy(alpha = 0.58f), fontSize = 9.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun IgnitionSectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            title,
            color = Color.White.copy(alpha = 0.78f),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun IgnitionHistoryRow(
    entry: ConfidenceIgnitionEntry,
    voiceLog: VoiceLog?
) {
    val couragePrompt = remember(entry.couragePromptId) {
        ConfidenceIgnitionLibrary.couragePromptById(entry.couragePromptId)
    }
    val dateLabel = remember(entry.date) {
        runCatching {
            LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US))
        }.getOrElse { entry.date }
    }
    val accent = if (entry.completed) SuccessGreen else WarningAmber
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        dateLabel.uppercase(Locale.ROOT),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        couragePrompt.title,
                        color = accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        if (entry.completed) "DONE" else "PENDING",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                couragePrompt.cue,
                color = Color.White.copy(alpha = 0.76f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IgnitionMiniChip(
                    label = if (entry.breathDone) "Breath" else "No Breath",
                    accent = if (entry.breathDone) SuccessGreen else Color.White.copy(alpha = 0.28f),
                    modifier = Modifier.weight(1f)
                )
                IgnitionMiniChip(
                    label = if (entry.moveDone) "Move" else "No Move",
                    accent = if (entry.moveDone) SuccessGreen else Color.White.copy(alpha = 0.28f),
                    modifier = Modifier.weight(1f)
                )
                IgnitionMiniChip(
                    label = if (entry.courageAccepted) "Edge" else "No Edge",
                    accent = if (entry.courageAccepted) WarningAmber else Color.White.copy(alpha = 0.28f),
                    modifier = Modifier.weight(1f)
                )
            }
            if (voiceLog != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.14f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(TextPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            "Voice rep saved • ${voiceLog.durationSeconds}s",
                            color = Color.White.copy(alpha = 0.84f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IgnitionMiniChip(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = if (accent.alpha > 0.5f) 0.12f else 0.04f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.14f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = if (accent.alpha > 0.5f) accent else Color.White.copy(alpha = 0.52f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun IgnitionEmptyState() {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(TextPrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No ignition history yet",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Complete the morning ignition once and this screen will start showing streaks, edges, and voice proof.",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

private fun ignitionStreak(entries: List<ConfidenceIgnitionEntry>): Int {
    val byDate = entries.associateBy { it.date }
    var streak = 0
    var cursor = LocalDate.now()
    var checked = 0
    while (checked < 30 && byDate[cursor.toString()]?.completed == true) {
        streak++
        cursor = cursor.minusDays(1)
        checked++
    }
    return streak
}

private fun ignitionRecentDays(entries: List<ConfidenceIgnitionEntry>, days: Int): List<Boolean> {
    val byDate = entries.associateBy { it.date }
    return ((days - 1).toLong() downTo 0L).map { offset ->
        val date = LocalDate.now().minusDays(offset).toString()
        byDate[date]?.completed == true
    }
}

private fun ignitionMostCommonEdge(entries: List<ConfidenceIgnitionEntry>): String? {
    return entries
        .filter { it.completed }
        .groupingBy { ConfidenceIgnitionLibrary.couragePromptById(it.couragePromptId).title }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ConfidenceIgnitionHistoryScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ConfidenceIgnitionHistoryScreen(onBack = {})
    }
}
