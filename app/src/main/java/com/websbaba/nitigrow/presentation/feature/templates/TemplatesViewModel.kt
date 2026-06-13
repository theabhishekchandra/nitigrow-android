package com.websbaba.nitigrow.presentation.feature.templates

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.MessageTemplate
import com.websbaba.nitigrow.domain.repository.TemplatesRepository
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val repo: TemplatesRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(TemplatesUiState(isRefreshing = true))
    val state: StateFlow<TemplatesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val r = repo.list()) {
                is ApiResult.Success -> _state.update {
                    it.copy(templates = r.data.map(::toUi), isRefreshing = false)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = r.message)
                }
            }
        }
    }

    /** Switch the active status filter (chip row on TemplatesScreen). */
    fun setFilter(f: TemplateFilter) {
        _state.update { it.copy(filter = f) }
    }

    private fun toUi(t: MessageTemplate): Template = Template(
        id = t.id,
        name = t.name,
        language = when (t.language.lowercase()) {
            "hi" -> TemplateLanguage.HI
            "mr" -> TemplateLanguage.MR
            else -> TemplateLanguage.EN
        },
        category = when (t.category.uppercase()) {
            "UTILITY" -> TemplateCategory.UTILITY
            "AUTHENTICATION" -> TemplateCategory.AUTHENTICATION
            else -> TemplateCategory.MARKETING
        },
        status = when (t.status.uppercase()) {
            "APPROVED" -> TemplateStatus.APPROVED
            "REJECTED" -> TemplateStatus.REJECTED
            else -> TemplateStatus.PENDING // PENDING / PAUSED → pending bucket
        },
        body = t.body,
        rejectionReason = t.rejectionReason,
        updatedAt = t.updatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.now()
    )
}
