package com.sanket_satpute_20.ironmind.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WbSunny
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
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP

private data class ClubMember(val name: String, val wakeTime: String, val days: Int)

private val mockMembers = listOf(
    ClubMember("Elena R.", "4:55 AM", 112),
    ClubMember("David T.", "5:00 AM", 84),
    ClubMember("You", "5:02 AM", 41),
    ClubMember("Sanjay K.", "5:04 AM", 29),
    ClubMember("Jessica M.", "5:08 AM", 12)
)

@Composable
fun FiveAmClubHubScreen(
    onBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Dawn Header Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(IronRed.copy(alpha = 0.4f), ElectricViolet.copy(alpha = 0.2f), DeepBackground)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column {
                    TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                        Text("← Back", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = GoldXP,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "5 AM CLUB",
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                color = Color.White,
                                letterSpacing = 3.sp
                            )
                            Text("Win the morning. Win the day.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatBox(modifier = Modifier.weight(1f), label = "TODAY'S WAKE", value = "5:02 AM", highlight = false)
                StatBox(modifier = Modifier.weight(1f), label = "CONSECUTIVE", value = "41 DAYS", highlight = true)
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "GLOBAL LEADERS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockMembers) { member ->
                    ClubMemberRow(member = member)
                }
            }
        }
    }
}

@Composable
private fun StatBox(modifier: Modifier = Modifier, label: String, value: String, highlight: Boolean) {
    Box(
        modifier = modifier
            .background(if (highlight) IronRed.copy(alpha = 0.15f) else DeepBackground, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = if (highlight) IronRed else Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
private fun ClubMemberRow(member: ClubMember) {
    val isYou = member.name == "You"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isYou) SurfaceElevated else DeepBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(if (isYou) IronRed else SurfaceElevated, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(member.name.first().toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(member.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Woke up at ${member.wakeTime}", fontSize = 12.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${member.days}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = GoldXP)
            Text("days", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FiveAmClubHubScreenPreview() {
    IronMindTheme { FiveAmClubHubScreen() }
}