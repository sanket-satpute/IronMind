package com.sanket_satpute_20.ironmind.social

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseSocialRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun syncFoundation(context: Context) {
        val userId = auth.currentUser?.uid ?: return
        val settings = SocialProfileBuilder.buildSettings(context)
        val profile = SocialProfileBuilder.build(context)
        val ownPhoneHash = auth.currentUser?.phoneNumber
            ?.let(PhoneNormalizer::normalize)
            ?.takeIf { it.isNotBlank() }
            ?.let(PhoneHashUtil::sha256)

        try {
            db.collection("users").document(userId)
                .set(
                    mapOf(
                        "phoneHash" to ownPhoneHash,
                        "discoverableByContacts" to settings.discoverableByContacts,
                        "ironCircleEnabled" to settings.ironCircleEnabled,
                        "socialInviteCode" to profile.inviteCode,
                        "socialDisplayName" to profile.displayName,
                        "socialProfileUpdatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()

            db.collection("users").document(userId)
                .collection("social_profile")
                .document("settings")
                .set(
                    mapOf(
                        "ironCircleEnabled" to settings.ironCircleEnabled,
                        "discoverableByContacts" to settings.discoverableByContacts,
                        "shareStreakBand" to settings.shareStreakBand,
                        "shareConsistencyTrend" to settings.shareConsistencyTrend,
                        "shareFiveAmWeekly" to settings.shareFiveAmWeekly,
                        "shareTitles" to settings.shareTitles,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()

            db.collection("users").document(userId)
                .collection("social_profile")
                .document("public")
                .set(
                    mapOf(
                        "uid" to profile.uid,
                        "displayName" to profile.displayName,
                        "inviteCode" to profile.inviteCode,
                        "streakBand" to profile.streakBand,
                        "consistencyTrend" to profile.consistencyTrend?.name,
                        "verifiedFiveAmThisWeek" to profile.verifiedFiveAmThisWeek,
                        "titles" to profile.titles,
                        "activeThisWeek" to profile.activeThisWeek,
                        "profileUpdatedAt" to profile.profileUpdatedAt
                    ),
                    SetOptions.merge()
                ).await()
        } catch (e: Exception) {
            Log.e("IronCircle", "Foundation sync failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun findDiscoverableContacts(candidates: List<LocalContactCandidate>): List<DiscoveredIronContact> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()
        if (candidates.isEmpty()) return emptyList()

        val hashes = candidates.map { it.phoneHash }.distinct()
        val candidateByHash = candidates.associateBy { it.phoneHash }
        val matches = mutableListOf<DiscoveredIronContact>()

        hashes.chunked(10).forEach { chunk ->
            val snapshot = db.collection("users")
                .whereIn("phoneHash", chunk)
                .whereEqualTo("discoverableByContacts", true)
                .get()
                .await()

            snapshot.documents.forEach { document ->
                if (document.id == currentUserId) return@forEach
                val matchedHash = document.getString("phoneHash").orEmpty()
                val localCandidate = candidateByHash[matchedHash] ?: return@forEach
                val publicDoc = db.collection("users")
                    .document(document.id)
                    .collection("social_profile")
                    .document("public")
                    .get()
                    .await()

                matches += DiscoveredIronContact(
                    uid = document.id,
                    displayName = publicDoc.getString("displayName")
                        ?: document.getString("name")
                        ?: localCandidate.displayName,
                    maskedPhone = localCandidate.maskedPhone,
                    streakBand = publicDoc.getString("streakBand"),
                    consistencyTrend = publicDoc.getString("consistencyTrend")
                        ?.let { runCatching { ConsistencyTrend.valueOf(it) }.getOrNull() },
                    verifiedFiveAmThisWeek = publicDoc.getLong("verifiedFiveAmThisWeek")?.toInt(),
                    titles = (publicDoc.get("titles") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    activeThisWeek = publicDoc.getBoolean("activeThisWeek") ?: false,
                    inviteCode = publicDoc.getString("inviteCode").orEmpty()
                )
            }
        }

        return matches.sortedBy { it.displayName.lowercase() }
    }

    suspend fun sendFriendRequest(targetUid: String, targetDisplayName: String, source: String = "CONTACT_DISCOVERY") {
        val currentUser = auth.currentUser ?: return
        if (targetUid == currentUser.uid) return

        val publicDoc = db.collection("users").document(currentUser.uid)
            .collection("social_profile")
            .document("public")
            .get()
            .await()
        val senderName = publicDoc.getString("displayName")
            ?: currentUser.displayName
            ?: "IronMind User"
        val senderInviteCode = publicDoc.getString("inviteCode").orEmpty()
        val now = System.currentTimeMillis()

        val incomingPayload = mapOf(
            "requestId" to currentUser.uid,
            "fromUid" to currentUser.uid,
            "fromName" to senderName,
            "fromInviteCode" to senderInviteCode,
            "status" to "PENDING",
            "source" to source,
            "createdAt" to now
        )

        val outgoingPayload = mapOf(
            "requestId" to targetUid,
            "targetUid" to targetUid,
            "targetName" to targetDisplayName,
            "status" to "PENDING",
            "source" to source,
            "createdAt" to now
        )

        db.collection("users").document(targetUid)
            .collection("friend_requests")
            .document(currentUser.uid)
            .set(incomingPayload, SetOptions.merge())
            .await()

        db.collection("users").document(currentUser.uid)
            .collection("outgoing_friend_requests")
            .document(targetUid)
            .set(outgoingPayload, SetOptions.merge())
            .await()
    }

    suspend fun getIncomingFriendRequests(): List<FriendRequestItem> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users").document(userId)
            .collection("friend_requests")
            .whereEqualTo("status", "PENDING")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            FriendRequestItem(
                requestId = doc.id,
                fromUid = doc.getString("fromUid") ?: return@mapNotNull null,
                fromName = doc.getString("fromName") ?: "IronMind User",
                fromInviteCode = doc.getString("fromInviteCode").orEmpty(),
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun acceptFriendRequest(request: FriendRequestItem) {
        val currentUser = auth.currentUser ?: return
        val currentPublic = db.collection("users").document(currentUser.uid)
            .collection("social_profile").document("public").get().await()
        val senderPublic = db.collection("users").document(request.fromUid)
            .collection("social_profile").document("public").get().await()
        val now = System.currentTimeMillis()

        val currentUserName = currentPublic.getString("displayName")
            ?: currentUser.displayName
            ?: "IronMind User"

        db.collection("users").document(currentUser.uid)
            .collection("friend_requests")
            .document(request.requestId)
            .set(mapOf("status" to "ACCEPTED", "acceptedAt" to now), SetOptions.merge())
            .await()

        db.collection("users").document(request.fromUid)
            .collection("outgoing_friend_requests")
            .document(currentUser.uid)
            .set(mapOf("status" to "ACCEPTED", "acceptedAt" to now), SetOptions.merge())
            .await()

        db.collection("users").document(currentUser.uid)
            .collection("friends")
            .document(request.fromUid)
            .set(
                mapOf(
                    "friendUid" to request.fromUid,
                    "displayName" to request.fromName,
                    "connectedAt" to now
                ),
                SetOptions.merge()
            ).await()

        db.collection("users").document(request.fromUid)
            .collection("friends")
            .document(currentUser.uid)
            .set(
                mapOf(
                    "friendUid" to currentUser.uid,
                    "displayName" to currentUserName,
                    "connectedAt" to now
                ),
                SetOptions.merge()
            ).await()
    }

    suspend fun rejectFriendRequest(requestId: String) {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid)
            .collection("friend_requests")
            .document(requestId)
            .set(mapOf("status" to "REJECTED"), SetOptions.merge())
            .await()

        db.collection("users").document(requestId)
            .collection("outgoing_friend_requests")
            .document(currentUser.uid)
            .set(mapOf("status" to "REJECTED"), SetOptions.merge())
            .await()
    }

    suspend fun getFriends(): List<FriendConnection> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users").document(userId)
            .collection("friends")
            .get()
            .await()

        val friends = mutableListOf<FriendConnection>()
        snapshot.documents.forEach { doc ->
            val friendUid = doc.getString("friendUid") ?: return@forEach
            val publicDoc = db.collection("users").document(friendUid)
                .collection("social_profile")
                .document("public")
                .get()
                .await()

            friends += FriendConnection(
                uid = friendUid,
                displayName = publicDoc.getString("displayName")
                    ?: doc.getString("displayName")
                    ?: "IronMind User",
                connectedAt = doc.getLong("connectedAt") ?: 0L,
                streakBand = publicDoc.getString("streakBand"),
                consistencyTrend = publicDoc.getString("consistencyTrend")
                    ?.let { runCatching { ConsistencyTrend.valueOf(it) }.getOrNull() },
                verifiedFiveAmThisWeek = publicDoc.getLong("verifiedFiveAmThisWeek")?.toInt(),
                titles = (publicDoc.get("titles") as? List<*>)?.map { it.toString() } ?: emptyList(),
                activeThisWeek = publicDoc.getBoolean("activeThisWeek") ?: false,
                inviteCode = publicDoc.getString("inviteCode").orEmpty()
            )
        }

        return friends.sortedByDescending { it.connectedAt }
    }

    suspend fun getFriend(friendUid: String): FriendConnection? {
        val userId = auth.currentUser?.uid ?: return null
        val friendDoc = db.collection("users").document(userId)
            .collection("friends")
            .document(friendUid)
            .get()
            .await()
        if (!friendDoc.exists()) return null

        val publicDoc = db.collection("users").document(friendUid)
            .collection("social_profile")
            .document("public")
            .get()
            .await()

        return FriendConnection(
            uid = friendUid,
            displayName = publicDoc.getString("displayName")
                ?: friendDoc.getString("displayName")
                ?: "IronMind User",
            connectedAt = friendDoc.getLong("connectedAt") ?: 0L,
            streakBand = publicDoc.getString("streakBand"),
            consistencyTrend = publicDoc.getString("consistencyTrend")
                ?.let { runCatching { ConsistencyTrend.valueOf(it) }.getOrNull() },
            verifiedFiveAmThisWeek = publicDoc.getLong("verifiedFiveAmThisWeek")?.toInt(),
            titles = (publicDoc.get("titles") as? List<*>)?.map { it.toString() } ?: emptyList(),
            activeThisWeek = publicDoc.getBoolean("activeThisWeek") ?: false,
            inviteCode = publicDoc.getString("inviteCode").orEmpty()
        )
    }

    suspend fun sendReaction(targetUid: String, targetDisplayName: String, type: SocialReactionType) {
        val currentUser = auth.currentUser ?: return
        if (targetUid == currentUser.uid) return

        val publicDoc = db.collection("users").document(currentUser.uid)
            .collection("social_profile")
            .document("public")
            .get()
            .await()
        val senderName = publicDoc.getString("displayName")
            ?: currentUser.displayName
            ?: "IronMind User"
        val now = System.currentTimeMillis()
        val reactionId = "${currentUser.uid}_$now"

        val payload = mapOf(
            "id" to reactionId,
            "fromUid" to currentUser.uid,
            "fromName" to senderName,
            "toUid" to targetUid,
            "toName" to targetDisplayName,
            "type" to type.name,
            "createdAt" to now
        )

        db.collection("users").document(targetUid)
            .collection("incoming_reactions")
            .document(reactionId)
            .set(payload, SetOptions.merge())
            .await()

        db.collection("users").document(currentUser.uid)
            .collection("sent_reactions")
            .document(reactionId)
            .set(payload, SetOptions.merge())
            .await()
    }

    suspend fun getRecentIncomingReactions(limit: Long = 8): List<SocialReactionItem> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users").document(userId)
            .collection("incoming_reactions")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val type = doc.getString("type")
                ?.let { runCatching { SocialReactionType.valueOf(it) }.getOrNull() }
                ?: return@mapNotNull null
            SocialReactionItem(
                id = doc.id,
                fromUid = doc.getString("fromUid") ?: return@mapNotNull null,
                fromName = doc.getString("fromName") ?: "IronMind User",
                toUid = doc.getString("toUid") ?: userId,
                type = type,
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }
    }

    suspend fun getLatestIncomingReactionFrom(friendUid: String): SocialReactionItem? {
        val userId = auth.currentUser?.uid ?: return null
        val snapshot = db.collection("users").document(userId)
            .collection("incoming_reactions")
            .whereEqualTo("fromUid", friendUid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: return null

        val type = snapshot.getString("type")
            ?.let { runCatching { SocialReactionType.valueOf(it) }.getOrNull() }
            ?: return null

        return SocialReactionItem(
            id = snapshot.id,
            fromUid = snapshot.getString("fromUid") ?: friendUid,
            fromName = snapshot.getString("fromName") ?: "IronMind User",
            toUid = snapshot.getString("toUid") ?: userId,
            type = type,
            createdAt = snapshot.getLong("createdAt") ?: 0L
        )
    }

    suspend fun getSocialOverview(): SocialOverview {
        val userId = auth.currentUser?.uid ?: return SocialOverview(0, 0, 0)
        val friendsSnapshot = db.collection("users").document(userId)
            .collection("friends")
            .get()
            .await()
        val pendingSnapshot = db.collection("users").document(userId)
            .collection("friend_requests")
            .whereEqualTo("status", "PENDING")
            .get()
            .await()
        val reactionsSnapshot = db.collection("users").document(userId)
            .collection("incoming_reactions")
            .limit(5)
            .get()
            .await()

        return SocialOverview(
            friendCount = friendsSnapshot.size(),
            pendingRequestCount = pendingSnapshot.size(),
            recentReactionCount = reactionsSnapshot.size()
        )
    }

    suspend fun findByInviteCode(inviteCode: String): DiscoveredIronContact? {
        val currentUserId = auth.currentUser?.uid ?: return null
        val normalizedCode = inviteCode.trim().uppercase()
        if (normalizedCode.isBlank()) return null

        val snapshot = db.collection("users")
            .whereEqualTo("socialInviteCode", normalizedCode)
            .whereEqualTo("ironCircleEnabled", true)
            .get()
            .await()

        val match = snapshot.documents.firstOrNull { it.id != currentUserId } ?: return null
        val publicDoc = db.collection("users").document(match.id)
            .collection("social_profile")
            .document("public")
            .get()
            .await()

        return DiscoveredIronContact(
            uid = match.id,
            displayName = publicDoc.getString("displayName")
                ?: match.getString("socialDisplayName")
                ?: "IronMind User",
            maskedPhone = "Invite code match",
            streakBand = publicDoc.getString("streakBand"),
            consistencyTrend = publicDoc.getString("consistencyTrend")
                ?.let { runCatching { ConsistencyTrend.valueOf(it) }.getOrNull() },
            verifiedFiveAmThisWeek = publicDoc.getLong("verifiedFiveAmThisWeek")?.toInt(),
            titles = (publicDoc.get("titles") as? List<*>)?.map { it.toString() } ?: emptyList(),
            activeThisWeek = publicDoc.getBoolean("activeThisWeek") ?: false,
            inviteCode = publicDoc.getString("inviteCode").orEmpty()
        )
    }
}
