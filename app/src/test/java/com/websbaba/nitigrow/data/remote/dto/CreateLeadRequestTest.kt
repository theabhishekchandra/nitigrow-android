package com.websbaba.nitigrow.data.remote.dto

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Test

/**
 * The backend's Lead model (backend/src/models/Lead.js) requires `name` and stores the
 * deal size as `value` — it does not look the contact up by contactId to backfill a
 * display name. A request missing `name` or sending `valueInr` instead of `value` gets
 * rejected (400) or silently drops the amount.
 */
class CreateLeadRequestTest {

    private val gson = Gson()

    @Test
    fun `serializes name and value under the field names the backend actually reads`() {
        val json = JsonParser.parseString(
            gson.toJson(
                CreateLeadRequest(
                    contactId = "c1", name = "Priya Sharma", source = "website",
                    stage = "new", valueInr = 50_000, notes = null
                )
            )
        ).asJsonObject

        assertThat(json.get("name").asString).isEqualTo("Priya Sharma")
        assertThat(json.get("value").asLong).isEqualTo(50_000)
        assertThat(json.has("valueInr")).isFalse()
    }
}
