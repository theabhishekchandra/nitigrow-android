package com.ardym.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ardym.nitigrow.data.local.entity.ProfileEntity
import com.ardym.nitigrow.data.local.entity.TeamMemberEntity
import com.ardym.nitigrow.data.local.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    fun observe(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProfileEntity)

    @Query("DELETE FROM profile")
    suspend fun clear()
}

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenant WHERE id = 0 LIMIT 1")
    fun observe(): Flow<TenantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TenantEntity)

    @Query("DELETE FROM tenant")
    suspend fun clear()
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM team_members ORDER BY isOwner DESC, name ASC")
    fun observeAll(): Flow<List<TeamMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TeamMemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TeamMemberEntity)

    @Query("DELETE FROM team_members WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM team_members")
    suspend fun clear()
}
