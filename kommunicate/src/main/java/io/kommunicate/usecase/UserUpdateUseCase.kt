package io.kommunicate.usecase

import android.content.Context
import dev.kommunicate.devkit.api.account.user.User
import dev.kommunicate.devkit.api.account.user.UserService
import dev.kommunicate.devkit.feed.ApiResponse
import dev.kommunicate.devkit.listners.AlCallback
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for updating a user's details in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property user The [User] object containing the updated user details.
 * @property isForEmail Boolean flag indicating whether the update is specifically for email.
 */
class UserUpdateUseCase(
    private val context: Context,
    private val user: User,
    private val isForEmail: Boolean = false
) : UseCase<APIResult<ApiResponse<Any>>> {

    private val userService: UserService = UserService.getInstance(context)

    /**
     * Executes the user update operation.
     *
     * @return An [APIResult] indicating success or failure of the update operation.
     */
    override suspend fun execute(): APIResult<ApiResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val response = userService.updateUserWithResponse(user, isForEmail)
            if (response != null && response.isSuccess) {
                APIResult.success(response)
            } else {
                val errorMessage = response?.errorResponse
                    ?.joinToString(" ") {
                        it.description
                    }
                APIResult.failed(errorMessage ?: "Unknown error occurred")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {

        /**
         * Executes the use case with the given [UseCaseExecutor].
         * This method provides an easy way to handle success or failure using a provided [AlCallback].
         *
         * @param context The Android context for accessing resources and services.
         * @param user The [User] object containing the updated user details.
         * @param isForEmail Boolean flag indicating whether the update is specifically for email.
         * @param callback Optional [AlCallback] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            user: User,
            isForEmail: Boolean,
            callback: AlCallback? = null
        ): UseCaseExecutor<UserUpdateUseCase, APIResult<ApiResponse<Any>>> {
            val userUpdateUseCase = UserUpdateUseCase(context, user, isForEmail)
            val executor = UseCaseExecutor(
                userUpdateUseCase,
                { result: APIResult<ApiResponse<Any>> ->
                    result.onSuccess { response ->
                        callback?.onSuccess(response.response)
                    }
                    result.onFailure { error ->
                        callback?.onError(error)
                    }
                },
                { exception: Exception ->
                    callback?.onError(exception.message ?: "Unexpected error occurred while doing API call")
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
