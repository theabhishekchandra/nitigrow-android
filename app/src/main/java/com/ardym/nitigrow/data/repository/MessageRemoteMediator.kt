package com.ardym.nitigrow.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.ardym.nitigrow.data.local.NitiGrowDatabase
import com.ardym.nitigrow.data.local.entity.MessageEntity
import com.ardym.nitigrow.data.mapper.toEntity
import com.ardym.nitigrow.data.remote.api.ChatApi
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.time.Instant

/**
 * APPEND = older messages (paging back through history).
 * PREPEND not supported — newest stays at top of paging source (DESC sort), pushed by WS.
 * REFRESH = top page only.
 */
@OptIn(ExperimentalPagingApi::class)
class MessageRemoteMediator(
    private val conversationId: String,
    private val api: ChatApi,
    private val db: NitiGrowDatabase
) : RemoteMediator<Int, MessageEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MessageEntity>
    ): MediatorResult {
        val cursor: String? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val oldest = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                Instant.ofEpochMilli(oldest.sentAtEpochMs).toString()
            }
        }

        return try {
            val response = api.page(
                conversationId = conversationId,
                beforeCursor = cursor,
                limit = state.config.pageSize
            )
            val entities = response.data.map { it.toEntity() }
            db.messageDao().upsertAll(entities)
            MediatorResult.Success(endOfPaginationReached = response.nextCursor == null)
        } catch (e: IOException) {
            Timber.w(e, "RemoteMediator IO")
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            Timber.w(e, "RemoteMediator HTTP ${e.code()}")
            MediatorResult.Error(e)
        }
    }
}
