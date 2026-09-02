package com.sanket_satpute_20.ironmind.share

object MilestoneDetector {

    private val MILESTONES = listOf(7, 14, 21, 30, 50, 75, 100, 200, 365)

    fun isMilestone(streakCount: Int): Boolean {
        return streakCount in MILESTONES
    }

    fun getMilestoneData(streakCount: Int): MilestoneData? {
        if (!isMilestone(streakCount)) return null

        return MilestoneData(
            streakDays = streakCount,
            title = getMilestoneTitle(streakCount),
            subtitle = getMilestoneSubtitle(streakCount),
            tier = getMilestoneTier(streakCount)
        )
    }

    private fun getMilestoneTitle(days: Int): String = when (days) {
        7    -> "ONE WEEK"
        14   -> "TWO WEEKS"
        21   -> "THREE WEEKS"
        30   -> "ONE MONTH"
        50   -> "50 DAYS"
        75   -> "75 DAYS"
        100  -> "100 DAYS"
        200  -> "200 DAYS"
        365  -> "ONE YEAR"
        else -> "$days DAYS"
    }

    private fun getMilestoneSubtitle(days: Int): String = when (days) {
        7    -> "Most people quit by day 3. You didn't."
        14   -> "Two weeks of zero excuses."
        21   -> "21 days. A habit is forming."
        30   -> "30 days. You are not the same person."
        50   -> "50 days of choosing discipline over comfort."
        75   -> "75 days. You are built different."
        100  -> "100 days. This is who you are now."
        200  -> "200 days. Legends are made of this."
        365  -> "365 days. One full year. Iron."
        else -> "$days days of showing up."
    }

    private fun getMilestoneTier(days: Int): MilestoneTier = when {
        days >= 365 -> MilestoneTier.LEGENDARY
        days >= 100 -> MilestoneTier.ELITE
        days >= 30  -> MilestoneTier.ADVANCED
        else        -> MilestoneTier.RISING
    }
}

data class MilestoneData(
    val streakDays: Int,
    val title: String,
    val subtitle: String,
    val tier: MilestoneTier
)

enum class MilestoneTier(
    val label: String,
    val primaryColor: Long,
    val secondaryColor: Long
) {
    RISING(   "Rising",    0xFFFF6D00, 0xFFFF9800),
    ADVANCED( "Advanced",  0xFF4CAF50, 0xFF81C784),
    ELITE(    "Elite",     0xFF2196F3, 0xFF64B5F6),
    LEGENDARY("Legendary", 0xFFFFD700, 0xFFFFF176)
}