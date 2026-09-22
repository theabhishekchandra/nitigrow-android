package com.websbaba.nitigrow.presentation.feature.campaigns.create

import com.websbaba.nitigrow.core.util.collapseWhitespace
import com.websbaba.nitigrow.core.util.DayTimeFormat
import com.websbaba.nitigrow.core.util.TimeOfDayFormat
import com.websbaba.nitigrow.core.util.DayFormat
import com.websbaba.nitigrow.core.util.formatIndian
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.presentation.components.NitiChip
import com.websbaba.nitigrow.presentation.components.NitiRadioDot
import com.websbaba.nitigrow.presentation.components.NitiSectionLabel
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.presentation.components.nitiTextFieldColors
import com.websbaba.nitigrow.core.ui.theme.BubbleInShape
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset


@Composable
private fun StepTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = NitiType.headline.copy(letterSpacing = (-0.6).sp),
            color = Niti.colors.onSurface
        )
        if (subtitle != null) {
            Text(text = subtitle, style = NitiType.bodyCompact, color = Niti.colors.onSurfaceVariant)
        }
    }
}

// ── Step 1 · Name and audience ───────────────────────────────────────────────

@Composable
fun StepAudience(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    val colors = Niti.colors
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        StepTitle(
            title = "Name and audience",
            subtitle = "Choose who receives this broadcast. Segments update live from your CRM tags."
        )
        OutlinedTextField(
            value = state.name,
            onValueChange = vm::onName,
            label = { Text("Broadcast name") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = NitiType.body.copy(fontSize = 16.sp),
            colors = nitiTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NitiSectionLabel("Segments")
            if (state.availableTags.isEmpty()) {
                Text(
                    text = "No tags found. Add tags to contacts first.",
                    style = NitiType.bodyCompact,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.availableTags.forEach { tag ->
                    SegmentRow(
                        name = tag,
                        count = state.tagCounts[tag],
                        selected = tag in state.selectedTags,
                        onToggle = { vm.toggleTag(tag) }
                    )
                }
            }
        }
        if (state.selectedTags.isNotEmpty()) {
            ReachBanner(estimate = state.audienceEstimate, isEstimating = state.isEstimating)
        }
    }
}

@Composable
private fun SegmentRow(name: String, count: Int?, selected: Boolean, onToggle: () -> Unit) {
    val colors = Niti.colors
    val tone = colors.primaryTone
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) tone.container else colors.surfaceLow)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (selected) Modifier.background(colors.primary)
                    else Modifier.border(2.dp, colors.outline, RoundedCornerShape(8.dp))
                )
        ) {
            if (selected) {
                Icon(NitiIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = name,
            style = NitiType.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            color = if (selected) tone.onContainer else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (count != null) {
            Text(
                text = formatIndian(count),
                style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) tone.onContainer else colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReachBanner(estimate: Int?, isEstimating: Boolean) {
    val tone = Niti.colors.secondaryTone
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tone.container)
            .padding(14.dp)
    ) {
        Icon(NitiIcons.Contacts, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isEstimating || estimate == null) "Counting contacts…"
                else "Sending to ${formatIndian(estimate)} ${if (estimate == 1) "contact" else "contacts"}",
                style = NitiType.bodyStrong.copy(fontWeight = FontWeight.SemiBold),
                color = tone.onContainer
            )
            Text(
                text = "Overlapping contacts are counted once.",
                style = NitiType.caption.copy(fontWeight = FontWeight.Normal),
                color = tone.onContainer
            )
        }
    }
}

// ── Step 2 · Template ────────────────────────────────────────────────────────

@Composable
fun StepTemplate(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    val colors = Niti.colors
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepTitle(
            title = "Pick a template",
            subtitle = "Only Meta-approved templates can be broadcast. Manage them under Templates."
        )
        if (state.templates.isEmpty()) {
            Text(
                text = "No approved templates. Submit one for approval first.",
                style = NitiType.bodyCompact,
                color = colors.onSurfaceVariant
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.templates.forEach { tpl ->
                TemplateOption(
                    template = tpl,
                    selected = state.selectedTemplateId == tpl.id,
                    onClick = { vm.pickTemplate(tpl.id) }
                )
            }
        }
        state.selectedTemplate?.let { PreviewSection(it.body) }
    }
}

@Composable
private fun TemplateOption(template: Template, selected: Boolean, onClick: () -> Unit) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.primaryTone.container else Color.Transparent)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        NitiRadioDot(selected)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = template.name,
                    style = NitiType.bodyStrong.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) colors.primaryTone.onContainer else colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                CategoryBadge(template.category)
            }
            Text(
                // Template bodies carry blank lines; flatten them so a two-line preview
                // shows the message rather than an empty line and an ellipsis.
                text = template.body.collapseWhitespace(),
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = if (selected) colors.primaryTone.onContainer else colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoryBadge(category: String) {
    val colors = Niti.colors
    val tone: NitiTone = when (category.uppercase()) {
        "MARKETING" -> colors.secondaryTone
        "UTILITY" -> colors.primaryTone
        else -> colors.infoTone
    }
    Text(
        text = category.lowercase().replaceFirstChar { it.uppercase() },
        style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        color = tone.onContainer,
        modifier = Modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tone.container)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

/** Template body in a chat-style bubble on a tonal wallpaper. */
@Composable
private fun PreviewSection(body: String) {
    val colors = Niti.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NitiSectionLabel("Message preview")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.primaryTone.container)
                .padding(16.dp)
        ) {
            Text(
                text = body,
                style = NitiType.bodyCompact.copy(lineHeight = 21.sp),
                color = colors.onBubbleIn,
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clip(BubbleInShape)
                    .background(colors.surface)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }
}

// ── Step 3 · Schedule ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepSchedule(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    val colors = Niti.colors
    val zone = remember { ZoneId.systemDefault() }
    var pickingDate by remember { mutableStateOf(false) }
    var pickingTime by remember { mutableStateOf(false) }
    val scheduledAt = state.scheduledAt
    val quickPicks = remember(state.sendNow) { SchedulePresets.quickPicks(Instant.now(), zone) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepTitle(title = "When should this broadcast go out?")
        ScheduleOption(
            title = "Send now",
            subtitle = "Starts immediately, throttled to your tier limit",
            icon = NitiIcons.Bolt,
            selected = state.sendNow,
            onClick = { vm.setSendNow(true) }
        )
        ScheduleOption(
            title = "Schedule for later",
            subtitle = "Pick a send time below",
            icon = NitiIcons.Calendar,
            selected = !state.sendNow,
            onClick = { vm.setSendNow(false) }
        ) {
            if (scheduledAt != null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
                    PickerField("Date", DayFormat.format(scheduledAt), NitiIcons.Calendar, colors.primaryTone.container) {
                        pickingDate = true
                    }
                    PickerField("Time", TimeOfDayFormat.format(scheduledAt), null, colors.primaryTone.container) {
                        pickingTime = true
                    }
                }
            }
        }
        if (!state.sendNow && scheduledAt != null) {
            Column {
                NitiSectionLabel("Quick pick")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val matched = quickPicks.any { it.at == scheduledAt }
                    quickPicks.forEach { preset ->
                        NitiChip(
                            label = preset.label,
                            selected = preset.at == scheduledAt,
                            onClick = { vm.setScheduledAt(preset.at) }
                        )
                    }
                    if (!matched) {
                        NitiChip(label = DayTimeFormat.format(scheduledAt).replace(" · ", ", "), selected = true, onClick = {})
                    }
                }
            }
        }
    }

    if (pickingDate && scheduledAt != null) {
        ScheduleDatePicker(
            current = scheduledAt,
            zone = zone,
            onDismiss = { pickingDate = false },
            onPicked = { vm.setScheduledAt(it); pickingDate = false }
        )
    }
    if (pickingTime && scheduledAt != null) {
        ScheduleTimePicker(
            current = scheduledAt,
            zone = zone,
            onDismiss = { pickingTime = false },
            onPicked = { vm.setScheduledAt(it); pickingTime = false }
        )
    }
}

@Composable
private fun ScheduleOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    extra: (@Composable () -> Unit)? = null
) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.primaryTone.container else Color.Transparent)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NitiRadioDot(selected)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = NitiType.titleUi.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) colors.primaryTone.onContainer else colors.onSurface
                )
                Text(
                    text = subtitle,
                    style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                    color = if (selected) colors.primaryTone.onContainer else colors.onSurfaceVariant
                )
            }
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) colors.primaryTone.onContainer else colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        if (selected) extra?.invoke()
    }
}

/** Read-only outlined field that opens a picker on tap. */
@Composable
private fun PickerField(label: String, value: String, icon: ImageVector?, labelBackground: Color, onClick: () -> Unit) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(16.dp)
    Box(modifier = Modifier.fillMaxWidth().padding(top = 9.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(shape)
                .border(1.dp, colors.outline, shape)
                .clickable(role = Role.Button, onClickLabel = "Change ${label.lowercase()}", onClick = onClick)
                .padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
            Text(text = value, style = NitiType.body.copy(fontSize = 16.sp), color = colors.onSurface)
        }
        Text(
            text = label,
            style = NitiType.caption,
            color = colors.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 12.dp, y = (-9).dp)
                .background(labelBackground)
                .padding(horizontal = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDatePicker(current: Instant, zone: ZoneId, onDismiss: () -> Unit, onPicked: (Instant) -> Unit) {
    val today = remember { LocalDate.now(zone) }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = current.atZone(zone).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(today)
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            NitiTextButton(text = "OK", onClick = {
                pickerState.selectedDateMillis?.let { ms ->
                    val date = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                    onPicked(SchedulePresets.combine(date, current.atZone(zone).toLocalTime(), zone))
                } ?: onDismiss()
            })
        },
        dismissButton = { NitiTextButton(text = "Cancel", onClick = onDismiss) }
    ) { DatePicker(state = pickerState) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTimePicker(current: Instant, zone: ZoneId, onDismiss: () -> Unit, onPicked: (Instant) -> Unit) {
    val local = current.atZone(zone)
    val pickerState = rememberTimePickerState(initialHour = local.hour, initialMinute = local.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Niti.colors.surface,
        shape = RoundedCornerShape(28.dp),
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            NitiTextButton(text = "OK", onClick = {
                onPicked(SchedulePresets.combine(local.toLocalDate(), LocalTime.of(pickerState.hour, pickerState.minute), zone))
            })
        },
        dismissButton = { NitiTextButton(text = "Cancel", onClick = onDismiss) }
    )
}

// ── Step 4 · Review ──────────────────────────────────────────────────────────

@Composable
fun StepReview(state: CreateCampaignUiState) {
    val colors = Niti.colors
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepTitle(title = "Review and ${if (state.sendNow) "send" else "schedule"}")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surfaceLow)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            val rows = listOf(
                "Name" to state.name,
                "Audience" to state.selectedTags.joinToString(", "),
                "Contacts" to (state.audienceEstimate?.let { formatIndian(it) } ?: "—"),
                "Template" to (state.selectedTemplate?.name ?: "—"),
                "Send time" to if (state.sendNow) "Immediately"
                else state.scheduledAt?.let(DayTimeFormat::format) ?: "—"
            )
            rows.forEachIndexed { i, (label, value) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (i < rows.lastIndex) Modifier.bottomDivider(colors.outlineVariant) else Modifier)
                        .padding(vertical = 14.dp)
                ) {
                    Text(text = label, style = NitiType.bodyCompact, color = colors.onSurfaceVariant)
                    Text(
                        text = value.ifBlank { "—" },
                        style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        val tone = colors.secondaryTone
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(tone.container)
                .padding(14.dp)
        ) {
            Icon(NitiIcons.Info, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(18.dp))
            Text(
                text = if (state.sendNow) "Messages start going out as soon as you confirm."
                else "You can cancel a scheduled broadcast any time before it starts.",
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = tone.onContainer
            )
        }
        state.selectedTemplate?.let { PreviewSection(it.body) }
    }
}

private fun Modifier.bottomDivider(color: Color): Modifier = drawBehind {
    val y = size.height - 0.5.dp.toPx()
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
}

