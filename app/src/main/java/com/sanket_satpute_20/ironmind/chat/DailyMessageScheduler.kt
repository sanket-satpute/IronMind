package com.sanket_satpute_20.ironmind.chat

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Message
import java.time.LocalDate

object DailyMessageScheduler {

    private const val BOT_USER_ID = "ironmind_bot"

    fun postDailyMessageIfNeeded(
        channelType: String,
        channelId: String,
        dayNumber: Int
    ) {
        val today = LocalDate.now().toString()
        val dailyMessageId = "daily_message_$today"
        val dailyPromptId = "daily_prompt_$today"
        
        val channelClient = ChatClient.instance().channel(channelType, channelId)

        channelClient.getMessage(dailyMessageId).enqueue { result ->
            if (result.isFailure) {
                val message = Message(
                    id = dailyMessageId,
                    text = DailyMessages.getMessageForDay(dayNumber),
                    extraData = mutableMapOf("is_bot_message" to true)
                )
                channelClient.sendMessage(message).enqueue { }
            }
        }

        channelClient.getMessage(dailyPromptId).enqueue { result ->
            if (result.isFailure) {
                val promptMessage = Message(
                    id = dailyPromptId,
                    text = DailyMessages.getDailyPrompt(dayNumber),
                    extraData = mutableMapOf(
                        "is_bot_message" to true,
                        "message_kind" to "DAILY_PROMPT"
                    )
                )
                channelClient.sendMessage(promptMessage).enqueue { }
            }
        }
    }

    fun postReaderUnlockIfNeeded(
        channelType: String,
        channelId: String,
        dayNumber: Int
    ) {
        postOneTimeClubMessage(
            channelType = channelType,
            channelId = channelId,
            messageId = "reader_unlock_day_$dayNumber",
            text = DailyMessages.getReaderUnlockMessage(dayNumber),
            kind = "READER_UNLOCK"
        )
    }

    fun postFullUnlockIfNeeded(
        channelType: String,
        channelId: String,
        dayNumber: Int
    ) {
        postOneTimeClubMessage(
            channelType = channelType,
            channelId = channelId,
            messageId = "full_unlock_day_$dayNumber",
            text = DailyMessages.getFullUnlockMessage(dayNumber),
            kind = "FULL_UNLOCK"
        )
    }

    private fun postOneTimeClubMessage(
        channelType: String,
        channelId: String,
        messageId: String,
        text: String,
        kind: String
    ) {
        val channelClient = ChatClient.instance().channel(channelType, channelId)
        channelClient.getMessage(messageId).enqueue { result ->
            if (result.isFailure) {
                val message = Message(
                    id = messageId,
                    text = text,
                    extraData = mutableMapOf(
                        "is_bot_message" to true,
                        "message_kind" to kind
                    )
                )
                channelClient.sendMessage(message).enqueue { }
            }
        }
    }
}
