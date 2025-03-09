package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.services.KmHttpClient
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class GetDataUseCase(
    private val context: Context,
    private var url: String,
    private val accept: String,
    private val contentType: String,
    private val data: String?,
    private val headers: Map<String, String>
) : UseCase<APIResult<String>> {

    override suspend fun execute(): APIResult<String> = withContext(Dispatchers.IO) {
        try {
            val finalUrl = if (!data.isNullOrEmpty()) {
                "$url${URLEncoder.encode(data, "UTF-8").trim()}"
            } else {
                url
            }

            val response = KmHttpClient(context).getResponseWithException(finalUrl, contentType, accept, headers)

            if (!response.isNullOrEmpty()) {
                APIResult.success(response)
            } else {
                APIResult.failed("Empty response received from the server.")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        /**
         * Executes the use case with a provided callback.
         *
         * @param context The Android context for accessing resources and services.
         * @param url The URL to fetch data from.
         * @param accept The accepted content type.
         * @param contentType The content type of the request.
         * @param data Optional data to append to the URL.
         * @param headers Headers for the HTTP request.
         * @param callback The callback to handle success or failure.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            url: String,
            accept: String,
            contentType: String,
            data: String? = null,
            headers: Map<String, String>,
            callback: TaskListener<String>
        ): UseCaseExecutor<GetDataUseCase, APIResult<String>> {

            val getDataUseCase = GetDataUseCase(context, url, accept, contentType, data, headers)
            val executor = UseCaseExecutor(
                getDataUseCase,
                { result: APIResult<String> ->
                    result.onSuccess { response ->
                        callback.onSuccess(response)
                    }
                    result.onFailure { error ->
                        callback.onFailure(error)
                    }
                },
                { exception ->
                    callback.onFailure(exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}