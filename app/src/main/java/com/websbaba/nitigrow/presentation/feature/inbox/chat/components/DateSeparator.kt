package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.ui.theme.Theme

/** Centered ALL-CAPS day pill ("TODAY", "YESTERDAY") on the chat wash. */
@Composable
fun DateSeparator(label: String, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = Theme.colors.muted,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Theme.colors.paper3)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
