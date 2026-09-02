package com.sanket_satpute_20.ironmind.context

import kotlin.math.roundToInt

/**
 * Responsible for clustering raw GPS coordinates into meaningful zones (Home, Work, Travel).
 * Runs locally on the device to preserve privacy.
 */
class LocationClusteringEngine {

    /**
     * Data class to hold clustered location data.
     */
    data class LocationCluster(
        val centerLatitude: Double,
        val centerLongitude: Double,
        val pointsCount: Int,
        val totalHoursSpent: Long
    )

    data class RawLocationPoint(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long
    )

    /**
     * Determines the top clusters from historical location points using a grid-based approach.
     * Groups points that fall into the same ~100x100 meter grid square.
     */
    fun calculateClusters(history: List<RawLocationPoint>): List<LocationCluster> {
        if (history.isEmpty()) return emptyList()

        // 1. Group points into a grid
        val gridMap = mutableMapOf<Pair<Int, Int>, MutableList<RawLocationPoint>>()
        
        for (point in history) {
            val gridKey = getGridKey(point.latitude, point.longitude)
            if (!gridMap.containsKey(gridKey)) {
                gridMap[gridKey] = mutableListOf()
            }
            gridMap[gridKey]?.add(point)
        }

        // 2. Convert grid groups into LocationClusters
        val clusters = gridMap.map { (_, points) ->
            val avgLat = points.sumOf { it.latitude } / points.size
            val avgLng = points.sumOf { it.longitude } / points.size
            
            // Very simplified hours calculation for now (assuming 1 point = 1 unit of time/duration)
            val hoursSpent = (points.size * 5L) / 60L // Assuming ping every 5 mins when stationary
            
            LocationCluster(
                centerLatitude = avgLat,
                centerLongitude = avgLng,
                pointsCount = points.size,
                totalHoursSpent = hoursSpent
            )
        }

        // 3. Sort by most time spent
        return clusters.sortedByDescending { it.pointsCount }
    }

    /**
     * Converts a lat/lng to a coarse grid key (roughly 100m x 100m depending on equator distance).
     * 0.001 degrees is ~111 meters.
     */
    private fun getGridKey(latitude: Double, longitude: Double): Pair<Int, Int> {
        val latGrid = (latitude * 1000).roundToInt()
        val lngGrid = (longitude * 1000).roundToInt()
        return Pair(latGrid, lngGrid)
    }
}
