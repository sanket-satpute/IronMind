package com.sanket_satpute_20.ironmind.integrity

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.ui.components.ShieldMode
import com.sanket_satpute_20.ironmind.utils.AppClockProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow

class ShieldMonitorService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: IronMindDatabase
    private lateinit var prefs: PrefManager

    override fun onCreate() {
        super.onCreate()
        database = IronMindDatabase.getDatabase(applicationContext)
        prefs = PrefManager.getInstance(applicationContext)

        val initialMode = prefs.toShieldMode()
        val initialState = ShieldUiState(mode = initialMode)
        val notification = OngoingShieldNotification.createNotification(this, initialState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                OngoingShieldNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(OngoingShieldNotification.NOTIFICATION_ID, notification)
        }

        observeLiveCounts()
        
        // Setup UsageStatsMonitor fallback when accessibility is off
        UsageStatsMonitor.startMonitoring(applicationContext, scope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeLiveCounts() {
        val dateKey = MutableStateFlow(AppClockProvider.clock.today().toString())
        
        // Simple day rollover check
        scope.launch {
            while (isActive) {
                val today = AppClockProvider.clock.today().toString()
                if (dateKey.value != today) {
                    dateKey.value = today
                }
                delay(60_000L)
            }
        }

        scope.launch {
            dateKey.flatMapLatest { date ->
                combine(
                    database.temptationLogDao().observeCountForDate(date),
                    database.focusSessionDao().observeCompletedCountForDate(date)
                ) { temptations, sessions -> Pair(temptations, sessions) }
            }.collect { (temptations, sessions) ->
                val integrity = integrityLevel(temptations, sessions)
                val state = ShieldUiState(
                    integrityLevel = integrity,
                    temptationCount = temptations,
                    completedSessionCount = sessions,
                    mode = prefs.toShieldMode()
                )
                prefs.lastKnownIntegrityLevel = integrity
                OngoingShieldNotification.updateNotification(this@ShieldMonitorService, state)
                
                // Update Widget
                sendBroadcast(Intent(this@ShieldMonitorService, IntegrityShieldWidgetProvider::class.java).apply {
                    action = IntegrityShieldWidgetProvider.ACTION_UPDATE_SHIELD
                })
            }
        }
    }

    private fun integrityLevel(temptations: Int, completedSessions: Int): Float {
        return (1f - temptations * 0.12f + completedSessions * 0.08f)
            .coerceIn(0.10f, 1f)
    }

    private fun PrefManager.toShieldMode(): ShieldMode = when (AdaptiveEngine.getCurrentMode(this).name) {
        ShieldMode.IRON.name -> ShieldMode.IRON
        ShieldMode.RECOVERY.name -> ShieldMode.RECOVERY
        ShieldMode.EXPERIMENT.name -> ShieldMode.EXPERIMENT
        else -> ShieldMode.BUILD
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
