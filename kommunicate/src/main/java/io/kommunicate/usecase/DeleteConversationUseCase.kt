package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.devkit.channel.service.ChannelService

class DeleteConversationUseCase(
    private val context: Context,
    private val conversationId: Int,
    private val updateClientConversationId: Boolean = true
) : UseCase<APIResult<String>> {

    override suspend fun execute(): APIResult<String> = withContext(Dispatchers.IO) {
        try {
            val response = ChannelService.getInstance(context)
                .deleteChannel(conversationId, updateClientConversationId, true)

            if (response == SUCCESS) {
                APIResult.success(response)
            } else {
                APIResult.failed(ERROR)
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val SUCCESS = "success"
        private const val ERROR = "error"

        /**
         * Executes the use case with the given [KmCallback] for success or failure handling.
         *
         * @param context The Android context for accessing resources and services.
         * @param conversationId The conversation ID to be deleted.
         * @param updateClientConversationId If true, updates the client conversation ID as well.
         * @param callback The callback to handle success or failure.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            conversationId: Int,
            updateClientConversationId: Boolean = true,
            callback: TaskListener<String>
        ): UseCaseExecutor<DeleteConversationUseCase, APIResult<String>> {

            val deleteConversationUseCase = DeleteConversationUseCase(context, conversationId, updateClientConversationId)
            val executor = UseCaseExecutor(
                deleteConversationUseCase,
                { result: APIResult<String> ->
                    result.onSuccess { status ->
                        callback.onSuccess(status)
                    }
                    result.onFailure { error ->
                        callback.onFailure(error)
                    }
                },
                { exception ->
                    callback.onFailure(exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
