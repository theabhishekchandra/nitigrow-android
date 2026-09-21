package com.websbaba.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.presentation.components.NitiChip
import com.websbaba.nitigrow.presentation.components.NitiPrimaryButton
import com.websbaba.nitigrow.presentation.components.NitiSectionLabel
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.presentation.feature.inbox.list.InboxFilter
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType

// Static presentation options. The Conversation model has no assignee or
// label fields yet, so these selections are remembered in InboxUiState and
// will start filtering once the domain model exposes the data.
val DefaultAssigneeOptions = listOf("Anyone", "Me", "Unassigned")

private val StatusOptions = listOf(
    "Any" to InboxFilter.ALL,
    "Unread" to InboxFilter.UNREAD,
    "Window expiring" to InboxFilter.EXPIRING,
    "Pinned" to InboxFilter.PINNED,
)

/**
 * "Filter inbox" bottom sheet: drag handle, title with Reset, chip sections and
 * an Apply button. Status maps onto the same quick filter as the chips under
 * the title, so the two stay in sync. A section is hidden when it has no
 * options to offer (e.g. no labels exist).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxFilterSheet(
    selectedAssignee: String,
    onAssigneeChange: (String) -> Unit,
    selectedLabel: String,
    onLabelChange: (String) -> Unit,
    selectedStatus: InboxFilter,
    onStatusChange: (InboxFilter) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    assigneeOptions: List<String> = DefaultAssigneeOptions,
    labelOptions: List<String> = emptyList(),
) {
    val colors = Niti.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .background(colors.outlineVariant, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Filter inbox", style = NitiType.title, color = colors.onSurface)
                NitiTextButton(text = "Reset", onClick = onReset)
            }

            if (assigneeOptions.isNotEmpty()) {
                ChipSection("Assigned to", assigneeOptions, selectedAssignee, onAssigneeChange)
            }
            if (labelOptions.isNotEmpty()) {
                ChipSection("Label", labelOptions, selectedLabel, onLabelChange)
            }
            ChipSection(
                title = "Status",
                options = StatusOptions.map { it.first },
                selected = StatusOptions.first { it.second == selectedStatus }.first,
                onSelect = { label -> onStatusChange(StatusOptions.first { it.first == label }.second) }
            )

            NitiPrimaryButton(text = "Apply filters", onClick = onApply, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NitiSectionLabel(title)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                NitiChip(label = option, selected = option == selected, onClick = { onSelect(option) })
            }
        }
    }
}
