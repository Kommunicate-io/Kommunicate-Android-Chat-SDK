package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.services.KmUserService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for fetching FAQ related data in the Kommunicate SDK.
 * It supports fetching articles, article answers, selected articles, or dashboard FAQs.
 *
 * @property context The application [Context] for interacting with the SDK.
 * @property accessKey The access key used for the API calls.
 * @property data Additional data required for some API calls (e.g., article ID, user query).
 */
class FaqUseCase(
    private val context: Context,
    private val accessKey: String,
    private val data: String,
    private val type: FAQType
) : UseCase<APIResult<String>> {

    private val kmUserService = KmUserService(context)

    override suspend fun execute(): APIResult<String> = withContext(Dispatchers.IO) {
        try {
            if (accessKey.isNotEmpty()) {
                when(type) {
                    FAQType.GET_ARTICLES -> return@withContext APIResult.success(kmUserService.getArticleList(accessKey))
                    FAQType.GET_SELECTED_ARTICLES -> return@withContext APIResult.success(kmUserService.getSelectedArticles(accessKey, data))
                    FAQType.GET_ANSWERS -> return@withContext APIResult.success(kmUserService.getArticleAnswer(accessKey, data))
                    FAQType.GET_DASHBOARD_FAQ -> return@withContext APIResult.success(kmUserService.getDashboardFaq(accessKey, data))
                }
            }

            return@withContext APIResult.failed("Invalid request configuration.")
        } catch (e: Exception) {
            return@withContext APIResult.failedWithException(e)
        }
    }

    companion object {
        /**x
         * Executes the [KMFaqUseCase] with the provided [TaskListener] for handling the result.
         *
         *
         * @param context The application [Context] required to interact with the Kommunicate SDK.
         * @param accessKey The access key used for the API calls.
         * @param data Additional data for some API requests (e.g., article ID, user query).
         * @param listener The [TaskListener] to handle the success or failure of the operation.
         * @return [UseCaseExecutor] to manage the execution of the use case, including cancellation.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            accessKey: String,
            data: String,
            type: FAQType,
            listener: TaskListener<String>
        ): UseCaseExecutor<FaqUseCase, APIResult<String>> {
            val kmFaqUseCase = FaqUseCase(context, accessKey, data, type)
            val executor = UseCaseExecutor(
                kmFaqUseCase,
                { result: APIResult<String> ->
                    result.onSuccess { responseData ->
                        listener.onSuccess(responseData)
                    }
                    result.onFailure { errorMessage ->
                        listener.onFailure(errorMessage)
                    }
                },
                { exception ->
                    listener.onFailure(exception)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}

enum class FAQType(type: String) {
    GET_ARTICLES("getArticles"),
    GET_SELECTED_ARTICLES("getSelectedArticles"),
    GET_ANSWERS("getAnswers"),
    GET_DASHBOARD_FAQ("getDashboardFaq");

    fun toFaqType(type: String): FAQType {
        return when(type) {
            "getDashboardFaq" -> GET_DASHBOARD_FAQ
            "getArticles" -> GET_ARTICLES
            "getSelectedArticles" -> GET_SELECTED_ARTICLES
            else -> GET_ANSWERS
        }
    }
}