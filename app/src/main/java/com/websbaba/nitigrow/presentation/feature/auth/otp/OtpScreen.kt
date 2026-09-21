package com.websbaba.nitigrow.presentation.feature.auth.otp

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
import com.websbaba.nitigrow.core.biometric.BiometricAuthenticator
import com.websbaba.nitigrow.core.biometric.BiometricResult
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiPrimaryButton
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.core.ui.theme.Bricolage
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
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
    val colors = Niti.colors
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

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

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Box(modifier = Modifier.fillMaxSize().background(colors.surface).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, bottom = 8.dp)) {
                NitiIconButton(icon = NitiIcons.Back, contentDescription = "Back", onClick = { backDispatcher?.onBackPressed() })
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(64.dp).background(colors.primaryTone.container, RoundedCornerShape(22.dp))
                ) {
                    Icon(NitiIcons.Chat, contentDescription = null, tint = colors.primaryTone.onContainer, modifier = Modifier.size(30.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Verify your number",
                        style = NitiType.display.copy(fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.9).sp),
                        color = colors.onSurface
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Enter the 6-digit code sent on WhatsApp to ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.onSurface)) {
                                append(formatPhone(state.phone))
                            }
                        },
                        style = NitiType.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                        color = colors.onSurfaceVariant
                    )
                }

                OtpCodeField(code = state.code, onCodeChange = viewModel::onCodeChange)

                state.error?.let {
                    Text(
                        text = it,
                        style = NitiType.label,
                        color = colors.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.error.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                if (state.resendSeconds > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(NitiIcons.Clock, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Resend code in ${resendLabel(state.resendSeconds)}",
                            style = NitiType.bodyCompact.copy(fontWeight = FontWeight.Medium),
                            color = colors.onSurfaceVariant
                        )
                    }
                } else {
                    NitiTextButton(text = "Resend code", onClick = viewModel::onResend, enabled = state.canResend)
                }

                BiometricCard(checked = biometricOptIn, onChange = { biometricOptIn = it })
            }

            NitiPrimaryButton(
                text = "Verify & continue",
                onClick = viewModel::onSubmit,
                enabled = state.canSubmit,
                loading = state.isLoading,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            )
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp))
    }
}

@Composable
private fun BiometricCard(checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = Niti.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceLow)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).background(colors.secondaryTone.container, CircleShape)
        ) {
            Icon(NitiIcons.Fingerprint, contentDescription = null, tint = colors.secondaryTone.onContainer, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Enable biometric unlock", style = NitiType.bodyStrong, color = colors.onSurface)
            Text(
                text = "Skip the code next time on this device",
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                checkedBorderColor = colors.primary,
                uncheckedThumbColor = colors.outline,
                uncheckedTrackColor = colors.surfaceHigh,
                uncheckedBorderColor = colors.outline
            )
        )
    }
}

/**
 * Six digit boxes backed by a single invisible text field (the IME target).
 * Filled boxes are tinted, and the next empty box gets a 2dp primary border
 * while the field is focused.
 */
@Composable
private fun OtpCodeField(code: String, onCodeChange: (String) -> Unit) {
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
                Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(OTP_LENGTH) { index ->
                        val isActive = focused && index == code.length.coerceAtMost(OTP_LENGTH - 1) && code.length < OTP_LENGTH
                        OtpDigitCell(digit = code.getOrNull(index), isActive = isActive, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    )
}

@Composable
private fun OtpDigitCell(digit: Char?, isActive: Boolean, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(16.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(if (digit != null) colors.surfaceLow else Color.Transparent)
            .border(if (isActive) 2.dp else 1.dp, if (isActive) colors.primary else colors.outline, shape)
    ) {
        digit?.let {
            Text(
                text = it.toString(),
                style = NitiType.headline.copy(fontFamily = Bricolage, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
                color = colors.onSurface
            )
        }
    }
}

/** "0:24" — the resend countdown in minutes:seconds. */
internal fun resendLabel(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/**
 * "9876543210" / "919876543210" → "+91 98765 43210" (+91 is fixed at login);
 * anything else falls back to "+<digits>".
 */
internal fun formatPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
        digits.isEmpty() -> "your WhatsApp number"
        digits.length == 10 -> "+91 ${digits.substring(0, 5)} ${digits.substring(5)}"
        digits.length == 12 && digits.startsWith("91") -> "+91 ${digits.substring(2, 7)} ${digits.substring(7)}"
        else -> "+$digits"
    }
}
