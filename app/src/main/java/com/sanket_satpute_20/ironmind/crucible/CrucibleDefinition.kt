package com.sanket_satpute_20.ironmind.crucible

import androidx.compose.ui.graphics.Color

/**
 * Defines the static properties of a seasonal Crucible event.
 * This is the blueprint for a challenge.
 */
data class CrucibleDefinition(
    val id: String,              // Unique identifier, e.g., "HELL_WEEK_2024_Q3"
    val title: String,           // The name of the Crucible, e.g., "Hell Week"
    val tagline: String,         // A short, punchy subtitle, e.g., "Embrace the Suck."
    val lore: String,            // A longer, narrative description of the challenge.
    val durationDays: Int,       // The number of days the Crucible lasts.
    val sessionDurationMinutes: Int, // The required duration of the daily/session test.
    val defaultPenalty: CruciblePenalty, // The default consequence for failing the session.
    val failureRule: String,     // Plain-language rule shown while the crucible is active.
    val rewardTitle: String,     // The permanent Identity Title earned upon completion.
    val accentColor: Color,      // The theme color for this specific Crucible.
    val icon: String,            // An emoji or icon representing the Crucible.
    val requiresPremium: Boolean = false // If true, the user must have IronMind Pro to attempt this
)
