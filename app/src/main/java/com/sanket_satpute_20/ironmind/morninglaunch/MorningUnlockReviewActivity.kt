package com.sanket_satpute_20.ironmind.morninglaunch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.confidence.ConfidenceIgnitionActivity
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionStatus
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

class MorningUnlockReviewActivity : ComponentActivity() {

    private var advancingToLaunch = false

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, MorningUnlockReviewActivity::class.java).apply {
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
        onBackPressedDispatcher.addCallback(this) { }

        setContent {
            IronMindTheme {
                MorningUnlockReviewRoute(
                    onContinue = {
                        advancingToLaunch = true
                        startActivity(ConfidenceIgnitionActivity.createIntent(this))
                        finish()
                    }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isFinishing && !advancingToLaunch) startActivity(createIntent(this))
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing && !isChangingConfigurations && !advancingToLaunch) {
            startActivity(createIntent(this))
        }
    }
}

@Composable
private fun MorningUnlockReviewRoute(
    onContinue: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val today = remember { LocalDate.now() }
    val database = remember { IronMindDatabase.getDatabase(context) }
    val rawTasks by database.taskDao().getTasksForDate(today.toString()).collectAsState(initial = emptyList())
    val nightDecisionManager = remember { NightDecisionManager(context) }
    val nightDecisionStatus = nightDecisionManager.statusForDate(today)
    val tasks = remember(rawTasks) {
        rawTasks
            .filterNot { it.isSkipped || it.isCompleted || it.isBreakSegment }
            .sortedWith(compareBy<Task> { it.startTime }.thenBy { it.id })
    }

    MorningUnlockReviewScreen(
        today = today,
        tasks = tasks,
        nightDecisionStatus = nightDecisionStatus,
        onContinue = onContinue
    )
}

@Composable
private fun MorningUnlockReviewScreen(
    today: LocalDate,
    tasks: List<Task>,
    nightDecisionStatus: NightDecisionStatus,
    onContinue: () -> Unit
) {
    val accent = WarningAmber
    val dateLabel = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.US)).uppercase()
    }
    val headline = when (nightDecisionStatus) {
        NightDecisionStatus.PLANNED -> "TODAY'S BOARD IS LOCKED"
        NightDecisionStatus.HOLIDAY -> "TODAY IS A HOLIDAY"
        NightDecisionStatus.EMERGENCY_LEAVE -> "TODAY IS EMERGENCY LEAVE"
        NightDecisionStatus.UNDECIDED -> "NO BOARD WAS LOCKED"
    }
    val subline = when (nightDecisionStatus) {
        NightDecisionStatus.PLANNED ->
            if (tasks.isEmpty()) "You cleared the night decision, but no missions are on today's board." else "See the board clearly before the day gets noisy."
        NightDecisionStatus.HOLIDAY -> "Recovery is the plan. No missions were armed for today."
        NightDecisionStatus.EMERGENCY_LEAVE -> "Today is sealed as leave. Protect the day and do not fake a work board."
        NightDecisionStatus.UNDECIDED -> "Nothing was locked last night. Start consciously instead of drifting."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, SurfaceDark, DeepBackground)
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
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "MORNING BRIEF",
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = dateLabel,
                        color = Color.White.copy(alpha = 0.58f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = headline,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 30.sp,
                        lineHeight = 34.sp
                    )
                    Text(
                        text = subline,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }

                MorningReviewMetaRow(
                    missionCount = tasks.count { !it.isSkipped },
                    topTime = tasks.firstOrNull { !it.isSkipped }?.startTime ?: "--:--",
                    accent = accent
                )

                if (tasks.isNotEmpty()) {
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
                            items(tasks.take(6)) { task ->
                                MorningReviewTaskRow(task = task, accent = accent)
                            }
                            item { Spacer(Modifier.height(6.dp)) }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = when (nightDecisionStatus) {
                                NightDecisionStatus.HOLIDAY -> "No missions. Protect recovery and keep the day clean."
                                NightDecisionStatus.EMERGENCY_LEAVE -> "No missions. Use the day honestly and come back stronger."
                                else -> "No missions are waiting. Build the day deliberately once the launch is complete."
                            },
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = "See the board. Know the first move. Then keep going.",
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                    )
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "CONTINUE",
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
private fun MorningReviewMetaRow(
    missionCount: Int,
    topTime: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MorningReviewMetaPill(
            modifier = Modifier.weight(1f),
            label = "MISSIONS",
            value = missionCount.toString(),
            accent = accent
        )
        MorningReviewMetaPill(
            modifier = Modifier.weight(1f),
            label = "FIRST MOVE",
            value = topTime,
            accent = accent
        )
    }
}

@Composable
private fun MorningReviewMetaPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.56f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = value,
                color = accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun MorningReviewTaskRow(
    task: Task,
    accent: Color
) {
    val importanceTone = when (task.importanceRank) {
        1 -> WarningAmber
        2 -> NeonCyan
        else -> WarningAmber
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = importanceTone.copy(alpha = 0.14f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, importanceTone.copy(alpha = 0.3f))
        ) {
            Text(
                text = task.startTime,
                color = importanceTone,
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
            text = when (task.importanceRank) {
                1 -> "TOP"
                2 -> "CORE"
                else -> "BUILD"
            },
            color = accent.copy(alpha = 0.85f),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@androidx.compose.runtime.Composable
fun MorningUnlockReviewScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        MorningUnlockReviewPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun MorningUnlockReviewPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                "🌅 Morning Unlock Review",
                color = WarningAmber,
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
            androidx.compose.material3.Text(
                "FRIDAY, 22 AUG",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}