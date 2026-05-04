package com.ardym.nitigrow.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ardym.nitigrow.data.local.dao.MessageDao
import com.ardym.nitigrow.domain.repository.ChatRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Walks pending outbound messages and retries each. Scheduled when network returns
 * (Step 4c will wire NetworkConnectivityObserver → enqueueUniqueWork).
 */
@HiltWorker
class SendPendingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val chatRepo: ChatRepository,
    private val messageDao: MessageDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val pending = messageDao.pendingOutbound()
        Timber.d("SendPendingWorker: ${pending.size} pending")
        var anyFail = false
        pending.forEach { msg ->
            val cid = msg.clientId ?: msg.localId
            val result = chatRepo.retry(cid)
            if (result is com.ardym.nitigrow.core.network.ApiResult.Error) anyFail = true
        }
        return if (anyFail) Result.retry() else Result.success()
    }
}
