package com.ardym.nitigrow.presentation.feature.settings.autoreply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val PageGutter = 16.dp
private val CardGutter = 16.dp
private val FieldSpacing = 12.dp
private const val WELCOME_MAX_LINES = 6
private const val AWAY_MAX_LINES = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoReplyScreen(
    onBack: () -> Unit,
    viewModel: AutoReplyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is AutoReplyEffect.Toast -> snackbar.showSnackbar(e.text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto-reply", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        AutoReplyBody(
            state = state,
            onWelcomeEnabled = viewModel::onWelcomeEnabled,
            onWelcomeMessage = viewModel::onWelcomeMessage,
            onAwayEnabled = viewModel::onAwayEnabled,
            onAwayMessage = viewModel::onAwayMessage,
            onAwayStart = viewModel::onAwayStart,
            onAwayEnd = viewModel::onAwayEnd,
            onSave = viewModel::save,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun AutoReplyBody(
    state: AutoReplyUiState,
    onWelcomeEnabled: (Boolean) -> Unit,
    onWelcomeMessage: (String) -> Unit,
    onAwayEnabled: (Boolean) -> Unit,
    onAwayMessage: (String) -> Unit,
    onAwayStart: (LocalTime) -> Unit,
    onAwayEnd: (LocalTime) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(PageGutter),
        verticalArrangement = Arrangement.spacedBy(FieldSpacing)
    ) {
        WelcomeCard(
            enabled = state.welcomeEnabled,
            message = state.welcomeMessage,
            onEnabled = onWelcomeEnabled,
            onMessage = onWelcomeMessage
        )
        AwayCard(
            enabled = state.awayEnabled,
            message = state.awayMessage,
            start = state.awayStart,
            end = state.awayEnd,
            onEnabled = onAwayEnabled,
            onMessage = onAwayMessage,
            onStart = onAwayStart,
            onEnd = onAwayEnd
        )
        Spacer(Modifier.height(4.dp))
        FilledTonalButton(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Text("Save")
            }
        }
        Spacer(Modifier.height(PageGutter))
    }
}

@Composable
private fun WelcomeCard(
    enabled: Boolean,
    message: String,
    onEnabled: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardGutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WavingHand, contentDescription = null, tint = Theme.colors.brand)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Welcome message",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Sent automatically when a new customer first messages you.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = onEnabled)
            }
            Spacer(Modifier.height(FieldSpacing))
            OutlinedTextField(
                value = message,
                onValueChange = onMessage,
                label = { Text("Message") },
                enabled = enabled,
                minLines = 3,
                maxLines = WELCOME_MAX_LINES,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AwayCard(
    enabled: Boolean,
    message: String,
    start: LocalTime,
    end: LocalTime,
    onEnabled: (Boolean) -> Unit,
    onMessage: (String) -> Unit,
    onStart: (LocalTime) -> Unit,
    onEnd: (LocalTime) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardGutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NightsStay, contentDescription = null, tint = Theme.colors.accent)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Out of hours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Sent when customers message between the times below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = onEnabled)
            }
            Spacer(Modifier.height(FieldSpacing))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FieldSpacing)
            ) {
                TimePickerStub(
                    label = "Start",
                    time = start,
                    enabled = enabled,
                    onChange = onStart,
                    modifier = Modifier.weight(1f)
                )
                TimePickerStub(
                    label = "End",
                    time = end,
                    enabled = enabled,
                    onChange = onEnd,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(FieldSpacing))
            OutlinedTextField(
                value = message,
                onValueChange = onMessage,
                label = { Text("Message") },
                enabled = enabled,
                minLines = 3,
                maxLines = AWAY_MAX_LINES,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Placeholder for a real Material 3 TimePickerDialog. Renders the current time
 * with a "Change" button that, for now, just rolls the time forward by 30
 * minutes so the screen feels live in previews and dev builds.
 */
@Composable
private fun TimePickerStub(
    label: String,
    time: LocalTime,
    enabled: Boolean,
    onChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                timeFormatter.format(time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = { onChange(time.plusMinutes(30)) },
                enabled = enabled
            ) { Text("Change") }
        }
    }
}

@Preview(showBackground = true, name = "Auto-reply — body")
@Composable
private fun PreviewAutoReplyBody() {
    NitiGrowTheme {
        AutoReplyBody(
            state = DummyAutoReplyData.config(),
            onWelcomeEnabled = {},
            onWelcomeMessage = {},
            onAwayEnabled = {},
            onAwayMessage = {},
            onAwayStart = {},
            onAwayEnd = {},
            onSave = {}
        )
    }
}
