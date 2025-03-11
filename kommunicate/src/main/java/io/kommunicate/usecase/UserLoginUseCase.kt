package io.kommunicate.usecase

import android.content.Context
import dev.kommunicate.devkit.api.account.register.RegisterUserClientService
import dev.kommunicate.devkit.api.account.register.RegistrationResponse
import dev.kommunicate.devkit.api.account.user.User
import dev.kommunicate.devkit.api.account.user.UserClientService
import dev.kommunicate.devkit.listners.AlLoginHandler
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers

/**
 * A use case for logging in or registering a user in the Kommunicate SDK.
 *
 * This class handles user login/registration by interacting with the Applozic user management services.
 *
 * @property user The [User] object containing the user details for login or registration.
 * @constructor Initializes the use case with the provided [Context] and [User].
 */
class UserLoginUseCase(
    context: Context,
    private val user: User,
): UseCase<APIResult<RegistrationResponse>> {

    private val userClientService: UserClientService = UserClientService(context)
    private val registerUserClientService: RegisterUserClientService = RegisterUserClientService(context)

    /**
     * Executes the login or registration operation for the provided user.
     *
     * - Clears any existing user data and preferences before attempting registration.
     * - Registers the user using [RegisterUserClientService].
     *
     * @return [APIResult] containing the [RegistrationResponse] if successful, or an error message/exception.
     */
    override suspend fun execute(): APIResult<RegistrationResponse> {
        return try {
            userClientService.clearDataAndPreference()
            val registrationResponse = registerUserClientService.createAccount(user)
            if (registrationResponse.isRegistrationSuccess) {
                APIResult.success(registrationResponse)
            } else {
                APIResult.failed("unable to register user on server.")
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
         * @param context The Android [Context] for accessing resources and services.
         * @param user The [User] object containing the user details for login or registration.
         * @param loginHandler An optional [AlLoginHandler] to handle the success or failure callbacks.
         * @return A [UseCaseExecutor] instance for managing the execution of the use case.
         */
        fun executeWithExecutor(
            context: Context,
            user: User,
            loginHandler: AlLoginHandler?
        ): UseCaseExecutor<UserLoginUseCase, APIResult<RegistrationResponse>> {
            val userLoginUseCase = UserLoginUseCase(context, user)
            val executor = UseCaseExecutor(
                userLoginUseCase,
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
