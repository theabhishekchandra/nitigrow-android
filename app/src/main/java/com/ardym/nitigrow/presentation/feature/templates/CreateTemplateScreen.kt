package com.ardym.nitigrow.presentation.feature.templates

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.ui.theme.BubbleInShape
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

private val ScreenPadding: Dp = 18.dp
private val SectionGap: Dp = 16.dp
private val FieldGap: Dp = 12.dp
private val PreviewBubblePadding: Dp = 14.dp
private val InputRadius: Dp = 12.dp
private val CtaRadius: Dp = 14.dp

private val InputShape = RoundedCornerShape(InputRadius)
private val CtaShape = RoundedCornerShape(CtaRadius)
private val PillShape = RoundedCornerShape(999.dp)

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
    Scaffold(containerColor = Theme.colors.paper) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CreateTemplateHeader(onClose = onClose)

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
private fun CreateTemplateHeader(onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 14.dp, top = 12.dp, bottom = 4.dp),
    ) {
        IconButton(onClick = onClose) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Theme.colors.ink,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "New template",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 21.sp,
                color = Theme.colors.ink,
            )
            Text(
                text = "Goes to Meta for review — usually under 24 hours.",
                style = MaterialTheme.typography.bodySmall,
                color = Theme.colors.muted,
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
    Column(modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 10.dp)) {
        Text(
            text = "STEP ${stepIndex(step)} OF 3 — ${stepLabel(step).uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = Theme.colors.muted,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Theme.colors.paper2),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Theme.colors.brand),
            )
        }
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
        DesignTextField(
            label = "Template name",
            value = state.name,
            onValueChange = onName,
            helper = "snake_case, 3-50 chars, no spaces",
            singleLine = true,
            monospace = true,
        )

        LanguagePicker(value = state.language, onChange = onLanguage)

        Column {
            FieldLabel("Category")
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                val items = listOf(
                    TemplateCategory.MARKETING to "Marketing",
                    TemplateCategory.UTILITY to "Utility",
                    TemplateCategory.AUTHENTICATION to "Authentication",
                )
                items.forEach { (cat, label) ->
                    SelectorPill(
                        label = label,
                        selected = state.category == cat,
                        onClick = { onCategory(cat) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguagePicker(
    value: TemplateLanguage,
    onChange: (TemplateLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        FieldLabel("Language")
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(InputShape)
                    .background(Theme.colors.card)
                    .border(1.dp, Theme.colors.border, InputShape)
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
            ) {
                Text(
                    text = "${value.label} — ${displayName(value)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Theme.colors.ink,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "Choose language",
                    tint = Theme.colors.muted,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
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
        DesignTextField(
            label = "Body",
            value = state.body,
            onValueChange = onBody,
            minHeight = 180.dp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                "${state.body.length} / $TEMPLATE_BODY_MAX_LENGTH",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 0.2.sp,
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
        Text(
            "Preview",
            style = MaterialTheme.typography.headlineSmall,
            color = Theme.colors.ink,
        )
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
                "WHATSAPP MESSAGE",
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.step != CreateStep.DETAILS) {
            OutlinedButton(
                onClick = onBackStep,
                shape = CtaShape,
                border = BorderStroke(1.dp, Theme.colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Theme.colors.card,
                    contentColor = Theme.colors.ink,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text("Back", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        val isPreview = state.step == CreateStep.PREVIEW
        val enabled = when (state.step) {
            CreateStep.DETAILS -> state.canAdvanceFromDetails
            CreateStep.BODY -> state.canAdvanceFromBody
            CreateStep.PREVIEW -> state.canSubmit
        }
        Button(
            onClick = if (isPreview) onSubmit else onNext,
            enabled = enabled,
            shape = CtaShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Theme.colors.brand,
                contentColor = Theme.colors.paper,
                disabledContainerColor = Theme.colors.paper3,
                disabledContentColor = Theme.colors.muted,
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.weight(2f),
        ) {
            Text(
                text = when {
                    !isPreview -> "Next"
                    state.isSubmitting -> "Submitting…"
                    else -> "Submit for review"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─── Design-language form primitives ─────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Theme.colors.ink3,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

/**
 * Input styled to the design language: 12sp w600 ink3 label above a card-surface
 * field with 1dp border and 12dp radius. `monospace` is used for the template
 * name so authors see the exact snake_case Meta will receive.
 */
@Composable
private fun DesignTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    helper: String? = null,
    singleLine: Boolean = false,
    monospace: Boolean = false,
    minHeight: Dp = Dp.Unspecified,
) {
    Column(modifier = modifier) {
        FieldLabel(label)
        val textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Theme.colors.ink,
            fontFamily = if (monospace) FontFamily.Monospace else MaterialTheme.typography.bodyLarge.fontFamily,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            cursorBrush = SolidColor(Theme.colors.brand),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.TopStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = minHeight)
                        .clip(InputShape)
                        .background(Theme.colors.card)
                        .border(1.dp, Theme.colors.border, InputShape)
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                ) {
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        helper?.let {
            Spacer(Modifier.height(5.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.5.sp,
                color = Theme.colors.muted,
            )
        }
    }
}

@Composable
private fun SelectorPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Theme.colors.paper else Theme.colors.ink3,
        modifier = Modifier
            .clip(PillShape)
            .background(if (selected) Theme.colors.brand else Theme.colors.paper2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
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
