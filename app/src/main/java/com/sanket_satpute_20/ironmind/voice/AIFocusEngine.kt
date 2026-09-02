package com.sanket_satpute_20.ironmind.voice

import com.sanket_satpute_20.ironmind.psychology.AppMode

enum class MoodVerdict(val label: String, val emoji: String, val color: Long) {
    STRESSED("Stressed", "😰", 0xFFF44336),
    NEUTRAL("Neutral", "😐", 0xFF9E9E9E),
    POWERFUL("Powerful", "🔥", 0xFF4CAF50)
}

object AIFocusEngine {

    private val STRESS_KEYWORDS = setOf(
        "tired", "hard", "struggling", "distracted", "anxious", "overwhelmed", 
        "bored", "quit", "fail", "difficult", "exhausted", "late", "busy"
    )

    private val POWER_KEYWORDS = setOf(
        "crushed", "focused", "easy", "proud", "done", "finished", "strong", 
        "discipline", "flow", "fast", "great", "productive", "success"
    )

    /**
     * Performs lightweight keyword-based mood analysis and returns a verdict + mode recommendation.
     */
    fun analyzeMood(text: String): Pair<MoodVerdict, AppMode?> {
        val words = text.lowercase().split(" ", ".", ",")
        var stressCount = 0
        var powerCount = 0

        for (word in words) {
            if (STRESS_KEYWORDS.contains(word)) stressCount++
            if (POWER_KEYWORDS.contains(word)) powerCount++
        }

        return when {
            stressCount > powerCount -> Pair(MoodVerdict.STRESSED, AppMode.RECOVERY)
            powerCount > stressCount -> Pair(MoodVerdict.POWERFUL, AppMode.IRON)
            else -> Pair(MoodVerdict.NEUTRAL, null)
        }
    }

    fun getAIDialogMessage(verdict: MoodVerdict, recommendedMode: AppMode?): String {
        return when (verdict) {
            MoodVerdict.STRESSED -> "Your check-in sounds strained. IronMind suggests shifting to ${recommendedMode?.label} to protect your energy."
            MoodVerdict.POWERFUL -> "Your check-in sounds strong. Are you ready to maintain this standard in ${recommendedMode?.label}?"
            MoodVerdict.NEUTRAL -> "Task recorded. Consistency is the only path to the top. Keep stacking wins."
        }
    }
}
