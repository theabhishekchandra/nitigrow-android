package com.websbaba.nitigrow.presentation.feature.contacts.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Contact
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// ContactSheet — Add / Edit contact bottom sheet (design: "Add contact").
//   • paper sheet, 24dp top radius, 38×4 drag handle
//   • Full name input, WhatsApp number with a fixed "+91" prefix box,
//     single-select tag pills, brand "Save contact" CTA.
// The tag options come from real CRM data (tags already used on contacts);
// the section is hidden entirely when no tags exist yet.
// ─────────────────────────────────────────────────────────────────────────────

private const val IN_PREFIX = "+91"

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
    val colors = Theme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var phone by remember(editing?.id) {
        val raw = editing?.phone.orEmpty()
        mutableStateOf(if (raw.startsWith(IN_PREFIX)) raw.removePrefix(IN_PREFIX) else raw)
    }
    var selectedTag by remember(editing?.id) { mutableStateOf(editing?.tags?.firstOrNull()) }

    // Real tags from the CRM, with the edited contact's own tags always present.
    val options = remember(tagOptions, editing?.id) {
        (editing?.tags.orEmpty() + tagOptions).distinct()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.paper,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .background(colors.muted3, RoundedCornerShape(999.dp))
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(
                text = if (editing == null) "Add contact" else "Edit contact",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    FieldLabel("Full name")
                    SheetInput(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "e.g. Suresh Kumar",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column {
                    FieldLabel("WhatsApp number")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .background(colors.paper2, RoundedCornerShape(12.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 13.dp)
                        ) {
                            Text(
                                text = IN_PREFIX,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.ink3
                            )
                        }
                        SheetInput(
                            value = phone,
                            onValueChange = { raw ->
                                phone = raw.filter { it.isDigit() || it == '+' || it == ' ' }
                            },
                            placeholder = "98XXX XXXXX",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (options.isNotEmpty()) {
                    Column {
                        FieldLabel("Tag")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            options.forEach { tag ->
                                TagChip(
                                    label = tag,
                                    selected = tag == selectedTag,
                                    onClick = {
                                        selectedTag = if (selectedTag == tag) null else tag
                                    }
                                )
                            }
                        }
                    }
                }
                error?.let { ErrorBanner(message = it) }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        val cleaned = phone.filter { it.isDigit() || it == '+' }
                        val fullPhone =
                            if (cleaned.startsWith("+")) cleaned else "$IN_PREFIX$cleaned"
                        onSave(
                            name.trim(),
                            fullPhone,
                            editing?.email, // email isn't edited here; keep the stored value
                            listOfNotNull(selectedTag)
                        )
                    },
                    enabled = name.isNotBlank() && phone.isNotBlank() && !saving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.brand,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = if (editing == null) "Save contact" else "Save changes",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = Theme.colors.ink3,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SheetInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = Theme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.brand),
        keyboardOptions = keyboardOptions,
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(colors.card, RoundedCornerShape(12.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.muted2,
                        maxLines = 1
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun TagChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = Theme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) colors.brand else colors.paper2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else colors.ink3
        )
    }
}
