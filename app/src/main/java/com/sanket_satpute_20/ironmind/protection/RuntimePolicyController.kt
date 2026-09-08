package com.sanket_satpute_20.ironmind.protection

import android.content.Context
import android.content.Intent
import com.sanket_satpute_20.ironmind.data.Task

/**
 * Single entry point for changing runtime protection.
 *
 * The AccessibilityService remains the enforcement engine; it already reloads its
 * cache from PrefManager whenever [ACTION_RELOAD_GUARD] is broadcast. This controller
 * only publishes that the desired policy changed - it does not enforce blocking itself.
 */
class RuntimePolicyController(context: Context) {

    private val appContext = context.applicationContext
    private val repository = RuntimePolicyRepository(appContext)

    fun activateForMission(
        task: Task,
        allowedPackages: Set<String>
    ): RuntimePolicy {
        val policy = repository.buildForMission(task = task, allowedPackages = allowedPackages)
        publishPolicyChanged()
        return policy
    }

    fun refresh(): RuntimePolicy {
        val policy = repository.currentPolicy()
        publishPolicyChanged()
        return policy
    }

    fun clear(): RuntimePolicy {
        val policy = repository.clear()
        publishPolicyChanged()
        return policy
    }

    private fun publishPolicyChanged() {
        appContext.sendBroadcast(
            Intent(ACTION_RUNTIME_POLICY_CHANGED).apply { setPackage(appContext.packageName) }
        )
        // Existing enforcement layer reloads its cache on this action today.
        appContext.sendBroadcast(
            Intent(ACTION_RELOAD_GUARD).apply { setPackage(appContext.packageName) }
        )
    }

    companion object {
        const val ACTION_RUNTIME_POLICY_CHANGED =
            "com.sanket_satpute_20.ironmind.RUNTIME_POLICY_CHANGED"

        const val ACTION_RELOAD_GUARD = "com.ironmind.RELOAD_GUARD"
    }
}
