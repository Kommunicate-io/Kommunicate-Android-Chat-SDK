package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicomkit.api.account.user.UserService
import com.applozic.mobicomkit.feed.ApiResponse
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for blocking or unblocking a user in the Applozic SDK.
 *
 * @property context The Android context for accessing resources and services.
 */
class UserBlockUseCase(
    private val context: Context,
    private val userId: String,
    private val block: Boolean
) : UseCase<APIResult<ApiResponse<Any>>> {

    private val userService = UserService.getInstance(context)

    /**
     * Executes the block or unblock operation.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<ApiResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val apiResponse = userService.processUserBlock(userId, block)
            if (apiResponse != null && apiResponse.isSuccess) {
                APIResult.success(apiResponse)
            } else {
                APIResult.failed("Failed to process user block request.")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {

        /**
         * Executes the use case with the given [UseCaseExecutor].
         * Provides an easy way to handle success or failure using a provided [KmCallback].
         *
         * @param context The Android context for accessing resources and services.
         * @param userId The ID of the user to block or unblock.
         * @param block A boolean indicating whether to block (true) or unblock (false) the user.
         * @param callback Optional [KmCallback] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            userId: String,
            block: Boolean,
            callback: TaskListener? = null
        ): UseCaseExecutor<UserBlockUseCase, APIResult<ApiResponse<Any>>> {
            val useCase = UserBlockUseCase(context, userId, block)
            val executor = UseCaseExecutor(
                useCase,
                { result: APIResult<ApiResponse<Any>> ->
                    result.onSuccess { response ->
                        callback?.onSuccess(response)
                    }
                    result.onFailure { error ->
                        callback?.onFailure(null, Exception(error))
                    }
                },
                { exception: Exception? ->
                    callback?.onFailure(null, exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            callback?.onCompletion()
            return executor
        }
    }

    /**
     * Listener for handling task events, including success, failure, and completion.
     */
    interface TaskListener {
        fun onSuccess(apiResponse: ApiResponse<Any>)
        fun onFailure(apiResponse: ApiResponse<Any>?, exception: Exception?)
        fun onCompletion()
    }
}
