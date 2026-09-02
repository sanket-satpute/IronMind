package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun FloatingXP(
    text: String = "+15 XP",
    color: Color = WarningAmber, // Gold
    onAnimationEnd: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    val transition = updateTransition(targetState = isVisible, label = "floating_xp")

    val offsetY by transition.animateDp(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "offsetY"
    ) { state ->
        if (state) 0.dp else (-60).dp
    }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "alpha"
    ) { state ->
        if (state) 1f else 0f
    }

    val scale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 400, easing = FastOutSlowInEasing) },
        label = "scale"
    ) { state ->
        if (state) 1.5f else 1f
    }

    LaunchedEffect(Unit) {
        isVisible = false // Trigger animation immediately
        delay(800)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color.copy(alpha = alpha),
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            modifier = Modifier
                .offset(y = offsetY)
        )
    }
}



@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ParticleSystemPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        FloatingXP(onAnimationEnd = {})
    }
}
