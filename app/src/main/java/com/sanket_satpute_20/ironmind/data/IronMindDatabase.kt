package com.sanket_satpute_20.ironmind.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Task::class, TemptationLog::class, VoiceLog::class, EmergencyValveEvent::class, TaskEvent::class, FocusSession::class, DailyCheckIn::class, ConfigChangeEvent::class, UserStateSnapshot::class, ChallengeEvent::class, ChallengeDayRecord::class, CrucibleEvent::class, CrucibleDayRecord::class, ClubChatEvent::class, SavedTaskBlueprint::class, SavedTaskBlueprintMission::class, AppClassification::class, MorningLaunchSession::class, MorningLaunchStepResult::class, DailyIntegrityRecord::class, ConfidenceIgnitionEntry::class],
    version = 29,
    exportSchema = false
)
abstract class IronMindDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun temptationLogDao(): TemptationLogDao
    abstract fun voiceLogDao(): VoiceLogDao
    abstract fun emergencyValveEventDao(): EmergencyValveEventDao
    abstract fun taskEventDao(): TaskEventDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun configChangeEventDao(): ConfigChangeEventDao
    abstract fun userStateSnapshotDao(): UserStateSnapshotDao
    abstract fun challengeEventDao(): ChallengeEventDao
    abstract fun challengeDayRecordDao(): ChallengeDayRecordDao
    abstract fun crucibleEventDao(): CrucibleEventDao
    abstract fun crucibleDayRecordDao(): CrucibleDayRecordDao
    abstract fun clubChatEventDao(): ClubChatEventDao
    abstract fun savedTaskBlueprintDao(): SavedTaskBlueprintDao
    abstract fun appClassificationDao(): AppClassificationDao
    abstract fun morningLaunchSessionDao(): MorningLaunchSessionDao
    abstract fun morningLaunchStepResultDao(): MorningLaunchStepResultDao
    abstract fun dailyIntegrityRecordDao(): DailyIntegrityRecordDao
    abstract fun confidenceIgnitionDao(): ConfidenceIgnitionDao

    companion object {
        @Volatile private var INSTANCE: IronMindDatabase? = null

        fun getDatabase(context: Context): IronMindDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IronMindDatabase::class.java,
                    "ironmind_database"
                )
                .addMigrations(
                    DatabaseMigrations.MIGRATION_12_13,
                    DatabaseMigrations.MIGRATION_13_14,
                    DatabaseMigrations.MIGRATION_14_15,
                    DatabaseMigrations.MIGRATION_15_16,
                    DatabaseMigrations.MIGRATION_16_17,
                    DatabaseMigrations.MIGRATION_17_18,
                    DatabaseMigrations.MIGRATION_18_19,
                    DatabaseMigrations.MIGRATION_19_20,
                    DatabaseMigrations.MIGRATION_20_21,
                    DatabaseMigrations.MIGRATION_21_22,
                    DatabaseMigrations.MIGRATION_22_23,
                    DatabaseMigrations.MIGRATION_23_24,
                    DatabaseMigrations.MIGRATION_24_25,
                    DatabaseMigrations.MIGRATION_25_26,
                    DatabaseMigrations.MIGRATION_26_27,
                    DatabaseMigrations.MIGRATION_27_28,
                    DatabaseMigrations.MIGRATION_28_29
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
