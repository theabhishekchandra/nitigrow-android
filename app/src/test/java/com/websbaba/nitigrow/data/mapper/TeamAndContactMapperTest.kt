package com.websbaba.nitigrow.data.mapper

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.websbaba.nitigrow.data.remote.dto.ContactDto
import com.websbaba.nitigrow.data.remote.dto.TeamMemberDto
import org.junit.Test

class TeamAndContactMapperTest {

    private val gson = Gson()

    @Test
    fun `team endpoint returns a bare array with createdAt instead of joinedAt`() {
        val json = """[{"_id":"u1","tenantId":"t","name":"NitiGrow Demo","email":"reviewer@nitigrow.in",
            "role":"owner","isActive":true,"createdAt":"2026-06-09T17:21:46.557Z"}]"""

        val members = gson.fromJson<List<TeamMemberDto>>(json, object : TypeToken<List<TeamMemberDto>>() {}.type)
        val entity = members.single().toEntity()

        assertThat(entity.name).isEqualTo("NitiGrow Demo")
        assertThat(entity.role).isEqualTo("OWNER")
        assertThat(entity.isOwner).isTrue()
        assertThat(entity.joinedAtEpochMs).isEqualTo(1_781_025_706_557L)
    }

    @Test
    fun `a team member with only an id still maps`() {
        val entity = gson.fromJson("""{"_id":"u2"}""", TeamMemberDto::class.java).toEntity()

        assertThat(entity.name).isEmpty()
        assertThat(entity.role).isEqualTo("AGENT")
        assertThat(entity.isOwner).isFalse()
    }

    @Test
    fun `contact blocked flag is read from the server's blocked key`() {
        val json = """{"_id":"c1","name":"A","phone":"9198","blocked":true,
            "createdAt":"2026-06-09T17:21:46.557Z","updatedAt":"2026-06-09T17:21:46.557Z"}"""

        assertThat(gson.fromJson(json, ContactDto::class.java).isBlocked).isTrue()
    }
}
