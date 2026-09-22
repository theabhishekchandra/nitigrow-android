package com.websbaba.nitigrow.data.mapper

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.websbaba.nitigrow.data.remote.dto.CampaignDto
import org.junit.Test

class CampaignMapperTest {

    private val gson = Gson()

    /** A campaign exactly as api.nitigrow.in returns it (nested audience and stats). */
    private val serverJson = """
        {
          "audience": {"type": "tag", "tags": ["lead"], "contactIds": []},
          "stats": {"total": 181, "sent": 177, "delivered": 173, "read": 91, "replied": 11, "failed": 4},
          "_id": "c1", "name": "Monsoon Service Reminder",
          "templateId": "t1", "templateName": "order_confirmed",
          "status": "completed", "createdAt": "2026-09-15T17:21:46.668Z"
        }
    """.trimIndent()

    @Test
    fun `nested server payload maps audience and counters`() {
        val e = gson.fromJson(serverJson, CampaignDto::class.java).toEntity()

        assertThat(e.audienceTagsCsv).isEqualTo("lead")
        assertThat(e.audienceSize).isEqualTo(181)
        assertThat(e.sentCount).isEqualTo(177)
        assertThat(e.deliveredCount).isEqualTo(173)
        assertThat(e.readCount).isEqualTo(91)
        assertThat(e.failedCount).isEqualTo(4)
        assertThat(e.status).isEqualTo("COMPLETED")
        assertThat(e.scheduledAtEpochMs).isNull()
    }

    @Test
    fun `legacy flat payload still maps`() {
        val json = """
            {"_id":"c2","name":"Old","templateId":"t","templateName":"n","audienceTags":["vip","new"],
             "audienceSize":50,"status":"scheduled","scheduledAt":"2026-10-10T03:30:00Z",
             "sentCount":10,"deliveredCount":9,"readCount":8,"failedCount":1,"createdAt":"2026-09-01T00:00:00Z"}
        """.trimIndent()
        val e = gson.fromJson(json, CampaignDto::class.java).toEntity()

        assertThat(e.audienceTagsCsv).isEqualTo("vip|new")
        assertThat(e.audienceSize).isEqualTo(50)
        assertThat(e.sentCount).isEqualTo(10)
        assertThat(e.scheduledAtEpochMs).isNotNull()
    }

    @Test
    fun `a bare campaign with no audience or stats does not crash and reads as empty`() {
        val e = gson.fromJson("""{"_id":"c3","status":"draft"}""", CampaignDto::class.java).toEntity()

        assertThat(e.audienceTagsCsv).isEmpty()
        assertThat(e.audienceSize).isEqualTo(0)
        assertThat(e.sentCount).isEqualTo(0)
        assertThat(e.name).isEmpty()
    }
}

class TemplateVariableCountTest {

    private fun dto(body: String) = com.websbaba.nitigrow.data.remote.dto.TemplateDto(
        id = "t", name = "n",
        components = listOf(com.websbaba.nitigrow.data.remote.dto.TemplateComponentDto(type = "BODY", text = body))
    )

    @Test
    fun `counts distinct placeholders`() {
        assertThat(dto("Hi {{1}}, order #{{2}} total {{ 3 }} — again {{1}}").variableCount).isEqualTo(3)
        assertThat(dto("No variables here").variableCount).isEqualTo(0)
    }
}
