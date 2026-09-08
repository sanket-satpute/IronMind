package com.sanket_satpute_20.ironmind.failure

import com.sanket_satpute_20.ironmind.data.AppClassificationDao
import com.sanket_satpute_20.ironmind.data.ChallengeDayRecordDao
import com.sanket_satpute_20.ironmind.data.ChallengeEventDao
import com.sanket_satpute_20.ironmind.data.ClubChatEventDao
import com.sanket_satpute_20.ironmind.data.ConfidenceIgnitionDao
import com.sanket_satpute_20.ironmind.data.ConfigChangeEventDao
import com.sanket_satpute_20.ironmind.data.CrucibleDayRecordDao
import com.sanket_satpute_20.ironmind.data.CrucibleEventDao
import com.sanket_satpute_20.ironmind.data.DailyCheckInDao
import com.sanket_satpute_20.ironmind.data.DailyIntegrityRecordDao
import com.sanket_satpute_20.ironmind.data.EmergencyValveEventDao
import com.sanket_satpute_20.ironmind.data.FocusSessionDao
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.MorningLaunchSessionDao
import com.sanket_satpute_20.ironmind.data.MorningLaunchStepResultDao
import com.sanket_satpute_20.ironmind.data.SavedTaskBlueprintDao
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.data.TaskDao
import com.sanket_satpute_20.ironmind.data.TaskEventDao
import com.sanket_satpute_20.ironmind.data.TemptationLogDao
import com.sanket_satpute_20.ironmind.data.UserStateSnapshotDao
import com.sanket_satpute_20.ironmind.data.VoiceLogDao
import com.sanket_satpute_20.ironmind.mission.MissionExecutionResult
import com.sanket_satpute_20.ironmind.mission.MissionExecutor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import java.lang.reflect.Proxy

class RecoveryExecutionServiceTest {

    @Test
    fun `TEST 1 successful retry completes recovery`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val task = Task(id = 1, isSkipped = true)
        val evidence = FailureEvidence(1, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val action = RecoveryAction.RetryMission(1)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, action))

        fakeExecutor.resultToReturn = MissionExecutionResult.Started(task, 100L)

        val result = service.execute(action)

        assertTrue(result is RecoveryExecutionResult.RetryStarted)
        val retryStarted = result as RecoveryExecutionResult.RetryStarted
        assertEquals(1, retryStarted.taskId)
        assertEquals(1, fakeExecutor.callCount)
        assertEquals(1, fakeExecutor.lastTaskId)
        assertEquals("RECOVERY_RETRY", fakeExecutor.lastRetryReason)

        val currentState = service.currentRecovery()
        assertTrue(currentState is RecoveryState.Completed)
    }

    @Test
    fun `TEST 2 retry InvalidWindow leaves recovery Required`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val task = Task(id = 1, isSkipped = true)
        val evidence = FailureEvidence(1, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val action = RecoveryAction.RetryMission(1)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, action))

        fakeExecutor.resultToReturn = MissionExecutionResult.InvalidWindow(task, "Too late")

        val result = service.execute(action)

        assertTrue(result is RecoveryExecutionResult.InvalidAction)
        assertEquals(1, fakeExecutor.callCount)

        val currentState = service.currentRecovery()
        assertTrue(currentState is RecoveryState.Required)
    }

    @Test
    fun `TEST 3 retry Failed leaves recovery Required`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val evidence = FailureEvidence(1, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val action = RecoveryAction.RetryMission(1)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, action))

        fakeExecutor.resultToReturn = MissionExecutionResult.Failed("Crash")

        val result = service.execute(action)

        assertTrue(result is RecoveryExecutionResult.Failed)
        assertEquals(1, fakeExecutor.callCount)

        val currentState = service.currentRecovery()
        assertTrue(currentState is RecoveryState.Required)
    }

    @Test
    fun `TEST 4 wrong requested action rejects and leaves recovery Required`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val evidence = FailureEvidence(1, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val actionRecommended = RecoveryAction.RetryMission(1)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, actionRecommended))

        val actionRequested = RecoveryAction.RescheduleMission(1)

        val result = service.execute(actionRequested)

        assertTrue(result is RecoveryExecutionResult.InvalidAction)
        assertEquals(0, fakeExecutor.callCount)

        val currentState = service.currentRecovery()
        assertTrue(currentState is RecoveryState.Required)
    }

    @Test
    fun `TEST 5 missing task leaves recovery Required`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val evidence = FailureEvidence(1, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val action = RecoveryAction.RetryMission(1)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, action))

        fakeExecutor.resultToReturn = MissionExecutionResult.MissingTask(1)

        val result = service.execute(action)

        assertTrue(result is RecoveryExecutionResult.InvalidAction)
        assertEquals(1, fakeExecutor.callCount)

        val currentState = service.currentRecovery()
        assertTrue(currentState is RecoveryState.Required)
    }

    @Test
    fun `TEST 6 already active task leaves recovery Required`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val task = Task(id = 1, isInProgress = true)
        val evidence = FailureEvidence(1, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val action = RecoveryAction.RetryMission(1)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, action))

        fakeExecutor.resultToReturn = MissionExecutionResult.AlreadyActive(task)

        val result = service.execute(action)

        assertTrue(result is RecoveryExecutionResult.InvalidAction)
        assertEquals(1, fakeExecutor.callCount)

        val currentState = service.currentRecovery()
        assertTrue(currentState is RecoveryState.Required)
    }

    @Test
    fun `TEST 7 verify exact taskId and retryReason passed to MissionExecutor`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val task = Task(id = 42, isSkipped = true)
        val evidence = FailureEvidence(42, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val action = RecoveryAction.RetryMission(42)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, action))

        fakeExecutor.resultToReturn = MissionExecutionResult.Started(task, 100L)

        service.execute(action)

        assertEquals(42, fakeExecutor.lastTaskId)
        assertEquals("RECOVERY_RETRY", fakeExecutor.lastRetryReason)
    }

    @Test
    fun `TEST 8 verify MissionExecutor is called exactly once for a valid retry request`() = runBlocking {
        val (service, fakeExecutor, fakeRecoveryRepo) = setupTestEnv()
        val task = Task(id = 99, isSkipped = true)
        val evidence = FailureEvidence(99, "TEST", "2023-01-01", FailureType.USER_SKIPPED, 0L)
        val action = RecoveryAction.RetryMission(99)
        fakeRecoveryRepo.setRequired(RecoveryState.Required(evidence, action))

        fakeExecutor.resultToReturn = MissionExecutionResult.Started(task, 100L)

        service.execute(action)

        assertEquals(1, fakeExecutor.callCount)
    }

    private fun setupTestEnv(): Triple<RecoveryExecutionService, FakeMissionExecutor, FakeRecoveryRepository> {
        val fakeFailureRepo = FakeFailureRepository()
        val fakeRecoveryRepo = FakeRecoveryRepository()
        val failureService = FailureService(fakeFailureRepo, fakeRecoveryRepo)
        val fakeExecutor = FakeMissionExecutor()
        val fakeDb = FakeIronMindDatabase()

        val service = RecoveryExecutionService(
            failureService = failureService,
            missionExecutor = fakeExecutor,
            db = fakeDb
        )
        return Triple(service, fakeExecutor, fakeRecoveryRepo)
    }
}

class FakeMissionExecutor : MissionExecutor {
    var callCount = 0
    var lastTaskId = -1
    var lastRetryReason = ""
    var resultToReturn: MissionExecutionResult = MissionExecutionResult.Failed("Fake not setup")

    override suspend fun retryMission(taskId: Int, retryReason: String): MissionExecutionResult {
        callCount++
        lastTaskId = taskId
        lastRetryReason = retryReason
        return resultToReturn
    }
}

class FakeFailureRepository : FailureRepository {
    override fun getLatest(): FailureEvidence? = null
    override fun countFailures(taskId: Int, date: String): Int = 0
    override fun contains(taskId: Int, failureType: FailureType, timestamp: Long): Boolean = false
    override fun record(evidence: FailureEvidence) {}
}

class FakeRecoveryRepository : RecoveryRepository {
    private var state: RecoveryState = RecoveryState.None

    override fun getCurrent(): RecoveryState = state

    override fun setRequired(state: RecoveryState.Required) {
        this.state = state
    }

    override fun markCompleted(action: RecoveryAction, completedAt: Long) {
        this.state = RecoveryState.Completed(action, completedAt)
    }

    override fun clear() {
        state = RecoveryState.None
    }
}

class FakeIronMindDatabase : IronMindDatabase() {
    override fun taskDao(): TaskDao {
        return Proxy.newProxyInstance(
            TaskDao::class.java.classLoader,
            arrayOf(TaskDao::class.java)
        ) { _, method, args ->
            if (method.name == "getTaskById") {
                val taskId = args?.get(0) as Int
                Task(id = taskId, isSkipped = true)
            } else if (method.name == "equals") {
                this === args?.get(0)
            } else if (method.name == "hashCode") {
                System.identityHashCode(this)
            } else if (method.name == "toString") {
                "FakeTaskDao"
            } else {
                throw NotImplementedError("FakeTaskDao.${method.name} is not implemented")
            }
        } as TaskDao
    }

    override fun temptationLogDao(): TemptationLogDao = throw NotImplementedError()
    override fun voiceLogDao(): VoiceLogDao = throw NotImplementedError()
    override fun emergencyValveEventDao(): EmergencyValveEventDao = throw NotImplementedError()
    override fun taskEventDao(): TaskEventDao = throw NotImplementedError()
    override fun focusSessionDao(): FocusSessionDao = throw NotImplementedError()
    override fun dailyCheckInDao(): DailyCheckInDao = throw NotImplementedError()
    override fun configChangeEventDao(): ConfigChangeEventDao = throw NotImplementedError()
    override fun userStateSnapshotDao(): UserStateSnapshotDao = throw NotImplementedError()
    override fun challengeEventDao(): ChallengeEventDao = throw NotImplementedError()
    override fun challengeDayRecordDao(): ChallengeDayRecordDao = throw NotImplementedError()
    override fun crucibleEventDao(): CrucibleEventDao = throw NotImplementedError()
    override fun crucibleDayRecordDao(): CrucibleDayRecordDao = throw NotImplementedError()
    override fun clubChatEventDao(): ClubChatEventDao = throw NotImplementedError()
    override fun savedTaskBlueprintDao(): SavedTaskBlueprintDao = throw NotImplementedError()
    override fun appClassificationDao(): AppClassificationDao = throw NotImplementedError()
    override fun morningLaunchSessionDao(): MorningLaunchSessionDao = throw NotImplementedError()
    override fun morningLaunchStepResultDao(): MorningLaunchStepResultDao = throw NotImplementedError()
    override fun dailyIntegrityRecordDao(): DailyIntegrityRecordDao = throw NotImplementedError()
    override fun confidenceIgnitionDao(): ConfidenceIgnitionDao = throw NotImplementedError()

    override fun clearAllTables() {}
    override fun createInvalidationTracker(): InvalidationTracker = throw NotImplementedError()
    override fun createOpenHelper(config: androidx.room.DatabaseConfiguration): SupportSQLiteOpenHelper = throw NotImplementedError()
}
