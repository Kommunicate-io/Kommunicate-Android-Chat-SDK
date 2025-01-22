package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicomkit.api.account.user.UserService
import com.applozic.mobicomkit.feed.ApiResponse
import com.applozic.mobicomkit.feed.ErrorResponseFeed
import com.applozic.mobicommons.json.GsonUtils
import io.kommunicate.R
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
            taskListener: MuteNotificationTaskListener?
        ): UseCaseExecutor<MuteUserNotificationUseCase, APIResult<ApiResponse<Any>>> {
            val useCase = MuteUserNotificationUseCase(context, userId, notificationAfterTime)
            val executor = UseCaseExecutor(
                useCase,
                { result: APIResult<ApiResponse<Any>> ->
                    result.onSuccess {
                        taskListener?.onSuccess(
                            context.getString(R.string.mute_notification),
                            context
                        )
                    }
                    result.onFailure { error ->
                        taskListener?.onFailure(error.message.toString(), context)
                    }
                },
                { exception: Exception? ->
                    taskListener?.onFailure(
                        context.getString(R.string.mute_err),
                        context
                    )
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}

/**
 * Interface definition for callbacks to be invoked when mute operation completes
 */
interface MuteNotificationTaskListener {
    fun onSuccess(status: String, context: Context)
    fun onFailure(error: String, context: Context)
}