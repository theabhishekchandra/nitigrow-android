package com.ardym.nitigrow.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radii are mirrored from app/src/index.css --r-* tokens. Compose's Shapes
// has only five slots; map them so that any default Material 3 component
// (Card, FilledButton, etc.) automatically picks the right corner. Anything
// that needs a specific token (chat bubbles, bottom sheets) overrides per
// site with explicit RoundedCornerShape(...).
val NitiGrowShapes = Shapes(
    extraSmall = RoundedCornerShape(9.dp),   // --r-sm — chips, kbd
    small      = RoundedCornerShape(12.dp),  // --r    — inputs, cards
    medium     = RoundedCornerShape(16.dp),  // --r-lg — cards, sheets
    large      = RoundedCornerShape(22.dp),  // --r-xl — hero panels
    extraLarge = RoundedCornerShape(28.dp),
)

// Bubble shapes used by the inbox chat. Outbound (visitor) sits on the
// right with a flattened bottom-right; inbound (agent) on the left with a
// flattened bottom-left. Mirrors the app's --r-lg pattern.
val BubbleOutShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomEnd = 4.dp,
    bottomStart = 16.dp,
)

val BubbleInShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomEnd = 16.dp,
    bottomStart = 4.dp,
)
