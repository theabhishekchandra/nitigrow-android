package com.websbaba.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.ui.theme.Theme

/** Brand-green unread pill: min 19dp circle, grows into a pill for 2+ digits. */
@Composable
fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    val label = if (count > 99) "99+" else count.toString()
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 19.dp, minHeight = 19.dp)
            .clip(CircleShape)
            .background(Theme.colors.brand)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Theme.colors.paper,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                letterSpacing = 0.sp
            ),
            fontWeight = FontWeight.Bold
        )
    }
}
