package com.sanket_satpute_20.ironmind.protection

import android.content.Context
import com.sanket_satpute_20.ironmind.core.logging.IronMindLogger
import com.sanket_satpute_20.ironmind.utils.PermissionHelper

interface PermissionChecker {
    fun isAccessibilityEnabled(context: Context): Boolean
    fun canDrawOverlays(context: Context): Boolean
    fun hasNotificationPermission(context: Context): Boolean
    fun hasUsageStatsPermission(context: Context): Boolean
}

class DefaultPermissionChecker : PermissionChecker {
    override fun isAccessibilityEnabled(context: Context) = PermissionHelper.isAccessibilityEnabled(context)
    override fun canDrawOverlays(context: Context) = PermissionHelper.canDrawOverlays(context)
    override fun hasNotificationPermission(context: Context) = PermissionHelper.hasNotificationPermission(context)
    override fun hasUsageStatsPermission(context: Context) = PermissionHelper.hasUsageStatsPermission(context)
}

class ProtectionReadinessService(
    private val context: Context,
    private val permissionChecker: PermissionChecker = DefaultPermissionChecker()
) {

    fun evaluate(): ProtectionReadiness {
        IronMindLogger.log("ProtectionReadiness", "CHECK", mapOf("action" to "evaluating_capabilities"))

        val capabilities = mutableMapOf<ProtectionCapability, CapabilityStatus>()
        val missingRequired = mutableSetOf<ProtectionCapability>()

        // 1. Accessibility Service (REQUIRED)
        val accessibilityEnabled = permissionChecker.isAccessibilityEnabled(context)
        capabilities[ProtectionCapability.ACCESSIBILITY_SERVICE] = CapabilityStatus(
            required = true,
            enabled = accessibilityEnabled,
            reason = "Required to enforce runtime policy and draw overlays."
        )
        if (!accessibilityEnabled) missingRequired.add(ProtectionCapability.ACCESSIBILITY_SERVICE)

        // 2. System Alert Window / Overlay (REQUIRED)
        val overlayEnabled = permissionChecker.canDrawOverlays(context)
        capabilities[ProtectionCapability.SYSTEM_ALERT_WINDOW] = CapabilityStatus(
            required = true,
            enabled = overlayEnabled,
            reason = "Required to display the blocking screen over forbidden apps."
        )
        if (!overlayEnabled) missingRequired.add(ProtectionCapability.SYSTEM_ALERT_WINDOW)

        // 3. Post Notifications (OPTIONAL but highly recommended for FocusSessionService)
        // If they deny it, the Foreground Service will still keep the process alive but without a visible notification.
        // Actually, on Android 13+, starting a foreground service might require the permission or it crashes?
        // No, it just doesn't show the notification, but it's legally allowed to start the foreground service.
        val notificationsEnabled = permissionChecker.hasNotificationPermission(context)
        capabilities[ProtectionCapability.POST_NOTIFICATIONS] = CapabilityStatus(
            required = false,
            enabled = notificationsEnabled,
            reason = "Optional. Enhances FocusSession resilience and visibility."
        )

        // 4. Package Usage Stats (OPTIONAL)
        val usageStatsEnabled = permissionChecker.hasUsageStatsPermission(context)
        capabilities[ProtectionCapability.PACKAGE_USAGE_STATS] = CapabilityStatus(
            required = false,
            enabled = usageStatsEnabled,
            reason = "Optional. Used for legacy detection mechanisms."
        )

        // Log the state of all capabilities
        capabilities.forEach { (cap, status) ->
            IronMindLogger.log("ProtectionReadiness", "CAPABILITY_STATE", mapOf(
                "capability" to cap.name,
                "required" to status.required,
                "enabled" to status.enabled,
                "reason" to status.reason
            ))
        }

        return if (missingRequired.isEmpty()) {
            IronMindLogger.log("ProtectionReadiness", "READY")
            ProtectionReadiness.Ready(capabilities)
        } else {
            IronMindLogger.e("ProtectionReadiness", "NOT_READY", mapOf("missing" to missingRequired.map { it.name }))
            ProtectionReadiness.NotReady(missingRequired, capabilities)
        }
    }
}
