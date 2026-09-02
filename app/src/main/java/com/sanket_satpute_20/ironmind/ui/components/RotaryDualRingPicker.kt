package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground

enum class RotaryRingLayer {
    PRIMARY,
    SECONDARY
}

@Composable
fun RotaryDualRingPicker(
    modifier: Modifier = Modifier,
    primaryValue: Int,
    secondaryValue: Int,
    primaryMaxValue: Int,
    secondaryMaxValue: Int,
    primaryStepDegrees: Float,
    secondaryStepDegrees: Float,
    primaryStepValue: Int = 1,
    secondaryStepValue: Int = 1,
    primaryAccent: Color,
    secondaryAccent: Color,
    primaryMarkerCount: Int,
    secondaryMarkerCount: Int,
    primaryStrongMarkerEvery: Int = 1,
    primaryGearStyle: Boolean = true,
    primaryWrap: Boolean = false,
    secondaryWrap: Boolean = false,
    hapticsEnabled: Boolean = true,
    onPrimaryValueChange: (Int) -> Unit,
    onSecondaryValueChange: (Int) -> Unit,
    centerContent: @Composable BoxScope.(RotaryRingLayer, Int, Int) -> Unit
) {
    var activeRing by remember { mutableStateOf(RotaryRingLayer.PRIMARY) }
    var dragActive by remember { mutableStateOf(false) }
    var displayPrimaryValue by remember { mutableIntStateOf(primaryValue) }
    var displaySecondaryValue by remember { mutableIntStateOf(secondaryValue) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(primaryValue) {
        displayPrimaryValue = primaryValue
    }
    LaunchedEffect(secondaryValue) {
        displaySecondaryValue = secondaryValue
    }
    val normalizedPrimaryProgress = if (primaryMaxValue <= 0) 0f else displayPrimaryValue.toFloat() / primaryMaxValue.toFloat()
    val normalizedSecondaryProgress = if (secondaryMaxValue <= 0) 0f else displaySecondaryValue.toFloat() / secondaryMaxValue.toFloat()
    val animatedPrimaryProgress by animateFloatAsState(
        targetValue = normalizedPrimaryProgress.coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "rotary_primary_progress"
    )
    val animatedSecondaryProgress by animateFloatAsState(
        targetValue = normalizedSecondaryProgress.coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "rotary_secondary_progress"
    )
    val primaryGlowAlpha by animateFloatAsState(
        targetValue = if (activeRing == RotaryRingLayer.PRIMARY) 0.22f else 0.12f,
        animationSpec = tween(160),
        label = "rotary_primary_glow"
    )
    val secondaryGlowAlpha by animateFloatAsState(
        targetValue = if (activeRing == RotaryRingLayer.SECONDARY) 0.24f else 0.14f,
        animationSpec = tween(160),
        label = "rotary_secondary_glow"
    )
    val scrollBlocker = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (source == NestedScrollSource.UserInput) available else Offset.Zero
            }
        }
    }
    val renderPrimaryProgress = if (dragActive) normalizedPrimaryProgress else animatedPrimaryProgress
    val renderSecondaryProgress = if (dragActive) normalizedSecondaryProgress else animatedSecondaryProgress

    Box(
        modifier = modifier
            .nestedScroll(scrollBlocker)
            .pointerInput(primaryValue, secondaryValue) {
            var previousAngle = 0f
            var dragRemainder = 0f
            detectDragGestures(
                onDragStart = { offset ->
                    dragActive = true
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (offset - center).getDistance()
                    activeRing = if (radius > minOf(size.width, size.height) * 0.31f) {
                        RotaryRingLayer.PRIMARY
                    } else {
                        RotaryRingLayer.SECONDARY
                    }
                    previousAngle = rotaryAngleFor(offset, center)
                    dragRemainder = 0f
                },
                onDragEnd = {
                    dragActive = false
                },
                onDragCancel = {
                    dragActive = false
                },
                onDrag = { change, _ ->
                    change.consume()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val currentAngle = rotaryAngleFor(change.position, center)
                    var delta = currentAngle - previousAngle
                    if (delta > 180f) delta -= 360f
                    if (delta < -180f) delta += 360f
                    previousAngle = currentAngle
                    dragRemainder += delta
                    val stepDegrees = if (activeRing == RotaryRingLayer.PRIMARY) primaryStepDegrees else secondaryStepDegrees

                    while (dragRemainder >= stepDegrees) {
                        val changed = if (activeRing == RotaryRingLayer.PRIMARY) {
                            val updated = adjustRingValue(
                                current = displayPrimaryValue,
                                step = primaryStepValue,
                                maxValue = primaryMaxValue,
                                wrap = primaryWrap,
                                direction = +1
                            )
                            if (updated != displayPrimaryValue) {
                                displayPrimaryValue = updated
                                onPrimaryValueChange(updated)
                                true
                            } else {
                                false
                            }
                        } else {
                            val updated = adjustRingValue(
                                current = displaySecondaryValue,
                                step = secondaryStepValue,
                                maxValue = secondaryMaxValue,
                                wrap = secondaryWrap,
                                direction = +1
                            )
                            if (updated != displaySecondaryValue) {
                                displaySecondaryValue = updated
                                onSecondaryValueChange(updated)
                                true
                            } else {
                                false
                            }
                        }
                        if (changed && hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        dragRemainder -= stepDegrees
                    }
                    while (dragRemainder <= -stepDegrees) {
                        val changed = if (activeRing == RotaryRingLayer.PRIMARY) {
                            val updated = adjustRingValue(
                                current = displayPrimaryValue,
                                step = primaryStepValue,
                                maxValue = primaryMaxValue,
                                wrap = primaryWrap,
                                direction = -1
                            )
                            if (updated != displayPrimaryValue) {
                                displayPrimaryValue = updated
                                onPrimaryValueChange(updated)
                                true
                            } else {
                                false
                            }
                        } else {
                            val updated = adjustRingValue(
                                current = displaySecondaryValue,
                                step = secondaryStepValue,
                                maxValue = secondaryMaxValue,
                                wrap = secondaryWrap,
                                direction = -1
                            )
                            if (updated != displaySecondaryValue) {
                                displaySecondaryValue = updated
                                onSecondaryValueChange(updated)
                                true
                            } else {
                                false
                            }
                        }
                        if (changed && hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        dragRemainder += stepDegrees
                    }
                }
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension * 0.45f
            val innerRadius = size.minDimension * 0.28f
            val outerStroke = size.minDimension * 0.11f
            val innerStroke = size.minDimension * 0.075f

            drawCircle(
                color = DeepBackground.copy(alpha = 0.92f),
                radius = outerRadius + outerStroke * 0.4f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(primaryAccent.copy(alpha = primaryGlowAlpha), Color.Transparent)
                ),
                radius = outerRadius + outerStroke
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(secondaryAccent.copy(alpha = secondaryGlowAlpha), Color.Transparent)
                ),
                radius = innerRadius + innerStroke * 1.2f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = outerRadius,
                style = Stroke(width = outerStroke)
            )
            drawCircle(
                color = if (activeRing == RotaryRingLayer.PRIMARY) {
                    primaryAccent.copy(alpha = 0.22f)
                } else {
                    Color.White.copy(alpha = 0.06f)
                },
                radius = outerRadius,
                style = Stroke(width = outerStroke * 0.18f)
            )

            repeat(primaryMarkerCount) { index ->
                val angle = Math.toRadians((index * (360f / primaryMarkerCount) - 90f).toDouble())
                val isStrongMarker = primaryStrongMarkerEvery <= 1 || index % primaryStrongMarkerEvery == 0
                val markerProgress = if (primaryMarkerCount <= 1) 1f else index.toFloat() / (primaryMarkerCount - 1).toFloat()
                val toothLength = if (primaryGearStyle) {
                    if (isStrongMarker) outerStroke * 0.72f else outerStroke * 0.42f
                } else {
                    if (isStrongMarker) outerStroke * 0.84f else outerStroke * 0.64f
                }
                val toothWidth = if (primaryGearStyle) {
                    if (isStrongMarker) outerStroke * 0.18f else outerStroke * 0.11f
                } else {
                    outerStroke * 0.14f
                }
                val radius = outerRadius + outerStroke * 0.12f
                val x = center.x + cos(angle).toFloat() * radius
                val y = center.y + sin(angle).toFloat() * radius
                rotate(index * (360f / primaryMarkerCount), pivot = Offset(x, y)) {
                    drawRoundRect(
                        color = if (markerProgress <= renderPrimaryProgress) {
                            primaryAccent.copy(alpha = if (isStrongMarker) 0.88f else 0.58f)
                        } else {
                            Color.White.copy(alpha = if (isStrongMarker) 0.16f else 0.1f)
                        },
                        topLeft = Offset(x - toothWidth / 2f, y - toothLength / 2f),
                        size = Size(toothWidth, toothLength),
                        cornerRadius = CornerRadius(toothWidth, toothWidth)
                    )
                }
            }

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        primaryAccent.copy(alpha = 0.72f),
                        secondaryAccent.copy(alpha = 0.9f),
                        primaryAccent.copy(alpha = 0.72f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = (360f * renderPrimaryProgress).coerceAtLeast(8f),
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2f, outerRadius * 2f),
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = innerRadius,
                style = Stroke(width = innerStroke)
            )
            drawCircle(
                color = if (activeRing == RotaryRingLayer.SECONDARY) {
                    secondaryAccent.copy(alpha = 0.24f)
                } else {
                    Color.White.copy(alpha = 0.06f)
                },
                radius = innerRadius,
                style = Stroke(width = innerStroke * 0.2f)
            )

            repeat(secondaryMarkerCount) { index ->
                val angle = Math.toRadians((index * (360f / secondaryMarkerCount) - 90f).toDouble())
                val markerCenter = Offset(
                    center.x + cos(angle).toFloat() * innerRadius,
                    center.y + sin(angle).toFloat() * innerRadius
                )
                val markerProgress = if (secondaryMarkerCount <= 1) 1f else index.toFloat() / (secondaryMarkerCount - 1).toFloat()
                drawCircle(
                    color = secondaryAccent.copy(alpha = if (markerProgress <= renderSecondaryProgress) 0.92f else 0.2f),
                    radius = innerStroke * 0.18f,
                    center = markerCenter
                )
            }

            drawArc(
                color = secondaryAccent,
                startAngle = -90f,
                sweepAngle = (360f * renderSecondaryProgress).coerceAtLeast(6f),
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2f, innerRadius * 2f),
                style = Stroke(width = innerStroke, cap = StrokeCap.Round)
            )
        }

        centerContent(activeRing, displayPrimaryValue, displaySecondaryValue)
    }
}

private fun rotaryAngleFor(position: Offset, center: Offset): Float {
    return ((atan2(position.y - center.y, position.x - center.x) * 180f / PI.toFloat()) + 90f + 360f) % 360f
}

private fun adjustRingValue(
    current: Int,
    step: Int,
    maxValue: Int,
    wrap: Boolean,
    direction: Int
): Int {
    val raw = current + (step * direction)
    return if (wrap) {
        when {
            raw < 0 -> maxValue
            raw > maxValue -> 0
            else -> raw
        }
    } else {
        raw.coerceIn(0, maxValue)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun RotaryDualRingPickerPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        RotaryDualRingPicker(primaryValue = 5, secondaryValue = 30, primaryMaxValue = 12, secondaryMaxValue = 60, primaryStepDegrees = 30f, secondaryStepDegrees = 6f, primaryMarkerCount = 12, secondaryMarkerCount = 60, primaryAccent = androidx.compose.ui.graphics.Color.Blue, secondaryAccent = androidx.compose.ui.graphics.Color.Red, onPrimaryValueChange = {}, onSecondaryValueChange = {}, centerContent = { _, _, _ -> })
    }
}