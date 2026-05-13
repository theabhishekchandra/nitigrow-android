package com.ardym.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ardym.nitigrow.data.local.entity.CampaignEntity
import com.ardym.nitigrow.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {

    @Query("SELECT * FROM campaigns ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    fun observeOne(id: String): Flow<CampaignEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CampaignEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CampaignEntity)

    @Query("UPDATE campaigns SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: String)

    @Query("""
        UPDATE campaigns
        SET sentCount = :sent, deliveredCount = :delivered,
            readCount = :read, failedCount = :failed
        WHERE id = :id
    """)
    suspend fun setProgress(
        id: String,
        sent: Long,
        delivered: Long,
        read: Long,
        failed: Long
    )

    @Query("DELETE FROM campaigns")
    suspend fun clear()
}

@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates ORDER BY name ASC")
    fun observeAll(): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TemplateEntity>)
}
