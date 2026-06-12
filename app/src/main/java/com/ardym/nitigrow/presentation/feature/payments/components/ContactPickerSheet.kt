package com.ardym.nitigrow.presentation.feature.payments.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.domain.model.Contact
import com.ardym.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ContactPickerSheet — "Choose contact" bottom sheet (design: pick-contact
// sheet). The [contacts] list is supplied by the caller from the live
// ContactRepository (GET /api/contacts) — see PaymentLinkViewModel.
//
//   ── drag handle ──
//   Choose contact            (Fraunces 19sp)
//   [🔍 Search name or phone]
//   ⬤ Priya Sharma                          ›
//      +91 98201 11122
//   ──────────────────────────── border2 ───
//   ⬤ Rahul Verma                           ›
//      +91 98765 43210
//
// Filtering is a case-insensitive `contains` on name OR phone — fine for the
// few hundred contacts an SMB realistically has on device.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerSheet(
    onPick: (contactId: String, contactName: String) -> Unit,
    onDismiss: () -> Unit,
    contacts: List<Contact>,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Theme.colors.paper,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { SheetDragHandle() },
    ) {
        ContactPickerContent(
            contacts = contacts,
            onPick = { c ->
                scope.launch {
                    sheetState.hide()
                    onPick(c.id, c.name)
                    onDismiss()
                }
            }
        )
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .width(38.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Theme.colors.muted3)
    )
}

@Composable
private fun ContactPickerContent(
    contacts: List<Contact>,
    onPick: (Contact) -> Unit,
) {
    val colors = Theme.colors
    var query by remember { mutableStateOf("") }

    val filtered = remember(contacts, query) {
        if (query.isBlank()) contacts
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.phone.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Choose contact",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 19.sp,
            color = colors.ink,
        )

        Spacer(Modifier.height(12.dp))

        SearchField(query = query, onQueryChange = { query = it })

        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching contacts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
            ) {
                items(filtered, key = { it.id }) { contact ->
                    ContactRow(contact = contact, onClick = { onPick(contact) })
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val colors = Theme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(16.dp),
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
            cursorBrush = SolidColor(colors.brand),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search name or phone",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.muted,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    val colors = Theme.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 11.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(name = contact.name, url = contact.avatarUrl, sizeDp = 40)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    color = colors.muted,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colors.muted2,
                modifier = Modifier.size(16.dp),
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.border2)
    }
}

// ── Previews ────────────────────────────────────────────────────────────────
// ModalBottomSheet itself doesn't render inside @Preview reliably, so we
// preview the inner content directly.

@Preview(showBackground = true, name = "ContactPicker — content")
@Composable
private fun PreviewContactPickerContent() {
    NitiGrowTheme {
        ContactPickerContent(
            contacts = listOf(
                Contact(
                    id = "c1", name = "Riya Shah", phone = "+919812345678", email = "riya@shah.in",
                    avatarUrl = null, tags = listOf("vip"), notes = null,
                    createdAt = java.time.Instant.now(), updatedAt = java.time.Instant.now(), isBlocked = false
                ),
                Contact(
                    id = "c2", name = "Aman Gupta", phone = "+919812300000", email = null,
                    avatarUrl = null, tags = emptyList(), notes = null,
                    createdAt = java.time.Instant.now(), updatedAt = java.time.Instant.now(), isBlocked = false
                )
            ),
            onPick = {}
        )
    }
}
