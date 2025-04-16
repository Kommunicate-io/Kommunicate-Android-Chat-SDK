package io.kommunicate.usecase

import android.content.Context
import android.os.ResultReceiver
import io.kommunicate.devkit.KommunicateSettings
import io.kommunicate.devkit.api.account.register.RegisterUserClientService
import io.kommunicate.devkit.api.account.register.RegistrationResponse
import io.kommunicate.devkit.listners.AlLoginHandler
import io.kommunicate.services.KmUserClientService
import io.kommunicate.users.KMUser
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.KmConstants
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for handling the login or registration of a KMUser in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property user The [KMUser] object containing user details for login or registration.
 * @property isAgent Boolean flag indicating if the user is an agent or a regular user.
 * @property preChatReceiver Optional [ResultReceiver] to handle pre-chat events.
 */
class KMUserLoginUseCase(
    private val context: Context,
    private val user: KMUser,
    private val isAgent: Boolean,
    private val preChatReceiver: ResultReceiver? = null
): UseCase<APIResult<RegistrationResponse>> {

    private val userClientService: KmUserClientService = KmUserClientService(context)
    private val registerUserClientService: RegisterUserClientService = RegisterUserClientService(context)

    override suspend fun execute(): APIResult<RegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = if (isAgent) {
                userClientService.clearDataAndPreference()
                userClientService.loginKmUser(user)
            } else {
                userClientService.clearDataAndPreference()
                AppSettingUseCase(
                    context = context,
                    appId = KommunicateSettings.getInstance(context).applicationKey,
                    updateCache = true // Forces to update the cache
                ).execute()
                registerUserClientService.createAccount(user)
            }

            preChatReceiver?.send(KmConstants.PRECHAT_RESULT_CODE, null)

            if (response.isRegistrationSuccess) {
                APIResult.success(response)
            } else {
                APIResult.failed("unable to login user on server.")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {

        /**
         * Executes the use case with the given [UseCaseExecutor]. This helper function invokes the
         * coroutine internally don't invoke explicitly.
         *
         * This method provides a simpler way to execute the use case and handle success or failure
         * using a provided [AlLoginHandler].
         *
         * @param context The Android context for accessing resources and services.
         * @param user The [KMUser] object containing user details for login or registration.
         * @param isAgent Boolean flag indicating if the user is an agent or a regular user.
         * @param preChatReceiver Optional [ResultReceiver] to handle pre-chat events.
         * @param loginHandler Optional [AlLoginHandler] for handling login success or failure callbacks.
         * @return [UseCaseExecutor] for cancelling the coroutine.
         */
        fun executeWithExecutor(
            context: Context,
            user: KMUser,
            isAgent: Boolean,
            preChatReceiver: ResultReceiver? = null,
            loginHandler: AlLoginHandler? = null,
        ): UseCaseExecutor<KMUserLoginUseCase, APIResult<RegistrationResponse>> {

            val kmUserLoginUseCase = KMUserLoginUseCase(context, user, isAgent, preChatReceiver)
            val executor = UseCaseExecutor(
                kmUserLoginUseCase,
                { result: APIResult<RegistrationResponse> ->
                    result.onSuccess { registrationResponse: RegistrationResponse? ->
                        loginHandler?.onSuccess(registrationResponse, context)
                    }
                    result.onFailure {
                        loginHandler?.onFailure(null, it)
                    }
                },
                { exception: java.lang.Exception? ->
                    loginHandler?.onFailure(null, exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}