package com.sanket_satpute_20.ironmind.home

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.ironmind.ui.components.ProgressRing
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.context.ManualOverrideToggle
import com.sanket_satpute_20.ironmind.context.ModeStateEngine
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@Composable
fun HeaderSectionWithIdentity(
    level: Int,
    title: String,
    progress: Float,
    streak: Int,
    currentMode: AppMode,
    modifier: Modifier = Modifier,
    onGraveyardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "XP_Glow")
    val progressPercent = (progress.coerceIn(0f, 1f) * 100f).toInt()
    val graveyardHeaderAction = remember(currentMode) { graveyardHeaderActionForMode(currentMode) }
    val xpAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "XP_Alpha"
    )

    // For demo purposes, defaulting to home mode
    var contextMode by rememberSaveable { mutableStateOf(ModeStateEngine.AppMode.DISCIPLINE_HOME) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("LEVEL $level", style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Black, letterSpacing = 2.sp), color = WarningAmber)
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Black),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Keep the board moving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.52f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                val context = androidx.compose.ui.platform.LocalContext.current
                val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
                val pref = if (isPreview) null else remember(context) {
                    com.sanket_satpute_20.ironmind.data.PrefManager.getInstance(context)
                }
                var showSmartUnlock by remember { mutableStateOf(false) }

                ManualOverrideToggle(
                    currentMode = contextMode,
                    onToggle = {
                        if (pref?.hasUnlockedSmartContext != true) {
                            showSmartUnlock = true
                        } else {
                            contextMode = if (contextMode == ModeStateEngine.AppMode.TRAVEL_FLEXIBLE) 
                                ModeStateEngine.AppMode.DISCIPLINE_HOME 
                            else 
                                ModeStateEngine.AppMode.TRAVEL_FLEXIBLE
                        }
                    }
                )
                
                if (showSmartUnlock) {
                    com.sanket_satpute_20.ironmind.context.SmartContextUnlockUI(
                        onUnlockComplete = { showSmartUnlock = false },
                        onDismiss = { showSmartUnlock = false }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderActionButton(
                    icon = null,
                    onClick = onGraveyardClick,
                    tint = graveyardHeaderAction.tint,
                    background = graveyardHeaderAction.background,
                    border = graveyardHeaderAction.border,
                    contentDescription = graveyardHeaderAction.contentDescription,
                    emoji = graveyardHeaderAction.emoji,
                    modifier = Modifier.padding(end = 6.dp)
                )
                HeaderActionButton(
                    icon = Icons.Rounded.Analytics,
                    onClick = onStatsClick,
                    tint = Color.White.copy(alpha = 0.82f),
                    background = Color.White.copy(alpha = 0.06f),
                    border = Color.White.copy(alpha = 0.10f),
                    contentDescription = "Open Performance Stats",
                    iconSize = 20.dp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                HeaderActionButton(
                    icon = Icons.Rounded.SettingsApplications,
                    onClick = onSettingsClick,
                    tint = Color.White.copy(alpha = 0.82f),
                    background = Color.White.copy(alpha = 0.06f),
                    border = Color.White.copy(alpha = 0.10f),
                    contentDescription = "Open Settings",
                    iconSize = 22.dp,
                    iconScale = 1.04f,
                    contentOffset = IntOffset.Zero
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        val context = androidx.compose.ui.platform.LocalContext.current
        val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
        val pref = if (isPreview) null else remember(context) {
            com.sanket_satpute_20.ironmind.data.PrefManager.getInstance(context)
        }
        
        com.sanket_satpute_20.ironmind.ui.components.SystemHealthDashboard()
        Spacer(modifier = Modifier.height(12.dp))

        com.sanket_satpute_20.ironmind.ui.components.IntegrityCoreVisual(
            streak = streak,
            shields = pref?.streakShields ?: 2,
            isCritical = pref?.isStreakInCriticalState ?: false
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = progress,
                size = 52.dp,
                strokeWidth = 5.dp,
                progressColor = WarningAmber,
                label = "$progressPercent%"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Identity progress is earned by the reps you keep.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.62f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$progressPercent% to next identity level",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                color = Color.White.copy(alpha = 0.68f)
            )
            Text(
                "Build consistency",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.38f)
            )
        }
    }
}

private data class GraveyardHeaderAction(
    val emoji: String,
    val tint: Color,
    val background: Color,
    val border: Color,
    val contentDescription: String
)

private fun graveyardHeaderActionForMode(mode: AppMode): GraveyardHeaderAction {
    return when (mode) {
        AppMode.IRON -> GraveyardHeaderAction("📋", ErrorRed, SurfaceDark, ErrorRed.copy(alpha = 0.32f), "Open Archive")
        AppMode.BUILD -> GraveyardHeaderAction("📋", WarningAmber, SurfaceDark, WarningAmber.copy(alpha = 0.28f), "Open Missed Promises")
        AppMode.RECOVERY -> GraveyardHeaderAction("📋", NeonCyan, SurfaceDark, SuccessGreen.copy(alpha = 0.24f), "Open Missed Moments")
        AppMode.EXPERIMENT -> GraveyardHeaderAction("📋", WarningAmber, DeepBackground, WarningAmber.copy(alpha = 0.26f), "Open Experiment Data")
    }
}

@Composable
fun HomeDoNowCard(
    missionItem: HomeMissionItem,
    onOpen: () -> Unit
) {
    val accent = when (missionItem.state) {
        HomeMissionState.LIVE_NOW -> SuccessGreen
        HomeMissionState.OVERDUE -> ErrorRed
        HomeMissionState.UP_NEXT -> WarningAmber
        else -> TextPrimary
    }
    val title = when (missionItem.state) {
        HomeMissionState.LIVE_NOW -> "DO NOW"
        HomeMissionState.OVERDUE -> "RECOVER NOW"
        HomeMissionState.UP_NEXT -> "UP NEXT"
        else -> "START HERE"
    }
    val cue = missionItem.task.focusNotes.takeIf { it.isNotBlank() }
    val actionLabel = when {
        missionItem.task.isInProgress -> "OPEN LIVE TASK"
        missionItem.task.focusModeEnabled -> "START FOCUS"
        missionItem.task.origin == DAILY_ONE_PERCENT_ORIGIN -> "START 1% ACTION"
        else -> "START MISSION"
    }

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = accent, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.2.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        missionItem.displayName,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(color = accent.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
                    Text(
                        missionItem.stateLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = accent,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                missionItem.supportingLabel ?: "This is the next move that matters.",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            cue?.let { cueText ->
                Surface(
                    color = accent.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.14f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            if (missionItem.task.origin == DAILY_ONE_PERCENT_ORIGIN) "TODAY'S EDGE" else "MISSION CUE",
                            color = accent,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            cueText,
                            color = Color.White.copy(alpha = 0.84f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent.copy(alpha = 0.18f),
                    contentColor = accent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(actionLabel, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, letterSpacing = 0.8.sp)
            }
        }
    }
}

@Composable
fun HomePerformanceCard(
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
    val pendingFraction = pending.toFloat() / total.toFloat()
    val summaryLabel = when {
        done == 0 && skipped == 0 && pending == 0 -> "No missions loaded"
        pending == 0 && skipped == 0 -> "Board secured"
        skipped == 0 && pending > 0 -> "$pending pending · clean so far"
        pending == 0 -> "$skipped skipped · day resolved"
        else -> "$pending pending · $skipped skipped"
    }

    var activeLexiconTerm by remember { mutableStateOf<com.sanket_satpute_20.ironmind.ui.components.LexiconTerm?>(null) }

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (snapshot.isCleanDay) SuccessGreen.copy(alpha = 0.28f)
            else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .gamifiedClick { expanded = !expanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (snapshot.isCleanDay) SuccessGreen.copy(alpha = 0.14f)
                            else ErrorRed.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Analytics, contentDescription = null, tint = if (snapshot.isCleanDay) SuccessGreen else Color.White.copy(alpha = 0.72f), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PERFORMANCE READ", color = Color.White.copy(alpha = 0.88f), fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.9.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        androidx.compose.material3.IconButton(
                            onClick = { activeLexiconTerm = com.sanket_satpute_20.ironmind.ui.components.LexiconTerm.DAILY_INTEGRITY },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Text("ⓘ", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    Text(summaryLabel, color = Color.White.copy(alpha = 0.52f), fontSize = 11.sp, lineHeight = 16.sp)
                }
                CompactScoreChip(
                    label = if (pending > 0) "OPEN" else "CLEAN",
                    value = if (pending > 0) "$pending" else "${snapshot.completionRate}%"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(999.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.82f),
                        modifier = Modifier.padding(8.dp).size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            RoundedScoreProgressBar(doneFraction = doneFraction, skippedFraction = skippedFraction, pendingFraction = pendingFraction)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreLegendChip(label = "DONE", value = done, accent = SuccessGreen, modifier = Modifier.weight(1f))
                ScoreLegendChip(label = "SKIPPED", value = skipped, accent = ErrorRed, modifier = Modifier.weight(1f))
                ScoreLegendChip(label = "PENDING", value = pending, accent = WarningAmber, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreActionChip(label = "OPEN REVIEW", icon = Icons.Rounded.Analytics, onClick = onStatsClick, modifier = Modifier.weight(1f))
                if (skipped > 0) {
                    ScoreActionChip(
                        label = "ARCHIVE",
                        emoji = "📋",
                        onClick = onGraveyardClick,
                        containerColor = DeepBackground,
                        contentColor = WarningAmber,
                        borderColor = ErrorRed.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Surface(color = Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)), modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text("ALL CLEAR TODAY", color = Color.White.copy(alpha = 0.68f), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
                        }
                    }
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(snapshot.verdict, color = Color.White.copy(alpha = 0.56f), fontSize = 11.sp, lineHeight = 17.sp)
                    snapshot.trendLabel?.let { trendLabel ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            trendLabel + snapshot.recentCompletionRate?.let { " · $it% clean" }.orEmpty(),
                            color = if ((snapshot.recentCompletionRate ?: 0) >= 75) SuccessGreen else WarningAmber,
                            fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
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
private fun CompactScoreChip(label: String, value: String) {
    Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.Gray, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 8.sp, letterSpacing = 0.9.sp)
            Text(value, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 15.sp)
        }
    }
}

@Composable
private fun RoundedScoreProgressBar(doneFraction: Float, skippedFraction: Float, pendingFraction: Float) {
    Row(modifier = Modifier.fillMaxWidth().height(10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (doneFraction > 0f) {
            Box(modifier = Modifier.weight(doneFraction).fillMaxHeight().clip(RoundedCornerShape(999.dp)).background(SuccessGreen))
        }
        if (skippedFraction > 0f) {
            Box(modifier = Modifier.weight(skippedFraction).fillMaxHeight().clip(RoundedCornerShape(999.dp)).background(ErrorRed))
        }
        if (pendingFraction > 0f) {
            Box(modifier = Modifier.weight(pendingFraction).fillMaxHeight().clip(RoundedCornerShape(999.dp)).background(WarningAmber.copy(alpha = 0.88f)))
        }
        if (doneFraction == 0f && skippedFraction == 0f && pendingFraction == 0f) {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.06f)))
        }
    }
}

@Composable
private fun ScoreLegendChip(label: String, value: Int, accent: Color, modifier: Modifier = Modifier) {
    Surface(color = accent.copy(alpha = 0.08f), shape = RoundedCornerShape(999.dp), border = BorderStroke(1.dp, accent.copy(alpha = 0.12f)), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(7.dp).background(accent, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text("$label $value", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
        }
    }
}

@Composable
private fun ScoreActionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    containerColor: Color = Color.White.copy(alpha = 0.07f),
    contentColor: Color = Color.White,
    borderColor: Color = Color.White.copy(alpha = 0.08f)
) {
    Surface(onClick = onClick, color = containerColor, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, borderColor), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(emoji, fontSize = 12.sp)
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            }
            if (emoji != null || icon != null) Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = contentColor, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
        }
    }
}

@Composable
fun HeaderActionButton(
    icon: ImageVector?,
    onClick: () -> Unit,
    tint: Color,
    background: Color,
    border: Color,
    contentDescription: String,
    emoji: String? = null,
    iconSize: Dp = 18.dp,
    iconScale: Float = 1f,
    contentOffset: IntOffset = IntOffset.Zero,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = background,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, border),
        modifier = modifier
            .size(40.dp)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.offset { contentOffset }) {
            if (emoji != null) {
                Text(emoji, fontSize = 16.sp)
            } else if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                )
            }
        }
    }
}

// =========================================================
// Previews
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun HeaderSectionWithIdentityPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HeaderSectionWithIdentity(
            level = 7,
            title = "Iron Operator",
            progress = 0.65f,
            streak = 21,
            currentMode = com.sanket_satpute_20.ironmind.psychology.AppMode.IRON,
            onGraveyardClick = {},
            onSettingsClick = {},
            onStatsClick = {}
        )
    }
}
