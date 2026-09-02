package com.sanket_satpute_20.ironmind

import android.app.Application
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanket_satpute_20.ironmind.chat.ChatManager
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IronMindApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        
        com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.init(this)
        
        // Initialize Stream Chat
        ChatManager.initialize(this)

        // Sign in anonymously if no user is currently signed in
        if (Firebase.auth.currentUser == null) {
            applicationScope.launch {
                try {
                    val authResult = Firebase.auth.signInAnonymously().await()
                    authResult.user?.let {
                        val prefs = PrefManager.getInstance(this@IronMindApplication)
                        prefs.firebaseUid = it.uid
                    }
                } catch (e: Exception) {
                    // Sign-in failed. The app can continue to function offline.
                }
            }
        }

        // Schedule Behavioral Profiler to run daily
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.sanket_satpute_20.ironmind.psychology.BehavioralProfilerWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BehavioralProfiler",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
