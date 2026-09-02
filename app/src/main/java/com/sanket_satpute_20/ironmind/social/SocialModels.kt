package com.sanket_satpute_20.ironmind.social

data class SocialPrivacySettings(
    val ironCircleEnabled: Boolean,
    val discoverableByContacts: Boolean,
    val shareStreakBand: Boolean,
    val shareConsistencyTrend: Boolean,
    val shareFiveAmWeekly: Boolean,
    val shareTitles: Boolean
)

enum class ConsistencyTrend {
    RISING,
    STABLE,
    SLIPPING
}

enum class SocialReactionType {
    RESPECT,
    LOCKED_IN,
    KEEP_GOING,
    RISING,
    SALUTE
}

data class SocialProfile(
    val uid: String,
    val displayName: String,
    val inviteCode: String,
    val streakBand: String?,
    val consistencyTrend: ConsistencyTrend?,
    val verifiedFiveAmThisWeek: Int?,
    val titles: List<String>,
    val activeThisWeek: Boolean,
    val profileUpdatedAt: Long
)

data class DiscoveredIronContact(
    val uid: String,
    val displayName: String,
    val maskedPhone: String,
    val streakBand: String?,
    val consistencyTrend: ConsistencyTrend?,
    val verifiedFiveAmThisWeek: Int?,
    val titles: List<String>,
    val activeThisWeek: Boolean,
    val inviteCode: String
)

data class FriendRequestItem(
    val requestId: String,
    val fromUid: String,
    val fromName: String,
    val fromInviteCode: String,
    val createdAt: Long
)

data class FriendConnection(
    val uid: String,
    val displayName: String,
    val connectedAt: Long,
    val streakBand: String?,
    val consistencyTrend: ConsistencyTrend?,
    val verifiedFiveAmThisWeek: Int?,
    val titles: List<String>,
    val activeThisWeek: Boolean,
    val inviteCode: String
)

data class SocialReactionItem(
    val id: String,
    val fromUid: String,
    val fromName: String,
    val toUid: String,
    val type: SocialReactionType,
    val createdAt: Long
)

data class SocialOverview(
    val friendCount: Int,
    val pendingRequestCount: Int,
    val recentReactionCount: Int
)
