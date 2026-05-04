package com.ardym.nitigrow.presentation.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.core.util.LocaleManager
import com.ardym.nitigrow.domain.model.NotificationPreferences
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.inbox.list.components.Avatar
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    onProfileEdit: () -> Unit,
    onTeam: () -> Unit,
    onBilling: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsHubViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var langDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var deleteReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is SettingsHubEffect.Toast -> snackbar.showSnackbar(e.text)
                SettingsHubEffect.DeleteConfirmed ->
                    snackbar.showSnackbar("Account deletion scheduled. You can undo within 7 days.")
                SettingsHubEffect.LoggedOut -> onLogout()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                state.profile?.let { profile ->
                    ProfileHeader(profile.name, profile.email, profile.phone, onProfileEdit)
                    HorizontalDivider()
                }
            }
            item {
                state.tenant?.let { tenant ->
                    TenantCard(tenant.name, tenant.wabaPhone, tenant.wabaStatus.name, tenant.planName)
                    HorizontalDivider()
                }
            }
            item {
                SectionHeader("Account")
                Row1(Icons.Filled.Group, "Team members", "Invite admins and agents", onTeam)
                Row1(Icons.Filled.CreditCard, "Billing", "Plans and payments", onBilling)
                HorizontalDivider()
            }
            item {
                SectionHeader("Notifications")
                NotifToggle(
                    "Chat", state.notificationPrefs.chatEnabled
                ) { v -> viewModel.toggleNotif { it.copy(chatEnabled = v) } }
                NotifToggle(
                    "Campaigns", state.notificationPrefs.campaignEnabled
                ) { v -> viewModel.toggleNotif { it.copy(campaignEnabled = v) } }
                NotifToggle(
                    "System", state.notificationPrefs.systemEnabled
                ) { v -> viewModel.toggleNotif { it.copy(systemEnabled = v) } }
                NotifToggle(
                    "Sound", state.notificationPrefs.soundEnabled
                ) { v -> viewModel.toggleNotif { it.copy(soundEnabled = v) } }
                NotifToggle(
                    "Show preview", state.notificationPrefs.previewVisible
                ) { v -> viewModel.toggleNotif { it.copy(previewVisible = v) } }
                HorizontalDivider()
            }
            item {
                SectionHeader("Preferences")
                val langLabel = LocaleManager.supported
                    .firstOrNull { it.first == state.languageTag }?.second ?: "System default"
                Row1(Icons.Filled.Language, "Language", langLabel) { langDialog = true }
                HorizontalDivider()
            }
            item {
                SectionHeader("Privacy")
                Row1(
                    icon = Icons.Filled.Download,
                    title = if (state.isExporting) "Exporting…" else "Export my data",
                    subtitle = "Receive a CSV/JSON export by email",
                    onClick = viewModel::requestExport
                )
                Row1(
                    icon = Icons.Filled.Delete,
                    title = "Delete account",
                    subtitle = "Schedules deletion within 30 days (DPDP Act)",
                    onClick = { deleteDialog = true }
                )
                HorizontalDivider()
            }
            item {
                state.error?.let { ErrorBanner(message = it, modifier = Modifier.padding(16.dp)) }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = viewModel::signOut,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("Sign out") }
            }
        }
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
            onDismissRequest = { deleteDialog = false },
            title = { Text("Delete account?") },
            text = {
                Column {
                    Text(
                        "This schedules permanent deletion within 30 days. You can undo within 7 days via email link."
                    )
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = deleteReason,
                        onValueChange = { deleteReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmDelete(deleteReason.ifBlank { null })
                        deleteDialog = false
                    },
                    enabled = !state.isDeleting
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileHeader(name: String, email: String, phone: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = name, url = null, sizeDp = 56)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Edit", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TenantCard(name: String, wabaPhone: String?, wabaStatus: String, planName: String?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("WABA: ${wabaPhone ?: "Not linked"} ($wabaStatus)", style = MaterialTheme.typography.bodySmall)
            planName?.let {
                Text("Plan: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun Row1(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NotifToggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable
private fun LanguageDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pick language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
