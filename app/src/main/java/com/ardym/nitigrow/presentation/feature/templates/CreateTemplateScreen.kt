package com.ardym.nitigrow.presentation.feature.templates

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.ui.theme.BubbleInShape
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

private val ScreenPadding: Dp = 16.dp
private val SectionGap: Dp = 16.dp
private val FieldGap: Dp = 12.dp
private val PreviewBubblePadding: Dp = 14.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateScreen(
    onBack: () -> Unit,
    viewModel: CreateTemplateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // One-shot Toast on submission success — observed from VM's SharedFlow.
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    CreateTemplateScreenContent(
        state = state,
        onName = viewModel::setName,
        onLanguage = viewModel::setLanguage,
        onCategory = viewModel::setCategory,
        onBody = viewModel::setBody,
        onNext = viewModel::next,
        onBackStep = viewModel::back,
        onSubmit = viewModel::submit,
        onClose = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTemplateScreenContent(
    state: CreateTemplateUiState,
    onName: (String) -> Unit,
    onLanguage: (TemplateLanguage) -> Unit,
    onCategory: (TemplateCategory) -> Unit,
    onBody: (String) -> Unit,
    onNext: () -> Unit,
    onBackStep: () -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New template", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Theme.colors.brand,
                    titleContentColor = Theme.colors.paper,
                    navigationIconContentColor = Theme.colors.paper,
                ),
            )
        },
        containerColor = Theme.colors.paper,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            StepProgress(step = state.step)

            Box(modifier = Modifier.weight(1f)) {
                when (state.step) {
                    CreateStep.DETAILS -> DetailsStep(
                        state = state,
                        onName = onName,
                        onLanguage = onLanguage,
                        onCategory = onCategory,
                    )
                    CreateStep.BODY -> BodyStep(
                        state = state,
                        onBody = onBody,
                    )
                    CreateStep.PREVIEW -> PreviewStep(state = state)
                }
            }

            StepButtons(
                state = state,
                onNext = onNext,
                onBackStep = onBackStep,
                onSubmit = onSubmit,
            )
        }
    }
}

@Composable
private fun StepProgress(step: CreateStep) {
    val fraction = when (step) {
        CreateStep.DETAILS -> 1f / 3f
        CreateStep.BODY -> 2f / 3f
        CreateStep.PREVIEW -> 1f
    }
    Column(modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 12.dp)) {
        Text(
            text = "Step ${stepIndex(step)} of 3 — ${stepLabel(step)}",
            style = MaterialTheme.typography.labelMedium,
            color = Theme.colors.muted,
        )
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = Theme.colors.brand,
            trackColor = Theme.colors.paper3,
        )
    }
}

private fun stepIndex(step: CreateStep): Int = when (step) {
    CreateStep.DETAILS -> 1
    CreateStep.BODY -> 2
    CreateStep.PREVIEW -> 3
}

private fun stepLabel(step: CreateStep): String = when (step) {
    CreateStep.DETAILS -> "Details"
    CreateStep.BODY -> "Body"
    CreateStep.PREVIEW -> "Preview"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsStep(
    state: CreateTemplateUiState,
    onName: (String) -> Unit,
    onLanguage: (TemplateLanguage) -> Unit,
    onCategory: (TemplateCategory) -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(FieldGap),
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = onName,
            label = { Text("Template name") },
            supportingText = { Text("snake_case, 3-50 chars, no spaces") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LanguageDropdown(value = state.language, onChange = onLanguage)

        Column {
            Text(
                "Category",
                style = MaterialTheme.typography.labelMedium,
                color = Theme.colors.ink2,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val items = listOf(
                    TemplateCategory.MARKETING to "Marketing",
                    TemplateCategory.UTILITY to "Utility",
                    TemplateCategory.AUTHENTICATION to "Authentication",
                )
                items.forEach { (cat, label) ->
                    FilterChip(
                        selected = state.category == cat,
                        onClick = { onCategory(cat) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Theme.colors.brandSoft,
                            selectedLabelColor = Theme.colors.brand,
                        ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    value: TemplateLanguage,
    onChange: (TemplateLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = "${value.label} — ${displayName(value)}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TemplateLanguage.values().forEach { lang ->
                DropdownMenuItem(
                    text = { Text("${lang.label} — ${displayName(lang)}") },
                    onClick = {
                        onChange(lang)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun displayName(lang: TemplateLanguage): String = when (lang) {
    TemplateLanguage.EN -> "English"
    TemplateLanguage.HI -> "हिन्दी"
    TemplateLanguage.MR -> "मराठी"
}

@Composable
private fun BodyStep(
    state: CreateTemplateUiState,
    onBody: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(FieldGap),
    ) {
        Text(
            "Use {{1}}, {{2}} … for variables Meta will fill at send time.",
            style = MaterialTheme.typography.bodySmall,
            color = Theme.colors.muted,
        )
        OutlinedTextField(
            value = state.body,
            onValueChange = onBody,
            label = { Text("Body") },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                "${state.body.length} / $TEMPLATE_BODY_MAX_LENGTH",
                style = MaterialTheme.typography.labelSmall,
                color = if (state.body.length >= TEMPLATE_BODY_MAX_LENGTH) Theme.colors.danger else Theme.colors.muted,
            )
        }
    }
}

@Composable
private fun PreviewStep(state: CreateTemplateUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        Text("Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TemplatePreviewBubble(body = state.body.ifBlank { "Your template body will appear here." })
        Text(
            "Name: ${state.name.ifBlank { "(unnamed)" }}  ·  ${state.language.label}  ·  ${state.category.name.lowercase().replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodySmall,
            color = Theme.colors.muted,
        )
    }
}

@Composable
private fun TemplatePreviewBubble(body: String) {
    Surface(
        color = Theme.colors.bubbleIn,
        contentColor = Theme.colors.bubbleInInk,
        shape = BubbleInShape,
        border = BorderStroke(1.dp, Theme.colors.bubbleInBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(PreviewBubblePadding)) {
            Text(
                "WhatsApp message",
                style = MaterialTheme.typography.labelSmall,
                color = Theme.colors.muted,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = Theme.colors.bubbleInInk,
            )
        }
    }
}

@Composable
private fun StepButtons(
    state: CreateTemplateUiState,
    onNext: () -> Unit,
    onBackStep: () -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.step != CreateStep.DETAILS) {
            TextButton(onClick = onBackStep) { Text("Back") }
        }
        if (state.step != CreateStep.PREVIEW) {
            val enabled = when (state.step) {
                CreateStep.DETAILS -> state.canAdvanceFromDetails
                CreateStep.BODY -> state.canAdvanceFromBody
                CreateStep.PREVIEW -> false
            }
            FilledTonalButton(onClick = onNext, enabled = enabled) {
                Text("Next")
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        } else {
            // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
            FilledTonalButton(onClick = onSubmit, enabled = state.canSubmit) {
                Text(if (state.isSubmitting) "Submitting…" else "Submit for approval")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTemplateScreenPreview() {
    NitiGrowTheme {
        CreateTemplateScreenContent(
            state = CreateTemplateUiState(name = "diwali_offer", body = "Hi {{1}}, get 25% off."),
            onName = {},
            onLanguage = {},
            onCategory = {},
            onBody = {},
            onNext = {},
            onBackStep = {},
            onSubmit = {},
            onClose = {},
        )
    }
}
