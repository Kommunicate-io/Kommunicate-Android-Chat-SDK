package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicommons.json.GsonUtils
import com.google.gson.reflect.TypeToken
import io.kommunicate.callbacks.KmFeedbackCallback
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.models.FeedbackDetailsData
import io.kommunicate.models.KmApiResponse
import io.kommunicate.models.KmFeedback
import io.kommunicate.services.KmService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

/**
 * Use case for handling conversation feedback operations in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property kmFeedback The feedback data to be submitted (nullable if fetching feedback).
 * @property kmFeedbackDetails Details related to the feedback and conversation.
 */
class ConversationFeedbackUseCase(
    private val context: Context,
    private val kmFeedback: KmFeedback?,
    private val kmFeedbackDetails: FeedbackDetailsData
) : UseCase<APIResult<KmApiResponse<KmFeedback>>> {

    private val kmService = KmService(context)

    /**
     * Executes the feedback operation, either posting or fetching feedback.
     *
     * @return An [APIResult] indicating success or failure of the operation.
     */
    override suspend fun execute(): APIResult<KmApiResponse<KmFeedback>> = withContext(Dispatchers.IO) {
        try {
            val response = if (kmFeedback == null) {
                // Fetch conversation feedback
                val conversationId = kmFeedbackDetails.conversationId
                if (conversationId.isNullOrEmpty()) {
                    throw Exception(KM_FEEDBACK_ID_NULL)
                }
                kmService.getConversationFeedback(conversationId)
            } else {
                // Post conversation feedback
                kmService.postConversationFeedback(kmFeedback, kmFeedbackDetails)
            }

            if (response.isNullOrEmpty()) {
                APIResult.failed(FEEDBACK_NULL)
            } else {
                val type: Type = object : TypeToken<KmApiResponse<KmFeedback>>() {}.type
                val kmApiResponse: KmApiResponse<KmFeedback> = GsonUtils.getObjectFromJson(response, type)
                APIResult.success(kmApiResponse)
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val FEEDBACK_NULL = "Feedback Response string null."
        private const val KM_FEEDBACK_ID_NULL = "KmFeedback and conversation ID parameters null."

        /**
         * Executes the use case with the given [KmFeedbackCallback].
         * Provides an easy way to handle success or failure using a provided listener.
         *
         * @param context The Android context for accessing resources and services.
         * @param kmFeedback The feedback data to be submitted (nullable if fetching feedback).
         * @param kmFeedbackDetails Details related to the feedback and conversation.
         * @param callback Passes the data using [TaskListener] for handling success or failure callbacks.
         * @return [UseCaseExecutor] for managing coroutine execution.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            kmFeedback: KmFeedback?,
            kmFeedbackDetails: FeedbackDetailsData,
            callback: TaskListener<KmApiResponse<KmFeedback>>
        ): UseCaseExecutor<ConversationFeedbackUseCase, APIResult<KmApiResponse<KmFeedback>>> {
            val useCase = ConversationFeedbackUseCase(context, kmFeedback, kmFeedbackDetails)
            val executor = UseCaseExecutor(
                useCase,
                { result ->
                    result.onSuccess { kmApiResponse ->
                        callback.onSuccess(kmApiResponse)
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
