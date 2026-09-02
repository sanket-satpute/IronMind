package com.sanket_satpute_20.ironmind.psychology

import androidx.compose.ui.graphics.Color
import com.sanket_satpute_20.ironmind.data.Task

// ── The 6 human types ────────────────────────────────────────
enum class UserType(val label: String, val emoji: String) {
    ACHIEVER        ("Achiever",       "🔥"),
    BROKEN_STRIVER  ("Starter",        "🌱"),
    PERFECTIONIST   ("Perfectionist",  "⚙️"),
    BURNED_OUT      ("Recovering",     "🔄"),
    IDENTITY_SEEKER ("Explorer",       "🧭"),
    SYSTEMS_PERSON  ("Analyst",        "📊"),
    DOOMSCROLLER    ("Doomscroller",   "📱"),
    PROCRASTINATOR  ("Procrastinator", "⏳")
}

// ── The 4 app modes ──────────────────────────────────────────
enum class AppMode(
    val label           : String,
    val emoji           : String,
    val color           : Long,
    val description     : String
) {
    IRON(
        label       = "Iron Mode",
        emoji       = "⚡",
        color       = 0xFFFF1744,
        description = "No mercy. Full accountability. Maximum growth."
    ),
    BUILD(
        label       = "Build Mode",
        emoji       = "🌱",
        color       = 0xFF4CAF50,
        description = "Steady progress. Compassion after failure. No shame."
    ),
    RECOVERY(
        label       = "Recovery Mode",
        emoji       = "🔄",
        color       = 0xFF2196F3,
        description = "Small steps. Zero pressure. Just showing up."
    ),
    EXPERIMENT(
        label       = "Experiment Mode",
        emoji       = "🧪",
        color       = 0xFFFF9800,
        description = "No streaks. Rolling rates. Imperfect is fine."
    )
}

// ── The user's full psychological profile ────────────────────
data class UserProfile(
    val primaryType         : UserType,
    val secondaryType       : UserType?,
    val assignedMode        : AppMode,
    val typeScores          : Map<UserType, Int>,   // raw scores from questionnaire
    val currentEnergyLevel  : Int = 3,              // 1-5
    val currentStressLevel  : Int = 3,              // 1-5
    val weeklyCheckInDue    : Boolean = false,
    val modeOverrideActive  : Boolean = false,      // user manually overrode mode
    val profileCreatedDate  : String = ""
)

// ── Adaptive text — every piece of UI text per mode ──────────
data class AdaptiveText(
    val iron        : String,
    val build       : String,
    val recovery    : String,
    val experiment  : String
) {
    fun forMode(mode: AppMode): String = when (mode) {
        AppMode.IRON       -> iron
        AppMode.BUILD      -> build
        AppMode.RECOVERY   -> recovery
        AppMode.EXPERIMENT -> experiment
    }
}

data class SkipResponseData(
    val emoji: String,
    val title: String,
    val color: Color,
    val body: String,
    val showInputField: Boolean,
    val action: String?
)

// ── All adaptive copy in one place ───────────────────────────
object AppCopy {

    val skipConsequenceTitle = AdaptiveText(
        iron       = "Face The Consequences",
        build      = "That's okay. Let's understand why.",
        recovery   = "Rest. Tomorrow is a new day.",
        experiment = "Skip logged. Data collected. Move on."
    )

    val skipConsequenceBody = AdaptiveText(
        iron       = "You made a promise. You broke it. Now you sit with that.",
        build      = "Everyone skips sometimes. What got in the way today?",
        recovery   = "You showed up today by opening this app. That counts.",
        experiment = "This skip is a data point, not a verdict. What can you learn from it?"
    )

    val taskDoneTitle = AdaptiveText(
        iron       = "Done. Keep the standard.",
        build      = "+1 to becoming who you said you are. 🌱",
        recovery   = "You did it. That took real effort today.",
        experiment = "Task complete. Pattern noted."
    )

    val streakBrokenMessage = AdaptiveText(
        iron       = "Streak broken. Zero tolerance tomorrow.",
        build      = "One break doesn't erase what you built. Come back.",
        recovery   = "Streaks don't define you. Returning does.",
        experiment = "Streak reset. Rolling rate unchanged. Keep going."
    )

    val morningGreeting = AdaptiveText(
        iron       = "Another day to prove who you are.",
        build      = "Today is a fresh start. You've got this.",
        recovery   = "Take it one task at a time. You don't have to be perfect.",
        experiment = "New data set. Let's see what today reveals."
    )

    val allTasksDoneMessage = AdaptiveText(
        iron       = "Perfect day. Maintain the standard tomorrow.",
        build      = "Full completion! You are becoming someone different. 🌱",
        recovery   = "You finished everything. That is genuinely remarkable right now.",
        experiment = "100% completion rate today. Logging pattern."
    )

    val weeklyReportVerdict = AdaptiveText(
        iron       = "This is your war record. Own it completely.",
        build      = "Progress is non-linear. Here's what you built this week.",
        recovery   = "You showed up this week. Every day you opened this app matters.",
        experiment = "Weekly dataset complete. Here are your patterns."
    )

    val punishmentDuration = AdaptiveText(
        iron       = "5 minutes. No exceptions.",
        build      = "2-minute reflection. What happened?",
        recovery   = "No lockout. Just a quiet moment to breathe.",
        experiment = "30-second pause. Then a question."
    )

    fun forSkipResponse(response: SkipResponse, task: Task): SkipResponseData {
        return when (response) {
            SkipResponse.PUNISHMENT -> SkipResponseData(
                emoji = "⚡",
                title = skipConsequenceTitle.iron,
                color = Color(AppMode.IRON.color),
                body  = skipConsequenceBody.iron,
                showInputField = false,
                action = "Start Punishment"
            )
            SkipResponse.REFLECTION -> SkipResponseData(
                emoji = "🌱",
                title = skipConsequenceTitle.build,
                color = Color(AppMode.BUILD.color),
                body  = skipConsequenceBody.build,
                showInputField = true,
                action = "Save Reflection"
            )
            SkipResponse.COMPASSION -> SkipResponseData(
                emoji = "🔄",
                title = skipConsequenceTitle.recovery,
                color = Color(AppMode.RECOVERY.color),
                body  = skipConsequenceBody.recovery,
                showInputField = false,
                action = "Back to Home"
            )
            SkipResponse.DATA_COLLECTION -> SkipResponseData(
                emoji = "🧪",
                title = skipConsequenceTitle.experiment,
                color = Color(AppMode.EXPERIMENT.color),
                body  = skipConsequenceBody.experiment,
                showInputField = true,
                action = "Log & Continue"
            )
        }
    }
}