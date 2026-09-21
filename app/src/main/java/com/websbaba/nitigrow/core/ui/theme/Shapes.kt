package com.websbaba.nitigrow.core.ui.theme

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

// Chat bubble shapes: 20dp corners with a 6dp tail corner — outbound flattens
// bottom-right, inbound bottom-left.
val BubbleOutShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 6.dp,
    bottomStart = 20.dp,
)

val BubbleInShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 20.dp,
    bottomStart = 6.dp,
)
