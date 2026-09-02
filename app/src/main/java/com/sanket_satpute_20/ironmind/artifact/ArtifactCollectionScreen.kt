package com.sanket_satpute_20.ironmind.artifact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private data class Artifact(
    val emoji: String,
    val name: String,
    val description: String,
    val unlocked: Boolean,
    val requiredStreak: Int
)

private val artifacts = listOf(
    Artifact("🔥", "Iron Starter", "Complete your first week", true, 7),
    Artifact("⚡", "Voltage", "14-day streak achieved", true, 14),
    Artifact("🛡️", "Shield Bearer", "Zero skips in 30 days", true, 30),
    Artifact("👑", "Iron Crown", "60-day streak", false, 60),
    Artifact("💎", "Diamond Mind", "100-day streak", false, 100),
    Artifact("🏆", "Legend", "365-day streak", false, 365),
    Artifact("🎯", "Precision", "7 perfect days in a row", true, 7),
    Artifact("🌙", "Night Warrior", "Sleep lock active 14 days", false, 14),
    Artifact("🧠", "Psychology Pro", "Complete 3 weekly check-ins", true, 0),
)

@Composable
fun ArtifactCollectionScreen(onBack: () -> Unit = {}) {
    val unlocked = artifacts.count { it.unlocked }

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
                    TextButton(onClick = onBack) { Text("← Back", color = Color.Gray) }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = GoldXP,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "ARTIFACT COLLECTION",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "$unlocked / ${artifacts.size} unlocked",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .height(6.dp)
                    .background(SurfaceDark, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(unlocked.toFloat() / artifacts.size)
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(listOf(GoldXP, WarningAmber)),
                            RoundedCornerShape(3.dp)
                        )
                )
            }

            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(artifacts) { artifact ->
                    ArtifactCard(artifact = artifact)
                }
            }
        }
    }
}

@Composable
private fun ArtifactCard(artifact: Artifact) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                if (artifact.unlocked) DeepBackground else DeepBackground,
                RoundedCornerShape(16.dp)
            )
            .then(
                if (artifact.unlocked)
                    Modifier.border(
                        1.dp,
                        Brush.verticalGradient(listOf(GoldXP.copy(0.6f), SurfaceElevated)),
                        RoundedCornerShape(16.dp)
                    )
                else Modifier
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (artifact.unlocked) {
                Text(artifact.emoji, fontSize = 28.sp)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(artifact.emoji, fontSize = 28.sp, modifier = Modifier)
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = artifact.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (artifact.unlocked) GoldXP else SurfaceElevated,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
            if (!artifact.unlocked) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (artifact.requiredStreak > 0) "${artifact.requiredStreak}d" else "?",
                    fontSize = 9.sp,
                    color = SurfaceElevated
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArtifactCollectionScreenPreview() {
    IronMindTheme { ArtifactCollectionScreen() }
}
