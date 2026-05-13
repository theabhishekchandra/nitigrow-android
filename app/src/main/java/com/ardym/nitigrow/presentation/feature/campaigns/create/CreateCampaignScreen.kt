package com.ardym.nitigrow.presentation.feature.campaigns.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.domain.model.Template
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.components.PrimaryButton
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateCampaignViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                CreateCampaignEffect.Created -> onCreated()
                is CreateCampaignEffect.ShowError -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.step) {
                            WizardStep.TEMPLATE -> "1/4 · Template"
                            WizardStep.AUDIENCE -> "2/4 · Audience"
                            WizardStep.SCHEDULE -> "3/4 · Schedule"
                            WizardStep.REVIEW -> "4/4 · Review"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = if (state.step == WizardStep.TEMPLATE) onBack else viewModel::back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.step) {
                    WizardStep.TEMPLATE -> StepTemplate(state, viewModel)
                    WizardStep.AUDIENCE -> StepAudience(state, viewModel)
                    WizardStep.SCHEDULE -> StepSchedule(state, viewModel)
                    WizardStep.REVIEW -> StepReview(state)
                }
            }
            state.error?.let {
                ErrorBanner(message = it, modifier = Modifier.padding(16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.step != WizardStep.TEMPLATE) {
                    OutlinedButton(
                        onClick = viewModel::back,
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                }
                PrimaryButton(
                    text = if (state.step == WizardStep.REVIEW) {
                        if (state.sendNow) "Send now" else "Schedule"
                    } else "Next",
                    onClick = {
                        if (state.step == WizardStep.REVIEW) viewModel.submit()
                        else viewModel.next()
                    },
                    loading = state.isSubmitting,
                    enabled = state.canNext,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
private fun StepTemplate(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.name,
            onValueChange = vm::onName,
            label = { Text("Campaign name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("Pick approved template", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (state.templates.isEmpty()) {
            Text(
                "No approved templates. Submit + get approval first.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.templates, key = { it.id }) { tpl ->
                    TemplateOption(
                        template = tpl,
                        selected = state.selectedTemplateId == tpl.id,
                        onClick = { vm.pickTemplate(tpl.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateOption(template: Template, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(template.name, fontWeight = FontWeight.Bold)
                Text(
                    "${template.category} · ${template.language}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    template.body,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepAudience(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pick tags", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (state.availableTags.isEmpty()) {
            Text(
                "No tags found. Add tags to contacts first.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items = state.availableTags) { tag ->
                FilterChip(
                    selected = tag in state.selectedTags,
                    onClick = { vm.toggleTag(tag) },
                    label = { Text(tag) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Estimated audience", style = MaterialTheme.typography.labelSmall)
                Text(
                    when {
                        state.isEstimating -> "Calculating…"
                        state.audienceEstimate == null -> "—"
                        else -> "${state.audienceEstimate} contacts"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StepSchedule(state: CreateCampaignUiState, vm: CreateCampaignViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { vm.setSendNow(true) }
        ) {
            RadioButton(selected = state.sendNow, onClick = { vm.setSendNow(true) })
            Text("Send now", modifier = Modifier.padding(start = 8.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { vm.setSendNow(false) }
        ) {
            RadioButton(selected = !state.sendNow, onClick = { vm.setSendNow(false) })
            Text("Schedule for later", modifier = Modifier.padding(start = 8.dp))
        }
        if (!state.sendNow) {
            Spacer(Modifier.height(12.dp))
            // Simple offset picker; replace with date/time pickers in 6c
            val offsets = listOf(
                "+1 hour" to 3600L,
                "+3 hours" to 3 * 3600L,
                "+24 hours" to 86_400L
            )
            offsets.forEach { (label, secs) ->
                AssistChip(
                    onClick = { vm.setScheduledAt(java.time.Instant.now().plusSeconds(secs)) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                )
            }
            state.scheduledAt?.let {
                Spacer(Modifier.height(8.dp))
                Text("Scheduled for $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StepReview(state: CreateCampaignUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ReviewRow("Name", state.name)
        ReviewRow(
            "Template",
            state.templates.firstOrNull { it.id == state.selectedTemplateId }?.name ?: "—"
        )
        ReviewRow("Tags", state.selectedTags.joinToString(", "))
        ReviewRow("Estimated audience", "${state.audienceEstimate ?: 0}")
        ReviewRow(
            "Send",
            if (state.sendNow) "Immediately" else state.scheduledAt?.toString() ?: "—"
        )
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
