package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun AnimatedEntry(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 500,
    initialOffsetY: Int = 100, // Slide up from 100px below
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis
            )
        ) + slideInVertically(
            initialOffsetY = { initialOffsetY },
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis
            )
        )
    ) {
        Box(content = content)
    }
}
