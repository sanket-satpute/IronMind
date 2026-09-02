package com.sanket_satpute_20.ironmind.ui.components

import android.content.Context
import android.os.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

private enum class RingType { HOUR, MINUTE }
private enum class TimelineZoneType { OCCUPIED, CAUTION, PROTECTED }

private data class TimelineStatePresentation(
    val title: String,
    val detail: String,
    val color: Color,
    val background: Color
)

private fun normalizeBounds(
    minTime: LocalTime?,
    maxTime: LocalTime?
): Pair<LocalTime?, LocalTime?> {
    return if (minTime != null && maxTime != null && minTime > maxTime) {
        maxTime to minTime
    } else {
        minTime to maxTime
    }
}

private fun clampTime(
    time: LocalTime,
    minTime: LocalTime?,
    maxTime: LocalTime?
): LocalTime {
    var result = time
    if (minTime != null && result < minTime) result = minTime
    if (maxTime != null && result > maxTime) result = maxTime
    return result
}

private fun hourToAngle(hour: Int): Float = ((hour % 24 + 24) % 24) * 15f

private fun minuteToAngle(minute: Int): Float = ((minute % 60 + 60) % 60) * 6f

private fun angleToHour(angle: Float): Int = ((angle / 15f).toInt() % 24 + 24) % 24

private fun angleToMinute(angle: Float): Int = ((angle / 6f).toInt() % 60 + 60) % 60

private fun calculateAngle(position: Offset, center: Offset): Float {
    return (
            atan2(
                y = (position.y - center.y).toDouble(),
                x = (position.x - center.x).toDouble()
            ) * (180.0 / PI) + 90.0 + 360.0
            ).toFloat() % 360f
}

private fun timeToDayAngle(time: LocalTime): Float {
    return (time.hour * 60f + time.minute) * 0.25f
}

private fun angleToOffset(
    center: Offset,
    radius: Float,
    angle: Float
): Offset {
    val radians = Math.toRadians((angle - 90f).toDouble())
    return Offset(
        x = center.x + radius * cos(radians).toFloat(),
        y = center.y + radius * sin(radians).toFloat()
    )
}

private fun isTimeWithinRange(time: LocalTime, start: LocalTime, end: LocalTime): Boolean {
    return !time.isBefore(start) && time.isBefore(end)
}

private fun minutesFrom(time: LocalTime, target: LocalTime): Long {
    return kotlin.math.abs(java.time.Duration.between(target, time).toMinutes())
}

private fun zoneForTime(
    time: LocalTime,
    occupiedRanges: List<Pair<LocalTime, LocalTime>>,
    cautionRanges: List<Pair<LocalTime, LocalTime>>,
    protectedRanges: List<Pair<LocalTime, LocalTime>>
): TimelineZoneType? {
    if (protectedRanges.any { (start, end) -> isTimeWithinRange(time, start, end) }) return TimelineZoneType.PROTECTED
    if (cautionRanges.any { (start, end) -> isTimeWithinRange(time, start, end) }) return TimelineZoneType.CAUTION
    if (occupiedRanges.any { (start, end) -> isTimeWithinRange(time, start, end) }) return TimelineZoneType.OCCUPIED
    return null
}

private fun timelinePresentationForZone(zone: TimelineZoneType?): TimelineStatePresentation {
    return when (zone) {
        TimelineZoneType.PROTECTED -> TimelineStatePresentation(
            title = "SLEEP LOCK",
            detail = "This window is sealed by Sleep Lock.",
            color = TextPrimary,
            background = SurfaceElevated.copy(alpha = 0.2f)
        )
        TimelineZoneType.CAUTION -> TimelineStatePresentation(
            title = "CAUTION BAND",
            detail = "This time sits too close to Sleep Lock.",
            color = TextPrimary,
            background = SurfaceElevated.copy(alpha = 0.2f)
        )
        else -> TimelineStatePresentation(
            title = "FREE TIME",
            detail = "This window is open and schedulable.",
            color = TextPrimary,
            background = SurfaceDark.copy(alpha = 0.2f)
        )
    }
}

private fun vibrateTick(vibrator: Vibrator?) {
    vibrator ?: return
    if (!vibrator.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(4L, 15))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(4L)
    }
}

@Composable
fun OrbitalTimePicker(
    initialTime: LocalTime,
    minTime: LocalTime? = null,
    maxTime: LocalTime? = null,
    occupiedRanges: List<Pair<LocalTime, LocalTime>> = emptyList(),
    protectedRanges: List<Pair<LocalTime, LocalTime>> = emptyList(),
    cautionRanges: List<Pair<LocalTime, LocalTime>> = emptyList(),
    hasConflict: Boolean = false,
    onTimeChange: (LocalTime) -> Unit
) {
    val latestOnTimeChange by rememberUpdatedState(onTimeChange)
    val (safeMinTime, safeMaxTime) = remember(minTime, maxTime) {
        normalizeBounds(minTime, maxTime)
    }

    val clampedInitialTime = remember(initialTime, safeMinTime, safeMaxTime) {
        clampTime(initialTime, safeMinTime, safeMaxTime)
    }

    var hour by remember { mutableIntStateOf(clampedInitialTime.hour) }
    var minute by remember { mutableIntStateOf(clampedInitialTime.minute) }

    val hAngle = remember { Animatable(hourToAngle(clampedInitialTime.hour)) }
    val mAngle = remember { Animatable(minuteToAngle(clampedInitialTime.minute)) }

    var activeRing by remember { mutableStateOf<RingType?>(null) }
    val sleepMotion = rememberInfiniteTransition(label = "sleep_motion")
    val sleepSweepPhase by sleepMotion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000),
            repeatMode = RepeatMode.Restart
        ),
        label = "sleep_sweep_phase"
    )

    LaunchedEffect(initialTime, safeMinTime, safeMaxTime, activeRing) {
        if (activeRing != null) return@LaunchedEffect

        val externalTime = clampTime(initialTime, safeMinTime, safeMaxTime)
        if (hour != externalTime.hour || minute != externalTime.minute) {
            hour = externalTime.hour
            minute = externalTime.minute
            hAngle.snapTo(hourToAngle(externalTime.hour))
            mAngle.snapTo(minuteToAngle(externalTime.minute))
        }
    }

    val centerScale by animateFloatAsState(
        targetValue = if (activeRing != null) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "centerScale"
    )

    val context = LocalContext.current
    val vibrator = remember(context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                    ?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    val density = LocalDensity.current
    val pickerCenterPx = with(density) { 170.dp.toPx() }
    val cautionLabelRadiusPx = with(density) { 172.dp.toPx() }
    val sleepLabelRadiusPx = with(density) { 182.dp.toPx() }
    val currentTime = remember(hour, minute) { LocalTime.of(hour, minute) }
    val activeZone = remember(currentTime, occupiedRanges, cautionRanges, protectedRanges) {
        zoneForTime(currentTime, occupiedRanges, cautionRanges, protectedRanges)
    }
    val timelinePresentation = remember(activeZone) {
        timelinePresentationForZone(activeZone)
    }
    val cautionMarkerTime = remember(cautionRanges) { cautionRanges.minByOrNull { it.first }?.first }
    val sleepMarkerTime = remember(protectedRanges) {
        protectedRanges
            .filter { it.first.hour >= 20 }
            .minByOrNull { it.first }
            ?.first
            ?: protectedRanges.minByOrNull { it.first }?.first
    }
    val showCautionLabel = remember(currentTime, cautionMarkerTime) {
        cautionMarkerTime != null && minutesFrom(currentTime, cautionMarkerTime) <= 75
    }
    val showSleepLabel = remember(currentTime, sleepMarkerTime) {
        sleepMarkerTime != null && minutesFrom(currentTime, sleepMarkerTime) <= 75
    }
    val cautionLabelOffset = remember(cautionMarkerTime) {
        cautionMarkerTime?.let { timeToDayAngle(it) }
    }
    val sleepLabelOffset = remember(sleepMarkerTime) {
        sleepMarkerTime?.let { timeToDayAngle(it) }
    }

    Box(
        modifier = Modifier.size(340.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(safeMinTime, safeMaxTime, density) {
                    val hourInner = with(density) { 105.dp.toPx() }
                    val hourOuter = with(density) { 185.dp.toPx() }
                    val minuteInner = with(density) { 45.dp.toPx() }
                    val minuteOuter = with(density) { 104.dp.toPx() }

                    coroutineScope {
                        var springJob: Job? = null

                        while (true) {
                            val (pointerId, startRing) = awaitPointerEventScope {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val distance = (down.position - center).getDistance()

                                val ring = when {
                                    distance in hourInner..hourOuter -> RingType.HOUR
                                    distance in minuteInner..minuteOuter -> RingType.MINUTE
                                    else -> null
                                }

                                down.id to ring
                            }

                            if (startRing == null) continue

                            springJob?.cancelAndJoin()
                            springJob = null

                            when (startRing) {
                                RingType.HOUR -> hAngle.stop()
                                RingType.MINUTE -> mAngle.stop()
                            }

                            activeRing = startRing

                            var dragging = true
                            while (dragging) {
                                val (pressed, position) = awaitPointerEventScope {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == pointerId }
                                    change?.consume()
                                    (change?.pressed ?: false) to change?.position
                                }

                                if (!pressed || position == null) {
                                    dragging = false

                                    val finalHour = hour
                                    val finalMinute = minute
                                    val ring = startRing

                                    springJob = launch {
                                        try {
                                            when (ring) {
                                                RingType.HOUR -> {
                                                    hAngle.animateTo(
                                                        targetValue = hourToAngle(finalHour),
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioMediumBouncy
                                                        )
                                                    )
                                                }

                                                RingType.MINUTE -> {
                                                    mAngle.animateTo(
                                                        targetValue = minuteToAngle(finalMinute),
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioMediumBouncy
                                                        )
                                                    )
                                                }
                                            }
                                            activeRing = null
                                        } catch (_: CancellationException) {
                                            throw CancellationException()
                                        }
                                    }

                                    continue
                                }

                                val center = Offset(size.width / 2f, size.height / 2f)
                                val rawAngle = calculateAngle(position, center)

                                when (startRing) {
                                    RingType.HOUR -> {
                                        val rawHour = angleToHour(rawAngle)
                                        val candidate = clampTime(
                                            time = LocalTime.of(rawHour, minute),
                                            minTime = safeMinTime,
                                            maxTime = safeMaxTime
                                        )

                                        val displayHourAngle =
                                            if (candidate.hour == rawHour) rawAngle else hourToAngle(candidate.hour)

                                        hAngle.snapTo(displayHourAngle)

                                        if (minute != candidate.minute) {
                                            mAngle.snapTo(minuteToAngle(candidate.minute))
                                        }

                                        val timeChanged =
                                            hour != candidate.hour || minute != candidate.minute

                                        if (timeChanged) {
                                            hour = candidate.hour
                                            minute = candidate.minute
                                            vibrateTick(vibrator)
                                            latestOnTimeChange(candidate)
                                        }
                                    }

                                    RingType.MINUTE -> {
                                        val rawMinute = angleToMinute(rawAngle)
                                        val candidate = clampTime(
                                            time = LocalTime.of(hour, rawMinute),
                                            minTime = safeMinTime,
                                            maxTime = safeMaxTime
                                        )

                                        val displayMinuteAngle =
                                            if (candidate.minute == rawMinute) rawAngle else minuteToAngle(candidate.minute)

                                        mAngle.snapTo(displayMinuteAngle)

                                        if (hour != candidate.hour) {
                                            hAngle.snapTo(hourToAngle(candidate.hour))
                                        }

                                        val timeChanged =
                                            hour != candidate.hour || minute != candidate.minute

                                        if (timeChanged) {
                                            hour = candidate.hour
                                            minute = candidate.minute
                                            vibrateTick(vibrator)
                                            latestOnTimeChange(candidate)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            val hRadius = 145.dp.toPx()
            val mRadius = 85.dp.toPx()
            val canvasCenter = center
            val hourRingTopLeft = Offset(canvasCenter.x - hRadius, canvasCenter.y - hRadius)
            val hourRingSize = Size(hRadius * 2, hRadius * 2)

            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = hRadius,
                center = canvasCenter,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = mRadius,
                center = canvasCenter,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = hRadius,
                center = canvasCenter,
                style = Stroke(36.dp.toPx())
            )

            cautionRanges.forEach { (start, end) ->
                if (!end.isAfter(start)) return@forEach
                val durationMinutes = (end.hour * 60 + end.minute) - (start.hour * 60 + start.minute)
                if (durationMinutes <= 0) return@forEach
                val isRelevant = activeZone == TimelineZoneType.CAUTION || showCautionLabel
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = 0.0f),
                            ElectricViolet.copy(alpha = 0.2f),
                            ElectricViolet.copy(alpha = 0.67f).copy(alpha = if (isRelevant) 0.9f else 0.55f),
                            ElectricViolet.copy(alpha = 0.33f),
                            ElectricViolet.copy(alpha = 0.0f)
                        ),
                        center = canvasCenter
                    ),
                    startAngle = timeToDayAngle(start) - 90f,
                    sweepAngle = durationMinutes * 0.25f,
                    useCenter = false,
                    topLeft = hourRingTopLeft,
                    size = hourRingSize,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )

                val markerAngle = timeToDayAngle(start)
                val outerMarker = angleToOffset(canvasCenter, hRadius + 12.dp.toPx(), markerAngle)
                val innerMarker = angleToOffset(canvasCenter, hRadius - 10.dp.toPx(), markerAngle)
                drawLine(
                    color = if (isRelevant) TextPrimary else TextPrimary,
                    start = innerMarker,
                    end = outerMarker,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = if (isRelevant) TextPrimary else TextPrimary,
                    radius = if (isRelevant) 4.dp.toPx() else 3.dp.toPx(),
                    center = outerMarker
                )
            }

            protectedRanges.forEach { (start, end) ->
                if (!end.isAfter(start)) return@forEach
                val durationMinutes = (end.hour * 60 + end.minute) - (start.hour * 60 + start.minute)
                if (durationMinutes <= 0) return@forEach
                val isRelevant = activeZone == TimelineZoneType.PROTECTED || showSleepLabel
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = 0.0f),
                            ElectricViolet.copy(alpha = 0.4f),
                            ElectricViolet.copy(alpha = if (isRelevant) 1f else 0.82f),
                            ElectricViolet.copy(alpha = 0.67f),
                            ElectricViolet.copy(alpha = 0.0f)
                        ),
                        center = canvasCenter
                    ),
                    startAngle = timeToDayAngle(start) - 90f,
                    sweepAngle = durationMinutes * 0.25f,
                    useCenter = false,
                    topLeft = hourRingTopLeft,
                    size = hourRingSize,
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                )

                val markerAngle = timeToDayAngle(start)
                val outerMarker = angleToOffset(canvasCenter, hRadius + 18.dp.toPx(), markerAngle)
                val innerMarker = angleToOffset(canvasCenter, hRadius - 18.dp.toPx(), markerAngle)
                val sweepGlowOuter = angleToOffset(
                    canvasCenter,
                    hRadius + 16.dp.toPx(),
                    ((markerAngle + (sleepSweepPhase * 0.08f)) % 360f)
                )
                drawLine(
                    color = if (isRelevant) TextPrimary else TextPrimary,
                    start = innerMarker,
                    end = outerMarker,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = if (isRelevant) TextPrimary else TextPrimary,
                    radius = if (isRelevant) 5.dp.toPx() else 4.dp.toPx(),
                    center = outerMarker
                )
                if (isRelevant) {
                    drawCircle(
                        color = ElectricViolet.copy(alpha = 0.4f),
                        radius = 8.dp.toPx(),
                        center = sweepGlowOuter
                    )
                }
            }

            occupiedRanges.forEach { (start, end) ->
                if (!end.isAfter(start)) return@forEach
                val durationMinutes = (end.hour * 60 + end.minute) - (start.hour * 60 + start.minute)
                if (durationMinutes <= 0) return@forEach
                val isRelevant = activeZone == TimelineZoneType.OCCUPIED
                drawArc(
                    color = ErrorRed.copy(alpha = if (isRelevant) 0.78f else 0.48f),
                    startAngle = timeToDayAngle(start) - 90f,
                    sweepAngle = durationMinutes * 0.25f,
                    useCenter = false,
                    topLeft = hourRingTopLeft,
                    size = hourRingSize,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = mRadius,
                center = canvasCenter,
                style = Stroke(24.dp.toPx())
            )

            val hRad = Math.toRadians((hAngle.value - 90f).toDouble())
            val hOffset = Offset(
                x = canvasCenter.x + hRadius * cos(hRad).toFloat(),
                y = canvasCenter.y + hRadius * sin(hRad).toFloat()
            )

            if (activeRing == RingType.HOUR) {
                drawCircle(
                    color = if (hasConflict) ErrorRed.copy(alpha = 0.24f) else GoldXP.copy(alpha = 0.2f),
                    radius = 32.dp.toPx(),
                    center = hOffset
                )
            }

            drawCircle(
                color = if (hasConflict) {
                    ErrorRed
                } else if (activeRing == RingType.HOUR) {
                    GoldXP
                } else {
                    GoldXP.copy(alpha = 0.6f)
                },
                radius = if (activeRing == RingType.HOUR) 20.dp.toPx() else 14.dp.toPx(),
                center = hOffset
            )

            val mRad = Math.toRadians((mAngle.value - 90f).toDouble())
            val mOffset = Offset(
                x = canvasCenter.x + mRadius * cos(mRad).toFloat(),
                y = canvasCenter.y + mRadius * sin(mRad).toFloat()
            )

            if (activeRing == RingType.MINUTE) {
                drawCircle(
                    color = if (hasConflict) ErrorRed.copy(alpha = 0.22f) else NeonCyan.copy(alpha = 0.2f),
                    radius = 26.dp.toPx(),
                    center = mOffset
                )
            }

            drawCircle(
                color = if (hasConflict) {
                    ErrorRed
                } else if (activeRing == RingType.MINUTE) {
                    NeonCyan
                } else {
                    NeonCyan.copy(alpha = 0.6f)
                },
                radius = if (activeRing == RingType.MINUTE) 16.dp.toPx() else 11.dp.toPx(),
                center = mOffset
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(centerScale)
        ) {
            Text(
                text = String.format("%02d:%02d", hour, minute),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = if (hasConflict) WarningAmber else Color.White
            )

            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = if (hour < 12) "AM" else "PM",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.Gray
                )
            }
        }

        Surface(
            color = timelinePresentation.background,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
        ) {
            Text(
                text = timelinePresentation.title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = timelinePresentation.color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }

        if (showCautionLabel && cautionLabelOffset != null) {
            val marker = angleToOffset(
                center = Offset(pickerCenterPx, pickerCenterPx),
                radius = cautionLabelRadiusPx,
                angle = cautionLabelOffset
            )
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.offset {
                    IntOffset(marker.x.toInt() - 48, marker.y.toInt() - 34)
                }
            ) {
                Text(
                    text = "SLOW DOWN",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (showSleepLabel && sleepLabelOffset != null) {
            val marker = angleToOffset(
                center = Offset(pickerCenterPx, pickerCenterPx),
                radius = sleepLabelRadiusPx,
                angle = sleepLabelOffset
            )
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.offset {
                    IntOffset(marker.x.toInt() - 34, marker.y.toInt() - 38)
                }
            ) {
                Text(
                    text = "SLEEP LOCK",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}



@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun OrbitalTimePickerPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        OrbitalTimePicker(initialTime = java.time.LocalTime.now(), onTimeChange = {})
    }
}
