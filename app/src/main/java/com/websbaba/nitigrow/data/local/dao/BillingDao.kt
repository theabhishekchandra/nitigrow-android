package com.websbaba.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.websbaba.nitigrow.data.local.entity.PaymentEntity
import com.websbaba.nitigrow.data.local.entity.PlanEntity
import com.websbaba.nitigrow.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY priceInr ASC")
    fun observeAll(): Flow<List<PlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PlanEntity>)

    @Query("DELETE FROM plans")
    suspend fun clear()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription WHERE id = 0 LIMIT 1")
    fun observe(): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SubscriptionEntity)

    @Query("DELETE FROM subscription")
    suspend fun clear()
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PaymentEntity>)

    @Query("DELETE FROM payments")
    suspend fun clear()
}
