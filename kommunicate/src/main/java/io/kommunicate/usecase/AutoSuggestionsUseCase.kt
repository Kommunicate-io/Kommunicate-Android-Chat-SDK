package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.commons.json.GsonUtils
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.models.KmApiResponse
import io.kommunicate.models.KmAutoSuggestionModel
import io.kommunicate.services.KmService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for fetching auto-suggestions in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 */
class AutoSuggestionsUseCase(
    private val context: Context
) : UseCase<APIResult<List<KmAutoSuggestionModel>>> {

    private val kmService = KmService(context)

    /**
     * Executes the auto-suggestions retrieval operation.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<List<KmAutoSuggestionModel>> = withContext(Dispatchers.IO) {
        try {
            val apiResponse = kmService.getKmAutoSuggestions()

            if (apiResponse != null && KmApiResponse.KM_AUTO_SUGGESSTION_SUCCESS_RESPONSE == apiResponse.code) {
                APIResult.success(apiResponse.data ?: emptyList())
            } else {
                val errorMessage = apiResponse?.data?.let {
                    GsonUtils.getJsonFromObject(it.toTypedArray(), Array<KmAutoSuggestionModel>::class.java)
                } ?: ERROR_OCCURRED
                APIResult.failed(errorMessage)
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val ERROR_OCCURRED = "Some error occurred"

        /**
         * Executes the use case with the given [TaskListener].
         * Provides an easy way to handle success or failure using a provided listener.
         *
         * @param context The Android context for accessing resources and services.
         * @param listener Optional [TaskListener] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            listener: TaskListener<List<KmAutoSuggestionModel>>? = null
        ): UseCaseExecutor<AutoSuggestionsUseCase, APIResult<List<KmAutoSuggestionModel>>> {
            val useCase = AutoSuggestionsUseCase(context)

            val executor = UseCaseExecutor(
                useCase,
                { result ->
                    result.onSuccess { data ->
                        listener?.onSuccess(data)
                    }
                    result.onFailure { error ->
                        listener?.onFailure(error)
                    }
                },
                { exception ->
                    listener?.onFailure(exception)
                },
                Dispatchers.IO
            )

            executor.invoke()
            return executor
        }
    }
}
