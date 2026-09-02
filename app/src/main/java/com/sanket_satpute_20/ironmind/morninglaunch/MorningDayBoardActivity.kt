package com.sanket_satpute_20.ironmind.morninglaunch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class MorningDayBoardActivity : ComponentActivity() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, MorningDayBoardActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContent {
            IronMindTheme {
                MorningDayBoardRoute(
                    onFinished = {
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                                )
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun MorningDayBoardRoute(
    onFinished: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val today = remember { LocalDate.now() }
    val database = remember { IronMindDatabase.getDatabase(context) }
    val prefs = remember { PrefManager.getInstance(context) }
    val rawTasks by database.taskDao().getTasksForDate(today.toString()).collectAsState(initial = emptyList())
    var secondsLeft by remember { mutableIntStateOf(5) }
    val tasks = remember(rawTasks) {
        rawTasks
            .filterNot { it.isSkipped || it.isCompleted || it.isBreakSegment }
            .sortedWith(compareBy<Task> { it.startTime }.thenBy { it.id })
    }

    LaunchedEffect(today) {
        prefs.morningBonusArmedDate = today.toString()
    }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
        onFinished()
    }

    MorningDayBoardScreen(
        tasks = tasks,
        secondsLeft = secondsLeft,
        onCloseNow = onFinished
    )
}

@Composable
private fun MorningDayBoardScreen(
    tasks: List<Task>,
    secondsLeft: Int,
    onCloseNow: () -> Unit
) {
    val accent = WarningAmber
    val pulseScale by animateFloatAsState(
        targetValue = if (secondsLeft % 2 == 0) 1.03f else 1f,
        animationSpec = tween(450),
        label = "day_board_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = accent.copy(alpha = 0.13f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "DAY DEPLOY",
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Text(
                    text = "CLEAR THE BOARD\nBEFORE NOON",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    modifier = Modifier.scale(pulseScale)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.32f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MORNING BONUS LIVE",
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Finish today's work before 12:00 PM and cash in 1.5x XP.",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(Modifier.height(6.dp)) }
                        if (tasks.isEmpty()) {
                            item {
                                Text(
                                    text = "No work missions are armed for today.",
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                                )
                            }
                        } else {
                            items(tasks.take(6)) { task ->
                                DayBoardTaskRow(task = task, accent = accent)
                            }
                        }
                        item { Spacer(Modifier.height(6.dp)) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUTO DEPLOY",
                                color = Color.White.copy(alpha = 0.56f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Moving to your day in $secondsLeft s",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        Surface(
                            modifier = Modifier.size(42.dp),
                            color = accent.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = secondsLeft.toString(),
                                    color = accent,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onCloseNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "DEPLOY NOW",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DayBoardTaskRow(
    task: Task,
    accent: Color
) {
    val priority = when (task.importanceRank) {
        1 -> "TOP"
        2 -> "CORE"
        else -> "BUILD"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = accent.copy(alpha = 0.14f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.32f))
        ) {
            Text(
                text = task.startTime,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.name.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${task.startTime} - ${task.endTime}",
                color = Color.White.copy(alpha = 0.58f),
                fontSize = 12.sp
            )
        }
        Text(
            text = priority,
            color = Color.White.copy(alpha = 0.86f),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            textAlign = TextAlign.End
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@androidx.compose.runtime.Composable
fun MorningDayBoardPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        MorningDayBoardPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun MorningDayBoardPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            modifier = androidx.compose.ui.Modifier.padding(24.dp)
        ) {
            androidx.compose.material3.Text(
                "📋 Morning Day Board",
                color = WarningAmber,
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            androidx.compose.material3.Text(
                "Today's mission board will appear here",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}