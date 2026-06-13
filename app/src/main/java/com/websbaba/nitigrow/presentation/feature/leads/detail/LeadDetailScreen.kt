package com.websbaba.nitigrow.presentation.feature.leads.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.leads.components.LeadAvatar
import com.websbaba.nitigrow.presentation.feature.leads.components.formatInr
import com.websbaba.nitigrow.presentation.feature.leads.kanban.components.relativeTime
import com.websbaba.nitigrow.ui.theme.Theme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// LeadDetailScreen — single-lead view per the Claude Design handoff:
// profile card (54dp avatar, name, value + source pill), STAGE chip selector
// wired to PATCH /api/leads/:id/stage, "Open chat" + "Mark as Won" actions,
// NOTES card (when the lead has notes) and an ACTIVITY timeline derived from
// the lead's real stage / created / updated fields. No fake content.
// ─────────────────────────────────────────────────────────────────────────────

private val CardShape16 = RoundedCornerShape(16.dp)
private val CardShape18 = RoundedCornerShape(18.dp)
private val ButtonShape = RoundedCornerShape(13.dp)
private val ChipShape = RoundedCornerShape(999.dp)
private val PillShape = RoundedCornerShape(999.dp)

private val createdDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale("en", "IN"))
        .withZone(ZoneId.systemDefault())

@Composable
fun LeadDetailScreen(
    onBack: () -> Unit,
    onOpenChat: (contactId: String) -> Unit = {},
    viewModel: LeadDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lead = state.lead

    Scaffold(containerColor = Theme.colors.paper) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 6.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Theme.colors.ink,
                    )
                }
                Text(
                    text = "Lead detail",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 19.sp),
                    color = Theme.colors.ink,
                )
            }

            state.error?.let {
                ErrorBanner(message = it, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
            }

            when {
                lead != null -> LeadDetailContent(
                    lead = lead,
                    movingStage = state.movingStage,
                    onMoveStage = viewModel::onMoveStage,
                    onOpenChat = { onOpenChat(lead.contactId) },
                )

                state.isRefreshing -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Theme.colors.brand) }

                else -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "This lead is no longer available.",
                        fontSize = 14.sp,
                        color = Theme.colors.muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeadDetailContent(
    lead: Lead,
    movingStage: Boolean,
    onMoveStage: (LeadStage) -> Unit,
    onOpenChat: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ProfileCard(lead = lead)
        StageSelector(
            current = lead.stage,
            enabled = !movingStage,
            onSelect = onMoveStage,
        )
        ActionRow(
            lead = lead,
            movingStage = movingStage,
            onOpenChat = onOpenChat,
            onMarkWon = { onMoveStage(LeadStage.WON) },
        )
        if (!lead.notes.isNullOrBlank()) {
            NotesCard(notes = lead.notes)
        }
        ActivityCard(lead = lead)
    }
}

// ─── Profile ────────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(lead: Lead) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape18)
            .background(Theme.colors.card)
            .border(width = 1.dp, color = Theme.colors.border, shape = CardShape18)
            .padding(18.dp),
    ) {
        LeadAvatar(name = lead.contactName, size = 54.dp, fontSize = 18.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lead.contactName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Theme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = lead.contactPhone,
                fontSize = 13.sp,
                color = Theme.colors.muted,
                modifier = Modifier.padding(top = 2.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Text(
                    text = formatInr(lead.valueInr),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Theme.colors.brand,
                )
                Text(
                    text = lead.source,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Theme.colors.paper2)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }
    }
}

// ─── Stage selector ─────────────────────────────────────────────────────────

@Composable
private fun StageSelector(
    current: LeadStage,
    enabled: Boolean,
    onSelect: (LeadStage) -> Unit,
) {
    Column {
        SectionLabel(text = "STAGE")
        // Six domain stages (the design shows four): two rows of three equal
        // chips so labels like "Contacted" stay readable on phone widths.
        val rows = LeadStage.entries.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    row.forEach { stage ->
                        StageChip(
                            stage = stage,
                            selected = stage == current,
                            enabled = enabled,
                            onClick = { onSelect(stage) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageChip(
    stage: LeadStage,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) Theme.colors.brand else Theme.colors.card
    val fg = if (selected) Theme.colors.paper else Theme.colors.ink3
    val borderColor = if (selected) Theme.colors.brand else Theme.colors.border

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.6f)
            .clip(ChipShape)
            .background(bg)
            .border(width = 1.dp, color = borderColor, shape = ChipShape)
            .clickable(
                enabled = enabled,
                onClickLabel = "Set stage to ${stage.label}",
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = stage.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Actions ────────────────────────────────────────────────────────────────

@Composable
private fun ActionRow(
    lead: Lead,
    movingStage: Boolean,
    onOpenChat: () -> Unit,
    onMarkWon: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        // Open chat — brand primary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .weight(1f)
                .clip(ButtonShape)
                .background(Theme.colors.brand)
                .clickable(onClickLabel = "Open chat with ${lead.contactName}", onClick = onOpenChat)
                .padding(vertical = 13.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = Theme.colors.paper,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "Open chat",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Theme.colors.paper,
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Mark as Won — brandSoft secondary
        val wonEnabled = !movingStage && lead.stage != LeadStage.WON
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .alpha(if (wonEnabled) 1f else 0.6f)
                .clip(ButtonShape)
                .background(Theme.colors.brandSoft)
                .border(
                    width = 1.dp,
                    color = Theme.colors.brand.copy(alpha = .25f),
                    shape = ButtonShape,
                )
                .clickable(
                    enabled = wonEnabled,
                    onClickLabel = "Mark ${lead.contactName} as won",
                    onClick = onMarkWon,
                )
                .padding(vertical = 13.dp),
        ) {
            Text(
                text = "Mark as Won",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Theme.colors.brand,
            )
        }
    }
}

// ─── Notes ──────────────────────────────────────────────────────────────────

@Composable
private fun NotesCard(notes: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape16)
            .background(Theme.colors.card)
            .border(width = 1.dp, color = Theme.colors.border, shape = CardShape16)
            .padding(16.dp),
    ) {
        SectionLabel(text = "NOTES", bottomGap = 10.dp)
        Text(
            text = notes,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            color = Theme.colors.ink2,
        )
    }
}

// ─── Activity timeline ──────────────────────────────────────────────────────

private data class ActivityEntry(val dot: Color, val title: String, val meta: String)

@Composable
private fun ActivityCard(lead: Lead) {
    // Real fields only: the domain model has no activity log, so the timeline
    // is derived from stage + updatedAt (latest) and source + createdAt (first).
    val colors = Theme.colors
    val entries = buildList {
        add(
            ActivityEntry(
                dot = colors.brand,
                title = "Stage set to ${lead.stage.label}",
                meta = relativeTime(lead.updatedAt) +
                    (lead.ownerName?.let { " · $it" } ?: ""),
            )
        )
        add(
            ActivityEntry(
                dot = colors.accent,
                title = "Lead created",
                meta = "${createdDateFormat.format(lead.createdAt)} · ${lead.source}",
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape16)
            .background(colors.card)
            .border(width = 1.dp, color = colors.border, shape = CardShape16)
            .padding(16.dp),
    ) {
        SectionLabel(text = "ACTIVITY", bottomGap = 12.dp)
        entries.forEachIndexed { index, entry ->
            TimelineRow(entry = entry, isLast = index == entries.lastIndex)
        }
    }
}

@Composable
private fun TimelineRow(entry: ActivityEntry, isLast: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(IntrinsicSize.Min),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(entry.dot),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(2.dp)
                        .weight(1f)
                        .background(Theme.colors.border2),
                )
            }
        }
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Text(
                text = entry.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Theme.colors.ink,
            )
            Text(
                text = entry.meta,
                fontSize = 11.sp,
                color = Theme.colors.muted2,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}

// ─── Shared ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, bottomGap: Dp = 8.dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
        fontWeight = FontWeight.SemiBold,
        color = Theme.colors.muted,
        modifier = Modifier.padding(bottom = bottomGap),
    )
}
