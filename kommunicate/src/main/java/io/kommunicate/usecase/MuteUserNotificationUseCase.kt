package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.devkit.api.account.user.UserService
import io.kommunicate.devkit.feed.ApiResponse
import io.kommunicate.devkit.feed.ErrorResponseFeed
import io.kommunicate.commons.json.GsonUtils
import io.kommunicate.R
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers

/**
 * A use case for muting user notifications in the Kommunicate SDK.
 *
 * This class handles muting notifications for a specific user for a specified duration.
 *
 * @property userId The ID of the user whose notifications are to be muted
 * @property notificationAfterTime The duration after which notifications should resume
 * @constructor Initializes the use case with the provided [Context], userId and notificationAfterTime
 */
class MuteUserNotificationUseCase(
    private val context: Context,
    private val userId: String,
    private val notificationAfterTime: Long
) : UseCase<APIResult<ApiResponse<Any>>> {

    private val userService: UserService = UserService.getInstance(context)

    /**
     * Executes the mute notification operation for the specified user.
     *
     * @return [APIResult] containing the [ApiResponse] if successful, or an error message/exception
     */
    override suspend fun execute(): APIResult<ApiResponse<Any>> {
        return try {
            val response = userService.muteUserNotifications(userId, notificationAfterTime)
            when {
                response == null -> {
                    APIResult.failed(context.getString(R.string.mute_err))
                }
                "success" == response.status -> {
                    APIResult.success(response)
                }
                else -> {
                    val errorMessage = if (response.errorResponse != null) {
                        GsonUtils.getJsonFromObject(
                            response.errorResponse.toTypedArray(),
                            Array<ErrorResponseFeed>::class.java
                        )
                    } else {
                        context.getString(R.string.mute_err)
                    }
                    APIResult.failed(errorMessage)
                }
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        /**
         * Executes the use case with a [UseCaseExecutor] for asynchronous processing.
         *
         * This method simplifies the use of the use case by providing callbacks for success and failure.
         *
         * @param context The Android [Context] for accessing resources and services
         * @param userId The ID of the user whose notifications are to be muted
         * @param notificationAfterTime The duration after which notifications should resume
         * @param taskListener An optional listener to handle the success or failure callbacks
         * @return A [UseCaseExecutor] instance for managing the execution of the use case
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            userId: String,
            notificationAfterTime: Long,
            taskListener: TaskListener<String>?
        ): UseCaseExecutor<MuteUserNotificationUseCase, APIResult<ApiResponse<Any>>> {
            val useCase = MuteUserNotificationUseCase(context, userId, notificationAfterTime)
            val executor = UseCaseExecutor(
                useCase,
                { result: APIResult<ApiResponse<Any>> ->
                    result.onSuccess {
                        taskListener?.onSuccess(
                            status = context.getString(R.string.mute_notification)
                        )
                    }
                    result.onFailure { error ->
                        taskListener?.onFailure(
                            error = error
                        )
                    }
                },
                { exception: Exception ->
                    taskListener?.onFailure(
                        error = exception
                    )
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}