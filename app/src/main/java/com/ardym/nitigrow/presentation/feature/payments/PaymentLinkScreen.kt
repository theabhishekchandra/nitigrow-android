package com.ardym.nitigrow.presentation.feature.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.payments.components.ContactPickerSheet
import com.ardym.nitigrow.presentation.feature.payments.components.PaymentLinkRow
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// PaymentLinkScreen — "Send a payment link" feature root.
//
// Layout (spec: docs/phase-3-mobile.md §1.3 "Payments Screen"):
//   ┌── TopAppBar "Send payment link" ◀ ─────────────────────┐
//   │                                                         │
//   │ ┌─────────────── Send link card ───────────────────┐    │
//   │ │  Amount                                          │    │
//   │ │  ┌──────────────────────────────────────────────┐│    │
//   │ │  │ ₹  [           5000                       ] ││    │
//   │ │  └──────────────────────────────────────────────┘│    │
//   │ │  To                                              │    │
//   │ │  [+ Pick a contact ▾]    or  [⬤ Priya Sharma ✕] │    │
//   │ │  Description (optional)                          │    │
//   │ │  [                                              ] │    │
//   │ │  ┌──────────── Send link ───────────────────────┐│    │
//   │ │  └──────────────────────────────────────────────┘│    │
//   │ └──────────────────────────────────────────────────┘    │
//   │                                                         │
//   │ Recent payment links                                    │
//   │ ┌─ Priya Sharma · ₹12,000 · PAID · 18 min ago ────────┐ │
//   │ ┌─ Rahul Verma  · ₹48,500 · PENDING · 2 h ago ────────┐ │
//   └─────────────────────────────────────────────────────────┘
// ─────────────────────────────────────────────────────────────────────────────

private const val AMOUNT_MAX_LENGTH = 7
private val SectionGap = 16.dp
private val CardInnerPad = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentLinkScreen(
    onBack: () -> Unit,
    viewModel: PaymentLinkViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PaymentLinkScreenContent(
        state = state,
        onBack = onBack,
        onAmountChange = viewModel::onAmountChange,
        onPickContact = viewModel::onPickContact,
        onClearContact = { viewModel.onPickContact("", "") },
        onDescriptionChange = viewModel::onDescriptionChange,
        onSend = viewModel::send,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentLinkScreenContent(
    state: PaymentLinkUiState,
    onBack: () -> Unit,
    onAmountChange: (String) -> Unit,
    onPickContact: (String, String) -> Unit,
    onClearContact: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val colors = Theme.colors
    var pickerOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send payment link", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.paper,
                    titleContentColor = colors.ink,
                    navigationIconContentColor = colors.ink,
                )
            )
        },
        containerColor = colors.paper,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(SectionGap),
        ) {
            item {
                SendLinkCard(
                    state = state,
                    onAmountChange = onAmountChange,
                    onOpenPicker = { pickerOpen = true },
                    onClearContact = onClearContact,
                    onDescriptionChange = onDescriptionChange,
                    onSend = onSend,
                )
            }

            state.error?.let { err ->
                item { ErrorBanner(message = err) }
            }

            item {
                Text(
                    text = "Recent payment links",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (state.recentLinks.isEmpty()) {
                item {
                    Text(
                        text = "Links you send will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }
            } else {
                items(state.recentLinks, key = { it.id }) { link ->
                    PaymentLinkRow(link = link)
                }
            }
        }
    }

    if (pickerOpen) {
        ContactPickerSheet(
            onPick = { id, name -> onPickContact(id, name) },
            onDismiss = { pickerOpen = false },
        )
    }
}

@Composable
private fun SendLinkCard(
    state: PaymentLinkUiState,
    onAmountChange: (String) -> Unit,
    onOpenPicker: () -> Unit,
    onClearContact: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val colors = Theme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardInnerPad),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.amountInr,
                onValueChange = onAmountChange,
                label = { Text("Amount") },
                prefix = { Text("₹ ", style = MaterialTheme.typography.titleLarge) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
            )

            ContactPickerField(
                contactName = state.selectedContactName,
                onOpenPicker = onOpenPicker,
                onClearContact = onClearContact,
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (optional)") },
                placeholder = { Text("e.g. Invoice #4821, advance payment") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            FilledTonalButton(
                onClick = onSend,
                enabled = state.isReadyToSend && !state.isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.brand,
                    )
                } else {
                    Text(
                        "Send link",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactPickerField(
    contactName: String?,
    onOpenPicker: () -> Unit,
    onClearContact: () -> Unit,
) {
    val colors = Theme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "To",
            style = MaterialTheme.typography.labelMedium,
            color = colors.ink3,
        )
        if (contactName.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.paper2)
                    .clickable(onClick = onOpenPicker)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = colors.ink3,
                )
                Text(
                    text = "Pick a contact",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.ink3,
                )
            }
        } else {
            SelectedContactChip(
                name = contactName,
                onClear = onClearContact,
                onTap = onOpenPicker,
            )
        }
    }
}

@Composable
private fun SelectedContactChip(
    name: String,
    onClear: () -> Unit,
    onTap: () -> Unit,
) {
    val colors = Theme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.brandSoft)
            .clickable(onClick = onTap)
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = colors.brand,
            fontWeight = FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onClear),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Clear contact",
                tint = colors.brand,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "PaymentLinkScreen — empty")
@Composable
private fun PreviewPaymentLinkScreenEmpty() {
    NitiGrowTheme {
        PaymentLinkScreenContent(
            state = PaymentLinkUiState(recentLinks = DummyPaymentLinkData.recent()),
            onBack = {},
            onAmountChange = {},
            onPickContact = { _, _ -> },
            onClearContact = {},
            onDescriptionChange = {},
            onSend = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentLinkScreen — ready to send")
@Composable
private fun PreviewPaymentLinkScreenReady() {
    NitiGrowTheme {
        PaymentLinkScreenContent(
            state = PaymentLinkUiState(
                amountInr = "5000",
                selectedContactId = "k-001",
                selectedContactName = "Priya Sharma",
                description = "Advance for saffron order",
                recentLinks = DummyPaymentLinkData.recent(),
            ),
            onBack = {},
            onAmountChange = {},
            onPickContact = { _, _ -> },
            onClearContact = {},
            onDescriptionChange = {},
            onSend = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentLinkScreen — sending")
@Composable
private fun PreviewPaymentLinkScreenSending() {
    NitiGrowTheme {
        PaymentLinkScreenContent(
            state = PaymentLinkUiState(
                amountInr = "12000",
                selectedContactId = "k-002",
                selectedContactName = "Rahul Verma",
                isSending = true,
                recentLinks = DummyPaymentLinkData.recent(),
            ),
            onBack = {},
            onAmountChange = {},
            onPickContact = { _, _ -> },
            onClearContact = {},
            onDescriptionChange = {},
            onSend = {},
        )
    }
}
