package com.sanket_satpute_20.ironmind.voice

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.VoiceLog
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionLibrary
import java.text.SimpleDateFormat
import java.util.*
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

private enum class VoiceLogsFilter(
    val routeValue: String,
    val label: String
) {
    ALL("all", "All"),
    CONFIDENCE("confidence", "Confidence"),
    IGNITION("ignition", "Ignition");

    companion object {
        fun fromRoute(value: String?): VoiceLogsFilter {
            return entries.firstOrNull { it.routeValue.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLogsScreen(
    onBack: () -> Unit = {},
    onRecordToday: () -> Unit = {},
    initialFilter: String = "all"
) {
    val context = LocalContext.current
    val dao = IronMindDatabase.getDatabase(context).voiceLogDao()
    val allLogs by dao.getAllLogs().collectAsState(initial = emptyList())
    var activeFilter by remember(initialFilter) { mutableStateOf(VoiceLogsFilter.fromRoute(initialFilter)) }

    val player = remember { VoicePlayer() }
    var currentlyPlayingId by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(Unit) {
        onDispose { player.stopPlayback() }
    }

    val filteredLogs = remember(allLogs, activeFilter) {
        when (activeFilter) {
            VoiceLogsFilter.ALL -> allLogs
            VoiceLogsFilter.CONFIDENCE -> allLogs.filter {
                it.entryType == "CONFIDENCE" || it.entryType == "CONFIDENCE_IGNITION"
            }
            VoiceLogsFilter.IGNITION -> allLogs.filter { it.entryType == "CONFIDENCE_IGNITION" }
        }
    }

    val groupedLogs = filteredLogs.groupBy { it.month }
    val avgClarity = remember(filteredLogs) {
        filteredLogs.map { it.clarityScore }.filter { it > 0 }.average().takeIf { !it.isNaN() } ?: 0.0
    }
    val avgConfidence = remember(filteredLogs) {
        filteredLogs.map { it.confidenceScore }.filter { it > 0 }.average().takeIf { !it.isNaN() } ?: 0.0
    }
    val avgDuration = remember(filteredLogs) {
        filteredLogs.map { it.durationSeconds }.average().takeIf { !it.isNaN() } ?: 0.0
    }

    Scaffold(
        containerColor = DeepBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("VOICE JOURNALS", 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text("🎙️", fontSize = 40.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "HEAR YOUR GROWTH",
                fontSize = 18.sp, fontWeight = FontWeight.Black,
                color = Color.White, letterSpacing = 3.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                when (activeFilter) {
                    VoiceLogsFilter.ALL -> "${filteredLogs.size} recordings captured."
                    VoiceLogsFilter.CONFIDENCE -> "${filteredLogs.size} confidence reps captured."
                    VoiceLogsFilter.IGNITION -> "${filteredLogs.size} ignition reps captured."
                },
                fontSize = 13.sp, color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            VoiceLogsFilterRow(
                activeFilter = activeFilter,
                onFilterSelected = { activeFilter = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            VoiceLogsSnapshotCard(
                filter = activeFilter,
                avgClarity = avgClarity,
                avgConfidence = avgConfidence,
                avgDuration = avgDuration,
                logsCount = filteredLogs.size
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRecordToday,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RECORD TODAY", color = Color.Black, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (filteredLogs.isEmpty()) {
                EmptyVoiceLogsState(filter = activeFilter)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    groupedLogs.entries
                        .sortedByDescending { it.key }
                        .forEach { (month, logs) ->
                            item {
                                MonthHeader(month = month, count = logs.size)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(logs) { log ->
                                VoiceLogCard(
                                    log = log,
                                    isPlaying = currentlyPlayingId == log.id,
                                    onPlayPause = {
                                        if (currentlyPlayingId == log.id) {
                                            player.stopPlayback()
                                            currentlyPlayingId = null
                                        } else {
                                            player.stopPlayback()
                                            currentlyPlayingId = log.id
                                            player.play(log.filePath) {
                                                currentlyPlayingId = null
                                            }
                                        }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                }
            }
        }
    }
}

@Composable
fun VoiceLogCard(log: VoiceLog, isPlaying: Boolean, onPlayPause: () -> Unit) {
    val bgColor = if (isPlaying) DeepBackground else DeepBackground
    val borderColor = if (isPlaying) SuccessGreen else SurfaceDark
    val isConfidenceLog = log.entryType == "CONFIDENCE" || log.entryType == "CONFIDENCE_IGNITION"
    val isIgnitionLog = log.entryType == "CONFIDENCE_IGNITION"
    val promptLabel = remember(log.promptType, isIgnitionLog) {
        when {
            isIgnitionLog && log.promptType.isNotBlank() ->
                runCatching { ConfidenceIgnitionLibrary.voicePromptById(log.promptType).title }.getOrNull()
            else -> null
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (isPlaying) SuccessGreen else SurfaceDark,
                        CircleShape
                    )
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    log.taskName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    "${log.date} • ${log.durationSeconds}s",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when {
                        isIgnitionLog -> ScoreChip("IGNITION", WarningAmber)
                        isConfidenceLog -> ScoreChip("CONFIDENCE", NeonCyan)
                        else -> ScoreChip("GENERAL", ElectricViolet)
                    }
                    promptLabel?.let { ScoreChip(it.uppercase(Locale.getDefault()), ElectricViolet) }
                }
                if (isConfidenceLog) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (log.clarityScore > 0 || log.confidenceScore > 0) {
                            ScoreChip("CLARITY ${log.clarityScore}/5", NeonCyan)
                            ScoreChip("CONF ${log.confidenceScore}/5", SuccessGreen)
                        } else if (isIgnitionLog) {
                            ScoreChip("MORNING REP", SuccessGreen)
                        }
                    }
                }
            }

            if (isPlaying) {
                // Animated waveform indicator
                WaveformIndicator()
            }
        }
    }
}

@Composable
private fun VoiceLogsFilterRow(
    activeFilter: VoiceLogsFilter,
    onFilterSelected: (VoiceLogsFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VoiceLogsFilter.entries.forEach { filter ->
            val active = filter == activeFilter
            Surface(
                modifier = Modifier.weight(1f),
                onClick = { onFilterSelected(filter) },
                color = if (active) NeonCyan.copy(alpha = 0.16f) else DeepBackground,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (active) NeonCyan.copy(alpha = 0.22f) else SurfaceDark)
            ) {
                Text(
                    text = filter.label.uppercase(),
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = if (active) TextPrimary else Color.White.copy(alpha = 0.58f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun VoiceLogsSnapshotCard(
    filter: VoiceLogsFilter,
    avgClarity: Double,
    avgConfidence: Double,
    avgDuration: Double,
    logsCount: Int
) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = when (filter) {
                    VoiceLogsFilter.ALL -> "VOICE READ"
                    VoiceLogsFilter.CONFIDENCE -> "CONFIDENCE TRACK"
                    VoiceLogsFilter.IGNITION -> "IGNITION VOICE TRACK"
                },
                color = when (filter) {
                    VoiceLogsFilter.ALL -> TextPrimary
                    VoiceLogsFilter.CONFIDENCE -> NeonCyan
                    VoiceLogsFilter.IGNITION -> WarningAmber
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = when (filter) {
                    VoiceLogsFilter.ALL -> "All voice proof in one place."
                    VoiceLogsFilter.CONFIDENCE -> "This is your speaking-confidence track."
                    VoiceLogsFilter.IGNITION -> "These are the morning reps that shape the tone of the day."
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreChip("${logsCount} LOGS", ElectricViolet)
                ScoreChip("${avgDuration.toInt()}s AVG", TextPrimary)
                if (avgClarity > 0 || avgConfidence > 0) {
                    ScoreChip("C ${"%.1f".format(Locale.US, avgClarity)}", NeonCyan)
                    ScoreChip("F ${"%.1f".format(Locale.US, avgConfidence)}", SuccessGreen)
                }
            }
        }
    }
}

@Composable
private fun ScoreChip(label: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
fun WaveformIndicator() {
    val anim = rememberInfiniteTransition()
    val heights = (1..4).map { index ->
        anim.animateFloat(
            initialValue = 4f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(300 + index * 80),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(SuccessGreen, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun MonthHeader(month: String, count: Int) {
    val displayMonth = runCatching {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdf.parse(month)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date!!)
    }.getOrElse { month }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            displayMonth.uppercase(),
            fontSize = 11.sp,
            color = WarningAmber,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text("  ($count recordings)", fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun EmptyVoiceLogsState(filter: VoiceLogsFilter = VoiceLogsFilter.ALL) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎙️", fontSize = 52.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("No recordings yet", fontSize = 18.sp,
            fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            when (filter) {
                VoiceLogsFilter.ALL ->
                    "Record one honest 60 to 90 second rep.\nListen back once. Rate clarity and confidence.\nThat is how this gets better."
                VoiceLogsFilter.CONFIDENCE ->
                    "Confidence reps will show up here once you record and rate them."
                VoiceLogsFilter.IGNITION ->
                    "Morning ignition reps will show up here once you use the morning flow."
            },
            fontSize = 14.sp, color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}





@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun VoiceLogsScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        VoiceLogsScreen()
    }
}
