package com.sanket_satpute_20.ironmind.context

import java.util.Calendar

/**
 * Determines the current operational mode of IronMind based on context data.
 */
class ModeStateEngine {

    enum class AppMode {
        DISCIPLINE_HOME,
        DISCIPLINE_WORK,
        TRAVEL_FLEXIBLE,
        MANUAL_OVERRIDE
    }

    /**
     * Evaluates all context to decide the current mode.
     * @param isDriving Whether ActivityRecognition detected driving.
     * @param currentLocation The user's current GPS location.
     * @param topClusters The historical clusters (0 is usually Home, 1 is usually Work).
     */
    fun evaluateCurrentMode(
        isDriving: Boolean,
        currentLocation: LocationClusteringEngine.RawLocationPoint?,
        topClusters: List<LocationClusteringEngine.LocationCluster>
    ): AppMode {
        
        // 1. Safety / Override Check: If driving, always allow flexible travel mode
        if (isDriving) {
            return AppMode.TRAVEL_FLEXIBLE
        }

        // 2. If we don't have location or haven't learned clusters yet, default to Travel/Flexible
        if (currentLocation == null || topClusters.isEmpty()) {
            return AppMode.TRAVEL_FLEXIBLE
        }

        val homeCluster = topClusters.getOrNull(0)
        val workCluster = topClusters.getOrNull(1)

        val inHome = homeCluster != null && isWithinRadius(currentLocation, homeCluster)
        val inWork = workCluster != null && isWithinRadius(currentLocation, workCluster)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val isNight = hour >= 22 || hour < 6

        // 3. Evaluate Work
        if (inWork) {
            return AppMode.DISCIPLINE_WORK
        }

        // 4. Evaluate Home
        if (inHome) {
            // If at home during the night, it's definitely discipline/sleep mode.
            // If at home during the day, could be WFH.
            return AppMode.DISCIPLINE_HOME
        }

        // 5. Default Fallback
        return AppMode.TRAVEL_FLEXIBLE
    }

    /**
     * Checks if a point is within roughly 150 meters of a cluster center.
     */
    private fun isWithinRadius(
        point: LocationClusteringEngine.RawLocationPoint,
        cluster: LocationClusteringEngine.LocationCluster
    ): Boolean {
        // Simplified Euclidean distance approximation for small distances
        val latDiff = Math.abs(point.latitude - cluster.centerLatitude)
        val lngDiff = Math.abs(point.longitude - cluster.centerLongitude)
        
        // 0.0015 degrees is roughly 150 meters. 
        return latDiff < 0.0015 && lngDiff < 0.0015
    }
}
