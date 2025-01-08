package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.models.KmAppSettingModel
import io.kommunicate.models.SessionCache
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.KmAppSettingPreferences
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Use case for fetching the app settings for a given app ID in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property appId The application ID for fetching the app settings.
 */
class AppSettingUseCase(
    private val context: Context,
    private val appId: String,
    private val updateCache: Boolean = false
) : UseCase<APIResult<KmAppSettingModel>> {

    override suspend fun execute(): APIResult<KmAppSettingModel> = withContext(Dispatchers.IO) {
        try {
            val cachedSettings = SessionCache.get<KmAppSettingModel>(CACHE_KEY)
            if (!updateCache && cachedSettings != null) {
                return@withContext APIResult.success(cachedSettings)
            }

            KmAppSettingPreferences.clearInstance()
            KmAppSettingPreferences.lastFetchTime = Calendar.getInstance().timeInMillis
            val appSettingModel = KmAppSettingPreferences.fetchAppSetting(context, appId)
                ?: return@withContext APIResult.failed("Unable to fetch app settings. Null value received")

            if (appSettingModel.isSuccess) {
                SessionCache.put(CACHE_KEY, appSettingModel, CACHE_REFRESH_TIME)
                APIResult.success(appSettingModel)
            } else {
                APIResult.failed("Failed to fetch app settings. ${appSettingModel.code}")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val CACHE_REFRESH_TIME: Long = 5 * 60 * 1000
        private const val CACHE_KEY = "KmAppSettingModel"

        /**
         * Executes the use case with the given [UseCaseExecutor]. This helper function invokes the
         * coroutine internally; don't invoke explicitly.
         *
         * This method provides a simpler way to execute the use case and handle success or failure
         * using a provided [KmCallback].
         *
         * @param context The Android context for accessing resources and services.
         * @param appId The application ID for fetching the app settings.
         * @param callback The callback to handle success or failure responses.
         * @return [UseCaseExecutor] for cancelling the coroutine.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            appId: String,
            callback: KmCallback? = null
        ): UseCaseExecutor<AppSettingUseCase, APIResult<KmAppSettingModel>> {
            return executeWithExecutor(context, appId, false, callback)
        }

        /**
         * Executes the use case with the given [UseCaseExecutor]. This helper function invokes the
         * coroutine internally; don't invoke explicitly.
         *
         * This method provides a simpler way to execute the use case and handle success or failure
         * using a provided [KmCallback].
         *
         * @param context The Android context for accessing resources and services.
         * @param appId The application ID for fetching the app settings.
         * @param callback The callback to handle success or failure responses.
         * @param updateCache If sent to true then it makes the API call and update the cache.
         * @return [UseCaseExecutor] for cancelling the coroutine.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            appId: String,
            updateCache: Boolean = false,
            callback: KmCallback? = null,
        ): UseCaseExecutor<AppSettingUseCase, APIResult<KmAppSettingModel>> {

            val kmAppSettingUseCase = AppSettingUseCase(context, appId, updateCache)
            val executor = UseCaseExecutor(
                kmAppSettingUseCase,
                { result: APIResult<KmAppSettingModel> ->
                    result.onSuccess { appSettingModel: KmAppSettingModel? ->
                        callback?.onSuccess(appSettingModel)
                    }
                    result.onFailure { errorMessage: Exception ->
                        callback?.onFailure(errorMessage)
                    }
                },
                { exception: java.lang.Exception? ->
                    callback?.onFailure(exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}