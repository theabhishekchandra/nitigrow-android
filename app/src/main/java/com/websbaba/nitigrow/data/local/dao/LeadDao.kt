package com.websbaba.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.websbaba.nitigrow.data.local.entity.LeadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {

    @Query("SELECT * FROM leads ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE id = :id LIMIT 1")
    suspend fun get(id: String): LeadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LeadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LeadEntity)

    @Query("UPDATE leads SET stage = :stage, updatedAtEpochMs = :now WHERE id = :id")
    suspend fun setStage(id: String, stage: String, now: Long)

    @Query("DELETE FROM leads")
    suspend fun clear()
}
