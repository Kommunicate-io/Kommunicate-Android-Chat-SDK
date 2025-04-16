package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.devkit.api.MobiComKitClientService
import io.kommunicate.devkit.api.account.register.RegisterUserClientService
import io.kommunicate.devkit.api.account.user.MobiComUserPreference
import io.kommunicate.devkit.listners.ResultCallback
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for refreshing the authentication token in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 */
class RefreshAuthTokenUseCase(
    private val context: Context
) : UseCase<APIResult<Boolean>> {

    private val clientService = RegisterUserClientService(context)

    /**
     * Executes the auth token refresh operation.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val isTokenRefreshed = clientService.refreshAuthToken(
                MobiComKitClientService.getApplicationKey(context),
                MobiComUserPreference.getInstance(context).getUserId()
            )
            if (isTokenRefreshed) {
                APIResult.success(true)
            } else {
                APIResult.failed("Failed to refresh authentication token.")
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
         * @param callback Optional [KmCallback] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            callback: ResultCallback? = null
        ): UseCaseExecutor<RefreshAuthTokenUseCase, APIResult<Boolean>> {
            val useCase = RefreshAuthTokenUseCase(context)
            val executor = UseCaseExecutor(
                useCase,
                { result: APIResult<Boolean> ->
                    result.onSuccess { response ->
                        callback?.onSuccess(response)
                    }
                    result.onFailure { error ->
                        callback?.onError(error)
                    }
                },
                { exception: Exception? ->
                    callback?.onError(exception?.message ?: "Unknown error occurred")
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
