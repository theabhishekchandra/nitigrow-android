package com.websbaba.nitigrow.presentation.feature.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.R
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiPrimaryButton
import com.websbaba.nitigrow.presentation.components.NitiTextButton
import com.websbaba.nitigrow.presentation.components.nitiTextFieldColors
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    @Suppress("UNUSED_PARAMETER") onLoginSuccess: () -> Unit = {},
    onOtpRequested: (String) -> Unit = {},
    onForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp -> onOtpRequested(effect.phone)
                is LoginEffect.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Column(
        verticalArrangement = Arrangement.spacedBy(28.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val tile = RoundedCornerShape(18.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp).shadow(6.dp, tile).background(colors.logoTile, tile)
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = "NitiGrow logo",
                    modifier = Modifier.size(42.dp)
                )
            }
            Text(
                text = "NitiGrow",
                style = NitiType.title.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                color = colors.onSurface
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Welcome back",
                style = NitiType.display.copy(fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-1).sp),
                color = colors.onSurface
            )
            Text(
                text = "Sign in to continue to your business",
                style = NitiType.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                color = colors.onSurfaceVariant
            )
        }

        ModeToggle(isEmail = state.isEmailMode, onSelect = viewModel::onToggleMode)

        if (state.isEmailMode) {
            EmailForm(state, viewModel, onForgotPassword)
        } else {
            PhoneForm(state, viewModel)
        }

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

        NitiPrimaryButton(
            text = if (state.isEmailMode) "Sign in" else "Send OTP",
            onClick = viewModel::onSubmit,
            enabled = state.canSubmit,
            loading = state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Email / Phone OTP switch: a 48dp outlined pill split in two, the active half tinted. */
@Composable
private fun ModeToggle(isEmail: Boolean, onSelect: (Boolean) -> Unit) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .border(1.dp, colors.outline, shape)
    ) {
        ToggleHalf("Email", NitiIcons.Mail, isEmail, { onSelect(true) }, Modifier.weight(1f))
        Box(Modifier.width(1.dp).fillMaxHeight().background(colors.outline))
        ToggleHalf("Phone OTP", NitiIcons.Chat, !isEmail, { onSelect(false) }, Modifier.weight(1f))
    }
}

@Composable
private fun ToggleHalf(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = Niti.colors
    val ink = if (selected) colors.primaryTone.onContainer else colors.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .fillMaxSize()
            .background(if (selected) colors.primaryTone.container else Color.Transparent)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    ) {
        Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
        Text(text = label, style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold), color = ink)
    }
}

@Composable
private fun EmailForm(state: LoginUiState, vm: LoginViewModel, onForgotPassword: () -> Unit) {
    var showPassword by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            label = { Text("Email address") },
            leadingIcon = { Icon(NitiIcons.Mail, contentDescription = null, modifier = Modifier.size(22.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = nitiTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(NitiIcons.Lock, contentDescription = null, modifier = Modifier.size(22.dp)) },
            trailingIcon = {
                NitiIconButton(
                    icon = if (showPassword) NitiIcons.EyeOff else NitiIcons.Eye,
                    contentDescription = if (showPassword) "Hide password" else "Show password",
                    onClick = { showPassword = !showPassword },
                    tint = Niti.colors.onSurfaceVariant
                )
            },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            colors = nitiTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { vm.onSubmit() }),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            NitiTextButton(text = "Forgot password?", onClick = onForgotPassword)
        }
    }
}

@Composable
private fun PhoneForm(state: LoginUiState, vm: LoginViewModel) {
    OutlinedTextField(
        value = state.phone,
        onValueChange = vm::onPhoneChange,
        label = { Text("WhatsApp number") },
        placeholder = { Text("98765 43210") },
        prefix = { Text("+91", style = NitiType.body.copy(fontWeight = FontWeight.Medium)) },
        supportingText = { Text("We will send a 6-digit code to this number on WhatsApp.") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = nitiTextFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { vm.onSubmit() }),
        modifier = Modifier.fillMaxWidth()
    )
}
