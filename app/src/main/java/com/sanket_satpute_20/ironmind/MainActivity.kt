package com.sanket_satpute_20.ironmind

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import com.sanket_satpute_20.ironmind.data.PrefManager
import androidx.lifecycle.lifecycleScope
import com.sanket_satpute_20.ironmind.integrity.DailyIntegrityEngine
import com.sanket_satpute_20.ironmind.onboarding.FocusBreachOverlay
import com.sanket_satpute_20.ironmind.navigation.MainNavHost
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.utils.LocaleHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.sanket_satpute_20.ironmind.context.ContextIntelligenceService
import kotlinx.coroutines.launch
import com.sanket_satpute_20.ironmind.gamification.*
import androidx.compose.runtime.CompositionLocalProvider


class MainActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_OPEN_ROUTE = "OPEN_ROUTE"

        fun createRouteIntent(context: Context, route: String): Intent =
            Intent(context, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
                putExtra(EXTRA_OPEN_ROUTE, route)
            }
    }

    private var _showBreachOverlay = mutableStateOf(false)
    private var _blockedIdentifier = mutableStateOf("Distraction")
    private var _openRoute = mutableStateOf<String?>(null)
    
    // Crucible state
    private var _crucibleFailed = mutableStateOf(false)
    private var _failedCrucibleTitle = mutableStateOf("")

    override fun attachBaseContext(newBase: Context) {
        val pref = PrefManager.getInstance(newBase)
        super.attachBaseContext(
            LocaleHelper.applyLanguage(newBase, pref.appLanguage)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { false }
            setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }
        super.onCreate(savedInstanceState)
        val pref = PrefManager.getInstance(this)
        handleIntent(intent)
        
        // Relapse Re-entry detection
        if (pref.lastRelapseTimestamp > 0L) {
            val timeSinceFailureMs = System.currentTimeMillis() - pref.lastRelapseTimestamp
            val fortyEightHoursMs = 48 * 60 * 60 * 1000L
            if (timeSinceFailureMs <= fortyEightHoursMs) {
                com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logRelapseReentry(timeSinceFailureMs)
            }
            // Reset to prevent duplicate logging
            pref.lastRelapseTimestamp = 0L
        }

        // ── Phase 6: D1/D7 Retention & Beta Tagging ──────────────────────────
        val nowMs = System.currentTimeMillis()
        if (pref.firstInstallDateEpoch == 0L) {
            // First ever launch — record install time and tag as beta user
            pref.firstInstallDateEpoch = nowMs
            if (!pref.betaUserTagLogged) {
                com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logBetaUserTagged(pref.userType)
                pref.betaUserTagLogged = true
            }
        } else {
            val daysSinceInstall = (nowMs - pref.firstInstallDateEpoch) / (1000L * 60 * 60 * 24)
            if (!pref.d1RetentionLogged && daysSinceInstall >= 1) {
                com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logD1Retention()
                pref.d1RetentionLogged = true
            }
            if (!pref.d7RetentionLogged && daysSinceInstall >= 7) {
                com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logD7Retention()
                pref.d7RetentionLogged = true
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        lifecycleScope.launch {
            val integrityEngine = DailyIntegrityEngine(this@MainActivity)
            integrityEngine.applyStrictnessBoostIfExpired()
            integrityEngine.finalizeYesterdayIfNeeded("APP_OPEN")
        }

        if (pref.hasUnlockedSmartContext) {
            val intent = Intent(this, ContextIntelligenceService::class.java)
            startService(intent)
        }

        // Start Phase 2 Integrity Shield Monitor
        val monitorIntent = Intent(this, com.sanket_satpute_20.ironmind.integrity.ShieldMonitorService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(monitorIntent)
        } else {
            startService(monitorIntent)
        }

        val hapticsManager = com.sanket_satpute_20.ironmind.gamification.HapticsManager(this)
        val soundManager = com.sanket_satpute_20.ironmind.gamification.SoundManager(this)

        setContent {
            CompositionLocalProvider(
                com.sanket_satpute_20.ironmind.gamification.LocalHapticsManager provides hapticsManager,
                com.sanket_satpute_20.ironmind.gamification.LocalSoundManager provides soundManager
            ) {
                IronMindTheme {
                    Box {
                    MainNavHost(
                        crucibleFailedTrigger = _crucibleFailed.value,
                        failedCrucibleTitle = _failedCrucibleTitle.value,
                        externalRoute = _openRoute.value,
                        onExternalRouteHandled = { _openRoute.value = null },
                        onCrucibleErrorHandled = { _crucibleFailed.value = false }
                    )
                    
                    if (_showBreachOverlay.value) {
                        FocusBreachOverlay(
                            blockedItem = _blockedIdentifier.value,
                            onReturn = { _showBreachOverlay.value = false }
                        )
                    }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("SHOW_IDENTITY_FLASH", false)) {
                _blockedIdentifier.value = it.getStringExtra("BLOCKED_IDENTIFIER") ?: "Distraction"
                _showBreachOverlay.value = true
            }

            it.getStringExtra(EXTRA_OPEN_ROUTE)?.let { route ->
                if (route.isNotBlank()) {
                    _openRoute.value = route
                }
            }
            
            if (it.getBooleanExtra("CRUCIBLE_FAILED", false)) {
                _failedCrucibleTitle.value = it.getStringExtra("CRUCIBLE_TITLE") ?: "Unknown Crucible"
                _crucibleFailed.value = true
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val activityGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        
        if (fineLocationGranted || activityGranted) {
            val intent = Intent(this, ContextIntelligenceService::class.java)
            startService(intent)
        }
    }

    private fun requestContextPermissionsAndStartService() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        
        // Note: Android 10+ requires background location to be requested separately after foreground is granted.
        // For simplicity in this step, we just request foreground and activity recognition.

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            val intent = Intent(this, ContextIntelligenceService::class.java)
            startService(intent)
        }
    }
}
