package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType

/** Centered day chip ("Today", "Yesterday") between message groups. */
@Composable
fun DateSeparator(label: String, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = NitiType.caption,
            color = Niti.colors.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Niti.colors.surfaceContainer)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
