package io.kommunicate.usecase

import dev.kommunicate.devkit.feed.ApiResponse
import dev.kommunicate.commons.ApplozicService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.services.KmClientService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type


/**
 * Use case for updating the assignee of a conversation in Kommunicate.
 *
 * @property groupId The ID of the conversation/group.
 * @property assigneeId The ID of the new assignee.
 * @property switchAssignee Whether to switch the assignee.
 * @property sendNotifyMessage Whether to send a notification message.
 * @property takeOverFromBot Whether the new assignee is taking over from a bot.
 */
class AssigneeUpdateUseCase(
    private val groupId: Int,
    private val assigneeId: String,
    private val switchAssignee: Boolean = true,
    private val sendNotifyMessage: Boolean = true,
    private val takeOverFromBot: Boolean = true
) : UseCase<APIResult<String>> {

    private val clientService = KmClientService(ApplozicService.getAppContext())

    /**
     * Executes the assignee update operation.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<String> = withContext(Dispatchers.IO) {
        try {
            val responseJson = clientService.switchConversationAssignee(
                groupId,
                assigneeId,
                switchAssignee,
                sendNotifyMessage,
                takeOverFromBot
            )

            if (!responseJson.isNullOrEmpty()) {
                val type: Type = TypeToken.getParameterized(
                    ApiResponse::class.java, String::class.java
                ).type
                val apiResponse: ApiResponse<String>? = Gson().fromJson(responseJson, type)

                if (apiResponse?.isSuccess == true) {
                    APIResult.success(apiResponse.response)
                } else {
                    val errorMessage = apiResponse?.errorResponse
                        ?.joinToString(" ") {
                            it.description
                        }
                    APIResult.failed(errorMessage ?: "Failed to update Assignee")
                }
            } else {
                APIResult.failed("Invalid Response, Failed to update Assignee")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {

        @JvmStatic
        fun executeWithExecutor(
            groupId: Int,
            assigneeId: String,
            callback: KmCallback? = null
        ) {
            executeWithExecutor(
                groupId = groupId,
                assigneeId = assigneeId,
                switchAssignee = true,
                sendNotifyMessage = true,
                takeOverFromBot = true,
                callback = callback
            )
        }

        /**
         * Executes the use case with the given [UseCaseExecutor].
         * Provides an easy way to handle success or failure using a provided [KmCallback].
         *
         * @param groupId The ID of the conversation/group.
         * @param assigneeId The ID of the new assignee.
         * @param switchAssignee Whether to switch the assignee.
         * @param sendNotifyMessage Whether to send a notification message.
         * @param takeOverFromBot Whether the new assignee is taking over from a bot.
         * @param callback Optional [KmCallback] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            groupId: Int,
            assigneeId: String,
            switchAssignee: Boolean = true,
            sendNotifyMessage: Boolean = true,
            takeOverFromBot: Boolean = true,
            callback: KmCallback? = null
        ): UseCaseExecutor<AssigneeUpdateUseCase, APIResult<String>> {
            val useCase = AssigneeUpdateUseCase(groupId, assigneeId, switchAssignee, sendNotifyMessage, takeOverFromBot)
            val executor = UseCaseExecutor(
                useCase,
                { result: APIResult<String> ->
                    result.onSuccess { response ->
                        callback?.onSuccess(response)
                    }
                    result.onFailure { error ->
                        callback?.onFailure(error)
                    }
                },
                { exception: Exception? ->
                    callback?.onFailure(exception?.message ?: "Unknown error occurred")
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
