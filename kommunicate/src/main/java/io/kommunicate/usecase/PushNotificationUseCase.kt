package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.devkit.api.account.register.RegisterUserClientService
import io.kommunicate.devkit.api.account.register.RegistrationResponse
import io.kommunicate.devkit.listners.PushNotificationHandler
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for updating the push notification ID in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property pushNotificationId The new push notification ID to be updated.
 */
class PushNotificationUseCase(
    val context: Context,
    private val pushNotificationId: String
) : UseCase<APIResult<RegistrationResponse>> {

    private val registerUserClientService: RegisterUserClientService = RegisterUserClientService(context)

    /**
     * Executes the push notification update operation.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<RegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            val registrationResponse =
                registerUserClientService.updatePushNotificationId(pushNotificationId)
            if (registrationResponse.isRegistrationSuccess) {
                APIResult.success(registrationResponse)
            } else {
                APIResult.failed("Failed to update push notification ID.")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {

        /**
         * Executes the use case with the given [UseCaseExecutor].
         * Provides an easy way to handle success or failure using a provided [PushNotificationHandler].
         *
         * @param context The Android context for accessing resources and services.
         * @param pushNotificationId The new push notification ID to be updated.
         * @param pushNotificationHandler Optional [PushNotificationHandler] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            pushNotificationId: String,
            pushNotificationHandler: PushNotificationHandler? = null
        ): UseCaseExecutor<PushNotificationUseCase, APIResult<RegistrationResponse>> {
            val useCase = PushNotificationUseCase(context, pushNotificationId)

            val executor = UseCaseExecutor(
                useCase,
                { result: APIResult<RegistrationResponse> ->
                    result.onSuccess { registrationResponse ->
                        pushNotificationHandler?.onSuccess(registrationResponse)
                    }
                    result.onFailure { error ->
                        pushNotificationHandler?.onFailure(null, Exception(error))
                    }
                },
                { exception: Exception? ->
                    pushNotificationHandler?.onFailure(null, exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }

    }
}
