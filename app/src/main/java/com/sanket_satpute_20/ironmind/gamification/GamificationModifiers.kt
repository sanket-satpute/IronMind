package com.sanket_satpute_20.ironmind.gamification

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.staticCompositionLocalOf

val LocalHapticsManager = staticCompositionLocalOf<HapticsManager> { 
    error("No HapticsManager provided") 
}

val LocalSoundManager = staticCompositionLocalOf<SoundManager> { 
    error("No SoundManager provided") 
}

/**
 * A drop-in replacement for Modifier.clickable that automatically
 * triggers a premium haptic heavy click, sound effect, and scale animation.
 */
fun Modifier.gamifiedClick(
    interactionSource: MutableInteractionSource? = null,
    indication: androidx.compose.foundation.Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: androidx.compose.ui.semantics.Role? = null,
    onClick: () -> Unit
) = composed {
    val hapticsManager = LocalHapticsManager.current
    val soundManager = LocalSoundManager.current
    val systemHaptics = LocalHapticFeedback.current
    
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualInteractionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "Button Press Scale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = actualInteractionSource,
            indication = indication ?: androidx.compose.foundation.LocalIndication.current,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = {
                if (enabled) {
                    systemHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    hapticsManager.playSuccess()
                    soundManager.playTaskComplete()
                    onClick()
                }
            }
        )
}
