package com.websbaba.nitigrow.presentation.feature.settings.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.settings.components.FieldLabel
import com.websbaba.nitigrow.presentation.feature.settings.components.NgTextField
import com.websbaba.nitigrow.presentation.feature.settings.components.PrimaryCta
import com.websbaba.nitigrow.presentation.feature.settings.components.SubScreenHeader
import com.websbaba.nitigrow.core.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────────────────
// ProfileEditScreen — Settings ▸ profile card ▸ edit. Not in the prototype;
// styled with the same back-header / field / CTA language as its siblings.
// ─────────────────────────────────────────────────────────────────────────────

private val FieldSpacing = 14.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Theme.colors
    val context = LocalContext.current

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val cr = context.contentResolver
        val mime = cr.getType(uri) ?: "image/jpeg"
        val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
            ?: return@rememberLauncherForActivityResult
        viewModel.upload(bytes, mime)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) { ProfileEditEffect.Saved -> onBack() }
        }
    }

    Scaffold(
        containerColor = colors.paper,
        topBar = { SubScreenHeader(title = "Profile", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.clickable {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Avatar(name = state.name.ifBlank { "?" }, url = state.avatarUrl, sizeDp = 96)
                if (state.uploading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            Text(
                "Tap photo to change",
                fontSize = 11.5.sp,
                color = colors.muted,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                FieldLabel("Name")
                NgTextField(value = state.name, onValueChange = viewModel::onName)
                Spacer(Modifier.height(FieldSpacing))

                FieldLabel("Email")
                NgTextField(value = state.email, onValueChange = viewModel::onEmail)

                state.error?.let {
                    Spacer(Modifier.height(FieldSpacing))
                    ErrorBanner(message = it)
                }
                Spacer(Modifier.height(20.dp))

                PrimaryCta(
                    text = "Save changes",
                    onClick = viewModel::save,
                    loading = state.saving,
                    enabled = state.name.isNotBlank() && state.email.isNotBlank()
                )
            }
        }
    }
}
