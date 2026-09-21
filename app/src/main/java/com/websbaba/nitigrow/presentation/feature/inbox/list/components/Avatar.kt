package com.websbaba.nitigrow.presentation.feature.inbox.list.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.websbaba.nitigrow.core.ui.theme.NitiType
import com.websbaba.nitigrow.core.ui.theme.Theme

/**
 * Circular initials avatar. Colour pair comes from the warm 7-way rotation in
 * Theme.colors.avatars, picked by a stable hash of the name so a contact keeps
 * the same colour everywhere.
 */
@Composable
fun Avatar(
    name: String,
    url: String?,
    modifier: Modifier = Modifier,
    sizeDp: Int = 48
) {
    val initials = remember(name) {
        name.trim()
            .split(" ", limit = 2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifBlank { "?" }
    }
    val palette = Theme.colors.avatars
    val (bg, fg) = remember(name, palette) {
        palette[(name.hashCode().toLong() and 0x7fffffff).rem(palette.size).toInt()]
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = name,
                modifier = Modifier.size(sizeDp.dp).clip(CircleShape),
                error = { Initials(initials, fg, sizeDp) },
                loading = { Initials(initials, fg, sizeDp) }
            )
        } else {
            Initials(initials, fg, sizeDp)
        }
    }
}

@Composable
private fun Initials(text: String, color: Color, sizeDp: Int) {
    Text(
        text = text,
        color = color,
        style = NitiType.titleUi,
        // 17sp on the standard 48dp avatar, scaled for smaller and larger ones.
        fontSize = (sizeDp * 17f / 48f).sp,
        lineHeight = TextUnit.Unspecified,
        fontWeight = FontWeight.SemiBold
    )
}
