package io.kommunicate.usecase

import android.content.Context
import dev.kommunicate.devkit.api.conversation.MessageIntentService
import dev.kommunicate.devkit.api.conversation.MobiComMessageService
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers

/**
 * A use case for deleting messages in the Chat.
 *
 * This class handles message deletion with support for deleting messages for all users.
 * @param context The Android [Context] for accessing resources and services
 * @param messageKey The message id which needs to be deleted
 * @param deleteForAll true if Message needs to be deleted for all the users.
 */
class MessageDeleteUseCase(
    context: Context,
    private val messageKey: String,
    private val deleteForAll: Boolean
) : UseCase<APIResult<String>> {

    private val mobiComMessageService: MobiComMessageService = MobiComMessageService(
        context,
        MessageIntentService::class.java
    )

    /**
     * Executes the message delete operation.
     *
     * @return [APIResult] containing the response string if successful, or an error message
     */
    override suspend fun execute(): APIResult<String> {
        return try {
            val response = mobiComMessageService.getMessageDeleteForAllResponse(
                messageKey,
                deleteForAll
            )
            if (response.isNotEmpty()) {
                APIResult.success(response)
            } else {
                APIResult.failed(INTERNAL_ERR)
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val INTERNAL_ERR = "Some internal error occurred."

        /**
         * Java-friendly static method for executing the message delete operation.
         */
        /**
         * Executes the use case with a [UseCaseExecutor] for asynchronous processing.
         *
         * This method simplifies the use of the use case by providing callbacks for success and failure.
         *
         * @param context The Android [Context] for accessing resources and services
         * @param messageKey The message id which needs to be deleted
         * @param deleteForAll true if Message needs to be deleted for all the users.
         * @param callback An optional listener to handle the success, failure
         * @return A [UseCaseExecutor] instance for managing the execution of the use case
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            messageKey: String,
            deleteForAll: Boolean,
            callback: TaskListener<String>
        ): UseCaseExecutor<MessageDeleteUseCase, APIResult<String>> {
            val useCase = MessageDeleteUseCase(context, messageKey, deleteForAll)
            val executor = UseCaseExecutor(
                useCase = useCase,
                onComplete = { result ->
                    result.onSuccess { data ->
                        if (data.isNotEmpty()) {
                            callback.onSuccess(data)
                        } else {
                            callback.onFailure(Exception(INTERNAL_ERR))
                        }
                    }
                    result.onFailure { error ->
                        callback.onFailure(error)
                    }
                },
                onFailed = { exception ->
                    callback.onFailure(exception)
                },
                dispatcher = Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}