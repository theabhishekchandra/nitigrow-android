package com.websbaba.nitigrow.presentation.feature.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.payments.components.ContactPickerSheet
import com.websbaba.nitigrow.presentation.feature.payments.components.PaymentLinkRow
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// PaymentLinkScreen — "Payment links" feature root (design: Payments screen).
//
//   ◀  Payment links                       ← Fraunces 21sp
//      ₹48,250 collected in June           ← only when something was collected
//   ┌─ [₹]  ₹6,800 · Rajesh · Today ──(Pending)──[⧉]─┐
//   ┌─ [₹]  ₹2,450 · Kavita · Tue ───(Paid)─────[⧉]─┐
//                                        [+ New link] ← extended FAB
//
// "+ New link" opens the new-link bottom sheet (amount → contact → note →
// create CTA); "Change" inside it opens the contact picker sheet. Both sheets
// drive the same ViewModel form state as before — no data-layer changes.
// ─────────────────────────────────────────────────────────────────────────────

private val InrFormat: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))
private val MonthFmt = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)

/** Which bottom sheet is showing. Presentation-only state. */
private enum class PaySheet { NONE, NEW_LINK, PICK_CONTACT }

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
    onDescriptionChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val colors = Theme.colors
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var sheet by remember { mutableStateOf(PaySheet.NONE) }

    // Close the new-link sheet only when the ViewModel reports a real success
    // (isSending flips off with no error — the form is cleared by then).
    var wasSending by remember { mutableStateOf(false) }
    LaunchedEffect(state.isSending) {
        if (wasSending && !state.isSending && state.error == null) {
            sheet = PaySheet.NONE
            snackbar.showSnackbar("Payment link sent")
        }
        wasSending = state.isSending
    }

    Scaffold(
        containerColor = colors.paper,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { sheet = PaySheet.NEW_LINK },
                shape = RoundedCornerShape(16.dp),
                containerColor = colors.brand,
                contentColor = if (colors.isLight) colors.paper else colors.brandInk,
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = {
                    Text(
                        "New link",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PaymentsHeader(state = state, onBack = onBack)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.recentLinks.isEmpty()) {
                    item {
                        Text(
                            text = "Links you send will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                        )
                    }
                } else {
                    items(state.recentLinks, key = { it.id }) { link ->
                        PaymentLinkRow(
                            link = link,
                            onCopyLink = { url ->
                                clipboard.setText(AnnotatedString(url))
                                scope.launch { snackbar.showSnackbar("Link copied") }
                            },
                        )
                    }
                }
            }
        }
    }

    if (sheet == PaySheet.NEW_LINK) {
        NewLinkSheet(
            state = state,
            onAmountChange = onAmountChange,
            onDescriptionChange = onDescriptionChange,
            onChangeContact = { sheet = PaySheet.PICK_CONTACT },
            onSend = onSend,
            onDismiss = { sheet = PaySheet.NONE },
        )
    }

    if (sheet == PaySheet.PICK_CONTACT) {
        ContactPickerSheet(
            contacts = state.contacts,
            onPick = { id, name ->
                onPickContact(id, name)
                sheet = PaySheet.NEW_LINK
            },
            onDismiss = { sheet = PaySheet.NEW_LINK },
        )
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun PaymentsHeader(state: PaymentLinkUiState, onBack: () -> Unit) {
    val colors = Theme.colors
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
                tint = colors.ink,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Payment links",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 21.sp,
                color = colors.ink,
            )
            val collected = state.collectedThisMonthInr
            if (collected > 0L) {
                Text(
                    text = "₹${InrFormat.format(collected)} collected in ${MonthFmt.format(LocalDate.now())}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    color = colors.muted,
                )
            }
        }
    }
}

// ── New-link sheet ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewLinkSheet(
    state: PaymentLinkUiState,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onChangeContact: () -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = Theme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.paper,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { SheetDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "New payment link",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 19.sp,
                color = colors.ink,
                modifier = Modifier.padding(bottom = 2.dp),
            )

            Column {
                FieldLabel("Amount")
                AmountField(value = state.amountInr, onValueChange = onAmountChange)
            }

            Column {
                FieldLabel("Send to")
                ContactSelectorRow(state = state, onClick = onChangeContact)
            }

            Column {
                FieldLabel("Note (shows on the link)")
                NoteField(value = state.description, onValueChange = onDescriptionChange)
            }

            state.error?.let { SheetErrorBanner(message = it) }

            Button(
                onClick = onSend,
                enabled = state.isReadyToSend && !state.isSending,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.brand,
                    contentColor = if (colors.isLight) colors.paper else colors.brandInk,
                    disabledContainerColor = colors.brand.copy(alpha = 0.4f),
                    disabledContentColor = if (colors.isLight) colors.paper else colors.brandInk,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .height(50.dp),
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = if (colors.isLight) colors.paper else colors.brandInk,
                    )
                } else {
                    Text(
                        "Create & send on WhatsApp",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .size(width = 38.dp, height = 4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Theme.colors.muted3)
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = Theme.colors.ink3,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun AmountField(value: String, onValueChange: (String) -> Unit) {
    val colors = Theme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "₹",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.muted,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
            ),
            cursorBrush = SolidColor(colors.brand),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, top = 13.dp, bottom = 13.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.muted2,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun ContactSelectorRow(state: PaymentLinkUiState, onClick: () -> Unit) {
    val colors = Theme.colors
    val name = state.selectedContactName
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        if (name.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.paper2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = "Pick a contact",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Choose",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.brand,
            )
        } else {
            val avatarUrl = state.contacts.firstOrNull { it.id == state.selectedContactId }?.avatarUrl
            Avatar(name = name, url = avatarUrl, sizeDp = 36)
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Change",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.brand,
            )
        }
    }
}

@Composable
private fun NoteField(value: String, onValueChange: (String) -> Unit) {
    val colors = Theme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.brand),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "e.g. Invoice #4821, advance payment",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.muted2,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun SheetErrorBanner(message: String) {
    val colors = Theme.colors
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = colors.danger,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.danger.copy(alpha = 0.12f))
            .padding(12.dp),
    )
}

// ── Previews ────────────────────────────────────────────────────────────────

// Inline sample data for @Preview only.
private val previewPaymentLinks = listOf(
    SentPaymentLink("p1", "Priya Sharma", 5000, SentLinkStatus.PAID, java.time.Instant.now(), "https://nitigrow.in/pay/p1"),
    SentPaymentLink("p2", "Rahul Verma", 1200, SentLinkStatus.PENDING, java.time.Instant.now(), "https://nitigrow.in/pay/p2"),
    SentPaymentLink("p3", "Anita Desai", 800, SentLinkStatus.EXPIRED, java.time.Instant.now(), null)
)

@Preview(showBackground = true, name = "PaymentLinkScreen — list")
@Composable
private fun PreviewPaymentLinkScreenList() {
    NitiGrowTheme {
        PaymentLinkScreenContent(
            state = PaymentLinkUiState(recentLinks = previewPaymentLinks),
            onBack = {},
            onAmountChange = {},
            onPickContact = { _, _ -> },
            onDescriptionChange = {},
            onSend = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentLinkScreen — empty")
@Composable
private fun PreviewPaymentLinkScreenEmpty() {
    NitiGrowTheme {
        PaymentLinkScreenContent(
            state = PaymentLinkUiState(),
            onBack = {},
            onAmountChange = {},
            onPickContact = { _, _ -> },
            onDescriptionChange = {},
            onSend = {},
        )
    }
}
