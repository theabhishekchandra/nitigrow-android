package com.websbaba.nitigrow.data.mapper

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.websbaba.nitigrow.data.remote.dto.LeadDto
import com.websbaba.nitigrow.domain.model.LeadStage
import org.junit.Test

class LeadMapperTest {

    private val gson = Gson()

    /** A lead exactly as api.nitigrow.in returns it. */
    private val serverJson = """
        {"_id":"l1","tenantId":"t","contactId":"c1","name":"Karan Sharma","phone":"919650015792",
         "channel":"whatsapp","stage":"warm","value":51401,"source":"website",
         "assignedTo":{"_id":"u1","name":"NitiGrow Demo"},"tags":["vip"],"isTest":false,
         "createdAt":"2026-09-03T17:21:46.675Z","updatedAt":"2026-09-21T17:21:46.679Z"}
    """.trimIndent()

    @Test
    fun `server payload maps name, phone, value and owner`() {
        val e = gson.fromJson(serverJson, LeadDto::class.java).toEntity()

        assertThat(e.contactName).isEqualTo("Karan Sharma")
        assertThat(e.contactPhone).isEqualTo("919650015792")
        assertThat(e.valueInr).isEqualTo(51401L)
        assertThat(e.ownerName).isEqualTo("NitiGrow Demo")
        assertThat(e.source).isEqualTo("website")
        assertThat(e.contactId).isEqualTo("c1")
    }

    @Test
    fun `legacy flat payload still maps`() {
        val json = """{"_id":"l2","contactId":"c","contactName":"Old","contactPhone":"+91 1","source":"s",
            "stage":"QUALIFIED","valueInr":900,"ownerName":"Anita","createdAt":"2026-09-01T00:00:00Z","updatedAt":"2026-09-02T00:00:00Z"}"""
        val e = gson.fromJson(json, LeadDto::class.java).toEntity()

        assertThat(e.contactName).isEqualTo("Old")
        assertThat(e.valueInr).isEqualTo(900L)
        assertThat(e.ownerName).isEqualTo("Anita")
        assertThat(e.stage).isEqualTo("QUALIFIED")
    }

    @Test
    fun `a bare lead does not crash`() {
        val e = gson.fromJson("""{"_id":"l3"}""", LeadDto::class.java).toEntity()

        assertThat(e.contactName).isEmpty()
        assertThat(e.valueInr).isEqualTo(0L)
        assertThat(e.ownerName).isNull()
        assertThat(e.stage).isEqualTo("NEW")
    }

    @Test
    fun `backend stages read as the closest app stage`() {
        assertThat(LeadStage.fromBackend("new")).isEqualTo(LeadStage.NEW)
        assertThat(LeadStage.fromBackend("warm")).isEqualTo(LeadStage.CONTACTED)
        assertThat(LeadStage.fromBackend("hot")).isEqualTo(LeadStage.QUALIFIED)
        assertThat(LeadStage.fromBackend("won")).isEqualTo(LeadStage.WON)
        assertThat(LeadStage.fromBackend("LOST")).isEqualTo(LeadStage.LOST)
        assertThat(LeadStage.fromBackend(null)).isEqualTo(LeadStage.NEW)
        assertThat(LeadStage.fromBackend("mystery")).isEqualTo(LeadStage.NEW)
    }
}
