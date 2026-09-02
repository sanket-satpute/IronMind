package com.sanket_satpute_20.ironmind.product

/**
 * User-facing scope for the current product version.
 *
 * Disabled features are intentionally frozen, not deleted. Keep their routes and
 * code available so they can return after the core promise loop is reliable:
 * plan mission -> lock distractions -> verify proof -> reward or recover.
 */
object CurrentVersionScope {
    const val SHOW_CLUB_CHAT = false
    const val SHOW_IRON_CIRCLE = false
    const val SHOW_SHAREABLE_CARDS = false
    const val SHOW_ARTIFACTS = false
    const val SHOW_CONFIDENCE_AND_VOICE = false
    const val SHOW_ADVANCED_PSYCHOLOGY = false
    const val SHOW_SPRING_CONTROL = false
    const val SHOW_BOSS_MODE = false
    const val SHOW_ADVANCED_CHALLENGES = false
    const val SHOW_ADVANCED_REPORTS = false
}
