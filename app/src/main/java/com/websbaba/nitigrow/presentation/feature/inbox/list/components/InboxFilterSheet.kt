package com.websbaba.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.ui.theme.Theme

// Static presentation options. The Conversation model has no assignee or
// label fields yet, so these selections are remembered in InboxUiState and
// will start filtering once the domain model exposes the data.
val DefaultAssigneeOptions = listOf("Anyone", "Me", "Unassigned")

/**
 * "Filter inbox" bottom sheet — paper surface, 24dp top radius, drag handle,
 * ALL-CAPS sections of selectable pill chips and a brand Apply CTA.
 * A section is hidden when it has no options to offer (e.g. no labels exist).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InboxFilterSheet(
    selectedAssignee: String,
    onAssigneeChange: (String) -> Unit,
    selectedLabel: String,
    onLabelChange: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    assigneeOptions: List<String> = DefaultAssigneeOptions,
    labelOptions: List<String> = emptyList(),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Theme.colors.paper,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { SheetDragHandle() }
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            Text(
                text = "Filter inbox",
                style = MaterialTheme.typography.headlineSmall,
                color = Theme.colors.ink
            )
            Spacer(Modifier.height(14.dp))

            if (assigneeOptions.isNotEmpty()) {
                ChipSection(
                    title = "ASSIGNED TO",
                    options = assigneeOptions,
                    selected = selectedAssignee,
                    onSelect = onAssigneeChange
                )
                Spacer(Modifier.height(16.dp))
            }

            if (labelOptions.isNotEmpty()) {
                ChipSection(
                    title = "LABEL",
                    options = labelOptions,
                    selected = selectedLabel,
                    onSelect = onLabelChange
                )
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onApply,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Theme.colors.brand,
                    contentColor = Theme.colors.paper
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Apply filters",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .width(38.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Theme.colors.muted3)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Theme.colors.muted
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            options.forEach { option ->
                SelectableChip(
                    label = option,
                    selected = option == selected,
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Theme.colors.paper else Theme.colors.ink3,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Theme.colors.brand else Theme.colors.paper2)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}
