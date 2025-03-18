package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.kommunicate.R
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.devkit.api.MobiComKitConstants
import io.kommunicate.devkit.channel.service.ChannelService

class RemoveMemberUseCase(
    private val context: Context,
    private val channelKey: Int,
    private val userId: String
) : UseCase<APIResult<String>> {

    override suspend fun execute(): APIResult<String> = withContext(Dispatchers.IO) {
        try {
            if (userId.isNotBlank()) {
                val channelService = ChannelService.getInstance(context)
                val removeResponse = channelService.removeMemberFromChannelProcess(channelKey, userId.trim())

                if (!removeResponse.isNullOrEmpty()) {
                    if (MobiComKitConstants.SUCCESS == removeResponse) {
                        APIResult.success(removeResponse)
                    } else {
                        APIResult.failed("Failed to remove member: $removeResponse")
                    }
                } else {
                    APIResult.failed("Empty response received")
                }
            } else {
                val errorMsg = context.getString(R.string.applozic_userId_error_info_in_logs)
                APIResult.failed(errorMsg)
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        /**
         * Executes the use case with a provided callback.
         *
         * @param context The Android context for accessing resources and services.
         * @param channelKey The channel key for identifying the conversation.
         * @param userId The user ID to remove from the channel.
         * @param callback The callback to handle success or failure.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            channelKey: Int,
            userId: String,
            callback: TaskListener<String>
        ): UseCaseExecutor<RemoveMemberUseCase, APIResult<String>> {

            val removeMemberUseCase = RemoveMemberUseCase(context, channelKey, userId)
            val executor = UseCaseExecutor(
                removeMemberUseCase,
                { result: APIResult<String> ->
                    result.onSuccess { response ->
                        callback.onSuccess(response)
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
