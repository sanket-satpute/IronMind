package com.sanket_satpute_20.ironmind.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanket_satpute_20.ironmind.challenge.ChallengeAlarmScheduler
import com.sanket_satpute_20.ironmind.challenge.ChallengeAlarmReceiver
import com.sanket_satpute_20.ironmind.challenge.ChallengeManager
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.detox.DetoxAlarmScheduler
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionAlarmReceiver
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionScheduler
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchActivity
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchManager
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchStage
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockAlarmScheduler
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockAlarmReceiver
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockManager
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockPolicyManager
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockSoundController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val dao = IronMindDatabase.getDatabase(context).taskDao()
            val prefs = PrefManager.getInstance(context)

            CoroutineScope(Dispatchers.IO).launch {
                // Restore today's task reminders and launch windows.
                val today = LocalDate.now().toString()
                val tasks = dao.getTasksForDateOnce(today)
                tasks.forEach { task ->
                    AlarmScheduler.scheduleTaskAlarms(context, task)
                }

                // Restore the nightly reconciliation alarm.
                AlarmScheduler.scheduleBedtimeReaper(context)

                // Restore the 5 AM Club wakeup alarm only if the challenge is still active.
                if (prefs.challengeActive) {
                    ChallengeAlarmScheduler.scheduleDailyAlarm(context)
                    val challengeStatus = ChallengeManager.reconcileChallengeState(context)
                    if (
                        challengeStatus.isActive &&
                        prefs.challengeAlarmActive &&
                        prefs.challengeAlarmDate == LocalDate.now().toString() &&
                        !ChallengeManager.hasCompletedToday(context)
                    ) {
                        ChallengeAlarmReceiver.showActiveAlarmSurface(context)
                    }
                } else {
                    prefs.challengeAlarmActive = false
                    prefs.challengeAlarmDate = ""
                }

                // Restore detox blackout alarms only if the user enabled detox mode.
                if (prefs.detoxEnabled) {
                    DetoxAlarmScheduler.schedule(
                        context = context,
                        startHour = prefs.detoxStartHour,
                        endHour = prefs.detoxEndHour
                    )
                }

                if (prefs.sleepLockEnabled) {
                    SleepLockAlarmScheduler.schedule(context)
                    val sleepLockManager = SleepLockManager(context)
                    val policyManager = SleepLockPolicyManager(context)
                    if (sleepLockManager.syncPersistentState()) {
                        policyManager.activateNightSilence()
                        if (prefs.sleepLockSoundEnabled) {
                            SleepLockSoundController(context).play()
                        }
                        SleepLockAlarmReceiver.showActiveSurface(context)
                    } else {
                        SleepLockSoundController(context).stop()
                        policyManager.restoreNotificationPolicy()
                    }
                }

                if (prefs.nightDecisionEnabled) {
                    val nightDecisionManager = NightDecisionManager(context)
                    nightDecisionManager.reconcileRuntimeState()
                    NightDecisionScheduler.schedule(context)
                    if (prefs.nightDecisionActive) {
                        NightDecisionAlarmReceiver.showActiveSurface(context)
                    }
                } else {
                    prefs.clearNightDecision()
                }

                if (prefs.morningLaunchEnabled) {
                    val morningLaunchManager = MorningLaunchManager(context)
                    when (morningLaunchManager.reconcileRuntimeState()) {
                        MorningLaunchStage.SNOOZED -> {
                            val triggerAt = prefs.morningLaunchSnoozedUntil
                            if (triggerAt > System.currentTimeMillis()) {
                                MorningLaunchActivity.scheduleSnoozeReminder(context, triggerAt)
                            } else if (prefs.morningLaunchSessionId > 0L) {
                                context.startActivity(MorningLaunchActivity.createIntent(context))
                            }
                        }
                        MorningLaunchStage.PROMPTING,
                        MorningLaunchStage.IN_PROGRESS -> {
                            if (prefs.morningLaunchSessionId > 0L) {
                                context.startActivity(MorningLaunchActivity.createIntent(context))
                            }
                        }
                        else -> Unit
                    }
                } else {
                    MorningLaunchActivity.cancelSnoozeReminder(context)
                }
            }
        }
    }
}
