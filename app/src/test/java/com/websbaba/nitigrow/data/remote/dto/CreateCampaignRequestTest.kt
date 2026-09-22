package com.websbaba.nitigrow.data.remote.dto

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Test

/**
 * The backend's Joi schema for POST /api/campaigns (createCampaignSchema in
 * backend/src/middleware/validate.js) requires a nested `audience` object and runs with
 * `stripUnknown: true` — any other top-level key, such as a flat `audienceTags` list, is
 * dropped without an error, and the campaign silently defaults to `audience: { type: 'all' }`
 * (every contact) instead of the tags the user picked. This pins the wire shape so that
 * regression can't come back unnoticed.
 */
class CreateCampaignRequestTest {

    private val gson = Gson()

    @Test
    fun `serializes a nested audience object with the picked tags, not a flat list`() {
        val json = JsonParser.parseString(
            gson.toJson(
                CreateCampaignRequest(
                    name = "Diwali offer",
                    templateId = "t1",
                    audience = CampaignAudienceRequest(type = "tag", tags = listOf("vip", "mumbai")),
                    scheduledAt = null
                )
            )
        ).asJsonObject

        assertThat(json.has("audienceTags")).isFalse()
        val audience = json.getAsJsonObject("audience")
        assertThat(audience.get("type").asString).isEqualTo("tag")
        assertThat(audience.getAsJsonArray("tags").map { it.asString }).containsExactly("vip", "mumbai")
    }
}
