package com.sanket_satpute_20.ironmind.protection

import android.content.Context
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.focus.WorkLockManager

/**
 * Builds the current runtime policy from persisted mission/protection state.
 *
 * This class does NOT enforce blocking. It only answers:
 * "What should the protection layer enforce right now?"
 */
class RuntimePolicyRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = PrefManager.getInstance(appContext)
    private val workLockManager = WorkLockManager(appContext)

    /** Builds the policy that should apply while [task] is being started/executed. */
    fun buildForMission(
        task: Task,
        allowedPackages: Set<String> = emptySet()
    ): RuntimePolicy {
        val workLockActive = workLockManager.isActive()

        val reason = when {
            prefs.sleepLockActive -> RuntimePolicyReason.SLEEP_LOCK
            prefs.morningLaunchActive -> RuntimePolicyReason.MORNING_LAUNCH
            workLockActive -> RuntimePolicyReason.WORK_LOCK
            prefs.crucibleActive -> RuntimePolicyReason.CRUCIBLE
            prefs.detoxCurrentlyActive -> RuntimePolicyReason.DETOX
            else -> RuntimePolicyReason.MISSION
        }

        return RuntimePolicy(
            enabled = true,
            activeMissionId = task.id,
            activeMissionName = task.name,
            allowedPackages = allowedPackages,
            blockedPackages = blockedPackagesSnapshot(),
            workLockActive = workLockActive,
            pomodoroActive = prefs.pomodoroActive,
            sleepLockActive = prefs.sleepLockActive,
            morningLaunchActive = prefs.morningLaunchActive,
            reason = reason
        )
    }

    /** Reconstructs the current policy from persisted state, e.g. after process restart. */
    fun currentPolicy(): RuntimePolicy {
        val workLockActive = workLockManager.isActive()

        // PrefManager has no standalone "active task id"; derive it from whichever
        // protection subsystem is currently holding a task reference.
        val activeMissionId = when {
            workLockActive && prefs.workLockTaskId > 0 -> prefs.workLockTaskId
            prefs.pomodoroActive && prefs.pomodoroTaskId > 0 -> prefs.pomodoroTaskId
            else -> null
        }

        val activeMissionName = when {
            workLockActive && prefs.workLockTaskName.isNotBlank() -> prefs.workLockTaskName
            prefs.activeTaskName.isNotBlank() -> prefs.activeTaskName
            else -> null
        }

        val reason = when {
            prefs.sleepLockActive -> RuntimePolicyReason.SLEEP_LOCK
            prefs.morningLaunchActive -> RuntimePolicyReason.MORNING_LAUNCH
            workLockActive -> RuntimePolicyReason.WORK_LOCK
            prefs.crucibleActive -> RuntimePolicyReason.CRUCIBLE
            prefs.detoxCurrentlyActive -> RuntimePolicyReason.DETOX
            activeMissionId != null -> RuntimePolicyReason.MISSION
            else -> RuntimePolicyReason.NONE
        }

        val enabled = activeMissionId != null ||
            workLockActive ||
            prefs.sleepLockActive ||
            prefs.morningLaunchActive ||
            prefs.crucibleActive ||
            prefs.detoxCurrentlyActive

        return RuntimePolicy(
            enabled = enabled,
            activeMissionId = activeMissionId,
            activeMissionName = activeMissionName,
            allowedPackages = prefs.activeMissionContextApps,
            blockedPackages = blockedPackagesSnapshot(),
            workLockActive = workLockActive,
            pomodoroActive = prefs.pomodoroActive,
            sleepLockActive = prefs.sleepLockActive,
            morningLaunchActive = prefs.morningLaunchActive,
            reason = reason
        )
    }

    fun clear(): RuntimePolicy = RuntimePolicy()

    private fun blockedPackagesSnapshot(): Set<String> {
        return prefs.blockedApps.filter { it.isNotBlank() }.toSet()
    }
}
