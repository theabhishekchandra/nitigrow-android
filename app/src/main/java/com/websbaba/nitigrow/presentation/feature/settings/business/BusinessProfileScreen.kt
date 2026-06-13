package com.websbaba.nitigrow.presentation.feature.settings.business

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.settings.components.FieldLabel
import com.websbaba.nitigrow.presentation.feature.settings.components.NgTextField
import com.websbaba.nitigrow.presentation.feature.settings.components.PrimaryCta
import com.websbaba.nitigrow.presentation.feature.settings.components.SubScreenHeader
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────────────────
// BusinessProfileScreen — Settings ▸ Business profile.
//
// Only the fields the backend actually persists are shown: business name
// (editable) and account email (read-only). Category / address / website /
// GSTIN from the prototype have no backing fields, so they are intentionally
// absent (see BusinessProfileUiState docs).
// ─────────────────────────────────────────────────────────────────────────────

private val FieldSpacing = 14.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    onBack: () -> Unit,
    viewModel: BusinessProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Theme.colors
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is BusinessProfileEffect.Toast -> snackbar.showSnackbar(e.text)
                BusinessProfileEffect.Saved -> onBack()
            }
        }
    }

    Scaffold(
        containerColor = colors.paper,
        topBar = { SubScreenHeader(title = "Business profile", onBack = onBack) },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(color = colors.border2)
                Box(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 16.dp)) {
                    PrimaryCta(
                        text = "Save changes",
                        onClick = viewModel::save,
                        enabled = state.name.isNotBlank(),
                        loading = state.isSaving
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        BusinessProfileForm(
            state = state,
            onName = viewModel::onName,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun BusinessProfileForm(
    state: BusinessProfileUiState,
    onName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 16.dp)
    ) {
        LogoBlock()
        Spacer(Modifier.height(FieldSpacing))

        FieldLabel("Business name")
        NgTextField(value = state.name, onValueChange = onName)
        Spacer(Modifier.height(FieldSpacing))

        FieldLabel("Email")
        NgTextField(value = state.email, onValueChange = {}, enabled = false)
        Text(
            "Managed by your account",
            fontSize = 11.5.sp,
            color = colors.muted,
            modifier = Modifier.padding(top = 6.dp)
        )

        state.error?.let {
            Spacer(Modifier.height(FieldSpacing))
            ErrorBanner(message = it)
        }
    }
}

@Composable
private fun LogoBlock() {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .background(colors.card, RoundedCornerShape(18.dp))
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
        ) {
            Icon(
                Icons.Filled.Storefront,
                contentDescription = "Business logo",
                tint = colors.brand,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.size(14.dp))
        Text(
            "Business logo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.ink
        )
    }
}

@Preview(showBackground = true, name = "Business profile — form")
@Composable
private fun PreviewBusinessProfileForm() {
    NitiGrowTheme {
        BusinessProfileForm(
            state = BusinessProfileUiState(
                name = "Sharma Sweets & Caterers",
                email = "anita@sharmasweets.in"
            ),
            onName = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
