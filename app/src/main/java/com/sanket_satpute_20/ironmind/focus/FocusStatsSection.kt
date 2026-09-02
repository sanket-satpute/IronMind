package com.sanket_satpute_20.ironmind.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun FocusStatsSection() {
    val context = LocalContext.current
    var patterns by remember { mutableStateOf<List<FocusPattern>>(emptyList()) }
    var dayPatterns by remember { mutableStateOf<List<DayOfWeekPattern>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            patterns = FocusAnalyzer.getTaskFocusPatterns(context)
            dayPatterns = FocusAnalyzer.getDayOfWeekPatterns(context)
        }
    }

    if (patterns.isEmpty()) return

    Column {
        Text(
            "FOCUS DEPTH",
            fontSize = 11.sp, color = WarningAmber,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Per-task focus scores
        patterns.forEach { pattern ->
            FocusScoreRow(pattern = pattern)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Best and worst day of week
        if (dayPatterns.isNotEmpty()) {
            val bestDay = dayPatterns.maxByOrNull { it.averageScore }
            val worstDay = dayPatterns.minByOrNull { it.averageScore }

            Text(
                "DAY OF WEEK PATTERNS",
                fontSize = 11.sp, color = WarningAmber,
                fontWeight = FontWeight.Bold, letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bestDay?.let {
                    DayPatternCard(
                        day = it, label = "SHARPEST",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                worstDay?.let {
                    DayPatternCard(
                        day = it, label = "WEAKEST",
                        color = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FocusScoreRow(pattern: FocusPattern) {
    val trendIcon = when (pattern.trend) {
        "improving" -> "📈"
        "declining" -> "📉"
        else -> "➡️"
    }

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, getFocusColor(pattern.averageScore.toInt())
            .copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pattern.taskName, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("${pattern.totalRatings} sessions rated  $trendIcon ${pattern.trend}",
                    fontSize = 11.sp, color = Color.Gray)
            }

            // Mini score bar
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format("%.1f", pattern.averageScore),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = getFocusColor(pattern.averageScore.toInt())
                )
                Text("/ 5.0", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DayPatternCard(day: DayOfWeekPattern, label: String,
                   color: Color, modifier: Modifier) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = color,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(day.dayName, fontSize = 16.sp,
                fontWeight = FontWeight.Black, color = Color.White)
            Text(String.format("%.1f avg", day.averageScore),
                fontSize = 13.sp, color = color)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun FocusStatsSectionPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FocusScoreRow(FocusPattern("Deep Work", 4.6f, 12, "improving"))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DayPatternCard(DayOfWeekPattern("Tuesday", 4.8f, 6), "SHARPEST", SuccessGreen, Modifier.weight(1f))
                DayPatternCard(DayOfWeekPattern("Friday", 3.1f, 5), "WEAKEST", ErrorRed, Modifier.weight(1f))
            }
        }
    }
}
