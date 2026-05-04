package com.ardym.nitigrow.presentation.feature.campaigns.create

import com.ardym.nitigrow.domain.model.Template
import java.time.Instant

enum class WizardStep { TEMPLATE, AUDIENCE, SCHEDULE, REVIEW }

data class CreateCampaignUiState(
    val step: WizardStep = WizardStep.TEMPLATE,
    val name: String = "",
    val templates: List<Template> = emptyList(),
    val selectedTemplateId: String? = null,
    val availableTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val audienceEstimate: Int? = null,
    val isEstimating: Boolean = false,
    val sendNow: Boolean = true,
    val scheduledAt: Instant? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val canNext: Boolean get() = when (step) {
        WizardStep.TEMPLATE -> name.isNotBlank() && selectedTemplateId != null
        WizardStep.AUDIENCE -> selectedTags.isNotEmpty() && (audienceEstimate ?: 0) > 0
        WizardStep.SCHEDULE -> sendNow || scheduledAt != null
        WizardStep.REVIEW -> true
    }
}

sealed interface CreateCampaignEffect {
    data object Created : CreateCampaignEffect
    data class ShowError(val message: String) : CreateCampaignEffect
}
