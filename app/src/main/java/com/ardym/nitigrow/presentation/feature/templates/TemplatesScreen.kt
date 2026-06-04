package com.ardym.nitigrow.presentation.feature.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.presentation.feature.templates.components.TemplateCard
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme

private val ScreenPadding: Dp = 16.dp
private val ChipSpacing: Dp = 8.dp
private val ItemSpacing: Dp = 4.dp

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatesScreenContent(
    state: TemplatesUiState,
    onFilter: (TemplateFilter) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                containerColor = Theme.colors.brand,
                contentColor = Theme.colors.paper,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Create") },
            )
        },
        containerColor = Theme.colors.paper,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(ItemSpacing),
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
private fun FilterChipRow(
    selected: TemplateFilter,
    onSelected: (TemplateFilter) -> Unit,
) {
    val scroll = rememberScrollState()
    val labels = remember {
        listOf(
            TemplateFilter.ALL to "All",
            TemplateFilter.APPROVED to "Approved",
            TemplateFilter.PENDING to "Pending",
            TemplateFilter.REJECTED to "Rejected",
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = ScreenPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ChipSpacing),
    ) {
        labels.forEach { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Theme.colors.brandSoft,
                    selectedLabelColor = Theme.colors.brand,
                ),
            )
        }
    }
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
                        updatedAt = java.time.Instant.now()
                    ),
                    Template(
                        id = "t2",
                        name = "diwali_offer",
                        language = TemplateLanguage.EN,
                        category = TemplateCategory.MARKETING,
                        status = TemplateStatus.PENDING,
                        body = "Celebrate Diwali with 25% off! Use DIWALI25.",
                        updatedAt = java.time.Instant.now()
                    )
                )
            ),
            onFilter = {},
            onCreate = {},
            onBack = {},
        )
    }
}
