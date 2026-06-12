package com.ardym.nitigrow.presentation.feature.campaigns.create

import com.ardym.nitigrow.domain.model.Template
import java.time.Instant

// Step order matches the design wizard: Audience → Template → Schedule → Review.
enum class WizardStep { AUDIENCE, TEMPLATE, SCHEDULE, REVIEW }

data class CreateCampaignUiState(
    val step: WizardStep = WizardStep.AUDIENCE,
    val name: String = "",
    val templates: List<Template> = emptyList(),
    val selectedTemplateId: String? = null,
    val availableTags: List<String> = emptyList(),
    /** Contacts per tag, derived from the cached contact list (display only). */
    val tagCounts: Map<String, Int> = emptyMap(),
    val selectedTags: Set<String> = emptySet(),
    val audienceEstimate: Int? = null,
    val isEstimating: Boolean = false,
    val sendNow: Boolean = true,
    val scheduledAt: Instant? = null,
    val isSubmitting: Boolean = false,
    /** True once the broadcast was accepted by the backend → show the success screen. */
    val queued: Boolean = false,
    val createdCampaignId: String? = null,
    val error: String? = null
) {
    val selectedTemplate: Template? get() = templates.firstOrNull { it.id == selectedTemplateId }

    val canNext: Boolean get() = when (step) {
        WizardStep.AUDIENCE -> selectedTags.isNotEmpty() && (audienceEstimate ?: 0) > 0
        WizardStep.TEMPLATE -> name.isNotBlank() && selectedTemplateId != null
        WizardStep.SCHEDULE -> sendNow || scheduledAt != null
        WizardStep.REVIEW -> true
    }
}

sealed interface CreateCampaignEffect {
    data class ShowError(val message: String) : CreateCampaignEffect
}
