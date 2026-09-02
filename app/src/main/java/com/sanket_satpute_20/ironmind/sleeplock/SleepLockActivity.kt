package com.sanket_satpute_20.ironmind.sleeplock

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhoneEnabled
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.HonestyOverridePanel
import com.sanket_satpute_20.ironmind.settings.getDefaultDialerPackage
import com.sanket_satpute_20.ironmind.settings.getDefaultSmsPackage
import com.sanket_satpute_20.ironmind.ui.components.AppIconImage
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

private val BG_DEEP       = DeepBackground
private val BG_MID        = DeepBackground
private val BG_CARD       = SurfaceDark
private val BG_CARD_ALT   = SurfaceDark
private val ICE_BLUE      = TextPrimary
private val ICE_BLUE_MID  = NeonCyan
private val AMBER         = WarningAmber
private val WHITE_HIGH    = Color.White.copy(alpha = 0.90f)
private val WHITE_MED     = Color.White.copy(alpha = 0.55f)
private val WHITE_LOW     = Color.White.copy(alpha = 0.28f)
private val WHITE_HINT    = Color.White.copy(alpha = 0.08f)
private val BORDER_SUBTLE = Color.White.copy(alpha = 0.07f)

class SleepLockActivity : ComponentActivity() {

    companion object {
        private const val ACTION_FINISH =
            "com.sanket_satpute_20.ironmind.sleeplock.FINISH_ACTIVITY"
        var emergencyAppLaunching = false

        fun createIntent(context: Context): Intent =
            Intent(context, SleepLockActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }

        fun finishIntent(context: Context): Intent =
            Intent(ACTION_FINISH).setPackage(context.packageName)
    }

    private var finishReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SleepLockManager(this).syncPersistentState()) {
            finish()
            return
        }
        setupWindowFlags()
        onBackPressedDispatcher.addCallback(this) { moveTaskToBack(true) }
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { finish() }
        }
        registerReceiver(
            finishReceiver,
            IntentFilter(ACTION_FINISH),
            RECEIVER_NOT_EXPORTED
        )
        setContent { 
            IronMindTheme { 
                com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry {
                    SleepLockActiveScreen() 
                }
            } 
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val prefs = PrefManager.getInstance(this)
        if (emergencyAppLaunching) { emergencyAppLaunching = false; return }
        if (prefs.sleepLockActive) startActivity(createIntent(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(finishReceiver) }
    }

    private fun setupWindowFlags() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }
}

@Composable
fun SleepLockActiveScreen() {
    val context          = androidx.compose.ui.platform.LocalContext.current
    val prefs            = remember { PrefManager.getInstance(context) }
    val manager          = remember { SleepLockManager(context) }
    val soundController  = remember { SleepLockSoundController(context) }
    val coroutineScope   = rememberCoroutineScope()
    val scrollState      = rememberScrollState()

    val sound = remember(prefs.sleepLockSelectedSound) {
        SleepLockSoundCatalog.byId(prefs.sleepLockSelectedSound)
    }
    var soundMode by remember {
        mutableStateOf(SleepLockSoundMode.fromWireValue(prefs.sleepLockSoundMode))
    }
    var selectedSoundIds by remember {
        mutableStateOf(
            SleepLockSoundCatalog.normalizeIds(
                prefs.sleepLockSelectedSounds,
                prefs.sleepLockSelectedSound
            )
        )
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.sleep_animation)
    )
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations  = LottieConstants.IterateForever
    )

    var emergencyPanelOpen by remember { mutableStateOf(false) }
    var isPlaying          by remember { mutableStateOf(soundController.isPlaying()) }
    var isDownloading      by remember { mutableStateOf(false) }
    var soundError         by remember { mutableStateOf<String?>(null) }
    var isDownloaded       by remember(sound.id) {
        mutableStateOf(soundController.isDownloaded(sound.id))
    }
    var nowMillis          by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showEmergencyExitDialog by remember { mutableStateOf(false) }

    val fmtTime12  = remember { DateTimeFormatter.ofPattern("h:mm") }
    val fmtAmPm    = remember { DateTimeFormatter.ofPattern("a") }
    val fmtWake    = remember { DateTimeFormatter.ofPattern("h:mm a") }

    val wakeTime = remember(prefs.sleepLockWakeHour, prefs.sleepLockWakeMinute) {
        manager.getWakeTime().format(fmtWake)
    }
    val totalWindowMillis = remember(prefs.sleepLockStartedAt, prefs.sleepLockEndsAt) {
        (prefs.sleepLockEndsAt - prefs.sleepLockStartedAt).coerceAtLeast(1L)
    }
    val emergencyExitRemaining = remember(
        prefs.sleepLockEmergencyExitCount,
        prefs.sleepLockEmergencyExitMonth
    ) {
        manager.remainingEmergencyExitAllowance()
    }

    val remainingMillis = (prefs.sleepLockEndsAt - nowMillis).coerceAtLeast(0L)
    val progress = (remainingMillis.toFloat() / totalWindowMillis.toFloat()).coerceIn(0f, 1f)
    val remainingLabel = formatRemainingTime(remainingMillis)

    val currentDt = remember(nowMillis) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), ZoneId.systemDefault())
    }
    val currentTimeStr = currentDt.format(fmtTime12)
    val currentAmPm    = currentDt.format(fmtAmPm).lowercase()

    LaunchedEffect(prefs.sleepLockSoundEnabled, sound.id, isDownloaded) {
        if (prefs.sleepLockSoundEnabled && isDownloaded && !soundController.isPlaying()) {
            soundController.play()
            isPlaying = soundController.isPlaying()
        }
    }

    LaunchedEffect(prefs.sleepLockSelectedSound, prefs.sleepLockSelectedSounds) {
        selectedSoundIds = SleepLockSoundCatalog.normalizeIds(
            prefs.sleepLockSelectedSounds,
            prefs.sleepLockSelectedSound
        )
    }

    LaunchedEffect(prefs.sleepLockSoundMode) {
        soundMode = SleepLockSoundMode.fromWireValue(prefs.sleepLockSoundMode)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == SleepLockSoundService.ACTION_STATE_CHANGED) {
                    isPlaying = intent.getBooleanExtra(
                        SleepLockSoundService.EXTRA_IS_PLAYING,
                        soundController.isPlaying()
                    )
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(SleepLockSoundService.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    LaunchedEffect(Unit) {
        while (true) { delay(1_000); nowMillis = System.currentTimeMillis() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SleepLockBackdrop()

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val heroHeight = maxHeight * 0.64f
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = heroHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SleepStatusPill()
                            WakeChip(wakeTime)
                        }

                        Spacer(Modifier.height(26.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text       = currentTimeStr,
                                color      = WHITE_HIGH,
                                fontSize   = 56.sp,
                                fontWeight = FontWeight.Light,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-1.5).sp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text       = currentAmPm,
                                color      = WHITE_LOW,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Normal,
                                modifier   = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text      = "your phone rests. so should you.",
                            color     = WHITE_LOW,
                            fontSize  = 12.sp,
                            letterSpacing = 0.2.sp
                        )

                        Spacer(Modifier.height(26.dp))

                        SleepProgressRing(
                            progress  = progress,
                            modifier  = Modifier.size(300.dp)
                        ) {
                            LottieAnimation(
                                composition = composition,
                                progress    = { animationProgress },
                                modifier    = Modifier.size(84.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text          = remainingLabel,
                                color         = Color.White,
                                fontSize      = 42.sp,
                                fontWeight    = FontWeight.Black,
                                fontFamily    = FontFamily.Monospace,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text      = "until morning",
                                color     = WHITE_LOW,
                                fontSize  = 11.sp,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.height(14.dp))
                            ProgressPercentChip(progress)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                SleepStateCard(
                    wakeTime = wakeTime,
                    emergencyExitRemaining = emergencyExitRemaining
                )

                Spacer(Modifier.height(12.dp))

                val soundSubtitle = when {
                soundError != null -> "download failed"
                isDownloading -> "downloading..."
                soundMode == SleepLockSoundMode.BLEND && selectedSoundIds.size > 1 && isPlaying -> "blend is playing"
                soundMode == SleepLockSoundMode.BLEND && selectedSoundIds.size > 1 -> "blend is armed"
                soundMode == SleepLockSoundMode.ROTATE && selectedSoundIds.size > 1 && isPlaying -> "deck is rotating"
                soundMode == SleepLockSoundMode.ROTATE && selectedSoundIds.size > 1 -> "deck rotation is armed"
                isDownloaded && isPlaying -> "playing now"
                isDownloaded -> "tap to play"
                else -> "tap to download first"
            }
                val soundAction = when {
                isDownloading -> "..."
                isDownloaded && isPlaying -> "Pause"
                isDownloaded -> "Play"
                else -> "Download"
                }
                val soundIcon = when {
                isDownloaded && isPlaying -> Icons.Rounded.Pause
                isDownloaded -> Icons.Rounded.PlayArrow
                else -> Icons.Rounded.Download
                }

                if (prefs.sleepLockSoundEnabled) {
                    ControlPill(
                        icon        = soundIcon,
                        accentColor = ICE_BLUE,
                        title       = sound.title,
                        subtitle    = "${selectedSoundIds.size} in night deck • ${soundMode.name.lowercase()} • $soundSubtitle",
                        actionLabel = soundAction,
                        onClick = {
                            if (isDownloaded) {
                                soundError = null
                                if (isPlaying) soundController.pause() else soundController.play()
                                isPlaying = soundController.isPlaying()
                            } else if (!isDownloading) {
                                soundError = null
                                isDownloading = true
                                coroutineScope.launch {
                                    val result = soundController.downloadSound(sound.id)
                                    isDownloading = false
                                    isDownloaded  = soundController.isDownloaded(sound.id)
                                    soundError = result.exceptionOrNull()?.message
                                    if (isDownloaded && prefs.sleepLockSoundEnabled) {
                                        soundController.play()
                                        isPlaying = soundController.isPlaying()
                                    }
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    SleepSoundModeStrip(
                        selectedMode = soundMode,
                        onSelected = { nextMode ->
                            soundMode = nextMode
                            prefs.sleepLockSoundMode = nextMode.name
                            if (isPlaying) {
                                soundController.stop()
                                soundController.play()
                                isPlaying = soundController.isPlaying()
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    SleepSoundDeckCard(
                        sounds = SleepLockSoundCatalog.sounds,
                        selectedSoundId = sound.id,
                        selectedSoundIds = selectedSoundIds,
                        soundController = soundController,
                        onChange = { nextSelectedIds, nextCurrentId ->
                            val normalized = SleepLockSoundCatalog.normalizeIds(nextSelectedIds, nextCurrentId)
                            selectedSoundIds = normalized
                            prefs.sleepLockSelectedSounds = normalized
                            prefs.sleepLockSelectedSound = SleepLockSoundCatalog.byId(nextCurrentId).id
                            isDownloaded = soundController.isDownloaded(prefs.sleepLockSelectedSound)
                            if (isPlaying) {
                                soundController.stop()
                                soundController.play()
                                isPlaying = soundController.isPlaying()
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))
                }

                ControlPill(
                    icon        = Icons.Rounded.Security,
                    accentColor = AMBER,
                    title       = "Emergency Access",
                    subtitle    = if (emergencyPanelOpen) "Dialer, SMS, and allowed emergency apps are visible now" else "Open the safe access panel only if something real needs attention",
                    actionLabel = if (emergencyPanelOpen) "Hide" else "Open",
                    onClick     = { emergencyPanelOpen = !emergencyPanelOpen }
                )

                Spacer(Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = emergencyPanelOpen,
                    enter   = expandVertically() + fadeIn(),
                    exit    = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        SleepLockEmergencyPanel(
                            emergencyPackages = manager.coreEmergencyPackages() +
                                prefs.sleepLockEmergencyApps,
                            onLaunchDialer = {
                                runCatching {
                                    SleepLockActivity.emergencyAppLaunching = true
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                }
                            },
                            onLaunchSms = {
                                runCatching {
                                    SleepLockActivity.emergencyAppLaunching = true
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW).apply {
                                            data = "sms:".toUri()
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                }
                            },
                            onLaunchPackage = { pkg ->
                                context.packageManager
                                    .getLaunchIntentForPackage(pkg)
                                    ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    ?.also {
                                        SleepLockActivity.emergencyAppLaunching = true
                                        context.startActivity(it)
                                    }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    onClick = { if (emergencyExitRemaining > 0) showEmergencyExitDialog = true },
                    color = SurfaceDark.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        1.dp,
                        if (emergencyExitRemaining > 0) WarningAmber.copy(alpha = 0.24f)
                        else Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Text(
                        text = if (emergencyExitRemaining > 0) {
                            "Safety Exit • $emergencyExitRemaining left this month"
                        } else {
                            "Safety Exit • no exits left this month"
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = if (emergencyExitRemaining > 0) TextPrimary else Color.White.copy(alpha = 0.42f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Honesty mode: transparent override with logged reason
                HonestyOverridePanel(
                    lockType = "SLEEP_LOCK",
                    onOverride = {
                        SleepLockAlarmReceiver.dispatchNow(
                            context,
                            SleepLockAlarmReceiver.ACTION_EMERGENCY_EXIT
                        )
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showEmergencyExitDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyExitDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    "Use safety exit?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This closes Sleep Lock for the rest of the current night window, restores normal notifications, records the override in your history, and uses 1 of your $emergencyExitRemaining monthly exits.",
                    color = Color.White.copy(alpha = 0.72f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyExitDialog = false
                        HistoryRecorder.recordConfigChange(
                            context,
                            "SLEEP_LOCK_EMERGENCY_EXIT_REQUESTED",
                            false,
                            true,
                            "SLEEP_LOCK"
                        )
                        SleepLockAlarmReceiver.dispatchNow(
                            context,
                            SleepLockAlarmReceiver.ACTION_EMERGENCY_EXIT
                        )
                    }
                ) {
                    Text("Use safety exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyExitDialog = false }) {
                    Text("Stay in Sleep Lock")
                }
            }
        )
    }
}

@Composable
private fun SleepStateCard(
    wakeTime: String,
    emergencyExitRemaining: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BG_CARD_ALT,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.5.dp, BORDER_SUBTLE)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "NIGHT STATE",
                color = ICE_BLUE,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "Sleep Lock is active until $wakeTime. Only emergency apps stay easy to reach from here.",
                color = WHITE_HIGH,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Text(
                text = if (emergencyExitRemaining > 0) {
                    "$emergencyExitRemaining safety exits remain this month."
                } else {
                    "No safety exits remain this month."
                },
                color = WHITE_LOW,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SleepSoundModeStrip(
    selectedMode: SleepLockSoundMode,
    onSelected: (SleepLockSoundMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BG_CARD,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(0.5.dp, BORDER_SUBTLE)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "NIGHT AUDIO MODE",
                        color = ICE_BLUE,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = when (selectedMode) {
                            SleepLockSoundMode.SINGLE -> "One sound holds the room steady."
                            SleepLockSoundMode.BLEND -> "Selected sounds layer into one calm scene."
                            SleepLockSoundMode.ROTATE -> "The deck shifts softly through the night."
                        },
                        color = WHITE_LOW,
                        fontSize = 11.sp
                    )
                }
                MiniStateChip(
                    label = when (selectedMode) {
                        SleepLockSoundMode.SINGLE -> "FOCUS"
                        SleepLockSoundMode.BLEND -> "BLEND"
                        SleepLockSoundMode.ROTATE -> "ROTATE"
                    },
                    accent = if (selectedMode == SleepLockSoundMode.SINGLE) ICE_BLUE else AMBER
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SleepLockSoundMode.entries.forEach { mode ->
                    val selected = mode == selectedMode
                    Surface(
                        modifier = Modifier.gamifiedClick { onSelected(mode) },
                        color = if (selected) ICE_BLUE.copy(alpha = 0.10f) else BG_CARD_ALT,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.5.dp, if (selected) ICE_BLUE.copy(alpha = 0.32f) else BORDER_SUBTLE)
                    ) {
                        Text(
                            text = when (mode) {
                                SleepLockSoundMode.SINGLE -> "Single"
                                SleepLockSoundMode.BLEND -> "Blend"
                                SleepLockSoundMode.ROTATE -> "Rotate"
                            },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (selected) ICE_BLUE else WHITE_HIGH,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepSoundDeckCard(
    sounds: List<SleepSoundOption>,
    selectedSoundId: String,
    selectedSoundIds: Set<String>,
    soundController: SleepLockSoundController,
    onChange: (Set<String>, String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloadingId by remember { mutableStateOf<String?>(null) }
    val activeSound = remember(selectedSoundId) { SleepLockSoundCatalog.byId(selectedSoundId) }
    val readyCount = remember(selectedSoundIds, sounds) {
        selectedSoundIds.count { id -> soundController.isDownloaded(id) }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BG_CARD,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(0.5.dp, BORDER_SUBTLE)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NIGHT SOUND DECK",
                        color = ICE_BLUE,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.6.sp
                    )
                    Text(
                        text = "${activeSound.title} is armed right now.",
                        color = WHITE_HIGH,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Keep only the sounds you want available tonight. Tap any included one to make it active.",
                        color = WHITE_LOW,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniStateChip(label = "$readyCount READY", accent = SuccessGreen)
                    MiniStateChip(label = "${selectedSoundIds.size} IN DECK", accent = ICE_BLUE)
                }
            }

            sounds.forEach { option ->
                val included = option.id in selectedSoundIds
                val active = option.id == selectedSoundId
                val downloaded = soundController.isDownloaded(option.id)
                val actionLabel = when {
                    !downloaded && downloadingId == option.id -> "..."
                    !downloaded -> "Download"
                    !included -> "Add"
                    active && selectedSoundIds.size > 1 -> "Remove"
                    active -> "Active"
                    else -> "Use"
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (active) ICE_BLUE.copy(alpha = 0.08f) else BG_CARD_ALT,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        0.5.dp,
                        when {
                            active -> ICE_BLUE.copy(alpha = 0.3f)
                            included -> Color.White.copy(alpha = 0.12f)
                            else -> Color.White.copy(alpha = 0.06f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    if (active) ICE_BLUE.copy(alpha = 0.14f)
                                    else Color.White.copy(alpha = 0.05f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.title.take(1),
                                color = if (active) ICE_BLUE else WHITE_HIGH,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = option.title,
                                color = if (active) WHITE_HIGH else Color.White.copy(alpha = if (included) 0.92f else 0.62f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = option.subtitle,
                                color = WHITE_LOW,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MiniStateChip(
                                    label = if (included) "IN DECK" else "OUT",
                                    accent = if (included) ICE_BLUE else WHITE_LOW
                                )
                                if (active) {
                                    MiniStateChip(label = "ACTIVE", accent = AMBER)
                                }
                                if (downloaded) {
                                    MiniStateChip(label = "READY", accent = SuccessGreen)
                                }
                            }
                        }

                        Surface(
                            color = if (active) ICE_BLUE.copy(alpha = 0.14f) else ICE_BLUE.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, ICE_BLUE.copy(alpha = if (active) 0.3f else 0.18f)),
                            modifier = Modifier.gamifiedClick {
                                when {
                                    !downloaded -> {
                                        if (downloadingId == null) {
                                            downloadingId = option.id
                                            coroutineScope.launch {
                                                soundController.downloadSound(option.id)
                                                downloadingId = null
                                                if (option.id !in selectedSoundIds) {
                                                    onChange(selectedSoundIds + option.id, option.id)
                                                }
                                            }
                                        }
                                    }
                                    !included -> {
                                        onChange(selectedSoundIds + option.id, option.id)
                                    }
                                    active && selectedSoundIds.size > 1 -> {
                                        val remaining = selectedSoundIds - option.id
                                        onChange(remaining, remaining.first())
                                    }
                                    else -> {
                                        onChange(selectedSoundIds, option.id)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = actionLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                color = ICE_BLUE,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStateChip(
    label: String,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(0.5.dp, accent.copy(alpha = 0.18f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = accent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun StarFieldBackground(modifier: Modifier = Modifier) {
    data class Star(
        val xRatio: Float,
        val yRatio: Float,
        val radius: Float,
        val alpha: Float,
        val twinkleSpeed: Float,
        val phaseOffset: Float,
        val driftDistance: Float
    )
    val stars = remember {
        List(96) {
            Star(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                radius = Random.nextFloat() * 1.4f + 0.4f,
                alpha = Random.nextFloat() * 0.18f + 0.02f,
                twinkleSpeed = Random.nextFloat() * 1.5f + 0.5f,
                phaseOffset = Random.nextFloat() * (2f * PI.toFloat()),
                driftDistance = Random.nextFloat() * 16f + 4f
            )
        }
    }
    val ambientTransition = rememberInfiniteTransition(label = "sleepAmbientParticles")
    val ambientPhase by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starDrift"
    )
    Canvas(modifier = modifier) {
        stars.forEach { star ->
            val twinkle = 0.45f + 0.55f * ((sin(ambientPhase * star.twinkleSpeed + star.phaseOffset) + 1f) / 2f)
            val verticalDrift = sin(ambientPhase + star.phaseOffset) * star.driftDistance
            drawCircle(
                color = Color.White.copy(alpha = star.alpha * twinkle),
                radius = star.radius.dp.toPx(),
                center = Offset(
                    star.xRatio * size.width,
                    star.yRatio * size.height + verticalDrift.dp.toPx()
                )
            )
        }
    }
}

@Composable
private fun SleepLockBackdrop() {
    val moonTransition = rememberInfiniteTransition(label = "sleepMoonAmbient")
    val moonDrift by moonTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5_800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonDrift"
    )
    val moonScale by moonTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7_600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonBreath"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, DeepBackground, DeepBackground)
                )
            )
    )

    StarFieldBackground(modifier = Modifier.fillMaxSize())
    GlowingMoonOverlay(
        backgroundColor = DeepBackground,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = moonDrift.dp.toPx()
                scaleX = moonScale
                scaleY = moonScale
            }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SurfaceElevated.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset.Unspecified,
                    radius = 800f
                )
            )
    )
}

@Composable
private fun SleepProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier        = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val radius = (size.minDimension / 2f) - (stroke * 1.15f)
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color  = Color.White.copy(alpha = 0.08f),
                radius = radius,
                center = center,
                style  = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            if (progress > 0.005f) {
                drawArc(
                    color      = ICE_BLUE_MID,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter  = false,
                    topLeft    = Offset(center.x - radius, center.y - radius),
                    size       = Size(radius * 2f, radius * 2f),
                    style      = Stroke(width = stroke, cap = StrokeCap.Round)
                )

                val startRad = (-90f * PI / 180f)
                val startX   = center.x + radius * cos(startRad).toFloat()
                val startY   = center.y + radius * sin(startRad).toFloat()
                drawCircle(ICE_BLUE.copy(alpha = 0.4f), stroke * 0.6f, Offset(startX, startY))

                val tipRad = ((-90f + 360f * progress) * PI / 180f)
                val tipX   = center.x + radius * cos(tipRad).toFloat()
                val tipY   = center.y + radius * sin(tipRad).toFloat()

                drawCircle(ICE_BLUE.copy(alpha = 0.20f), stroke * 2.2f, Offset(tipX, tipY))
                drawCircle(ICE_BLUE.copy(alpha = 0.40f), stroke * 1.2f, Offset(tipX, tipY))
                drawCircle(TextPrimary, stroke * 0.42f, Offset(tipX, tipY))
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content             = content
        )
    }
}

@Composable
private fun SleepStatusPill() {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by pulse.animateFloat(
        initialValue   = 0.35f,
        targetValue    = 1.0f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(ICE_BLUE.copy(alpha = dotAlpha), CircleShape)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text          = "SLEEP LOCK",
            color         = ICE_BLUE.copy(alpha = 0.65f),
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 2.5.sp
        )
    }
}

@Composable
private fun WakeChip(wakeTime: String) {
    Surface(
        color  = AMBER.copy(alpha = 0.10f),
        shape  = RoundedCornerShape(999.dp),
        border = BorderStroke(0.5.dp, AMBER.copy(alpha = 0.28f))
    ) {
        Row(
            modifier           = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment  = Alignment.CenterVertically
        ) {
            Icon(
                imageVector     = Icons.Rounded.WbTwilight,
                contentDescription = null,
                tint            = AMBER,
                modifier        = Modifier.size(11.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text          = "WAKE $wakeTime",
                color         = AMBER,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
private fun ProgressPercentChip(progress: Float) {
    val pct = ((1f - progress) * 100).toInt().coerceIn(0, 100)
    Surface(
        color  = ICE_BLUE.copy(alpha = 0.08f),
        shape  = RoundedCornerShape(999.dp),
        border = BorderStroke(0.5.dp, ICE_BLUE.copy(alpha = 0.18f))
    ) {
        Text(
            text          = "$pct% of sleep complete",
            modifier      = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color         = ICE_BLUE.copy(alpha = 0.7f),
            fontSize      = 10.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ControlPill(
    icon:        ImageVector,
    accentColor: Color,
    title:       String,
    subtitle:    String,
    actionLabel: String,
    onClick:     () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = BG_CARD,
        shape    = RoundedCornerShape(18.dp),
        border   = BorderStroke(0.5.dp, BORDER_SUBTLE)
    ) {
        Row(
            modifier           = Modifier
                .fillMaxWidth()
                .gamifiedClick { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment  = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(42.dp)
                    .background(accentColor.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector     = icon,
                    contentDescription = null,
                    tint            = accentColor,
                    modifier        = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    color      = WHITE_HIGH,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = subtitle,
                    color    = WHITE_LOW,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Surface(
                color  = accentColor.copy(alpha = 0.10f),
                shape  = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.25f))
            ) {
                Text(
                    text          = actionLabel,
                    modifier      = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                    color         = accentColor,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Composable
private fun SleepLockEmergencyPanel(
    emergencyPackages: Set<String>,
    onLaunchDialer:    () -> Unit,
    onLaunchSms:       () -> Unit,
    onLaunchPackage:   (String) -> Unit
) {
    val context         = androidx.compose.ui.platform.LocalContext.current
    val dialerPackage   = remember { getDefaultDialerPackage(context) }
    val smsPackage      = remember { getDefaultSmsPackage(context) }
    val visiblePackages = remember(emergencyPackages) {
        emergencyPackages.filter { it.isNotBlank() }.sorted()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = BG_CARD_ALT,
        shape    = RoundedCornerShape(20.dp),
        border   = BorderStroke(0.5.dp, AMBER.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text          = "EMERGENCY",
                    color         = AMBER,
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(AMBER.copy(alpha = 0.4f), CircleShape)
                )
            Spacer(Modifier.height(28.dp))
                Text(
                    text     = "Real overnight needs only.",
                    color    = WHITE_LOW,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyActionButton(
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Rounded.PhoneEnabled,
                    label    = "Call",
                    onClick  = onLaunchDialer
                )
                EmergencyActionButton(
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Rounded.Sms,
                    label    = "Message",
                    onClick  = onLaunchSms
                )
            }

            if (visiblePackages.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(BORDER_SUBTLE)
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier            = Modifier.height(
                        (visiblePackages.size.coerceAtMost(4) * 56).dp
                    )
                ) {
                    items(visiblePackages, key = { it }) { pkg ->
                        SleepEmergencyAppRow(
                            packageName = pkg,
                            isCore      = pkg == dialerPackage ||
                                          pkg == smsPackage    ||
                                          pkg == context.packageName,
                            onLaunch    = { onLaunchPackage(pkg) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyActionButton(
    modifier: Modifier = Modifier,
    icon:     ImageVector,
    label:    String,
    onClick:  () -> Unit
) {
    Surface(
        modifier = modifier,
        color    = AMBER.copy(alpha = 0.08f),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(0.5.dp, AMBER.copy(alpha = 0.22f))
    ) {
        Row(
            modifier           = Modifier
                .fillMaxWidth()
                .gamifiedClick { onClick() }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment  = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = AMBER,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = label,
                color      = Color.White,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SleepEmergencyAppRow(
    packageName: String,
    isCore:      Boolean,
    onLaunch:    () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val label   = remember(packageName) {
        runCatching {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        }.getOrElse {
            packageName.substringAfterLast('.').replaceFirstChar { it.titlecase() }
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = WHITE_HINT,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(0.5.dp, BORDER_SUBTLE)
    ) {
        Row(
            modifier           = Modifier
                .fillMaxWidth()
                .gamifiedClick { onLaunch() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment  = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))) {
                AppIconImage(packageName = packageName, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    color      = WHITE_HIGH,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = if (isCore) "core access" else "user-allowed",
                    color    = WHITE_LOW,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector        = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint               = WHITE_LOW,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatRemainingTime(remainingMillis: Long): String {
    val s = (remainingMillis / 1_000L).coerceAtLeast(0L)
    val h = s / 3_600L
    val m = (s % 3_600L) / 60L
    val sec = s % 60L
    return if (h > 0L) String.format("%02d:%02d:%02d", h, m, sec)
    else              String.format("%02d:%02d", m, sec)
}

@Preview(showBackground = true)
@Composable
fun SleepLockActiveScreenPreview() {
    IronMindTheme {
        SleepLockActiveScreen()
    }
}
