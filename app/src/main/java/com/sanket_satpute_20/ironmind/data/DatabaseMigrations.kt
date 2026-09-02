package com.sanket_satpute_20.ironmind.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean {
        query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                    return true
                }
            }
        }
        return false
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN startedAt INTEGER")
            database.execSQL("ALTER TABLE tasks ADD COLUMN completedAt INTEGER")
            database.execSQL("ALTER TABLE tasks ADD COLUMN skippedAt INTEGER")
            database.execSQL("ALTER TABLE tasks ADD COLUMN deferredAt INTEGER")
            database.execSQL("ALTER TABLE tasks ADD COLUMN skipReason TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE tasks ADD COLUMN completionSource TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE tasks ADD COLUMN origin TEXT NOT NULL DEFAULT 'MANUAL'")
            database.execSQL("ALTER TABLE tasks ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS task_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    taskId INTEGER NOT NULL,
                    taskName TEXT NOT NULL,
                    date TEXT NOT NULL,
                    eventType TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    oldStartTime TEXT NOT NULL,
                    oldEndTime TEXT NOT NULL,
                    newStartTime TEXT NOT NULL,
                    newEndTime TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    focusScoreSnapshot INTEGER NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS focus_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    taskId INTEGER NOT NULL,
                    taskName TEXT NOT NULL,
                    date TEXT NOT NULL,
                    startTimestamp INTEGER NOT NULL,
                    endTimestamp INTEGER,
                    plannedDurationMinutes INTEGER NOT NULL,
                    actualDurationMinutes INTEGER NOT NULL,
                    result TEXT NOT NULL,
                    interruptionCount INTEGER NOT NULL,
                    usedResetProtocol INTEGER NOT NULL,
                    cooldownUsed INTEGER NOT NULL,
                    punishmentTriggered INTEGER NOT NULL,
                    completed INTEGER NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS daily_checkins (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    energyScore INTEGER NOT NULL,
                    stressScore INTEGER NOT NULL,
                    moodWord TEXT NOT NULL,
                    userType TEXT NOT NULL,
                    mode TEXT NOT NULL,
                    source TEXT NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS config_change_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    configType TEXT NOT NULL,
                    oldValueJson TEXT NOT NULL,
                    newValueJson TEXT NOT NULL,
                    sourceScreen TEXT NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_state_snapshot (
                    key TEXT NOT NULL PRIMARY KEY,
                    uid TEXT NOT NULL,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    photoUrl TEXT NOT NULL,
                    language TEXT NOT NULL,
                    userType TEXT NOT NULL,
                    secondaryType TEXT NOT NULL,
                    appMode TEXT NOT NULL,
                    totalXp INTEGER NOT NULL,
                    currentIdentityLevel INTEGER NOT NULL,
                    streakCount INTEGER NOT NULL,
                    totalCompleted INTEGER NOT NULL,
                    totalSkipped INTEGER NOT NULL,
                    lastCompleteDate TEXT NOT NULL,
                    challengeActive INTEGER NOT NULL,
                    challengeDaysCompleted INTEGER NOT NULL,
                    ironStatusUnlocked INTEGER NOT NULL,
                    crucibleActive INTEGER NOT NULL,
                    activeCrucibleId TEXT NOT NULL,
                    bossModeActive INTEGER NOT NULL,
                    detoxEnabled INTEGER NOT NULL,
                    detoxStartHour INTEGER NOT NULL,
                    detoxEndHour INTEGER NOT NULL,
                    blockedAppsSerialized TEXT NOT NULL,
                    blockedWebsitesSerialized TEXT NOT NULL,
                    emergencyAppsSerialized TEXT NOT NULL,
                    identityStatementsSerialized TEXT NOT NULL,
                    routineType TEXT NOT NULL,
                    emergencyValveTokens INTEGER NOT NULL,
                    emergencyValveCooldownUntil INTEGER NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS challenge_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    runId TEXT NOT NULL,
                    eventType TEXT NOT NULL,
                    dayNumber INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    routineType TEXT NOT NULL,
                    details TEXT NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS challenge_day_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    runId TEXT NOT NULL,
                    date TEXT NOT NULL,
                    dayNumber INTEGER NOT NULL,
                    completedAt INTEGER NOT NULL,
                    routineType TEXT NOT NULL,
                    mathSolved INTEGER NOT NULL,
                    completed INTEGER NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS crucible_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    runId TEXT NOT NULL,
                    crucibleId TEXT NOT NULL,
                    crucibleTitle TEXT NOT NULL,
                    eventType TEXT NOT NULL,
                    dayNumber INTEGER NOT NULL,
                    completedDaysSnapshot INTEGER NOT NULL,
                    totalDays INTEGER NOT NULL,
                    penaltyType TEXT NOT NULL,
                    sessionDurationMinutes INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    details TEXT NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS crucible_day_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    runId TEXT NOT NULL,
                    crucibleId TEXT NOT NULL,
                    crucibleTitle TEXT NOT NULL,
                    date TEXT NOT NULL,
                    dayNumber INTEGER NOT NULL,
                    completedAt INTEGER NOT NULL,
                    sessionDurationMinutes INTEGER NOT NULL,
                    penaltyType TEXT NOT NULL,
                    completed INTEGER NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS club_chat_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    eventType TEXT NOT NULL,
                    membershipTier TEXT NOT NULL,
                    challengeDaysSnapshot INTEGER NOT NULL,
                    streakBand TEXT NOT NULL,
                    postCountDelta INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    details TEXT NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS saved_task_blueprints (
                    id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    lastModified INTEGER NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS saved_task_blueprint_missions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    blueprintId TEXT NOT NULL,
                    missionOrder INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    startTime TEXT NOT NULL,
                    endTime TEXT NOT NULL,
                    importanceRank INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    packageName TEXT,
                    FOREIGN KEY(blueprintId) REFERENCES saved_task_blueprints(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )

            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_saved_task_blueprint_missions_blueprintId ON saved_task_blueprint_missions(blueprintId)"
            )
        }
    }

    val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN parentMissionId TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE tasks ADD COLUMN segmentBaseName TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE tasks ADD COLUMN segmentIndex INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE tasks ADD COLUMN segmentCount INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE tasks ADD COLUMN isBreakSegment INTEGER NOT NULL DEFAULT 0")

            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN parentMissionId TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN segmentBaseName TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN segmentIndex INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN segmentCount INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN isBreakSegment INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_classifications (
                    packageName TEXT NOT NULL PRIMARY KEY,
                    appName TEXT NOT NULL,
                    category TEXT NOT NULL,
                    source TEXT NOT NULL,
                    confidence TEXT NOT NULL,
                    lastDecisionSource TEXT NOT NULL,
                    isSystemApp INTEGER NOT NULL,
                    lastSeenAt INTEGER NOT NULL,
                    lastPromptedAt INTEGER NOT NULL,
                    timesPrompted INTEGER NOT NULL,
                    timesOpenedDuringFocus INTEGER NOT NULL,
                    timesOpenedOverall INTEGER NOT NULL,
                    isLockedByUser INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_app_classifications_category ON app_classifications(category)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_app_classifications_source ON app_classifications(source)"
            )
        }
    }

    val MIGRATION_21_22 = object : Migration(21, 22) {
        override fun migrate(database: SupportSQLiteDatabase) {
            if (!database.hasColumn("app_classifications", "lastDecisionSource")) {
                database.execSQL(
                    "ALTER TABLE app_classifications ADD COLUMN lastDecisionSource TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }

    val MIGRATION_22_23 = object : Migration(22, 23) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS morning_launch_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    alarmDismissedAt INTEGER NOT NULL,
                    mode TEXT NOT NULL,
                    outcome TEXT NOT NULL,
                    source TEXT NOT NULL,
                    startedAt INTEGER NOT NULL,
                    completedAt INTEGER NOT NULL,
                    currentStepIndex INTEGER NOT NULL,
                    stepsCompleted INTEGER NOT NULL,
                    delayCount INTEGER NOT NULL,
                    totalDurationMs INTEGER NOT NULL,
                    details TEXT NOT NULL,
                    lastModified INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_morning_launch_sessions_date ON morning_launch_sessions(date)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_morning_launch_sessions_outcome ON morning_launch_sessions(outcome)"
            )
        }
    }

    val MIGRATION_23_24 = object : Migration(23, 24) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS morning_launch_step_results (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    sessionId INTEGER NOT NULL,
                    mode TEXT NOT NULL,
                    stepId TEXT NOT NULL,
                    stepOrder INTEGER NOT NULL,
                    targetValue INTEGER NOT NULL,
                    actualValue INTEGER NOT NULL,
                    unit TEXT NOT NULL,
                    completed INTEGER NOT NULL,
                    skipped INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    completedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_morning_launch_step_results_sessionId ON morning_launch_step_results(sessionId)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_morning_launch_step_results_mode ON morning_launch_step_results(mode)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_morning_launch_step_results_stepId ON morning_launch_step_results(stepId)"
            )
        }
    }

    val MIGRATION_24_25 = object : Migration(24, 25) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS daily_integrity_records (
                    date TEXT NOT NULL PRIMARY KEY,
                    plannedCount INTEGER NOT NULL,
                    completedCount INTEGER NOT NULL,
                    scorePercent INTEGER NOT NULL,
                    finalizedAt INTEGER NOT NULL,
                    modeAtFinalization TEXT NOT NULL,
                    strictnessEscalatedForNextDay INTEGER NOT NULL,
                    source TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_25_26 = object : Migration(25, 26) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN taskType TEXT NOT NULL DEFAULT 'OTHER'")
            database.execSQL("ALTER TABLE tasks ADD COLUMN focusModeEnabled INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE tasks ADD COLUMN focusPreset TEXT NOT NULL DEFAULT 'CLASSIC_25_5'")
            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN taskType TEXT NOT NULL DEFAULT 'OTHER'")
            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN focusModeEnabled INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE saved_task_blueprint_missions ADD COLUMN focusPreset TEXT NOT NULL DEFAULT 'CLASSIC_25_5'")
        }
    }

    val MIGRATION_26_27 = object : Migration(26, 27) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE voice_logs ADD COLUMN entryType TEXT NOT NULL DEFAULT 'GENERAL'")
            database.execSQL("ALTER TABLE voice_logs ADD COLUMN promptType TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE voice_logs ADD COLUMN clarityScore INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE voice_logs ADD COLUMN confidenceScore INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE voice_logs ADD COLUMN playbackCompleted INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_27_28 = object : Migration(27, 28) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS confidence_ignition_entries (
                    date TEXT NOT NULL PRIMARY KEY,
                    ignitionLineId TEXT NOT NULL,
                    voicePromptId TEXT NOT NULL,
                    movePromptId TEXT NOT NULL,
                    couragePromptId TEXT NOT NULL,
                    breathDone INTEGER NOT NULL,
                    voiceRecorded INTEGER NOT NULL,
                    moveDone INTEGER NOT NULL,
                    courageAccepted INTEGER NOT NULL,
                    completed INTEGER NOT NULL,
                    completedAt INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_confidence_ignition_entries_completed ON confidence_ignition_entries(completed)"
            )
        }
    }
    val MIGRATION_28_29 = object : Migration(28, 29) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE daily_integrity_records ADD COLUMN missCount INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE daily_integrity_records ADD COLUMN shieldState INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE daily_integrity_records ADD COLUMN sleepHoursPulled REAL NOT NULL DEFAULT -1.0")
            database.execSQL("ALTER TABLE daily_integrity_records ADD COLUMN reflectionNote TEXT")
        }
    }
}
