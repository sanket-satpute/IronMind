package com.sanket_satpute_20.ironmind.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sanket_satpute_20.ironmind.auth.LoginScreen
import com.sanket_satpute_20.ironmind.challenge.ChallengeAcceptScreen
import com.sanket_satpute_20.ironmind.challenge.FiveAmClubHubScreen

import com.sanket_satpute_20.ironmind.chat.ClubChatScreen
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionActivity
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionHistoryScreen
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.FirebaseSyncRepository
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.graveyard.GraveyardScreen
import com.sanket_satpute_20.ironmind.home.MainScreen
import com.sanket_satpute_20.ironmind.home.TaskViewModel
import com.sanket_satpute_20.ironmind.onboarding.FocusTaskType
import com.sanket_satpute_20.ironmind.onboarding.MissionType
import com.sanket_satpute_20.ironmind.onboarding.IntroPromiseScreen
import com.sanket_satpute_20.ironmind.onboarding.TaskBuilderScreen
import com.sanket_satpute_20.ironmind.onboarding.AppSelectorScreen

import com.sanket_satpute_20.ironmind.operations.OperationsScreen
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchActivity
import com.sanket_satpute_20.ironmind.morninglaunch.MorningUnlockReviewActivity
import com.sanket_satpute_20.ironmind.psychology.*
import com.sanket_satpute_20.ironmind.settings.ManualTypeSelectScreen
import com.sanket_satpute_20.ironmind.settings.PsychologySettingsScreen
import com.sanket_satpute_20.ironmind.settings.DebugEventScreen
import com.sanket_satpute_20.ironmind.settings.PrivacyAndDataScreen
import com.sanket_satpute_20.ironmind.stats.StatsScreen
import com.sanket_satpute_20.ironmind.temptation.TemptationLogScreen
import com.sanket_satpute_20.ironmind.voice.VoiceLogsScreen
import com.sanket_satpute_20.ironmind.voice.VoiceCheckInScreen
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockSetupScreen
import com.sanket_satpute_20.ironmind.ui.language.LanguageSelectionScreen
import com.sanket_satpute_20.ironmind.ui.splash.SplashScreen
import com.sanket_satpute_20.ironmind.health.HealthAnalyticsScreen
import com.sanket_satpute_20.ironmind.health.HealthDashboardScreen
import com.sanket_satpute_20.ironmind.focus.EmergencyCooldownScreen
import com.sanket_satpute_20.ironmind.focus.EmergencyValveScreen
import com.sanket_satpute_20.ironmind.focus.SpringControlScreen
import com.sanket_satpute_20.ironmind.crucible.CrucibleScreen
import com.sanket_satpute_20.ironmind.crucible.CrucibleCompleteScreen
import com.sanket_satpute_20.ironmind.crucible.CrucibleFailedScreen
import com.sanket_satpute_20.ironmind.artifact.ArtifactCollectionScreen
import com.sanket_satpute_20.ironmind.detox.DetoxSchedulerScreen
import com.sanket_satpute_20.ironmind.autopsy.ReviewConsoleScreen
import com.sanket_satpute_20.ironmind.contract.CommitmentContractScreen
import com.sanket_satpute_20.ironmind.rewards.DailyCompleteScreen
import com.sanket_satpute_20.ironmind.rewards.StreakCelebrationScreen
import com.sanket_satpute_20.ironmind.share.ShareableCardScreen
import com.sanket_satpute_20.ironmind.accountability.AccountabilitySetupScreen
import com.sanket_satpute_20.ironmind.identity.IdentitySetupScreen
import com.sanket_satpute_20.ironmind.social.ContactDiscoveryScreen
import com.sanket_satpute_20.ironmind.social.FriendCompareScreen
import com.sanket_satpute_20.ironmind.social.IronCircleScreen
import com.sanket_satpute_20.ironmind.social.IronCircleNetworkScreen
import com.sanket_satpute_20.ironmind.utils.PermissionCenterScreen
import com.sanket_satpute_20.ironmind.utils.PermissionRationaleScreen
import java.time.LocalDate
import java.util.Locale

@Composable
fun MainNavHost(
    crucibleFailedTrigger: Boolean = false,
    failedCrucibleTitle: String = "",
    externalRoute: String? = null,
    onExternalRouteHandled: () -> Unit = {},
    onCrucibleErrorHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    val viewModel: TaskViewModel = viewModel()
    val firebaseSyncRepository = remember { FirebaseSyncRepository() }
    val taskDao = remember { IronMindDatabase.getDatabase(context).taskDao() }
    var diagnosticProfile by remember { mutableStateOf<UserProfile?>(null) }

    // External Trigger Handling
    LaunchedEffect(crucibleFailedTrigger) {
        if (crucibleFailedTrigger) {
            navController.navigate("crucible_failed/$failedCrucibleTitle") {
                popUpTo("home") { inclusive = false }
            }
            onCrucibleErrorHandled()
        }
    }

    LaunchedEffect(externalRoute) {
        externalRoute?.takeIf { it.isNotBlank() }?.let { route ->
            navController.navigate(route) {
                launchSingleTop = true
            }
            onExternalRouteHandled()
        }
    }

    NavHost(
        navController = navController, 
        startDestination = "splash",
        enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it / 2 } + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it / 2 } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
    ) {
        
        composable("splash") {
            SplashScreen(onSplashComplete = {
                navController.navigate(resolveEntryRoute(prefs)) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("language_select") {
            LanguageSelectionScreen(
                onLanguageSelected = { lang ->
                    prefs.appLanguage = lang.code
                    prefs.languageSelected = true
                    navController.navigate("intro_promise") {
                        popUpTo("language_select") { inclusive = true }
                    }
                }
            )
        }

        composable("intro_promise") {
            IntroPromiseScreen(
                onBegin = {
                    prefs.introSeen = true
                    navController.navigate("diagnostic") {
                        popUpTo("intro_promise") { inclusive = true }
                    }
                }
            )
        }

        composable("diagnostic") {
            DiagnosticScreen(
                onComplete = { profile ->
                    diagnosticProfile = profile
                    prefs.diagnosticDone = true
                    navController.navigate("profile_reveal") {
                        popUpTo("diagnostic") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingRoot(onOnboardingComplete = { onboardingResult ->
                prefs.intensity = onboardingResult.intensity
                prefs.onboardingComplete = true
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }

        composable("home") {
            MainScreen(
                viewModel = viewModel,
                onTaskSkipped = { /* Handled by ViewModel */ },
                onStatsClick = { navController.navigate("stats") },
                onWeeklyScoreClick = { navController.navigate("weekly_checkin") },
                onSettingsClick = { navController.navigate("operations") },
                onTemptationLogClick = { navController.navigate("temptation_log") },
                onGraveyardClick = { navController.navigate("graveyard") },
                onPermissionCenterClick = { navController.navigate("permission_center") },
                onChallengeClick = { navController.navigate("five_am_club") },
                onIgnitionHistoryClick = { navController.navigate("confidence_ignition_history") },
                onVoiceLogsClick = { navController.navigate("voice_logs") },
                onVoiceRepClick = { navController.navigate("voice_checkin") },
                onPrepBuilderClick = { navController.navigate("task_builder?date=${LocalDate.now()}&type=prep") },
                onAddTaskClick = { navController.navigate("task_builder") },
                onEmergencyValveClick = { navController.navigate("emergency_valve") },
                onIdentitySetupClick = { navController.navigate("identity_setup") },
                onCommitmentContractClick = { navController.navigate("commitment_contract") },
                onDistractionsSetupClick = { navController.navigate("distractions_config") },
                onCrucibleClick = { id -> navController.navigate("crucible/$id") },
                onCrucibleWon = { id -> navController.navigate("crucible_complete/$id/true/0/0") },
                onCrucibleFailed = { title -> navController.navigate("crucible_failed/$title") },
                onClubChatClick = { navController.navigate("five_am_club") },
                onIronCircleClick = { navController.navigate("iron_circle") },
                onStreakMilestone = { streakDays ->
                    navController.navigate("shareable_card/$streakDays/${Uri.encode(prefs.userName)}")
                },
                onAccountLinkClick = { navController.navigate("login") },
                onDailyCompleteClick = { navController.navigate("daily_complete") },
                onArtifactsClick = { navController.navigate("artifacts_collection") },
                onModeSwitcherClick = { navController.navigate("mode_switcher") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.popBackStack() },
                taskViewModel = viewModel,
                firebaseSyncRepository = firebaseSyncRepository,
                taskDao = taskDao
            )
        }

        composable("operations") {
            OperationsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onOpenPsychologySettings = { navController.navigate("psychology_settings") },
                onOpenModeSwitcher = { navController.navigate("mode_switcher") },
                onOpenPermissionCenter = { navController.navigate("permission_center") },
                onOpenLogin = { navController.navigate("login") },
                onOpenIdentitySetup = { navController.navigate("identity_setup") },
                onOpenCommitmentContract = { navController.navigate("commitment_contract") },
                onOpenDistractionsSetup = { navController.navigate("distractions_config") },
                onOpenSleepLock = { navController.navigate("sleep_lock_setup") },
                onOpenAppReviewQueue = { navController.navigate("distractions_config?review=pending") },
                onOpenEmergencyValve = { navController.navigate("emergency_valve") },
                onOpenPremiumUpgrade = { navController.navigate("premium_upgrade") },
                onOpenSpringControl = { navController.navigate("spring_control") },
                onOpenChallenge = { navController.navigate("five_am_club") },
                onOpenClubChat = { navController.navigate("five_am_club") },
                onOpenIronCircle = { navController.navigate("iron_circle") },
                onTestMorningBrief = {
                    context.startActivity(MorningUnlockReviewActivity.createIntent(context))
                },
                onTestConfidenceIgnition = {
                    context.startActivity(ConfidenceIgnitionActivity.createIntent(context))
                },
                onTestMorningLaunch = {
                    context.startActivity(MorningLaunchActivity.createIntent(context, forceTest = true))
                }
            )
        }

        composable("spring_control") {
            SpringControlScreen(
                onBack = { navController.popBackStack() },
                onOpenMonthlyAudit = { navController.navigate("monthly_audit") },
                onOpenTemptationLog = { navController.navigate("temptation_log") }
            )
        }

        composable("sleep_lock_setup") {
            SleepLockSetupScreen(onBack = { navController.popBackStack() })
        }

        composable("premium_upgrade") {
            com.sanket_satpute_20.ironmind.billing.PremiumUpgradeScreen(
                onBack = { navController.popBackStack() },
                onUpgradeSuccess = { navController.popBackStack() }
            )
        }

        composable("psychology_settings") {
            PsychologySettingsScreen(
                onChangeModeClick = { navController.navigate("mode_switcher") },
                onRetakeDiagnostic = { navController.navigate("diagnostic") },
                onChangeTypeManually = { navController.navigate("manual_type_select") },
                onLinkGoogleClick = { navController.navigate("login") },
                onOpenPremiumUpgrade = { navController.navigate("premium_upgrade") },
                onManageDistractions = { navController.navigate("distractions_config") },
                navController = navController,
                taskViewModel = viewModel
            )
        }

        composable("five_am_club") {
            FiveAmClubHubScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("club_chat") { ClubChatScreen(onBack = { navController.popBackStack() }) }
        composable(
            route = "task_builder?date={date}&type={type}",
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStack ->
            val dateArg = backStack.arguments?.getString("date").orEmpty()
            val typeArg = backStack.arguments?.getString("type").orEmpty()
            val initialDate = dateArg
                .takeIf { it.isNotBlank() }
                ?.let { rawDate -> runCatching { LocalDate.parse(rawDate) }.getOrNull() }
            val initialMissionType = when (typeArg.lowercase(Locale.ROOT)) {
                "prep" -> MissionType.PREP
                else -> null
            }
            val initialFocusTaskType = when (typeArg.lowercase(Locale.ROOT)) {
                "prep" -> FocusTaskType.DSA
                else -> null
            }
            TaskBuilderScreen(
                onNext = { navController.popBackStack() },
                initialDate = initialDate,
                initialMissionType = initialMissionType,
                initialFocusTaskType = initialFocusTaskType
            )
        }
        composable("stats") { StatsScreen(onBack = { navController.popBackStack() }, onArtifactsClick = { navController.navigate("artifacts_collection") }, onMonthlyAuditClick = { navController.navigate("review_console?tab=monthly") }, onAutopsyClick = { navController.navigate("review_console?tab=yesterday") }, onHealthCorrelationClick = { navController.navigate("health_dashboard") }, onWeeklyReportClick = { navController.navigate("review_console?tab=weekly") }, onWeeklyScoreClick = { navController.navigate("weekly_checkin") }, onIgnitionHistoryClick = { navController.navigate("confidence_ignition_history") }, onVoiceLogsClick = { navController.navigate("voice_logs") }, onVoiceRepClick = { navController.navigate("voice_checkin") }, onPrepBuilderClick = { navController.navigate("task_builder?date=${LocalDate.now()}&type=prep") }) }
        composable("confidence_ignition_history") {
            ConfidenceIgnitionHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenVoiceTrack = { navController.navigate("voice_logs?filter=ignition") }
            )
        }
        composable(
            route = "review_console?tab={tab}",
            arguments = listOf(navArgument("tab") {
                type = NavType.StringType
                defaultValue = "yesterday"
                nullable = true
            })
        ) { backStack ->
            ReviewConsoleScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("monthly_audit") { com.sanket_satpute_20.ironmind.autopsy.MonthlyAuditScreen(onBack = { navController.popBackStack() }) }
        composable("artifacts_collection") { ArtifactCollectionScreen(onBack = { navController.popBackStack() }) }
        composable("challenge") { ChallengeAcceptScreen(onAccept = { navController.popBackStack() }, onDecline = { navController.popBackStack() }) }
        composable(
            route = "voice_logs?filter={filter}",
            arguments = listOf(navArgument("filter") {
                type = NavType.StringType
                defaultValue = "all"
                nullable = true
            })
        ) { backStack ->
            VoiceLogsScreen(
                onBack = { navController.popBackStack() },
                onRecordToday = { navController.navigate("voice_checkin") },
                initialFilter = backStack.arguments?.getString("filter").orEmpty()
            )
        }
        composable("temptation_log") { TemptationLogScreen(onBack = { navController.popBackStack() }) }
        composable("graveyard") {
            GraveyardScreen(
                onBack = { navController.popBackStack() },
                onRebuildBoardClick = { navController.navigate("task_builder") },
                onModeShiftClick = { navController.navigate("mode_switcher") }
            )
        }
        composable("permission_center") { 
            PermissionCenterScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRationale = { type -> navController.navigate("permission_rationale/${type.name}") }
            ) 
        }
        composable(
            route = "distractions_config?review={review}",
            arguments = listOf(navArgument("review") {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            })
        ) { backStack ->
            AppSelectorScreen(
                onNext = { navController.popBackStack() },
                reviewFilter = backStack.arguments?.getString("review").orEmpty()
            )
        }
        composable("mode_switcher") { ModeSwitcherScreen(onSaved = { navController.popBackStack() }) }
        composable("manual_type_select") { ManualTypeSelectScreen(onSaved = { navController.popBackStack() }) }
        composable("debug_event_screen") { DebugEventScreen(onBack = { navController.popBackStack() }) }
        composable("weekly_checkin") {
            WeeklyCheckInScreen(
                onComplete = { navController.popBackStack() }
            )
        }
        composable("system_analysis") {
            HealthAnalyticsScreen(
                onBack = { navController.popBackStack() },
                onOpenPremiumUpgrade = { navController.navigate("premium_upgrade") }
            )
        }
        composable("health_dashboard") { HealthDashboardScreen(onBack = { navController.popBackStack() }) }
        composable("emergency_valve") {
            EmergencyValveScreen(
                onDismiss = { navController.popBackStack() },
                onFiveMinuteReset = {
                    navController.navigate("emergency_cooldown/reset") {
                        popUpTo("emergency_valve") { inclusive = true }
                    }
                },
                onDecisionDelay = {
                    navController.navigate("emergency_cooldown/delay") {
                        popUpTo("emergency_valve") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "emergency_cooldown/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStack ->
            EmergencyCooldownScreen(
                mode = backStack.arguments?.getString("mode") ?: "reset",
                onReturnToMission = { navController.popBackStack() },
                onBackToHome = { navController.popBackStack() }
            )
        }
        composable("detox_scheduler") { DetoxSchedulerScreen(onSave = { navController.popBackStack() }) }
        composable("autopsy") { ReviewConsoleScreen(onBack = { navController.popBackStack() }) }
        composable(
            route = "commitment_contract?entry={entry}",
            arguments = listOf(navArgument("entry") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStack ->
            val entryFlow = backStack.arguments?.getBoolean("entry") ?: false
            CommitmentContractScreen(
                onSigned = {
                    if (entryFlow) {
                        navController.navigate("onboarding") {
                            popUpTo("commitment_contract?entry=true") { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable("weekly_report") { ReviewConsoleScreen(onBack = { navController.popBackStack() }) }
        composable(
            route = "shareable_card/{streakDays}/{userName}",
            arguments = listOf(
                navArgument("streakDays") { type = NavType.IntType },
                navArgument("userName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStack ->
            ShareableCardScreen(
                streakDays = backStack.arguments?.getInt("streakDays") ?: 0,
                userName = backStack.arguments?.getString("userName") ?: "",
                onContinue = { navController.popBackStack() }
            )
        }
        composable("accountability_setup") { AccountabilitySetupScreen(onSavePartner = { navController.popBackStack() }, onBack = { navController.popBackStack() }) }
        composable("privacy_data") { PrivacyAndDataScreen(onBack = { navController.popBackStack() }) }
        composable(
            route = "permission_rationale/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStack ->
            val typeStr = backStack.arguments?.getString("type") ?: ""
            PermissionRationaleScreen(
                permissionTypeStr = typeStr,
                onBack = { navController.popBackStack() },
                onContinueToSettings = {
                    navController.popBackStack()
                    val type = runCatching { com.sanket_satpute_20.ironmind.utils.PermissionRequirementType.valueOf(typeStr) }.getOrNull()
                    if (type != null) {
                        com.sanket_satpute_20.ironmind.utils.PermissionHelper.openSettingsForRequirement(context, type)
                    }
                }
            )
        }
        composable(
            route = "identity_setup?entry={entry}",
            arguments = listOf(navArgument("entry") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStack ->
            val entryFlow = backStack.arguments?.getBoolean("entry") ?: false
            IdentitySetupScreen(
                onNext = {
                    if (entryFlow) {
                        navController.navigate("commitment_contract?entry=true") {
                            popUpTo("identity_setup?entry=true") { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable("iron_circle") {
            IronCircleScreen(
                onBack = { navController.popBackStack() },
                onDiscoverContacts = { navController.navigate("iron_circle_discovery") },
                onOpenNetwork = { navController.navigate("iron_circle_network") },
                onOpenPremiumUpgrade = { navController.navigate("premium_upgrade") }
            )
        }
        composable("iron_circle_discovery") {
            ContactDiscoveryScreen(
                onBack = { navController.popBackStack() },
                onManageCircle = { navController.navigate("iron_circle_network") }
            )
        }
        composable("iron_circle_network") {
            IronCircleNetworkScreen(
                onBack = { navController.popBackStack() },
                onCompareFriend = { friendUid -> navController.navigate("iron_circle_compare/$friendUid") }
            )
        }
        composable(
            route = "iron_circle_compare/{friendUid}",
            arguments = listOf(navArgument("friendUid") { type = NavType.StringType })
        ) { backStack ->
            FriendCompareScreen(
                friendUid = backStack.arguments?.getString("friendUid") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "crucible/{crucibleId}", arguments = listOf(navArgument("crucibleId") { type = NavType.StringType })) { backStack -> 
            CrucibleScreen(
                crucibleId = backStack.arguments?.getString("crucibleId") ?: "",
                onAbort = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }
        composable(
            route = "crucible_complete/{crucibleId}/{campaignComplete}/{completedDays}/{totalDays}",
            arguments = listOf(
                navArgument("crucibleId") { type = NavType.StringType },
                navArgument("campaignComplete") { type = NavType.BoolType },
                navArgument("completedDays") { type = NavType.IntType },
                navArgument("totalDays") { type = NavType.IntType }
            )
        ) { backStack ->
            CrucibleCompleteScreen(
                crucibleId = backStack.arguments?.getString("crucibleId") ?: "",
                campaignComplete = backStack.arguments?.getBoolean("campaignComplete") ?: false,
                completedDays = backStack.arguments?.getInt("completedDays") ?: 0,
                totalDays = backStack.arguments?.getInt("totalDays") ?: 0,
                onContinue = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable(route = "crucible_failed/{title}", arguments = listOf(navArgument("title") { type = NavType.StringType })) { backStack -> CrucibleFailedScreen(backStack.arguments?.getString("title") ?: "Unknown Crucible", onContinue = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }) }
        composable("voice_checkin") {
            VoiceCheckInScreen(
                taskName = prefs.activeTaskName.ifBlank { "Voice Reflection" },
                onComplete = { navController.popBackStack() }
            )
        }
        composable("profile_reveal") {
            ProfileRevealScreen(
                profile = diagnosticProfile ?: buildProfileFromPrefs(prefs),
                onContinue = {
                    diagnosticProfile = null
                    val nextRoute = resolvePostProfileRevealRoute(prefs)
                    if (nextRoute == null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(nextRoute) {
                            popUpTo("profile_reveal") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("daily_complete") {
            DailyCompleteScreen(
                streak = prefs.streakCount,
                onContinue = { navController.popBackStack() }
            )
        }
        composable("streak_celebration") {
            StreakCelebrationScreen(
                streakCount = prefs.streakCount.coerceAtLeast(1),
                onContinue = { navController.popBackStack() }
            )
        }
    }
}

private fun resolveEntryRoute(prefs: PrefManager): String = when {
    !prefs.languageSelected -> "language_select"
    prefs.onboardingComplete -> "home"
    !prefs.introSeen -> "intro_promise"
    !prefs.diagnosticDone -> "diagnostic"
    !prefs.identitySetupComplete -> "identity_setup?entry=true"
    !prefs.commitmentContractComplete -> "commitment_contract?entry=true"
    !prefs.onboardingComplete -> "onboarding"
    else -> "home"
}

private fun resolvePostProfileRevealRoute(prefs: PrefManager): String? = when {
    prefs.onboardingComplete -> null
    !prefs.identitySetupComplete -> "identity_setup?entry=true"
    !prefs.commitmentContractComplete -> "commitment_contract?entry=true"
    else -> "onboarding"
}

private fun buildProfileFromPrefs(prefs: PrefManager): UserProfile {
    return UserProfile(
        primaryType = AdaptiveEngine.getCurrentType(prefs),
        secondaryType = prefs.secondaryType.takeIf { it.isNotBlank() }?.let { storedType ->
            runCatching { UserType.valueOf(storedType) }.getOrNull()
        },
        assignedMode = AdaptiveEngine.getCurrentMode(prefs),
        typeScores = emptyMap(),
        profileCreatedDate = LocalDate.now().toString()
    )
}
