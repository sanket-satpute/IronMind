package com.sanket_satpute_20.ironmind.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

private data class CompareRow(val label: String, val you: String, val friend: String, val youWins: Boolean)

private val sampleRows = listOf(
    CompareRow("Current Streak", "21 days", "47 days", false),
    CompareRow("Completion Rate", "82%", "89%", false),
    CompareRow("Tasks / Day", "5", "4", true),
    CompareRow("Days Tracked", "45", "62", false),
    CompareRow("Perfect Days", "12", "18", false),
)

@Composable
fun FriendCompareScreen(
    friendUid: String = "",
    friendName: String = "Alex K.",
    onBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // Back
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Back", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "HEAD-TO-HEAD",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(24.dp))

            // VS header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatar(name = "You", color = ErrorRed)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VS", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SurfaceElevated)
                    Spacer(Modifier.height(4.dp))
                    Row {
                        val youWins = sampleRows.count { it.youWins }
                        val friendWins = sampleRows.count { !it.youWins }
                        Text(youWins.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = ErrorRed)
                        Text(" : ", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                        Text(friendWins.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = ElectricViolet)
                    }
                }
                PlayerAvatar(name = friendName.split(" ").first(), color = ElectricViolet)
            }

            Spacer(Modifier.height(28.dp))

            // Compare rows
            sampleRows.forEach { row ->
                CompareStatRow(row = row)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.weight(1f))

            // Motivational footer
            val youWins = sampleRows.count { it.youWins }
            val friendWins = sampleRows.count { !it.youWins }
            val message = if (youWins >= friendWins) "You're competitive! Keep pushing." else "Close gap. One task at a time."
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(message, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun PlayerAvatar(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
        }
        Spacer(Modifier.height(8.dp))
        Text(name, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompareStatRow(row: CompareRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBackground, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // You
        Text(
            text = row.you,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (row.youWins) ErrorRed else Color.White
        )
        if (row.youWins) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
        }

        // Label
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(text = row.label, fontSize = 11.sp, color = Color.Gray, letterSpacing = 0.5.sp)
        }

        // Friend
        if (!row.youWins) {
            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(14.dp))
        }
        Text(
            text = row.friend,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (!row.youWins) ElectricViolet else Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FriendCompareScreenPreview() {
    IronMindTheme { FriendCompareScreen() }
}
