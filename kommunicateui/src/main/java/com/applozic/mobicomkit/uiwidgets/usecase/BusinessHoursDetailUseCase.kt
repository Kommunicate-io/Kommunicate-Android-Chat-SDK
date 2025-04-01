package com.applozic.mobicomkit.uiwidgets.usecase

import android.content.Context

import com.applozic.mobicomkit.uiwidgets.data.BusinessSettingsData
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.applozic.mobicomkit.uiwidgets.data.BusinessSettingsResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.devkit.api.account.user.UserClientService
import io.kommunicate.models.SessionCache
import io.kommunicate.usecase.UseCase

/**
 * Use case for fetching user details for a given user ID in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property teamId The Team Id whose business hour needs to be fetched
 */
class BusinessHoursDetailUseCase(
    private val context: Context,
    private val teamId: String
) : UseCase<APIResult<BusinessSettingsResponse>> {

    private val userService = UserClientService(context)

    override suspend fun execute(): APIResult<BusinessSettingsResponse> = withContext(Dispatchers.IO) {
        try {
            val businessSettingsFromCache = SessionCache.get<BusinessSettingsResponse>(teamId)
            if (businessSettingsFromCache != null) {
                return@withContext APIResult.success(businessSettingsFromCache)
            }

            val businessSettingsData = userService.businessHoursData

            if (!businessSettingsData.isNullOrEmpty()) {
                val type = object : TypeToken<BusinessSettingsData>(){}.type
                val apiResponse: BusinessSettingsData = Gson().fromJson(businessSettingsData, type)

                if (apiResponse.status == "success") {
                    val businessSettingsResponse = apiResponse.response.firstOrNull {
                        it.teamId.toString() == teamId
                    }

                    businessSettingsResponse?.let {
                        SessionCache.put(teamId, it, CACHE_REFRESH_TIME)
                        APIResult.success(it)
                    } ?: APIResult.failed("Unable to find business hour of given team id")
                } else {
                    APIResult.failed("Failed to fetch business hours settings.")
                }
            } else {
                APIResult.failed("Unable to fetch business hours settings.")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val CACHE_REFRESH_TIME: Long = 5 * 60 * 1000 // 5 sec

        /**
         * Executes the use case with the given [UseCaseExecutor]. This helper function invokes the
         * coroutine internally; don't invoke explicitly.
         *
         * This method provides a simpler way to execute the use case and handle success or failure
         * using a provided [TaskListener].
         *
         * @param context The Android context for accessing resources and services.
         * @param teamId The ID of the user whose details need to be fetched.
         * @param callback The callback to handle success or failure responses.
         * @return [UseCaseExecutor] for cancelling the coroutine.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            teamId: String,
            callback: TaskListener<BusinessSettingsResponse>
        ): UseCaseExecutor<BusinessHoursDetailUseCase, APIResult<BusinessSettingsResponse>> {

            val kmUserDetailUseCase = BusinessHoursDetailUseCase(context, teamId)
            val executor = UseCaseExecutor(
                kmUserDetailUseCase,
                onComplete = {
                    it.onSuccess { response: BusinessSettingsResponse ->
                        callback.onSuccess(response)
                    }
                    it.onFailure { ex: Exception ->
                        callback.onFailure(ex)
                    }
                },
                onFailed = { ex: Exception ->
                    callback.onFailure(ex)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
