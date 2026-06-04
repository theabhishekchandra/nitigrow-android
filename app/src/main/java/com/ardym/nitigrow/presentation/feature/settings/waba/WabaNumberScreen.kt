package com.ardym.nitigrow.presentation.feature.settings.waba

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.domain.model.WabaStatus
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PageGutter = 16.dp
private val CardGutter = 16.dp
private val SectionSpacing = 12.dp
private val PillCornerRadius = 999.dp
private val QualityDotSize = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WabaNumberScreen(
    onBack: () -> Unit,
    viewModel: WabaNumberViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp number", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        WabaNumberBody(
            state = state,
            onReverify = viewModel::reverify,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun WabaNumberBody(
    state: WabaNumberUiState,
    onReverify: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(PageGutter),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        PrimaryNumberCard(state = state)
        QualityRatingCard(rating = state.qualityRating)
        MessagingLimitCard(limit = state.messagingLimit, verifiedAt = state.verifiedAt)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SectionSpacing)
        ) {
            OutlinedButton(
                onClick = onReverify,
                modifier = Modifier.weight(1f)
            ) { Text("Re-verify") }
        }
    }
}

@Composable
private fun PrimaryNumberCard(state: WabaNumberUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardGutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = null,
                    tint = Theme.colors.brand
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    state.displayName.ifBlank { "Not linked" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                StatusPill(state.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                state.phone.ifBlank { "—" },
                style = MaterialTheme.typography.headlineSmall,
                color = Theme.colors.ink
            )
        }
    }
}

@Composable
private fun StatusPill(status: WabaStatus) {
    val (bg, fg, label) = when (status) {
        WabaStatus.ACTIVE -> Triple(Theme.colors.success.copy(alpha = 0.16f), Theme.colors.success, "ACTIVE")
        WabaStatus.PENDING -> Triple(Theme.colors.warning.copy(alpha = 0.16f), Theme.colors.warning, "PENDING")
        WabaStatus.SUSPENDED -> Triple(Theme.colors.danger.copy(alpha = 0.16f), Theme.colors.danger, "SUSPENDED")
        WabaStatus.FAILED -> Triple(Theme.colors.danger.copy(alpha = 0.16f), Theme.colors.danger, "FAILED")
        WabaStatus.NOT_LINKED -> Triple(Theme.colors.muted.copy(alpha = 0.16f), Theme.colors.muted, "NOT LINKED")
    }
    Box(
        modifier = Modifier
            .background(bg, shape = RoundedCornerShape(PillCornerRadius))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun QualityRatingCard(rating: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(CardGutter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QualityDot(rating)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Quality rating",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    rating.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                qualityHint(rating),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QualityDot(rating: String) {
    val color = when (rating.uppercase(Locale.ROOT)) {
        "GREEN" -> Theme.colors.success
        "YELLOW" -> Theme.colors.warning
        "RED" -> Theme.colors.danger
        else -> Theme.colors.muted
    }
    Box(
        modifier = Modifier
            .size(QualityDotSize)
            .background(color, CircleShape)
    )
}

private fun qualityHint(rating: String): String = when (rating.uppercase(Locale.ROOT)) {
    "GREEN" -> "Healthy"
    "YELLOW" -> "Watch"
    "RED" -> "Action needed"
    else -> ""
}

@Composable
private fun MessagingLimitCard(limit: String, verifiedAt: Instant?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardGutter)) {
            Text(
                "Messaging limit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                limit,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (verifiedAt != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Verified ${formatVerifiedAt(verifiedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                displayName = "Aarav Traders",
                status = com.ardym.nitigrow.domain.model.WabaStatus.ACTIVE,
                qualityRating = "GREEN",
                messagingLimit = "1K/24h"
            ),
            onReverify = {}
        )
    }
}
