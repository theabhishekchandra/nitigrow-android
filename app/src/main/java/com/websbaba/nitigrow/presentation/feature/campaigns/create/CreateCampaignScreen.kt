package com.websbaba.nitigrow.presentation.feature.campaigns.create

import com.websbaba.nitigrow.core.util.TimeOfDayFormat
import com.websbaba.nitigrow.core.util.DayFormat
import com.websbaba.nitigrow.core.util.formatIndian
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiPrimaryButton
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────────────────
// CreateCampaignScreen — 4-step "New broadcast" wizard.
//
//   ◀  New broadcast
//   ▰▰▱▱  Step 1 of 4 · Audience
//
//   1 Audience  — broadcast name, segment rows, estimated-reach banner
//   2 Template  — approved-template radio cards + message preview
//   3 Schedule  — Send now / Schedule for later, date & time pickers, quick picks
//   4 Review    — summary card, info banner, message preview
//   Footer      — Back + primary CTA
//   Queued      — success state with "View live report"
// ─────────────────────────────────────────────────────────────────────────────


private val StepNames = listOf("Audience", "Template", "Schedule", "Review")

@Composable
fun CreateCampaignScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    onViewReport: (String) -> Unit = {},
    viewModel: CreateCampaignViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is CreateCampaignEffect.ShowError -> Unit // surfaced via state.error banner
            }
        }
    }

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Box(modifier = Modifier.fillMaxSize().background(colors.surface).statusBarsPadding()) {
        if (state.queued) {
            QueuedSuccess(state = state, onViewReport = onViewReport, onDone = onCreated)
        } else {
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                WizardHeader(onBack = if (state.step == WizardStep.AUDIENCE) onBack else viewModel::back)
                WizardProgress(step = state.step)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
                ) {
                    when (state.step) {
                        WizardStep.AUDIENCE -> StepAudience(state, viewModel)
                        WizardStep.TEMPLATE -> StepTemplate(state, viewModel)
                        WizardStep.SCHEDULE -> StepSchedule(state, viewModel)
                        WizardStep.REVIEW -> StepReview(state)
                    }
                    state.error?.let {
                        Text(
                            text = it,
                            style = NitiType.label,
                            color = colors.error,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.error.copy(alpha = 0.12f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
                WizardFooter(
                    state = state,
                    onBack = if (state.step == WizardStep.AUDIENCE) onBack else viewModel::back,
                    onNext = { if (state.step == WizardStep.REVIEW) viewModel.submit() else viewModel.next() }
                )
            }
        }
    }
}

// ── Chrome ───────────────────────────────────────────────────────────────────

@Composable
private fun WizardHeader(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        NitiIconButton(icon = NitiIcons.Back, contentDescription = "Back", onClick = onBack)
        Text(text = "New broadcast", style = NitiType.title, color = Niti.colors.onSurface)
    }
}

/** Four segments, filled up to the current step, with a "Step 2 of 4 · Template" caption. */
@Composable
private fun WizardProgress(step: WizardStep) {
    val colors = Niti.colors
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            WizardStep.entries.forEach { s ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (s.ordinal <= step.ordinal) colors.primary else colors.track)
                )
            }
        }
        Text(
            text = "Step ${step.ordinal + 1} of ${WizardStep.entries.size}  ·  ${StepNames[step.ordinal]}",
            style = NitiType.label.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun WizardFooter(state: CreateCampaignUiState, onBack: () -> Unit, onNext: () -> Unit) {
    val colors = Niti.colors
    val label = when {
        state.step != WizardStep.REVIEW -> "Continue"
        state.sendNow -> "Send to ${formatIndian(state.audienceEstimate ?: 0)} contacts"
        else -> "Schedule broadcast"
    }
    val enabled = state.canNext && !state.isSubmitting

    Column(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
        HorizontalDivider(color = colors.outlineVariant)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, colors.outline, RoundedCornerShape(26.dp))
                    .clickable(role = Role.Button, onClick = onBack)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Back",
                    style = NitiType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.primary
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (enabled || state.isSubmitting) colors.primary else colors.surfaceHigh)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onNext)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = colors.onPrimary
                    )
                } else {
                    Text(
                        text = label,
                        style = NitiType.body.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
                        color = if (enabled) colors.onPrimary else colors.outline
                    )
                }
            }
        }
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
    val colors = Niti.colors
    val pulse = rememberInfiniteTransition(label = "queued-pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "queued-pulse-scale"
    )
    val contacts = formatIndian(state.audienceEstimate ?: 0)
    val summary = when {
        state.sendNow -> "${state.name} is now going out to $contacts contacts."
        state.scheduledAt != null ->
            "${state.name} will go out to $contacts contacts on ${DayFormat.format(state.scheduledAt)} at ${TimeOfDayFormat.format(state.scheduledAt)}."
        else -> "${state.name} is queued for $contacts contacts."
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(112.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .background(colors.primaryTone.container, CircleShape)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp).background(colors.primary, CircleShape)
            ) {
                Icon(NitiIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(40.dp))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Broadcast queued",
                style = NitiType.display.copy(fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.8).sp),
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = summary,
                style = NitiType.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            state.createdCampaignId?.let { id ->
                NitiPrimaryButton(text = "View live report", onClick = { onViewReport(id) }, modifier = Modifier.fillMaxWidth())
            }
            NitiTextButton(text = "Done", onClick = onDone, modifier = Modifier.fillMaxWidth())
        }
    }
}
