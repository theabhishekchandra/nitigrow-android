package com.ardym.nitigrow.presentation.feature.auth.otp

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.core.biometric.BiometricAuthenticator
import com.ardym.nitigrow.core.biometric.BiometricResult
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val OTP_LENGTH = 6

@Composable
fun OtpScreen(
    onAuthSuccess: () -> Unit,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var biometricOptIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                OtpEffect.NavigateToHome -> {
                    if (biometricOptIn) {
                        val activity = context as? FragmentActivity
                        if (activity != null && BiometricAuthenticator.isAvailable(activity)) {
                            val res = BiometricAuthenticator.prompt(activity)
                            if (res is BiometricResult.Success) viewModel.enableBiometric(true)
                        }
                    }
                    onAuthSuccess()
                }
                is OtpEffect.ShowMessage -> scope.launch { snackbar.showSnackbar(effect.message) }
            }
        }
    }

    val colors = Theme.colors
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        containerColor = colors.paper,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            IconButton(
                onClick = { backDispatcher?.onBackPressed() },
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.ink
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Verify your number",
                style = MaterialTheme.typography.displayMedium,
                color = colors.ink
            )
            Spacer(Modifier.height(6.dp))
            Text(
                buildAnnotatedString {
                    append("Enter the 6-digit code sent on WhatsApp to ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.ink)) {
                        append(formatPhone(state.phone))
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.ink3
            )

            Spacer(Modifier.height(28.dp))
            OtpCodeField(
                code = state.code,
                onCodeChange = viewModel::onCodeChange
            )

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                ErrorBanner(message = it)
            }

            Spacer(Modifier.height(16.dp))
            if (state.resendSeconds > 0) {
                Text(
                    buildAnnotatedString {
                        append("Resend code in ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.ink)) {
                            append("0:%02d".format(state.resendSeconds))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                TextButton(
                    onClick = viewModel::onResend,
                    enabled = state.canResend,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Resend code",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.brand
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = biometricOptIn, onCheckedChange = { biometricOptIn = it })
                Text(
                    "Enable biometric unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.ink3
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::onSubmit,
                enabled = state.canSubmit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.brand,
                    contentColor = colors.paper,
                    disabledContainerColor = colors.brand.copy(alpha = 0.4f),
                    disabledContentColor = colors.paper
                ),
                contentPadding = PaddingValues(vertical = 15.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.paper
                    )
                } else {
                    Text("Verify & continue", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Six 54dp digit boxes backed by a single invisible text field. The next empty
 * box gets the active treatment (2dp brand border + brandRing glow) while the
 * field is focused.
 */
@Composable
private fun OtpCodeField(
    code: String,
    onCodeChange: (String) -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .semantics { contentDescription = "6-digit code" },
        decorationBox = { innerTextField ->
            Box {
                // Keep the real field in the tree (IME target) but visually hidden.
                Box(
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0f)
                ) { innerTextField() }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(OTP_LENGTH) { index ->
                        val isActive = focused &&
                            index == code.length.coerceAtMost(OTP_LENGTH - 1) &&
                            code.length < OTP_LENGTH
                        OtpDigitCell(
                            digit = code.getOrNull(index),
                            isActive = isActive,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun OtpDigitCell(
    digit: Char?,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    val cellShape = RoundedCornerShape(12.dp)
    // Outer 3dp gutter renders the brandRing glow of the active cell, mirroring
    // the design's `box-shadow: 0 0 0 4px brandRing` without resizing the cell.
    Box(
        modifier = modifier
            .height(60.dp)
            .background(
                if (isActive) colors.brandRing else Color.Transparent,
                RoundedCornerShape(15.dp)
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.card, cellShape)
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) colors.brand else colors.border,
                    shape = cellShape
                ),
            contentAlignment = Alignment.Center
        ) {
            digit?.let {
                Text(
                    it.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.ink
                )
            }
        }
    }
}

/**
 * "9876543210" / "919876543210" → "+91 98765 43210" (+91 is fixed at login);
 * anything else falls back to "+<digits>".
 */
private fun formatPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
        digits.isEmpty() -> "your WhatsApp number"
        digits.length == 10 ->
            "+91 ${digits.substring(0, 5)} ${digits.substring(5)}"
        digits.length == 12 && digits.startsWith("91") ->
            "+91 ${digits.substring(2, 7)} ${digits.substring(7)}"
        else -> "+$digits"
    }
}
