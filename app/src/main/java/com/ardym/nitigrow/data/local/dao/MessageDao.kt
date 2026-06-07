package com.ardym.nitigrow.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ardym.nitigrow.data.local.entity.MessageEntity

@Dao
interface MessageDao {

    @Query("""
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY sentAtEpochMs DESC
    """)
    fun pagingSource(conversationId: String): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE localId = :localId LIMIT 1")
    suspend fun get(localId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE clientId = :clientId LIMIT 1")
    suspend fun getByClientId(clientId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE localId = :localId")
    suspend fun updateStatus(localId: String, status: String)

    @Query("UPDATE messages SET status = :status, errorReason = :reason WHERE localId = :localId")
    suspend fun setFailed(localId: String, status: String, reason: String?)

    @Query("""
        UPDATE messages SET serverId = :serverId, status = :status, localId = :serverId
        WHERE clientId = :clientId
    """)
    suspend fun confirmServerId(clientId: String, serverId: String, status: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearForConversation(conversationId: String)

    /** Wipes the entire local message cache (logout / tenant switch). */
    @Query("DELETE FROM messages")
    suspend fun clear()

    @Query("SELECT * FROM messages WHERE outbound = 1 AND status = 'PENDING'")
    suspend fun pendingOutbound(): List<MessageEntity>
}
