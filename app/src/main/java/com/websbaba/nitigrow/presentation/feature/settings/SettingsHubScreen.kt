package com.websbaba.nitigrow.presentation.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.BuildConfig
import com.websbaba.nitigrow.core.util.LocaleManager
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────────────────
// SettingsHubScreen — "Settings" tab root.
//
//   Settings
//   [ avatar · name · email · OWNER ›]           tonal profile card
//   WORKSPACE   Business profile · WhatsApp account · Auto-replies · Team
//   GROWTH      Billing & plan · Refer & earn
//   SHORTCUTS   Commerce · Flows · Templates · Analytics · Payment link · Leads …
//   NOTIFICATIONS   Chat · Campaigns · System · Sound · Show preview   (switches)
//   APP         Language · Appearance
//   PRIVACY     Export my data · Delete account (owner only)
//   [ Log out ]  → confirm sheet          NitiGrow v1.0 · com.websbaba.nitigrow
// ─────────────────────────────────────────────────────────────────────────────

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
    onCommerce: () -> Unit = {},
    onFlows: () -> Unit = {},
    onConflicts: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: SettingsHubViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors
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

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Box(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.size(56.dp))
            Text(
                text = "Settings",
                style = NitiType.display.copy(letterSpacing = (-0.8).sp),
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
            )

            state.profile?.let { profile ->
                HubProfileCard(name = profile.name, email = profile.email, role = profile.role, onClick = onProfileEdit)
            }

            HubSection("WORKSPACE") {
                HubRow(NitiIcons.Building, colors.primaryTone, "Business profile", onBusinessProfile)
                HubRow(
                    NitiIcons.Chat, colors.secondaryTone, "WhatsApp account", onWabaNumber,
                    pill = state.tenant?.let { wabaPillFor(it.wabaStatus) }
                )
                HubRow(NitiIcons.Bolt, colors.tertiaryTone, "Auto-replies", onAutoReply)
                HubRow(
                    NitiIcons.Contacts, colors.infoTone, "Team", onTeam,
                    value = state.teamCount?.let(::teamLabel)
                )
            }

            HubSection("GROWTH") {
                HubRow(NitiIcons.Card, colors.primaryTone, "Billing & plan", onBilling, value = state.tenant?.planName)
                HubRow(
                    NitiIcons.Gift, colors.secondaryTone, "Refer & earn", onReferrals,
                    pill = state.referralCreditPaise?.takeIf { it > 0 }
                        ?.let { PillSpec("${formatRupees(it)} EARNED", PillKind.WARNING) }
                )
            }

            HubSection("SHORTCUTS") {
                HubRow(NitiIcons.Bag, colors.tertiaryTone, "Commerce", onCommerce)
                HubRow(NitiIcons.Flow, colors.infoTone, "WhatsApp Flows", onFlows)
                HubRow(NitiIcons.Document, colors.primaryTone, "Templates", onTemplates)
                HubRow(NitiIcons.BarChart, colors.secondaryTone, "Analytics", onAnalytics)
                HubRow(NitiIcons.Rupee, colors.tertiaryTone, "Send payment link", onPaymentLink)
                HubRow(NitiIcons.Leads, colors.infoTone, "Leads — Kanban", onLeadsKanban)
                HubRow(NitiIcons.ListBullets, colors.primaryTone, "Leads — List", onLeadsList)
                HubRow(NitiIcons.Refresh, colors.secondaryTone, "Sync conflicts", onConflicts)
            }

            HubSection("NOTIFICATIONS") {
                val prefs = state.notificationPrefs
                HubToggleRow(NitiIcons.Chat, colors.tertiaryTone, "Chat", prefs.chatEnabled) { v ->
                    viewModel.toggleNotif { it.copy(chatEnabled = v) }
                }
                HubToggleRow(NitiIcons.Megaphone, colors.infoTone, "Campaigns", prefs.campaignEnabled) { v ->
                    viewModel.toggleNotif { it.copy(campaignEnabled = v) }
                }
                HubToggleRow(NitiIcons.Bell, colors.primaryTone, "System", prefs.systemEnabled) { v ->
                    viewModel.toggleNotif { it.copy(systemEnabled = v) }
                }
                HubToggleRow(NitiIcons.Speaker, colors.secondaryTone, "Sound", prefs.soundEnabled) { v ->
                    viewModel.toggleNotif { it.copy(soundEnabled = v) }
                }
                HubToggleRow(NitiIcons.Eye, colors.tertiaryTone, "Show preview", prefs.previewVisible) { v ->
                    viewModel.toggleNotif { it.copy(previewVisible = v) }
                }
            }

            HubSection("APP") {
                val langLabel = LocaleManager.supported.firstOrNull { it.first == state.languageTag }?.second
                    ?: "System default"
                HubRow(NitiIcons.Globe, colors.infoTone, "Language", { langDialog = true }, value = langLabel)
                HubRow(NitiIcons.Palette, colors.primaryTone, "Appearance", onAppearance)
            }

            HubSection("PRIVACY") {
                HubRow(
                    NitiIcons.Upload, colors.secondaryTone,
                    if (state.isExporting) "Exporting…" else "Export my data",
                    viewModel::requestExport
                )
                // Deleting the account erases the whole tenant — owner-only,
                // mirroring the backend's requireRole('owner') guard.
                if (state.profile?.role == UserRole.OWNER) {
                    HubRow(NitiIcons.Trash, colors.errorTone, "Delete account", { deleteDialog = true })
                }
            }

            state.error?.let { msg ->
                if (!deleteDialog) {
                    Text(
                        text = msg,
                        style = NitiType.label,
                        color = colors.error,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.error.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            LogoutButton(onClick = { logoutSheet = true })

            Text(
                text = "NitiGrow v${BuildConfig.VERSION_NAME} · ${BuildConfig.APPLICATION_ID}",
                style = NitiType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp)
            )
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
    }

    if (logoutSheet) {
        LogoutSheet(
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
        DeleteAccountDialog(
            password = deletePassword,
            onPasswordChange = { deletePassword = it },
            isDeleting = state.isDeleting,
            error = state.error,
            onConfirm = { viewModel.confirmDelete(deletePassword) },
            onDismiss = { deleteDialog = false; deletePassword = "" }
        )
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(26.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp)
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .border(1.dp, colors.error, shape)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Icon(NitiIcons.Logout, contentDescription = null, tint = colors.error, modifier = Modifier.size(20.dp))
        Text(
            text = "Log out",
            style = NitiType.body.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
            color = colors.error
        )
    }
}
