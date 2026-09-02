package com.sanket_satpute_20.ironmind.psychology

import android.content.Context
import android.util.Log

class PersonaAnalysisEngine(private val context: Context) {
    
    companion object {
        const val PREFS_NAME = "BehavioralProfile"
        const val KEY_DAYS_TRACKED = "daysRecorded"
    }
    
    fun analyzeAndDeterminePersona(): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val daysRecorded = prefs.getInt(KEY_DAYS_TRACKED, 0)
        
        if (daysRecorded < 3) {
            Log.d("PersonaAnalysisEngine", "Not enough data for calibration.")
            return null
        }
        
        var totalScreenTimeMin = 0L
        var totalSocialTimeMin = 0L
        var totalSleepTimeMin = 0L
        var daysWithSleepData = 0
        var totalUnlocks = 0
        
        for (i in 0 until daysRecorded) {
            totalScreenTimeMin += prefs.getLong("day_${i}_total_time", 0L)
            totalSocialTimeMin += prefs.getLong("day_${i}_social_time", 0L)
            totalUnlocks += prefs.getInt("day_${i}_unlocks", 0)
            
            val sleepTime = prefs.getLong("day_${i}_sleep_time", 0L)
            if (sleepTime > 0) {
                totalSleepTimeMin += sleepTime
                daysWithSleepData++
            }
        }
        
        val avgScreenTime = if (daysRecorded > 0) totalScreenTimeMin / daysRecorded else 0
        val avgSocialTime = if (daysRecorded > 0) totalSocialTimeMin / daysRecorded else 0
        val avgUnlocks = if (daysRecorded > 0) totalUnlocks / daysRecorded else 0
        val avgSleepTime = if (daysWithSleepData > 0) totalSleepTimeMin / daysWithSleepData else -1L
        
        Log.d("PersonaAnalysisEngine", "Averages -> Screen: $avgScreenTime, Social: $avgSocialTime, Sleep: $avgSleepTime, Unlocks: $avgUnlocks")
        
        var assignedPersona = UserType.ACHIEVER
        var assignedMode = AppMode.IRON
        
        if (avgSocialTime > 180) {
            assignedPersona = UserType.DOOMSCROLLER
            assignedMode = AppMode.IRON
        } else if (avgSleepTime != -1L && avgSleepTime < 360 && avgScreenTime > 240) {
            // Less than 6 hours sleep + high screen time = Escapism/Burnout
            assignedPersona = UserType.BURNED_OUT
            assignedMode = AppMode.RECOVERY
        } else if (avgScreenTime > 240 && avgSocialTime > 60 && avgUnlocks > 100) {
            // High unlocks = fragmented attention / procrastination
            assignedPersona = UserType.PROCRASTINATOR
            assignedMode = AppMode.BUILD
        }
        
        Log.d("PersonaAnalysisEngine", "Assigned Persona: $assignedPersona, Mode: $assignedMode")
        
        return UserProfile(
            primaryType = assignedPersona, 
            secondaryType = null, 
            assignedMode = assignedMode,
            typeScores = emptyMap()
        )
    }
}
