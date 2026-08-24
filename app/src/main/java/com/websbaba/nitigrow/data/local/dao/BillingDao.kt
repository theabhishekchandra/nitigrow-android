package com.websbaba.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.websbaba.nitigrow.data.local.entity.BillingStatusEntity
import com.websbaba.nitigrow.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillingStatusDao {
    @Query("SELECT * FROM billing_status WHERE id = 0 LIMIT 1")
    fun observe(): Flow<BillingStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BillingStatusEntity)

    @Query("DELETE FROM billing_status")
    suspend fun clear()
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY paidAtEpochMs DESC")
    fun observeAll(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<InvoiceEntity>)

    @Query("DELETE FROM invoices")
    suspend fun clear()
}
