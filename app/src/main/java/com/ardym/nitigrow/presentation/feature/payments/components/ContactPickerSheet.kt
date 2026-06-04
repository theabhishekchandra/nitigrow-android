package com.ardym.nitigrow.presentation.feature.payments.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.domain.model.Contact
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ContactPickerSheet — bottom sheet that lets the user pick a contact to send
// a payment link to. Backed by DummyData.contacts() for now; will be replaced
// with the live ContactRepository observable once that's wired.
//
//   ┌──────────────────────────────────────┐
//   │ Send to                              │
//   │ ┌──────────────────────────────────┐ │
//   │ │ 🔍 Search name or phone          │ │
//   │ └──────────────────────────────────┘ │
//   │ ⬤ Priya Sharma                       │
//   │ P  +91 98201 11122                   │
//   │ ⬤ Rahul Verma                        │
//   │ R  +91 98765 43210                   │
//   │  ...                                 │
//   └──────────────────────────────────────┘
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
        containerColor = Theme.colors.card,
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
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Send to",
            style = MaterialTheme.typography.titleLarge,
            color = colors.ink,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.padding(top = 8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search name or phone") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.padding(top = 12.dp))

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
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(filtered, key = { it.id }) { contact ->
                    ContactRow(contact = contact, onClick = { onPick(contact) })
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    val colors = Theme.colors
    val (bg, fg) = colors.avatars[Math.floorMod(contact.name.hashCode(), colors.avatars.size)]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contact.initial.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.ink,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
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
