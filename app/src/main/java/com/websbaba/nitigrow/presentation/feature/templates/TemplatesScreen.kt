package com.websbaba.nitigrow.presentation.feature.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.feature.templates.components.TemplateCard
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.Theme
import java.time.Instant

private val ListHorizontalPadding: Dp = 18.dp
private val CardSpacing: Dp = 10.dp
private val ChipSpacing: Dp = 7.dp

private val ChipShape = RoundedCornerShape(999.dp)

@Composable
fun TemplatesScreen(
    onCreate: () -> Unit,
    onBack: () -> Unit,
    viewModel: TemplatesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TemplatesScreenContent(
        state = state,
        onFilter = viewModel::setFilter,
        onCreate = onCreate,
        onBack = onBack,
    )
}

@Composable
private fun TemplatesScreenContent(
    state: TemplatesUiState,
    onFilter: (TemplateFilter) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                containerColor = Theme.colors.brand,
                contentColor = Theme.colors.paper,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New template", fontWeight = FontWeight.SemiBold) },
            )
        },
        containerColor = Theme.colors.paper,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TemplatesHeader(
                approvedCount = state.templates.count { it.status == TemplateStatus.APPROVED },
                onBack = onBack,
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = ListHorizontalPadding,
                    end = ListHorizontalPadding,
                    top = 8.dp,
                    bottom = 120.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(CardSpacing),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    FilterChipRow(
                        selected = state.filter,
                        onSelected = onFilter,
                    )
                }

                if (state.filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "No templates in this filter yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Theme.colors.muted,
                            )
                        }
                    }
                } else {
                    items(items = state.filtered, key = { it.id }) { template ->
                        TemplateCard(template = template, onClick = { /* details — wired by parent */ })
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplatesHeader(
    approvedCount: Int,
    onBack: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Theme.colors.ink,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Message templates",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 21.sp,
                color = Theme.colors.ink,
            )
            Text(
                text = "Synced with Meta · $approvedCount approved",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.5.sp,
                color = Theme.colors.muted,
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    selected: TemplateFilter,
    onSelected: (TemplateFilter) -> Unit,
) {
    val scroll = rememberScrollState()
    val labels = remember {
        listOf(
            TemplateFilter.ALL to "All",
            TemplateFilter.APPROVED to "Approved",
            TemplateFilter.PENDING to "In review",
            TemplateFilter.REJECTED to "Rejected",
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(ChipSpacing),
    ) {
        labels.forEach { (filter, label) ->
            SelectorChip(
                label = label,
                selected = selected == filter,
                onClick = { onSelected(filter) },
            )
        }
    }
}

@Composable
private fun SelectorChip(
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
            .clip(ChipShape)
            .background(if (selected) Theme.colors.brand else Theme.colors.paper2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun TemplatesScreenPreview() {
    NitiGrowTheme {
        TemplatesScreenContent(
            state = TemplatesUiState(
                templates = listOf(
                    Template(
                        id = "t1",
                        name = "order_confirmation",
                        language = TemplateLanguage.EN,
                        category = TemplateCategory.UTILITY,
                        status = TemplateStatus.APPROVED,
                        body = "Hi {{1}}, your order {{2}} is confirmed. Total ₹{{3}}.",
                        updatedAt = Instant.now()
                    ),
                    Template(
                        id = "t2",
                        name = "diwali_offer",
                        language = TemplateLanguage.EN,
                        category = TemplateCategory.MARKETING,
                        status = TemplateStatus.PENDING,
                        body = "Celebrate Diwali with 25% off! Use DIWALI25.",
                        updatedAt = Instant.now()
                    ),
                    Template(
                        id = "t3",
                        name = "catering_quote",
                        language = TemplateLanguage.EN,
                        category = TemplateCategory.MARKETING,
                        status = TemplateStatus.REJECTED,
                        body = "Catering chahiye? Humse quote lo.",
                        rejectionReason = "Meta: promotional content not allowed in Utility category. Edit and resubmit.",
                        updatedAt = Instant.now()
                    )
                )
            ),
            onFilter = {},
            onCreate = {},
            onBack = {},
        )
    }
}
