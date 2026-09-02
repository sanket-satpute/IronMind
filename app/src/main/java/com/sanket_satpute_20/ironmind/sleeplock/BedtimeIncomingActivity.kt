package com.sanket_satpute_20.ironmind.sleeplock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.alarm.AlarmScheduler
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.data.TaskEvent
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionManager
import com.sanket_satpute_20.ironmind.nightdecision.NightDecisionStatus
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

private val NightDeep = DeepBackground
private val NightMid = DeepBackground
private val NightFloor = DeepBackground
private val NightCard = SurfaceDark
private val NightCardAlt = SurfaceDark
private val NightBlue = TextPrimary
private val NightBlueMid = NeonCyan
private val NightAmber = WarningAmber
private val NightWhiteHigh = Color.White.copy(alpha = 0.90f)
private val NightWhiteMed = Color.White.copy(alpha = 0.72f)
private val NightWhiteLow = Color.White.copy(alpha = 0.56f)
private val NightBorder = Color.White.copy(alpha = 0.08f)

class BedtimeIncomingActivity : ComponentActivity() {

    companion object {
        private const val ACTION_FINISH = "com.sanket_satpute_20.ironmind.sleeplock.BEDTIME_INCOMING_FINISH"

        fun createIntent(context: Context): Intent =
            Intent(context, BedtimeIncomingActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }

        fun finishIntent(context: Context): Intent =
            Intent(ACTION_FINISH).setPackage(context.packageName)
    }

    private var actionChosen = false

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            actionChosen = true
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SleepLockManager(this).isActive()) {
            actionChosen = true
            startActivity(SleepLockActivity.createIntent(this))
            finish()
            return
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        onBackPressedDispatcher.addCallback(this) { }

        val filter = IntentFilter(ACTION_FINISH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(finishReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(finishReceiver, filter)
        }

        setContent {
            IronMindTheme {
                BedtimeIncomingRoute(
                    onFinishNow = {
                        actionChosen = true
                        HistoryRecorder.recordConfigChange(
                            this,
                            "BEDTIME_INCOMING_ACTION",
                            "",
                            "FINISH_NOW",
                            "SLEEP_LOCK"
                        )
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
                    },
                    onMoveToTomorrow = {
                        actionChosen = true
                        finish()
                    },
                    onAcceptTonight = {
                        actionChosen = true
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(finishReceiver) }
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isFinishing && !actionChosen) {
            startActivity(createIntent(this))
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing && !isChangingConfigurations && !actionChosen) {
            startActivity(createIntent(this))
        }
    }
}

@Composable
private fun BedtimeIncomingRoute(
    onFinishNow: () -> Unit,
    onMoveToTomorrow: () -> Unit,
    onAcceptTonight: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { IronMindDatabase.getDatabase(context) }
    val manager = remember { SleepLockManager(context) }
    val nightDecisionManager = remember { NightDecisionManager(context) }
    val today = remember { LocalDate.now() }
    val tomorrow = remember(today) { today.plusDays(1) }
    val allTasks by db.taskDao().getTasksForDate(today.toString()).collectAsState(initial = emptyList())
    val tomorrowTasks by db.taskDao().getTasksForDate(tomorrow.toString()).collectAsState(initial = emptyList())
    val pendingTasks = remember(allTasks) {
        allTasks.filter { !it.isBreakSegment && !it.isCompleted && !it.isSkipped }
    }
    val liveTomorrowTasks = remember(tomorrowTasks) {
        tomorrowTasks.filter { !it.isBreakSegment && !it.isCompleted && !it.isSkipped }
    }
    val tomorrowStatus = remember { nightDecisionManager.statusForDate(tomorrow) }
    val moveConflictCount = remember(pendingTasks, liveTomorrowTasks) {
        pendingTasks.count { pending ->
            liveTomorrowTasks.any { existing ->
                tasksOverlap(existing, pending)
            }
        }
    }
    val wouldExceedMissionCap = remember(pendingTasks, liveTomorrowTasks) {
        liveTomorrowTasks.size + pendingTasks.size > 6
    }
    val canMoveToTomorrow = remember(
        tomorrow,
        nightDecisionManager,
        pendingTasks,
        liveTomorrowTasks,
        moveConflictCount,
        wouldExceedMissionCap
    ) {
        nightDecisionManager.isTaskCreationAllowedForDate(tomorrow) &&
            pendingTasks.isNotEmpty() &&
            moveConflictCount == 0 &&
            !wouldExceedMissionCap
    }
    val moveBlockedReason = remember(tomorrowStatus, pendingTasks, moveConflictCount, wouldExceedMissionCap) {
        when {
            pendingTasks.isEmpty() -> "Nothing is left to move."
            tomorrowStatus == NightDecisionStatus.HOLIDAY ->
                "Tomorrow is already sealed as a holiday."
            tomorrowStatus == NightDecisionStatus.EMERGENCY_LEAVE ->
                "Tomorrow is already sealed as emergency leave."
            wouldExceedMissionCap ->
                "Moving everything would overload tomorrow beyond the 6-mission cap."
            moveConflictCount > 0 ->
                "One or more open missions collide with tomorrow's existing schedule."
            else -> null
        }
    }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            nowMillis = System.currentTimeMillis()
        }
    }

    val bedtime = remember(manager) { manager.getBedtime() }
    val countdownLabel = remember(nowMillis, bedtime) {
        val now = LocalDateTime.now()
        val target = now.toLocalDate().atTime(bedtime)
        val safeTarget = if (target.isBefore(now)) target.plusDays(1) else target
        val duration = Duration.between(now, safeTarget)
        val totalSeconds = duration.seconds.coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    BedtimeIncomingScreen(
        bedtime = bedtime,
        countdownLabel = countdownLabel,
        pendingTasks = pendingTasks,
        visibleTaskCount = pendingTasks.take(6).size,
        moveToTomorrowEnabled = canMoveToTomorrow && pendingTasks.isNotEmpty(),
        moveToTomorrowReason = moveBlockedReason,
        onFinishNow = onFinishNow,
        onMoveToTomorrow = {
            if (!canMoveToTomorrow || pendingTasks.isEmpty()) return@BedtimeIncomingScreen
            scope.launch(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                pendingTasks.forEach { task ->
                    val movedTask = task.copy(
                        date = tomorrow.toString(),
                        isDeferred = false,
                        deferredAt = null,
                        isRescheduled = true,
                        originalStartTime = task.originalStartTime.ifBlank { task.startTime },
                        originalEndTime = task.originalEndTime.ifBlank { task.endTime },
                        rescheduleReason = "BEDTIME_INCOMING_MOVE_TO_TOMORROW",
                        lastModified = now,
                        syncStatus = "PENDING"
                    )
                    db.taskDao().updateTask(movedTask)
                    db.taskEventDao().insert(
                        TaskEvent(
                            taskId = movedTask.id,
                            taskName = movedTask.name,
                            date = movedTask.date,
                            eventType = "RESCHEDULED",
                            timestamp = now,
                            oldStartTime = task.startTime,
                            oldEndTime = task.endTime,
                            newStartTime = movedTask.startTime,
                            newEndTime = movedTask.endTime,
                            reason = "BEDTIME_INCOMING_MOVE_TO_TOMORROW"
                        )
                    )
                    AlarmScheduler.scheduleTaskAlarms(context, movedTask)
                }
                HistoryRecorder.recordConfigChange(
                    context,
                    "BEDTIME_INCOMING_ACTION",
                    "",
                    "MOVE_TO_TOMORROW_${pendingTasks.size}",
                    "SLEEP_LOCK"
                )
                launch(Dispatchers.Main) { onMoveToTomorrow() }
            }
        },
        onAcceptTonight = {
            HistoryRecorder.recordConfigChange(
                context,
                "BEDTIME_INCOMING_ACTION",
                "",
                "ACCEPT_TONIGHT_${pendingTasks.size}",
                "SLEEP_LOCK"
            )
            onAcceptTonight()
        }
    )
}

@Composable
private fun BedtimeIncomingScreen(
    bedtime: LocalTime,
    countdownLabel: String,
    pendingTasks: List<Task>,
    visibleTaskCount: Int,
    moveToTomorrowEnabled: Boolean,
    moveToTomorrowReason: String?,
    onFinishNow: () -> Unit,
    onMoveToTomorrow: () -> Unit,
    onAcceptTonight: () -> Unit
) {
    val bedtimeLabel = remember(bedtime) {
        bedtime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BedtimeIncomingBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = NightAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, NightAmber.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = "SLEEP LOCK INCOMING",
                        color = NightAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Text(
                    text = "SLEEP WINDOW STARTS IN\n$countdownLabel",
                    color = NightWhiteHigh,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    lineHeight = 34.sp
                )

                Text(
                    text = "Bedtime is set for $bedtimeLabel. Close what still matters or move it consciously. When the clock turns, the phone shifts into protected night mode.",
                    color = NightWhiteMed,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NightCardAlt,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, NightAmber.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LANDING CHOICE",
                            color = NightAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.8.sp
                        )
                        Text(
                            text = if (pendingTasks.isEmpty()) {
                                "The board is already clean. Accept tonight and let the handoff into sleep stay quiet."
                            } else {
                                "Finish what truly matters now, or accept tonight and let the remaining tasks land without a last-minute spiral."
                            },
                            color = NightWhiteHigh,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NightCard,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, NightBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${pendingTasks.size} MISSIONS STILL OPEN",
                            color = NightAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        if (pendingTasks.size > visibleTaskCount) {
                            Text(
                                text = "Showing the first $visibleTaskCount. Any move applies to all ${pendingTasks.size} open missions.",
                                color = NightWhiteLow,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                        if (pendingTasks.isEmpty()) {
                            Text(
                                text = "The board is clean. Let the handoff into sleep stay quiet.",
                                color = NightWhiteMed,
                                fontSize = 14.sp
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(pendingTasks.take(6)) { task ->
                                    BedtimePendingTaskRow(task = task)
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NightCardAlt,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, NightAmber.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NEXT STATE",
                            color = NightAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.8.sp
                        )
                        Text(
                            text = "At bedtime, IronMind switches from warning mode into full Sleep Lock. Only emergency access remains easy to reach.",
                            color = NightWhiteHigh,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                moveToTomorrowReason?.let { reason ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = NightBlue.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, NightBlue.copy(alpha = 0.16f))
                    ) {
                        Text(
                            text = reason,
                            color = NightWhiteMed,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onFinishNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NightAmber,
                        contentColor = Color.Black
                    )
                ) {
                    Text("FINISH NOW", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }

                Button(
                    onClick = onAcceptTonight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NightCard,
                        contentColor = NightWhiteHigh
                    )
                ) {
                    Text("ACCEPT TONIGHT", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Button(
                    onClick = onMoveToTomorrow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = moveToTomorrowEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        contentColor = NightWhiteHigh
                    ).copy(
                        disabledContainerColor = NightCardAlt,
                        disabledContentColor = NightWhiteLow.copy(alpha = 0.75f)
                    )
                ) {
                    Text("MOVE TO TOMORROW", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun BedtimeIncomingBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NightDeep, NightMid, NightFloor)
                )
            )
    )
    BedtimeStarField(modifier = Modifier.fillMaxSize())
    GlowingMoonOverlay(backgroundColor = NightDeep, modifier = Modifier.fillMaxSize())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        NightBlueMid.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset.Unspecified,
                    radius = 900f
                )
            )
    )
}

@Composable
internal fun GlowingMoonOverlay(backgroundColor: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val radius = 160f
        val centerOffset = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.18f)
        
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(Color(0xFFE2E8F0).copy(alpha = 0.25f), Color.Transparent),
                center = centerOffset,
                radius = radius * 4f
            ),
            radius = radius * 4f,
            center = centerOffset
        )
        
        drawCircle(
            color = Color(0xFFE2E8F0),
            radius = radius,
            center = centerOffset
        )
        
        drawCircle(
            color = backgroundColor,
            radius = radius * 0.85f,
            center = androidx.compose.ui.geometry.Offset(centerOffset.x - radius * 0.35f, centerOffset.y - radius * 0.35f)
        )
    }
}

@Composable
private fun BedtimeStarField(modifier: Modifier = Modifier) {
    data class Star(val xRatio: Float, val yRatio: Float, val radius: Float, val baseAlpha: Float, val speed: Int)
    val stars = remember {
        List(120) {
            Star(
                xRatio = kotlin.random.Random.nextFloat(),
                yRatio = kotlin.random.Random.nextFloat(),
                radius = kotlin.random.Random.nextFloat() * 1.8f + 0.4f,
                baseAlpha = kotlin.random.Random.nextFloat() * 0.4f + 0.05f,
                speed = 600 + kotlin.random.Random.nextInt(1800)
            )
        }
    }

    // Global twinkling animation driver — 3 offsets to create staggered twinkle phases
    val transition = rememberInfiniteTransition(label = "twinkle")
    val twinkle0 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Reverse),
        label = "t0"
    )
    val twinkle1 by transition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Reverse),
        label = "t1"
    )
    val twinkle2 by transition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Reverse),
        label = "t2"
    )
    val twinkleValues = listOf(twinkle0, twinkle1, twinkle2)

    Canvas(modifier = modifier) {
        stars.forEachIndexed { index, star ->
            val twinkle = twinkleValues[index % 3]
            drawCircle(
                color = Color.White.copy(alpha = star.baseAlpha * twinkle),
                radius = star.radius * density,
                center = Offset(star.xRatio * size.width, star.yRatio * size.height)
            )
        }
    }
}


@Composable
private fun BedtimePendingTaskRow(task: Task) {
    val tone = when (task.importanceRank) {
        1 -> NightAmber
        2 -> NightBlue
        else -> WarningAmber
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = tone.copy(alpha = 0.14f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, tone.copy(alpha = 0.28f))
        ) {
            Text(
                text = task.startTime,
                color = tone,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.name.uppercase(),
                color = NightWhiteHigh,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${task.startTime} - ${task.endTime}",
                color = NightWhiteLow,
                fontSize = 12.sp
            )
        }
        Text(
            text = when (task.importanceRank) {
                1 -> "TOP"
                2 -> "CORE"
                else -> "BUILD"
            },
            color = NightWhiteMed,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp
        )
    }
}

private fun tasksOverlap(existing: Task, incoming: Task): Boolean {
    val existingStart = parseTimeForOverlap(existing.startTime) ?: return false
    val existingEnd = parseTimeForOverlap(existing.endTime) ?: return false
    val incomingStart = parseTimeForOverlap(incoming.startTime) ?: return false
    val incomingEnd = parseTimeForOverlap(incoming.endTime) ?: return false
    return incomingStart < existingEnd && incomingEnd > existingStart
}

private fun parseTimeForOverlap(value: String): LocalTime? {
    val formats = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
    for (format in formats) {
        runCatching {
            return LocalTime.parse(
                value.trim().uppercase(),
                DateTimeFormatter.ofPattern(format, Locale.US)
            )
        }
    }
    return null
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun BedtimeIncomingRoutePreview() {
    IronMindTheme {
        BedtimeIncomingRoute(onFinishNow = {}, onMoveToTomorrow = {}, onAcceptTonight = {})
    }
}
