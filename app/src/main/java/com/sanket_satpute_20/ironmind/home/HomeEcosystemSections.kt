package com.sanket_satpute_20.ironmind.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.crucible.CrucibleDefinition
import com.sanket_satpute_20.ironmind.crucible.CrucibleManager
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.product.CurrentVersionScope
import com.sanket_satpute_20.ironmind.social.SocialOverview
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

internal fun LazyListScope.HomeEcosystemSection(
    prefManager: PrefManager,
    activeCrucible: CrucibleDefinition?,
    seasonalCrucible: CrucibleDefinition?,
    crucibleManager: CrucibleManager,
    socialOverview: SocialOverview?,
    emergencyCooldownUntil: Long,
    cooldownClock: Long,
    currentLevel: Int,
    onChallengeClick: () -> Unit,
    onCrucibleClick: (String) -> Unit,
    onClubChatClick: () -> Unit,
    onIronCircleClick: () -> Unit
) {
    val crucibleRecentlyTouched = isRecentTimestamp(prefManager.crucibleRunStartedAt, 21) ||
        prefManager.activeCrucibleId.isNotBlank()
    val ironCircleRecentlyTouched = CurrentVersionScope.SHOW_IRON_CIRCLE && (prefManager.ironCircleEnabled ||
        (socialOverview?.friendCount ?: 0) > 0
        )
    val hasCrucibleHistory =
        prefManager.crucibleTotalCompleted > 0 ||
            prefManager.crucibleTotalFailed > 0 ||
            prefManager.activeCrucibleId.isNotBlank()
    val shouldShowCrucibleCard = currentLevel >= 3 && (activeCrucible != null || (seasonalCrucible != null && hasCrucibleHistory))
    val showCrucibleFull = activeCrucible != null && currentLevel >= 3
    val showCrucibleCompact = shouldShowCrucibleCard && !showCrucibleFull
    val hasIronCircleSignals = CurrentVersionScope.SHOW_IRON_CIRCLE && currentLevel >= 3 && (prefManager.ironCircleEnabled ||
        (socialOverview?.friendCount ?: 0) > 0 ||
        (socialOverview?.pendingRequestCount ?: 0) > 0 ||
        (socialOverview?.recentReactionCount ?: 0) > 0)
    val showIronCircleFull = CurrentVersionScope.SHOW_IRON_CIRCLE && currentLevel >= 3 && ((socialOverview?.friendCount ?: 0) > 0 ||
        (socialOverview?.pendingRequestCount ?: 0) > 0 ||
        (socialOverview?.recentReactionCount ?: 0) > 0)
    val showIronCircleCompact = hasIronCircleSignals && !showIronCircleFull && ironCircleRecentlyTouched
    val shouldShowEcosystemHeader = emergencyCooldownUntil > cooldownClock ||
        shouldShowCrucibleCard ||
        showIronCircleFull ||
        showIronCircleCompact
    if (!shouldShowEcosystemHeader) return

    item {
        HomeAuxSectionHeader(
            title = "CIRCLE & CHALLENGE",
            subtitle = "Only the challenge, recovery, and accountability systems that matter right now."
        )
    }

    if (emergencyCooldownUntil > cooldownClock) {
        item {
            EmergencyCooldownCard(
                remainingMillis = emergencyCooldownUntil - cooldownClock
            )
        }
    }

    if (shouldShowCrucibleCard) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeCrucible != null) {
                    CrucibleMiniCard(
                        title = activeCrucible.title,
                        currentDay = crucibleManager.getCurrentDayOfCrucible().toInt(),
                        totalDays = activeCrucible.durationDays,
                        completedDays = prefManager.crucibleCompletedDates.size,
                        todaySecured = crucibleManager.hasCompletedToday(),
                        accentColor = activeCrucible.accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onCrucibleClick(activeCrucible.id) }
                    )
                }
            }
        }
    }

    if (showCrucibleCompact) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showCrucibleCompact) {
                    val seasonal = seasonalCrucible ?: return@Column
                    CompactEcosystemStrip(
                        title = seasonal.title,
                        subtitle = if (crucibleRecentlyTouched) {
                            "Your last crucible run is still fresh. Re-enter when you want another test."
                        } else {
                            "Seasonal crucible available. Open when you're ready to take another run."
                        },
                        accent = seasonal.accentColor,
                        icon = "🔥",
                        onClick = { onCrucibleClick(seasonal.id) }
                    )
                }
            }
        }
    }

    if (showIronCircleFull) {
        item {
            Surface(
                onClick = onIronCircleClick,
                color = DeepBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.16f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SuccessGreen.copy(alpha = 0.14f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Groups,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("IRON CIRCLE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                        Text(
                            when {
                                !prefManager.ironCircleEnabled -> "Private accountability network · finish your safe setup"
                                (socialOverview?.pendingRequestCount ?: 0) > 0 -> "${socialOverview?.pendingRequestCount ?: 0} pending requests · ${socialOverview?.friendCount ?: 0} trusted people"
                                (socialOverview?.recentReactionCount ?: 0) > 0 -> "${socialOverview?.recentReactionCount ?: 0} recent signals · ${socialOverview?.friendCount ?: 0} friends in circle"
                                (socialOverview?.friendCount ?: 0) > 0 -> "${socialOverview?.friendCount ?: 0} trusted people · compare safely and send signals"
                                else -> "Circle enabled · open it when you want to bring trusted people in"
                            },
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        if ((socialOverview?.pendingRequestCount ?: 0) > 0 || (socialOverview?.recentReactionCount ?: 0) > 0) {
                            Spacer(modifier = Modifier.size(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if ((socialOverview?.pendingRequestCount ?: 0) > 0) {
                                    Surface(
                                        color = NeonCyan.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Text(
                                            "${socialOverview?.pendingRequestCount ?: 0} waiting",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                if ((socialOverview?.recentReactionCount ?: 0) > 0) {
                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Text(
                                            "${socialOverview?.recentReactionCount ?: 0} signals",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }
    }

    if (showIronCircleCompact) {
        item {
            CompactEcosystemStrip(
                title = "IRON CIRCLE",
                subtitle = if (prefManager.ironCircleEnabled) {
                    "Circle is enabled, but quiet. Open it when you want accountability or comparison."
                } else {
                    "Your circle exists, but nothing needs you right now."
                },
                accent = SuccessGreen,
                icon = "👥",
                onClick = onIronCircleClick
            )
        }
    }
}

@Composable
private fun CompactEcosystemStrip(
    title: String,
    subtitle: String,
    accent: Color,
    icon: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accent.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.8.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.62f), fontSize = 11.sp, lineHeight = 16.sp)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
    }
}

internal fun isRecentTimestamp(timestamp: Long, days: Long): Boolean {
    if (timestamp <= 0L) return false
    val cutoffMillis = days * 24L * 60L * 60L * 1000L
    return System.currentTimeMillis() - timestamp <= cutoffMillis
}

@Composable
fun ChallengeMiniCard(
    title: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isActive) GoldXP.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.04f)),
        modifier = modifier.then(if (isActive) Modifier.scale(scale) else Modifier)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CHALLENGE", color = if (isActive) GoldXP.copy(alpha = 0.88f) else Color.Gray, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.sp)
                if (isActive) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Box(modifier = Modifier.size(6.dp).background(GoldXP.copy(alpha = 0.82f), CircleShape))
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏰", fontSize = 18.sp)
                Spacer(modifier = Modifier.size(10.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun FiveAmClubMiniCard(
    challengeActive: Boolean,
    readerUnlocked: Boolean,
    fullUnlocked: Boolean,
    dayCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = when {
        fullUnlocked -> GoldXP
        readerUnlocked -> TextPrimary
        challengeActive -> WarningAmber
        else -> TextPrimary
    }
    val surfaceColor = when {
        fullUnlocked -> DeepBackground
        readerUnlocked -> SurfaceDark
        challengeActive -> DeepBackground
        else -> DeepBackground
    }
    val stateLabel = when {
        fullUnlocked -> "FULL MEMBER"
        readerUnlocked -> "READER ACCESS"
        challengeActive -> "RUN ACTIVE"
        else -> "STORED PROGRESS"
    }
    val summary = when {
        fullUnlocked -> "The run is complete and the room is fully yours."
        readerUnlocked -> "The room is open. Keep climbing toward full member voice."
        challengeActive -> "The challenge is live and the room is being earned."
        else -> "Your mornings are still tracked here. Re-enter when you want the next push."
    }

    Surface(
        onClick = onClick,
        color = surfaceColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("☀", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("5 AM CLUB", color = accent, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
                    Text(stateLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.size(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = accent.copy(alpha = if (fullUnlocked) 0.84f else 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        if (fullUnlocked) "ROOM OPEN" else if (readerUnlocked) "ROOM UNLOCKED" else "RUN LADDER",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = if (fullUnlocked) Color.Black else accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
                Surface(
                    color = NeonCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        "DAY $dayCount",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(summary, color = Color.Gray, fontSize = 11.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
fun CrucibleMiniCard(
    title: String,
    currentDay: Int,
    totalDays: Int,
    completedDays: Int,
    todaySecured: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.24f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (todaySecured) "CRUCIBLE: TODAY SECURED" else "CRUCIBLE: DAY $currentDay",
                color = accentColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Spacer(modifier = Modifier.size(6.dp))
            Text("$completedDays / $totalDays verified days", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun CrucibleHeraldCard(
    title: String,
    tagline: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NEW CRUCIBLE", color = accentColor.copy(alpha = 0.88f), fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚔️", fontSize = 18.sp)
                Spacer(modifier = Modifier.size(10.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }
            Spacer(modifier = Modifier.size(6.dp))
            Text(tagline, color = Color.Gray, fontSize = 11.sp, maxLines = 2)
        }
    }
}

@Composable
fun EmergencyValveButton(tokens: Int, onClick: () -> Unit) {
    Surface(
        onClick = if (tokens > 0) onClick else ({}),
        color = if (tokens > 0) DeepBackground else DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (tokens > 0) ErrorRed.copy(alpha = 0.26f) else Color.DarkGray.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(if (tokens > 0) ErrorRed.copy(alpha = 0.1f) else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = if (tokens > 0) ErrorRed else Color.DarkGray,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("RESET PROTOCOL", color = if (tokens > 0) Color.White else Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.9.sp)
                Text(if (tokens > 0) "Interrupt the spiral before breach." else "No rescue tokens remaining this month.", color = Color.Gray, fontSize = 11.sp)
            }
            Surface(
                color = if (tokens > 0) ErrorRed.copy(alpha = 0.16f) else Color.Transparent,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    "$tokens TOKENS",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (tokens > 0) ErrorRed else Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun EmergencyCooldownCard(remainingMillis: Long) {
    val remainingMinutes = (remainingMillis / 60_000L).coerceAtLeast(0L)
    val shieldLabel = if (remainingMinutes >= 9L) "Decision Delay Active" else "Recovery Reset Active"

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SuccessGreen.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡", fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("RECOVERY SHIELD ACTIVE", color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.size(4.dp))
                Text(shieldLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    "Blocked apps still stay blocked. Punishment is softened while this cooldown runs.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Surface(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TIME LEFT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        formatCooldownRemaining(remainingMillis),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

private fun formatCooldownRemaining(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

// =========================================================
// Previews
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun ChallengeMiniCardPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ChallengeMiniCard(
            title = "Iron Challenge: Week 3",
            isActive = true,
            onClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun FiveAmClubMiniCardPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        FiveAmClubMiniCard(
            challengeActive = true,
            readerUnlocked = true,
            fullUnlocked = false,
            dayCount = 14,
            onClick = {}
        )
    }
}