package com.ardym.nitigrow.presentation.feature.campaigns.create

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.CampaignRepository
import com.ardym.nitigrow.domain.repository.ContactRepository
import com.ardym.nitigrow.domain.usecase.campaigns.CreateCampaignUseCase
import com.ardym.nitigrow.domain.usecase.campaigns.EstimateAudienceUseCase
import com.ardym.nitigrow.domain.usecase.campaigns.ObserveTemplatesUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateCampaignViewModel @Inject constructor(
    observeTemplates: ObserveTemplatesUseCase,
    private val campaignRepo: CampaignRepository,
    private val contactRepo: ContactRepository,
    private val createCampaign: CreateCampaignUseCase,
    private val estimate: EstimateAudienceUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(CreateCampaignUiState())
    val state: StateFlow<CreateCampaignUiState> = _state.asStateFlow()

    private val _effects = Channel<CreateCampaignEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observeTemplates()
            .onEach { tpls ->
                _state.update { it.copy(templates = tpls.filter { t -> t.status.equals("APPROVED", true) }) }
            }
            .launchIn(viewModelScope)

        // Derive available tags from cached contacts
        contactRepo.observeContacts()
            .onEach { contacts ->
                val tags = contacts.flatMap { it.tags }.distinct().sorted()
                _state.update { it.copy(availableTags = tags) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch { campaignRepo.refreshTemplates() }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun pickTemplate(id: String) = _state.update { it.copy(selectedTemplateId = id) }

    fun toggleTag(tag: String) {
        _state.update {
            val updated = if (tag in it.selectedTags) it.selectedTags - tag else it.selectedTags + tag
            it.copy(selectedTags = updated, audienceEstimate = null)
        }
        runEstimate()
    }

    private fun runEstimate() {
        viewModelScope.launch {
            val tags = _state.value.selectedTags.toList()
            if (tags.isEmpty()) {
                _state.update { it.copy(audienceEstimate = 0) }
                return@launch
            }
            _state.update { it.copy(isEstimating = true) }
            when (val res = estimate(tags)) {
                is ApiResult.Success -> _state.update {
                    it.copy(audienceEstimate = res.data, isEstimating = false)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isEstimating = false, error = res.message)
                }
            }
        }
    }

    fun setSendNow(now: Boolean) = _state.update { it.copy(sendNow = now, scheduledAt = if (now) null else it.scheduledAt) }
    fun setScheduledAt(instant: Instant) = _state.update { it.copy(scheduledAt = instant) }

    fun next() {
        val s = _state.value
        if (!s.canNext) return
        val nextStep = WizardStep.entries[s.step.ordinal + 1.coerceAtMost(WizardStep.entries.lastIndex - s.step.ordinal)]
        _state.update { it.copy(step = nextStep) }
    }

    fun back() {
        val s = _state.value
        if (s.step.ordinal == 0) return
        _state.update { it.copy(step = WizardStep.entries[s.step.ordinal - 1]) }
    }

    fun submit() {
        viewModelScope.launch {
            val s = _state.value
            _state.update { it.copy(isSubmitting = true, error = null) }
            val res = createCampaign(
                name = s.name,
                templateId = s.selectedTemplateId.orEmpty(),
                audienceTags = s.selectedTags.toList(),
                scheduledAt = if (s.sendNow) null else s.scheduledAt
            )
            _state.update { it.copy(isSubmitting = false) }
            when (res) {
                is ApiResult.Success -> _effects.send(CreateCampaignEffect.Created)
                is ApiResult.Error -> {
                    _state.update { it.copy(error = res.message) }
                    _effects.send(CreateCampaignEffect.ShowError(res.message))
                }
            }
        }
    }
}
