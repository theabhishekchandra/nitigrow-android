package com.websbaba.nitigrow.presentation.feature.auth.forgot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiPrimaryButton
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.presentation.components.NitiTonalButton
import com.websbaba.nitigrow.presentation.components.nitiTextFieldColors
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordScreen — 2-step email-link reset flow.
//
//   STEP 1: ENTER_EMAIL  key tile · "Forgot your password?" · email field · [Send reset link]
//   STEP 2: LINK_SENT    mail tile · "Check your email" · [Resend email] · Back to login
//
// The password change itself happens on the web page the emailed link opens, so
// the app never handles the reset token. Mirrors POST /auth/forgot-password.
//
// Spec: docs/phase-3-mobile.md §1.2 "Forgot password screen"
// ─────────────────────────────────────────────────────────────────────────────

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

    ForgotPasswordContent(
        state = state,
        snackbar = snackbar,
        onBack = onBack,
        onEmailChange = viewModel::onEmailChange,
        onPrimaryAction = viewModel::onPrimaryAction,
        onResend = viewModel::onResend,
    )
}

@Composable
private fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    onResend: () -> Unit,
) {
    val colors = Niti.colors
    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Box(modifier = Modifier.fillMaxSize().background(colors.surface).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, bottom = 8.dp)) {
                NitiIconButton(icon = NitiIcons.Back, contentDescription = "Back", onClick = onBack)
            }
            when (state.step) {
                ForgotStep.ENTER_EMAIL -> EnterEmailStep(state, onEmailChange, onPrimaryAction)
                ForgotStep.LINK_SENT -> LinkSentStep(state, onPrimaryAction, onResend)
            }
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp))
    }
}

/** Shared frame: a tinted icon tile, a large title and a supporting line. */
@Composable
private fun StepHeader(icon: ImageVector, tone: NitiTone, title: String, body: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(64.dp).background(tone.container, RoundedCornerShape(22.dp))
        ) {
            Icon(icon, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(30.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = NitiType.display.copy(fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.9).sp),
                color = Niti.colors.onSurface
            )
            body()
        }
    }
}

@Composable
private fun EnterEmailStep(state: ForgotPasswordUiState, onEmailChange: (String) -> Unit, onSubmit: () -> Unit) {
    val colors = Niti.colors
    Column(modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) {
            StepHeader(NitiIcons.Key, colors.primaryTone, "Forgot your password?") {
                Text(
                    text = "Enter your account email — we'll send you a reset link.",
                    style = NitiType.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                    color = colors.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                placeholder = { Text("you@example.com") },
                leadingIcon = { Icon(NitiIcons.Mail, contentDescription = null, modifier = Modifier.size(22.dp)) },
                singleLine = true,
                isError = state.error != null,
                shape = RoundedCornerShape(16.dp),
                colors = nitiTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth()
            )
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
        }
        NitiPrimaryButton(
            text = "Send reset link",
            onClick = onSubmit,
            enabled = state.email.isNotBlank(),
            loading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LinkSentStep(state: ForgotPasswordUiState, onBackToLogin: () -> Unit, onResend: () -> Unit) {
    val colors = Niti.colors
    Column(modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            StepHeader(NitiIcons.Mail, colors.secondaryTone, "Check your email") {
                Text(
                    text = buildAnnotatedString {
                        append("We sent a reset link to ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.onSurface)) { append(state.email.trim()) }
                        append(". It expires in 30 minutes.")
                    },
                    style = NitiType.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                    color = colors.onSurfaceVariant
                )
            }
            state.error?.let {
                Text(
                    text = it,
                    style = NitiType.label,
                    color = colors.error,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.error.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NitiTonalButton(text = "Resend email", onClick = onResend, loading = state.isSubmitting, modifier = Modifier.fillMaxWidth())
            NitiTextButton(text = "Back to login", onClick = onBackToLogin, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Forgot — enter email", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewForgotEnterEmail() {
    NitiGrowTheme(darkTheme = false) {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(email = "anita@sharmasweets.in"),
            snackbar = SnackbarHostState(),
            onBack = {}, onEmailChange = {}, onPrimaryAction = {}, onResend = {}
        )
    }
}

@Preview(name = "Forgot — link sent", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewForgotLinkSent() {
    NitiGrowTheme(darkTheme = false) {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(email = "anita@sharmasweets.in", step = ForgotStep.LINK_SENT),
            snackbar = SnackbarHostState(),
            onBack = {}, onEmailChange = {}, onPrimaryAction = {}, onResend = {}
        )
    }
}
