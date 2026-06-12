package com.ardym.nitigrow.presentation.feature.contacts.list.components

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.ui.theme.Theme

@Composable
fun AlphabetScrubber(
    letters: List<Char>,
    onLetter: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    if (letters.isEmpty()) return
    var heightPx by remember { mutableIntStateOf(1) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxHeight()
            .padding(end = 4.dp)
            .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
            .pointerInput(letters) {
                detectVerticalDragGestures { change, _ ->
                    val idx = ((change.position.y / heightPx) * letters.size)
                        .toInt().coerceIn(0, letters.lastIndex)
                    onLetter(letters[idx])
                }
            }
    ) {
        letters.forEach { ch ->
            Text(
                text = ch.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.sp
                ),
                color = Theme.colors.muted,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
    }
}
