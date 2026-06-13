package com.websbaba.nitigrow.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.websbaba.nitigrow.data.local.NitiGrowDatabase
import com.websbaba.nitigrow.data.local.entity.MessageEntity
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.ChatApi
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * Pages a contact's message thread against the backend's offset pagination.
 *
 * The backend exposes GET messages/conversation/{contactId}?page=&limit= with a
 * 1-based `page`; there is NO cursor / `before` parameter. It returns
 * { messages: [...] } sorted ascending (oldest→newest). The local paging source
 * sorts DESC (newest first), so:
 *   - REFRESH = page 1 (the newest `limit` messages).
 *   - APPEND  = older history; the next page is derived from how many rows are
 *               already cached: page = floor(loaded / limit) + 1.
 *   - PREPEND  = unsupported; newest messages arrive via the realtime socket.
 *
 * `endOfPaginationReached` is true once a page returns fewer rows than `limit`,
 * which is the correct end condition for offset paging.
 *
 * NOTE: `contactId` IS the conversation id throughout the app.
 */
@OptIn(ExperimentalPagingApi::class)
class MessageRemoteMediator(
    private val contactId: String,
    private val api: ChatApi,
    private val db: NitiGrowDatabase
) : RemoteMediator<Int, MessageEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MessageEntity>
    ): MediatorResult {
        val limit = state.config.pageSize
        val page: Int = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val loaded = state.pages.sumOf { it.data.size }
                // No rows yet means nothing to page back from; let REFRESH drive.
                if (loaded == 0) return MediatorResult.Success(endOfPaginationReached = true)
                (loaded / limit) + 1
            }
        }

        return try {
            val response = api.page(
                contactId = contactId,
                page = page,
                limit = limit
            )
            val entities = response.messages.map { it.toEntity() }
            db.messageDao().upsertAll(entities)
            MediatorResult.Success(endOfPaginationReached = entities.size < limit)
        } catch (e: IOException) {
            Timber.w(e, "RemoteMediator IO")
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            Timber.w(e, "RemoteMediator HTTP ${e.code()}")
            MediatorResult.Error(e)
        }
    }
}
