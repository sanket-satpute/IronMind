package com.sanket_satpute_20.ironmind.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
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
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private val previewNetwork = listOf(
    CircleMember("Amit V.", 62, 97, 1),
    CircleMember("Sara L.", 55, 93, 2),
    CircleMember("Rohit D.", 49, 91, 3),
    CircleMember("Alex K.", 47, 89, 4),
    CircleMember("Maya S.", 31, 84, 5),
    CircleMember("You", 21, 82, 6),
    CircleMember("Raj P.", 14, 76, 7),
)

@Composable
fun IronCircleNetworkScreen(
    onBack: () -> Unit = {},
    onCompareFriend: (String) -> Unit = {}
) {
    val network = previewNetwork

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground)
                    .padding(24.dp)
            ) {
                Column {
                    TextButton(onClick = onBack) {
                        Text("← Back", color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = GoldXP,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "GLOBAL LEADERBOARD",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "IronMind warriors ranked by discipline",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(network) { member ->
                    NetworkMemberRow(
                        member = member,
                        onCompare = { onCompareFriend(member.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkMemberRow(member: CircleMember, onCompare: () -> Unit) {
    val isYou = member.name == "You"
    val rankEmoji = when (member.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#${member.rank}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isYou) DeepBackground else DeepBackground,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (member.rank <= 3) {
                Text(text = rankEmoji, fontSize = 22.sp)
            } else {
                Text(
                    text = "#${member.rank}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceElevated
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Avatar circle
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    if (isYou) ErrorRed.copy(alpha = 0.2f) else SurfaceElevated,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.first().toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isYou) ErrorRed else Color.Gray
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isYou) ErrorRed else Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "${member.streak}d  ·  ${member.completionRate}%",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        if (!isYou) {
            TextButton(
                onClick = onCompare,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Rounded.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Compare", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IronCircleNetworkScreenPreview() {
    IronMindTheme { IronCircleNetworkScreen() }
}
