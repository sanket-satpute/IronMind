package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*

import androidx.compose.foundation.BorderStroke
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

// ... existing imports ...
enum class LexiconTerm {
    CRUCIBLE,
    BLACK_HOLE,
    DAILY_INTEGRITY,
    BEDTIME_REAPER,
    SMART_CONTEXT,
    STREAK_SHIELD
}

private data class ArsenalItem(
    val term: LexiconTerm,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IronMindLexiconSheet(
    term: LexiconTerm?,
    onDismiss: () -> Unit
) {
    val arsenal = listOf(
        ArsenalItem(
            term = LexiconTerm.CRUCIBLE,
            title = "CRUCIBLE",
            description = "A hardcore focus session. Quitting wipes your streak.",
            icon = Icons.Rounded.LocalFireDepartment,
            accent = ErrorRed
        ),
        ArsenalItem(
            term = LexiconTerm.BLACK_HOLE,
            title = "BLACK HOLE",
            description = "Deep work void. Repairs a CRITICAL streak.",
            icon = Icons.Rounded.Block,
            accent = SurfaceElevated
        ),
        ArsenalItem(
            term = LexiconTerm.DAILY_INTEGRITY,
            title = "INTEGRITY",
            description = "Promises kept vs broken. Stay above 70%.",
            icon = Icons.Rounded.Analytics,
            accent = SuccessGreen
        ),
        ArsenalItem(
            term = LexiconTerm.BEDTIME_REAPER,
            title = "REAPER",
            description = "Enforces sleep. Apps die after bedtime.",
            icon = Icons.Rounded.Nightlight,
            accent = ElectricViolet
        ),
        ArsenalItem(
            term = LexiconTerm.SMART_CONTEXT,
            title = "RADAR",
            description = "AI Engine. Auto-shifts focus based on location.",
            icon = Icons.Rounded.AutoAwesome,
            accent = NeonCyan
        ),
        ArsenalItem(
            term = LexiconTerm.STREAK_SHIELD,
            title = "SHIELD",
            description = "Earned via 7 perfect days. Absorbs one failure.",
            icon = Icons.Rounded.Security,
            accent = WarningAmber
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "YOUR ARSENAL",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                items(arsenal) { item ->
                    val isHighlighted = item.term == term
                    
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            width = if (isHighlighted) 2.dp else 1.dp,
                            color = if (isHighlighted) item.accent else Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = item.accent.copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.accent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.description,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("CLOSE", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun IronMindLexiconSheetPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        IronMindLexiconSheet(term = null, onDismiss = {})
    }
}