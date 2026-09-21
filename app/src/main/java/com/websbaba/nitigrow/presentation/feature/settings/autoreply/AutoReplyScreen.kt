package com.websbaba.nitigrow.presentation.feature.settings.autoreply

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.feature.settings.components.NgToggle
import com.websbaba.nitigrow.presentation.feature.settings.components.PrimaryCta
import com.websbaba.nitigrow.presentation.feature.settings.components.SubScreenHeader
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// AutoReplyScreen — Settings ▸ Auto-replies.
//
//   ‹ Auto-replies
//   ┌ Welcome message · subtitle              [toggle] ┐
//   │ ┌ editable message block (paper2) ┐              │
//   ┌ Away message · subtitle                 [toggle] ┐
//   │ ┌ editable message block ┐  [FROM 9:30 PM][TO …] │
//   [Save changes]
//
// The prototype shows a separate "Quiet hours" rule; the backend only stores
// welcome + away (enabled/message), so the FROM/TO window lives on the Away
// card and keeps its existing UI-only time-picker behaviour.
// ─────────────────────────────────────────────────────────────────────────────

private val CardSpacing = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoReplyScreen(
    onBack: () -> Unit,
    viewModel: AutoReplyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Theme.colors
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is AutoReplyEffect.Toast -> snackbar.showSnackbar(e.text)
            }
        }
    }

    Scaffold(
        containerColor = colors.paper,
        topBar = { SubScreenHeader(title = "Auto-replies", onBack = onBack) },
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
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(CardSpacing)
    ) {
        RuleCard(
            title = "Welcome message",
            subtitle = "Sent on a customer's first message",
            enabled = state.welcomeEnabled,
            onEnabled = onWelcomeEnabled
        ) {
            MessageBlock(
                value = state.welcomeMessage,
                onValueChange = onWelcomeMessage,
                enabled = state.welcomeEnabled
            )
        }
        RuleCard(
            title = "Away message",
            subtitle = "Sent outside business hours",
            enabled = state.awayEnabled,
            onEnabled = onAwayEnabled
        ) {
            MessageBlock(
                value = state.awayMessage,
                onValueChange = onAwayMessage,
                enabled = state.awayEnabled
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                TimeBox(
                    label = "FROM",
                    time = state.awayStart,
                    enabled = state.awayEnabled,
                    onChange = onAwayStart,
                    modifier = Modifier.weight(1f)
                )
                TimeBox(
                    label = "TO",
                    time = state.awayEnd,
                    enabled = state.awayEnabled,
                    onChange = onAwayEnd,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        PrimaryCta(text = "Save changes", onClick = onSave, loading = state.isSaving)
    }
}

@Composable
private fun RuleCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink
                )
                Text(
                    subtitle,
                    fontSize = 11.5.sp,
                    color = colors.muted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            NgToggle(checked = enabled, onCheckedChange = onEnabled)
        }
        content()
    }
}

/**
 * The prototype renders the rule text as a paper2 preview block; here it stays
 * editable (the backend persists the message), styled to match the block.
 */
@Composable
private fun MessageBlock(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    val colors = Theme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        minLines = 2,
        maxLines = 6,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.5.sp,
            lineHeight = 19.sp,
            color = if (enabled) colors.ink2 else colors.muted
        ),
        cursorBrush = SolidColor(colors.brand),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.paper2, RoundedCornerShape(12.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp)
            ) { inner() }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

/**
 * Bordered FROM/TO box backed by the real Material 3 [TimePicker] dialog.
 * Tapping the box opens the dial seeded with the current value; confirming
 * propagates the chosen [LocalTime] back through [onChange].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeBox(
    label: String,
    time: LocalTime,
    enabled: Boolean,
    onChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    var showPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { showPicker = true }
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = colors.muted
        )
        Text(
            timeFormatter.format(time),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) colors.ink else colors.muted,
            modifier = Modifier.padding(top = 2.dp)
        )
    }

    if (showPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(if (label == "FROM") "From time" else "To time") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onChange(LocalTime.of(pickerState.hour, pickerState.minute))
                        showPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Preview(showBackground = true, name = "Auto-replies — body")
@Composable
private fun PreviewAutoReplyBody() {
    NitiGrowTheme {
        AutoReplyBody(
            state = AutoReplyUiState(
                welcomeEnabled = true,
                welcomeMessage = "Namaste! Welcome to Sharma Sweets & Caterers. Hum 10 minute me reply karenge.",
                awayEnabled = true,
                awayMessage = "Dukaan abhi band hai (9 PM – 9 AM). Aapka message mil gaya hai."
            ),
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
