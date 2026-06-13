package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// TemplatePickerSheet — bottom sheet for picking an APPROVED WhatsApp template
// to send when the 24h messaging window is closed.
// See docs/phase-3-mobile.md "Window CLOSED state" + "Send Template" picker.
//
// Layout:
//   ┌──────────────────────────────────────┐
//   │ Send a Template                      │  title
//   │ Re-open the 24h conversation window. │  subtitle
//   │ [All] [Marketing] [Utility] [Auth]   │  filter chips
//   │ ──────────────────────────────────── │
//   │ ┌──────────────────────────────────┐ │  one card per template
//   │ │ diwali_offer_2026   en  MKT      │ │
//   │ │ Namaste {{1}}! Wishing you a…    │ │
//   │ │                         [Send →] │ │
//   │ └──────────────────────────────────┘ │
//   └──────────────────────────────────────┘
//
// Filtering is purely visual: we keep all templates in memory and apply a
// predicate. APPROVED-only filtering is the caller's responsibility (or future
// enhancement) but we surface non-APPROVED templates with a muted badge.
// ─────────────────────────────────────────────────────────────────────────────

private val CategoryAll = "All"
private val CategoryMarketing = "Marketing"
private val CategoryUtility = "Utility"
private val CategoryAuthentication = "Authentication"

private val Categories = listOf(
    CategoryAll, CategoryMarketing, CategoryUtility, CategoryAuthentication
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerSheet(
    templates: List<Template>,
    onPick: (Template) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf(CategoryAll) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Theme.colors.card
    ) {
        TemplatePickerContent(
            templates = templates,
            selectedCategory = selectedCategory,
            onCategoryChange = { selectedCategory = it },
            onPick = onPick
        )
    }
}

@Composable
private fun TemplatePickerContent(
    templates: List<Template>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onPick: (Template) -> Unit
) {
    val filtered = remember(templates, selectedCategory) {
        if (selectedCategory == CategoryAll) templates
        else templates.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Send a Template",
            style = MaterialTheme.typography.titleLarge,
            color = Theme.colors.ink,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.padding(top = 4.dp))
        Text(
            text = "Re-open the conversation window by sending an approved template.",
            style = MaterialTheme.typography.bodyMedium,
            color = Theme.colors.ink3
        )

        Spacer(Modifier.padding(top = 12.dp))
        CategoryChipRow(
            selected = selectedCategory,
            onSelect = onCategoryChange
        )

        Spacer(Modifier.padding(top = 12.dp))
        if (filtered.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onSend = { onPick(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(selected: String, onSelect: (String) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Categories.forEach { cat ->
            CategoryChip(
                label = cat,
                selected = cat == selected,
                onClick = { onSelect(cat) }
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = Theme.colors
    val bg = if (selected) colors.brand else colors.paper2
    val ink = if (selected) colors.paper else colors.ink2
    val borderColor = if (selected) colors.brand else colors.border

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ink,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TemplateCard(template: Template, onSend: () -> Unit) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.paper)
            .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Header row: name + language + category badge.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            LanguagePill(language = template.language)
            CategoryBadge(category = template.category)
        }

        Spacer(Modifier.padding(top = 8.dp))

        // Body preview — collapsed to two lines, ellipsised.
        Text(
            text = template.body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (template.variableCount > 0) {
            Spacer(Modifier.padding(top = 6.dp))
            Text(
                text = "${template.variableCount} variable(s) to fill",
                style = MaterialTheme.typography.labelSmall,
                color = colors.muted
            )
        }

        Spacer(Modifier.padding(top = 10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(status = template.status)
            Spacer(Modifier.padding(start = 8.dp))
            Button(
                onClick = onSend,
                enabled = template.status.equals("APPROVED", ignoreCase = true),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.brand,
                    contentColor = colors.paper
                )
            ) {
                Text("Send", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LanguagePill(language: String) {
    val colors = Theme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.paper2)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = language,
            style = MaterialTheme.typography.labelSmall,
            color = colors.ink3,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CategoryBadge(category: String) {
    val colors = Theme.colors
    val (bg, ink) = when (category.uppercase()) {
        "MARKETING" -> colors.accentSoft to colors.accent
        "UTILITY" -> colors.brandSoft to colors.brand
        "AUTHENTICATION" -> colors.turmericSoft to colors.warning
        else -> colors.paper2 to colors.ink3
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val colors = Theme.colors
    val (bg, ink) = when (status.uppercase()) {
        "APPROVED" -> colors.brandSoft to colors.brand
        "PENDING" -> colors.turmericSoft to colors.warning
        "REJECTED" -> colors.accentSoft to colors.danger
        else -> colors.paper2 to colors.muted
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No templates in this category",
            style = MaterialTheme.typography.bodyMedium,
            color = Theme.colors.muted
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────
// ModalBottomSheet doesn't render inside @Preview reliably, so we preview the
// inner content directly. This still exercises the chip + card composables.

@Preview(name = "TemplatePicker — content (All)", showBackground = true)
@Composable
private fun PreviewTemplatePickerAll() {
    NitiGrowTheme {
        TemplatePickerContent(
            templates = previewTemplates(),
            selectedCategory = CategoryAll,
            onCategoryChange = {},
            onPick = {}
        )
    }
}

@Preview(name = "TemplatePicker — content (Marketing filter)", showBackground = true)
@Composable
private fun PreviewTemplatePickerMarketing() {
    NitiGrowTheme {
        TemplatePickerContent(
            templates = previewTemplates(),
            selectedCategory = CategoryMarketing,
            onCategoryChange = {},
            onPick = {}
        )
    }
}

@Preview(name = "TemplatePicker — empty", showBackground = true)
@Composable
private fun PreviewTemplatePickerEmpty() {
    NitiGrowTheme {
        TemplatePickerContent(
            templates = emptyList(),
            selectedCategory = CategoryAll,
            onCategoryChange = {},
            onPick = {}
        )
    }
}

private fun previewTemplates(): List<Template> = listOf(
    Template(
        id = "tpl-01",
        name = "diwali_offer_2026",
        language = "en_IN",
        category = "MARKETING",
        status = "APPROVED",
        body = "Namaste {{1}}! Wishing you a sparkling Diwali. Flat 20% off on all orders this week. Reply YES to claim.",
        variableCount = 1,
        updatedAt = Instant.now().minus(2, ChronoUnit.DAYS)
    ),
    Template(
        id = "tpl-02",
        name = "order_confirmation",
        language = "en_IN",
        category = "UTILITY",
        status = "APPROVED",
        body = "Hi {{1}}, your order #{{2}} totalling ₹{{3}} is confirmed. We'll ship it within 24 hours.",
        variableCount = 3,
        updatedAt = Instant.now().minus(7, ChronoUnit.DAYS)
    )
)
