package com.websbaba.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Placeholder so Room generates a database. Remove once real entities exist.
 */
@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String
)
