package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import kotlin.math.abs
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// VoiceRecorderBar — visual-only UI shown in place of the input bar while the
// user is recording a voice note.
// See docs/phase-3-mobile.md "Voice note button".
//
// Contents (left → right):
//   • Cancel icon / red record dot (pulses)
//   • MM:SS elapsed timer (caller drives elapsedMs via a ticker)
//   • Animated waveform — pseudo-random pulsing bars in `Canvas`
//   • "← Slide to cancel" hint (hidden once `locked = true`)
//   • Lock toggle (replaces the slide-to-cancel hint once activated)
//   • Send button
//
// State semantics:
//   cancelled → bar fades into a "Cancelling…" state and shows the trash icon.
//   locked    → slide-to-cancel hint disappears and lock icon flips to filled.
//
// Actual MediaRecorder hookup is out of scope (see phase-3 doc) — this file
// renders the visual layer only and emits callbacks for cancel/lock/send.
// ─────────────────────────────────────────────────────────────────────────────

private const val WAVEFORM_BAR_COUNT = 32
private const val WAVEFORM_MIN_HEIGHT_RATIO = 0.18f
private const val WAVEFORM_MAX_HEIGHT_RATIO = 0.95f
private const val WAVEFORM_ANIM_DURATION_MS = 900
private const val PULSE_DURATION_MS = 1_000

@Composable
fun VoiceRecorderBar(
    elapsedMs: Long,
    cancelled: Boolean,
    locked: Boolean,
    onLock: () -> Unit,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val colors = Niti.colors

    Surface(
        tonalElevation = 2.dp,
        color = colors.surfaceLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .height(56.dp)
        ) {
            // Cancel / record-dot indicator on the far left.
            IconButton(onClick = onCancel) {
                if (cancelled) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Cancel recording",
                        tint = colors.error
                    )
                } else {
                    RecordDot()
                }
            }

            // MM:SS timer — fixed-width feel via tabular digits in body style.
            Text(
                text = formatElapsed(elapsedMs),
                style = MaterialTheme.typography.bodyLarge,
                color = if (cancelled) colors.onSurfaceVariant else colors.onSurface,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.padding(start = 10.dp))

            // Waveform + hint share the middle column.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (cancelled) {
                    Text(
                        text = "Cancelling…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.error,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Waveform(
                        color = colors.primary,
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    )
                }

                if (!cancelled && !locked) {
                    SlideToCancelHint(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.padding(start = 10.dp))

            // Lock toggle.
            IconButton(onClick = onLock, enabled = !cancelled) {
                Icon(
                    imageVector = if (locked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (locked) "Recording locked" else "Lock recording",
                    tint = if (locked) colors.primary else colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.padding(start = 4.dp))

            // Send button — circular brand chip.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
            ) {
                IconButton(
                    onClick = onSend,
                    enabled = !cancelled,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send voice note",
                        tint = colors.surface
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordDot() {
    val colors = Niti.colors
    val transition = rememberInfiniteTransition(label = "record-dot")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = PULSE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "record-dot-alpha"
    )
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(colors.error.copy(alpha = alpha))
    )
}

@Composable
private fun Waveform(color: Color, modifier: Modifier = Modifier) {
    // A single animated phase shift drives every bar — cheaper than animating
    // each bar independently and yields a more cohesive "scrolling" look.
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = WAVEFORM_ANIM_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform-phase"
    )

    // Per-bar pseudo-random seed so the waveform looks organic rather than a
    // perfect sine. Re-uses `remember` so we don't churn allocations.
    val seeds = remember {
        FloatArray(WAVEFORM_BAR_COUNT) { i -> ((i * 7919) % 100) / 100f }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gap = w / (WAVEFORM_BAR_COUNT * 2f)
        val barWidth = (w - gap * (WAVEFORM_BAR_COUNT + 1)) / WAVEFORM_BAR_COUNT
        for (i in 0 until WAVEFORM_BAR_COUNT) {
            val seed = seeds[i]
            val pulse = abs(sin(phase + i * 0.35f + seed * 3f))
            val ratio = WAVEFORM_MIN_HEIGHT_RATIO + (WAVEFORM_MAX_HEIGHT_RATIO - WAVEFORM_MIN_HEIGHT_RATIO) * pulse
            val barHeight = h * ratio
            val x = gap + i * (barWidth + gap)
            val y = (h - barHeight) / 2f
            drawRoundedBar(color = color, topLeft = Offset(x, y), size = Size(barWidth, barHeight))
        }
    }
}

private fun DrawScope.drawRoundedBar(
    color: Color,
    topLeft: Offset,
    size: Size
) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)
    )
}

@Composable
private fun SlideToCancelHint(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Niti.colors.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.padding(start = 4.dp))
        Text(
            text = "Slide to cancel",
            style = MaterialTheme.typography.labelMedium,
            color = Niti.colors.onSurfaceVariant
        )
    }
}

private fun formatElapsed(elapsedMs: Long): String {
    val totalSeconds = (elapsedMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "VoiceRecorderBar — recording 00:07", showBackground = true)
@Composable
private fun PreviewVoiceRecorderRecording() {
    NitiGrowTheme {
        VoiceRecorderBar(
            elapsedMs = 7_300L,
            cancelled = false,
            locked = false,
            onLock = {},
            onCancel = {},
            onSend = {}
        )
    }
}

@Preview(name = "VoiceRecorderBar — locked 00:42", showBackground = true)
@Composable
private fun PreviewVoiceRecorderLocked() {
    NitiGrowTheme {
        VoiceRecorderBar(
            elapsedMs = 42_000L,
            cancelled = false,
            locked = true,
            onLock = {},
            onCancel = {},
            onSend = {}
        )
    }
}

@Preview(name = "VoiceRecorderBar — cancelling", showBackground = true)
@Composable
private fun PreviewVoiceRecorderCancelled() {
    NitiGrowTheme {
        VoiceRecorderBar(
            elapsedMs = 12_000L,
            cancelled = true,
            locked = false,
            onLock = {},
            onCancel = {},
            onSend = {}
        )
    }
}
