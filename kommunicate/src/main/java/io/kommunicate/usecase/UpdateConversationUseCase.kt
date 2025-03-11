package io.kommunicate.usecase

import android.content.Context
import dev.kommunicate.devkit.channel.service.ChannelService
import dev.kommunicate.devkit.feed.GroupInfoUpdate
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * A use case for updating a conversation channel with the given [GroupInfoUpdate] in the Kommunicate SDK.
 * This use case is executed asynchronously using Kotlin Coroutines.
 *
 * @property context The application [Context] required to interact with the Kommunicate SDK.
 * @property groupInfoUpdate The [GroupInfoUpdate] object that contains the updated conversation details.
 *
 * @see [ChannelService.updateChannel] for the underlying API call.
 */
class UpdateConversationUseCase(
    private val context: Context,
    private val groupInfoUpdate: GroupInfoUpdate
) : UseCase<APIResult<Context>> {

    override suspend fun execute(): APIResult<Context> = withContext(Dispatchers.IO) {
        try {
            // Perform the channel update operation
            val result = ChannelService.getInstance(context).updateChannel(groupInfoUpdate)
            return@withContext if (result == "success") {
                APIResult.success(context)
            } else {
                APIResult.failed("Failed to update the conversation.")
            }
        } catch (e: Exception) {
            return@withContext APIResult.failedWithException(e)
        }
    }

    companion object {
        /**
         * Executes the [UpdateConversationUseCase] and handles success or failure via the provided [callback].
         *
         * This function simplifies the execution of the use case by wrapping it into an executor and passing
         * the result to the provided [TaskListener]. The [callback] is invoked when the operation completes
         * successfully or with an error.
         *
         * @param context The application [Context] required to interact with the Kommunicate SDK.
         * @param groupInfoUpdate The [GroupInfoUpdate] object containing the updated conversation details.
         * @param callback The [TaskListener] to handle the success or failure of the operation.
         * @return [UseCaseExecutor] that can be used to cancel the operation if necessary.
         *
         * @see [TaskListener.onSuccess] for success handling
         * @see [TaskListener.onFailure] for failure handling
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            groupInfoUpdate: GroupInfoUpdate,
            callback: TaskListener<Context>
        ): UseCaseExecutor<UpdateConversationUseCase, APIResult<Context>> {

            val kmUpdateConversationUseCase = UpdateConversationUseCase(context, groupInfoUpdate)
            val executor = UseCaseExecutor(
                kmUpdateConversationUseCase,
                { result: APIResult<Context> ->
                    result.onSuccess { context: Context ->
                        callback.onSuccess(context)
                    }
                    result.onFailure { errorMessage: Exception ->
                        callback.onFailure(errorMessage)
                    }
                },
                { exception: Exception ->
                    callback.onFailure(exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
