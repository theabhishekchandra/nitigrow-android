package com.websbaba.nitigrow.presentation.feature.leads.components

import androidx.compose.ui.graphics.Color
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.core.ui.theme.NitiColors
import com.websbaba.nitigrow.core.ui.theme.NitiTone

/** Stages shown along the pipeline stepper, in order. LOST is an exit, not a step. */
internal val PipelineStages: List<LeadStage> =
    listOf(LeadStage.NEW, LeadStage.CONTACTED, LeadStage.QUALIFIED, LeadStage.PROPOSAL, LeadStage.WON)

/** Pill colours for a stage; WON is the one solid, celebratory fill. */
internal fun stageTone(stage: LeadStage, c: NitiColors): NitiTone = when (stage) {
    LeadStage.NEW -> c.infoTone
    LeadStage.CONTACTED -> c.secondaryTone
    LeadStage.QUALIFIED -> c.primaryTone
    LeadStage.PROPOSAL -> c.tertiaryTone
    LeadStage.WON -> NitiTone(c.primary, c.onPrimary)
    LeadStage.LOST -> c.errorTone
}

/** Small dot beside a pipeline column's name. */
internal fun stageDot(stage: LeadStage, c: NitiColors): Color = when (stage) {
    LeadStage.NEW -> c.read
    LeadStage.CONTACTED -> c.chartSecondary
    LeadStage.QUALIFIED -> c.primary
    LeadStage.PROPOSAL -> c.badge
    LeadStage.WON -> c.primary
    LeadStage.LOST -> c.outline
}

internal enum class StepState { DONE, CURRENT, UPCOMING }

/**
 * State of pipeline [step] for a lead at [current]. A lost lead has left the
 * pipeline, so nothing is marked done or current.
 */
internal fun stepState(current: LeadStage, step: LeadStage): StepState {
    if (current == LeadStage.LOST) return StepState.UPCOMING
    val at = PipelineStages.indexOf(current)
    val idx = PipelineStages.indexOf(step)
    return when {
        idx < at -> StepState.DONE
        idx == at -> StepState.CURRENT
        else -> StepState.UPCOMING
    }
}
