package com.websbaba.nitigrow.presentation.components.a11y

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// A11y helpers — accessibility primitives applied across every new screen.
// Spec: phase-3-mobile.md Section 5.3 "Accessibility".
//
//   • Modifier.semanticHeading() — marks a Text as a heading for screen readers
//   • Modifier.minTouchTarget()  — enforces 48×48 dp minimum (Android baseline)
//   • A11yIconButton             — IconButton with mandatory contentDescription
//   • A11yStatusDot              — colored dot + screen-reader-only label so
//                                  status is never colour-only
// ─────────────────────────────────────────────────────────────────────────────

/** Minimum touch target — Android's WCAG-equivalent of 48dp. */
val MinTouchTargetDp = 48.dp

/** Diameter of the visible status dot in [A11yStatusDot]. */
private val StatusDotSize = 10.dp

/**
 * Mark this element as a heading so TalkBack announces it as one and lets
 * the user navigate between headings.
 */
fun Modifier.semanticHeading(): Modifier =
    this.semantics { heading() }

/**
 * Enforce the 48dp minimum touch target. Use on small icons/dots that need
 * to be tappable without being visually large.
 */
fun Modifier.minTouchTarget(): Modifier =
    this.defaultMinSize(minWidth = MinTouchTargetDp, minHeight = MinTouchTargetDp)

/**
 * IconButton wrapper that REQUIRES a non-null [contentDescription] and
 * enforces the [MinTouchTargetDp] minimum. Use this anywhere the existing
 * Material `IconButton` is used to remove the chance of a missing label.
 */
@Composable
fun A11yIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    require(contentDescription.isNotBlank()) {
        "A11yIconButton requires a non-blank contentDescription"
    }
    val description = contentDescription
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .minTouchTarget()
            .semantics { this.contentDescription = description },
    ) {
        content()
    }
}

/**
 * A colored status dot whose meaning is also conveyed by a [label] that the
 * screen reader announces. The label is not visible on screen — sighted
 * users see only the dot — but TalkBack reads the label so the colour is
 * never the only signal (phase-3 Section 5.3).
 *
 * @param color the visible dot color (e.g. Niti.colors.success)
 * @param label the spoken label (e.g. "Delivered")
 * @param modifier modifier — by default we DON'T add a touch target since the
 *                 dot is decorative; pass [minTouchTarget] if it's interactive.
 */
@Composable
fun A11yStatusDot(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(StatusDotSize)
            .clip(CircleShape)
            .background(color)
            .semantics(mergeDescendants = true) {
                contentDescription = label
            },
    )
}
