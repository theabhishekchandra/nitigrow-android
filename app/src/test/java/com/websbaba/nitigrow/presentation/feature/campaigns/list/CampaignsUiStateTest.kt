package com.websbaba.nitigrow.presentation.feature.campaigns.list

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.presentation.feature.campaigns.list.components.campaignProgress
import org.junit.Test
import java.time.Instant

class CampaignsUiStateTest {

    private fun campaign(
        id: String,
        status: CampaignStatus,
        name: String = "Broadcast $id",
        template: String = "tpl_$id",
        audience: Int = 100,
        sent: Long = 0,
    ) = Campaign(
        id = id,
        name = name,
        templateId = "t-$id",
        templateName = template,
        audienceTags = emptyList(),
        audienceSize = audience,
        status = status,
        scheduledAt = null,
        sentCount = sent,
        deliveredCount = 0,
        readCount = 0,
        failedCount = 0,
        createdAt = Instant.EPOCH
    )

    private val items = listOf(
        campaign("1", CampaignStatus.RUNNING, name = "Diwali Early Bird", template = "diwali_offer"),
        campaign("2", CampaignStatus.SCHEDULED, name = "Navratri Thali", template = "catering_quote"),
        campaign("3", CampaignStatus.COMPLETED),
        campaign("4", CampaignStatus.DRAFT),
        campaign("5", CampaignStatus.FAILED),
    )

    @Test
    fun `each chip selects its own statuses and All includes failed ones`() {
        val base = CampaignsUiState(items = items)

        assertThat(base.visibleItems.map { it.id }).containsExactly("1", "2", "3", "4", "5")
        assertThat(base.copy(filter = BroadcastFilter.SENDING).visibleItems.map { it.id }).containsExactly("1")
        assertThat(base.copy(filter = BroadcastFilter.SCHEDULED).visibleItems.map { it.id }).containsExactly("2")
        assertThat(base.copy(filter = BroadcastFilter.COMPLETED).visibleItems.map { it.id }).containsExactly("3")
        assertThat(base.copy(filter = BroadcastFilter.DRAFT).visibleItems.map { it.id }).containsExactly("4")
    }

    @Test
    fun `search matches name or template case-insensitively and combines with the chip`() {
        val base = CampaignsUiState(items = items)

        assertThat(base.copy(query = "diwali").visibleItems.map { it.id }).containsExactly("1")
        assertThat(base.copy(query = "CATERING").visibleItems.map { it.id }).containsExactly("2")
        assertThat(base.copy(query = "  ").visibleItems).hasSize(5)
        assertThat(base.copy(query = "diwali", filter = BroadcastFilter.DRAFT).visibleItems).isEmpty()
    }

    @Test
    fun `sending and scheduled counts feed the chips`() {
        val state = CampaignsUiState(items = items)

        assertThat(state.sendingCount).isEqualTo(1)
        assertThat(state.scheduledCount).isEqualTo(1)
    }

    @Test
    fun `subtitle pluralises and mentions sending only when something is`() {
        assertThat(subtitle(total = 5, sending = 1)).isEqualTo("5 broadcasts · 1 sending now")
        assertThat(subtitle(total = 1, sending = 0)).isEqualTo("1 broadcast")
        assertThat(subtitle(total = 0, sending = 0)).isEqualTo("0 broadcasts")
    }

    @Test
    fun `progress is sent over audience, clamped, and zero for an empty audience`() {
        assertThat(campaignProgress(campaign("a", CampaignStatus.RUNNING, audience = 1240, sent = 620))).isEqualTo(0.5f)
        assertThat(campaignProgress(campaign("b", CampaignStatus.RUNNING, audience = 100, sent = 250))).isEqualTo(1f)
        assertThat(campaignProgress(campaign("c", CampaignStatus.RUNNING, audience = 0, sent = 10))).isEqualTo(0f)
    }
}
