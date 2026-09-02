package com.sanket_satpute_20.ironmind.graveyard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

private data class DeadHabit(val id: Int, val name: String, val streakBeforeDeath: Int, val dateOfDeath: String)

private val mockGraveyard = listOf(
    DeadHabit(1, "Cold Showers", 14, "Oct 12, 2025"),
    DeadHabit(2, "No Sugar", 3, "Sep 04, 2025"),
    DeadHabit(3, "Read 10 Pages", 42, "Aug 22, 2025"),
    DeadHabit(4, "Morning Run", 1, "Aug 01, 2025")
)

@Composable
fun GraveyardScreen(
    onBack: () -> Unit = {},
    onRebuildBoardClick: () -> Unit = {},
    onModeShiftClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground) // Absolute darkness
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Text("← Escape", color = Color.Gray)
                }
                
                Text("GRAVEYARD", color = Color.DarkGray, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Here lie your failed commitments.",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Every broken promise is recorded here permanently. Resurrect them to try again, or leave them as monuments to your weakness.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(32.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(mockGraveyard) { habit ->
                    TombstoneCard(habit)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onRebuildBoardClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RESURRECT A HABIT", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TombstoneCard(habit: DeadHabit) {
    Box(
        modifier = Modifier
            .background(DeepBackground, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceElevated, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = habit.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Died: ${habit.dateOfDeath}",
                color = Color.Gray,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Lived ${habit.streakBeforeDeath} days",
                color = ErrorRed.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GraveyardScreenPreview() {
    IronMindTheme { GraveyardScreen() }
}