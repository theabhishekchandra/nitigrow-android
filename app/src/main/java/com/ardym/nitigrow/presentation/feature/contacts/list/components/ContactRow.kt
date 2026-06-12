package com.ardym.nitigrow.presentation.feature.contacts.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.domain.model.Contact
import com.ardym.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.ardym.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// ContactRow — one CRM contact in the Contacts list.
//   [44dp avatar]  Name (14sp / 600)            [tag pill]
//                  +91 98XXX XXXXX (12sp muted)
// Tag pill = first tag of the contact (neutral paper2/ink3 pill); hidden when
// the contact has no tags. Divider is drawn by the list, not the row.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ContactRow(contact: Contact, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Theme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = contact.name, url = contact.avatarUrl, sizeDp = 44)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                maxLines = 1
            )
        }
        contact.tags.firstOrNull()?.let { tag ->
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .background(colors.paper2, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.sp
                    ),
                    color = colors.ink3,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun LetterHeader(letter: Char, modifier: Modifier = Modifier) {
    Text(
        text = letter.toString(),
        style = MaterialTheme.typography.labelSmall,
        color = Theme.colors.muted,
        modifier = modifier
            .fillMaxWidth()
            .background(Theme.colors.paper)
            .padding(horizontal = 18.dp, vertical = 5.dp)
    )
}
