package com.ardym.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ardym.nitigrow.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("""
        SELECT * FROM conversations
        ORDER BY isPinned DESC, lastMessageAtEpochMs DESC
    """)
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("""
        SELECT * FROM conversations
        WHERE contactName LIKE '%' || :query || '%'
           OR contactPhone LIKE '%' || :query || '%'
           OR lastMessage LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, lastMessageAtEpochMs DESC
    """)
    fun search(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ConversationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun clearUnread(id: String)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("DELETE FROM conversations")
    suspend fun clear()
}
