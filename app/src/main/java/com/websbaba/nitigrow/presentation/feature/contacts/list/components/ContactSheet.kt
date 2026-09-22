package com.websbaba.nitigrow.presentation.feature.contacts.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.websbaba.nitigrow.domain.model.Contact
import com.websbaba.nitigrow.presentation.components.NitiChip
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiSectionLabel
import com.websbaba.nitigrow.presentation.components.nitiTextFieldColors
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType

// ─────────────────────────────────────────────────────────────────────────────
// ContactSheet — Add / Edit contact bottom sheet (design: "Add contact").
//   Full name · WhatsApp number (+91 prefix) · Email (optional) · Tags · Save.
// Tags are multi-select from the tags already used across the CRM, plus an
// "Add tag" chip that creates a new one inline.
// ─────────────────────────────────────────────────────────────────────────────

internal const val IN_PREFIX = "+91"

/**
 * Number as typed in the field: the +91 prefix is shown separately, so drop it.
 * The backend stores Indian numbers both as "+91…" and as bare "91…" (12 digits,
 * no plus), so both are recognised. Anything else is left as it is.
 */
internal fun phoneForEditing(stored: String): String {
    val trimmed = stored.trim()
    if (trimmed.startsWith(IN_PREFIX)) return trimmed.removePrefix(IN_PREFIX).trim()
    val digits = trimmed.filter { it.isDigit() }
    return if (trimmed.all { it.isDigit() || it == ' ' } && digits.length == 12 && digits.startsWith("91")) {
        digits.removePrefix("91")
    } else trimmed
}

/** Full number to save: digits (and a leading +), defaulting to the +91 country code. */
internal fun phoneForSaving(typed: String): String {
    val cleaned = typed.filter { it.isDigit() || it == '+' }
    return if (cleaned.startsWith("+")) cleaned else "$IN_PREFIX$cleaned"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactSheet(
    editing: Contact?,
    saving: Boolean,
    error: String?,
    tagOptions: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String?, tags: List<String>) -> Unit
) {
    val colors = Niti.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var phone by remember(editing?.id) { mutableStateOf(phoneForEditing(editing?.phone.orEmpty())) }
    var email by remember(editing?.id) { mutableStateOf(editing?.email.orEmpty()) }
    var selectedTags by remember(editing?.id) { mutableStateOf(editing?.tags.orEmpty().toSet()) }
    var createdTags by remember(editing?.id) { mutableStateOf(emptyList<String>()) }
    var addingTag by remember(editing?.id) { mutableStateOf(false) }
    var newTag by remember(editing?.id) { mutableStateOf("") }

    // Real CRM tags, the edited contact's own tags and any tag created in this sheet.
    val options = remember(tagOptions, editing?.id, createdTags) {
        (editing?.tags.orEmpty() + tagOptions + createdTags).distinct()
    }

    fun commitNewTag() {
        val tag = newTag.trim()
        if (tag.isNotEmpty()) {
            if (options.none { it.equals(tag, ignoreCase = true) }) createdTags = createdTags + tag
            selectedTags = selectedTags + (options.firstOrNull { it.equals(tag, ignoreCase = true) } ?: tag)
        }
        newTag = ""
        addingTag = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .background(colors.outlineVariant, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (editing == null) "Add contact" else "Edit contact",
                    style = NitiType.title.copy(fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.5).sp),
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                NitiIconButton(
                    icon = NitiIcons.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    tint = colors.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                placeholder = { Text("e.g. Suresh Kumar") },
                leadingIcon = { Icon(NitiIcons.Person, contentDescription = null, modifier = Modifier.size(22.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = nitiTextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { raw -> phone = raw.filter { it.isDigit() || it == '+' || it == ' ' } },
                label = { Text("WhatsApp number") },
                placeholder = { Text("98XXX XXXXX") },
                prefix = { Text(IN_PREFIX, style = NitiType.body.copy(fontWeight = FontWeight.Medium)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = nitiTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional)") },
                placeholder = { Text("name@example.com") },
                leadingIcon = { Icon(NitiIcons.Mail, contentDescription = null, modifier = Modifier.size(22.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = nitiTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                NitiSectionLabel("Tags")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { tag ->
                        NitiChip(
                            label = tag,
                            selected = tag in selectedTags,
                            onClick = { selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag }
                        )
                    }
                    if (!addingTag) {
                        NitiChip(label = "Add tag", selected = false, onClick = { addingTag = true }, icon = NitiIcons.Plus)
                    }
                }
                if (addingTag) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text("New tag") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = nitiTextFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { commitNewTag() }),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }

            error?.let {
                Text(
                    text = it,
                    style = NitiType.label,
                    color = colors.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.error.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            val canSave = name.isNotBlank() && phone.isNotBlank() && !saving
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (canSave || saving) colors.primary else colors.surfaceHigh)
                    .clickable(enabled = canSave, role = Role.Button) {
                        // A tag still being typed counts as added when the user saves.
                        val pending = newTag.trim()
                        val tags = (selectedTags + listOfNotNull(pending.takeIf { addingTag && it.isNotEmpty() })).toList()
                        onSave(name.trim(), phoneForSaving(phone), email.trim().ifEmpty { null }, tags)
                    }
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                } else {
                    Text(
                        text = if (editing == null) "Save contact" else "Save changes",
                        style = NitiType.body.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
                        color = if (canSave) colors.onPrimary else colors.outline
                    )
                }
            }
        }
    }
}
