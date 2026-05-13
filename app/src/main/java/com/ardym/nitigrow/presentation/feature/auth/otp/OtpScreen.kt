package com.ardym.nitigrow.presentation.feature.auth.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.core.biometric.BiometricAuthenticator
import com.ardym.nitigrow.core.biometric.BiometricResult
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.components.PrimaryButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Enter OTP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Sent to +${state.phone}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = state.code,
                onValueChange = viewModel::onCodeChange,
                label = { Text("6-digit code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = state.error != null,
                modifier = Modifier.fillMaxWidth()
            )
            state.error?.let {
                Spacer(Modifier.height(12.dp))
                ErrorBanner(message = it)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = biometricOptIn, onCheckedChange = { biometricOptIn = it })
                Text("Enable biometric unlock", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = "Verify",
                onClick = viewModel::onSubmit,
                loading = state.isLoading,
                enabled = state.canSubmit
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = viewModel::onResend,
                enabled = state.canResend,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    if (state.resendSeconds > 0) "Resend in ${state.resendSeconds}s" else "Resend OTP",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
