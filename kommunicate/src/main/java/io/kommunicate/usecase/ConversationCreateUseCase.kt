package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.devkit.api.people.ChannelInfo
import io.kommunicate.devkit.channel.service.ChannelService
import io.kommunicate.devkit.feed.ChannelFeedApiResponse
import io.kommunicate.callbacks.KMStartChatHandler
import io.kommunicate.callbacks.KmStartConversationHandler
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for creating a conversation in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property channelInfo Information needed to create the channel.
 */
class ConversationCreateUseCase(
    private val context: Context,
    private val channelInfo: ChannelInfo
) : UseCase<APIResult<ChannelFeedApiResponse>> {

    private val channelService: ChannelService = ChannelService.getInstance(context)

    /**
     * Executes the conversation creation process.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<ChannelFeedApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = channelService.createChannelWithResponse(channelInfo)
            if (response != null && response.isSuccess) {
                APIResult.success(response)
            } else {
                APIResult.failed("Failed to create conversation. $response")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        /**
         * Executes the use case with the given [KMStartChatHandler] or [KmStartConversationHandler].
         * Provides an easy way to handle success or failure using a provided listener.
         *
         * @param context The Android context for accessing resources and services.
         * @param channelInfo The information required to create the channel.
         * @param chatHandler Optional [KMStartChatHandler] for handling success or failure callbacks.
         * @param conversationHandler Optional [KmStartConversationHandler] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            channelInfo: ChannelInfo,
            conversationHandler: KmStartConversationHandler?,
            chatHandler: KMStartChatHandler?
        ): UseCaseExecutor<ConversationCreateUseCase, APIResult<ChannelFeedApiResponse>> {
            val useCase = ConversationCreateUseCase(context, channelInfo)
            val channelService = ChannelService.getInstance(context)

            val executor = UseCaseExecutor(
                useCase,
                { result ->
                    result.onSuccess { response ->
                        val channel = channelService.getChannel(response.response)
                        conversationHandler?.onSuccess(channel, context)
                        chatHandler?.onSuccess(channel, context)
                    }
                    result.onFailure { error ->
                        conversationHandler?.onFailure(error, context)
                        chatHandler?.onFailure(error, context)
                    }
                },
                { exception ->
                    conversationHandler?.onFailure(exception, context)
                    chatHandler?.onFailure(exception, context)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
