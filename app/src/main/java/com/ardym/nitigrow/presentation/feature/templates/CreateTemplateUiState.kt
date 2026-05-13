package com.ardym.nitigrow.presentation.feature.templates

/** Multi-step wizard step for the template-create flow. */
enum class CreateStep { DETAILS, BODY, PREVIEW }

/** Maximum allowed length for the body field — matches Meta's WhatsApp template policy. */
const val TEMPLATE_BODY_MAX_LENGTH: Int = 1024

data class CreateTemplateUiState(
    val step: CreateStep = CreateStep.DETAILS,
    val name: String = "",
    val language: TemplateLanguage = TemplateLanguage.EN,
    val category: TemplateCategory = TemplateCategory.MARKETING,
    val body: String = "",
    val isSubmitting: Boolean = false,
) {
    /** Step 1 is valid when name is non-blank, snake_case-ish, and category is chosen. */
    val canAdvanceFromDetails: Boolean
        get() = name.trim().length in 3..50 && name.none { it.isWhitespace() }

    /** Step 2 requires non-empty body within Meta's character ceiling. */
    val canAdvanceFromBody: Boolean
        get() = body.trim().isNotEmpty() && body.length <= TEMPLATE_BODY_MAX_LENGTH

    /** Submit button enabled only when both prior steps are valid + not already submitting. */
    val canSubmit: Boolean
        get() = !isSubmitting && canAdvanceFromDetails && canAdvanceFromBody
}
