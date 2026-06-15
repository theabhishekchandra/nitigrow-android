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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordScreen — 2-step email-link reset flow.
//
//   STEP 1: ENTER_EMAIL
//     "Enter your account email — we'll send you a reset link."
//     [OutlinedTextField] → [Send reset link]
//
//   STEP 2: LINK_SENT
//     ✓  "Check your email" — a reset link was sent to {email}; open it within
//        30 minutes to set a new password.   [Back to login]   Resend email
//
// The password change itself happens on the web page the emailed link opens, so
// the app never handles the reset token. Mirrors POST /auth/forgot-password.
//
// Spec: docs/phase-3-mobile.md §1.2 "Forgot password screen"
// ─────────────────────────────────────────────────────────────────────────────

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

    ForgotPasswordScaffold(
        state = state,
        snackbar = snackbar,
        onBack = onBack,
        onEmailChange = viewModel::onEmailChange,
        onPrimaryAction = viewModel::onPrimaryAction,
        onResend = viewModel::onResend,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordScaffold(
    state: ForgotPasswordUiState,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    onResend: () -> Unit,
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
                    ForgotStep.ENTER_EMAIL -> EnterEmailStep(
                        state = state,
                        onEmailChange = onEmailChange,
                        onPrimaryAction = onPrimaryAction,
                    )
                    ForgotStep.LINK_SENT -> LinkSentStep(
                        state = state,
                        onPrimaryAction = onPrimaryAction,
                        onResend = onResend,
                    )
                }
            }
        }
    }
}

// ── Step 1 — email ──────────────────────────────────────────────────────────

@Composable
private fun EnterEmailStep(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
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
            "Enter your account email — we'll send you a link to reset your password.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink3,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            placeholder = { Text("you@example.com") },
            singleLine = true,
            isError = state.error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        state.error?.let { ErrorBanner(message = it) }
        Spacer(Modifier.height(8.dp))
        PrimaryAction(
            text = "Send reset link",
            loading = state.isSubmitting,
            enabled = state.email.isNotBlank(),
            onClick = onPrimaryAction,
        )
    }
}

// ── Step 2 — link sent ────────────────────────────────────────────────────────

@Composable
private fun LinkSentStep(
    state: ForgotPasswordUiState,
    onPrimaryAction: () -> Unit,
    onResend: () -> Unit,
) {
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
            "Check your email",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "If an account exists for ${state.email.trim()}, we've sent a password " +
                "reset link. Open it within 30 minutes to set a new password — " +
                "remember to check your spam folder.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink3,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        PrimaryAction(
            text = "Back to login",
            loading = false,
            enabled = true,
            onClick = onPrimaryAction,
        )
        TextButton(
            onClick = onResend,
            enabled = !state.isSubmitting,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Resend email")
        }
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

@Preview(showBackground = true, name = "Forgot — Enter email")
@Composable
private fun PreviewForgotEnterEmail() {
    NitiGrowTheme {
        ForgotPasswordScaffold(
            state = ForgotPasswordUiState(email = "owner@websbaba.in"),
            snackbar = remember { SnackbarHostState() },
            onBack = {},
            onEmailChange = {},
            onPrimaryAction = {},
            onResend = {},
        )
    }
}

@Preview(showBackground = true, name = "Forgot — Link sent")
@Composable
private fun PreviewForgotLinkSent() {
    NitiGrowTheme {
        ForgotPasswordScaffold(
            state = ForgotPasswordUiState(
                step = ForgotStep.LINK_SENT,
                email = "owner@websbaba.in",
            ),
            snackbar = remember { SnackbarHostState() },
            onBack = {},
            onEmailChange = {},
            onPrimaryAction = {},
            onResend = {},
        )
    }
}
