package com.ardym.nitigrow.presentation.feature.campaigns.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.domain.model.CampaignStatus
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.dashboard.components.RateRow
import java.text.NumberFormat
import java.util.Locale

private val nf = NumberFormat.getInstance(Locale("en", "IN"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    onBack: () -> Unit,
    viewModel: CampaignDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.campaign?.name ?: "Campaign", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val c = state.campaign
        if (c == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.error?.let { ErrorBanner(message = it) } ?: Text("Loading…")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status: ${c.status.name}", style = MaterialTheme.typography.titleSmall)
                    Text("Template: ${c.templateName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Tags: ${c.audienceTags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    Text("Audience: ${nf.format(c.audienceSize)}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Progress",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    val progress = if (c.audienceSize > 0)
                        c.sentCount.toFloat() / c.audienceSize else 0f
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sent ${nf.format(c.sentCount)} / ${nf.format(c.audienceSize)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (c.failedCount > 0) {
                        Text(
                            "Failed: ${nf.format(c.failedCount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Engagement",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    RateRow("Delivery rate", c.deliveryRate)
                    Spacer(Modifier.height(8.dp))
                    RateRow("Read rate", c.readRate)
                }
            }

            if (c.status == CampaignStatus.SCHEDULED || c.status == CampaignStatus.RUNNING) {
                OutlinedButton(
                    onClick = viewModel::onCancel,
                    enabled = !state.cancelling,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel campaign") }
            }
            state.error?.let { ErrorBanner(message = it) }
        }
    }
}
