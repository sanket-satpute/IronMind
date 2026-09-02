package com.sanket_satpute_20.ironmind.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.components.ProgressRing
import com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry

private data class RitualStep(val id: Int, val title: String, val subtitle: String, var isCompleted: Boolean = false)

@Composable
fun MorningRoutineScreen(
    onComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val steps = remember {
        mutableStateListOf(
            RitualStep(1, "Hydrate", "Drink 500ml of water immediately."),
            RitualStep(2, "Sunlight", "Get 10 minutes of direct sunlight outside."),
            RitualStep(3, "Movement", "15 minutes of stretching or light cardio."),
            RitualStep(4, "Deep Work", "Complete 60 mins of focused, uninterrupted work.")
        )
    }

    val completedCount = steps.count { it.isCompleted }
    val progress = completedCount.toFloat() / steps.size
    val allDone = completedCount == steps.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
    AnimatedEntry(modifier = Modifier.fillMaxSize()) {
    Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack) { Text("← Retreat", color = Color.Gray) }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.RocketLaunch,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "LAUNCH RITUAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("Execute sequence to start the day.", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProgressRing(
                    progress = progress,
                    size = 76.dp,
                    strokeWidth = 7.dp,
                    progressColor = WarningAmber,
                    label = "${(progress * 100).toInt()}%"
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "$completedCount / ${steps.size} COMPLETED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WarningAmber,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(32.dp))

            // Checklist
            steps.forEachIndexed { index, step ->
                RoutineStepRow(
                    step = step,
                    onClick = {
                        val updated = steps[index].copy(isCompleted = !step.isCompleted)
                        steps[index] = updated
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { if (allDone) onComplete() },
                enabled = allDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningAmber,
                    disabledContainerColor = SurfaceDark,
                    contentColor = Color.Black,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (allDone) "FINISH RITUAL" else "COMPLETE ALL STEPS",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 14.sp
                )
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
    }
}

@Composable
private fun RoutineStepRow(step: RitualStep, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (step.isCompleted) SurfaceDark else DeepBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (step.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (step.isCompleted) WarningAmber else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = step.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (step.isCompleted) Color.Gray else Color.White,
                    textDecoration = if (step.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = step.subtitle,
                    fontSize = 13.sp,
                    color = if (step.isCompleted) Color.DarkGray else Color.Gray,
                    textDecoration = if (step.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MorningRoutineScreenPreview() {
    IronMindTheme { MorningRoutineScreen() }
}
