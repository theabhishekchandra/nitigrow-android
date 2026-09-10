package com.websbaba.nitigrow.presentation.feature.flows

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.FlowSubmission
import com.websbaba.nitigrow.domain.model.WaFlow
import com.websbaba.nitigrow.presentation.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowsScreen(
    onBack: () -> Unit,
    viewModel: FlowsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sendTarget by remember { mutableStateOf<WaFlow?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Flows") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.flows.isEmpty() -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("No flows yet", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Build a flow on the web dashboard, then send it here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
                    )
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.flows, key = { it.id }) { f ->
                        FlowCard(
                            flow = f,
                            onSend = { sendTarget = f },
                            onResponses = { viewModel.openSubmissions(f) },
                        )
                    }
                }
            }
            state.error?.let { ErrorBanner(it, Modifier.align(Alignment.BottomCenter).padding(16.dp)) }
            state.sentMessage?.let {
                Surface(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) { Text(it, Modifier.padding(12.dp)) }
            }
        }
    }

    sendTarget?.let { flow ->
        SendFlowDialog(
            flow = flow, sending = state.sending,
            onDismiss = { sendTarget = null; viewModel.clearSent() },
            onSend = { phone, msg -> viewModel.sendFlow(flow.id, phone, msg) },
        )
    }

    state.submissionsFor?.let { flow ->
        SubmissionsDialog(
            flow = flow, loading = state.loadingSubmissions, submissions = state.submissions,
            onDismiss = { viewModel.closeSubmissions() },
        )
    }
}

@Composable
private fun FlowCard(flow: WaFlow, onSend: () -> Unit, onResponses: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(flow.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(flow.status, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            if (flow.categories.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(flow.categories.joinToString(" · "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (flow.isPublished) {
                    OutlinedButton(onClick = onSend) { Text("Send") }
                }
                OutlinedButton(onClick = onResponses) { Text("Responses") }
            }
        }
    }
}

@Composable
private fun SendFlowDialog(flow: WaFlow, sending: Boolean, onDismiss: () -> Unit, onSend: (String, String?) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send \"${flow.name}\"") },
        text = {
            Column {
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Customer phone (E.164)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message, onValueChange = { message = it },
                    label = { Text("Message (optional)") }, modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(phone.trim(), message.trim().ifBlank { null }) }, enabled = !sending && phone.isNotBlank()) {
                Text(if (sending) "Sending…" else "Send")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SubmissionsDialog(flow: WaFlow, loading: Boolean, submissions: List<FlowSubmission>, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Responses · ${flow.name}") },
        text = {
            when {
                loading -> Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) { CircularProgressIndicator() }
                submissions.isEmpty() -> Text("No responses yet.")
                // A plain scrollable Column (not LazyColumn) — nesting a lazy list
                // inside a dialog's unbounded-height slot can crash on some Compose
                // versions. Submissions per flow are few, so this is fine.
                else -> Column(
                    Modifier
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    submissions.forEach { s -> SubmissionRow(s) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun SubmissionRow(s: FlowSubmission) {
    Column(Modifier.fillMaxWidth()) {
        Text(s.contactName ?: s.contactPhone ?: "Unknown", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        s.fields.forEach { (k, v) ->
            Text("$k: $v", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
