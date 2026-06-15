package com.websbaba.nitigrow.presentation.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.BuildConfig
import com.websbaba.nitigrow.core.util.LocaleManager
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.domain.model.WabaStatus
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.settings.components.NgToggle
import com.websbaba.nitigrow.presentation.feature.settings.components.StatusPill
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// SettingsHubScreen — "Settings" tab root.
//
//   Settings                                       (Fraunces, no back)
//   ┌ avatar · name/email · OWNER pill ┐
//   ┌ WORKSPACE: Business profile / WhatsApp account / Auto-replies / Team ┐
//   ┌ GROWTH: Billing & plan / Refer & earn ┐
//   ┌ SHORTCUTS / NOTIFICATIONS / APP / PRIVACY ┐   (kept functionality)
//   [Log out]  → confirm bottom sheet
//   NitiGrow v1.0 · com.websbaba.nitigrow
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    onProfileEdit: () -> Unit,
    onTeam: () -> Unit,
    onBilling: () -> Unit,
    onTemplates: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onPaymentLink: () -> Unit = {},
    onBusinessProfile: () -> Unit = {},
    onWabaNumber: () -> Unit = {},
    onAutoReply: () -> Unit = {},
    onAppearance: () -> Unit = {},
    onLeadsKanban: () -> Unit = {},
    onLeadsList: () -> Unit = {},
    onReferrals: () -> Unit = {},
    onConflicts: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: SettingsHubViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Theme.colors
    val snackbar = remember { SnackbarHostState() }
    var langDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var logoutSheet by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is SettingsHubEffect.Toast -> snackbar.showSnackbar(e.text)
                SettingsHubEffect.LoggedOut -> onLogout()
            }
        }
    }

    Scaffold(
        containerColor = colors.paper,
        topBar = {
            Text(
                "Settings",
                style = MaterialTheme.typography.displaySmall,
                color = colors.ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                state.profile?.let { profile ->
                    ProfileCard(
                        name = profile.name,
                        email = profile.email,
                        role = profile.role,
                        onClick = onProfileEdit
                    )
                }
            }

            item {
                GroupCard("WORKSPACE") {
                    HubRow(title = "Business profile", onClick = onBusinessProfile)
                    HubRow(
                        title = "WhatsApp account",
                        pill = state.tenant?.let { wabaPillFor(it.wabaStatus) },
                        onClick = onWabaNumber
                    )
                    HubRow(title = "Auto-replies", onClick = onAutoReply)
                    HubRow(
                        title = "Team",
                        value = state.teamCount?.let { n ->
                            if (n == 1) "1 member" else "$n members"
                        },
                        onClick = onTeam
                    )
                }
            }

            item {
                GroupCard("GROWTH") {
                    HubRow(title = "Billing & plan", value = state.tenant?.planName, onClick = onBilling)
                    HubRow(
                        title = "Refer & earn",
                        pill = state.referralCreditPaise
                            ?.takeIf { it > 0 }
                            ?.let { PillSpec("${formatRupees(it)} EARNED", PillTone.TURMERIC) },
                        onClick = onReferrals
                    )
                }
            }

            item {
                GroupCard("SHORTCUTS") {
                    HubRow(title = "Templates", onClick = onTemplates)
                    HubRow(title = "Analytics", onClick = onAnalytics)
                    HubRow(title = "Send payment link", onClick = onPaymentLink)
                    HubRow(title = "Leads — Kanban", onClick = onLeadsKanban)
                    HubRow(title = "Leads — List", onClick = onLeadsList)
                    HubRow(title = "Sync conflicts", onClick = onConflicts)
                }
            }

            item {
                GroupCard("NOTIFICATIONS") {
                    ToggleRow("Chat", state.notificationPrefs.chatEnabled) { v ->
                        viewModel.toggleNotif { it.copy(chatEnabled = v) }
                    }
                    ToggleRow("Campaigns", state.notificationPrefs.campaignEnabled) { v ->
                        viewModel.toggleNotif { it.copy(campaignEnabled = v) }
                    }
                    ToggleRow("System", state.notificationPrefs.systemEnabled) { v ->
                        viewModel.toggleNotif { it.copy(systemEnabled = v) }
                    }
                    ToggleRow("Sound", state.notificationPrefs.soundEnabled) { v ->
                        viewModel.toggleNotif { it.copy(soundEnabled = v) }
                    }
                    ToggleRow("Show preview", state.notificationPrefs.previewVisible) { v ->
                        viewModel.toggleNotif { it.copy(previewVisible = v) }
                    }
                }
            }

            item {
                val langLabel = LocaleManager.supported
                    .firstOrNull { it.first == state.languageTag }?.second ?: "System default"
                GroupCard("APP") {
                    HubRow(title = "Language", value = langLabel, onClick = { langDialog = true })
                    HubRow(title = "Appearance", icon = Icons.Filled.Palette, onClick = onAppearance)
                }
            }

            item {
                GroupCard("PRIVACY") {
                    HubRow(
                        title = if (state.isExporting) "Exporting…" else "Export my data",
                        onClick = viewModel::requestExport
                    )
                    // Deleting the account erases the whole tenant — owner-only,
                    // mirroring the backend's requireRole('owner') guard.
                    if (state.profile?.role == UserRole.OWNER) {
                        HubRow(title = "Delete account", onClick = { deleteDialog = true })
                    }
                }
            }

            item {
                state.error?.let {
                    ErrorBanner(message = it, modifier = Modifier.padding(bottom = 14.dp))
                }
                LogoutButton(onClick = { logoutSheet = true })
            }

            item {
                Text(
                    "NitiGrow v${BuildConfig.VERSION_NAME} · ${BuildConfig.APPLICATION_ID}",
                    fontSize = 11.sp,
                    color = colors.muted2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (logoutSheet) {
        LogoutConfirmSheet(
            onConfirm = {
                logoutSheet = false
                viewModel.signOut()
            },
            onDismiss = { logoutSheet = false }
        )
    }

    if (langDialog) {
        LanguageDialog(
            current = state.languageTag,
            onPick = { tag ->
                viewModel.setLanguage(tag)
                langDialog = false
            },
            onDismiss = { langDialog = false }
        )
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeleting) { deleteDialog = false; deletePassword = "" }
            },
            title = { Text("Delete account?") },
            text = {
                Column {
                    Text(
                        "This permanently deletes your business account and all its " +
                            "data within 30 days. Enter your password to confirm."
                    )
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    state.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Theme.colors.danger, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete(deletePassword) },
                    enabled = deletePassword.isNotBlank() && !state.isDeleting
                ) { Text(if (state.isDeleting) "Deleting…" else "Delete", color = Theme.colors.danger) }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteDialog = false; deletePassword = "" },
                    enabled = !state.isDeleting
                ) { Text("Cancel") }
            }
        )
    }
}

// ── profile card ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(name: String, email: String, role: UserRole, onClick: () -> Unit) {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Avatar(name = name, url = null, sizeDp = 52)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                maxLines = 1
            )
            Text(
                email,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                maxLines = 1,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
        RolePill(role)
    }
}

@Composable
private fun RolePill(role: UserRole) {
    val colors = Theme.colors
    val (bg, fg) = when (role) {
        UserRole.OWNER -> colors.brandSoft to colors.brand
        UserRole.ADMIN -> colors.turmericSoft to colors.turmericInk
        UserRole.AGENT -> colors.paper2 to colors.ink3
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 4.dp)
    ) {
        Text(role.name, color = fg, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, lineHeight = 13.sp)
    }
}

// ── grouped rows ─────────────────────────────────────────────────────────────

@Composable
private fun GroupCard(label: String, content: @Composable () -> Unit) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
        )
        content()
    }
}

private enum class PillTone { BRAND, TURMERIC, DANGER, NEUTRAL }
private data class PillSpec(val label: String, val tone: PillTone)

private fun wabaPillFor(status: WabaStatus): PillSpec = when (status) {
    WabaStatus.ACTIVE -> PillSpec("CONNECTED", PillTone.BRAND)
    WabaStatus.PENDING -> PillSpec("PENDING", PillTone.TURMERIC)
    WabaStatus.SUSPENDED -> PillSpec("SUSPENDED", PillTone.DANGER)
    WabaStatus.FAILED -> PillSpec("FAILED", PillTone.DANGER)
    WabaStatus.NOT_LINKED -> PillSpec("NOT LINKED", PillTone.NEUTRAL)
}

@Composable
private fun HubRow(
    title: String,
    onClick: (() -> Unit)?,
    value: String? = null,
    pill: PillSpec? = null,
    icon: ImageVector? = null
) {
    val colors = Theme.colors
    HorizontalDivider(color = colors.border2)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 13.dp)
    ) {
        icon?.let {
            Icon(it, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
        }
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.ink,
            modifier = Modifier.weight(1f)
        )
        pill?.let {
            val (bg, fg) = when (it.tone) {
                PillTone.BRAND -> colors.brandSoft to colors.brand
                PillTone.TURMERIC -> colors.turmericSoft to colors.turmericInk
                PillTone.DANGER -> colors.danger.copy(alpha = 0.12f) to colors.danger
                PillTone.NEUTRAL -> colors.paper2 to colors.ink3
            }
            StatusPill(it.label, bg = bg, fg = fg)
        }
        value?.let {
            Text(it, fontSize = 11.5.sp, color = colors.muted)
        }
        if (onClick != null) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colors.muted2,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    val colors = Theme.colors
    HorizontalDivider(color = colors.border2)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.ink,
            modifier = Modifier.weight(1f)
        )
        NgToggle(checked = value, onCheckedChange = onChange)
    }
}

// ── log out ──────────────────────────────────────────────────────────────────

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    val colors = Theme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .border(1.dp, colors.danger.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Text("Log out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.danger)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutConfirmSheet(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = Theme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
        Column(modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 24.dp)) {
            Text(
                "Log out?",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                "You'll need to sign in again to access your workspace on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink3,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.danger)
                    .clickable(onClick = onConfirm)
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    "Log out",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (colors.isLight) colors.paper else colors.ink
                )
            }
            Spacer(Modifier.height(9.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 13.dp)
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
            }
        }
    }
}

// ── language picker (existing behaviour, lightly restyled) ──────────────────

@Composable
private fun LanguageDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Pick language",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LocaleManager.supported.forEach { (tag, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(tag) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = tag == current,
                            onClick = { onPick(tag) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        }
    }
}

// ── helpers ──────────────────────────────────────────────────────────────────

private fun formatRupees(paise: Long): String =
    "₹" + NumberFormat.getIntegerInstance(Locale.ENGLISH).format(paise / 100)
