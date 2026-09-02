package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.integrity.ShieldUiState
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary
import com.sanket_satpute_20.ironmind.ui.theme.TextSecondary

/** Compact live host for the Home screen. Room updates drive it without polling. */
@Composable
fun LiveIntegrityShieldCard(
    state: ShieldUiState,
    onEventHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val percent = (state.integrityLevel * 100).toInt()
    val status = when {
        percent >= 85 -> "INTACT"
        percent >= 60 -> "HOLDING"
        else -> "REFORGE IN PROGRESS"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DeepBackground,
        border = BorderStroke(1.dp, state.mode.secondary.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(state.mode.primary.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(92.dp)) {
                IntegrityShield(
                    integrityLevel = state.integrityLevel,
                    mode = state.mode,
                    event = state.currentEvent,
                    eventId = state.currentEventId,
                    onEventHandled = onEventHandled,
                    onHaptic = { com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(context).playError() },
                    onSound = { com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(context).playTemptationBlocked() },
                    modifier = Modifier.size(78.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "INTEGRITY SHIELD",
                    color = state.mode.secondary,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "$percent% · $status",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${state.completedSessionCount} focus wins  ·  ${state.temptationCount} catches",
                    color = TextSecondary.copy(alpha = 0.82f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.mode.name + " MODE",
                    color = state.mode.secondary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}



@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun LiveIntegrityShieldCardPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        LiveIntegrityShieldCard(state = com.sanket_satpute_20.ironmind.integrity.ShieldUiState(), onEventHandled = {})
    }
}
