package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

/**
 * IronMind's core visual identity: the Integrity Shield.
 *
 * - Fills bottom-up with today's integrity level.
 * - Accumulates hairline cracks as temptations are caught (TemptationCaught).
 * - Glows briefly on wins (SessionCompleted) and bursts with particles on milestones.
 * - Seals at night: a clean gleam sweep on a good day (cracks reset to zero),
 *   or a dimmer "reforging" sweep on a rough day (cracks halve, never fully erased —
 *   the point is visible, incremental repair, never a wall of shame).
 *
 * This composable is pure UI. It never plays sound or vibrates directly — it reports
 * intent via onHaptic/onSound so the host (ViewModel / Activity) owns actual playback
 * through your existing WorkLockManager / SleepLockSoundService-style infrastructure.
 * That keeps this file trivially reusable and previewable without a real device.
 */

enum class ShieldMode(val primary: Color, val secondary: Color) {
    IRON(SurfaceElevated, ElectricViolet),
    BUILD(SurfaceElevated, SuccessGreen),
    RECOVERY(WarningAmber, WarningAmber),
    EXPERIMENT(ElectricViolet, ElectricViolet)
}

sealed class ShieldEvent {
    data object TemptationCaught : ShieldEvent()
    data object SessionCompleted : ShieldEvent()
    data object MilestoneHit : ShieldEvent()
    data class DaySealed(val wasGoodDay: Boolean) : ShieldEvent()
}

/** Custom vibration patterns — trigger via VibratorManager/VibrationEffect at the host level. */
enum class HapticPattern { STARTLE, SEGMENT_WIN, CELEBRATION, WARM_SEAL, REFORGE_PULSE }

/** Sound cues — play via SoundPool/MediaPlayer at the host level. */
enum class ShieldSound { GLASS_CRACK, DING, LOW_CHIME, RISING_CHORD, REFORGE_SHIMMER }

@Composable
fun IntegrityShield(
    integrityLevel: Float,
    mode: ShieldMode,
    event: ShieldEvent?,
    onEventHandled: () -> Unit,
    modifier: Modifier = Modifier,
    initialCrackCount: Int = 0,
    eventId: Long = 0L,
    onHaptic: (HapticPattern) -> Unit = {},
    onSound: (ShieldSound) -> Unit = {}
) {
    val systemHaptic = LocalHapticFeedback.current
    val fillLevel by animateFloatAsState(
        targetValue = integrityLevel.coerceIn(0f, 1f),
        animationSpec = tween(600, easing = EaseInOutCubic),
        label = "fillLevel"
    )

    // A recap may render a persisted shield from yesterday. During a live day this stays
    // unchanged (zero by default) and events add the visible cracks one at a time.
    var crackCount by remember(initialCrackCount) {
        mutableIntStateOf(initialCrackCount.coerceIn(0, 6))
    }
    val flashAlpha = remember { Animatable(0f) }
    val glowPulse = remember { Animatable(0f) }
    val sealSweep = remember { Animatable(0f) }
    var particles by remember { mutableStateOf(emptyList<Particle>()) }

    // eventId makes back-to-back events of the same type (two temptation catches, for
    // example) distinct to Compose. Keying only on the sealed event object drops repeats.
    LaunchedEffect(eventId) {
        when (val e = event) {
            is ShieldEvent.TemptationCaught -> {
                crackCount = (crackCount + 1).coerceAtMost(6)
                onHaptic(HapticPattern.STARTLE)
                onSound(ShieldSound.GLASS_CRACK)
                systemHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                flashAlpha.snapTo(0.9f)
                flashAlpha.animateTo(0f, tween(400))
                onEventHandled()
            }

            is ShieldEvent.SessionCompleted -> {
                onHaptic(HapticPattern.SEGMENT_WIN)
                onSound(ShieldSound.DING)
                glowPulse.snapTo(0f)
                glowPulse.animateTo(1f, tween(250))
                glowPulse.animateTo(0f, tween(450))
                onEventHandled()
            }

            is ShieldEvent.MilestoneHit -> {
                onHaptic(HapticPattern.CELEBRATION)
                onSound(ShieldSound.DING)
                particles = generateParticles(mode.primary, mode.secondary)
                delay(900)
                particles = emptyList()
                onEventHandled()
            }

            is ShieldEvent.DaySealed -> {
                if (e.wasGoodDay) {
                    onHaptic(HapticPattern.WARM_SEAL)
                    onSound(ShieldSound.LOW_CHIME)
                    sealSweep.snapTo(0f)
                    sealSweep.animateTo(1f, tween(1200, easing = EaseInOutCubic))
                    crackCount = 0
                } else {
                    onHaptic(HapticPattern.REFORGE_PULSE)
                    onSound(ShieldSound.REFORGE_SHIMMER)
                    sealSweep.snapTo(0f)
                    sealSweep.animateTo(1f, tween(1400, easing = EaseInOutCubic))
                    crackCount /= 2 // visible, partial repair — never a full wipe, never left broken
                }
                sealSweep.animateTo(0f, tween(400))
                onEventHandled()
            }

            null -> Unit
        }
    }

    Canvas(modifier = modifier.aspectRatio(0.9f)) {
        val shieldPath = buildShieldPath(size.width, size.height)

        clipPath(shieldPath) {
            drawRect(color = mode.primary.copy(alpha = 0.12f))

            val fillTop = size.height * (1f - fillLevel)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(mode.secondary, mode.primary),
                    startY = fillTop,
                    endY = size.height
                ),
                topLeft = Offset(0f, fillTop),
                size = Size(size.width, size.height - fillTop)
            )

            repeat(crackCount) { index ->
                drawCrack(seed = index, size = size, alpha = 0.3f + (index * 0.05f))
            }

            if (flashAlpha.value > 0f) {
                drawRect(color = Color.White.copy(alpha = flashAlpha.value * 0.35f))
            }

            if (glowPulse.value > 0f) {
                drawRect(color = mode.secondary.copy(alpha = glowPulse.value * 0.4f))
            }

            if (sealSweep.value > 0f) {
                val sweepY = size.height * sealSweep.value
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent),
                        startY = sweepY - 60f,
                        endY = sweepY + 60f
                    )
                )
            }
        }

        drawPath(shieldPath, color = mode.primary, style = Stroke(width = 2.5f))

        particles.forEach { p ->
            drawCircle(color = p.color.copy(alpha = p.alpha), radius = p.radius, center = p.position(size))
        }
    }
}

private fun buildShieldPath(width: Float, height: Float): Path = Path().apply {
    moveTo(width * 0.5f, 0f)
    cubicTo(width * 0.85f, height * 0.05f, width * 0.95f, height * 0.15f, width * 0.95f, height * 0.35f)
    cubicTo(width * 0.95f, height * 0.68f, width * 0.75f, height * 0.87f, width * 0.5f, height)
    cubicTo(width * 0.25f, height * 0.87f, width * 0.05f, height * 0.68f, width * 0.05f, height * 0.35f)
    cubicTo(width * 0.05f, height * 0.15f, width * 0.15f, height * 0.05f, width * 0.5f, 0f)
    close()
}

/** Deterministic jagged line, seeded by index so each accumulated crack is distinct but stable across recompositions. */
private fun DrawScope.drawCrack(seed: Int, size: Size, alpha: Float) {
    val rnd = Random(seed * 97 + 13)
    val startX = size.width * (0.3f + rnd.nextFloat() * 0.4f)
    val path = Path().apply {
        moveTo(startX, 0f)
        var x = startX
        var y = 0f
        repeat(5) {
            x += (rnd.nextFloat() - 0.5f) * size.width * 0.18f
            y += size.height / 5f
            lineTo(x, y)
        }
    }
    drawPath(path, color = Color.White.copy(alpha = alpha), style = Stroke(width = 1.5f, cap = StrokeCap.Round))
}

private data class Particle(val angle: Float, val distance: Float, val radius: Float, val alpha: Float, val color: Color) {
    fun position(size: Size): Offset {
        val cx = size.width / 2f
        val cy = size.height / 2f
        return Offset(cx + cos(angle) * distance, cy + sin(angle) * distance)
    }
}

private fun generateParticles(c1: Color, c2: Color): List<Particle> {
    val rnd = Random.Default
    return List(14) {
        Particle(
            angle = rnd.nextFloat() * (2 * Math.PI.toFloat()),
            distance = 40f + rnd.nextFloat() * 90f,
            radius = 2f + rnd.nextFloat() * 3f,
            alpha = 0.5f + rnd.nextFloat() * 0.5f,
            color = if (rnd.nextBoolean()) c1 else c2
        )
    }
}

// ---------- Previews ----------

@Preview(showBackground = true, name = "Intact - Iron mode")
@Composable
private fun PreviewIntact() {
    IntegrityShield(
        integrityLevel = 0.7f, mode = ShieldMode.IRON,
        event = null, onEventHandled = {}, modifier = Modifier.size(220.dp)
    )
}

@Preview(showBackground = true, name = "Recovery mode, low fill")
@Composable
private fun PreviewRecovery() {
    IntegrityShield(
        integrityLevel = 0.3f, mode = ShieldMode.RECOVERY,
        event = null, onEventHandled = {}, modifier = Modifier.size(220.dp)
    )
}

@Preview(showBackground = true, name = "Build mode, near full")
@Composable
private fun PreviewBuild() {
    IntegrityShield(
        integrityLevel = 0.95f, mode = ShieldMode.BUILD,
        event = null, onEventHandled = {}, modifier = Modifier.size(220.dp)
    )
}

/**
 * Drop this into a debug screen or Activity to test every event live with real
 * animations, before wiring the shield into Morning Launch / Live Day / Night Flow.
 */
@Composable
fun IntegrityShieldDemo() {
    var event by remember { mutableStateOf<ShieldEvent?>(null) }
    var level by remember { mutableFloatStateOf(0.5f) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IntegrityShield(
            integrityLevel = level,
            mode = ShieldMode.IRON,
            event = event,
            onEventHandled = { event = null },
            modifier = Modifier.size(240.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = { event = ShieldEvent.TemptationCaught }) { Text("Temptation caught") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            event = ShieldEvent.SessionCompleted
            level = (level + 0.1f).coerceAtMost(1f)
        }) { Text("Session completed") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { event = ShieldEvent.MilestoneHit }) { Text("Milestone hit") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { event = ShieldEvent.DaySealed(wasGoodDay = true) }) { Text("Seal day - good") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { event = ShieldEvent.DaySealed(wasGoodDay = false) }) { Text("Seal day - rough") }
    }
}
