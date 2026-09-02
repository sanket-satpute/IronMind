package com.sanket_satpute_20.ironmind.home

import android.content.Context
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.graphics.Color
import com.sanket_satpute_20.ironmind.crucible.CrucibleDefinition
import com.sanket_satpute_20.ironmind.crucible.CrucibleManager
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.focus.PomodoroSetupActivity
import com.sanket_satpute_20.ironmind.social.SocialOverview
import com.sanket_satpute_20.ironmind.utils.PermissionPriority
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

internal fun LazyListScope.HomeSupportSection(
    context: Context,
    pomodoroOverview: HomePomodoroOverview?,
    homePerformanceSnapshot: HomePerformanceSnapshot,
    topMissingPermissionPriority: PermissionPriority?,
    canShowPermissionAction: Boolean,
    canShowEmergencyLeaveAction: Boolean,
    shouldShowFiveAmClubQuickAction: Boolean,
    fiveAmClubAccent: Color,
    fiveAmClubDescription: String,
    fiveAmClubBadge: String?,
    prefManager: PrefManager,
    activeCrucible: CrucibleDefinition?,
    seasonalCrucible: CrucibleDefinition?,
    crucibleManager: CrucibleManager,
    socialOverview: SocialOverview?,
    emergencyCooldownUntil: Long,
    cooldownClock: Long,
    currentLevel: Int,
    onStatsClick: () -> Unit,
    onGraveyardClick: () -> Unit,
    onPermissionCenterClick: () -> Unit,
    onEmergencyLeaveClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onCrucibleClick: (String) -> Unit,
    onClubChatClick: () -> Unit,
    onIronCircleClick: () -> Unit
) {
    item {
        HomeQuickActionsRow(
            actions = buildList {
                add(
                    HomeQuickActionSpec(
                        icon = Icons.Rounded.Timer,
                        iconTint = TextPrimary,
                        iconBackground = TextPrimary.copy(alpha = 0.16f),
                        title = "Focus Run",
                        description = "Start a Pomodoro chamber for deep work.",
                        badge = null,
                        surfaceColor = DeepBackground,
                        borderColor = TextPrimary.copy(alpha = 0.24f),
                        onClick = {
                            context.startActivity(PomodoroSetupActivity.createIntent(context))
                        }
                    )
                )
                if (canShowPermissionAction) {
                    add(
                        HomeQuickActionSpec(
                            icon = Icons.Rounded.Security,
                            iconTint = when (topMissingPermissionPriority) {
                                PermissionPriority.CRITICAL -> WarningAmber
                                PermissionPriority.IMPORTANT -> NeonCyan
                                else -> TextPrimary
                            },
                            iconBackground = when (topMissingPermissionPriority) {
                                PermissionPriority.CRITICAL -> WarningAmber.copy(alpha = 0.14f)
                                PermissionPriority.IMPORTANT -> NeonCyan.copy(alpha = 0.14f)
                                else -> TextPrimary.copy(alpha = 0.12f)
                            },
                            title = "Fix Permissions",
                            description = when (topMissingPermissionPriority) {
                                PermissionPriority.CRITICAL -> "Critical device protection is still incomplete."
                                PermissionPriority.IMPORTANT -> "Important protection access still needs review."
                                else -> "Some required protection access is still missing."
                            },
                            badge = when (topMissingPermissionPriority) {
                                PermissionPriority.CRITICAL -> "Critical"
                                PermissionPriority.IMPORTANT -> "Important"
                                else -> "Pending"
                            },
                            surfaceColor = DeepBackground,
                            borderColor = when (topMissingPermissionPriority) {
                                PermissionPriority.CRITICAL -> WarningAmber.copy(alpha = 0.22f)
                                PermissionPriority.IMPORTANT -> NeonCyan.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.08f)
                            },
                            onClick = onPermissionCenterClick
                        )
                    )
                }
                if (canShowEmergencyLeaveAction) {
                    add(
                        HomeQuickActionSpec(
                            icon = Icons.Rounded.EventBusy,
                            iconTint = ErrorRed,
                            iconBackground = ErrorRed.copy(alpha = 0.14f),
                            title = "Leave Tomorrow",
                            description = "Tomorrow is empty. Clear the day only if you truly need it.",
                            badge = "${prefManager.emergencyValveTokens} tokens",
                            surfaceColor = DeepBackground,
                            borderColor = ErrorRed.copy(alpha = 0.22f),
                            onClick = onEmergencyLeaveClick
                        )
                    )
                }
                if (shouldShowFiveAmClubQuickAction) {
                    add(
                        HomeQuickActionSpec(
                            icon = Icons.Rounded.Alarm,
                            iconTint = fiveAmClubAccent,
                            iconBackground = fiveAmClubAccent.copy(alpha = 0.14f),
                            title = "5 AM Club",
                            description = fiveAmClubDescription,
                            badge = fiveAmClubBadge,
                            surfaceColor = DeepBackground,
                            borderColor = fiveAmClubAccent.copy(alpha = 0.22f),
                            onClick = onChallengeClick
                        )
                    )
                }
            }
        )
    }

    pomodoroOverview?.let { overview ->
        item {
            HomePomodoroAnalyticsCard(overview = overview)
        }
    }

    item {
        HomePerformanceCard(
            snapshot = homePerformanceSnapshot,
            onStatsClick = onStatsClick,
            onGraveyardClick = onGraveyardClick
        )
    }

    HomeEcosystemSection(
        prefManager = prefManager,
        activeCrucible = activeCrucible,
        seasonalCrucible = seasonalCrucible,
        crucibleManager = crucibleManager,
        socialOverview = socialOverview,
        emergencyCooldownUntil = emergencyCooldownUntil,
        cooldownClock = cooldownClock,
        currentLevel = currentLevel,
        onChallengeClick = onChallengeClick,
        onCrucibleClick = onCrucibleClick,
        onClubChatClick = onClubChatClick,
        onIronCircleClick = onIronCircleClick
    )
}
