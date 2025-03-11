package io.kommunicate.usecase

import android.content.Context
import dev.kommunicate.devkit.channel.service.ChannelClientService
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.models.InQueueData
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers

/**
* A use case for fetching queue status in the Chat.
 *
 * This class fetches the data for message that is in queue for the given team.
 * @param context The Android [Context] for accessing resources and services
 * @param teamId Fetch waiting queue for that particular team.
 */
class WaitingQueueStatusUseCase(
    context: Context,
    private val teamId: Long
): UseCase<APIResult<List<Long>>> {

    private val channelClientService: ChannelClientService = ChannelClientService.getInstance(context)

    override suspend fun execute(): APIResult<List<Long>> {
        return try {
            val response: InQueueData = channelClientService.getChannelInQueueStatus(teamId)
            if (response.status.lowercase() == "success") {
                APIResult.success(response.response)
            }else {
                APIResult.failed(INTERNAL_ERR)
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val INTERNAL_ERR = "Unable to fetch the waiting queue status"

        /**
         * Executes the use case with a [UseCaseExecutor] for asynchronous processing.
         *
         * This method simplifies the use of the use case by providing callbacks for success and failure.
         *
         * @param context The Android [Context] for accessing resources and services
         * @param teamId Fetch waiting queue for that particular team.
         * @param callback An optional listener to handle the success, failure
         * @return A [UseCaseExecutor] instance for managing the execution of the use case
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            teamId: Long,
            callback: TaskListener<List<Long>>
        ): UseCaseExecutor<WaitingQueueStatusUseCase, APIResult<List<Long>>> {
            val useCase = WaitingQueueStatusUseCase(context, teamId)
            val executor = UseCaseExecutor(
                useCase = useCase,
                onComplete = { result ->
                    result.onSuccess { data ->
                        callback.onSuccess(data)
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