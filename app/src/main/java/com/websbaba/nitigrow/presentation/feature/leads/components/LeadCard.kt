package com.websbaba.nitigrow.presentation.feature.leads.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import java.text.NumberFormat
import java.util.Locale

private val nf = NumberFormat.getInstance(Locale("en", "IN"))

@Composable
fun LeadCard(
    lead: Lead,
    onMovePrev: (() -> Unit)?,
    onMoveNext: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                lead.contactName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                lead.contactPhone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "₹${nf.format(lead.valueInr)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                IconButton(onClick = { onMovePrev?.invoke() }, enabled = onMovePrev != null) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Move back")
                }
                Text(
                    lead.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = { onMoveNext?.invoke() }, enabled = onMoveNext != null) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Move forward")
                }
            }
        }
    }
}

fun LeadStage.next(): LeadStage? = LeadStage.entries.let { stages ->
    val idx = stages.indexOf(this)
    if (idx < stages.lastIndex) stages[idx + 1] else null
}

fun LeadStage.previous(): LeadStage? = LeadStage.entries.let { stages ->
    val idx = stages.indexOf(this)
    if (idx > 0) stages[idx - 1] else null
}
