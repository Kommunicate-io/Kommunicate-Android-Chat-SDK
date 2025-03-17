package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicomkit.channel.service.ChannelService
import com.applozic.mobicommons.people.channel.Channel
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for retrieving conversation info in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property conversationId The conversation ID (nullable if clientConversationId is used).
 * @property clientConversationId The client conversation ID (nullable if conversationId is used).
 */
class ConversationInfoUseCase(
    private val context: Context,
    private val conversationId: Int?,
    private val clientConversationId: String?
) : UseCase<APIResult<Channel>> {

    private val channelService = ChannelService.getInstance(context)

    /**
     * Executes the conversation info retrieval process.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<Channel> = withContext(Dispatchers.IO) {
        try {
            val channel: Channel? = when {
                !clientConversationId.isNullOrEmpty() -> channelService.getChannelInfo(clientConversationId)
                conversationId != null -> channelService.getChannelInfo(conversationId)
                else -> null
            }

            if (channel != null) {
                APIResult.success(channel)
            } else {
                APIResult.failed("Conversation info not found. conversationId: $conversationId clientConversationId: $clientConversationId")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {

        /**
         * Executes the use case with the given [TaskListener].
         * Provides an easy way to handle success or failure using a provided listener.
         *
         * @param context The Android context for accessing resources and services.
         * @param conversationId The conversation ID (nullable if clientConversationId is used).
         * @param clientConversationId The client conversation ID (nullable if conversationId is used).
         * @param callback Uses [TaskListener] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            conversationId: Int? = null,
            clientConversationId: String? = null,
            callback: TaskListener<Channel>
        ): UseCaseExecutor<ConversationInfoUseCase, APIResult<Channel>> {
            val useCase = ConversationInfoUseCase(context, conversationId, clientConversationId)
            val executor = UseCaseExecutor(
                useCase,
                { result ->
                    result.onSuccess { channel ->
                        callback.onSuccess(channel)
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
