package com.sanket_satpute_20.ironmind.focus

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.sanket_satpute_20.ironmind.MainActivity

class SpringDedicatedDeviceManager(private val context: Context) {

    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    private val admin = ComponentName(context, SpringDeviceAdminReceiver::class.java)

    fun canControlDedicatedMode(): Boolean =
        SpringLockTaskController.isDeviceOwner(context) && dpm != null

    fun enableFullPrisonMode(): Boolean {
        if (!canControlDedicatedMode()) return false
        return runCatching {
            dpm!!.setLockTaskPackages(admin, arrayOf(context.packageName))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setStatusBarDisabled(admin, true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setKeyguardDisabled(admin, true)
            }
            dpm.addPersistentPreferredActivity(admin, buildHomeFilter(), ComponentName(context, MainActivity::class.java))
            true
        }.getOrDefault(false)
    }

    fun disableFullPrisonMode(): Boolean {
        if (!canControlDedicatedMode()) return false
        return runCatching {
            dpm!!.clearPackagePersistentPreferredActivities(admin, context.packageName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setStatusBarDisabled(admin, false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setKeyguardDisabled(admin, false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(
                    admin,
                    DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
                        DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW or
                        DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS or
                        DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
                )
            }
            true
        }.getOrDefault(false)
    }

    private fun buildHomeFilter(): IntentFilter =
        IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
}
