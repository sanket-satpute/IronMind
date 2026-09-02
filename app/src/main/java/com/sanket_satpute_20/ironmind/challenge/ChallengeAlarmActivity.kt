package com.sanket_satpute_20.ironmind.challenge

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchManager
import com.sanket_satpute_20.ironmind.morninglaunch.MorningLaunchStage
import com.sanket_satpute_20.ironmind.morninglaunch.MorningUnlockReviewActivity
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP

class ChallengeAlarmActivity : ComponentActivity() {

    companion object {
        fun createIntent(context: android.content.Context): android.content.Intent =
            android.content.Intent(context, ChallengeAlarmActivity::class.java).apply {
                addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            }
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Block back button
        onBackPressedDispatcher.addCallback(this) { /* cannot dismiss */ }

        val prefs = PrefManager.getInstance(this)
        val status = ChallengeManager.reconcileChallengeState(this)
        val today = LocalDate.now().toString()
        if (!status.isActive || ChallengeManager.hasCompletedToday(this)) {
            prefs.challengeAlarmActive = false
            prefs.challengeAlarmDate = ""
            ChallengeAlarmReceiver.dismissAlarmNotification(this)
            finish()
            return
        }
        prefs.challengeAlarmActive = true
        prefs.challengeAlarmDate = today

        // Start alarm sound
        startAlarmSound()

        setContent {
            IronMindTheme {
                var mathSolved by remember { mutableStateOf(false) }

                if (!mathSolved) {
                    ChallengeAlarmScreen(
                        dayNumber = status.daysCompleted + 1,
                        onSolved = {
                            stopAlarmSound()
                            mathSolved = true
                        }
                    )
                } else {
                    val launchManager = remember { MorningLaunchManager(this@ChallengeAlarmActivity) }
                    var handedOffToMorningReview by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        val stage = launchManager.reconcileRuntimeState()
                        val shouldOpenMorningLaunch = prefs.challengeActive &&
                            !ChallengeManager.hasCompletedToday(this@ChallengeAlarmActivity)

                        if (!shouldOpenMorningLaunch) {
                            completeChallengeDay(prefs)
                            return@LaunchedEffect
                        }

                        launchManager.syncModeFromRoutineType()
                        if (prefs.morningLaunchSessionId <= 0L || stage == MorningLaunchStage.IDLE) {
                            launchManager.beginPrompting(source = "CHALLENGE_ALARM")
                        }

                        handedOffToMorningReview = true
                        prefs.challengeAlarmActive = false
                        prefs.challengeAlarmDate = ""
                        ChallengeAlarmReceiver.dismissAlarmNotification(this@ChallengeAlarmActivity)
                        startActivity(MorningUnlockReviewActivity.createIntent(this@ChallengeAlarmActivity))
                        finish()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }
            }
        }
    }

    private fun completeChallengeDay(prefs: PrefManager) {
        val today = LocalDate.now().toString()
        prefs.routineCompletedToday = true
        prefs.routineDate = today
        prefs.challengeAlarmActive = false
        prefs.challengeAlarmDate = ""
        ChallengeAlarmReceiver.dismissAlarmNotification(this)

        val result = ChallengeManager.recordDayCompleted(this)
        if (!result.recorded && result.alreadyCompletedToday) {
            finish()
            return
        }

        finish()
    }

    private fun startAlarmSound() {
        val prefs = PrefManager.getInstance(this)
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (
            prefs.muteAudio ||
            audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL ||
            audioManager.getStreamVolume(AudioManager.STREAM_ALARM) == 0
        ) return
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                val alarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                setDataSource(applicationContext, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Fallback
        }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val prefs = PrefManager.getInstance(this)
        if (!isFinishing && prefs.challengeAlarmActive) {
            startActivity(createIntent(this))
        }
    }

    override fun onStop() {
        super.onStop()
        val prefs = PrefManager.getInstance(this)
        if (!isFinishing && !isChangingConfigurations && prefs.challengeAlarmActive) {
            startActivity(createIntent(this))
        }
    }
}

@Composable
fun ChallengeAlarmScreen(dayNumber: Int, onSolved: () -> Unit) {
    var problem by remember { mutableStateOf(ChallengeManager.generateMathProblem()) }
    var userInput by remember { mutableStateOf("") }
    var shakeError by remember { mutableStateOf(false) }
    var attempts by remember { mutableStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Shake animation on wrong answer
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeError) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { shakeError = false },
        label = "shake"
    )

    // Pulse animation for the time
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulse by pulseAnim.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulse"
    )

    fun checkAnswer() {
        val answer = userInput.trim().toIntOrNull()
        if (answer == problem.answer) {
            keyboardController?.hide()
            onSolved()
        } else {
            attempts++
            shakeError = true
            userInput = ""
            if (attempts >= 2) {
                problem = ChallengeManager.generateMathProblem()
                attempts = 0
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {

            Text(
                "5:00 AM",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = GoldXP,
                modifier = Modifier.scale(pulse)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "⚡ 5 AM CLUB — DAY $dayNumber / 30",
                fontSize = 14.sp,
                color = GoldXP.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "PROVE YOU\'RE AWAKE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Solve this math problem to dismiss the alarm.\nNo calculators. No going back to sleep.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Math problem display
            AnimatedContent(
                targetState = problem.question,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn())
                        .togetherWith(slideOutVertically { it } + fadeOut())
                },
                label = "problem"
            ) { question ->
                Surface(
                    color = DeepBackground,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp, GoldXP.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        question,
                        modifier = Modifier.padding(32.dp),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Answer input field
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it.filter { c -> c.isDigit() || c == '-' } },
                label = { Text("Your answer", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { checkAnswer() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldXP,
                    unfocusedBorderColor = SurfaceElevated,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = GoldXP
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = (shakeOffset * 20f * if ((attempts % 2) == 0) 1 else -1).dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { checkAnswer() },
                colors = ButtonDefaults.buttonColors(containerColor = GoldXP),
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "DISMISS ALARM",
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "The 5 AM version of you is already\nahead of 99% of people.",
                fontSize = 13.sp,
                color = SurfaceElevated,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun ChallengeAlarmScreenPreview() {
    IronMindTheme { ChallengeAlarmScreen(dayNumber = 12, onSolved = {}) }
}
