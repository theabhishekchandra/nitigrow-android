package com.websbaba.nitigrow.presentation.feature.conflicts

import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// ConflictViewModel — drives the offline-sync conflict resolution screen.
// Spec: phase-3-mobile.md Section 4.2 "Conflict Resolution Rules".
//
// The offline-queue + server-side conflict detector do not exist yet (there is
// no backend sync/conflicts endpoint), so this screen has no real source to
// observe. It therefore starts empty and renders the "No conflicts" state.
//
// When the conflict detector lands, inject a ConflictRepository here, observe
// its flow into [_state], and route [resolve] to its
// POST /api/sync/conflicts/{id}/resolve call.
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class ConflictViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow(ConflictUiState())
    val state: StateFlow<ConflictUiState> = _state.asStateFlow()

    /**
     * Resolve a single conflict by the user-picked [action].
     *
     * With no backend conflict source there is nothing to resolve yet; this
     * only drops the item locally so the UI stays consistent if a conflict is
     * ever injected (e.g. from a preview). Wire to the repository call once the
     * sync engine exists.
     */
    fun resolve(id: String, @Suppress("UNUSED_PARAMETER") action: ConflictAction) {
        _state.update { current ->
            current.copy(conflicts = current.conflicts.filterNot { it.id == id })
        }
    }
}
