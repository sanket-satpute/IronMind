package com.sanket_satpute_20.ironmind.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {

    private val FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("H:mm", Locale.US),
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("HH.mm", Locale.US)
    )

    /**
     * Skillfully parses a time string using multiple formatters.
     * Returns a safe default [LocalTime.MIDNIGHT] if parsing fails to prevent crashes.
     */
    fun parseTimeSafe(timeStr: String?): LocalTime {
        if (timeStr.isNullOrBlank()) return LocalTime.MIDNIGHT
        
        val cleanTime = timeStr.trim().uppercase()
        
        for (formatter in FORMATTERS) {
            try {
                return LocalTime.parse(cleanTime, formatter)
            } catch (e: Exception) {
                continue
            }
        }
        
        // Final fallback: attempt manual split if formatters fail
        return try {
            val parts = cleanTime.split(":", ".")
            if (parts.size >= 2) {
                LocalTime.of(parts[0].toInt(), parts[1].toInt())
            } else {
                LocalTime.MIDNIGHT
            }
        } catch (e: Exception) {
            LocalTime.MIDNIGHT
        }
    }

    /**
     * Standardizes time for database/pref storage.
     */
    fun formatToStandard(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
    }
}
