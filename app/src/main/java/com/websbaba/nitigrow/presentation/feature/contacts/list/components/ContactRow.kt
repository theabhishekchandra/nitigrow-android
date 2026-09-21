package com.websbaba.nitigrow.presentation.feature.contacts.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Contact
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.core.ui.theme.Bricolage
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType

private const val MAX_TAG_PILLS = 2

// ─────────────────────────────────────────────────────────────────────────────
// ContactRow — one CRM contact in the Contacts list.
//   [44dp avatar]  Name (16/22 · 500)                    [tag] [tag]
//                  +91 98XXX XXXXX (13/18)
// Up to two tag pills; "VIP" is amber, everything else viridian. The row keeps
// 44dp clear on the right for the A–Z scrubber.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ContactRow(contact: Contact, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clickable(role = Role.Button, onClickLabel = "Edit contact", onClick = onClick)
            .padding(start = 20.dp, end = 44.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = contact.name, url = contact.avatarUrl, sizeDp = 44)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = NitiType.titleUi.copy(fontWeight = FontWeight.Medium),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phone,
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
                maxLines = 1
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            contact.tags.take(MAX_TAG_PILLS).forEach { TagPill(it) }
        }
    }
}

@Composable
private fun TagPill(tag: String) {
    val colors = Niti.colors
    val tone = if (tag.equals("VIP", ignoreCase = true)) colors.secondaryTone else colors.primaryTone
    Text(
        text = tag,
        style = NitiType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        color = tone.onContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .height(20.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tone.container)
            .padding(horizontal = 9.dp)
            .wrapContentHeight()
    )
}

/** A–Z section header. */
@Composable
fun LetterHeader(letter: Char, modifier: Modifier = Modifier) {
    Text(
        text = letter.toString(),
        style = NitiType.label.copy(
            fontFamily = Bricolage,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        ),
        color = Niti.colors.primary,
        modifier = modifier
            .fillMaxWidth()
            .background(Niti.colors.surface)
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 4.dp)
    )
}
