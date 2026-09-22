package com.websbaba.nitigrow.presentation.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.websbaba.nitigrow.core.util.LocaleManager
import com.websbaba.nitigrow.presentation.components.NitiRadioDot
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.presentation.components.nitiTextFieldColors
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType

/** "Log out of NitiGrow?" confirmation — a bottom sheet with a destructive primary action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutSheet(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = Niti.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .background(colors.outlineVariant, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Log out of NitiGrow?",
                    style = NitiType.title.copy(fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.4).sp),
                    color = colors.onSurface
                )
                Text(
                    text = "You will need to sign in again to reply to customers on this device.",
                    style = NitiType.bodyCompact,
                    color = colors.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.error)
                    .clickable(role = Role.Button, onClick = onConfirm)
            ) {
                Icon(NitiIcons.Logout, contentDescription = null, tint = colors.surface, modifier = Modifier.size(20.dp))
                Text(
                    text = "Log out",
                    style = NitiType.body.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
                    color = colors.surface
                )
            }
            NitiTextButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Language picker: radio rows with the native name and an English gloss. */
@Composable
fun LanguageDialog(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = Niti.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surfaceContainer)
                .padding(24.dp)
        ) {
            Text(
                text = "Pick language",
                style = NitiType.title.copy(fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.4).sp),
                color = colors.onSurface
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
            ) {
                LocaleManager.supported.forEach { (tag, label) ->
                    val selected = tag == current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(selected = selected, role = Role.RadioButton, onClick = { onPick(tag) })
                    ) {
                        NitiRadioDot(selected)
                        Text(
                            text = label,
                            style = NitiType.titleUi.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = languageGloss(tag), style = NitiType.label.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                NitiTextButton(text = "Cancel", onClick = onDismiss)
            }
        }
    }
}

/** Owner-only: permanent account deletion, confirmed with the account password. */
@Composable
fun DeleteAccountDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    isDeleting: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = Niti.colors
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        containerColor = colors.surfaceContainer,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Delete account?",
                style = NitiType.title.copy(fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.4).sp),
                color = colors.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "This permanently deletes your business account and all its data within 30 days. " +
                        "Enter your password to confirm.",
                    style = NitiType.bodyCompact,
                    color = colors.onSurfaceVariant
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    colors = nitiTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(text = it, style = NitiType.label, color = colors.error) }
            }
        },
        confirmButton = {
            NitiTextButton(
                text = if (isDeleting) "Deleting…" else "Delete",
                onClick = onConfirm,
                color = colors.error,
                enabled = password.isNotBlank() && !isDeleting
            )
        },
        dismissButton = { NitiTextButton(text = "Cancel", onClick = onDismiss, enabled = !isDeleting) }
    )
}
