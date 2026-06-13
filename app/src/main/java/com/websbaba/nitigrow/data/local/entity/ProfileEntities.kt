package com.websbaba.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val userId: String,
    val tenantId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val avatarUrl: String?
)

@Entity(tableName = "tenant")
data class TenantEntity(
    @PrimaryKey val id: Int = 0,
    val tenantId: String,
    val name: String,
    val wabaPhone: String?,
    val wabaStatus: String,
    val planName: String?,
    val createdAtEpochMs: Long
)

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val isOwner: Boolean,
    val joinedAtEpochMs: Long
)
