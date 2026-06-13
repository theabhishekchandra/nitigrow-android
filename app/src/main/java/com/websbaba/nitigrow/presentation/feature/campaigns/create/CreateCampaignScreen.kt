package com.websbaba.nitigrow.presentation.feature.campaigns.create

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// CreateCampaignScreen — 4-step "New broadcast" wizard.
//
//   ◀  STEP 1 OF 4 · AUDIENCE          ── caps micro-label
//      New broadcast                    ── Fraunces title
//   ▰▰▱▱▱▱▱▱  4dp progress (25/50/75/100%)
//
//   Step 1 Audience  — checkbox segment rows + estimated-reach banner
//   Step 2 Template  — broadcast name + selectable approved-template cards
//   Step 3 Schedule  — Send now / Schedule later radio cards + tip banner
//   Step 4 Review    — summary card + WhatsApp-style message preview
//   Footer           — full-width brand CTA
//   Queued           — pulsing check + "Broadcast queued" success state
// ─────────────────────────────────────────────────────────────────────────────

private val nf: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))
private val scheduleFormat =
    DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault())

private val OptionShape = RoundedCornerShape(14.dp)
private val BannerShape = RoundedCornerShape(12.dp)

@Composable
fun CreateCampaignScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    onViewReport: (String) -> Unit = {},
    viewModel: CreateCampaignViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Theme.colors

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is CreateCampaignEffect.ShowError -> Unit // surfaced via state.error banner
            }
        }
    }

    Scaffold(containerColor = colors.paper) { padding ->
        if (state.queued) {
            QueuedSuccess(
                state = state,
                onViewReport = onViewReport,
                onDone = onCreated,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                WizardHeader(
                    step = state.step,
                    onBack = if (state.step == WizardStep.AUDIENCE) onBack else viewModel::back
                )
                WizardProgress(step = state.step)
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (state.step) {
                        WizardStep.AUDIENCE -> StepAudience(state, viewModel)
                        WizardStep.TEMPLATE -> StepTemplate(state, viewModel)
                        WizardStep.SCHEDULE -> StepSchedule(state, viewModel)
                        WizardStep.REVIEW -> StepReview(state)
                    }
                }
                state.error?.let {
                    ErrorBanner(message = it, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
                }
                WizardFooter(
                    state = state,
                    onNext = {
                        if (state.step == WizardStep.REVIEW) viewModel.submit() else viewModel.next()
                    }
                )
            }
        }
    }
}

// ── Chrome ───────────────────────────────────────────────────────────────────

@Composable
private fun WizardHeader(step: WizardStep, onBack: () -> Unit) {
    val colors = Theme.colors
    val stepLabel = when (step) {
        WizardStep.AUDIENCE -> "STEP 1 OF 4 · AUDIENCE"
        WizardStep.TEMPLATE -> "STEP 2 OF 4 · TEMPLATE"
        WizardStep.SCHEDULE -> "STEP 3 OF 4 · SCHEDULE"
        WizardStep.REVIEW -> "STEP 4 OF 4 · REVIEW"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 14.dp, top = 12.dp, bottom = 8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.ink,
                modifier = Modifier.size(21.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stepLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = colors.muted
            )
            Text(
                "New broadcast",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 19.sp),
                color = colors.ink
            )
        }
    }
}

@Composable
private fun WizardProgress(step: WizardStep) {
    val colors = Theme.colors
    val fraction by animateFloatAsState(
        targetValue = (step.ordinal + 1) / 4f,
        label = "wizard-progress"
    )
    Box(
        modifier = Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.paper3)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.brand)
        )
    }
}

@Composable
private fun WizardFooter(state: CreateCampaignUiState, onNext: () -> Unit) {
    val colors = Theme.colors
    val label = if (state.step == WizardStep.REVIEW) {
        if (state.sendNow) "Send to ${nf.format(state.audienceEstimate ?: 0)} contacts"
        else "Schedule broadcast"
    } else "Continue"

    Column(modifier = Modifier.fillMaxWidth().background(colors.paper)) {
        HorizontalDivider(color = colors.border2)
        Button(
            onClick = onNext,
            enabled = state.canNext && !state.isSubmitting,
            shape = OptionShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.brand,
                contentColor = if (colors.isLight) colors.paper else colors.brandInk
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 16.dp)
                .height(52.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = if (colors.isLight) colors.paper else colors.brandInk
                )
            } else {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HelperText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = Theme.colors.ink3,
        modifier = Modifier.padding(bottom = 3.dp)
    )
}

// ── Step 1 · Audience ────────────────────────────────────────────────────────

@Composable
private fun StepAudience(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    val colors = Theme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item {
            HelperText("Choose who receives this broadcast. Segments update live from your CRM tags.")
        }
        if (state.availableTags.isEmpty()) {
            item {
                Text(
                    "No tags found. Add tags to contacts first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted
                )
            }
        }
        items(items = state.availableTags, key = { it }) { tag ->
            SegmentRow(
                name = tag,
                count = state.tagCounts[tag],
                selected = tag in state.selectedTags,
                onToggle = { vm.toggleTag(tag) }
            )
        }
        if (state.selectedTags.isNotEmpty()) {
            item {
                ReachBanner(
                    estimate = state.audienceEstimate,
                    isEstimating = state.isEstimating,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun SegmentRow(name: String, count: Int?, selected: Boolean, onToggle: () -> Unit) {
    val colors = Theme.colors
    val boxShape = RoundedCornerShape(7.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(OptionShape)
            .background(colors.card)
            .border(1.dp, colors.border, OptionShape)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(boxShape)
                .then(
                    if (selected) Modifier.background(colors.brand)
                    else Modifier.border(2.dp, colors.muted3, boxShape)
                )
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (colors.isLight) colors.paper else colors.brandInk,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        Text(
            name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        count?.let {
            Text(
                nf.format(it),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun ReachBanner(estimate: Int?, isEstimating: Boolean, modifier: Modifier = Modifier) {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(BannerShape)
            .background(colors.brandSoft)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Icon(
            Icons.Filled.Groups,
            contentDescription = null,
            tint = colors.brand,
            modifier = Modifier.size(16.dp)
        )
        Text(
            buildAnnotatedString {
                append("Estimated reach: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(
                        if (isEstimating || estimate == null) "calculating…"
                        else "${nf.format(estimate)} contacts"
                    )
                }
                append(" (duplicates removed)")
            },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = colors.brandInk
        )
    }
}

// ── Step 2 · Template ────────────────────────────────────────────────────────

@Composable
private fun StepTemplate(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    val colors = Theme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item {
            HelperText("Only Meta-approved templates can be broadcast. Manage them under Templates.")
        }
        item {
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::onName,
                label = { Text("Broadcast name") },
                singleLine = true,
                shape = OptionShape,
                modifier = Modifier.fillMaxWidth().padding(bottom = 3.dp)
            )
        }
        if (state.templates.isEmpty()) {
            item {
                Text(
                    "No approved templates. Submit + get approval first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted
                )
            }
        }
        items(items = state.templates, key = { it.id }) { tpl ->
            TemplateCard(
                template = tpl,
                selected = state.selectedTemplateId == tpl.id,
                onClick = { vm.pickTemplate(tpl.id) }
            )
        }
    }
}

@Composable
private fun TemplateCard(template: Template, selected: Boolean, onClick: () -> Unit) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OptionShape)
            .background(if (selected) colors.brandSoft else colors.card)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) colors.brand else colors.border,
                shape = OptionShape
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    template.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    template.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.4.sp),
                    fontWeight = FontWeight.Bold,
                    color = colors.muted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.paper2)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            if (selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = colors.brand,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            template.body,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = colors.ink3,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Step 3 · Schedule ────────────────────────────────────────────────────────

@Composable
private fun StepSchedule(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        HelperText("When should this broadcast go out?")
        ScheduleOption(
            title = "Send now",
            subtitle = "Starts immediately, throttled to your tier limit",
            selected = state.sendNow,
            onClick = { vm.setSendNow(true) }
        )
        ScheduleOption(
            title = "Schedule for later",
            subtitle = state.scheduledAt?.let { "${scheduleFormat.format(it)} — tap a quick pick to change" }
                ?: "Pick a send time below",
            selected = !state.sendNow,
            onClick = { vm.setSendNow(false) }
        )
        if (!state.sendNow) {
            Text(
                "QUICK PICK",
                style = MaterialTheme.typography.labelSmall,
                color = colors.muted,
                modifier = Modifier.padding(top = 5.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "+1 hour" to 3_600L,
                    "+3 hours" to 3 * 3_600L,
                    "+24 hours" to 86_400L
                ).forEach { (label, secs) ->
                    QuickPickChip(label) { vm.setScheduledAt(Instant.now().plusSeconds(secs)) }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
                .clip(BannerShape)
                .background(colors.turmericSoft)
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Icon(
                Icons.Filled.Schedule,
                contentDescription = null,
                tint = colors.turmericInk,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Morning sends (9–11 AM) get the best read rates for your audience.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.turmericInk
            )
        }
    }
}

@Composable
private fun ScheduleOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(OptionShape)
            .background(if (selected) colors.brandSoft else colors.card)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) colors.brand else colors.border,
                shape = OptionShape
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 15.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .border(2.dp, colors.brand, CircleShape)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.brand)
                )
            }
        }
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun QuickPickChip(label: String, onClick: () -> Unit) {
    val colors = Theme.colors
    val shape = RoundedCornerShape(999.dp)
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = colors.ink2,
        modifier = Modifier
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

// ── Step 4 · Review ──────────────────────────────────────────────────────────

@Composable
private fun StepReview(state: CreateCampaignUiState) {
    val colors = Theme.colors
    val summaryShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(summaryShape)
                .background(colors.card)
                .border(1.dp, colors.border, summaryShape)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            ReviewRow("Name", state.name)
            HorizontalDivider(color = colors.border2)
            ReviewRow(
                "Audience",
                state.audienceEstimate?.let { "${nf.format(it)} contacts" } ?: "—"
            )
            HorizontalDivider(color = colors.border2)
            ReviewRow("Segments", state.selectedTags.joinToString(", ").ifBlank { "—" })
            HorizontalDivider(color = colors.border2)
            ReviewRow("Template", state.selectedTemplate?.name ?: "—", monospace = true)
            HorizontalDivider(color = colors.border2)
            ReviewRow(
                "Sending",
                if (state.sendNow) "Immediately"
                else state.scheduledAt?.let(scheduleFormat::format) ?: "—"
            )
        }
        Text(
            "MESSAGE PREVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.paper2)
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                    .background(colors.bubbleIn)
                    .border(1.dp, colors.bubbleInBorder, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    state.selectedTemplate?.body ?: "—",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 19.sp),
                    color = colors.bubbleInInk
                )
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String, monospace: Boolean = false) {
    val colors = Theme.colors
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted
        )
        Text(
            value.ifBlank { "—" },
            style = if (monospace) {
                MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = FontWeight.SemiBold,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

// ── Queued success ───────────────────────────────────────────────────────────

@Composable
private fun QueuedSuccess(
    state: CreateCampaignUiState,
    onViewReport: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    val pulse = rememberInfiniteTransition(label = "queued-pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "queued-pulse-scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .background(colors.brandSoft)
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(46.dp)
            )
        }
        Text(
            "Broadcast queued",
            style = MaterialTheme.typography.displaySmall,
            color = colors.ink,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            buildAnnotatedString {
                append("Sending to ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.ink)) {
                    append(nf.format(state.audienceEstimate ?: 0))
                }
                append(" contacts with ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(state.selectedTemplate?.name ?: "your template")
                }
                append(". You'll get live delivery updates here and on the dashboard.")
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 21.sp),
            color = colors.ink3,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
        state.createdCampaignId?.let { id ->
            Button(
                onClick = { onViewReport(id) },
                shape = OptionShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.brand,
                    contentColor = if (colors.isLight) colors.paper else colors.brandInk
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
                    .height(52.dp)
            ) {
                Text(
                    "View live report",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(OptionShape)
                .background(colors.card)
                .border(1.dp, colors.border, OptionShape)
                .clickable(onClick = onDone)
                .padding(vertical = 13.dp)
        ) {
            Text(
                "Done",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink
            )
        }
    }
}
