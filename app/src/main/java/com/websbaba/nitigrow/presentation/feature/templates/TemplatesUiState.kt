package com.websbaba.nitigrow.presentation.feature.templates

import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// Templates UI state
//
// We deliberately keep the Template domain type local to the templates feature
// for now — there is no shared `domain/model/Template.kt` yet, and the parent
// agent will lift this into a proper domain model when the real Meta WABA
// template API is wired in. Until then this struct is the single source of
// truth that the screen, the create-form, and DummyTemplatesData all consume.
//
// `filtered` is a *computed* property so the screen can simply observe state
// and re-render — no need for a ViewModel-side filter pipeline.
// ─────────────────────────────────────────────────────────────────────────────

/** Meta-approval lifecycle for a WhatsApp message template. */
enum class TemplateStatus { APPROVED, PENDING, REJECTED }

/** Meta-defined template category — drives pricing + approval bar. */
enum class TemplateCategory { MARKETING, UTILITY, AUTHENTICATION }

/** ISO 639-1 language code surfaced as a 2-letter pill on the template card. */
enum class TemplateLanguage(val code: String, val label: String) {
    EN("en", "EN"),
    HI("hi", "HI"),
    MR("mr", "MR"),
}

/** Filter chips above the template list. */
enum class TemplateFilter { ALL, APPROVED, PENDING, REJECTED }

/**
 * Local Template type for the templates feature.
 *
 * `body` may contain `{{1}}` / `{{2}}` Meta placeholders verbatim — the preview
 * surface renders them as-is so authors see exactly what Meta will approve.
 */
data class Template(
    val id: String,
    val name: String,
    val language: TemplateLanguage,
    val category: TemplateCategory,
    val status: TemplateStatus,
    val body: String,
    val rejectionReason: String? = null,
    val updatedAt: Instant,
)

data class TemplatesUiState(
    val templates: List<Template> = emptyList(),
    val filter: TemplateFilter = TemplateFilter.ALL,
    val isRefreshing: Boolean = false,
    val error: String? = null,
) {
    /** Convenience accessor — filters `templates` by `filter` without allocating extra state. */
    val filtered: List<Template>
        get() = when (filter) {
            TemplateFilter.ALL -> templates
            TemplateFilter.APPROVED -> templates.filter { it.status == TemplateStatus.APPROVED }
            TemplateFilter.PENDING -> templates.filter { it.status == TemplateStatus.PENDING }
            TemplateFilter.REJECTED -> templates.filter { it.status == TemplateStatus.REJECTED }
        }
}
