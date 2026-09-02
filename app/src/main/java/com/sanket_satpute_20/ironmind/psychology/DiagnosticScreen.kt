package com.sanket_satpute_20.ironmind.psychology

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@Composable
fun DiagnosticScreen(
    viewModel  : DiagnosticViewModel = if (LocalInspectionMode.current) DiagnosticViewModel() else viewModel(),
    onComplete : (UserProfile) -> Unit
) {
    val context         = LocalContext.current
    val currentIndex    by viewModel.currentQuestion.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val profile         by viewModel.profile.collectAsState()

    // Navigate when profile is ready
    LaunchedEffect(profile) {
        profile?.let { onComplete(it) }
    }

    val question = viewModel.questions[currentIndex]
    val selectedForThis = selectedAnswers.getOrNull(currentIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Progress dots ─────────────────────────────
            ProgressDots(
                total   = viewModel.questions.size,
                current = currentIndex
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Question text ─────────────────────────────
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it } + fadeOut())
                }
            ) { idx ->
                val q = viewModel.questions[idx]
                Column {
                    Text(
                        text       = "Question ${idx + 1} of ${viewModel.questions.size}",
                        fontSize   = 11.sp,
                        color      = SurfaceElevated,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text       = q.question,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Black,
                        color      = Color.White,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text     = q.subtext,
                        fontSize = 14.sp,
                        color    = SurfaceElevated,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Answer options ─────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.answers.forEachIndexed { index, answer ->
                    val isSelected = selectedForThis == answer

                    // Staggered entrance animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(currentIndex) {
                        kotlinx.coroutines.delay(index * 60L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter   = fadeIn(tween(300)) +
                                  slideInHorizontally(tween(300)) { it / 3 }
                    ) {
                        AnswerCard(
                            answer     = answer,
                            isSelected = isSelected,
                            onClick    = { viewModel.selectAnswer(answer) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Navigation ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back button
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuestion() },
                        colors  = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border  = BorderStroke(1.dp, SurfaceElevated),
                        modifier = Modifier.height(52.dp),
                        shape   = RoundedCornerShape(12.dp)
                    ) {
                        Text("← Back")
                    }
                }

                // Next / Finish button
                val isLastQuestion = currentIndex == viewModel.questions.size - 1
                Button(
                    onClick = {
                        if (isLastQuestion) {
                            viewModel.finishDiagnostic(context)
                        } else {
                            viewModel.nextQuestion()
                        }
                    },
                    enabled = selectedForThis != null,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = if (selectedForThis != null)
                            ErrorRed else SurfaceDark,
                        disabledContainerColor = SurfaceDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape   = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text       = if (isLastQuestion) "See My Profile →"
                                     else "Next →",
                        fontWeight = FontWeight.Black,
                        color      = if (selectedForThis != null)
                            Color.White else SurfaceElevated
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerCard(
    answer     : DiagnosticAnswer,
    isSelected : Boolean,
    onClick    : () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue  = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val borderColor = if (isSelected) ErrorRed else SurfaceDark
    val bgColor     = if (isSelected) DeepBackground else DeepBackground

    Surface(
        color    = bgColor,
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(answer.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text       = answer.text,
                fontSize   = 15.sp,
                color      = if (isSelected) Color.White else TextPrimary,
                lineHeight = 22.sp,
                modifier   = Modifier.weight(1f)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier          = Modifier
                        .size(22.dp)
                        .background(ErrorRed, CircleShape),
                    contentAlignment  = Alignment.Center
                ) {
                    Text("✓", fontSize = 12.sp, color = Color.White,
                        fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ProgressDots(total: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isActive   = index == current
            val isComplete = index < current

            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp
            )
            val color by animateColorAsState(
                targetValue = when {
                    isActive   -> Color.White
                    isComplete -> ErrorRed
                    else       -> SurfaceElevated
                }
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiagnosticScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        DiagnosticScreen(
            onComplete = {}
        )
    }
}