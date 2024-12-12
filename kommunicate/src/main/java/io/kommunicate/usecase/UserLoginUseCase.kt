package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import com.applozic.mobicomkit.api.account.user.User
import com.applozic.mobicomkit.api.account.user.UserClientService
import com.applozic.mobicomkit.listners.AlLoginHandler
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers

class UserLoginUseCase(
    context: Context,
    private val user: User,
): UseCase<APIResult<RegistrationResponse>> {

    private val userClientService: UserClientService = UserClientService(context)
    private val registerUserClientService: RegisterUserClientService = RegisterUserClientService(context)

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
