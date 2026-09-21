package com.websbaba.nitigrow.presentation.feature.leads.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiStateView
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.leads.components.LeadStagePill
import com.websbaba.nitigrow.presentation.feature.leads.components.PipelineStages
import com.websbaba.nitigrow.presentation.feature.leads.components.StepState
import com.websbaba.nitigrow.presentation.feature.leads.components.formatInr
import com.websbaba.nitigrow.presentation.feature.leads.components.relativeTime
import com.websbaba.nitigrow.presentation.feature.leads.components.stageTone
import com.websbaba.nitigrow.presentation.feature.leads.components.stepState
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// LeadDetailScreen — single lead.
//   ◀ Lead detail
//   avatar · name · "phone · source" · stage pill
//   ┌ Deal value ₹42,500                              Owner Anita ┐
//   │ STAGE  ●──●──●──④──⑤   (tap a step to move the lead)        │
//   [Open chat] [Mark as Won]        Mark as lost
//   Notes (when present) · Activity timeline (from real fields only)
// ─────────────────────────────────────────────────────────────────────────────

private val createdDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale("en", "IN")).withZone(ZoneId.systemDefault())

@Composable
fun LeadDetailScreen(
    onBack: () -> Unit,
    onOpenChat: (contactId: String) -> Unit = {},
    viewModel: LeadDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors
    val lead = state.lead

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
            Text(text = "Lead detail", style = NitiType.title, color = colors.onSurface)
        }

        when {
            lead != null -> LeadDetailContent(
                lead = lead,
                error = state.error,
                movingStage = state.movingStage,
                onMoveStage = viewModel::onMoveStage,
                onOpenChat = { onOpenChat(lead.contactId) },
            )
            state.isRefreshing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary, trackColor = colors.primaryTone.container)
            }
            else -> NitiStateView(
                icon = NitiIcons.Leads,
                tone = colors.tertiaryTone,
                title = "Lead not available",
                body = state.error ?: "This lead is no longer available."
            )
        }
    }
}

@Composable
private fun LeadDetailContent(
    lead: Lead,
    error: String?,
    movingStage: Boolean,
    onMoveStage: (LeadStage) -> Unit,
    onOpenChat: () -> Unit,
) {
    val colors = Niti.colors
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Identity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
        ) {
            Avatar(name = lead.contactName, url = null, sizeDp = 64)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lead.contactName,
                    style = NitiType.title.copy(fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.5).sp),
                    color = colors.onSurface
                )
                Text(
                    text = listOf(lead.contactPhone, lead.source).filter { it.isNotBlank() }.joinToString(" · "),
                    style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurfaceVariant
                )
                LeadStagePill(lead.stage, modifier = Modifier.padding(top = 8.dp))
            }
        }

        // Deal value + stepper
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surfaceLow)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Deal value", style = NitiType.label, color = colors.onSurfaceVariant)
                    Text(text = formatInr(lead.valueInr), style = NitiType.numberLg, color = colors.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Owner", style = NitiType.label, color = colors.onSurfaceVariant)
                    Text(
                        text = lead.ownerName?.takeIf { it.isNotBlank() } ?: "Unassigned",
                        style = NitiType.bodyStrong.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface
                    )
                }
            }
            Text(
                text = "Stage",
                style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                color = colors.onSurfaceVariant
            )
            StageStepper(current = lead.stage, enabled = !movingStage, onSelect = onMoveStage)
        }

        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            ActionButton(
                label = "Open chat",
                icon = NitiIcons.Chat,
                container = colors.primaryTone.container,
                content = colors.primaryTone.onContainer,
                enabled = true,
                onClick = onOpenChat,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                label = "Mark as Won",
                icon = NitiIcons.Check,
                container = colors.primary,
                content = colors.onPrimary,
                enabled = !movingStage && lead.stage != LeadStage.WON,
                onClick = { onMoveStage(LeadStage.WON) },
                modifier = Modifier.weight(1f)
            )
        }
        if (lead.stage != LeadStage.LOST && lead.stage != LeadStage.WON) {
            NitiTextButton(
                text = "Mark as lost",
                onClick = { onMoveStage(LeadStage.LOST) },
                color = colors.error,
                enabled = !movingStage,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        error?.let { msg ->
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

        if (!lead.notes.isNullOrBlank()) {
            DetailCard(title = "Notes") {
                Text(
                    text = lead.notes,
                    style = NitiType.bodyCompact.copy(lineHeight = 21.sp),
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        ActivityCard(lead)
        Box(Modifier.height(24.dp))
    }
}

// ── Stage stepper ────────────────────────────────────────────────────────────

/** Five connected steps; done steps are checked, the current one is highlighted. Tap to move. */
@Composable
private fun StageStepper(current: LeadStage, enabled: Boolean, onSelect: (LeadStage) -> Unit) {
    val colors = Niti.colors
    Box(modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.6f)) {
        // Connector behind the circles, running between the first and last centres.
        Box(
            Modifier
                .padding(top = 23.dp, start = 36.dp, end = 36.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(colors.outlineVariant)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            PipelineStages.forEach { step ->
                val state = stepState(current, step)
                val tone = stageTone(step, colors)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .selectable(
                            selected = state == StepState.CURRENT,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelect(step) }
                        )
                        .padding(top = 8.dp)
                ) {
                    val fill = when (state) {
                        StepState.DONE -> colors.primary
                        StepState.CURRENT -> tone.container
                        StepState.UPCOMING -> colors.surfaceHigh
                    }
                    val ink = when (state) {
                        StepState.DONE -> colors.onPrimary
                        StepState.CURRENT -> tone.onContainer
                        StepState.UPCOMING -> colors.onSurfaceVariant
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (state == StepState.CURRENT)
                                    Modifier.drawBehind { drawCircle(fill, radius = 20.dp.toPx()) }
                                else Modifier
                            )
                            .background(fill, CircleShape)
                    ) {
                        if (state == StepState.DONE) {
                            Icon(NitiIcons.Check, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                text = (PipelineStages.indexOf(step) + 1).toString(),
                                style = NitiType.label.copy(fontWeight = FontWeight.Bold),
                                color = ink
                            )
                        }
                    }
                    Text(
                        text = step.label,
                        style = NitiType.caption.copy(
                            fontSize = 11.sp,
                            fontWeight = if (state == StepState.CURRENT) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (state == StepState.CURRENT) colors.onSurface else colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ── Actions ──────────────────────────────────────────────────────────────────

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(container)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = NitiType.body.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
            color = content,
            maxLines = 1
        )
    }
}

// ── Cards ────────────────────────────────────────────────────────────────────

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Niti.colors.surfaceLow)
            .padding(18.dp)
    ) {
        Text(text = title, style = NitiType.titleUi, color = Niti.colors.onSurface)
        content()
    }
}

private data class ActivityEntry(val title: String, val meta: String)

/** Timeline built only from real fields: latest stage change, then creation. */
private fun activityFor(lead: Lead): List<ActivityEntry> = listOf(
    ActivityEntry(
        title = "Stage set to ${lead.stage.label}",
        meta = relativeTime(lead.updatedAt) + (lead.ownerName?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")
    ),
    ActivityEntry(
        title = "Lead created",
        meta = createdDateFormat.format(lead.createdAt) + (lead.source.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")
    )
)

@Composable
private fun ActivityCard(lead: Lead) {
    val entries = activityFor(lead)
    DetailCard(title = "Activity") {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            entries.forEachIndexed { i, entry -> TimelineRow(entry, isLast = i == entries.lastIndex) }
        }
    }
}

@Composable
private fun TimelineRow(entry: ActivityEntry, isLast: Boolean) {
    val colors = Niti.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
            Box(Modifier.padding(top = 5.dp).size(12.dp).background(colors.primary, CircleShape))
            if (!isLast) {
                Box(Modifier.padding(top = 2.dp).width(2.dp).weight(1f).background(colors.outlineVariant))
            }
        }
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Text(text = entry.title, style = NitiType.bodyCompact.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            Text(text = entry.meta, style = NitiType.caption.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant)
        }
    }
}
