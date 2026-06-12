package com.ardym.nitigrow.presentation.feature.leads.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.ardym.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// LeadAvatar — circular initials avatar for the Leads feature.
// Colour pair comes from Theme.colors.avatars, picked by a stable hash of the
// name (same rotation used by payments / analytics) so a lead keeps its colour
// across kanban, list and detail.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LeadAvatar(
    name: String,
    size: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val colors = Theme.colors
    val initials = remember(name) {
        name.trim()
            .split(" ", limit = 2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifBlank { "?" }
    }
    val (bg, fg) = colors.avatars[Math.floorMod(name.hashCode(), colors.avatars.size)]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = fg,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}
