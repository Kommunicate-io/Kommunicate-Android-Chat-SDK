package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicomkit.api.notification.MuteNotificationRequest
import com.applozic.mobicomkit.channel.service.ChannelService
import com.applozic.mobicomkit.feed.ApiResponse
import io.kommunicate.R
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers

/**
 * A use case for muting notifications in the Kommunicate SDK.
 *
 * This class handles muting notifications based on the provided request parameters.
 *
 * @property muteNotificationRequest The request object containing mute notification parameters
 * @constructor Initializes the use case with the provided [Context] and [MuteNotificationRequest]
 */
class MuteGroupNotificationUseCase(
    context: Context,
    private val muteNotificationRequest: MuteNotificationRequest
) : UseCase<APIResult<ApiResponse<Any>>> {

    private val channelService: ChannelService = ChannelService.getInstance(context)

    /**
     * Executes the mute notification operation with the provided request parameters.
     *
     * @return [APIResult] containing the [ApiResponse] if successful, or an exception if failed
     */
    override suspend fun execute(): APIResult<ApiResponse<Any>> {
        return try {
            val response = channelService.muteNotifications(muteNotificationRequest)
            APIResult.success(response)
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
         * @param request The [MuteNotificationRequest] containing mute parameters
         * @param taskListener An optional listener to handle the success, failure, and completion callbacks
         * @return A [UseCaseExecutor] instance for managing the execution of the use case
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            request: MuteNotificationRequest,
            taskListener: MuteNotificationTaskListener?
        ): UseCaseExecutor<MuteGroupNotificationUseCase, APIResult<ApiResponse<Any>>> {
            val useCase = MuteGroupNotificationUseCase(context, request)
            val executor = UseCaseExecutor(
                useCase,
                { result: APIResult<ApiResponse<Any>> ->
                    result.onSuccess { response ->
                        taskListener?.onSuccess(
                            status = context.getString(R.string.mute_notification),
                            context = context
                        )
                    }
                    result.onFailure { error ->
                        taskListener?.onFailure(
                            error = error.message.toString(),
                            context = context
                        )
                    }
                },
                { exception: Exception ->
                    taskListener?.onFailure(
                        error = exception.message.toString(),
                        context = context
                    )
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}