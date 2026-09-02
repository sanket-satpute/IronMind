package com.sanket_satpute_20.ironmind.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.IronRed
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary
import com.sanket_satpute_20.ironmind.ui.components.ProgressRing

data class CircleMember(
    val name: String,
    val streak: Int,
    val completionRate: Int,
    val rank: Int
)

private val previewMembers = listOf(
    CircleMember("Alex K.", 47, 94, 1),
    CircleMember("Maya S.", 31, 88, 2),
    CircleMember("You", 21, 82, 3),
    CircleMember("Raj P.", 14, 76, 4),
    CircleMember("Priya M.", 9, 65, 5)
)

@Composable
fun IronCircleScreen(
    onBack: () -> Unit = {},
    onDiscoverContacts: () -> Unit = {},
    onOpenNetwork: () -> Unit = {},
    onOpenPremiumUpgrade: () -> Unit = {}
) {
    val members = previewMembers

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
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DeepBackground, DeepBackground)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onBack) {
                            Text("← Back", color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Group,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "IRON CIRCLE",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Accountability network · ${members.size} members",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscoverContacts,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(ErrorRed, IronRed))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Discover", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onOpenNetwork,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Leaderboard", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Leaderboard list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(members) { member ->
                    CircleMemberRow(member = member)
                }
            }
        }
    }
}

@Composable
private fun CircleMemberRow(member: CircleMember) {
    val isYou = member.name == "You"
    val rankColor = when (member.rank) {
        1 -> GoldXP
        2 -> TextPrimary
        3 -> WarningAmber
        else -> SurfaceElevated
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isYou) DeepBackground else DeepBackground,
                RoundedCornerShape(14.dp)
            )
            .then(
                if (isYou) Modifier.border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                else Modifier
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(rankColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, rankColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${member.rank}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankColor
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isYou) ErrorRed else Color.White
                    )
                    if (isYou) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(ErrorRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("YOU", fontSize = 9.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProgressRing(
                        progress = member.completionRate / 100f,
                        size = 44.dp,
                        strokeWidth = 4.dp,
                        progressColor = if (member.completionRate >= 90) SuccessGreen else WarningAmber,
                        trackColor = SurfaceElevated,
                        label = "${member.completionRate}%"
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${member.completionRate}% completion rate",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.width(12.dp))

            // Streak
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${member.streak}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Text(text = "days", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IronCircleScreenPreview() {
    IronMindTheme { IronCircleScreen() }
}
