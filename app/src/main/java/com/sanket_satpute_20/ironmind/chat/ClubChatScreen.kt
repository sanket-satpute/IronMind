package com.sanket_satpute_20.ironmind.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan

private data class ChatMessage(val id: Int, val sender: String, val message: String, val isMe: Boolean)

private val mockMessages = listOf(
    ChatMessage(1, "Elena", "Woke up at 4:55 today. The run was brutal.", false),
    ChatMessage(2, "David", "Same. But we don't skip days.", false),
    ChatMessage(3, "You", "Just finished the morning ritual.", true),
    ChatMessage(4, "Sanjay", "Let's go!! 40 day streak approaching.", false)
)

@Composable
fun ClubChatScreen(
    onBack: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Text("←", color = Color.White, fontSize = 20.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("5 AM CLUB CHAT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                    Text("14 active members", color = NeonCyan, fontSize = 12.sp)
                }
            }

            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockMessages) { msg ->
                    ChatBubble(message = msg)
                }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Send a message...", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = { messageText = "" },
                    modifier = Modifier
                        .size(50.dp)
                        .background(NeonCyan, CircleShape)
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isMe) NeonCyan.copy(alpha = 0.2f) else SurfaceElevated
    val textColor = if (message.isMe) Color.White else Color.White
    val shape = if (message.isMe) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
        ) {
            if (!message.isMe) {
                Text(message.sender, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp, start = 8.dp))
            }
            Box(
                modifier = Modifier
                    .background(bgColor, shape)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(message.message, color = textColor, fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClubChatScreenPreview() {
    IronMindTheme { ClubChatScreen() }
}