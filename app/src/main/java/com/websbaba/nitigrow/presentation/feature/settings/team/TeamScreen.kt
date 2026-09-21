package com.websbaba.nitigrow.presentation.feature.settings.team

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.settings.components.FieldLabel
import com.websbaba.nitigrow.presentation.feature.settings.components.NgTextField
import com.websbaba.nitigrow.presentation.feature.settings.components.PillShape
import com.websbaba.nitigrow.presentation.feature.settings.components.PrimaryCta
import com.websbaba.nitigrow.presentation.feature.settings.components.StatusPill
import com.websbaba.nitigrow.presentation.feature.settings.components.SubScreenHeader
import kotlinx.coroutines.flow.collectLatest
import com.websbaba.nitigrow.core.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// TeamScreen — Settings ▸ Team.
//
//   ‹ Team
//     N members
//   ┌ ◯AS  Anita Sharma · email             [OWNER] ┐
//   ┌ ◯RS  Rohit Sharma · email             [AGENT] 🗑 ┐
//   [Invite teammate]  → invite bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    onBack: () -> Unit,
    viewModel: TeamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Theme.colors
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) { is TeamEffect.Toast -> snackbar.showSnackbar(e.text) }
        }
    }

    Scaffold(
        containerColor = colors.paper,
        topBar = {
            SubScreenHeader(
                title = "Team",
                onBack = onBack,
                subtitle = state.members.size.takeIf { it > 0 }?.let { n ->
                    if (n == 1) "1 member" else "$n members"
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.error?.let {
                item { ErrorBanner(message = it) }
            }
            items(items = state.members, key = { it.id }) { m ->
                MemberCard(m, onRemove = { viewModel.removeMember(m.id) })
            }
            item {
                InviteButton(onClick = viewModel::openInviteSheet)
            }
        }
    }

    if (state.sheetOpen) {
        InviteSheet(
            saving = state.saving,
            error = state.error,
            onDismiss = viewModel::closeInviteSheet,
            onInvite = viewModel::inviteMember
        )
    }
}

@Composable
private fun MemberCard(member: TeamMember, onRemove: () -> Unit) {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(15.dp))
            .padding(14.dp)
    ) {
        Avatar(name = member.name, url = null, sizeDp = 44)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                member.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
                maxLines = 1
            )
            Text(
                member.email,
                fontSize = 11.5.sp,
                color = colors.muted,
                maxLines = 1
            )
        }
        RolePill(member.role)
        if (!member.isOwner) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove ${member.name}",
                    tint = colors.danger,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
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
    StatusPill(role.name, bg = bg, fg = fg)
}

@Composable
private fun InviteButton(onClick: () -> Unit) {
    val colors = Theme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.brandSoft)
            .border(1.dp, colors.brand.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Text(
            "Invite teammate",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.brand
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteSheet(
    saving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onInvite: (String, String) -> Unit
) {
    val colors = Theme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("AGENT") }

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
                    .background(colors.muted3, PillShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Invite teammate",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp),
                color = colors.ink
            )
            Column {
                FieldLabel("Email")
                NgTextField(value = email, onValueChange = { email = it })
            }
            Column {
                FieldLabel("Role")
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    RoleChip(label = "Admin", selected = role == "ADMIN") { role = "ADMIN" }
                    RoleChip(label = "Agent", selected = role == "AGENT") { role = "AGENT" }
                }
            }
            error?.let { ErrorBanner(message = it) }
            PrimaryCta(
                text = "Send invite",
                onClick = { onInvite(email, role) },
                loading = saving,
                enabled = email.isNotBlank()
            )
        }
    }
}

@Composable
private fun RoleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = Theme.colors
    val bg = if (selected) colors.brand else colors.paper2
    val fg = when {
        !selected -> colors.ink3
        colors.isLight -> colors.paper
        else -> colors.brandInk
    }
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(bg)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}
