    package com.sanket_satpute_20.ironmind.psychology

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet

private val checkInQuestions = listOf(
    "How would you rate your energy levels this week?" to listOf("Very Low", "Low", "Moderate", "High", "Very High"),
    "How consistent were you with your tasks?" to listOf("Missed most", "Missed some", "Hit half", "Hit most", "All done"),
    "How was your stress level?" to listOf("Overwhelming", "High", "Manageable", "Low", "Minimal"),
    "Do you want to change your App Mode this week?" to listOf("Stay same", "Go harder", "Ease off", "Experiment")
)

@Composable
fun WeeklyCheckInScreen(
    onComplete: (answers: List<Int>) -> Unit = {}
) {
    val selectedAnswers = remember { mutableStateListOf(*Array(checkInQuestions.size) { -1 }) }
    val allAnswered = selectedAnswers.all { it >= 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Psychology,
                contentDescription = null,
                tint = ElectricViolet,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "WEEKLY CHECK-IN",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    "Helps your mode adapt to your reality",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            checkInQuestions.forEachIndexed { i, _ ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (selectedAnswers[i] >= 0) ElectricViolet else SurfaceElevated,
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Questions
        checkInQuestions.forEachIndexed { qi, (question, options) ->
            Column {
                Text(
                    text = "${qi + 1}. $question",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(12.dp))
                options.forEachIndexed { oi, option ->
                    val isSelected = selectedAnswers[qi] == oi
                    OutlinedButton(
                        onClick = { selectedAnswers[qi] = oi },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) ElectricViolet.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (isSelected) ElectricViolet else Color.Gray
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = if (isSelected)
                                androidx.compose.ui.graphics.SolidColor(ElectricViolet)
                            else
                                androidx.compose.ui.graphics.SolidColor(SurfaceElevated)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { if (allAnswered) onComplete(selectedAnswers.toList()) },
            enabled = allAnswered,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricViolet,
                disabledContainerColor = SurfaceElevated
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                if (allAnswered) "SUBMIT CHECK-IN" else "ANSWER ALL QUESTIONS",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun WeeklyCheckInScreenPreview() {
    IronMindTheme { WeeklyCheckInScreen() }
}
