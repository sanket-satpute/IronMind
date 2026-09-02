package com.sanket_satpute_20.ironmind.settings

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.*
import kotlinx.coroutines.launch
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@Composable
fun ManualTypeSelectScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}) {
    val context     = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    val currentType = AdaptiveEngine.getCurrentType(prefs)
    var selected    by remember { mutableStateOf(currentType) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        Text("Who are you right now?", fontSize = 26.sp,
            fontWeight = FontWeight.Black, color = Color.White)
        Text(
            "Choose the type that matches your real current state, not your ideal one.",
            fontSize = 14.sp, color = Color.Gray, lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Selection guide",
                    fontSize = 11.sp,
                    color = WarningAmber,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This changes the tone of the app, the recommended mode, and how identity language is framed across the system.",
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        UserType.values().forEach { type ->
            val isSelected = selected == type
            val isCurrent  = type == currentType
            val scale by animateFloatAsState(
                targetValue  = if (isSelected) 1.02f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            Surface(
                color  = if (isSelected) DeepBackground else DeepBackground,
                shape  = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) WarningAmber else SurfaceDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .padding(vertical = 5.dp)
                    .gamifiedClick { selected = type }
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(type.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(type.label, fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White
                                        else TextPrimary)
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = WarningAmber.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("CURRENT", fontSize = 9.sp,
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 5.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            getTypeShortDescription(type),
                            fontSize  = 13.sp,
                            color     = if (isSelected) Color.White.copy(alpha = 0.75f)
                                        else SurfaceElevated,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // What changes when type changes
        if (selected != currentType) {
            Surface(
                color  = DeepBackground,
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("What changes", fontSize = 11.sp, color = WarningAmber,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• How the app talks to you\n" +
                        "• The recommended mode for your type\n" +
                        "• The Identity statements shown during tasks\n" +
                        "• The archive and stats framing",
                        fontSize  = 13.sp,
                        color     = Color.White.copy(alpha = 0.7f),
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Note: Your mode stays the same unless you change it separately.",
                        fontSize = 12.sp, color = SurfaceElevated
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                val previousType = prefs.userType
                prefs.userType = selected.name
                HistoryRecorder.recordConfigChange(context, "USER_TYPE", previousType, prefs.userType, "MANUAL_TYPE_SELECT")
                onSaved()
            },
            enabled = selected != currentType,
            colors  = ButtonDefaults.buttonColors(
                containerColor = if (selected != currentType) WarningAmber
                                 else SurfaceDark,
                disabledContainerColor = SurfaceDark
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Text(
                if (selected != currentType) "Save As ${selected.label}"
                else "Select a different type to save",
                fontWeight = FontWeight.Black,
                color = if (selected != currentType) Color.Black else SurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


    }
}

fun getTypeShortDescription(type: UserType): String = when (type) {
    UserType.ACHIEVER ->
        "Thrives on challenge. Pressure sharpens you.\n" +
        "Best mode: Iron."
    UserType.BROKEN_STRIVER ->
        "Knows who you want to be. The gap is frustrating.\n" +
        "Best mode: Build."
    UserType.PERFECTIONIST ->
        "One bad day can collapse everything. High standards.\n" +
        "Best mode: Experiment."
    UserType.BURNED_OUT ->
        "Running on empty. Not laziness — protection.\n" +
        "Best mode: Recovery."
    UserType.IDENTITY_SEEKER ->
        "Motivation runs deep when tied to who you are.\n" +
        "Best mode: Build."
    UserType.SYSTEMS_PERSON ->
        "Execution through structure, analytics, and mechanics.\n" +
        "Best mode: Experiment."
    UserType.DOOMSCROLLER ->
        "Attention hijacked by algorithms. Needs a fortress.\n" +
        "Best mode: Iron."
    UserType.PROCRASTINATOR ->
        "Delays until pressure is overwhelming. Needs strict windows.\n" +
        "Best mode: Build."
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ManualTypeSelectScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ManualTypeSelectScreen(onSaved = {}, onBack = {})
    }
}