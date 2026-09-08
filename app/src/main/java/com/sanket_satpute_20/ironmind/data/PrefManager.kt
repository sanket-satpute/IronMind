package com.sanket_satpute_20.ironmind.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class PrefManager internal constructor(private val prefs: SharedPreferences) {

    var languageSelected: Boolean
        get() = prefs.getBoolean(KEY_LANGUAGE_SELECTED, false)
        set(value) = prefs.edit { putBoolean(KEY_LANGUAGE_SELECTED, value) }

    var diagnosticDone: Boolean
        get() = prefs.getBoolean(KEY_DIAGNOSTIC_DONE, false)
        set(value) = prefs.edit { putBoolean(KEY_DIAGNOSTIC_DONE, value) }

    var introSeen: Boolean
        get() = prefs.getBoolean(KEY_INTRO_SEEN, false)
        set(value) = prefs.edit { putBoolean(KEY_INTRO_SEEN, value) }

    var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETE, value) }

    var hasUnlockedSmartContext: Boolean
        get() = prefs.getBoolean("has_unlocked_smart_context", false)
        set(value) = prefs.edit { putBoolean("has_unlocked_smart_context", value) }

    var firstHomeArrivalDate: String
        get() = prefs.getString(KEY_FIRST_HOME_ARRIVAL_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_FIRST_HOME_ARRIVAL_DATE, value) }

    var contractSigned: Boolean
        get() = prefs.getBoolean(KEY_CONTRACT_SIGNED, false)
        set(value) = prefs.edit { putBoolean(KEY_CONTRACT_SIGNED, value) }

    var identitySetupComplete: Boolean
        get() = prefs.getBoolean(KEY_IDENTITY_SETUP_COMPLETE, false) || identityStatements.isNotEmpty()
        set(value) = prefs.edit { putBoolean(KEY_IDENTITY_SETUP_COMPLETE, value) }

    var commitmentContractComplete: Boolean
        get() = prefs.getBoolean(KEY_COMMITMENT_CONTRACT_COMPLETE, false) || contractSigned
        set(value) = prefs.edit { putBoolean(KEY_COMMITMENT_CONTRACT_COMPLETE, value) }

    var distractionsSetupComplete: Boolean
        get() = prefs.getBoolean(KEY_DISTRACTIONS_SETUP_COMPLETE, false) || blockedApps.isNotEmpty() || websiteBlockingEnabled
        set(value) = prefs.edit { putBoolean(KEY_DISTRACTIONS_SETUP_COMPLETE, value) }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_USER_NAME, value) }

    var appLanguage: String
        get() = prefs.getString(KEY_APP_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit { putString(KEY_APP_LANGUAGE, value) }

    var hasExperiencedFirstBlock: Boolean
        get() = prefs.getBoolean("has_experienced_first_block", false)
        set(value) = prefs.edit { putBoolean("has_experienced_first_block", value) }

    var userType: String
        get() = prefs.getString(KEY_USER_TYPE, "BROKEN_STRIVER") ?: "BROKEN_STRIVER"
        set(value) = prefs.edit { putString(KEY_USER_TYPE, value) }

    var secondaryType: String
        get() = prefs.getString(KEY_SECONDARY_TYPE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_SECONDARY_TYPE, value) }

    var appMode: String
        get() = prefs.getString(KEY_APP_MODE, "BUILD") ?: "BUILD"
        set(value) = prefs.edit { putString(KEY_APP_MODE, value) }

    var streakCount: Int
        get() = prefs.getInt(KEY_STREAK_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_STREAK_COUNT, value) }

    var frictionLevelOverride: Int
        get() = prefs.getInt("friction_level_override", -1) // -1 means no override
        set(value) = prefs.edit { putInt("friction_level_override", value) }

    var streakShields: Int
        get() = prefs.getInt("streak_shields", 0)
        set(value) = prefs.edit { putInt("streak_shields", value.coerceIn(0, 2)) }

    var isStreakInCriticalState: Boolean
        get() = prefs.getBoolean("is_streak_in_critical_state", false)
        set(value) = prefs.edit { putBoolean("is_streak_in_critical_state", value) }

    var streakCriticalTimestamp: Long
        get() = prefs.getLong("streak_critical_timestamp", 0L)
        set(value) = prefs.edit { putLong("streak_critical_timestamp", value) }

    var lastRecoveryDayMonth: String
        get() = prefs.getString("last_recovery_day_month", "") ?: ""
        set(value) = prefs.edit { putString("last_recovery_day_month", value) }

    var recoveryDaysBudget: Int
        get() {
            val currentMonth = java.time.LocalDate.now().monthValue.toString() + "-" + java.time.LocalDate.now().year.toString()
            if (lastRecoveryDayMonth != currentMonth) {
                // Reset budget to 2 for a new month
                prefs.edit { 
                    putInt("recovery_days_budget", 2)
                    putString("last_recovery_day_month", currentMonth)
                }
                return 2
            }
            return prefs.getInt("recovery_days_budget", 2)
        }
        set(value) = prefs.edit { putInt("recovery_days_budget", value) }

    var isRecoveryDayActive: Boolean
        get() = prefs.getBoolean("is_recovery_day_active", false)
        set(value) = prefs.edit { putBoolean("is_recovery_day_active", value) }

    var recoveryDayTimestamp: Long
        get() = prefs.getLong("recovery_day_timestamp", 0L)
        set(value) = prefs.edit { putLong("recovery_day_timestamp", value) }

    var failureLatestJson: String
        get() = prefs.getString("failure_latest_json", "") ?: ""
        set(value) = prefs.edit { putString("failure_latest_json", value) }

    var failureHistoryJson: String
        get() = prefs.getString("failure_history_json", "") ?: ""
        set(value) = prefs.edit { putString("failure_history_json", value) }

    var recoveryCurrentJson: String
        get() = prefs.getString("recovery_current_json", "") ?: ""
        set(value) = prefs.edit { putString("recovery_current_json", value) }


    var lastCompleteDate: String
        get() = prefs.getString(KEY_LAST_COMPLETE_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_COMPLETE_DATE, value) }

    var totalCompleted: Int
        get() = prefs.getInt(KEY_TOTAL_COMPLETED, 0)
        set(value) = prefs.edit { putInt(KEY_TOTAL_COMPLETED, value) }

    var totalSkipped: Int
        get() = prefs.getInt(KEY_TOTAL_SKIPPED, 0)
        set(value) = prefs.edit { putInt(KEY_TOTAL_SKIPPED, value) }

    var contractGoal: String
        get() = prefs.getString(KEY_CONTRACT_GOAL, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CONTRACT_GOAL, value) }

    var contractReason: String
        get() = prefs.getString(KEY_CONTRACT_REASON, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CONTRACT_REASON, value) }

    var contractSignature: String
        get() = prefs.getString(KEY_CONTRACT_SIGNATURE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CONTRACT_SIGNATURE, value) }

    var partnerName: String
        get() = prefs.getString(KEY_PARTNER_NAME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PARTNER_NAME, value) }

    var partnerPhone: String
        get() = prefs.getString(KEY_PARTNER_PHONE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PARTNER_PHONE, value) }

    var cloudBackupEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLOUD_BACKUP_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_CLOUD_BACKUP_ENABLED, value) }

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_TIMESTAMP, value) }

    var firebaseUid: String
        get() = prefs.getString(KEY_FIREBASE_UID, "") ?: ""
        set(value) = prefs.edit { putString(KEY_FIREBASE_UID, value) }

    var isPremium: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_PREMIUM, value) }

    var lastRelapseTimestamp: Long
        get() = prefs.getLong(KEY_LAST_RELAPSE_TIMESTAMP, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_RELAPSE_TIMESTAMP, value) }

    var blockedApps: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_BLOCKED_APPS, HashSet(value)) }

    var emergencyApps: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_EMERGENCY_APPS, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_EMERGENCY_APPS, HashSet(value)) }

    var identityStatements: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_IDENTITY_STATEMENTS, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_IDENTITY_STATEMENTS, HashSet(value)) }

    var detoxEnabled: Boolean
        get() = prefs.getBoolean(KEY_DETOX_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_DETOX_ENABLED, value) }

    var detoxStartHour: Int
        get() = prefs.getInt(KEY_DETOX_START_HOUR, 22)
        set(value) = prefs.edit { putInt(KEY_DETOX_START_HOUR, value) }

    var detoxEndHour: Int
        get() = prefs.getInt(KEY_DETOX_END_HOUR, 6)
        set(value) = prefs.edit { putInt(KEY_DETOX_END_HOUR, value) }

    var detoxCurrentlyActive: Boolean
        get() = prefs.getBoolean(KEY_DETOX_CURRENTLY_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_DETOX_CURRENTLY_ACTIVE, value) }

    var lastCheckinDate: String
        get() = prefs.getString(KEY_LAST_CHECKIN_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_CHECKIN_DATE, value) }

    var lastWeeklyHonestScoreWeek: String
        get() = prefs.getString(KEY_LAST_WEEKLY_HONEST_SCORE_WEEK, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_WEEKLY_HONEST_SCORE_WEEK, value) }

    var lastWeeklyHonestScore: Int
        get() = prefs.getInt(KEY_LAST_WEEKLY_HONEST_SCORE, 0)
        set(value) = prefs.edit { putInt(KEY_LAST_WEEKLY_HONEST_SCORE, value) }

    var weeklyHonestScoreHistory: String
        get() = prefs.getString(KEY_WEEKLY_HONEST_SCORE_HISTORY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_WEEKLY_HONEST_SCORE_HISTORY, value) }

    var intensity: String
        get() = prefs.getString(KEY_INTENSITY, "MEDIUM") ?: "MEDIUM"
        set(value) = prefs.edit { putString(KEY_INTENSITY, value) }

    var websiteBlockingEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEBSITE_BLOCKING_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_WEBSITE_BLOCKING_ENABLED, value) }

    var blockedWebsites: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_BLOCKED_WEBSITES, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_BLOCKED_WEBSITES, HashSet(value)) }

    var activeTaskName: String
        get() = prefs.getString(KEY_ACTIVE_TASK_NAME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_ACTIVE_TASK_NAME, value) }

    var activeTaskStartTime: String
        get() = prefs.getString(KEY_ACTIVE_TASK_START, "") ?: ""
        set(value) = prefs.edit { putString(KEY_ACTIVE_TASK_START, value) }

    var activeTaskEndTime: String
        get() = prefs.getString(KEY_ACTIVE_TASK_END, "") ?: ""
        set(value) = prefs.edit { putString(KEY_ACTIVE_TASK_END, value) }

    var activeTaskDate: String
        get() = prefs.getString(KEY_ACTIVE_TASK_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_ACTIVE_TASK_DATE, value) }

    var activeMissionContextApps: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_ACTIVE_MISSION_CONTEXT_APPS, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_ACTIVE_MISSION_CONTEXT_APPS, HashSet(value)) }

    var earnedUnlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_EARNED_UNLOCK_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_EARNED_UNLOCK_ENABLED, value) }

    var earnedUnlockActive: Boolean
        get() = prefs.getBoolean(KEY_EARNED_UNLOCK_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_EARNED_UNLOCK_ACTIVE, value) }

    var earnedUnlockUnlockedToday: Boolean
        get() = prefs.getBoolean(KEY_EARNED_UNLOCK_UNLOCKED_TODAY, false)
        set(value) = prefs.edit { putBoolean(KEY_EARNED_UNLOCK_UNLOCKED_TODAY, value) }

    var earnedUnlockMode: String
        get() = prefs.getString(KEY_EARNED_UNLOCK_MODE, "DAY") ?: "DAY"
        set(value) = prefs.edit { putString(KEY_EARNED_UNLOCK_MODE, value) }

    var earnedUnlockDate: String
        get() = prefs.getString(KEY_EARNED_UNLOCK_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_EARNED_UNLOCK_DATE, value) }

    var earnedUnlockRemainingCount: Int
        get() = prefs.getInt(KEY_EARNED_UNLOCK_REMAINING_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_EARNED_UNLOCK_REMAINING_COUNT, value) }

    var earnedUnlockTotalCount: Int
        get() = prefs.getInt(KEY_EARNED_UNLOCK_TOTAL_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_EARNED_UNLOCK_TOTAL_COUNT, value) }

    var earnedUnlockCompletedCount: Int
        get() = prefs.getInt(KEY_EARNED_UNLOCK_COMPLETED_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_EARNED_UNLOCK_COMPLETED_COUNT, value) }

    var earnedUnlockSkippedCount: Int
        get() = prefs.getInt(KEY_EARNED_UNLOCK_SKIPPED_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_EARNED_UNLOCK_SKIPPED_COUNT, value) }

    var workLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_WORK_LOCK_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_WORK_LOCK_ENABLED, value) }

    var workLockActive: Boolean
        get() = prefs.getBoolean(KEY_WORK_LOCK_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_WORK_LOCK_ACTIVE, value) }

    var workLockStage: String
        get() = prefs.getString(KEY_WORK_LOCK_STAGE, "IDLE") ?: "IDLE"
        set(value) = prefs.edit { putString(KEY_WORK_LOCK_STAGE, value) }

    var workLockTaskId: Int
        get() = prefs.getInt(KEY_WORK_LOCK_TASK_ID, -1)
        set(value) = prefs.edit { putInt(KEY_WORK_LOCK_TASK_ID, value) }

    var workLockTaskName: String
        get() = prefs.getString(KEY_WORK_LOCK_TASK_NAME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_WORK_LOCK_TASK_NAME, value) }

    var workLockStartedAt: Long
        get() = prefs.getLong(KEY_WORK_LOCK_STARTED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_WORK_LOCK_STARTED_AT, value) }

    var workLockEndsAt: Long
        get() = prefs.getLong(KEY_WORK_LOCK_ENDS_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_WORK_LOCK_ENDS_AT, value) }

    var workLockPenaltyArmed: Boolean
        get() = prefs.getBoolean(KEY_WORK_LOCK_PENALTY_ARMED, false)
        set(value) = prefs.edit { putBoolean(KEY_WORK_LOCK_PENALTY_ARMED, value) }

    var workLockEmergencyExitCount: Int
        get() = prefs.getInt(KEY_WORK_LOCK_EMERGENCY_EXIT_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_WORK_LOCK_EMERGENCY_EXIT_COUNT, value) }

    var workLockEmergencyExitMonth: String
        get() = prefs.getString(KEY_WORK_LOCK_EMERGENCY_EXIT_MONTH, "") ?: ""
        set(value) = prefs.edit { putString(KEY_WORK_LOCK_EMERGENCY_EXIT_MONTH, value) }

    var workLockBreakCountToday: Int
        get() = prefs.getInt(KEY_WORK_LOCK_BREAK_COUNT_TODAY, 0)
        set(value) = prefs.edit { putInt(KEY_WORK_LOCK_BREAK_COUNT_TODAY, value) }

    var pomodoroActive: Boolean
        get() = prefs.getBoolean(KEY_POMODORO_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_POMODORO_ACTIVE, value) }

    var pomodoroTaskId: Int
        get() = prefs.getInt(KEY_POMODORO_TASK_ID, -1)
        set(value) = prefs.edit { putInt(KEY_POMODORO_TASK_ID, value) }

    var pomodoroSessionId: String
        get() = prefs.getString(KEY_POMODORO_SESSION_ID, "") ?: ""
        set(value) = prefs.edit { putString(KEY_POMODORO_SESSION_ID, value) }

    var pomodoroSource: String
        get() = prefs.getString(KEY_POMODORO_SOURCE, "TASK") ?: "TASK"
        set(value) = prefs.edit { putString(KEY_POMODORO_SOURCE, value) }

    var pomodoroTitle: String
        get() = prefs.getString(KEY_POMODORO_TITLE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_POMODORO_TITLE, value) }

    var pomodoroDate: String
        get() = prefs.getString(KEY_POMODORO_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_POMODORO_DATE, value) }

    var pomodoroStartTime: String
        get() = prefs.getString(KEY_POMODORO_START_TIME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_POMODORO_START_TIME, value) }

    var pomodoroEndTime: String
        get() = prefs.getString(KEY_POMODORO_END_TIME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_POMODORO_END_TIME, value) }

    var pomodoroTotalDurationMinutes: Int
        get() = prefs.getInt(KEY_POMODORO_TOTAL_DURATION_MINUTES, 0)
        set(value) = prefs.edit { putInt(KEY_POMODORO_TOTAL_DURATION_MINUTES, value) }

    var pomodoroPreset: String
        get() = prefs.getString(KEY_POMODORO_PRESET, "CLASSIC_25_5") ?: "CLASSIC_25_5"
        set(value) = prefs.edit { putString(KEY_POMODORO_PRESET, value) }

    var pomodoroPhase: String
        get() = prefs.getString(KEY_POMODORO_PHASE, "IDLE") ?: "IDLE"
        set(value) = prefs.edit { putString(KEY_POMODORO_PHASE, value) }

    var pomodoroPhaseEndsAt: Long
        get() = prefs.getLong(KEY_POMODORO_PHASE_ENDS_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_POMODORO_PHASE_ENDS_AT, value) }

    var pomodoroIntervalIndex: Int
        get() = prefs.getInt(KEY_POMODORO_INTERVAL_INDEX, 0)
        set(value) = prefs.edit { putInt(KEY_POMODORO_INTERVAL_INDEX, value) }

    var pomodoroCompletedWorkIntervals: Int
        get() = prefs.getInt(KEY_POMODORO_COMPLETED_WORK_INTERVALS, 0)
        set(value) = prefs.edit { putInt(KEY_POMODORO_COMPLETED_WORK_INTERVALS, value) }

    var pomodoroBreachCount: Int
        get() = prefs.getInt(KEY_POMODORO_BREACH_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_POMODORO_BREACH_COUNT, value) }

    var pomodoroBreakUnlocked: Boolean
        get() = prefs.getBoolean(KEY_POMODORO_BREAK_UNLOCKED, false)
        set(value) = prefs.edit { putBoolean(KEY_POMODORO_BREAK_UNLOCKED, value) }

    var pomodoroStartedAt: Long
        get() = prefs.getLong(KEY_POMODORO_STARTED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_POMODORO_STARTED_AT, value) }

    var pomodoroSessionHardEndsAt: Long
        get() = prefs.getLong(KEY_POMODORO_SESSION_HARD_ENDS_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_POMODORO_SESSION_HARD_ENDS_AT, value) }

    var totalXp: Long
        get() = prefs.getLong(KEY_TOTAL_XP, 0L)
        set(value) = prefs.edit { putLong(KEY_TOTAL_XP, value) }

    var currentIdentityLevel: Int
        get() = prefs.getInt(KEY_CURRENT_IDENTITY_LEVEL, 1)
        set(value) = prefs.edit { putInt(KEY_CURRENT_IDENTITY_LEVEL, value) }

    var bedTime: String
        get() = prefs.getString(KEY_BEDTIME, "22:00") ?: "22:00"
        set(value) = prefs.edit { putString(KEY_BEDTIME, value) }

    var sleepLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_ENABLED, value) }

    var sleepLockBedHour: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_BED_HOUR, 22)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_BED_HOUR, value) }

    var sleepLockBedMinute: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_BED_MINUTE, 30)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_BED_MINUTE, value) }

    var sleepLockWakeHour: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_WAKE_HOUR, 6)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_WAKE_HOUR, value) }

    var sleepLockWakeMinute: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_WAKE_MINUTE, 30)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_WAKE_MINUTE, value) }

    var sleepLockStoredWakeHourBeforeChallenge: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_STORED_WAKE_HOUR_BEFORE_CHALLENGE, -1)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_STORED_WAKE_HOUR_BEFORE_CHALLENGE, value) }

    var sleepLockStoredWakeMinuteBeforeChallenge: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_STORED_WAKE_MINUTE_BEFORE_CHALLENGE, -1)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_STORED_WAKE_MINUTE_BEFORE_CHALLENGE, value) }

    var sleepLockWarning30Enabled: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_WARNING_30_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_WARNING_30_ENABLED, value) }

    var sleepLockWarning15Enabled: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_WARNING_15_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_WARNING_15_ENABLED, value) }

    var sleepLockWarningFinalEnabled: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_WARNING_FINAL_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_WARNING_FINAL_ENABLED, value) }

    var sleepLockEmergencyApps: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_SLEEP_LOCK_EMERGENCY_APPS, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_SLEEP_LOCK_EMERGENCY_APPS, HashSet(value)) }

    var sleepLockSilenceNotifications: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_SILENCE_NOTIFICATIONS, true)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_SILENCE_NOTIFICATIONS, value) }

    var sleepLockSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_SOUND_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_SOUND_ENABLED, value) }

    var sleepLockSelectedSound: String
        get() = prefs.getString(KEY_SLEEP_LOCK_SELECTED_SOUND, "rain") ?: "rain"
        set(value) = prefs.edit { putString(KEY_SLEEP_LOCK_SELECTED_SOUND, value) }

    var sleepLockSelectedSounds: Set<String>
        get() = HashSet(
            prefs.getStringSet(
                KEY_SLEEP_LOCK_SELECTED_SOUNDS,
                setOf(sleepLockSelectedSound)
            ) ?: setOf(sleepLockSelectedSound)
        )
        set(value) = prefs.edit { putStringSet(KEY_SLEEP_LOCK_SELECTED_SOUNDS, HashSet(value)) }

    var sleepLockSoundMode: String
        get() = prefs.getString(KEY_SLEEP_LOCK_SOUND_MODE, "SINGLE") ?: "SINGLE"
        set(value) = prefs.edit { putString(KEY_SLEEP_LOCK_SOUND_MODE, value) }

    var sleepLockActive: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_ACTIVE, value) }

    var sleepLockStartedAt: Long
        get() = prefs.getLong(KEY_SLEEP_LOCK_STARTED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_SLEEP_LOCK_STARTED_AT, value) }

    var sleepLockEndsAt: Long
        get() = prefs.getLong(KEY_SLEEP_LOCK_ENDS_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_SLEEP_LOCK_ENDS_AT, value) }

    var sleepLockLastWarningStage: String
        get() = prefs.getString(KEY_SLEEP_LOCK_LAST_WARNING_STAGE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_SLEEP_LOCK_LAST_WARNING_STAGE, value) }

    var sleepLockDndPermissionAcknowledged: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_DND_PERMISSION_ACKNOWLEDGED, false)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_DND_PERMISSION_ACKNOWLEDGED, value) }

    var sleepLockBypassUsedAt: Long
        get() = prefs.getLong(KEY_SLEEP_LOCK_BYPASS_USED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_SLEEP_LOCK_BYPASS_USED_AT, value) }

    var sleepLockEmergencyOverrideUntil: Long
        get() = prefs.getLong(KEY_SLEEP_LOCK_EMERGENCY_OVERRIDE_UNTIL, 0L)
        set(value) = prefs.edit { putLong(KEY_SLEEP_LOCK_EMERGENCY_OVERRIDE_UNTIL, value) }

    var sleepLockEmergencyExitCount: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_EMERGENCY_EXIT_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_EMERGENCY_EXIT_COUNT, value) }

    var sleepLockEmergencyExitMonth: String
        get() = prefs.getString(KEY_SLEEP_LOCK_EMERGENCY_EXIT_MONTH, "") ?: ""
        set(value) = prefs.edit { putString(KEY_SLEEP_LOCK_EMERGENCY_EXIT_MONTH, value) }

    var sleepLockOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_ONBOARDING_COMPLETE, value) }

    var sleepLockPreviousInterruptionFilter: Int
        get() = prefs.getInt(KEY_SLEEP_LOCK_PREVIOUS_INTERRUPTION_FILTER, 1)
        set(value) = prefs.edit { putInt(KEY_SLEEP_LOCK_PREVIOUS_INTERRUPTION_FILTER, value) }

    var sleepLockSilenceActive: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_LOCK_SILENCE_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_SLEEP_LOCK_SILENCE_ACTIVE, value) }

    var morningRitualCurrentStep: String
        get() = prefs.getString(KEY_MORNING_RITUAL_CURRENT_STEP, "WAKE_ENTRY") ?: "WAKE_ENTRY"
        set(value) = prefs.edit { putString(KEY_MORNING_RITUAL_CURRENT_STEP, value) }

    // --- Night Ritual Persistence ---
    var nightRitualCurrentStep: String
        get() = prefs.getString("night_ritual_current_step", "SHIELD_RECAP") ?: "SHIELD_RECAP"
        set(value) = prefs.edit { putString("night_ritual_current_step", value) }

    var nightRitualDate: String
        get() = prefs.getString("night_ritual_date", "") ?: ""
        set(value) = prefs.edit { putString("night_ritual_date", value) }

    var nightRitualReflectionText: String
        get() = prefs.getString("night_ritual_reflection_text", "") ?: ""
        set(value) = prefs.edit { putString("night_ritual_reflection_text", value) }


    var morningLaunchEnabled: Boolean
        get() = prefs.getBoolean(KEY_MORNING_LAUNCH_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_MORNING_LAUNCH_ENABLED, value) }

    var morningLaunchActive: Boolean
        get() = prefs.getBoolean(KEY_MORNING_LAUNCH_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_MORNING_LAUNCH_ACTIVE, value) }

    var morningLaunchStage: String
        get() = prefs.getString(KEY_MORNING_LAUNCH_STAGE, "IDLE") ?: "IDLE"
        set(value) = prefs.edit { putString(KEY_MORNING_LAUNCH_STAGE, value) }

    var morningLaunchSessionId: Long
        get() = prefs.getLong(KEY_MORNING_LAUNCH_SESSION_ID, 0L)
        set(value) = prefs.edit { putLong(KEY_MORNING_LAUNCH_SESSION_ID, value) }

    var morningLaunchStartedAt: Long
        get() = prefs.getLong(KEY_MORNING_LAUNCH_STARTED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_MORNING_LAUNCH_STARTED_AT, value) }

    var morningLaunchAlarmDismissedAt: Long
        get() = prefs.getLong(KEY_MORNING_LAUNCH_ALARM_DISMISSED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_MORNING_LAUNCH_ALARM_DISMISSED_AT, value) }

    var morningLaunchSnoozedUntil: Long
        get() = prefs.getLong(KEY_MORNING_LAUNCH_SNOOZED_UNTIL, 0L)
        set(value) = prefs.edit { putLong(KEY_MORNING_LAUNCH_SNOOZED_UNTIL, value) }

    var morningLaunchMode: String
        get() = prefs.getString(KEY_MORNING_LAUNCH_MODE, "MONK") ?: "MONK"
        set(value) = prefs.edit { putString(KEY_MORNING_LAUNCH_MODE, value) }

    var morningLaunchCurrentStepIndex: Int
        get() = prefs.getInt(KEY_MORNING_LAUNCH_CURRENT_STEP_INDEX, 0)
        set(value) = prefs.edit { putInt(KEY_MORNING_LAUNCH_CURRENT_STEP_INDEX, value) }

    var morningLaunchCompletedToday: Boolean
        get() = prefs.getBoolean(KEY_MORNING_LAUNCH_COMPLETED_TODAY, false)
        set(value) = prefs.edit { putBoolean(KEY_MORNING_LAUNCH_COMPLETED_TODAY, value) }

    var morningLaunchSkippedToday: Boolean
        get() = prefs.getBoolean(KEY_MORNING_LAUNCH_SKIPPED_TODAY, false)
        set(value) = prefs.edit { putBoolean(KEY_MORNING_LAUNCH_SKIPPED_TODAY, value) }

    var morningLaunchDelayCount: Int
        get() = prefs.getInt(KEY_MORNING_LAUNCH_DELAY_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_MORNING_LAUNCH_DELAY_COUNT, value) }

    var morningLaunchLastOutcome: String
        get() = prefs.getString(KEY_MORNING_LAUNCH_LAST_OUTCOME, "PENDING") ?: "PENDING"
        set(value) = prefs.edit { putString(KEY_MORNING_LAUNCH_LAST_OUTCOME, value) }

    var morningLaunchDate: String
        get() = prefs.getString(KEY_MORNING_LAUNCH_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_MORNING_LAUNCH_DATE, value) }

    var morningLaunchEmergencyExitCount: Int
        get() = prefs.getInt(KEY_MORNING_LAUNCH_EMERGENCY_EXIT_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_MORNING_LAUNCH_EMERGENCY_EXIT_COUNT, value) }

    var morningLaunchEmergencyExitMonth: String
        get() = prefs.getString(KEY_MORNING_LAUNCH_EMERGENCY_EXIT_MONTH, "") ?: ""
        set(value) = prefs.edit { putString(KEY_MORNING_LAUNCH_EMERGENCY_EXIT_MONTH, value) }

    var morningBonusArmedDate: String
        get() = prefs.getString(KEY_MORNING_BONUS_ARMED_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_MORNING_BONUS_ARMED_DATE, value) }

    var morningBonusAppliedDate: String
        get() = prefs.getString(KEY_MORNING_BONUS_APPLIED_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_MORNING_BONUS_APPLIED_DATE, value) }

    var morningBonusMultiplier: Float
        get() = prefs.getFloat(KEY_MORNING_BONUS_MULTIPLIER, 1.5f)
        set(value) = prefs.edit { putFloat(KEY_MORNING_BONUS_MULTIPLIER, value) }

    var lastIntegrityFinalizedDate: String
        get() = prefs.getString(KEY_LAST_INTEGRITY_FINALIZED_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_INTEGRITY_FINALIZED_DATE, value) }

    var ironIntegrityStrictnessBoostDate: String
        get() = prefs.getString(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_DATE, value) }

    var ironIntegrityStrictnessBoostActive: Boolean
        get() = prefs.getBoolean(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_ACTIVE, value) }

    var ironIntegrityStrictnessBoostSeconds: Long
        get() = prefs.getLong(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_SECONDS, 0L)
        set(value) = prefs.edit { putLong(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_SECONDS, value) }

    var ironIntegrityStrictnessBoostWorkLockPenaltyBonus: Int
        get() = prefs.getInt(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_WORK_LOCK_PENALTY_BONUS, 0)
        set(value) = prefs.edit { putInt(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_WORK_LOCK_PENALTY_BONUS, value) }

    var ironIntegrityStrictnessBoostEmergencyExitReduction: Int
        get() = prefs.getInt(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_EMERGENCY_EXIT_REDUCTION, 0)
        set(value) = prefs.edit { putInt(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_EMERGENCY_EXIT_REDUCTION, value) }

    var ironIntegrityStrictnessBoostRequireCleanEarnedUnlock: Boolean
        get() = prefs.getBoolean(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_REQUIRE_CLEAN_EARNED_UNLOCK, false)
        set(value) = prefs.edit { putBoolean(KEY_IRON_INTEGRITY_STRICTNESS_BOOST_REQUIRE_CLEAN_EARNED_UNLOCK, value) }

    // ── Integrity: supportive pattern tracking (replaces penalty escalation) ──
    var integrityStrugglingDaysInRow: Int
        get() = prefs.getInt("integrity_struggling_days_in_row", 0)
        set(value) = prefs.edit { putInt("integrity_struggling_days_in_row", value) }

    var integritySuggestRecovery: Boolean
        get() = prefs.getBoolean("integrity_suggest_recovery", false)
        set(value) = prefs.edit { putBoolean("integrity_suggest_recovery", value) }

    var integrityConsistentDaysInRow: Int
        get() = prefs.getInt("integrity_consistent_days_in_row", 0)
        set(value) = prefs.edit { putInt("integrity_consistent_days_in_row", value) }

    var integrityPerfectDaysInRow: Int
        get() = prefs.getInt("integrity_perfect_days_in_row", 0)
        set(value) = prefs.edit { putInt("integrity_perfect_days_in_row", value) }

    var emergencyExitBudget: Int
        get() = prefs.getInt("emergency_exit_budget", 5)
        set(value) = prefs.edit { putInt("emergency_exit_budget", value) }

    var lastReflectionDate: String
        get() = prefs.getString("last_reflection_date", "") ?: ""
        set(value) = prefs.edit { putString("last_reflection_date", value) }

    var lastReflectionText: String
        get() = prefs.getString("last_reflection_text", "") ?: ""
        set(value) = prefs.edit { putString("last_reflection_text", value) }

    var lastKnownIntegrityLevel: Float
        get() = prefs.getFloat("last_known_integrity_level", 1.0f)
        set(value) = prefs.edit { putFloat("last_known_integrity_level", value) }

    // ── Honesty Mode: transparent override system ──
    var honestyModeEnabled: Boolean
        get() = prefs.getBoolean("honesty_mode_enabled", true)
        set(value) = prefs.edit { putBoolean("honesty_mode_enabled", value) }

    var honestyOverrideCount: Int
        get() = prefs.getInt("honesty_override_count", 0)
        set(value) = prefs.edit { putInt("honesty_override_count", value) }

    var lastHonestyOverrideDate: String
        get() = prefs.getString("last_honesty_override_date", "") ?: ""
        set(value) = prefs.edit { putString("last_honesty_override_date", value) }

    var lastModeSwitchTimestamp: Long
        get() = prefs.getLong("last_mode_switch_timestamp", 0L)
        set(value) = prefs.edit { putLong("last_mode_switch_timestamp", value) }

    var nightDecisionEnabled: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_DECISION_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_NIGHT_DECISION_ENABLED, value) }

    var nightDecisionActive: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_DECISION_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_NIGHT_DECISION_ACTIVE, value) }

    var nightDecisionStatus: String
        get() = prefs.getString(KEY_NIGHT_DECISION_STATUS, "UNDECIDED") ?: "UNDECIDED"
        set(value) = prefs.edit { putString(KEY_NIGHT_DECISION_STATUS, value) }

    var nightDecisionDate: String
        get() = prefs.getString(KEY_NIGHT_DECISION_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_NIGHT_DECISION_DATE, value) }

    var nightDecisionReason: String
        get() = prefs.getString(KEY_NIGHT_DECISION_REASON, "") ?: ""
        set(value) = prefs.edit { putString(KEY_NIGHT_DECISION_REASON, value) }

    var nightDecisionTriggeredAt: Long
        get() = prefs.getLong(KEY_NIGHT_DECISION_TRIGGERED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_NIGHT_DECISION_TRIGGERED_AT, value) }

    var nightDecisionCompletedAt: Long
        get() = prefs.getLong(KEY_NIGHT_DECISION_COMPLETED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_NIGHT_DECISION_COMPLETED_AT, value) }

    var nightDecisionXpPenaltyApplied: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_DECISION_XP_PENALTY_APPLIED, false)
        set(value) = prefs.edit { putBoolean(KEY_NIGHT_DECISION_XP_PENALTY_APPLIED, value) }

    var nightDecisionPlannerCompleted: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_DECISION_PLANNER_COMPLETED, false)
        set(value) = prefs.edit { putBoolean(KEY_NIGHT_DECISION_PLANNER_COMPLETED, value) }

    var nightDecisionSource: String
        get() = prefs.getString(KEY_NIGHT_DECISION_SOURCE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_NIGHT_DECISION_SOURCE, value) }

    // --- CRUCIBLE TRACKING ---
    var activeCrucibleId: String
        get() = prefs.getString(KEY_ACTIVE_CRUCIBLE_ID, "") ?: ""
        set(value) = prefs.edit { putString(KEY_ACTIVE_CRUCIBLE_ID, value) }

    var crucibleStartDate: String
        get() = prefs.getString(KEY_CRUCIBLE_START_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CRUCIBLE_START_DATE, value) }

    var crucibleCompletedDates: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_CRUCIBLE_COMPLETED_DATES, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_CRUCIBLE_COMPLETED_DATES, HashSet(value)) }

    var earnedCrucibleTitles: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_EARNED_CRUCIBLE_TITLES, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_EARNED_CRUCIBLE_TITLES, HashSet(value)) }

    var crucibleActive: Boolean
        get() = prefs.getBoolean(KEY_CRUCIBLE_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_CRUCIBLE_ACTIVE, value) }

    var crucibleEndTime: Long
        get() = prefs.getLong(KEY_CRUCIBLE_END_TIME, 0L)
        set(value) = prefs.edit { putLong(KEY_CRUCIBLE_END_TIME, value) }

    var crucibleTaskName: String
        get() = prefs.getString(KEY_CRUCIBLE_TASK_NAME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CRUCIBLE_TASK_NAME, value) }

    var cruciblePenalty: String
        get() = prefs.getString(KEY_CRUCIBLE_PENALTY, "STREAK_RESET") ?: "STREAK_RESET"
        set(value) = prefs.edit { putString(KEY_CRUCIBLE_PENALTY, value) }

    var crucibleResult: String
        get() = prefs.getString(KEY_CRUCIBLE_RESULT, "NONE") ?: "NONE"
        set(value) = prefs.edit { putString(KEY_CRUCIBLE_RESULT, value) }

    var crucibleTotalCompleted: Int
        get() = prefs.getInt(KEY_CRUCIBLE_TOTAL_COMPLETED, 0)
        set(value) = prefs.edit { putInt(KEY_CRUCIBLE_TOTAL_COMPLETED, value) }

    var crucibleTotalFailed: Int
        get() = prefs.getInt(KEY_CRUCIBLE_TOTAL_FAILED, 0)
        set(value) = prefs.edit { putInt(KEY_CRUCIBLE_TOTAL_FAILED, value) }

    var crucibleRunStartedAt: Long
        get() = prefs.getLong(KEY_CRUCIBLE_RUN_STARTED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_CRUCIBLE_RUN_STARTED_AT, value) }

    // --- BOSS MODE TRACKING ---
    var lastBossModeAppliedDate: String
        get() = prefs.getString(KEY_LAST_BOSS_MODE_APPLIED_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_BOSS_MODE_APPLIED_DATE, value) }

    var bossModeActive: Boolean
        get() = prefs.getBoolean(KEY_BOSS_MODE_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_BOSS_MODE_ACTIVE, value) }

    var bossModeDurationBonus: Int
        get() = prefs.getInt(KEY_BOSS_MODE_DURATION_BONUS, 0)
        set(value) = prefs.edit { putInt(KEY_BOSS_MODE_DURATION_BONUS, value) }

    // --- CHALLENGE TRACKING ---
    var challengeActive: Boolean
        get() = prefs.getBoolean(KEY_CHALLENGE_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_CHALLENGE_ACTIVE, value) }

    var challengeStartDate: String
        get() = prefs.getString(KEY_CHALLENGE_START_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CHALLENGE_START_DATE, value) }

    var challengeDaysCompleted: Int
        get() = prefs.getInt(KEY_CHALLENGE_DAYS_COMPLETED, 0)
        set(value) = prefs.edit { putInt(KEY_CHALLENGE_DAYS_COMPLETED, value) }

    var ironStatusUnlocked: Boolean
        get() = prefs.getBoolean(KEY_IRON_STATUS_UNLOCKED, false)
        set(value) = prefs.edit { putBoolean(KEY_IRON_STATUS_UNLOCKED, value) }

    var challengeLastCompleted: String
        get() = prefs.getString(KEY_CHALLENGE_LAST_COMPLETED, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CHALLENGE_LAST_COMPLETED, value) }

    var challengeRunStartedAt: Long
        get() = prefs.getLong(KEY_CHALLENGE_RUN_STARTED_AT, 0L)
        set(value) = prefs.edit { putLong(KEY_CHALLENGE_RUN_STARTED_AT, value) }

    var challengeAlarmActive: Boolean
        get() = prefs.getBoolean(KEY_CHALLENGE_ALARM_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_CHALLENGE_ALARM_ACTIVE, value) }

    var challengeAlarmDate: String
        get() = prefs.getString(KEY_CHALLENGE_ALARM_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_CHALLENGE_ALARM_DATE, value) }

    // --- ADAPTIVE ENGINE & BEHAVIOUR ---
    var modeOverrideActive: Boolean
        get() = prefs.getBoolean(KEY_MODE_OVERRIDE_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_MODE_OVERRIDE_ACTIVE, value) }

    var lastAppOpen: String
        get() = prefs.getString(KEY_LAST_APP_OPEN, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_APP_OPEN, value) }

    var lastRejectedRecommendationMode: String
        get() = prefs.getString(KEY_LAST_REJECTED_RECOMMENDATION_MODE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_REJECTED_RECOMMENDATION_MODE, value) }

    var lastRejectedRecommendationDate: String
        get() = prefs.getString(KEY_LAST_REJECTED_RECOMMENDATION_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_REJECTED_RECOMMENDATION_DATE, value) }

    var reducedMotion: Boolean
        get() = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        set(value) = prefs.edit { putBoolean(KEY_REDUCED_MOTION, value) }

    var muteAudio: Boolean
        get() = prefs.getBoolean(KEY_MUTE_AUDIO, false)
        set(value) = prefs.edit { putBoolean(KEY_MUTE_AUDIO, value) }

    var reducedHaptics: Boolean
        get() = prefs.getBoolean(KEY_REDUCED_HAPTICS, false)
        set(value) = prefs.edit { putBoolean(KEY_REDUCED_HAPTICS, value) }

    var largeText: Boolean
        get() = prefs.getBoolean(KEY_LARGE_TEXT, false)
        set(value) = prefs.edit { putBoolean(KEY_LARGE_TEXT, value) }

    var profileDate: String
        get() = prefs.getString(KEY_PROFILE_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PROFILE_DATE, value) }

    // --- BETA / RETENTION TRACKING ---
    var firstInstallDateEpoch: Long
        get() = prefs.getLong(KEY_FIRST_INSTALL_DATE_EPOCH, 0L)
        set(value) = prefs.edit { putLong(KEY_FIRST_INSTALL_DATE_EPOCH, value) }

    var d1RetentionLogged: Boolean
        get() = prefs.getBoolean(KEY_D1_RETENTION_LOGGED, false)
        set(value) = prefs.edit { putBoolean(KEY_D1_RETENTION_LOGGED, value) }

    var d7RetentionLogged: Boolean
        get() = prefs.getBoolean(KEY_D7_RETENTION_LOGGED, false)
        set(value) = prefs.edit { putBoolean(KEY_D7_RETENTION_LOGGED, value) }

    var betaUserTagLogged: Boolean
        get() = prefs.getBoolean(KEY_BETA_USER_TAG_LOGGED, false)
        set(value) = prefs.edit { putBoolean(KEY_BETA_USER_TAG_LOGGED, value) }

    var recoveryModeEnteredDate: String
        get() = prefs.getString(KEY_RECOVERY_MODE_ENTERED_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_RECOVERY_MODE_ENTERED_DATE, value) }

    var lastEnergyScore: Int
        get() = prefs.getInt(KEY_LAST_ENERGY_SCORE, 0)
        set(value) = prefs.edit { putInt(KEY_LAST_ENERGY_SCORE, value) }

    var lastStressScore: Int
        get() = prefs.getInt(KEY_LAST_STRESS_SCORE, 0)
        set(value) = prefs.edit { putInt(KEY_LAST_STRESS_SCORE, value) }

    var lastMoodWord: String
        get() = prefs.getString(KEY_LAST_MOOD_WORD, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_MOOD_WORD, value) }

    // --- BLACK HOLE MODE ---
    var isBlackHoleActive: Boolean
        get() = prefs.getBoolean(KEY_BLACK_HOLE_ACTIVE, false)
        set(value) = prefs.edit { putBoolean(KEY_BLACK_HOLE_ACTIVE, value) }

    // --- EMERGENCY VALVE ---
    var emergencyValveTokens: Int
        get() {
            checkEmergencyTokenReset()
            return prefs.getInt(KEY_EMERGENCY_VALVE_TOKENS, 3)
        }
        set(value) = prefs.edit { putInt(KEY_EMERGENCY_VALVE_TOKENS, value) }

    private var lastEmergencyResetMonth: Int
        get() = prefs.getInt(KEY_LAST_EMERGENCY_RESET_MONTH, 0)
        set(value) = prefs.edit { putInt(KEY_LAST_EMERGENCY_RESET_MONTH, value) }

    var emergencyValveCooldownUntil: Long
        get() = prefs.getLong(KEY_EMERGENCY_VALVE_COOLDOWN_UNTIL, 0L)
        set(value) = prefs.edit { putLong(KEY_EMERGENCY_VALVE_COOLDOWN_UNTIL, value) }

    val emergencyValveCooldownActive: Boolean
        get() = emergencyValveCooldownUntil > System.currentTimeMillis()

    fun activateEmergencyValveCooldown(durationMillis: Long, context: Context) {
        emergencyValveCooldownUntil = System.currentTimeMillis() + durationMillis
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD"))
    }

    fun clearEmergencyValveCooldown(context: Context) {
        emergencyValveCooldownUntil = 0L
        context.sendBroadcast(Intent("com.ironmind.RELOAD_GUARD"))
    }

    // --- ARTIFACT SYSTEM ---
    var unlockedArtifactIds: Set<String>
        get() = HashSet(prefs.getStringSet(KEY_UNLOCKED_ARTIFACT_IDS, emptySet()) ?: emptySet())
        set(value) = prefs.edit { putStringSet(KEY_UNLOCKED_ARTIFACT_IDS, HashSet(value)) }

    var lastAutopsyOpenDate: String
        get() = prefs.getString(KEY_LAST_AUTOPSY_OPEN_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_AUTOPSY_OPEN_DATE, value) }

    var autopsyConsecutiveDays: Int
        get() = prefs.getInt(KEY_AUTOPSY_CONSECUTIVE_DAYS, 0)
        set(value) = prefs.edit { putInt(KEY_AUTOPSY_CONSECUTIVE_DAYS, value) }

    // --- MORNING ROUTINE ---
    var reduceMotionEnabled: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_MOTION_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_REDUCE_MOTION_ENABLED, value) }

    var routineType: String
        get() = prefs.getString(KEY_ROUTINE_TYPE, "STANDARD") ?: "STANDARD"
        set(value) = prefs.edit { putString(KEY_ROUTINE_TYPE, value) }

    var routineCompletedToday: Boolean
        get() = prefs.getBoolean(KEY_ROUTINE_COMPLETED_TODAY, false)
        set(value) = prefs.edit { putBoolean(KEY_ROUTINE_COMPLETED_TODAY, value) }

    var routineDate: String
        get() = prefs.getString(KEY_ROUTINE_DATE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_ROUTINE_DATE, value) }

    var chatUnlocked: Boolean
        get() = prefs.getBoolean(KEY_CHAT_UNLOCKED, false)
        set(value) = prefs.edit { putBoolean(KEY_CHAT_UNLOCKED, value) }

    var chatMinStreakMet: Boolean
        get() = prefs.getBoolean(KEY_CHAT_MIN_STREAK_MET, false)
        set(value) = prefs.edit { putBoolean(KEY_CHAT_MIN_STREAK_MET, value) }

    var clubReadWelcomeSeen: Boolean
        get() = prefs.getBoolean(KEY_CLUB_READ_WELCOME_SEEN, false)
        set(value) = prefs.edit { putBoolean(KEY_CLUB_READ_WELCOME_SEEN, value) }

    var clubFullWelcomeSeen: Boolean
        get() = prefs.getBoolean(KEY_CLUB_FULL_WELCOME_SEEN, false)
        set(value) = prefs.edit { putBoolean(KEY_CLUB_FULL_WELCOME_SEEN, value) }

    var clubFirstPostPromptSeen: Boolean
        get() = prefs.getBoolean(KEY_CLUB_FIRST_POST_PROMPT_SEEN, false)
        set(value) = prefs.edit { putBoolean(KEY_CLUB_FIRST_POST_PROMPT_SEEN, value) }

    var clubTrackedPostCount: Int
        get() = prefs.getInt(KEY_CLUB_TRACKED_POST_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_CLUB_TRACKED_POST_COUNT, value) }

    var ironCircleEnabled: Boolean
        get() = prefs.getBoolean(KEY_IRON_CIRCLE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_IRON_CIRCLE_ENABLED, value) }

    var socialDiscoverable: Boolean
        get() = prefs.getBoolean(KEY_SOCIAL_DISCOVERABLE, false)
        set(value) = prefs.edit { putBoolean(KEY_SOCIAL_DISCOVERABLE, value) }

    var socialShareStreakBand: Boolean
        get() = prefs.getBoolean(KEY_SOCIAL_SHARE_STREAK_BAND, true)
        set(value) = prefs.edit { putBoolean(KEY_SOCIAL_SHARE_STREAK_BAND, value) }

    var socialShareConsistencyTrend: Boolean
        get() = prefs.getBoolean(KEY_SOCIAL_SHARE_CONSISTENCY_TREND, true)
        set(value) = prefs.edit { putBoolean(KEY_SOCIAL_SHARE_CONSISTENCY_TREND, value) }

    var socialShareFiveAmWeekly: Boolean
        get() = prefs.getBoolean(KEY_SOCIAL_SHARE_FIVE_AM_WEEKLY, true)
        set(value) = prefs.edit { putBoolean(KEY_SOCIAL_SHARE_FIVE_AM_WEEKLY, value) }

    var socialShareTitles: Boolean
        get() = prefs.getBoolean(KEY_SOCIAL_SHARE_TITLES, true)
        set(value) = prefs.edit { putBoolean(KEY_SOCIAL_SHARE_TITLES, value) }

    var permissionBannerSnoozedUntil: Long
        get() = prefs.getLong(KEY_PERMISSION_BANNER_SNOOZED_UNTIL, 0L)
        set(value) = prefs.edit { putLong(KEY_PERMISSION_BANNER_SNOOZED_UNTIL, value) }

    var permissionBannerSnoozedPriority: String
        get() = prefs.getString(KEY_PERMISSION_BANNER_SNOOZED_PRIORITY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PERMISSION_BANNER_SNOOZED_PRIORITY, value) }

    var savedTaskBlueprintsJson: String
        get() = prefs.getString(KEY_SAVED_TASK_BLUEPRINTS_JSON, "[]") ?: "[]"
        set(value) = prefs.edit { putString(KEY_SAVED_TASK_BLUEPRINTS_JSON, value) }

    fun clearPermissionBannerSnooze() {
        permissionBannerSnoozedUntil = 0L
        permissionBannerSnoozedPriority = ""
    }

    fun clearActiveMissionContextApps() {
        activeMissionContextApps = emptySet()
    }

    fun saveMissionContextApps(taskId: Int, apps: Set<String>) {
        prefs.edit { putStringSet("mission_context_$taskId", HashSet(apps)) }
    }

    fun getMissionContextApps(taskId: Int): Set<String>? {
        return prefs.getStringSet("mission_context_$taskId", null)?.toSet()
    }

    fun clearMissionContextApps(taskId: Int) {
        prefs.edit { remove("mission_context_$taskId") }
    }

    fun clearEarnedUnlock() {
        earnedUnlockActive = false
        earnedUnlockUnlockedToday = false
        earnedUnlockMode = "DAY"
        earnedUnlockDate = ""
        earnedUnlockRemainingCount = 0
        earnedUnlockTotalCount = 0
        earnedUnlockCompletedCount = 0
        earnedUnlockSkippedCount = 0
    }

    fun clearWorkLock() {
        workLockActive = false
        workLockStage = "IDLE"
        workLockTaskId = -1
        workLockTaskName = ""
        workLockStartedAt = 0L
        workLockEndsAt = 0L
        workLockPenaltyArmed = false
    }

    fun clearPomodoro() {
        pomodoroActive = false
        pomodoroTaskId = -1
        pomodoroSessionId = ""
        pomodoroSource = "TASK"
        pomodoroTitle = ""
        pomodoroDate = ""
        pomodoroStartTime = ""
        pomodoroEndTime = ""
        pomodoroTotalDurationMinutes = 0
        pomodoroPreset = "CLASSIC_25_5"
        pomodoroPhase = "IDLE"
        pomodoroPhaseEndsAt = 0L
        pomodoroIntervalIndex = 0
        pomodoroCompletedWorkIntervals = 0
        pomodoroBreachCount = 0
        pomodoroBreakUnlocked = false
        pomodoroStartedAt = 0L
        pomodoroSessionHardEndsAt = 0L
    }

    fun clearNightDecision() {
        nightDecisionActive = false
        nightDecisionStatus = "UNDECIDED"
        nightDecisionDate = ""
        nightDecisionReason = ""
        nightDecisionTriggeredAt = 0L
        nightDecisionCompletedAt = 0L
        nightDecisionXpPenaltyApplied = false
        nightDecisionPlannerCompleted = false
        nightDecisionSource = ""
    }

    fun loadSavedTaskBlueprints(): List<SavedTaskBlueprintRecord> {
        val json = runCatching { JSONArray(savedTaskBlueprintsJson) }.getOrElse { JSONArray() }
        return buildList {
            for (index in 0 until json.length()) {
                val item = json.optJSONObject(index) ?: continue
                add(
                    SavedTaskBlueprintRecord(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        summary = item.optString("summary"),
                        createdAt = item.optLong("createdAt"),
                        missions = buildList {
                            val missionsJson = item.optJSONArray("missions") ?: JSONArray()
                            for (missionIndex in 0 until missionsJson.length()) {
                                val mission = missionsJson.optJSONObject(missionIndex) ?: continue
                                add(
                                    SavedTaskBlueprintMissionRecord(
                                        name = mission.optString("name"),
                                        startTime = mission.optString("startTime"),
                                        endTime = mission.optString("endTime"),
                                        importanceRank = mission.optInt("importanceRank", 3),
                                        type = mission.optString("type"),
                                        taskType = mission.optString("taskType", "OTHER"),
                                        focusModeEnabled = mission.optBoolean("focusModeEnabled", false),
                                        focusPreset = mission.optString("focusPreset", "CLASSIC_25_5"),
                                        parentMissionId = mission.optString("parentMissionId"),
                                        segmentBaseName = mission.optString("segmentBaseName"),
                                        segmentIndex = mission.optInt("segmentIndex", 0),
                                        segmentCount = mission.optInt("segmentCount", 0),
                                        isBreakSegment = mission.optBoolean("isBreakSegment", false),
                                        packageName = mission.optString("packageName").ifBlank { null }
                                    )
                                )
                            }
                        }
                    )
                )
            }
        }
    }

    fun saveTaskBlueprints(records: List<SavedTaskBlueprintRecord>) {
        val root = JSONArray()
        records.forEach { record ->
            val item = JSONObject()
                .put("id", record.id)
                .put("title", record.title)
                .put("summary", record.summary)
                .put("createdAt", record.createdAt)
            val missions = JSONArray()
            record.missions.forEach { mission ->
                missions.put(
                    JSONObject()
                        .put("name", mission.name)
                        .put("startTime", mission.startTime)
                        .put("endTime", mission.endTime)
                        .put("importanceRank", mission.importanceRank)
                        .put("type", mission.type)
                        .put("taskType", mission.taskType)
                        .put("focusModeEnabled", mission.focusModeEnabled)
                        .put("focusPreset", mission.focusPreset)
                        .put("parentMissionId", mission.parentMissionId)
                        .put("segmentBaseName", mission.segmentBaseName)
                        .put("segmentIndex", mission.segmentIndex)
                        .put("segmentCount", mission.segmentCount)
                        .put("isBreakSegment", mission.isBreakSegment)
                        .put("packageName", mission.packageName ?: "")
                )
            }
            item.put("missions", missions)
            root.put(item)
        }
        savedTaskBlueprintsJson = root.toString()
    }

    private fun checkEmergencyTokenReset() {
        val currentMonth = LocalDate.now().monthValue
        if (lastEmergencyResetMonth != currentMonth) {
            emergencyValveTokens = 3
            lastEmergencyResetMonth = currentMonth
        }
    }

    fun toBackupMap(): Map<String, Any?> {
        return prefs.all
    }

    companion object {
        private const val PREFS_NAME = "ironmind_master_prefs"
        private const val KEY_LANGUAGE_SELECTED = "language_selected"
        private const val KEY_DIAGNOSTIC_DONE = "diagnostic_done"
        private const val KEY_INTRO_SEEN = "intro_seen"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_FIRST_HOME_ARRIVAL_DATE = "first_home_arrival_date"
        private const val KEY_CONTRACT_SIGNED = "contract_signed"
        private const val KEY_IDENTITY_SETUP_COMPLETE = "identity_setup_complete"
        private const val KEY_COMMITMENT_CONTRACT_COMPLETE = "commitment_contract_complete"
        private const val KEY_DISTRACTIONS_SETUP_COMPLETE = "distractions_setup_complete"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_SECONDARY_TYPE = "secondary_type"
        private const val KEY_APP_MODE = "app_mode"
        private const val KEY_STREAK_COUNT = "streak_count"
        private const val KEY_LAST_COMPLETE_DATE = "last_complete_date"
        private const val KEY_TOTAL_COMPLETED = "total_completed"
        private const val KEY_TOTAL_SKIPPED = "total_skipped"
        private const val KEY_CONTRACT_GOAL = "contract_goal"
        private const val KEY_CONTRACT_REASON = "contract_reason"
        private const val KEY_CONTRACT_SIGNATURE = "contract_signature"
        private const val KEY_PARTNER_NAME = "partner_name"
        private const val KEY_PARTNER_PHONE = "partner_phone"
        private const val KEY_CLOUD_BACKUP_ENABLED = "cloud_backup_enabled"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private const val KEY_FIREBASE_UID = "firebase_uid"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_LAST_RELAPSE_TIMESTAMP = "last_relapse_timestamp"
        private const val KEY_BLOCKED_APPS = "blocked_apps"
        private const val KEY_EMERGENCY_APPS = "emergency_apps"
        private const val KEY_IDENTITY_STATEMENTS = "identity_statements"
        private const val KEY_DETOX_ENABLED = "detox_enabled"
        private const val KEY_DETOX_START_HOUR = "detox_start_hour"
        private const val KEY_DETOX_END_HOUR = "detox_end_hour"
        private const val KEY_DETOX_CURRENTLY_ACTIVE = "detox_currently_active"
        private const val KEY_LAST_CHECKIN_DATE = "last_checkin_date"
        private const val KEY_LAST_WEEKLY_HONEST_SCORE_WEEK = "last_weekly_honest_score_week"
        private const val KEY_LAST_WEEKLY_HONEST_SCORE = "last_weekly_honest_score"
        private const val KEY_WEEKLY_HONEST_SCORE_HISTORY = "weekly_honest_score_history"
        private const val KEY_INTENSITY = "intensity"
        private const val KEY_WEBSITE_BLOCKING_ENABLED = "website_blocking_enabled"
        private const val KEY_BLOCKED_WEBSITES = "blocked_websites"
        private const val KEY_ACTIVE_TASK_NAME = "active_task_name"
        private const val KEY_ACTIVE_TASK_START = "active_task_start"
        private const val KEY_ACTIVE_TASK_END = "active_task_end"
        private const val KEY_ACTIVE_TASK_DATE = "active_task_date"
        private const val KEY_ACTIVE_MISSION_CONTEXT_APPS = "active_mission_context_apps"
        private const val KEY_EARNED_UNLOCK_ENABLED = "earned_unlock_enabled"
        private const val KEY_EARNED_UNLOCK_ACTIVE = "earned_unlock_active"
        private const val KEY_EARNED_UNLOCK_UNLOCKED_TODAY = "earned_unlock_unlocked_today"
        private const val KEY_EARNED_UNLOCK_MODE = "earned_unlock_mode"
        private const val KEY_EARNED_UNLOCK_DATE = "earned_unlock_date"
        private const val KEY_EARNED_UNLOCK_REMAINING_COUNT = "earned_unlock_remaining_count"
        private const val KEY_EARNED_UNLOCK_TOTAL_COUNT = "earned_unlock_total_count"
        private const val KEY_EARNED_UNLOCK_COMPLETED_COUNT = "earned_unlock_completed_count"
        private const val KEY_EARNED_UNLOCK_SKIPPED_COUNT = "earned_unlock_skipped_count"
        private const val KEY_WORK_LOCK_ENABLED = "work_lock_enabled"
        private const val KEY_WORK_LOCK_ACTIVE = "work_lock_active"
        private const val KEY_WORK_LOCK_STAGE = "work_lock_stage"
        private const val KEY_WORK_LOCK_TASK_ID = "work_lock_task_id"
        private const val KEY_WORK_LOCK_TASK_NAME = "work_lock_task_name"
        private const val KEY_WORK_LOCK_STARTED_AT = "work_lock_started_at"
        private const val KEY_WORK_LOCK_ENDS_AT = "work_lock_ends_at"
        private const val KEY_WORK_LOCK_PENALTY_ARMED = "work_lock_penalty_armed"
        private const val KEY_WORK_LOCK_EMERGENCY_EXIT_COUNT = "work_lock_emergency_exit_count"
        private const val KEY_WORK_LOCK_EMERGENCY_EXIT_MONTH = "work_lock_emergency_exit_month"
        private const val KEY_WORK_LOCK_BREAK_COUNT_TODAY = "work_lock_break_count_today"
        private const val KEY_POMODORO_ACTIVE = "pomodoro_active"
        private const val KEY_POMODORO_TASK_ID = "pomodoro_task_id"
        private const val KEY_POMODORO_SESSION_ID = "pomodoro_session_id"
        private const val KEY_POMODORO_SOURCE = "pomodoro_source"
        private const val KEY_POMODORO_TITLE = "pomodoro_title"
        private const val KEY_POMODORO_DATE = "pomodoro_date"
        private const val KEY_POMODORO_START_TIME = "pomodoro_start_time"
        private const val KEY_POMODORO_END_TIME = "pomodoro_end_time"
        private const val KEY_POMODORO_TOTAL_DURATION_MINUTES = "pomodoro_total_duration_minutes"
        private const val KEY_POMODORO_PRESET = "pomodoro_preset"
        private const val KEY_POMODORO_PHASE = "pomodoro_phase"
        private const val KEY_POMODORO_PHASE_ENDS_AT = "pomodoro_phase_ends_at"
        private const val KEY_POMODORO_INTERVAL_INDEX = "pomodoro_interval_index"
        private const val KEY_POMODORO_COMPLETED_WORK_INTERVALS = "pomodoro_completed_work_intervals"
        private const val KEY_POMODORO_BREACH_COUNT = "pomodoro_breach_count"
        private const val KEY_POMODORO_BREAK_UNLOCKED = "pomodoro_break_unlocked"
        private const val KEY_POMODORO_STARTED_AT = "pomodoro_started_at"
        private const val KEY_POMODORO_SESSION_HARD_ENDS_AT = "pomodoro_session_hard_ends_at"
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_CURRENT_IDENTITY_LEVEL = "current_identity_level"
        private const val KEY_BEDTIME = "bed_time"
        private const val KEY_SLEEP_LOCK_ENABLED = "sleep_lock_enabled"
        private const val KEY_SLEEP_LOCK_BED_HOUR = "sleep_lock_bed_hour"
        private const val KEY_SLEEP_LOCK_BED_MINUTE = "sleep_lock_bed_minute"
        private const val KEY_SLEEP_LOCK_WAKE_HOUR = "sleep_lock_wake_hour"
        private const val KEY_SLEEP_LOCK_WAKE_MINUTE = "sleep_lock_wake_minute"
        private const val KEY_SLEEP_LOCK_STORED_WAKE_HOUR_BEFORE_CHALLENGE = "sleep_lock_stored_wake_hour_before_challenge"
        private const val KEY_SLEEP_LOCK_STORED_WAKE_MINUTE_BEFORE_CHALLENGE = "sleep_lock_stored_wake_minute_before_challenge"
        private const val KEY_SLEEP_LOCK_WARNING_30_ENABLED = "sleep_lock_warning_30_enabled"
        private const val KEY_SLEEP_LOCK_WARNING_15_ENABLED = "sleep_lock_warning_15_enabled"
        private const val KEY_SLEEP_LOCK_WARNING_FINAL_ENABLED = "sleep_lock_warning_final_enabled"
        private const val KEY_SLEEP_LOCK_EMERGENCY_APPS = "sleep_lock_emergency_apps"
        private const val KEY_SLEEP_LOCK_SILENCE_NOTIFICATIONS = "sleep_lock_silence_notifications"
        private const val KEY_SLEEP_LOCK_SOUND_ENABLED = "sleep_lock_sound_enabled"
        private const val KEY_SLEEP_LOCK_SELECTED_SOUND = "sleep_lock_selected_sound"
        private const val KEY_SLEEP_LOCK_SELECTED_SOUNDS = "sleep_lock_selected_sounds"
        private const val KEY_SLEEP_LOCK_SOUND_MODE = "sleep_lock_sound_mode"
        private const val KEY_SLEEP_LOCK_ACTIVE = "sleep_lock_active"
        private const val KEY_SLEEP_LOCK_STARTED_AT = "sleep_lock_started_at"
        private const val KEY_SLEEP_LOCK_ENDS_AT = "sleep_lock_ends_at"
        private const val KEY_SLEEP_LOCK_LAST_WARNING_STAGE = "sleep_lock_last_warning_stage"
        private const val KEY_SLEEP_LOCK_DND_PERMISSION_ACKNOWLEDGED = "sleep_lock_dnd_permission_acknowledged"
        private const val KEY_SLEEP_LOCK_BYPASS_USED_AT = "sleep_lock_bypass_used_at"
        private const val KEY_SLEEP_LOCK_EMERGENCY_OVERRIDE_UNTIL = "sleep_lock_emergency_override_until"
        private const val KEY_SLEEP_LOCK_EMERGENCY_EXIT_COUNT = "sleep_lock_emergency_exit_count"
        private const val KEY_SLEEP_LOCK_EMERGENCY_EXIT_MONTH = "sleep_lock_emergency_exit_month"
        private const val KEY_SLEEP_LOCK_ONBOARDING_COMPLETE = "sleep_lock_onboarding_complete"
        private const val KEY_SLEEP_LOCK_PREVIOUS_INTERRUPTION_FILTER = "sleep_lock_previous_interruption_filter"
        private const val KEY_SLEEP_LOCK_SILENCE_ACTIVE = "sleep_lock_silence_active"
        private const val KEY_MORNING_LAUNCH_ENABLED = "morning_launch_enabled"
        private const val KEY_MORNING_LAUNCH_ACTIVE = "morning_launch_active"
        private const val KEY_MORNING_LAUNCH_STAGE = "morning_launch_stage"
        private const val KEY_MORNING_LAUNCH_SESSION_ID = "morning_launch_session_id"
        private const val KEY_MORNING_LAUNCH_STARTED_AT = "morning_launch_started_at"
        private const val KEY_MORNING_LAUNCH_ALARM_DISMISSED_AT = "morning_launch_alarm_dismissed_at"
        private const val KEY_MORNING_LAUNCH_SNOOZED_UNTIL = "morning_launch_snoozed_until"
        private const val KEY_MORNING_LAUNCH_MODE = "morning_launch_mode"
        private const val KEY_MORNING_LAUNCH_CURRENT_STEP_INDEX = "morning_launch_current_step_index"
        private const val KEY_MORNING_LAUNCH_COMPLETED_TODAY = "morning_launch_completed_today"
        private const val KEY_MORNING_LAUNCH_SKIPPED_TODAY = "morning_launch_skipped_today"
        private const val KEY_MORNING_LAUNCH_DELAY_COUNT = "morning_launch_delay_count"
        private const val KEY_MORNING_LAUNCH_LAST_OUTCOME = "morning_launch_last_outcome"
        private const val KEY_MORNING_LAUNCH_DATE = "morning_launch_date"
        private const val KEY_MORNING_LAUNCH_EMERGENCY_EXIT_COUNT = "morning_launch_emergency_exit_count"
        private const val KEY_MORNING_LAUNCH_EMERGENCY_EXIT_MONTH = "morning_launch_emergency_exit_month"
        private const val KEY_MORNING_BONUS_ARMED_DATE = "morning_bonus_armed_date"
        private const val KEY_MORNING_BONUS_APPLIED_DATE = "morning_bonus_applied_date"
        private const val KEY_MORNING_BONUS_MULTIPLIER = "morning_bonus_multiplier"
        private const val KEY_LAST_INTEGRITY_FINALIZED_DATE = "last_integrity_finalized_date"
        private const val KEY_IRON_INTEGRITY_STRICTNESS_BOOST_DATE = "iron_integrity_strictness_boost_date"
        private const val KEY_IRON_INTEGRITY_STRICTNESS_BOOST_ACTIVE = "iron_integrity_strictness_boost_active"
        private const val KEY_IRON_INTEGRITY_STRICTNESS_BOOST_SECONDS = "iron_integrity_strictness_boost_seconds"
        private const val KEY_IRON_INTEGRITY_STRICTNESS_BOOST_WORK_LOCK_PENALTY_BONUS = "iron_integrity_strictness_boost_work_lock_penalty_bonus"
        private const val KEY_IRON_INTEGRITY_STRICTNESS_BOOST_EMERGENCY_EXIT_REDUCTION = "iron_integrity_strictness_boost_emergency_exit_reduction"
        private const val KEY_IRON_INTEGRITY_STRICTNESS_BOOST_REQUIRE_CLEAN_EARNED_UNLOCK = "iron_integrity_strictness_boost_require_clean_earned_unlock"
        private const val KEY_NIGHT_DECISION_ENABLED = "night_decision_enabled"
        private const val KEY_NIGHT_DECISION_ACTIVE = "night_decision_active"
        private const val KEY_NIGHT_DECISION_STATUS = "night_decision_status"
        private const val KEY_NIGHT_DECISION_DATE = "night_decision_date"
        private const val KEY_NIGHT_DECISION_REASON = "night_decision_reason"
        private const val KEY_NIGHT_DECISION_TRIGGERED_AT = "night_decision_triggered_at"
        private const val KEY_NIGHT_DECISION_COMPLETED_AT = "night_decision_completed_at"
        private const val KEY_NIGHT_DECISION_XP_PENALTY_APPLIED = "night_decision_xp_penalty_applied"
        private const val KEY_NIGHT_DECISION_PLANNER_COMPLETED = "night_decision_planner_completed"
        private const val KEY_NIGHT_DECISION_SOURCE = "night_decision_source"

        // Crucible Keys
        private const val KEY_ACTIVE_CRUCIBLE_ID = "active_crucible_id"
        private const val KEY_CRUCIBLE_START_DATE = "crucible_start_date"
        private const val KEY_CRUCIBLE_COMPLETED_DATES = "crucible_completed_dates"
        private const val KEY_EARNED_CRUCIBLE_TITLES = "earned_crucible_titles"
        
        private const val KEY_CRUCIBLE_ACTIVE = "crucible_active"
        private const val KEY_CRUCIBLE_END_TIME = "crucible_end_time"
        private const val KEY_CRUCIBLE_TASK_NAME = "crucible_task_name"
        private const val KEY_CRUCIBLE_PENALTY = "crucible_penalty"
        private const val KEY_CRUCIBLE_RESULT = "crucible_result"
        private const val KEY_CRUCIBLE_TOTAL_COMPLETED = "crucible_total_completed"
        private const val KEY_CRUCIBLE_TOTAL_FAILED = "crucible_total_failed"
        private const val KEY_CRUCIBLE_RUN_STARTED_AT = "crucible_run_started_at"

        // Boss Mode Keys
        private const val KEY_LAST_BOSS_MODE_APPLIED_DATE = "last_boss_mode_applied_date"
        private const val KEY_BOSS_MODE_ACTIVE = "boss_mode_active"
        private const val KEY_BOSS_MODE_DURATION_BONUS = "boss_mode_duration_bonus"

        // Challenge Keys
        private const val KEY_CHALLENGE_ACTIVE = "challenge_active"
        private const val KEY_CHALLENGE_START_DATE = "challenge_start_date"
        private const val KEY_CHALLENGE_DAYS_COMPLETED = "challenge_days_completed"
        private const val KEY_IRON_STATUS_UNLOCKED = "iron_status_unlocked"
        private const val KEY_CHALLENGE_LAST_COMPLETED = "challenge_last_completed"
        private const val KEY_CHALLENGE_RUN_STARTED_AT = "challenge_run_started_at"
        private const val KEY_CHALLENGE_ALARM_ACTIVE = "challenge_alarm_active"
        private const val KEY_CHALLENGE_ALARM_DATE = "challenge_alarm_date"

        // Adaptive Engine Keys
        private const val KEY_MODE_OVERRIDE_ACTIVE = "mode_override_active"
        private const val KEY_LAST_APP_OPEN = "last_app_open"
        private const val KEY_LAST_REJECTED_RECOMMENDATION_MODE = "last_rejected_rec_mode"
        private const val KEY_LAST_REJECTED_RECOMMENDATION_DATE = "last_rejected_rec_date"
        private const val KEY_PROFILE_DATE = "profile_date"
        
        // Accessibility Keys
        private const val KEY_REDUCE_MOTION_ENABLED = "reduce_motion_enabled"
        private const val KEY_REDUCED_MOTION = "reduced_motion"
        private const val KEY_MUTE_AUDIO = "mute_audio"
        private const val KEY_REDUCED_HAPTICS = "reduced_haptics"
        private const val KEY_LARGE_TEXT = "large_text"
        private const val KEY_LAST_ENERGY_SCORE = "last_energy_score"
        private const val KEY_LAST_STRESS_SCORE = "last_stress_score"
        private const val KEY_LAST_MOOD_WORD = "last_mood_word"

        // Black Hole Keys
        private const val KEY_BLACK_HOLE_ACTIVE = "black_hole_active"

        // Emergency Valve Keys
        private const val KEY_EMERGENCY_VALVE_TOKENS = "emergency_valve_tokens"
        private const val KEY_LAST_EMERGENCY_RESET_MONTH = "last_emergency_reset_month"
        private const val KEY_EMERGENCY_VALVE_COOLDOWN_UNTIL = "emergency_valve_cooldown_until"

        // Artifact Keys
        private const val KEY_UNLOCKED_ARTIFACT_IDS = "unlocked_artifact_ids"
        private const val KEY_LAST_AUTOPSY_OPEN_DATE = "last_autopsy_open_date"
        private const val KEY_AUTOPSY_CONSECUTIVE_DAYS = "autopsy_consecutive_days"

        // Morning Routine Keys
        private const val KEY_MORNING_RITUAL_CURRENT_STEP = "morning_ritual_current_step"
        private const val KEY_ROUTINE_TYPE = "routine_type"
        private const val KEY_ROUTINE_COMPLETED_TODAY = "routine_completed_today"
        private const val KEY_ROUTINE_DATE = "routine_date"
        private const val KEY_CHAT_UNLOCKED = "chat_unlocked"
        private const val KEY_CHAT_MIN_STREAK_MET = "chat_min_streak_met"
        private const val KEY_CLUB_READ_WELCOME_SEEN = "club_read_welcome_seen"
        private const val KEY_CLUB_FULL_WELCOME_SEEN = "club_full_welcome_seen"
        private const val KEY_CLUB_FIRST_POST_PROMPT_SEEN = "club_first_post_prompt_seen"
        private const val KEY_CLUB_TRACKED_POST_COUNT = "club_tracked_post_count"
        private const val KEY_IRON_CIRCLE_ENABLED = "iron_circle_enabled"
        private const val KEY_SOCIAL_DISCOVERABLE = "social_discoverable"
        private const val KEY_SOCIAL_SHARE_STREAK_BAND = "social_share_streak_band"
        private const val KEY_SOCIAL_SHARE_CONSISTENCY_TREND = "social_share_consistency_trend"
        private const val KEY_SOCIAL_SHARE_FIVE_AM_WEEKLY = "social_share_five_am_weekly"
        private const val KEY_SOCIAL_SHARE_TITLES = "social_share_titles"
        private const val KEY_PERMISSION_BANNER_SNOOZED_UNTIL = "permission_banner_snoozed_until"
        private const val KEY_PERMISSION_BANNER_SNOOZED_PRIORITY = "permission_banner_snoozed_priority"
        private const val KEY_SAVED_TASK_BLUEPRINTS_JSON = "saved_task_blueprints_json"

        // Beta / Retention Keys
        private const val KEY_FIRST_INSTALL_DATE_EPOCH = "first_install_date_epoch"
        private const val KEY_D1_RETENTION_LOGGED = "d1_retention_logged"
        private const val KEY_D7_RETENTION_LOGGED = "d7_retention_logged"
        private const val KEY_BETA_USER_TAG_LOGGED = "beta_user_tag_logged"
        private const val KEY_RECOVERY_MODE_ENTERED_DATE = "recovery_mode_entered_date"

        @Volatile
        private var INSTANCE: PrefManager? = null

        fun getInstance(context: Context): PrefManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PrefManager(context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
                INSTANCE = instance
                instance
            }
        }
    }
}

data class SavedTaskBlueprintRecord(
    val id: String,
    val title: String,
    val summary: String,
    val createdAt: Long,
    val missions: List<SavedTaskBlueprintMissionRecord>
)

data class SavedTaskBlueprintMissionRecord(
    val name: String,
    val startTime: String,
    val endTime: String,
    val importanceRank: Int,
    val type: String,
    val taskType: String = "OTHER",
    val focusModeEnabled: Boolean = false,
    val focusPreset: String = "CLASSIC_25_5",
    val parentMissionId: String = "",
    val segmentBaseName: String = "",
    val segmentIndex: Int = 0,
    val segmentCount: Int = 0,
    val isBreakSegment: Boolean = false,
    val packageName: String?
)
