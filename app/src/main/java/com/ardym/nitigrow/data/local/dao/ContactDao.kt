package com.ardym.nitigrow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ardym.nitigrow.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY nameKey ASC")
    fun observeAll(): Flow<List<ContactEntity>>

    @Query("""
        SELECT * FROM contacts
        WHERE nameKey LIKE '%' || :q || '%'
           OR phone LIKE '%' || :q || '%'
           OR email LIKE '%' || :q || '%'
        ORDER BY nameKey ASC
    """)
    fun search(q: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM contacts")
    suspend fun clear()
}
