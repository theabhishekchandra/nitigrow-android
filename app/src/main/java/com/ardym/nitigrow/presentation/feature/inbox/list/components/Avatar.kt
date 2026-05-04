package com.ardym.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

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
    val color = remember(name) { colorForName(name) }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = name,
                modifier = Modifier.size(sizeDp.dp).clip(CircleShape),
                error = { Initials(initials) },
                loading = { Initials(initials) }
            )
        } else {
            Initials(initials)
        }
    }
}

@Composable
private fun Initials(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

private val palette = listOf(
    Color(0xFF1F8A70), Color(0xFF2E7CB7), Color(0xFFB8651D),
    Color(0xFF8E44AD), Color(0xFFC0392B), Color(0xFF16A085),
    Color(0xFF2C3E50), Color(0xFFD35400)
)

private fun colorForName(name: String): Color {
    val idx = (name.hashCode().toLong() and 0x7fffffff).rem(palette.size).toInt()
    return palette[idx]
}
