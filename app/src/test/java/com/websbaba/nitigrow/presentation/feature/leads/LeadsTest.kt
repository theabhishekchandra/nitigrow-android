package com.websbaba.nitigrow.presentation.feature.leads

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.presentation.feature.leads.components.PipelineStages
import com.websbaba.nitigrow.presentation.feature.leads.components.StepState
import com.websbaba.nitigrow.presentation.feature.leads.components.formatInrCompact
import com.websbaba.nitigrow.presentation.feature.leads.components.leadMeta
import com.websbaba.nitigrow.core.util.relativeTime
import com.websbaba.nitigrow.presentation.feature.leads.components.stageDot
import com.websbaba.nitigrow.presentation.feature.leads.components.stageTone
import com.websbaba.nitigrow.presentation.feature.leads.components.stepState
import com.websbaba.nitigrow.core.ui.theme.NitiLightColors
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class LeadsUiStateTest {

    private fun lead(id: String, name: String, stage: LeadStage, phone: String = "+9199000000$id", source: String = "Referral") = Lead(
        id = id, contactId = "c$id", contactName = name, contactPhone = phone, source = source, stage = stage,
        valueInr = 1000, ownerName = null, notes = null, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH
    )

    private val leads = listOf(
        lead("1", "Priya Sharma", LeadStage.PROPOSAL, source = "WhatsApp inbound"),
        lead("2", "Vikram Singh", LeadStage.QUALIFIED, source = "Website form"),
        lead("3", "Kavita Joshi", LeadStage.NEW),
        lead("4", "Rahul Verma", LeadStage.WON),
        lead("5", "Suresh Kumar", LeadStage.LOST),
    )

    @Test
    fun `open leads exclude won and lost`() {
        assertThat(LeadsUiState(leads = leads).openCount).isEqualTo(3)
    }

    @Test
    fun `stage chip narrows the list`() {
        val state = LeadsUiState(leads = leads, stageFilter = LeadStage.QUALIFIED)
        assertThat(state.visibleLeads.map { it.id }).containsExactly("2")
        assertThat(state.countIn(LeadStage.NEW)).isEqualTo(1)
        assertThat(state.countIn(LeadStage.CONTACTED)).isEqualTo(0)
    }

    @Test
    fun `search matches name, phone or source and combines with the chip`() {
        val base = LeadsUiState(leads = leads)
        assertThat(base.copy(query = "priya").visibleLeads.map { it.id }).containsExactly("1")
        assertThat(base.copy(query = "website").visibleLeads.map { it.id }).containsExactly("2")
        assertThat(base.copy(query = "99000000 3".replace(" ", "")).visibleLeads.map { it.id }).containsExactly("3")
        assertThat(base.copy(query = "priya", stageFilter = LeadStage.WON).visibleLeads).isEmpty()
        assertThat(base.copy(query = "   ").visibleLeads).hasSize(5)
    }

    @Test
    fun `subtitle pluralises`() {
        assertThat(leadsSubtitle(148, 61)).isEqualTo("148 leads · 61 open")
        assertThat(leadsSubtitle(1, 1)).isEqualTo("1 lead · 1 open")
    }
}

class LeadStageStyleTest {

    @Test
    fun `stepper marks earlier stages done, the current one current, the rest upcoming`() {
        val states = PipelineStages.map { stepState(LeadStage.QUALIFIED, it) }
        assertThat(states).containsExactly(
            StepState.DONE, StepState.DONE, StepState.CURRENT, StepState.UPCOMING, StepState.UPCOMING
        ).inOrder()
    }

    @Test
    fun `a won lead completes every earlier step and a lost lead completes none`() {
        assertThat(PipelineStages.map { stepState(LeadStage.WON, it) }.last()).isEqualTo(StepState.CURRENT)
        assertThat(PipelineStages.map { stepState(LeadStage.WON, it) }.count { it == StepState.DONE }).isEqualTo(4)
        assertThat(PipelineStages.map { stepState(LeadStage.LOST, it) }.all { it == StepState.UPCOMING }).isTrue()
    }

    @Test
    fun `every stage has a tone and a dot, and won is the solid one`() {
        val c = NitiLightColors
        LeadStage.entries.forEach {
            assertThat(stageTone(it, c)).isNotNull()
            assertThat(stageDot(it, c)).isNotNull()
        }
        assertThat(stageTone(LeadStage.WON, c).container).isEqualTo(c.primary)
        assertThat(stageTone(LeadStage.LOST, c)).isEqualTo(c.errorTone)
    }
}

class LeadFormattersTest {

    private val now = Instant.parse("2026-09-21T10:00:00Z")

    @Test
    fun `relative time uses minutes, hours, days, weeks and months`() {
        assertThat(relativeTime(now.minusSeconds(10), now)).isEqualTo("just now")
        assertThat(relativeTime(now.minus(5, ChronoUnit.MINUTES), now)).isEqualTo("5m ago")
        assertThat(relativeTime(now.minus(2, ChronoUnit.HOURS), now)).isEqualTo("2h ago")
        assertThat(relativeTime(now.minus(3, ChronoUnit.DAYS), now)).isEqualTo("3d ago")
        assertThat(relativeTime(now.minus(14, ChronoUnit.DAYS), now)).isEqualTo("2w ago")
        assertThat(relativeTime(now.minus(65, ChronoUnit.DAYS), now)).isEqualTo("2mo ago")
        assertThat(relativeTime(now.plusSeconds(60), now)).isEqualTo("just now")
    }

    @Test
    fun `lead meta joins source, owner or Unassigned, and age`() {
        val at = now.minus(2, ChronoUnit.HOURS)
        assertThat(leadMeta("WhatsApp inbound", "Anita", at, now)).isEqualTo("WhatsApp inbound · Anita · 2h ago")
        assertThat(leadMeta("Referral", null, at, now)).isEqualTo("Referral · Unassigned · 2h ago")
        assertThat(leadMeta("", " ", at, now)).isEqualTo("Unassigned · 2h ago")
    }

    @Test
    fun `compact rupees switch to lakhs at one lakh`() {
        assertThat(formatInrCompact(140_000)).isEqualTo("₹1.4L")
        assertThat(formatInrCompact(95_000)).startsWith("₹")
        assertThat(formatInrCompact(95_000)).doesNotContain("L")
    }
}

/**
 * The backend's stage enum is only `new/warm/hot/won/lost` (backend/src/models/Lead.js) and
 * its PATCH /leads/:id/stage rejects anything else with a 400 — so [LeadStage.toBackend] has
 * to land on one of those five, and round-tripping every stage through [LeadStage.fromBackend]
 * must not drop or invent one.
 */
class LeadStageWireFormatTest {

    @Test
    fun `every stage encodes to one of the backend's five values`() {
        val backendVocabulary = setOf("new", "warm", "hot", "won", "lost")
        LeadStage.entries.forEach { stage ->
            assertThat(stage.toBackend()).isIn(backendVocabulary)
        }
    }

    @Test
    fun `stage moves send lowercase backend words, not the enum's own name`() {
        assertThat(LeadStage.NEW.toBackend()).isEqualTo("new")
        assertThat(LeadStage.CONTACTED.toBackend()).isEqualTo("warm")
        assertThat(LeadStage.QUALIFIED.toBackend()).isEqualTo("hot")
        assertThat(LeadStage.WON.toBackend()).isEqualTo("won")
        assertThat(LeadStage.LOST.toBackend()).isEqualTo("lost")
    }

    @Test
    fun `PROPOSAL has no backend slot of its own, so it folds into the same bucket as QUALIFIED`() {
        assertThat(LeadStage.PROPOSAL.toBackend()).isEqualTo(LeadStage.QUALIFIED.toBackend())
    }

    @Test
    fun `a stage read back from the backend re-encodes to the same wire value`() {
        listOf("new", "warm", "hot", "won", "lost").forEach { wire ->
            assertThat(LeadStage.fromBackend(wire).toBackend()).isEqualTo(wire)
        }
    }
}
