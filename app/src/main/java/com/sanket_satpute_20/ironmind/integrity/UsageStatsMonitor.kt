package com.sanket_satpute_20.ironmind.integrity

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import android.util.Log
import com.sanket_satpute_20.ironmind.blocker.IronMindAccessibilityService
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object UsageStatsMonitor {
    private const val TAG = "UsageStatsMonitor"

    fun startMonitoring(context: Context, scope: CoroutineScope) {
        scope.launch {
            while (isActive) {
                if (isAccessibilityEnabled(context)) {
                    // Accessibility is handling enforcement; sleep for longer.
                    delay(15_000L)
                } else {
                    if (hasUsageStatsPermission(context)) {
                        checkForegroundApp(context)
                    }
                    // Faster polling when Accessibility is off, per user requirement 
                    // (using 5 seconds to balance battery vs reaction time).
                    delay(5_000L)
                }
            }
        }
    }

    private fun checkForegroundApp(context: Context) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 30 // Check last 30 seconds

        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundApp: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForegroundApp = event.packageName
            }
        }

        lastForegroundApp?.let { pkg ->
            val prefs = PrefManager.getInstance(context)
            if (pkg != context.packageName && prefs.blockedApps.contains(pkg)) {
                Log.d(TAG, "Fallback intercept: $pkg")
                
                // Route fallback intercepts to NeutralBlockActivity
                val intent = Intent(context, NeutralBlockActivity::class.java).apply {
                    putExtra(NeutralBlockActivity.EXTRA_BLOCKED_PACKAGE, pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
            }
        }
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        var accessibilityEnabled = 0
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.message)
        }
        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (services != null && services.contains(context.packageName + "/" + IronMindAccessibilityService::class.java.name)) {
                return true
            }
        }
        return false
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
