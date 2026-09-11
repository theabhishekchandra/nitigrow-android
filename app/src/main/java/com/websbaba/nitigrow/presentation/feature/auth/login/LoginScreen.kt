package com.websbaba.nitigrow.presentation.feature.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.R
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    @Suppress("UNUSED_PARAMETER") onLoginSuccess: () -> Unit = {},
    onOtpRequested: (String) -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp -> onOtpRequested(effect.phone)
                is LoginEffect.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    val colors = Theme.colors
    val fieldShape = RoundedCornerShape(12.dp)

    Scaffold(containerColor = colors.paper) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = "NitiGrow logo",
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(56.dp)
            )
            Spacer(Modifier.height(22.dp))
            Text(
                "Welcome back",
                style = MaterialTheme.typography.displayMedium,
                color = colors.ink
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Sign in to continue to your business",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.ink3
            )

            Spacer(Modifier.height(24.dp))
            
            // Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.paper2, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (state.isEmailMode) colors.card else colors.paper2)
                        .clickable { viewModel.onToggleMode(true) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Email",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (state.isEmailMode) colors.ink else colors.ink3
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!state.isEmailMode) colors.card else colors.paper2)
                        .clickable { viewModel.onToggleMode(false) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Phone OTP",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!state.isEmailMode) colors.ink else colors.ink3
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (state.isEmailMode) {
                Text(
                    "Email address",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.ink3
                )
                Spacer(Modifier.height(6.dp))
                var emailFocused by remember { mutableStateOf(false) }
                BasicTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.brand),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { emailFocused = it.isFocused }
                        .background(colors.card, fieldShape)
                        .border(
                            width = if (emailFocused) 2.dp else 1.dp,
                            color = if (emailFocused) colors.brand else colors.border,
                            shape = fieldShape
                        ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (state.email.isEmpty()) {
                                Text(
                                    "you@example.com",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.muted2
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    "Password",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.ink3
                )
                Spacer(Modifier.height(6.dp))
                var passFocused by remember { mutableStateOf(false) }
                BasicTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.brand),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.onSubmit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { passFocused = it.isFocused }
                        .background(colors.card, fieldShape)
                        .border(
                            width = if (passFocused) 2.dp else 1.dp,
                            color = if (passFocused) colors.brand else colors.border,
                            shape = fieldShape
                        ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (state.password.isEmpty()) {
                                Text(
                                    "Enter password",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.muted2
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else {
                Text(
                    "WhatsApp number",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.ink3
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(colors.paper2, fieldShape)
                            .border(1.dp, colors.border, fieldShape)
                            .padding(horizontal = 12.dp, vertical = 13.dp)
                    ) {
                        Text(
                            "+91",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.ink3
                        )
                    }
                    var focused by remember { mutableStateOf(false) }
                    BasicTextField(
                        value = state.phone,
                        onValueChange = viewModel::onPhoneChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
                        cursorBrush = SolidColor(colors.brand),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { viewModel.onSubmit() }),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focused = it.isFocused }
                            .background(colors.card, fieldShape)
                            .border(
                                width = if (focused) 2.dp else 1.dp,
                                color = if (focused) colors.brand else colors.border,
                                shape = fieldShape
                            ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (state.phone.isEmpty()) {
                                    Text(
                                        "10-digit number",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.muted2
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "We will send a 6-digit code to this number on WhatsApp.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted
                )
            }

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                ErrorBanner(message = it)
            }

            Spacer(Modifier.height(24.dp))
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
                    Text(
                        if (state.isEmailMode) "Sign In" else "Send OTP",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
