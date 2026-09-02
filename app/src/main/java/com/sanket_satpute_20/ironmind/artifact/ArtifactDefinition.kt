package com.sanket_satpute_20.ironmind.artifact

/**
 * Metadata for a legendary achievement.
 */
data class ArtifactDefinition(
    val id: String,
    val title: String,
    val description: String,
    val requirementLore: String, // How to earn it
    val icon: String, // Emoji for now
    val category: ArtifactCategory
)

enum class ArtifactCategory {
    NIGHT_OWL,    // Late night focus
    DEEP_WORK,    // High intensity sessions
    EARLY_BIRD,   // Morning discipline
    CONSISTENCY,  // Long streaks
    RECOVERY      // Bouncing back from failure
}

object ArtifactRepository {
    val MIDNIGHT_LAMP = ArtifactDefinition(
        id = "MIDNIGHT_LAMP",
        title = "The Midnight Lamp",
        description = "Your mind is a sanctuary of silence when the world sleeps.",
        requirementLore = "Complete 5 missions after 11:00 PM with focus > 80.",
        icon = "🕯️",
        category = ArtifactCategory.NIGHT_OWL
    )

    val IRON_ANCHOR = ArtifactDefinition(
        id = "IRON_ANCHOR",
        title = "The Iron Anchor",
        description = "An immovable object in a sea of distractions.",
        requirementLore = "Complete a Deep Work mission of 2+ hours with zero focus breaches.",
        icon = "⚓",
        category = ArtifactCategory.DEEP_WORK
    )

    val DAWN_BREAKER = ArtifactDefinition(
        id = "DAWN_BREAKER",
        title = "The Dawn Breaker",
        description = "You own the first light.",
        requirementLore = "Log 7 5:00 AM Club completions.",
        icon = "🌅",
        category = ArtifactCategory.EARLY_BIRD
    )
    
    val TERMINAL_OF_TRUTH = ArtifactDefinition(
        id = "TERMINAL_OF_TRUTH",
        title = "Terminal of Truth",
        description = "You no longer hide from your own data.",
        requirementLore = "Open the System Autopsy 3 days in a row.",
        icon = "📟",
        category = ArtifactCategory.CONSISTENCY
    )

    val ALL_ARTIFACTS = listOf(MIDNIGHT_LAMP, IRON_ANCHOR, DAWN_BREAKER, TERMINAL_OF_TRUTH)
    
    fun getById(id: String) = ALL_ARTIFACTS.find { it.id == id }
}
