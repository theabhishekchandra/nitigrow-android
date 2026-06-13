package com.websbaba.nitigrow.presentation.feature.auth.forgot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordScreen — 4-step reset flow.
//
//   STEP 1: ENTER_ID
//     "Enter your email or phone — we'll send a one-time code."
//     [OutlinedTextField] → [Send code]
//
//   STEP 2: ENTER_OTP
//     "Enter the 6-digit code we just sent."
//     [⎯ ⎯ ⎯ ⎯ ⎯ ⎯] → [Verify code]    Resend
//
//   STEP 3: ENTER_NEW_PASSWORD
//     New password [••••••••]
//     Confirm      [••••••••]   (red helper if mismatch)
//                                 → [Update password]
//
//   STEP 4: DONE
//     ✓  Password updated. Returning to login…    (auto-navigates after 1.5 s)
//
// Auto-return on DONE is implemented with a LaunchedEffect → primary action,
// which feeds back into the same `onPrimaryAction` path → emits
// NavigateBackToLogin via the effects Channel.
//
// Spec: docs/phase-3-mobile.md §1.2 "Forgot password screen"
// ─────────────────────────────────────────────────────────────────────────────

private const val DONE_AUTO_RETURN_MS = 1500L
private const val OTP_LEN = 6
private const val MIN_PASSWORD_LEN = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                ForgotEffect.NavigateBackToLogin -> onComplete()
                is ForgotEffect.Toast -> scope.launch { snackbar.showSnackbar(effect.text) }
            }
        }
    }

    // When the flow reaches DONE we auto-fire the primary action after a
    // short delay so the user sees the confirmation animation before nav.
    LaunchedEffect(state.step) {
        if (state.step == ForgotStep.DONE) {
            delay(DONE_AUTO_RETURN_MS)
            viewModel.onPrimaryAction()
        }
    }

    ForgotPasswordScaffold(
        state = state,
        snackbar = snackbar,
        onBack = onBack,
        onIdentifierChange = viewModel::onIdentifierChange,
        onOtpChange = viewModel::onOtpChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onPrimaryAction = viewModel::onPrimaryAction,
        onResendOtp = viewModel::onResendOtp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordScaffold(
    state: ForgotPasswordUiState,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onIdentifierChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    onResendOtp: () -> Unit,
) {
    val colors = Theme.colors
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset password", fontWeight = FontWeight.SemiBold) },
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
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = colors.paper,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "forgot-step",
            ) { step ->
                when (step) {
                    ForgotStep.ENTER_ID -> EnterIdStep(
                        state = state,
                        onIdentifierChange = onIdentifierChange,
                        onPrimaryAction = onPrimaryAction,
                    )
                    ForgotStep.ENTER_OTP -> EnterOtpStep(
                        state = state,
                        onOtpChange = onOtpChange,
                        onPrimaryAction = onPrimaryAction,
                        onResendOtp = onResendOtp,
                    )
                    ForgotStep.ENTER_NEW_PASSWORD -> EnterNewPasswordStep(
                        state = state,
                        onNewPasswordChange = onNewPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onPrimaryAction = onPrimaryAction,
                    )
                    ForgotStep.DONE -> DoneStep()
                }
            }
        }
    }
}

// ── Step 1 — identifier ─────────────────────────────────────────────────────

@Composable
private fun EnterIdStep(
    state: ForgotPasswordUiState,
    onIdentifierChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
) {
    val colors = Theme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Forgot your password?",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Enter your email or phone — we'll send a one-time code.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink3,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.identifier,
            onValueChange = onIdentifierChange,
            label = { Text("Email or phone") },
            placeholder = { Text("you@example.com") },
            singleLine = true,
            isError = state.error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        state.error?.let { ErrorBanner(message = it) }
        Spacer(Modifier.height(8.dp))
        PrimaryAction(
            text = "Send code",
            loading = state.isSubmitting,
            enabled = state.identifier.isNotBlank(),
            onClick = onPrimaryAction,
        )
    }
}

// ── Step 2 — OTP ────────────────────────────────────────────────────────────

@Composable
private fun EnterOtpStep(
    state: ForgotPasswordUiState,
    onOtpChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    onResendOtp: () -> Unit,
) {
    val colors = Theme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Enter the 6-digit code",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
            fontWeight = FontWeight.Bold,
        )
        Text(
            state.message ?: "Enter the 6-digit code we just sent.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink3,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.otp,
            onValueChange = onOtpChange,
            label = { Text("6-digit code") },
            singleLine = true,
            isError = state.error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        state.error?.let { ErrorBanner(message = it) }
        Spacer(Modifier.height(8.dp))
        PrimaryAction(
            text = "Verify code",
            loading = state.isSubmitting,
            enabled = state.otp.length == OTP_LEN,
            onClick = onPrimaryAction,
        )
        TextButton(
            onClick = onResendOtp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Resend code")
        }
    }
}

// ── Step 3 — new password ───────────────────────────────────────────────────

@Composable
private fun EnterNewPasswordStep(
    state: ForgotPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
) {
    val colors = Theme.colors
    val mismatch = state.confirmPassword.isNotEmpty() &&
        state.newPassword != state.confirmPassword
    val tooShort = state.newPassword.isNotEmpty() &&
        state.newPassword.length < MIN_PASSWORD_LEN

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Set a new password",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Choose a password with at least $MIN_PASSWORD_LEN characters.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink3,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("New password") },
            singleLine = true,
            isError = tooShort,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm password") },
            singleLine = true,
            isError = mismatch,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            supportingText = if (mismatch) {
                { Text("Passwords do not match.", color = colors.danger) }
            } else null,
        )
        state.error?.let { ErrorBanner(message = it) }
        Spacer(Modifier.height(8.dp))
        PrimaryAction(
            text = "Update password",
            loading = state.isSubmitting,
            enabled = state.newPassword.length >= MIN_PASSWORD_LEN &&
                state.newPassword == state.confirmPassword,
            onClick = onPrimaryAction,
        )
    }
}

// ── Step 4 — done ───────────────────────────────────────────────────────────

@Composable
private fun DoneStep() {
    val colors = Theme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.success.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(
            "Password updated",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "Returning to login…",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink3,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Shared primary action button ────────────────────────────────────────────

@Composable
private fun PrimaryAction(
    text: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = Theme.colors
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = colors.brand,
            )
        } else {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Forgot — Enter ID")
@Composable
private fun PreviewForgotEnterId() {
    NitiGrowTheme {
        ForgotPasswordScaffold(
            state = ForgotPasswordUiState(identifier = "owner@websbaba.in"),
            snackbar = remember { SnackbarHostState() },
            onBack = {},
            onIdentifierChange = {},
            onOtpChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onPrimaryAction = {},
            onResendOtp = {},
        )
    }
}

@Preview(showBackground = true, name = "Forgot — Enter OTP")
@Composable
private fun PreviewForgotEnterOtp() {
    NitiGrowTheme {
        ForgotPasswordScaffold(
            state = ForgotPasswordUiState(
                step = ForgotStep.ENTER_OTP,
                identifier = "owner@websbaba.in",
                otp = "1234",
                message = "Code sent to o****@websbaba.in",
            ),
            snackbar = remember { SnackbarHostState() },
            onBack = {},
            onIdentifierChange = {},
            onOtpChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onPrimaryAction = {},
            onResendOtp = {},
        )
    }
}

@Preview(showBackground = true, name = "Forgot — New password (mismatch)")
@Composable
private fun PreviewForgotNewPassword() {
    NitiGrowTheme {
        ForgotPasswordScaffold(
            state = ForgotPasswordUiState(
                step = ForgotStep.ENTER_NEW_PASSWORD,
                newPassword = "secret12",
                confirmPassword = "secret13",
            ),
            snackbar = remember { SnackbarHostState() },
            onBack = {},
            onIdentifierChange = {},
            onOtpChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onPrimaryAction = {},
            onResendOtp = {},
        )
    }
}

@Preview(showBackground = true, name = "Forgot — Done")
@Composable
private fun PreviewForgotDone() {
    NitiGrowTheme {
        ForgotPasswordScaffold(
            state = ForgotPasswordUiState(step = ForgotStep.DONE),
            snackbar = remember { SnackbarHostState() },
            onBack = {},
            onIdentifierChange = {},
            onOtpChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onPrimaryAction = {},
            onResendOtp = {},
        )
    }
}
