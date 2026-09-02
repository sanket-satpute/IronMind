package com.sanket_satpute_20.ironmind.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.ConfigChangeEvent
import java.time.LocalDate
import java.util.Locale
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

internal data class HomePomodoroOverview(
    val started: Int,
    val completed: Int,
    val broken: Int,
    val focusedMinutes: Int,
    val averageScore: Int,
    val latestTitle: String?
)

private data class HomePomodoroMetricSnapshot(
    val sessionId: String = "",
    val title: String = "",
    val minutes: Int = 0,
    val score: Int = 0
)

internal fun buildHomePomodoroOverview(
    events: List<ConfigChangeEvent>,
    today: LocalDate
): HomePomodoroOverview? {
    val recent = events.filter { it.date == today.toString() && it.configType.startsWith("POMODORO_HOME_") }
    if (recent.isEmpty()) return null

    val parsed = recent.map { parseHomePomodoroMetrics(it.newValueJson) to it.configType }
    val starts = parsed
        .filter { it.second == "POMODORO_HOME_STARTED" }
        .distinctBy { it.first.sessionId }
    val outcomes = parsed
        .filter {
            it.second == "POMODORO_HOME_COMPLETED" ||
                it.second == "POMODORO_HOME_BROKEN" ||
                it.second == "POMODORO_HOME_EMERGENCY_EXIT"
        }
        .distinctBy { it.first.sessionId }
    if (starts.isEmpty() && outcomes.isEmpty()) return null

    return HomePomodoroOverview(
        started = starts.size,
        completed = outcomes.count { it.second == "POMODORO_HOME_COMPLETED" },
        broken = outcomes.count { it.second != "POMODORO_HOME_COMPLETED" },
        focusedMinutes = outcomes.sumOf { it.first.minutes },
        averageScore = outcomes.map { it.first.score }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0,
        latestTitle = outcomes.lastOrNull()?.first?.title ?: starts.lastOrNull()?.first?.title
    )
}

private fun parseHomePomodoroMetrics(serialized: String): HomePomodoroMetricSnapshot {
    if (serialized.isBlank()) return HomePomodoroMetricSnapshot()
    val parts = serialized.split('|')
        .mapNotNull { entry ->
            val index = entry.indexOf('=')
            if (index <= 0) null else entry.substring(0, index) to entry.substring(index + 1)
        }
        .toMap()
    return HomePomodoroMetricSnapshot(
        sessionId = parts["SESSION_ID"].orEmpty(),
        title = parts["TITLE"].orEmpty(),
        minutes = parts["MINUTES"]?.toIntOrNull() ?: 0,
        score = parts["SCORE"]?.toIntOrNull() ?: 0
    )
}

@Composable
internal fun HomePomodoroAnalyticsCard(
    overview: HomePomodoroOverview
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "TODAY'S CHAMBER PRESSURE",
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "${overview.started} runs armed · ${overview.completed} sealed · ${overview.focusedMinutes} focused min",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
            Text(
                text = "Broken runs: ${overview.broken} · Avg score: ${overview.averageScore}${overview.latestTitle?.takeIf { it.isNotBlank() }?.let { " · Latest: $it" } ?: ""}",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
internal fun IntegrityPressureCard(
    punishmentBonusSeconds: Long,
    cleanUnlockRequired: Boolean,
    springPenaltyBonus: Int,
    emergencyExitReduction: Int
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(WarningAmber.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = WarningAmber,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ELEVATED CHALLENGE TODAY",
                        color = WarningAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Today's rules are adjusted based on recent patterns.",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntegrityPressurePill(
                    label = "Punishment",
                    value = "+${punishmentBonusSeconds}s",
                    modifier = Modifier.weight(1f)
                )
                IntegrityPressurePill(
                    label = "Spring",
                    value = "+$springPenaltyBonus XP",
                    modifier = Modifier.weight(1f)
                )
                IntegrityPressurePill(
                    label = "Emergency",
                    value = if (emergencyExitReduction > 0) "-$emergencyExitReduction exit" else "Normal",
                    modifier = Modifier.weight(1f)
                )
            }

            if (cleanUnlockRequired) {
                Text(
                    text = "Earned unlock requires a clean day: all work completed, zero skips.",
                    color = Color.White.copy(alpha = 0.64f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun IntegrityPressurePill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(Locale.getDefault()),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
internal fun OperationsAttentionCard(
    title: String,
    subtitle: String,
    accent: Color,
    actionLabel: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.035f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                subtitle,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        actionLabel,
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun OperationsCompactBanner(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.025f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = Color.White.copy(alpha = 0.72f),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.56f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun CloudBackupBanner(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(30.dp).background(WarningAmber.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CloudUpload,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CLOUD SEAL RECOMMENDED",
                    color = WarningAmber,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.9.sp
                )
                Text(
                    "Your progress is still local only.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            Text("OPEN", color = WarningAmber, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
    }
}

@Composable
internal fun FoundationProgressCard(
    needsIdentity: Boolean,
    needsContract: Boolean,
    needsProtection: Boolean,
    firstHomeArrivalDate: String,
    onIdentityClick: () -> Unit,
    onContractClick: () -> Unit,
    onProtectionClick: () -> Unit
) {
    val remaining = listOf(needsIdentity, needsContract, needsProtection).count { it }
    val completed = 3 - remaining
    val steps = listOf(
        FoundationStepAction(
            key = "identity",
            title = "Identity",
            actionLabel = if (needsIdentity) "DEFINE IDENTITY" else "IDENTITY SET",
            done = !needsIdentity,
            onClick = onIdentityClick
        ),
        FoundationStepAction(
            key = "contract",
            title = "Commitment",
            actionLabel = if (needsContract) "SIGN COMMITMENT" else "COMMITMENT SET",
            done = !needsContract,
            onClick = onContractClick
        ),
        FoundationStepAction(
            key = "protection",
            title = "Distractions",
            actionLabel = if (needsProtection) "SECURE DISTRACTIONS" else "PROTECTION SET",
            done = !needsProtection,
            onClick = onProtectionClick
        )
    )
    val nextPendingStep = steps.firstOrNull { !it.done }
    val secondarySteps = steps.filterNot { it.key == nextPendingStep?.key }
    val nextStepLabel = when {
        needsIdentity -> "Define identity"
        needsContract -> "Sign commitment"
        needsProtection -> "Secure distractions"
        else -> "Foundation locked"
    }
    val supportCopy = when {
        remaining == 3 -> "You reached the app faster. Lock the core systems now so the pressure stays real."
        remaining == 2 -> "The foundation is taking shape. Finish the next layer so the app can hold you properly."
        remaining == 1 -> "One final setup step remains before your foundation is fully locked."
        else -> "All core setup systems are locked."
    }
    val arrivalCopy = if (firstHomeArrivalDate.isBlank()) {
        "Started today"
    } else {
        "Home unlocked on $firstHomeArrivalDate"
    }

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NeonCyan.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "COMPLETE YOUR FOUNDATION",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "$completed of 3 core setup steps completed",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        "$remaining LEFT",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color.White.copy(alpha = 0.82f),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    supportCopy,
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = NeonCyan.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        "NEXT: ${nextStepLabel.uppercase()}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                arrivalCopy,
                color = Color.Gray,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FoundationProgressSegment(done = !needsIdentity, modifier = Modifier.weight(1f))
                FoundationProgressSegment(done = !needsContract, modifier = Modifier.weight(1f))
                FoundationProgressSegment(done = !needsProtection, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                steps.forEach { step ->
                    FoundationStatusBadge(
                        title = step.title,
                        done = step.done,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (nextPendingStep != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = nextPendingStep.onClick,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CONTINUE WITH ${nextPendingStep.actionLabel}",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                secondarySteps.forEach { step ->
                    FoundationActionChip(
                        label = step.actionLabel,
                        onClick = step.onClick,
                        done = step.done
                    )
                }
            }
        }
    }
}

@Composable
internal fun FoundationCompactBanner(
    remaining: Int,
    onClick: () -> Unit
) {
    val label = if (remaining == 1) "1 FOUNDATION STEP LEFT" else "$remaining FOUNDATION STEPS LEFT"
    Surface(
        onClick = onClick,
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(NeonCyan.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Verified, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 0.9.sp)
                Text("Finish the next setup layer when you're ready.", color = Color.White.copy(alpha = 0.68f), fontSize = 11.sp)
            }
            Text("CONTINUE", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
    }
}

private data class FoundationStepAction(
    val key: String,
    val title: String,
    val actionLabel: String,
    val done: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun FoundationProgressSegment(done: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(999.dp))
            .background(if (done) SuccessGreen else Color.White.copy(alpha = 0.08f))
    )
}

@Composable
private fun FoundationStatusBadge(
    title: String,
    done: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (done) SuccessGreen.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (done) SuccessGreen.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.06f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (done) SuccessGreen else WarningAmber.copy(alpha = 0.8f),
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                title.uppercase(),
                color = if (done) SuccessGreen else Color.White.copy(alpha = 0.82f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun FoundationActionChip(
    label: String,
    onClick: () -> Unit,
    done: Boolean
) {
    Surface(
        onClick = onClick,
        color = if (done) SurfaceDark else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            1.dp,
            if (done) SuccessGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (done) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                label,
                color = if (done) SuccessGreen else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// =========================================================
// Previews
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun HomeSystemSectionPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        IntegrityPressureCard(
            punishmentBonusSeconds = 300L,
            cleanUnlockRequired = true,
            springPenaltyBonus = 5,
            emergencyExitReduction = 2
        )
    }
}