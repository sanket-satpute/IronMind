package com.sanket_satpute_20.ironmind.blocker

import android.accessibilityservice.AccessibilityService
import android.content.*
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.apps.AppCategory
import com.sanket_satpute_20.ironmind.apps.AppClassificationRepository
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.punishment.PunishmentActivity
import com.sanket_satpute_20.ironmind.onboarding.FocusBreachActivity
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.psychology.AppMode
import com.sanket_satpute_20.ironmind.crucible.CrucibleResult
import com.sanket_satpute_20.ironmind.crucible.CrucibleManager
import com.sanket_satpute_20.ironmind.crucible.CrucibleFailActivity
import com.sanket_satpute_20.ironmind.crucible.CruciblePenalty
import com.sanket_satpute_20.ironmind.crucible.CruciblePenaltyEngine
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.TaskEvent
import com.sanket_satpute_20.ironmind.focus.WorkLockActivity
import com.sanket_satpute_20.ironmind.focus.WorkLockStage
import com.sanket_satpute_20.ironmind.focus.EarnedUnlockManager
import com.sanket_satpute_20.ironmind.focus.PomodoroEngine
import com.sanket_satpute_20.ironmind.focus.PomodoroPhase
import com.sanket_satpute_20.ironmind.focus.PomodoroSessionSource
import com.sanket_satpute_20.ironmind.focus.PomodoroChamberActivity
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchActivity
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchStage
import com.sanket_satpute_20.ironmind.settings.getDefaultDialerPackage
import com.sanket_satpute_20.ironmind.settings.getDefaultSmsPackage
import com.sanket_satpute_20.ironmind.sleeplock.SleepLockActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class IronMindAccessibilityService : AccessibilityService() {

    private val TAG = "IronMind_Guard"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var classificationRepository: AppClassificationRepository
    
    private var blockedApps: Set<String> = emptySet()
    private var blockedWebsites: Set<String> = emptySet()
    private var contextApps: Set<String> = emptySet()
    private var activeMissionContextApps: Set<String> = emptySet()
    private var lastCacheLoadTime = 0L
    private var isFreedomActive = false
    private var isDetoxActive = false
    private var isCrucibleActive = false
    private var isSleepLockActive = false
    private var sleepLockEmergencyApps: Set<String> = emptySet()
    private var isMorningLaunchActive = false
    private var morningLaunchStage = MorningLaunchStage.IDLE
    private var isEarnedUnlockActive = false
    private var isEarnedUnlockUnlockedToday = false
    private var earnedUnlockDate = ""
    private var isWorkLockActive = false
    private var workLockStage = WorkLockStage.IDLE
    private var isPomodoroActive = false
    private var pomodoroTaskId = -1
    private var pomodoroPhase = PomodoroPhase.IDLE
    private var pomodoroSource = PomodoroSessionSource.TASK

    private val BROWSER_PACKAGES = setOf(
        "com.android.chrome", "com.brave.browser", "org.mozilla.firefox", 
        "com.opera.browser", "com.sec.android.app.sbrowser", "com.microsoft.emmx"
    )

    // Critical apps that should NEVER be blocked
    private val SYSTEM_WHITELIST = setOf(
        "com.android.dialer", "com.google.android.dialer", "com.android.phone", 
        "com.android.settings", "com.android.systemui", "com.sanket_satpute_20.ironmind"
    )

    private var lastBreachTime = 0L
    private var lastBreachItem = ""
    private val temporaryWhitelist = mutableMapOf<String, Long>()

    // Crucible grace: warn on first breach, fail on second within CRUCIBLE_GRACE_MS
    private var crucibleWarningTime = 0L
    private var crucibleWarningPkg = ""
    private val CRUCIBLE_GRACE_MS = 3000L

    private val reloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.ironmind.RELOAD_GUARD" -> loadCache()
                "com.ironmind.FORCE_FREEDOM" -> {
                    isFreedomActive = true
                    Log.d(TAG, "MISSION ACCOMPLISHED: Freedom Granted.")
                }
                "com.ironmind.WHITELIST_PACKAGE" -> {
                    val pkg = intent.getStringExtra("package_name")
                    if (pkg != null) {
                        // Whitelist for 5 minutes
                        temporaryWhitelist[pkg] = System.currentTimeMillis() + (5 * 60 * 1000)
                        Log.d(TAG, "Temporarily whitelisted $pkg after Nudge yield")
                    }
                }
                ACTION_SLEEP_LOCK_SCREEN_TIMEOUT_LOCK -> {
                    if (isSleepLockActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                    }
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isAlive = true
        classificationRepository = AppClassificationRepository.getInstance(applicationContext)
        loadCache()
        val filter = IntentFilter().apply {
            addAction("com.ironmind.RELOAD_GUARD")
            addAction("com.ironmind.FORCE_FREEDOM")
            addAction("com.ironmind.WHITELIST_PACKAGE")
            addAction(ACTION_SLEEP_LOCK_SCREEN_TIMEOUT_LOCK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(reloadReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(reloadReceiver, filter)
        }
        Log.d(TAG, "IronMind Focus Guard: ARMED")
    }
    
    private var isRecoveryDayActive: Boolean = false

    private fun loadCache() {
        val pref = PrefManager.getInstance(this)
        val runtimePolicy = runBlockingOrNull { classificationRepository.getRuntimePolicySnapshot() }
        blockedApps = linkedSetOf<String>().apply {
            addAll(pref.blockedApps)
            addAll(runtimePolicy?.blockedPackages.orEmpty())
        }
        blockedWebsites = HashSet(pref.blockedWebsites)
        activeMissionContextApps = HashSet(pref.activeMissionContextApps)
        isDetoxActive = pref.detoxCurrentlyActive
        isCrucibleActive = pref.crucibleActive
        isSleepLockActive = pref.sleepLockActive
        isMorningLaunchActive = pref.morningLaunchActive
        morningLaunchStage = MorningLaunchStage.fromWireValue(pref.morningLaunchStage)
        isEarnedUnlockActive = pref.earnedUnlockActive
        isEarnedUnlockUnlockedToday = pref.earnedUnlockUnlockedToday
        earnedUnlockDate = pref.earnedUnlockDate
        isWorkLockActive = pref.workLockActive
        workLockStage = WorkLockStage.fromWireValue(pref.workLockStage)
        isPomodoroActive = pref.pomodoroActive
        pomodoroTaskId = pref.pomodoroTaskId
        pomodoroPhase = PomodoroPhase.fromWireValue(pref.pomodoroPhase)
        pomodoroSource = PomodoroSessionSource.fromWireValue(pref.pomodoroSource)
        sleepLockEmergencyApps = linkedSetOf<String>().apply {
            add(getDefaultDialerPackage(this@IronMindAccessibilityService))
            add(getDefaultSmsPackage(this@IronMindAccessibilityService))
            add(packageName)
            addAll(pref.sleepLockEmergencyApps)
        }
        contextApps = runtimePolicy?.contextPackages.orEmpty()
        
        if (pref.activeTaskDate != LocalDate.now().toString()) {
            isFreedomActive = false
        }
        isRecoveryDayActive = pref.isRecoveryDayActive
        lastCacheLoadTime = System.currentTimeMillis()
        Log.d(
            TAG,
            "Cache Synced. Detox: $isDetoxActive, Crucible: $isCrucibleActive, Blocking ${blockedApps.size} apps, Context pool ${contextApps.size}, Allowed context ${activeMissionContextApps.size}."
        )
    }

    private fun loadCacheIfStale() {
        if (System.currentTimeMillis() - lastCacheLoadTime > 5000) loadCache()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        loadCacheIfStale()

        if (isFreedomActive || isRecoveryDayActive) return 

        val pkg = event.packageName?.toString() ?: return
        if (pkg in SYSTEM_WHITELIST) return

        val isWindowChange = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        val isContentChange = event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        
        if (!isWindowChange && !isContentChange) return

        val now = System.currentTimeMillis()
        if (isWindowChange) {
            scope.launch {
                classificationRepository.recordAppSeen(applicationContext, pkg, isTaskActive())
            }
        }

        // 1. Sleep Lock has priority once active. Only emergency apps should survive it.
        if (isSleepLockActive) {
            if (pkg !in sleepLockEmergencyApps) {
                handleSleepLockBreach(pkg, now)
                return
            }
        }

        // 2. Check Crucible (Highest Priority during normal day enforcement)
        if (isMorningLaunchActive && morningLaunchStage != MorningLaunchStage.IDLE) {
            if (pkg != packageName) {
                handleMorningLaunchBreach(pkg, now)
                return
            }
        }

        // 3. Work Lock sits above Crucible and normal mission blocking.
        if (isWorkLockActive && workLockStage == WorkLockStage.ACTIVE) {
            if (pkg != packageName) {
                handleWorkLockAccess(pkg, now)
                return
            }
        }

        // 3.5 Home Pomodoro uses its own chamber but the same break/work discipline.
        if (isHomePomodoroSessionActive()) {
            if (pkg != packageName) {
                handleHomePomodoroAccess(pkg, now)
                return
            }
        }

        // 4. Check Crucible (Highest Priority during normal day enforcement)
        if (isCrucibleActive) {
            handleCrucibleBreach(pkg, now)
            return
        }

        // 5. Check Phone Detox
        if (isDetoxActive) {
            handleBreach(pkg, now, isDetox = true)
            return
        }

        // 6. Check Earned Unlock sealed state for blocked apps all day.
        if (isEarnedUnlockSealedToday() && pkg in blockedApps) {
            handleEarnedUnlockBreach(pkg, now)
            return
        }

        // 6.5. Check Temporary Whitelist (The Nudge yield)
        val whitelistExpiry = temporaryWhitelist[pkg]
        if (whitelistExpiry != null) {
            if (now < whitelistExpiry) {
                return // Allowed to use it temporarily
            } else {
                temporaryWhitelist.remove(pkg) // Expired
            }
        }

        // 7. Check Active Mission
        val taskActive = isTaskActive()
        if (taskActive) {
            if (pkg in blockedApps) {
                handleBreach(pkg, now, isDetox = false)
                return
            }

            if (pkg in contextApps && pkg !in activeMissionContextApps) {
                handleBreach(pkg, now, isDetox = false)
                return
            }

            if (isContentChange && pkg in BROWSER_PACKAGES) {
                val url = extractUrl(rootInActiveWindow) ?: extractUrl(event.source)
                if (url != null) {
                    val cleanUrl = url.lowercase()
                    if (blockedWebsites.any { cleanUrl.contains(it.lowercase()) }) {
                        handleBreach(url, now, isDetox = false)
                    }
                }
            }
        }
    }

    private fun handleCrucibleBreach(pkg: String, now: Long) {
        if (pkg == packageName) return // Don't fail if we are in our own app

        // Grace period: first breach in a window shows a warning, second breach fails
        if (now - crucibleWarningTime < CRUCIBLE_GRACE_MS && crucibleWarningPkg.isNotEmpty()) {
            // Second breach within grace window — actually fail the crucible
            crucibleWarningTime = 0L
            crucibleWarningPkg = ""
            executeCrucibleFail(pkg, now)
        } else {
            // First breach — warn the user and give them 3 seconds to return
            crucibleWarningTime = now
            crucibleWarningPkg = pkg
            performGlobalAction(GLOBAL_ACTION_HOME)
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SHOW_IDENTITY_FLASH", true)
                putExtra("BLOCKED_IDENTIFIER", "⚠️ CRUCIBLE WARNING: Return to IronMind within 3 seconds or your run fails!")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            Log.w(TAG, "CRUCIBLE WARNING: $pkg opened during crucible. Grace period active.")
        }
    }

    private fun executeCrucibleFail(pkg: String, now: Long) {
        val prefs = PrefManager.getInstance(this)
        val crucibleManager = CrucibleManager(prefs)
        val activeCrucible = crucibleManager.getActiveCrucible()
        val runId = crucibleManager.getCurrentRunId()
        val completedDays = prefs.crucibleCompletedDates.size
        val failedCrucible = crucibleManager.failActiveCrucibleRun()
        
        prefs.crucibleActive = false
        isCrucibleActive = false
        prefs.crucibleResult = CrucibleResult.FAILED.name
        prefs.crucibleTotalFailed++
        CruciblePenaltyEngine.applyPenalty(
            applicationContext,
            prefs,
            runCatching { CruciblePenalty.valueOf(prefs.cruciblePenalty) }
                .getOrDefault(CruciblePenalty.STREAK_RESET)
        )

        if (activeCrucible != null && runId.isNotBlank()) {
            HistoryRecorder.recordCrucibleEvent(
                context = applicationContext,
                runId = runId,
                crucibleId = activeCrucible.id,
                crucibleTitle = activeCrucible.title,
                eventType = "FAILED",
                dayNumber = completedDays + 1,
                completedDaysSnapshot = completedDays,
                totalDays = activeCrucible.durationDays,
                penaltyType = prefs.cruciblePenalty,
                sessionDurationMinutes = activeCrucible.sessionDurationMinutes,
                details = "Accessibility breach: $pkg"
            )
        }
        
        performGlobalAction(GLOBAL_ACTION_HOME)
        
        val intent = Intent(this, CrucibleFailActivity::class.java).apply {
            putExtra("BREACH_ITEM", pkg)
            putExtra("CRUCIBLE_TITLE", failedCrucible?.title ?: prefs.crucibleTaskName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        
        Log.e(TAG, "CRUCIBLE BREACHED by $pkg. Session Terminated.")
    }

    private fun handleBreach(item: String, now: Long, isDetox: Boolean) {
        if (item == lastBreachItem && now - lastBreachTime < 3000) return
        lastBreachTime = now
        lastBreachItem = item

        val prefs = PrefManager.getInstance(this)
        
        // Log temptation early so shield cracks regardless of blocking action
        scope.launch(Dispatchers.IO) {
            val db = com.sanket_satpute_20.ironmind.data.IronMindDatabase.getDatabase(applicationContext)
            db.temptationLogDao().insertLog(
                com.sanket_satpute_20.ironmind.data.TemptationLog(
                    blockedAppName = item,
                    blockedPackage = item, // Store actual item (package or url)
                    duringTaskName = if (isDetox) "DETOX BLACKOUT" else prefs.activeTaskName,
                    timestamp = now,
                    date = LocalDate.now().toString()
                )
            )
            
            if (!prefs.hasExperiencedFirstBlock) {
                prefs.hasExperiencedFirstBlock = true
                com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logFirstBlockExperienced(item)
            }
        }

        val level = com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine.getLevelFromXp(prefs.totalXp)
        val mode = AdaptiveEngine.getCurrentMode(prefs)

        // Level 1: The Observer (Unless Detox)
        if (!isDetox && mode == AppMode.IRON && level <= 1) {
            // Temporarily whitelist for 1 minute to prevent log spam while they use the app
            temporaryWhitelist[item] = System.currentTimeMillis() + (60 * 1000)
            Log.d(TAG, "Level 1 (Observer) tracking breach for $item. Allowed entry.")
            return // Skip blocking
        }

        performGlobalAction(GLOBAL_ACTION_HOME)

        if (!isDetox && prefs.emergencyValveCooldownActive) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SHOW_IDENTITY_FLASH", true)
                putExtra("BLOCKED_IDENTIFIER", "Cooldown active. $item stays blocked. Regroup and return clean.")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            return
        }
        
        val intent = if (isDetox) {
            // Specialized Detox Screen (keep it harsh)
            Intent(this, FocusBreachActivity::class.java).apply {
                putExtra("BLOCKED_ITEM", "Phone Detox Active")
                putExtra("IS_DETOX", true)
            }
        } else {
            when (mode) {
                AppMode.IRON -> {
                    when (level) {
                        2 -> Intent(this, com.sanket_satpute_20.ironmind.integrity.NeutralBlockActivity::class.java).apply {
                            putExtra(com.sanket_satpute_20.ironmind.integrity.NeutralBlockActivity.EXTRA_BLOCKED_PACKAGE, item)
                        }
                        else -> Intent(this, PunishmentActivity::class.java) // Level 3+ IronMind
                    }
                }
                else -> Intent(this, com.sanket_satpute_20.ironmind.integrity.NeutralBlockActivity::class.java).apply {
                    putExtra(com.sanket_satpute_20.ironmind.integrity.NeutralBlockActivity.EXTRA_BLOCKED_PACKAGE, item)
                }
            }
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun handleSleepLockBreach(item: String, now: Long) {
        if (item == lastBreachItem && now - lastBreachTime < 2500) return
        lastBreachTime = now
        lastBreachItem = item

        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(SleepLockActivity.createIntent(this))

        scope.launch(Dispatchers.IO) {
            val db = com.sanket_satpute_20.ironmind.data.IronMindDatabase.getDatabase(applicationContext)
            db.temptationLogDao().insertLog(
                com.sanket_satpute_20.ironmind.data.TemptationLog(
                    blockedAppName = item,
                    blockedPackage = item,
                    duringTaskName = "SLEEP LOCK",
                    timestamp = now,
                    date = LocalDate.now().toString()
                )
            )
        }
        Log.d(TAG, "Sleep Lock redirected blocked package: $item")
    }

    private fun handleMorningLaunchBreach(item: String, now: Long) {
        if (item == lastBreachItem && now - lastBreachTime < 2500) return
        lastBreachTime = now
        lastBreachItem = item

        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(MorningLaunchActivity.createIntent(this))

        scope.launch(Dispatchers.IO) {
            val db = com.sanket_satpute_20.ironmind.data.IronMindDatabase.getDatabase(applicationContext)
            db.temptationLogDao().insertLog(
                com.sanket_satpute_20.ironmind.data.TemptationLog(
                    blockedAppName = item,
                    blockedPackage = item,
                    duringTaskName = "MORNING LAUNCH",
                    timestamp = now,
                    date = LocalDate.now().toString()
                )
            )
        }
        Log.d(TAG, "Morning Launch redirected blocked package: $item")
    }

    private fun handleWorkLockAccess(item: String, now: Long) {
        val category = runBlockingOrNull {
            classificationRepository.getClassification(item)?.category
        } ?: if (item in blockedApps) AppCategory.VOID else AppCategory.CONTEXT

        if (isPomodoroBreakOpen()) {
            if (category == AppCategory.SIGNAL) {
                Log.d(TAG, "Pomodoro break allowed signal app: $item")
                return
            }
            handleWorkLockBreach(item, now, category, isPomodoroWork = false)
            return
        }

        handleWorkLockBreach(item, now, category, isPomodoroWork = isPomodoroWorkPhase())
    }

    private fun handleWorkLockBreach(
        item: String,
        now: Long,
        category: AppCategory,
        isPomodoroWork: Boolean
    ) {
        if (item == lastBreachItem && now - lastBreachTime < 2500) return
        lastBreachTime = now
        lastBreachItem = item

        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(WorkLockActivity.createIntent(this))

        scope.launch(Dispatchers.IO) {
            val prefs = PrefManager.getInstance(applicationContext)
            val db = com.sanket_satpute_20.ironmind.data.IronMindDatabase.getDatabase(applicationContext)
            var pomodoroSuffix = ""
            if (isPomodoroWork && category == AppCategory.VOID && prefs.workLockTaskId == pomodoroTaskId) {
                val shouldReset = AdaptiveEngine.getCurrentMode(prefs) == AppMode.IRON
                PomodoroEngine(applicationContext).registerVoidBreachDuringWork(resetCurrentInterval = shouldReset)
                pomodoroSuffix = if (shouldReset) ":POMODORO_RESET" else ":POMODORO_BREACH"
            }
            db.temptationLogDao().insertLog(
                com.sanket_satpute_20.ironmind.data.TemptationLog(
                    blockedAppName = item,
                    blockedPackage = item,
                    duringTaskName = if (isPomodoroWork) "POMODORO WORK" else "WORK LOCK",
                    timestamp = now,
                    date = LocalDate.now().toString()
                )
            )
            val taskId = prefs.workLockTaskId
            if (taskId != -1) {
                val task = db.taskDao().getTaskById(taskId)
                if (task != null) {
                    db.taskEventDao().insert(
                        TaskEvent(
                            taskId = task.id,
                            taskName = task.name,
                            date = task.date,
                            eventType = "WORK_LOCK_BREACH",
                            timestamp = now,
                            oldStartTime = task.startTime,
                            oldEndTime = task.endTime,
                            newStartTime = task.startTime,
                            newEndTime = task.endTime,
                            focusScoreSnapshot = task.focusScore,
                            reason = "SPRING_PROTOCOL_BREACH:$item$pomodoroSuffix"
                        )
                    )
                    if (isPomodoroWork && category == AppCategory.VOID) {
                        db.taskEventDao().insert(
                            TaskEvent(
                                taskId = task.id,
                                taskName = task.name,
                                date = task.date,
                                eventType = if (pomodoroSuffix.contains("RESET")) "POMODORO_RESET" else "POMODORO_BREACH",
                                timestamp = now,
                                oldStartTime = task.startTime,
                                oldEndTime = task.endTime,
                                newStartTime = task.startTime,
                                newEndTime = task.endTime,
                                focusScoreSnapshot = task.focusScore,
                                reason = "POMODORO_VOID_BREACH:$item"
                            )
                        )
                    }
                }
            }
        }
        Log.d(TAG, "Work Lock intercepted blocked package: $item")
    }

    private fun isPomodoroBreakOpen(): Boolean {
        val prefs = PrefManager.getInstance(this)
        return isPomodoroActive &&
            pomodoroTaskId != -1 &&
            pomodoroTaskId == prefs.workLockTaskId &&
            (pomodoroPhase == PomodoroPhase.SHORT_BREAK || pomodoroPhase == PomodoroPhase.LONG_BREAK)
    }

    private fun isPomodoroWorkPhase(): Boolean {
        val prefs = PrefManager.getInstance(this)
        return isPomodoroActive &&
            pomodoroTaskId != -1 &&
            pomodoroTaskId == prefs.workLockTaskId &&
            pomodoroPhase == PomodoroPhase.WORK
    }

    private fun isHomePomodoroSessionActive(): Boolean {
        return isPomodoroActive &&
            pomodoroSource == PomodoroSessionSource.HOME &&
            pomodoroPhase != PomodoroPhase.COMPLETED &&
            pomodoroPhase != PomodoroPhase.BROKEN
    }

    private fun isHomePomodoroBreakOpen(): Boolean {
        return isHomePomodoroSessionActive() &&
            (pomodoroPhase == PomodoroPhase.SHORT_BREAK || pomodoroPhase == PomodoroPhase.LONG_BREAK)
    }

    private fun isHomePomodoroWorkPhase(): Boolean {
        return isHomePomodoroSessionActive() && pomodoroPhase == PomodoroPhase.WORK
    }

    private fun handleHomePomodoroAccess(item: String, now: Long) {
        val category = runBlockingOrNull {
            classificationRepository.getClassification(item)?.category
        } ?: if (item in blockedApps) AppCategory.VOID else AppCategory.CONTEXT

        if (isHomePomodoroBreakOpen()) {
            if (category == AppCategory.SIGNAL) {
                Log.d(TAG, "Home Pomodoro break allowed signal app: $item")
                return
            }
            handleHomePomodoroBreach(item, now, category, isPomodoroWork = false)
            return
        }

        handleHomePomodoroBreach(item, now, category, isPomodoroWork = isHomePomodoroWorkPhase())
    }

    private fun handleHomePomodoroBreach(
        item: String,
        now: Long,
        category: AppCategory,
        isPomodoroWork: Boolean
    ) {
        if (item == lastBreachItem && now - lastBreachTime < 2500) return
        lastBreachTime = now
        lastBreachItem = item

        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(PomodoroChamberActivity.createIntent(this))

        scope.launch(Dispatchers.IO) {
            val prefs = PrefManager.getInstance(applicationContext)
            if (isPomodoroWork && category == AppCategory.VOID) {
                val shouldReset = AdaptiveEngine.getCurrentMode(prefs) == AppMode.IRON
                PomodoroEngine(applicationContext).registerVoidBreachDuringWork(resetCurrentInterval = shouldReset)
            }

            val db = com.sanket_satpute_20.ironmind.data.IronMindDatabase.getDatabase(applicationContext)
            db.temptationLogDao().insertLog(
                com.sanket_satpute_20.ironmind.data.TemptationLog(
                    blockedAppName = item,
                    blockedPackage = item,
                    duringTaskName = if (isPomodoroWork) "POMODORO CHAMBER" else "POMODORO BREAK",
                    timestamp = now,
                    date = LocalDate.now().toString()
                )
            )
        }
        Log.d(TAG, "Home Pomodoro intercepted package: $item")
    }

    private fun handleEarnedUnlockBreach(item: String, now: Long) {
        if (item == lastBreachItem && now - lastBreachTime < 2500) return
        lastBreachTime = now
        lastBreachItem = item

        performGlobalAction(GLOBAL_ACTION_HOME)
        scope.launch(Dispatchers.Main) {
            delay(140)
            startActivity(FocusBreachActivity.createEarnedUnlockIntent(this@IronMindAccessibilityService, item))
        }

        scope.launch(Dispatchers.IO) {
            val db = com.sanket_satpute_20.ironmind.data.IronMindDatabase.getDatabase(applicationContext)
            db.temptationLogDao().insertLog(
                com.sanket_satpute_20.ironmind.data.TemptationLog(
                    blockedAppName = item,
                    blockedPackage = item,
                    duringTaskName = "EARNED UNLOCK",
                    timestamp = now,
                    date = LocalDate.now().toString()
                )
            )
        }
        Log.d(TAG, "Earned Unlock sealed blocked package: $item")
    }

    private fun extractUrl(root: AccessibilityNodeInfo?): String? {
        root ?: return null
        val ids = listOf(
            "com.android.chrome:id/url_bar", "com.brave.browser:id/url_bar",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view", "com.opera.browser:id/url_bar",
            "com.sec.android.app.sbrowser:id/location_bar_edit_text", "com.microsoft.emmx:id/url_bar"
        )
        for (id in ids) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (nodes != null && nodes.isNotEmpty()) {
                val text = nodes[0].text?.toString()
                if (!text.isNullOrBlank()) return text
            }
        }
        return findUrlRecursive(root)
    }

    private fun findUrlRecursive(node: AccessibilityNodeInfo?): String? {
        node ?: return null
        val contentDesc = node.contentDescription?.toString() ?: ""
        val text = node.text?.toString() ?: ""
        val viewId = node.viewIdResourceName ?: ""
        if (viewId.contains("url", true) || contentDesc.contains("Address", true) || 
            node.className?.contains("EditText", true) == true) {
            if (text.contains(".") || text.startsWith("http")) return text
        }
        for (i in 0 until node.childCount) {
            val res = findUrlRecursive(node.getChild(i))
            if (res != null) return res
        }
        return null
    }

    private fun isTaskActive(): Boolean {
        val pref = PrefManager.getInstance(this)
        val start = pref.activeTaskStartTime
        val end = pref.activeTaskEndTime
        val date = pref.activeTaskDate
        val today = LocalDate.now().toString()
        if (start.isEmpty() || end.isEmpty() || date != today) return false
        return try {
            val now = LocalTime.now()
            val startTime = parseTime(start) ?: return false
            val endTime = parseTime(end) ?: return false
            now.isAfter(startTime) && now.isBefore(endTime)
        } catch (e: Exception) { false }
    }

    private fun isEarnedUnlockSealedToday(): Boolean {
        val today = LocalDate.now().toString()
        if (earnedUnlockDate.isNotBlank() && earnedUnlockDate != today) {
            EarnedUnlockManager(this).resetIfDayRolled()
            loadCache()
            return false
        }
        return isEarnedUnlockActive && !isEarnedUnlockUnlockedToday && earnedUnlockDate == today
    }

    private fun parseTime(timeStr: String): LocalTime? {
        val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
        for (f in formats) {
            runCatching { return LocalTime.parse(timeStr.trim().uppercase(), DateTimeFormatter.ofPattern(f, Locale.US)) }
        }
        return null
    }

    override fun onInterrupt() {}
    override fun onDestroy() {
        super.onDestroy()
        isAlive = false
        runCatching { unregisterReceiver(reloadReceiver) }
        scope.cancel()
    }

    companion object {
        const val ACTION_SLEEP_LOCK_SCREEN_TIMEOUT_LOCK = "com.ironmind.SLEEP_LOCK_SCREEN_TIMEOUT_LOCK"
        var isAlive = false
    }
}

private fun <T> runBlockingOrNull(block: suspend () -> T): T? {
    return runCatching { runBlocking { block() } }.getOrNull()
}
