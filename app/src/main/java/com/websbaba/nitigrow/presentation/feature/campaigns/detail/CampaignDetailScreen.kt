package com.websbaba.nitigrow.presentation.feature.campaigns.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiStateView
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.AudienceCard
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.DeliveryFunnelCard
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.DraftHeroCard
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.RecipientsCard
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.ScheduledHeroCard
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.SentHeroCard
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.TemplateCard
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.completedCaption
import com.websbaba.nitigrow.presentation.feature.campaigns.list.components.CampaignStatusPill
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// CampaignDetailScreen — report for one broadcast.
//
//   ◀  Diwali Early Bird
//   [Sending now]
//   ┌─ hero ─ TOTAL SENT + progress (running) / SCHEDULED FOR (scheduled) ┐
//   ┌─ Delivery funnel ─ sent · delivered · read · failed ────────────────┐
//   ┌─ Recipients (scheduled) · Audience · Template ──────────────────────┐
//   [ Cancel broadcast ]  (scheduled / running only)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    onBack: () -> Unit,
    viewModel: CampaignDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors
    val c = state.campaign

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Column(modifier = Modifier.fillMaxSize().background(colors.surface).statusBarsPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            NitiIconButton(icon = NitiIcons.Back, contentDescription = "Back", onClick = onBack)
            Text(
                text = c?.name ?: "Broadcast",
                style = NitiType.title,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        val ptrState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            state = ptrState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = ptrState,
                    isRefreshing = state.isRefreshing,
                    containerColor = colors.primaryTone.container,
                    color = colors.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (c == null) {
                if (state.error != null) {
                    NitiStateView(
                        icon = NitiIcons.Warning,
                        tone = colors.tertiaryTone,
                        title = "Couldn't load this broadcast",
                        body = state.error.orEmpty(),
                        actionLabel = "Retry",
                        onAction = viewModel::refresh
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading…", style = NitiType.body, color = colors.onSurfaceVariant)
                    }
                }
            } else {
                DetailContent(
                    campaign = c,
                    state = state,
                    onCancel = viewModel::onCancelRequested
                )
            }
        }
    }

    if (state.confirmingCancel) {
        CancelDialog(onConfirm = viewModel::onCancelConfirmed, onDismiss = viewModel::onCancelDismissed)
    }
}

@Composable
private fun DetailContent(
    campaign: Campaign,
    state: CampaignDetailUiState,
    onCancel: () -> Unit
) {
    val colors = Niti.colors
    val now = remember(campaign) { Instant.now() }
    val status = campaign.status
    val cancellable = status == CampaignStatus.SCHEDULED || status == CampaignStatus.RUNNING
    val hasSent = status == CampaignStatus.RUNNING || status == CampaignStatus.COMPLETED ||
        status == CampaignStatus.FAILED || status == CampaignStatus.CANCELLED

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CampaignStatusPill(status, modifier = Modifier.padding(start = 20.dp, bottom = 12.dp))

        when {
            status == CampaignStatus.SCHEDULED -> ScheduledHeroCard(campaign.scheduledAt, now)
            status == CampaignStatus.DRAFT -> DraftHeroCard()
            else -> SentHeroCard(
                campaign = campaign,
                showProgress = status == CampaignStatus.RUNNING,
                caption = if (status == CampaignStatus.RUNNING) "Sending in progress" else completedCaption(campaign)
            )
        }

        if (hasSent) DeliveryFunnelCard(campaign) else RecipientsCard(campaign.audienceSize)
        AudienceCard(campaign.audienceTags)
        TemplateCard(templateName = campaign.templateName, template = state.template)

        state.error?.let { msg ->
            Text(
                text = msg,
                style = NitiType.label,
                color = colors.error,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.error.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        if (cancellable) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp)
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, colors.error.copy(alpha = if (state.cancelling) 0.4f else 1f), RoundedCornerShape(26.dp))
                    .clickable(enabled = !state.cancelling, role = Role.Button, onClick = onCancel)
            ) {
                Icon(NitiIcons.Close, contentDescription = null, tint = colors.error, modifier = Modifier.size(20.dp))
                Text(
                    text = if (state.cancelling) "Cancelling…" else "Cancel broadcast",
                    style = NitiType.body.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
                    color = colors.error
                )
            }
        }
        Box(Modifier.size(24.dp))
    }
}

/** Cancelling can't be undone, so ask first. */
@Composable
private fun CancelDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = Niti.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Cancel this broadcast?", style = NitiType.title, color = colors.onSurface) },
        text = {
            Text(
                "Messages that were already sent can't be recalled. Anyone who hasn't received it yet won't.",
                style = NitiType.body,
                color = colors.onSurfaceVariant
            )
        },
        confirmButton = { NitiTextButton(text = "Cancel broadcast", onClick = onConfirm) },
        dismissButton = { NitiTextButton(text = "Keep sending", onClick = onDismiss) }
    )
}
