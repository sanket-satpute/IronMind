package com.sanket_satpute_20.ironmind.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.psychology.UserType
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

@Composable
internal fun EmptyState(onAddTaskClick: () -> Unit, userType: UserType, lastActiveDate: String? = null) {
    val daysSinceActive = lastActiveDate?.let { dateStr ->
        runCatching {
            ChronoUnit.DAYS.between(LocalDate.parse(dateStr), LocalDate.now()).toInt()
        }.getOrNull()
    }
    val isWelcomeBack = daysSinceActive != null && daysSinceActive >= 3
    val hour = LocalTime.now().hour
    val greeting = if (isWelcomeBack) {
        "Welcome back. Glad you're here."
    } else when {
        hour < 12 -> if (userType == UserType.ACHIEVER) "Good morning. What's your first win?" else "Start with one small win."
        hour < 17 -> "Afternoon momentum. Keep building."
        else -> "Evening wind-down. Wrap up strong."
    }
    val subtitle = if (isWelcomeBack) {
        "No pressure. Start with just one thing."
    } else {
        "What matters to you today?"
    }
    val emoji = if (isWelcomeBack) "👋" else "🦾"
    val buttonText = if (isWelcomeBack) "START FRESH" else if (hour < 12) "BUILD DAILY MISSIONS" else "ADD QUICK SPRINT"
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(modifier = Modifier.size(80.dp), color = SurfaceLighter, shape = CircleShape) {
            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 40.sp) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(greeting, style = MaterialTheme.typography.titleMedium, color = Color.White, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddTaskClick,
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.height(52.dp).padding(horizontal = 24.dp)
        ) {
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun AllDoneCelebration(allDone: Boolean) {
    var isVisible by rememberSaveable { mutableStateOf(false) }
    var hasDismissedCurrentPerfectDay by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(allDone) {
        if (!allDone) {
            isVisible = false
            hasDismissedCurrentPerfectDay = false
            return@LaunchedEffect
        }
        if (!hasDismissedCurrentPerfectDay) {
            isVisible = true
        }
    }

    AnimatedVisibility(
        visible = allDone && isVisible && !hasDismissedCurrentPerfectDay,
        enter = fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = fadeOut(animationSpec = tween(durationMillis = 280)) + shrinkVertically(animationSpec = tween(durationMillis = 280))
    ) {
        Surface(
            onClick = {
                isVisible = false
                hasDismissedCurrentPerfectDay = true
            },
            color = SuccessGreen.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🎉", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("PERFECT DAY", color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("Every mission complete. Well done.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
                Text("✕", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp)
            }
        }
    }
}

// =========================================================
// Previews
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun HomeFeedbackEmptyStatePreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.background(DeepBackground)
        ) {
            EmptyState(
                onAddTaskClick = {},
                userType = com.sanket_satpute_20.ironmind.psychology.UserType.ACHIEVER
            )
        }
    }
}