package com.sanket_satpute_20.ironmind.focus

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

object SpringLockTaskController {

    private fun adminComponent(context: Context): ComponentName =
        ComponentName(context, SpringDeviceAdminReceiver::class.java)

    fun canAttemptLockTask(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    fun isDeviceOwner(context: Context): Boolean {
        if (!canAttemptLockTask()) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return runCatching { dpm.isDeviceOwnerApp(context.packageName) }.getOrDefault(false)
    }

    fun isFullLockTaskPermitted(context: Context): Boolean {
        if (!canAttemptLockTask()) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return runCatching { dpm.isLockTaskPermitted(context.packageName) }.getOrDefault(false)
    }

    fun isInRealLockTaskMode(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_LOCKED
        } else {
            @Suppress("DEPRECATION")
            activityManager.isInLockTaskMode
        }
    }

    fun tryEnterLockTask(activity: Activity): Boolean {
        if (!canAttemptLockTask()) return false
        ensurePackageAllowlisted(activity)
        return runCatching {
            activity.startLockTask()
            true
        }.getOrDefault(false)
    }

    fun tryExitLockTask(activity: Activity) {
        if (!canAttemptLockTask()) return
        runCatching { activity.stopLockTask() }
    }

    fun ensurePackageAllowlisted(context: Context): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val admin = adminComponent(context)
        return runCatching {
            dpm.setLockTaskPackages(admin, arrayOf(context.packageName))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(
                    admin,
                    DevicePolicyManager.LOCK_TASK_FEATURE_NONE
                )
            }
            true
        }.getOrDefault(false)
    }
}
