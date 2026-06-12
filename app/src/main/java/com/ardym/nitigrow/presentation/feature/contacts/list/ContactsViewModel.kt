package com.ardym.nitigrow.presentation.feature.contacts.list

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Contact
import com.ardym.nitigrow.domain.usecase.contacts.ImportCsvContactsUseCase
import com.ardym.nitigrow.domain.usecase.contacts.ObserveContactsUseCase
import com.ardym.nitigrow.domain.usecase.contacts.RefreshContactsUseCase
import com.ardym.nitigrow.domain.usecase.contacts.SaveContactUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ContactsViewModel @Inject constructor(
    observe: ObserveContactsUseCase,
    private val refresh: RefreshContactsUseCase,
    private val save: SaveContactUseCase,
    private val importCsv: ImportCsvContactsUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(ContactsUiState(isRefreshing = true))
    val state: StateFlow<ContactsUiState> = _state.asStateFlow()

    private val _effects = Channel<ContactsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _query = MutableStateFlow("")

    init {
        _query
            .debounce { if (it.isEmpty()) 0L else 300L }
            .distinctUntilChanged()
            .flatMapLatest { observe(it) }
            .onEach { items -> _state.update { it.copy(items = items) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        _query.value = value
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = refresh.invoke()) {
                is ApiResult.Success -> _state.update {
                    it.copy(isRefreshing = false, lastSyncedAt = Instant.now())
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }

    fun openCreate() = _state.update { it.copy(sheetOpen = true, editing = null, sheetError = null) }
    fun openEdit(c: Contact) = _state.update { it.copy(sheetOpen = true, editing = c, sheetError = null) }
    fun closeSheet() = _state.update { it.copy(sheetOpen = false, editing = null, sheetError = null) }

    fun saveSheet(name: String, phone: String, email: String?, tags: List<String>) {
        viewModelScope.launch {
            _state.update { it.copy(savingSheet = true, sheetError = null) }
            val id = _state.value.editing?.id
            when (val res = save(id, name, phone, email, tags)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(savingSheet = false, sheetOpen = false, editing = null) }
                    _effects.send(ContactsEffect.ShowMessage(if (id == null) "Contact added" else "Contact updated"))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(savingSheet = false, sheetError = res.message)
                }
            }
        }
    }

    fun importFromCsv(text: String) {
        viewModelScope.launch {
            when (val res = importCsv(text)) {
                is ApiResult.Success -> _effects.send(
                    ContactsEffect.ShowMessage("Imported ${res.data} contacts")
                )
                is ApiResult.Error -> _effects.send(ContactsEffect.ShowMessage(res.message))
            }
        }
    }
}
