package com.sanket_satpute_20.ironmind.crucible

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

/**
 * The source of truth for all available seasonal Crucibles.
 */
object CrucibleRepository {

    private val ALL_CRUCIBLES = listOf(
        CrucibleDefinition(
            id = "HELL_WEEK_2024",
            title = "HELL WEEK",
            tagline = "The standard is the standard.",
            lore = "For 7 days, you will face your reflections without blinking. No distractions. No excuses. Only total commitment to the mission. Failure resets you to zero.",
            durationDays = 7,
            sessionDurationMinutes = 45,
            defaultPenalty = CruciblePenalty.STREAK_RESET,
            failureRule = "Any breach, surrender, or missed day breaks the week and resets the run.",
            rewardTitle = "Hell Week Survivor",
            accentColor = ErrorRed, // Deep Red
            icon = "🔥"
        ),
        CrucibleDefinition(
            id = "DAWN_WALKER_2024",
            title = "THE DAWN WALKER",
            tagline = "Own the morning, own the day.",
            lore = "The world is quietest before the sun rises. For 14 days, you will wake at 5:00 AM and complete your primary mission before the rest of the world wakes up.",
            durationDays = 14,
            sessionDurationMinutes = 30,
            defaultPenalty = CruciblePenalty.SKIP_COUNT,
            failureRule = "Miss the session or break focus, and the dawn run is broken for that day.",
            rewardTitle = "Dawn Walker",
            accentColor = WarningAmber, // Solar Orange
            icon = "☀️",
            requiresPremium = true
        ),
        CrucibleDefinition(
            id = "DIGITAL_MONK_2024",
            title = "DIGITAL MONK",
            tagline = "Silence the noise.",
            lore = "Your focus is your most valuable resource. For 21 days, you will restrict all social media and non-essential browsing to zero. Reclaim your mind.",
            durationDays = 21,
            sessionDurationMinutes = 60,
            defaultPenalty = CruciblePenalty.PUNISHMENT_DOUBLE,
            failureRule = "One distraction breach ends the session and doubles the consequence you chose to carry.",
            rewardTitle = "Zenith Mind",
            accentColor = ElectricViolet, // Deep Indigo
            icon = "🧘",
            requiresPremium = true
        )
    )

    /**
     * Returns the Crucible that is currently active for the user.
     * In a real app, this might depend on the date or a remote config.
     * For now, we'll return a crucible based on the current month.
     */
    fun getCurrentCrucible(): CrucibleDefinition? {
        val month = Calendar.getInstance().get(Calendar.MONTH) // 0-11
        return when (month) {
            in 0..3 -> ALL_CRUCIBLES[0] // Jan-Apr: Hell Week
            in 4..7 -> ALL_CRUCIBLES[1] // May-Aug: Dawn Walker
            else -> ALL_CRUCIBLES[2]    // Sep-Dec: Digital Monk
        }
    }

    fun getCrucibleById(id: String): CrucibleDefinition? {
        return ALL_CRUCIBLES.find { it.id == id }
    }
}
