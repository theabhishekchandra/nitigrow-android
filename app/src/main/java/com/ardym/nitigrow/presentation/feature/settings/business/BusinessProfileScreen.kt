package com.ardym.nitigrow.presentation.feature.settings.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

private val FormGutter = 20.dp
private val FieldSpacing = 12.dp
private val LogoSize = 96.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    onBack: () -> Unit,
    viewModel: BusinessProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
        topBar = {
            TopAppBar(
                title = { Text("Business profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        BusinessProfileForm(
            state = state,
            onLogoTap = viewModel::onLogoTap,
            onName = viewModel::onName,
            onAddress = viewModel::onAddress,
            onWebsite = viewModel::onWebsite,
            onEmail = viewModel::onEmail,
            onGstin = viewModel::onGstin,
            onSave = viewModel::save,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun BusinessProfileForm(
    state: BusinessProfileUiState,
    onLogoTap: () -> Unit,
    onName: (String) -> Unit,
    onAddress: (String) -> Unit,
    onWebsite: (String) -> Unit,
    onEmail: (String) -> Unit,
    onGstin: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FormGutter, vertical = FormGutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoPlaceholder(onClick = onLogoTap)
        Text(
            "Tap to change logo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(FormGutter))

        OutlinedTextField(
            value = state.name,
            onValueChange = onName,
            label = { Text("Business name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FieldSpacing))

        OutlinedTextField(
            value = state.address,
            onValueChange = onAddress,
            label = { Text("Address") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FieldSpacing))

        OutlinedTextField(
            value = state.website,
            onValueChange = onWebsite,
            label = { Text("Website") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FieldSpacing))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmail,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FieldSpacing))

        OutlinedTextField(
            value = state.gstin,
            onValueChange = onGstin,
            label = { Text("GSTIN") },
            supportingText = { Text("Format: 22AAAAA0000A1Z5") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let {
            Spacer(Modifier.height(FieldSpacing))
            ErrorBanner(message = it)
        }

        Spacer(Modifier.height(FormGutter))

        FilledTonalButton(
            onClick = onSave,
            enabled = !state.isSaving && state.name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Text("Save")
            }
        }
        Spacer(Modifier.height(FormGutter))
    }
}

@Composable
private fun LogoPlaceholder(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Theme.colors.brandSoft),
        modifier = Modifier.size(LogoSize)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Storefront,
                contentDescription = "Business logo",
                tint = Theme.colors.brand
            )
        }
    }
}

@Preview(showBackground = true, name = "Business profile — form")
@Composable
private fun PreviewBusinessProfileForm() {
    NitiGrowTheme {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            BusinessProfileForm(
                state = BusinessProfileUiState(
                    name = "Aarav Traders",
                    address = "MG Road, Pune, MH",
                    website = "aaravtraders.in",
                    email = "owner@aaravtraders.in",
                    gstin = "27AAACA1234A1Z5"
                ),
                onLogoTap = {},
                onName = {},
                onAddress = {},
                onWebsite = {},
                onEmail = {},
                onGstin = {},
                onSave = {}
            )
        }
    }
}
