package com.sanket_satpute_20.ironmind.autopsy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.DailyIntegrityRecord
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@Composable
fun Integrity14DayCard(
    title: String,
    records: List<DailyIntegrityRecord>,
    modifier: Modifier = Modifier
) {
    val paddedRecords = remember(records) { padIntegrityRecords(records, 14) }
    val latest = paddedRecords.lastOrNull()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.14f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = latest?.let { "${it.scorePercent}% integrity on ${it.date}" } ?: "No finalized integrity yet",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            latest?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${it.completedCount}/${it.plannedCount} missions honored",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                paddedRecords.forEach { record ->
                    Box(modifier = Modifier.weight(1f)) {
                        IntegrityBar(
                            score = record?.scorePercent ?: 0,
                            label = record?.date?.takeLast(2) ?: "--",
                            escalated = record?.strictnessEscalatedForNextDay == true
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IntegrityLegend("85+", SuccessGreen)
                IntegrityLegend("70-84", WarningAmber)
                IntegrityLegend("<70", ErrorRed)
                IntegrityLegend("▲ Iron boost", TextPrimary)
            }
            if (records.any { it.strictnessEscalatedForNextDay }) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "▲ marks days that triggered stricter Iron pressure for the next day.",
                    color = Color.White.copy(alpha = 0.54f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun IntegrityBar(score: Int, label: String, escalated: Boolean) {
    val clamped = score.coerceIn(0, 100)
    val color = when {
        clamped >= 85 -> SuccessGreen
        clamped >= 70 -> WarningAmber
        else -> ErrorRed
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((clamped.coerceAtLeast(6) * 0.92f).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
            if (escalated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(TextPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▲",
                        color = TextPrimary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.54f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IntegrityLegend(label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.58f),
            fontSize = 11.sp
        )
    }
}

private fun padIntegrityRecords(
    records: List<DailyIntegrityRecord>,
    days: Int
): List<DailyIntegrityRecord?> {
    val byDate = records.associateBy { it.date }
    val start = LocalDate.now().minusDays((days - 1).toLong())
    return (0 until days).map { offset ->
        byDate[start.plusDays(offset.toLong()).toString()]
    }
}

/** A complete integrity dashboard: current standard, recent pressure, and the 14-day evidence rail. */
@Composable
fun IntegrityCardStack(
    records: List<DailyIntegrityRecord>,
    modifier: Modifier = Modifier
) {
    val recentRecords = remember(records) { records.sortedBy { it.date }.takeLast(7) }
    val averageScore = remember(recentRecords) {
        if (recentRecords.isEmpty()) 0 else recentRecords.map { it.scorePercent }.average().toInt()
    }
    val missCount = remember(recentRecords) { recentRecords.sumOf { it.missCount } }
    val escalationCount = remember(recentRecords) { recentRecords.count { it.strictnessEscalatedForNextDay } }
    val latest = recentRecords.lastOrNull()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.20f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("INTEGRITY STANDARD", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(
                    when {
                        latest == null -> "Build your first seven-day signal."
                        averageScore >= 85 -> "Standard intact. Keep the pressure honest."
                        averageScore >= 70 -> "Standard is holding, but needs attention."
                        else -> "Integrity is under pressure. Rebuild the next rep."
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    if (latest == null) "No finalized days yet." else "Latest close: ${latest.scorePercent}% · ${latest.completedCount}/${latest.plannedCount} missions resolved",
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 12.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            IntegrityMetricCard("7D AVG", if (recentRecords.isEmpty()) "—" else "$averageScore%", if (averageScore >= 85) SuccessGreen else if (averageScore >= 70) WarningAmber else ErrorRed, Modifier.weight(1f))
            IntegrityMetricCard("MISSES", "$missCount", if (missCount == 0) SuccessGreen else WarningAmber, Modifier.weight(1f))
            IntegrityMetricCard("IRON BOOSTS", "$escalationCount", TextPrimary, Modifier.weight(1f))
        }

        Integrity14DayCard(title = "14-DAY EVIDENCE", records = records)
    }
}

@Composable
private fun IntegrityMetricCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp, fontWeight = FontWeight.Black)
            Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun IntegrityCardStackPreview() {
    val today = LocalDate.now()
    val records = (0 until 7).map { offset ->
        DailyIntegrityRecord(
            date = today.minusDays((6 - offset).toLong()).toString(),
            plannedCount = 4,
            completedCount = if (offset == 2) 2 else 4,
            scorePercent = if (offset == 2) 65 else 88,
            finalizedAt = 0L,
            modeAtFinalization = "IRON",
            strictnessEscalatedForNextDay = offset == 2,
            source = "PREVIEW",
            missCount = if (offset == 2) 2 else 0
        )
    }
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme { IntegrityCardStack(records = records) }
}
