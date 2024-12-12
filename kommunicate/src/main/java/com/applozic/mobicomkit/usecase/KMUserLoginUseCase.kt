package com.applozic.mobicomkit.usecase

import android.content.Context
import android.os.ResultReceiver
import com.applozic.mobicomkit.Applozic
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import com.applozic.mobicomkit.listners.AlLoginHandler
import io.kommunicate.services.KmUserClientService
import io.kommunicate.users.KMUser
import io.kommunicate.utils.KmAppSettingPreferences
import io.kommunicate.utils.KmConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                KmAppSettingPreferences.fetchAppSetting(context, Applozic.getInstance(context).applicationKey)
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
        fun executeWithExecutor(
            context: Context,
            user: KMUser,
            isAgent: Boolean,
            preChatReceiver: ResultReceiver? = null,
            loginHandler: AlLoginHandler? = null,
        ): UseCaseExecutor<KMUserLoginUseCase, APIResult<RegistrationResponse>> {

            // Login user.
            UserLoginUseCase.executeWithExecutor(context, user, loginHandler)

           // KMUserLoginUseCase Execution
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