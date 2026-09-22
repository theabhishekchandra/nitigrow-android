package com.websbaba.nitigrow.presentation.feature.settings.waba

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.domain.model.WabaStatus
import com.websbaba.nitigrow.presentation.feature.settings.components.StatusPill
import com.websbaba.nitigrow.presentation.feature.settings.components.SubScreenHeader
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// WabaNumberScreen — Settings ▸ WhatsApp account.
//
//   ‹ WhatsApp account                                  ⟳
//   ┌ ◯wa  +91 98765 43210  Display name: …   [VERIFIED] ┐
//   │ Quality rating              ● GREEN                │
//   │ Messaging limit             Tier · 1K/24h          │
//   ┌ TIER USAGE TODAY  Business-initiated  412 / 1,000  ┐
//   [Re-link WhatsApp account]
// ─────────────────────────────────────────────────────────────────────────────

private val SectionSpacing = 14.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WabaNumberScreen(
    onBack: () -> Unit,
    viewModel: WabaNumberViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        viewModel.effects.collectLatest { e ->
            when (e) {
                is WabaNumberEffect.Toast -> snackbar.showSnackbar(e.text)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            SubScreenHeader(title = "WhatsApp account", onBack = onBack) {
                IconButton(onClick = viewModel::refresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = colors.onSurfaceVariant)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        WabaNumberBody(
            state = state,
            onRelink = viewModel::reverify,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun WabaNumberBody(
    state: WabaNumberUiState,
    onRelink: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Niti.colors
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        AccountCard(state)
        if (state.dailyLimit > 0) {
            TierUsageCard(used = state.dailyUsed, limit = state.dailyLimit)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceLow)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClick = onRelink)
                .padding(vertical = 13.dp)
        ) {
            Text(
                "Re-link WhatsApp account",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
        }
    }
}

@Composable
private fun AccountCard(state: WabaNumberUiState) {
    val colors = Niti.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceLow)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .background(colors.primaryTone.container, CircleShape)
            ) {
                Icon(
                    Icons.Filled.Whatsapp,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.phone.ifBlank { "Not linked" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                if (state.displayName.isNotBlank()) {
                    Text(
                        "Display name: ${state.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
            StatusPillFor(state.status)
        }
        HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(vertical = 14.dp))
        KeyValueRow(label = "Quality rating") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(qualityColor(state.qualityRating), CircleShape)
                )
                Text(
                    state.qualityRating.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = qualityColor(state.qualityRating)
                )
            }
        }
        KeyValueRow(label = "Messaging limit") {
            Text(
                state.messagingLimit,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
        }
        state.verifiedAt?.let { at ->
            KeyValueRow(label = "Verified") {
                Text(
                    formatVerifiedAt(at),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun KeyValueRow(label: String, value: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Niti.colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        value()
    }
}

@Composable
private fun StatusPillFor(status: WabaStatus) {
    val colors = Niti.colors
    val (bg, fg, label) = when (status) {
        WabaStatus.ACTIVE -> Triple(colors.primaryTone.container, colors.primary, "VERIFIED")
        WabaStatus.PENDING -> Triple(colors.secondaryTone.container, colors.secondaryTone.onContainer, "PENDING")
        WabaStatus.SUSPENDED -> Triple(colors.error.copy(alpha = 0.12f), colors.error, "SUSPENDED")
        WabaStatus.FAILED -> Triple(colors.error.copy(alpha = 0.12f), colors.error, "FAILED")
        WabaStatus.NOT_LINKED -> Triple(colors.surfaceLow, colors.onSurfaceVariant, "NOT LINKED")
    }
    StatusPill(label, bg = bg, fg = fg)
}

@Composable
private fun qualityColor(rating: String): Color = when (rating.uppercase(Locale.ROOT)) {
    "GREEN" -> Niti.colors.primary
    "YELLOW" -> Niti.colors.warning
    "RED" -> Niti.colors.error
    else -> Niti.colors.onSurfaceVariant
}

@Composable
private fun TierUsageCard(used: Int, limit: Int) {
    val colors = Niti.colors
    val nf = remember { NumberFormat.getIntegerInstance(Locale.ENGLISH) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceLow)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            "TIER USAGE TODAY",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
            Text(
                "Business-initiated",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${nf.format(used)} / ${nf.format(limit)}",
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(colors.surfaceLow, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (used.toFloat() / limit).coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(colors.primary, RoundedCornerShape(4.dp))
            )
        }
    }
}

private val verifiedAtFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

private fun formatVerifiedAt(instant: Instant): String =
    verifiedAtFormatter.format(instant.atZone(ZoneId.systemDefault()).toLocalDate())

@Preview(showBackground = true, name = "WABA — active")
@Composable
private fun PreviewWabaNumberBody() {
    NitiGrowTheme {
        WabaNumberBody(
            state = WabaNumberUiState(
                phone = "+91 98765 43210",
                displayName = "Sharma Sweets",
                status = WabaStatus.ACTIVE,
                qualityRating = "GREEN",
                messagingLimit = "1K/24h",
                dailyUsed = 412,
                dailyLimit = 1000
            ),
            onRelink = {}
        )
    }
}
