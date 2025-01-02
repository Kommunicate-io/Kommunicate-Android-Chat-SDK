package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicomkit.listners.AlLogoutHandler
import io.kommunicate.services.KmUserClientService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for handling the logout operation for a user in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 */
class UserLogoutUseCase(
    private val context: Context
) : UseCase<APIResult<Boolean>> {

    private val userClientService: KmUserClientService = KmUserClientService(context)

    /**
     * Executes the logout operation.
     *
     * @return An [APIResult] indicating success or failure of the logout operation.
     */
    override suspend fun execute(): APIResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            userClientService.logout()
            APIResult.success(true)
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {

        /**
         * Executes the use case with the given [UseCaseExecutor].
         * Provides a simpler way to handle success or failure using callbacks.
         *
         * @param context The Android context for accessing resources and services.
         * @param logoutHandler Optional handler for handling logout success or failure callbacks.
         * @return [UseCaseExecutor] for cancelling the coroutine.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            logoutHandler: AlLogoutHandler? = null
        ): UseCaseExecutor<UserLogoutUseCase, APIResult<Boolean>> {

            val userLogoutUseCase = UserLogoutUseCase(context)
            val executor = UseCaseExecutor(
                userLogoutUseCase,
                { result: APIResult<Boolean> ->
                    result.onSuccess { success: Boolean ->
                        if (success) {
                            logoutHandler?.onSuccess(context)
                        } else {
                            logoutHandler?.onFailure(Exception("Unknown logout failure"))
                        }
                    }
                    result.onFailure { exception: Exception ->
                        logoutHandler?.onFailure(exception)
                    }
                },
                { exception: Exception? ->
                    logoutHandler?.onFailure(exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
