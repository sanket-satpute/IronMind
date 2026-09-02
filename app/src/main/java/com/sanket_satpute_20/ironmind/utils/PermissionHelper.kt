package com.sanket_satpute_20.ironmind.utils

import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.app.usage.UsageStatsManager
import android.app.NotificationManager
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.PowerManager
import android.os.Process
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

enum class PermissionPriority {
    CRITICAL,
    IMPORTANT,
    SUPPORTIVE
}

enum class PermissionRequirementType {
    ACCESSIBILITY,
    EXACT_ALARMS,
    DO_NOT_DISTURB,
    USAGE_ACCESS,
    BATTERY_OPTIMIZATION,
    OVERLAYS
}

data class PermissionRequirement(
    val type: PermissionRequirementType,
    val title: String,
    val shortStatus: String,
    val description: String,
    val actionLabel: String,
    val isGranted: Boolean,
    val priority: PermissionPriority
)

object PermissionHelper {

    fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }

    fun openAccessibilitySettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun hasDNDAccess(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun openDNDSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasHealthConnectPermissions(context: Context): Boolean {
        // Simple stub for now. Health Connect uses specific read/write permissions.
        // We assume false if not integrated yet.
        return false 
    }

    fun getCoreProtectionRequirements(context: Context): List<PermissionRequirement> {
        return listOf(
            PermissionRequirement(
                type = PermissionRequirementType.ACCESSIBILITY,
                title = "Accessibility",
                shortStatus = "Accessibility is off",
                description = "App blocking cannot work until the IronMind accessibility service is enabled.",
                actionLabel = "ENABLE",
                isGranted = isAccessibilityEnabled(context),
                priority = PermissionPriority.CRITICAL
            ),
            PermissionRequirement(
                type = PermissionRequirementType.EXACT_ALARMS,
                title = "Exact Alarms",
                shortStatus = "Exact alarms need attention",
                description = "5 AM Club and timed mission alarms may fail without exact alarm access.",
                actionLabel = "ALLOW",
                isGranted = canScheduleExactAlarms(context),
                priority = PermissionPriority.IMPORTANT
            ),
            PermissionRequirement(
                type = PermissionRequirementType.DO_NOT_DISTURB,
                title = "Do Not Disturb",
                shortStatus = "Do Not Disturb access is missing",
                description = "Silent punishment and cleaner interruption control work better with DND access.",
                actionLabel = "ALLOW",
                isGranted = hasDNDAccess(context),
                priority = PermissionPriority.SUPPORTIVE
            )
        )
    }

    fun getPermissionCenterRequirements(context: Context): List<PermissionRequirement> {
        return getCoreProtectionRequirements(context) + listOf(
            PermissionRequirement(
                type = PermissionRequirementType.USAGE_ACCESS,
                title = "Usage Access",
                shortStatus = "Usage access is off",
                description = "Autopsy, distraction analysis, and app-based review flows depend on usage access to read your app activity.",
                actionLabel = "ENABLE",
                isGranted = hasUsageStatsPermission(context),
                priority = PermissionPriority.IMPORTANT
            ),
            PermissionRequirement(
                type = PermissionRequirementType.BATTERY_OPTIMIZATION,
                title = "Battery Optimization",
                shortStatus = "Battery optimization is still active",
                description = "Allowing IronMind to ignore battery optimization improves alarm, challenge, and background reliability on aggressive Android devices.",
                actionLabel = "ALLOW",
                isGranted = isIgnoringBatteryOptimizations(context),
                priority = PermissionPriority.SUPPORTIVE
            ),
            PermissionRequirement(
                type = PermissionRequirementType.OVERLAYS,
                title = "Display Over Other Apps",
                shortStatus = "Overlay access is missing",
                description = "Required to enforce focus limits effectively, showing block screens over prohibited apps.",
                actionLabel = "ALLOW",
                isGranted = canDrawOverlays(context),
                priority = PermissionPriority.CRITICAL
            )
        )
    }

    fun openSettingsForRequirement(context: Context, type: PermissionRequirementType) {
        when (type) {
            PermissionRequirementType.ACCESSIBILITY -> openAccessibilitySettings(context)
            PermissionRequirementType.EXACT_ALARMS -> openExactAlarmSettings(context)
            PermissionRequirementType.DO_NOT_DISTURB -> openDNDSettings(context)
            PermissionRequirementType.USAGE_ACCESS -> openUsageStatsSettings(context)
            PermissionRequirementType.BATTERY_OPTIMIZATION -> requestIgnoreBatteryOptimizations(context)
            PermissionRequirementType.OVERLAYS -> requestOverlayPermission(context)
        }
    }
}
