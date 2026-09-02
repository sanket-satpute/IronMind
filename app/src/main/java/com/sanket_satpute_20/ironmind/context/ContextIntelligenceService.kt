package com.sanket_satpute_20.ironmind.context

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Service responsible for intelligent context gathering.
 * Uses ActivityTransition API and FusedLocationProvider to determine if the user is 
 * at Home, Work, or Traveling, while minimizing battery drain.
 */
class ContextIntelligenceService : Service() {

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val clusteringEngine = LocationClusteringEngine()
    private val modeStateEngine = ModeStateEngine()

    // In a real app, this would be a Room Database retrieving history
    private val mockLocationHistory = mutableListOf<LocationClusteringEngine.RawLocationPoint>()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        activityRecognitionClient = ActivityRecognition.getClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ContextIntelligence", "Service started. Ready to track activity transitions.")
        
        // TODO: In a production scenario, we would register a PendingIntent here
        // for ActivityTransitionRequest to wake this service up when the user starts 
        // or stops driving.
        
        // For now, we simulate a mock evaluation
        evaluateCurrentContext(isDriving = false)
        
        return START_STICKY
    }
    
    private fun evaluateCurrentContext(isDriving: Boolean) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentPoint = LocationClusteringEngine.RawLocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    mockLocationHistory.add(currentPoint)
                    
                    // Calculate clusters based on history
                    val topClusters = clusteringEngine.calculateClusters(mockLocationHistory)
                    
                    // Decide what mode IronMind should be in right now
                    val currentMode = modeStateEngine.evaluateCurrentMode(
                        isDriving = isDriving,
                        currentLocation = currentPoint,
                        topClusters = topClusters
                    )
                    
                    Log.d("ContextIntelligence", "Current IronMind Mode evaluated to: \$currentMode")
                    
                    // TODO: Broadcast this mode change to the UI/Blocker service
                }
            }
        } catch (e: SecurityException) {
            Log.e("ContextIntelligence", "Missing location permissions", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ContextIntelligence", "Service destroyed.")
    }
}
