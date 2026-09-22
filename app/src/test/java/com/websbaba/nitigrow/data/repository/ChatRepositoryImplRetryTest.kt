package com.websbaba.nitigrow.data.repository

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.realtime.RealtimeClient
import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.NitiGrowDatabase
import com.websbaba.nitigrow.data.local.dao.ConversationDao
import com.websbaba.nitigrow.data.local.dao.MessageDao
import com.websbaba.nitigrow.data.local.entity.MessageEntity
import com.websbaba.nitigrow.data.remote.api.ChatApi
import com.websbaba.nitigrow.data.remote.api.InboxApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * A send that fails because the device is offline must stay retryable without the user
 * doing anything (SendPendingWorker retries every PENDING row once connectivity returns —
 * see PendingMessageScheduler); a send the server actively rejected should not be retried
 * automatically, since the same request would only fail again the same way.
 */
class ChatRepositoryImplRetryTest {

    private val chatApi: ChatApi = mockk()
    private val messageDao: MessageDao = mockk(relaxUnitFun = true)
    private val dispatchers: DispatcherProvider = mockk { every { io } returns Dispatchers.Unconfined }

    private val repo = ChatRepositoryImpl(
        chatApi = chatApi,
        inboxApi = mockk<InboxApi>(),
        db = mockk<NitiGrowDatabase>(relaxed = true),
        messageDao = messageDao,
        conversationDao = mockk<ConversationDao>(relaxed = true),
        realtime = mockk<RealtimeClient> { every { events } returns MutableSharedFlow() },
        tokenStore = mockk<TokenDataStore>(relaxed = true),
        dispatchers = dispatchers
    )

    private fun pendingMessage(clientId: String) = MessageEntity(
        localId = clientId, serverId = null, clientId = clientId, conversationId = "contact-1",
        text = "hi", sentAtEpochMs = 0, outbound = true, status = "PENDING", type = "TEXT",
        mediaUrl = null, mediaMimeType = null, mediaSizeBytes = null, replyToMessageId = null,
        errorReason = null
    )

    @Test
    fun `a dropped connection leaves the message PENDING for the reconnect worker to retry`() = runTest {
        coEvery { messageDao.getByClientId("c1") } returns pendingMessage("c1")
        coEvery { chatApi.send(any()) } throws IOException("no network")

        val result = repo.retry("c1")

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        coVerify(exactly = 0) { messageDao.setFailed(any(), any(), any()) }
    }

    @Test
    fun `a server rejection marks the message FAILED so it is not retried blindly`() = runTest {
        coEvery { messageDao.getByClientId("c1") } returns pendingMessage("c1")
        coEvery { chatApi.send(any()) } throws HttpException(
            Response.error<Any>(400, "".toResponseBody(null))
        )

        val result = repo.retry("c1")

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        coVerify(exactly = 1) { messageDao.setFailed("c1", "FAILED", any()) }
    }
}
