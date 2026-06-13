package com.websbaba.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.websbaba.nitigrow.data.local.entity.DashboardStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM dashboard_stats WHERE id = 0 LIMIT 1")
    fun observe(): Flow<DashboardStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DashboardStatsEntity)

    @Query("DELETE FROM dashboard_stats")
    suspend fun clear()
}
