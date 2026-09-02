package com.sanket_satpute_20.ironmind.home

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import com.sanket_satpute_20.ironmind.gamification.gamifiedClick
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanket_satpute_20.ironmind.psychology.ModeSuggestionDialog
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

private data class BottomNavTab(
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    BottomNavTab("Home", Icons.Rounded.Home),
    BottomNavTab("Progress", Icons.Rounded.Analytics),
    BottomNavTab("Profile", Icons.Rounded.Person)
)

@Composable
fun UsageAccessBanner(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    if (!hasPermission) {
        Surface(
            color = DeepBackground, // Dark red background
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .gamifiedClick { onRequestPermission() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = "Warning",
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable AI Tracking",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Required to accurately profile your behavior and adapt app difficulty automatically. Tap to enable Usage Access.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AccessibilityFallbackBanner(
    isAccessibilityEnabled: Boolean,
    isServiceAlive: Boolean,
    onRequestPermission: () -> Unit
) {
    if (isAccessibilityEnabled && !isServiceAlive) {
        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .gamifiedClick { onRequestPermission() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = "Warning",
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Guard is Dead",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your device killed the background service. Tap to restart it by toggling Accessibility off and on.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun MainScreen(
    viewModel: TaskViewModel = viewModel(),
    onTaskSkipped: () -> Unit,
    onStatsClick: () -> Unit,
    onWeeklyScoreClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTemptationLogClick: () -> Unit,
    onGraveyardClick: () -> Unit,
    onPermissionCenterClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onIgnitionHistoryClick: () -> Unit,
    onVoiceLogsClick: () -> Unit,
    onVoiceRepClick: () -> Unit,
    onPrepBuilderClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onEmergencyValveClick: () -> Unit,
    onIdentitySetupClick: () -> Unit,
    onCommitmentContractClick: () -> Unit,
    onDistractionsSetupClick: () -> Unit,
    onCrucibleClick: (String) -> Unit,
    onCrucibleWon: (String) -> Unit,
    onCrucibleFailed: (String) -> Unit,
    onClubChatClick: () -> Unit,
    onIronCircleClick: () -> Unit,
    onStreakMilestone: (Int) -> Unit = {},
    onAccountLinkClick: () -> Unit = {},
    onDailyCompleteClick: () -> Unit = {},
    onArtifactsClick: () -> Unit = {},
    onModeSwitcherClick: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    var hasUsageStatsPermission by remember { mutableStateOf(checkUsageStatsPermission(context)) }

    // Re-check permission when app resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStatsPermission = checkUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = TextPrimary,
        selectedTextColor = TextPrimary,
        indicatorColor = TextPrimary.copy(alpha = 0.12f),
        unselectedIconColor = Color.White.copy(alpha = 0.48f),
        unselectedTextColor = Color.White.copy(alpha = 0.48f)
    )

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(
                containerColor = DeepBackground,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = {
                            Text(
                                tab.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        },
                        colors = navBarItemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            UsageAccessBanner(
                hasPermission = hasUsageStatsPermission,
                onRequestPermission = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            )

            var isServiceAlive by remember { mutableStateOf(com.sanket_satpute_20.ironmind.blocker.IronMindAccessibilityService.isAlive) }
            val isAccessibilityEnabled = com.sanket_satpute_20.ironmind.utils.PermissionHelper.isAccessibilityEnabled(context)

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isServiceAlive = com.sanket_satpute_20.ironmind.blocker.IronMindAccessibilityService.isAlive
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            var showAccessibilityConsent by remember { mutableStateOf(false) }

            if (showAccessibilityConsent) {
                com.sanket_satpute_20.ironmind.ui.components.AccessibilityConsentDialog(
                    onAgree = {
                        showAccessibilityConsent = false
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    onDecline = {
                        showAccessibilityConsent = false
                    }
                )
            }

            AccessibilityFallbackBanner(
                isAccessibilityEnabled = isAccessibilityEnabled,
                isServiceAlive = isServiceAlive,
                onRequestPermission = {
                    showAccessibilityConsent = true
                }
            )

            ModeSuggestionDialog(
                onDecline = { /* Simply dismisses without rejecting completely */ },
                onWrongRecommendation = { mode -> 
                    viewModel.prefManager.lastRejectedRecommendationMode = mode.name
                    viewModel.prefManager.lastRejectedRecommendationDate = java.time.LocalDate.now().toString()
                    com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logRecommendationAction(mode.name, false)
                },
                onAccept = { mode ->
                    com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logRecommendationAction(mode.name, true)
                    // Handled inside the dialog (switchMode is called).
                }
            )
            
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onTaskSkipped = onTaskSkipped,
                        onStatsClick = onStatsClick,
                        onWeeklyScoreClick = onWeeklyScoreClick,
                        onSettingsClick = onSettingsClick,
                        onTemptationLogClick = onTemptationLogClick,
                        onGraveyardClick = onGraveyardClick,
                        onPermissionCenterClick = onPermissionCenterClick,
                        onChallengeClick = onChallengeClick,
                        onIgnitionHistoryClick = onIgnitionHistoryClick,
                        onVoiceLogsClick = onVoiceLogsClick,
                        onVoiceRepClick = onVoiceRepClick,
                        onPrepBuilderClick = onPrepBuilderClick,
                        onAddTaskClick = onAddTaskClick,
                        onEmergencyValveClick = onEmergencyValveClick,
                        onIdentitySetupClick = onIdentitySetupClick,
                        onCommitmentContractClick = onCommitmentContractClick,
                        onDistractionsSetupClick = onDistractionsSetupClick,
                        onCrucibleClick = onCrucibleClick,
                        onCrucibleWon = onCrucibleWon,
                        onCrucibleFailed = onCrucibleFailed,
                        onClubChatClick = onClubChatClick,
                        onIronCircleClick = onIronCircleClick,
                        onStreakMilestone = onStreakMilestone,
                        onAccountLinkClick = onAccountLinkClick,
                        onDailyCompleteClick = onDailyCompleteClick
                    )
                    1 -> ProgressScreen(
                        viewModel = viewModel,
                        onStatsClick = onStatsClick,
                        onWeeklyScoreClick = onWeeklyScoreClick,
                        onGraveyardClick = onGraveyardClick,
                        onPermissionCenterClick = onPermissionCenterClick,
                        onChallengeClick = onChallengeClick,
                        onIgnitionHistoryClick = onIgnitionHistoryClick,
                        onVoiceLogsClick = onVoiceLogsClick,
                        onVoiceRepClick = onVoiceRepClick,
                        onPrepBuilderClick = onPrepBuilderClick,
                        onCrucibleClick = onCrucibleClick,
                        onClubChatClick = onClubChatClick,
                        onIronCircleClick = onIronCircleClick
                    )
                    2 -> ProfileScreen(
                        viewModel = viewModel,
                        onSettingsClick = onSettingsClick,
                        onGraveyardClick = onGraveyardClick,
                        onAccountLinkClick = onAccountLinkClick,
                        onIdentitySetupClick = onIdentitySetupClick,
                        onCommitmentContractClick = onCommitmentContractClick,
                        onDistractionsSetupClick = onDistractionsSetupClick,
                        onArtifactsClick = onArtifactsClick,
                        onModeSwitcherClick = onModeSwitcherClick
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun MainScreenPreview() {
    if (!androidx.compose.ui.platform.LocalInspectionMode.current) return
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        MainScreenPreviewContent()
    }
}

@Composable
private fun MainScreenPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
        .background(DeepBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("IRON OPERATOR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Text("LEVEL 7  •  21 DAY STREAK", color = TextPrimary, fontWeight = FontWeight.Bold)
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.07f)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NEXT MISSION", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Deep Focus Block", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("09:00 — 10:30", color = TextPrimary)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        NavigationBar(containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = index == 0,
                    onClick = {},
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }
    }
}

