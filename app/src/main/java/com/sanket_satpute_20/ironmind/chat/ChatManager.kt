package com.sanket_satpute_20.ironmind.chat

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanket_satpute_20.ironmind.BuildConfig
import com.sanket_satpute_20.ironmind.data.GlobalConstants
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ChatManager {

    private var isInitialized = false
    private const val TAG = "ChatManager"

    fun initialize(context: Context) {
        if (isInitialized) return
        ChatClient.Builder(GlobalConstants.STREAM_API_KEY, context).build()
        isInitialized = true
    }

    fun connectUser(
        userId: String,
        userName: String,
        streakDays: Int,
        overallStreak: Int,
        canPost: Boolean,
        ironStatusUnlocked: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // If already connected with same user, skip
        val currentUser = ChatClient.instance().getCurrentUser()
        if (currentUser?.id == userId) {
            onSuccess()
            return
        }

        val user = User(
            id = userId,
            name = userName,
            extraData = mutableMapOf(
                "streak_days" to streakDays,
                "overall_streak" to overallStreak,
                "club_role" to if (canPost) "FULL_MEMBER" else "READER",
                "iron_status" to ironStatusUnlocked,
                "streak_band" to buildStreakBand(overallStreak)
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken(userId)
                CoroutineScope(Dispatchers.Main).launch {
                    connectWithRetry(user, token, retryCount = 0, onSuccess, onError)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chat token fetch failed", e)
                val message = e.message ?: "Unable to authenticate club chat."
                CoroutineScope(Dispatchers.Main).launch {
                    onError(message)
                }
            }
        }
    }

    /**
     * Returns a Stream Chat authentication token for the given user.
     *
     * SECURITY WARNING — PRODUCTION MIGRATION REQUIRED:
     * Currently uses `devToken()` which generates an UNSIGNED token on the client.
     * This is acceptable for development/testing but MUST be replaced before
     * any public release, because:
     *   - Any user can impersonate any other user by supplying a different userId
     *   - There is no server-side validation of identity
     *
     * PRODUCTION FIX:
     *   1. Deploy the Firebase Cloud Function in /firebase/functions/index.js
     *   2. Replace the devToken() call below with a network call to that function:
     *      - Send the Firebase Auth ID token to the Cloud Function
     *      - Receive a signed Stream Chat token in response
     *   3. Disable "Dev Mode" in the Stream Chat dashboard
     */
    private suspend fun getToken(userId: String): String {
        val endpoint = BuildConfig.STREAM_TOKEN_ENDPOINT.trim()
        if (endpoint.isNotEmpty()) {
            return fetchServerToken(endpoint)
        }

        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Using development token. Configure STREAM_TOKEN_ENDPOINT before release.")
            return ChatClient.instance().devToken(userId)
        }

        throw IllegalStateException(
            "Club chat is not configured for production. Set BuildConfig.STREAM_TOKEN_ENDPOINT to your deployed getStreamToken function."
        )
    }

    private suspend fun fetchServerToken(endpoint: String): String {
        val firebaseUser = Firebase.auth.currentUser
            ?: throw IllegalStateException("Sign in is required to access club chat.")
        val idToken = firebaseUser.getIdToken(false).await().token
            ?: throw IllegalStateException("Unable to verify your session for club chat.")

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $idToken")
            connectTimeout = 15000
            readTimeout = 15000
        }

        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            } ?: throw IllegalStateException("Empty response from chat auth server.")

            val body = BufferedReader(InputStreamReader(stream)).use { reader ->
                buildString {
                    var line = reader.readLine()
                    while (line != null) {
                        append(line)
                        line = reader.readLine()
                    }
                }
            }

            if (responseCode !in 200..299) {
                val error = runCatching { JSONObject(body).optString("error") }.getOrNull()
                throw IllegalStateException(error?.ifBlank { null } ?: "Chat authentication failed ($responseCode).")
            }

            val token = JSONObject(body).optString("token")
            if (token.isBlank()) {
                throw IllegalStateException("Chat auth server returned an invalid token.")
            }
            token
        } finally {
            connection.disconnect()
        }
    }

    private fun connectWithRetry(
        user: User,
        token: String,
        retryCount: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val maxRetries = 3
        val delayMs = when (retryCount) {
            0 -> 0L
            1 -> 2000L    // 2 seconds
            2 -> 5000L    // 5 seconds
            else -> 10000L // 10 seconds
        }

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            ChatClient.instance()
                .connectUser(user, token)
                .enqueue { result ->
                    when {
                        result.isSuccess -> onSuccess()
                        retryCount < maxRetries -> {
                            connectWithRetry(
                                user, token,
                                retryCount + 1,
                                onSuccess, onError
                            )
                        }
                        else -> {
                            onError("Could not connect after $maxRetries attempts. " +
                                    "Check your internet connection.")
                        }
                    }
                }
        }, delayMs)
    }

    fun getOrCreateClubChannel(onSuccess: (Channel) -> Unit) {
        ChatClient.instance()
            .createChannel(
                channelType = GlobalConstants.CHANNEL_TYPE_MESSAGING,
                channelId = GlobalConstants.CLUB_CHANNEL_ID,
                memberIds = emptyList(),
                extraData = mapOf(
                    "name" to "5AM Iron Club",
                    "description" to "Earned through verified 5 AM mornings."
                )
            ).enqueue { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let { onSuccess(it) }
                }
            }
    }

    fun syncClubPostCount(
        channelType: String,
        channelId: String,
        userId: String,
        knownPostCount: Int,
        onNewPostsDetected: (Int, Int) -> Unit
    ) {
        val channelClient = ChatClient.instance().channel(channelType, channelId)
        channelClient.watch().enqueue { result ->
            if (result.isSuccess) {
                val messages = result.getOrNull()?.messages.orEmpty()
                val totalPosts = messages.count { message ->
                    message.user.id == userId && !message.isBotClubMessage()
                }
                if (totalPosts > knownPostCount) {
                    onNewPostsDetected(totalPosts - knownPostCount, totalPosts)
                }
            }
        }
    }

    private fun buildStreakBand(streak: Int): String {
        return when {
            streak <= 0 -> "Offline"
            streak <= 3 -> "1-3"
            streak <= 7 -> "4-7"
            streak <= 14 -> "8-14"
            streak <= 30 -> "15-30"
            else -> "30+"
        }
    }

    private fun Message.isBotClubMessage(): Boolean {
        val kind = extraData["message_kind"]?.toString()
        val isBot = extraData["is_bot_message"] as? Boolean ?: false
        return isBot || kind != null
    }
}

