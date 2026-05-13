package com.ardym.nitigrow.presentation.feature.conflicts

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// ConflictViewModel — drives the offline-sync conflict resolution screen.
// Spec: phase-3-mobile.md Section 4.2 "Conflict Resolution Rules".
//
// Today the conflicts come from [DummyConflictData]; once the offline-queue
// + server-side conflict detector are wired this VM will observe a real
// repository flow.
// ─────────────────────────────────────────────────────────────────────────────

private const val RESOLVE_DELAY_MS = 400L

@HiltViewModel
class ConflictViewModel @Inject constructor() : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    // Replace with a flow from a ConflictRepository observing the local Room
    // table that the sync worker writes into when it detects divergence.
    private val _state = MutableStateFlow(
        ConflictUiState(conflicts = DummyConflictData.recent())
    )
    val state: StateFlow<ConflictUiState> = _state.asStateFlow()

    /**
     * Resolve a single conflict by the user-picked [action]. We simulate a
     * short network round-trip and then remove the conflict from the list.
     *
     * TODO: replace the delay with the real repository call. The repo should
     *   - POST `/api/sync/conflicts/{id}/resolve` with action,
     *   - update the local cache,
     *   - emit a new ConflictUiState from its flow.
     */
    fun resolve(id: String, action: ConflictAction) {
        viewModelScope.launch {
            _state.update { it.copy(isResolving = true) }
            // TODO: send `action` to the backend.
            delay(RESOLVE_DELAY_MS)
            _state.update { current ->
                current.copy(
                    isResolving = false,
                    conflicts = current.conflicts.filterNot { it.id == id },
                )
            }
            // `action` is intentionally unused at the moment — kept in the
            // signature so the screen wires the real intent today.
            @Suppress("UNUSED_VARIABLE") val pickedAction = action
        }
    }
}
