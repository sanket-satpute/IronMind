package com.sanket_satpute_20.ironmind.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sanket_satpute_20.ironmind.analytics.AnalyticsManager
import com.sanket_satpute_20.ironmind.product.CurrentVersionScope
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine
import com.sanket_satpute_20.ironmind.ui.components.ProgressRing
import com.sanket_satpute_20.ironmind.ui.components.StreakFlame
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import java.time.LocalDate

@Composable
fun ProfileScreen(
    viewModel: TaskViewModel,
    onSettingsClick: () -> Unit,
    onGraveyardClick: () -> Unit,
    onAccountLinkClick: () -> Unit,
    onIdentitySetupClick: () -> Unit,
    onCommitmentContractClick: () -> Unit,
    onDistractionsSetupClick: () -> Unit,
    onArtifactsClick: () -> Unit,
    onModeSwitcherClick: () -> Unit
) {
    val userSnapshot by viewModel.userSnapshot.collectAsState()
    val currentXp by viewModel.totalXp.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val streak = viewModel.prefManager.streakCount

    val userType = AdaptiveEngine.getCurrentType(viewModel.prefManager)
    val currentMode = AdaptiveEngine.getCurrentMode(viewModel.prefManager)
    val identityTitle = IdentityLevelEngine.getIdentityTitle(userType, currentLevel)
    val nextLevelProgress = IdentityLevelEngine.getProgressToNextLevel(currentXp)
    val progressPercent = (nextLevelProgress.coerceIn(0f, 1f) * 100f).toInt()

    LaunchedEffect(Unit) { AnalyticsManager.logProfileRevealed() }

    val isAnonymous = userSnapshot?.isAnonymous ?: true
    val displayName = userSnapshot?.displayName ?: "Anonymous"
    val email = userSnapshot?.email
    val photoUrl = userSnapshot?.photoUrl

    val foundationNeedsIdentity = !viewModel.prefManager.identitySetupComplete
    val foundationNeedsContract = !viewModel.prefManager.commitmentContractComplete
    val foundationNeedsProtection = !viewModel.prefManager.distractionsSetupComplete
    val hasFoundationStepsPending = foundationNeedsIdentity || foundationNeedsContract || foundationNeedsProtection
    val foundationFirstHomeDate = remember(viewModel.prefManager.firstHomeArrivalDate) {
        runCatching { LocalDate.parse(viewModel.prefManager.firstHomeArrivalDate) }.getOrNull()
    }
    val shouldShowFoundation = shouldShowFoundationFull(
        hasFoundationStepsPending = hasFoundationStepsPending,
        foundationRemainingCount = listOf(foundationNeedsIdentity, foundationNeedsContract, foundationNeedsProtection).count { it },
        foundationFirstHomeDate = foundationFirstHomeDate
    ) || hasFoundationStepsPending

    ProfileScreenContent(
        currentLevel = currentLevel,
        streak = streak,
        identityTitle = identityTitle,
        progressPercent = progressPercent,
        nextLevelProgress = nextLevelProgress,
        currentModeName = currentMode.name,
        isAnonymous = isAnonymous,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        shouldShowFoundation = shouldShowFoundation,
        foundationNeedsIdentity = foundationNeedsIdentity,
        foundationNeedsContract = foundationNeedsContract,
        foundationNeedsProtection = foundationNeedsProtection,
        firstHomeArrivalDate = viewModel.prefManager.firstHomeArrivalDate,
        onSettingsClick = onSettingsClick,
        onAccountLinkClick = onAccountLinkClick,
        onIdentitySetupClick = onIdentitySetupClick,
        onCommitmentContractClick = onCommitmentContractClick,
        onDistractionsSetupClick = onDistractionsSetupClick,
        onArtifactsClick = onArtifactsClick,
        onModeSwitcherClick = onModeSwitcherClick
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  ProfileScreenContent — Routes to logged-in vs anonymous layouts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreenContent(
    currentLevel: Int,
    streak: Int,
    identityTitle: String,
    progressPercent: Int,
    nextLevelProgress: Float,
    currentModeName: String,
    isAnonymous: Boolean,
    displayName: String,
    email: String?,
    photoUrl: String?,
    shouldShowFoundation: Boolean,
    foundationNeedsIdentity: Boolean,
    foundationNeedsContract: Boolean,
    foundationNeedsProtection: Boolean,
    firstHomeArrivalDate: String,
    onSettingsClick: () -> Unit,
    onAccountLinkClick: () -> Unit,
    onIdentitySetupClick: () -> Unit,
    onCommitmentContractClick: () -> Unit,
    onDistractionsSetupClick: () -> Unit,
    onArtifactsClick: () -> Unit,
    onModeSwitcherClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeCanvas)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Screen title ──────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "PROFILE",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = HomeTextPrimary,
                        letterSpacing = 2.sp
                    )
                    // Settings shortcut
                    ProfileQuickSettingsButton(onClick = onSettingsClick)
                }
            }

            // ── Main profile hero card — two entirely different states ────
            item {
                if (isAnonymous) {
                    GhostProfileCard(onAccountLinkClick = onAccountLinkClick)
                } else {
                    LoggedInProfileCard(
                        displayName = displayName,
                        email = email,
                        photoUrl = photoUrl,
                        currentLevel = currentLevel,
                        identityTitle = identityTitle,
                        streak = streak,
                        progressPercent = progressPercent,
                        nextLevelProgress = nextLevelProgress,
                        currentModeName = currentModeName
                    )
                }
            }

            // ── Cloud backup banner for logged-in users ───────────────────
            if (!isAnonymous) {
                item { CloudBackupBanner(onClick = onAccountLinkClick) }
            }

            // ── Foundation progress ───────────────────────────────────────
            if (shouldShowFoundation) {
                item {
                    FoundationProgressCard(
                        needsIdentity = foundationNeedsIdentity,
                        needsContract = foundationNeedsContract,
                        needsProtection = foundationNeedsProtection,
                        firstHomeArrivalDate = firstHomeArrivalDate,
                        onIdentityClick = onIdentitySetupClick,
                        onContractClick = onCommitmentContractClick,
                        onProtectionClick = onDistractionsSetupClick
                    )
                }
            }

            // ── Quick links label ─────────────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp).height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(HomeOutline)
                    )
                    Text(
                        "QUICK LINKS",
                        color = HomeTextSecondary,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileLinkRow(
                        icon = Icons.Rounded.SettingsApplications,
                        label = "Settings",
                        accent = TextPrimary,
                        onClick = onSettingsClick
                    )
                    if (CurrentVersionScope.SHOW_ARTIFACTS) {
                        ProfileLinkRow(
                            icon = Icons.Rounded.CollectionsBookmark,
                            label = "Artifacts Collection",
                            accent = ElectricViolet,
                            onClick = onArtifactsClick
                        )
                    }
                    if (CurrentVersionScope.SHOW_ADVANCED_PSYCHOLOGY) {
                        ProfileLinkRow(
                            icon = Icons.Rounded.SwapHoriz,
                            label = "Mode Switcher",
                            accent = WarningAmber,
                            onClick = onModeSwitcherClick
                        )
                    }
                    ProfileLinkRow(
                        icon = Icons.Rounded.Feedback,
                        label = "Beta Feedback",
                        accent = SuccessGreen,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@ironmind.app")
                                putExtra(Intent.EXTRA_SUBJECT, "IronMind Beta Feedback")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send feedback"))
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(36.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  GhostProfileCard — Anonymous / not logged in state
//
//  Dark mystery card with amber warning glow. Shows the "ghost" of what they're
//  missing: locked progress ring, locked identity, animated "link account" CTA.
//
//  ┌──────────────────────────────────────────────────────┐
//  │ amber glow top halo                                   │
//  │  [Ghost ring 🔒]   GHOST OPERATOR                    │
//  │                    Your progress is local-only.       │
//  │  ⚠ Your data lives only on this device.             │
//  │  [══ LINK ACCOUNT — PROTECT YOUR PROGRESS ══]        │
//  │  [Google] [Email]  (pulsing gradient, 56dp)          │
//  │  Tiny: "No account required to use IronMind"        │
//  └──────────────────────────────────────────────────────┘
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GhostProfileCard(onAccountLinkClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "ghost_pulse")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.25f, targetValue = 0.60f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ghost_glow"
    )
    val ctaScale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.012f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ghost_cta_scale"
    )
    val scanLine by infinite.animateFloat(
        initialValue = -0.1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "ghost_scan"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(HomeSurface)
            .drawBehind {
                // Amber ambient glow from top
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(WarningAmber.copy(alpha = glowAlpha * 0.40f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.50f
                    )
                )
                // Top amber highlight edge
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, WarningAmber.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Scanning line (animated, subtle)
                val scanY = size.height * scanLine
                if (scanY > 0f && scanY < size.height) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, WarningAmber.copy(alpha = 0.10f), Color.Transparent)
                        ),
                        start = Offset(0f, scanY), end = Offset(size.width, scanY),
                        strokeWidth = 40f
                    )
                }
                // Dashed border
                drawRoundRect(
                    color = WarningAmber.copy(alpha = glowAlpha * 0.40f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx()),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Identity row ───────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ghost / locked avatar ring
                GhostAvatarRing(glowAlpha = glowAlpha)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(WarningAmber.copy(alpha = 0.12f))
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "⚠ GUEST MODE",
                                color = WarningAmber,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Text(
                        "GHOST OPERATOR",
                        color = HomeTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        "Progress stored locally only",
                        color = HomeTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // ── Risk banner ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WarningAmber.copy(alpha = 0.08f))
                    .drawBehind {
                        drawLine(
                            color = WarningAmber.copy(alpha = 0.35f),
                            start = Offset(0f, 0f), end = Offset(0f, size.height),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "PROGRESS AT RISK",
                        color = WarningAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "If you uninstall this app or switch devices, every mission, streak, and XP point you've earned disappears. Permanently.",
                        color = HomeTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // ── CTA Button ─────────────────────────────────────────────────
            val ctaInteraction = remember { MutableInteractionSource() }
            val ctaPressed by ctaInteraction.collectIsPressedAsState()
            val ctaPressScale by animateFloatAsState(
                targetValue = if (ctaPressed) 0.96f else ctaScale,
                animationSpec = spring(Spring.DampingRatioMediumBouncy),
                label = "ghost_cta_press"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(ctaPressScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(WarningAmber, WarningAmber.copy(alpha = 0.72f))
                        )
                    )
                    .clickable(interactionSource = ctaInteraction, indication = null, onClick = onAccountLinkClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = HomeCanvas, modifier = Modifier.size(20.dp))
                    Text(
                        "LINK ACCOUNT — SAVE PROGRESS",
                        color = HomeCanvas,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            // ── Sub-note ───────────────────────────────────────────────────
            Text(
                "No subscription required. Link Google or email to sync your data.",
                color = HomeTextSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GhostAvatarRing(glowAlpha: Float) {
    Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
        // Dashed ghost ring canvas
        androidx.compose.foundation.Canvas(modifier = Modifier.size(66.dp)) {
            val strokePx = 2.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            drawCircle(color = WarningAmber.copy(alpha = 0.10f))
            drawArc(
                color = WarningAmber.copy(alpha = glowAlpha * 0.55f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(
                    width = strokePx,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 6f))
                )
            )
        }
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(WarningAmber.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = WarningAmber.copy(alpha = 0.80f), modifier = Modifier.size(22.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LoggedInProfileCard — Authenticated operator HUD
//
//  Premium identity card with avatar, level ring, XP bar, streak, mode badge.
//  Feels like a gaming character card / operator dossier.
//
//  ┌──────────────────────────────────────────────────────┐
//  │ crimson glow top halo                                 │
//  │  [Avatar + neon ring]  IRON OPERATOR  [IRON MODE] 🔥│
//  │                        alex@email.com · Lv. 7        │
//  │  ══════ XP bar ════════════════════════════ 78%      │
//  │  [streak]  [XP]  [level]  [missions]  stat grid      │
//  └──────────────────────────────────────────────────────┘
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoggedInProfileCard(
    displayName: String,
    email: String?,
    photoUrl: String?,
    currentLevel: Int,
    identityTitle: String,
    streak: Int,
    progressPercent: Int,
    nextLevelProgress: Float,
    currentModeName: String
) {
    val infinite = rememberInfiniteTransition(label = "profile_card")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.30f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "profile_glow"
    )
    val xpBarGlow by infinite.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "xp_bar_glow"
    )
    val modeColor = when (currentModeName.uppercase()) {
        "IRON"    -> HomeAction
        "FOCUS"   -> HomeFocus
        "RECOVER" -> SuccessGreen
        else      -> HomeReward
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(HomeSurface)
            .drawBehind {
                // Mode-color ambient glow from top
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(modeColor.copy(alpha = glowAlpha * 0.35f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.45f
                    )
                )
                // Top highlight edge
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, modeColor.copy(alpha = 0.60f), Color.Transparent)
                    ),
                    start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Solid neon border
                drawRoundRect(
                    color = modeColor.copy(alpha = glowAlpha * 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Avatar + identity row ──────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                OperatorAvatarRing(
                    photoUrl = photoUrl,
                    displayName = displayName,
                    modeColor = modeColor,
                    glowAlpha = glowAlpha,
                    progressPercent = progressPercent,
                    nextLevelProgress = nextLevelProgress
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    // Mode badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(modeColor.copy(alpha = 0.15f))
                                .drawBehind {
                                    drawRoundRect(
                                        color = modeColor.copy(alpha = glowAlpha * 0.35f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(50f),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "⚡ ${currentModeName.uppercase()} MODE",
                                color = modeColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(HomeOutline)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "LVL $currentLevel",
                                color = HomeTextSecondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                    Text(
                        identityTitle.uppercase(),
                        color = HomeTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        letterSpacing = (-0.3).sp
                    )
                    if (email != null) {
                        Text(email, color = HomeTextSecondary, fontSize = 11.sp)
                    } else {
                        Text(displayName, color = HomeTextSecondary, fontSize = 11.sp)
                    }
                }
                StreakFlame(streakCount = streak)
            }

            // ── XP progress bar ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "XP TO NEXT LEVEL",
                        color = HomeTextSecondary,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "$progressPercent%",
                        color = HomeReward,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
                // Animated XP bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(HomeOutline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(nextLevelProgress.coerceIn(0f, 1f))
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(HomeReward.copy(alpha = 0.7f), HomeReward)
                                )
                            )
                    )
                    // Glow tip
                    if (nextLevelProgress > 0.04f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(nextLevelProgress.coerceIn(0f, 1f))
                                .height(7.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(HomeReward.copy(alpha = xpBarGlow * 0.70f))
                            )
                        }
                    }
                }
            }

            // ── Stat chips row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStatChip("🔥 $streak", "STREAK", WarningAmber, Modifier.weight(1f))
                ProfileStatChip("LVL $currentLevel", "LEVEL", modeColor, Modifier.weight(1f))
                ProfileStatChip("$progressPercent%", "XP PROG", HomeReward, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OperatorAvatarRing(
    photoUrl: String?,
    displayName: String,
    modeColor: Color,
    glowAlpha: Float,
    progressPercent: Int,
    nextLevelProgress: Float
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(nextLevelProgress) { animatedProgress = nextLevelProgress }
    val animRingProgress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "avatar_ring_anim"
    )

    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        // Animated XP ring behind avatar
        androidx.compose.foundation.Canvas(modifier = Modifier.size(72.dp)) {
            val strokePx = 3.5.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)

            drawCircle(color = modeColor.copy(alpha = 0.10f * glowAlpha))
            drawArc(
                color = Color.White.copy(alpha = 0.07f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (animRingProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Transparent, modeColor.copy(alpha = 0.55f), modeColor),
                        center = Offset(size.width / 2f, size.height / 2f)
                    ),
                    startAngle = -90f, sweepAngle = 360f * animRingProgress,
                    useCenter = false, topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Avatar circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(modeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    displayName.take(1).uppercase(),
                    color = modeColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileStatChip(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HomeSurfaceRaised)
            .drawBehind {
                drawRoundRect(
                    color = accent.copy(alpha = 0.20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = accent, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = HomeTextSecondary, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.5.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ProfileQuickSettingsButton — top-right gear shortcut
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileQuickSettingsButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "settings_btn_scale"
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(HomeSurfaceRaised)
            .drawBehind {
                drawCircle(
                    color = HomeOutline,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.SettingsApplications, contentDescription = "Settings", tint = HomeTextSecondary, modifier = Modifier.size(18.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ProfileLinkRow — Quick link row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileLinkRow(icon: ImageVector, label: String, accent: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "link_row_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(HomeSurface)
            .drawBehind {
                drawRoundRect(
                    color = HomeOutline.copy(alpha = if (pressed) 0.55f else 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
                // Left accent edge
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.50f), accent.copy(alpha = 0.10f))
                    ),
                    topLeft = Offset(0f, 10.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height - 20.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, color = HomeTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = HomeTextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E13)
@Composable
fun ProfileScreenLoggedInPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ProfileScreenContent(
            currentLevel = 7,
            streak = 14,
            identityTitle = "Iron Operator",
            progressPercent = 78,
            nextLevelProgress = 0.78f,
            currentModeName = "IRON",
            isAnonymous = false,
            displayName = "Alex Ryker",
            email = "alex@ironmind.app",
            photoUrl = null,
            shouldShowFoundation = false,
            foundationNeedsIdentity = false,
            foundationNeedsContract = false,
            foundationNeedsProtection = false,
            firstHomeArrivalDate = LocalDate.now().minusDays(7).toString(),
            onSettingsClick = {}, onAccountLinkClick = {}, onIdentitySetupClick = {},
            onCommitmentContractClick = {}, onDistractionsSetupClick = {},
            onArtifactsClick = {}, onModeSwitcherClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E13)
@Composable
fun ProfileScreenGuestPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ProfileScreenContent(
            currentLevel = 1,
            streak = 3,
            identityTitle = "Initiate",
            progressPercent = 22,
            nextLevelProgress = 0.22f,
            currentModeName = "IRON",
            isAnonymous = true,
            displayName = "Anonymous",
            email = null,
            photoUrl = null,
            shouldShowFoundation = true,
            foundationNeedsIdentity = true,
            foundationNeedsContract = true,
            foundationNeedsProtection = false,
            firstHomeArrivalDate = LocalDate.now().minusDays(2).toString(),
            onSettingsClick = {}, onAccountLinkClick = {}, onIdentitySetupClick = {},
            onCommitmentContractClick = {}, onDistractionsSetupClick = {},
            onArtifactsClick = {}, onModeSwitcherClick = {}
        )
    }
}
