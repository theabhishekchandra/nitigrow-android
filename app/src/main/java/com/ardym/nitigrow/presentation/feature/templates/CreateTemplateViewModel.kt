package com.ardym.nitigrow.presentation.feature.templates

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTemplateViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow(CreateTemplateUiState())
    val state: StateFlow<CreateTemplateUiState> = _state.asStateFlow()

    /** One-shot UI events (toasts / snackbars / nav-back signals). */
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setLanguage(v: TemplateLanguage) = _state.update { it.copy(language = v) }
    fun setCategory(v: TemplateCategory) = _state.update { it.copy(category = v) }

    fun setBody(v: String) {
        // Hard-cap at the Meta limit so the OutlinedTextField can't visually overflow either.
        val trimmed = if (v.length > TEMPLATE_BODY_MAX_LENGTH) v.substring(0, TEMPLATE_BODY_MAX_LENGTH) else v
        _state.update { it.copy(body = trimmed) }
    }

    fun next() {
        _state.update { s ->
            val nextStep = when (s.step) {
                CreateStep.DETAILS -> if (s.canAdvanceFromDetails) CreateStep.BODY else CreateStep.DETAILS
                CreateStep.BODY -> if (s.canAdvanceFromBody) CreateStep.PREVIEW else CreateStep.BODY
                CreateStep.PREVIEW -> CreateStep.PREVIEW
            }
            s.copy(step = nextStep)
        }
    }

    fun back() {
        _state.update { s ->
            val prev = when (s.step) {
                CreateStep.DETAILS -> CreateStep.DETAILS
                CreateStep.BODY -> CreateStep.DETAILS
                CreateStep.PREVIEW -> CreateStep.BODY
            }
            s.copy(step = prev)
        }
    }

    /**
     * Fake submit — emits a toast event after a tiny delay so the UI feels real.
     * TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
     */
    fun submit() {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        if (!_state.value.canSubmit) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            // No real network call — just surface the success toast immediately.
            _events.emit("Submitted for Meta approval")
            _state.update { it.copy(isSubmitting = false) }
        }
    }
}
