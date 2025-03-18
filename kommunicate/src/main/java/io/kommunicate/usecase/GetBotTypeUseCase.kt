package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.models.MessageTypeKmApiResponse
import io.kommunicate.preference.KmBotPreference
import io.kommunicate.services.KmUserService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.reflect.TypeToken
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.commons.json.GsonUtils

class GetBotTypeUseCase(
    private val context: Context,
    private val botId: String
) : UseCase<APIResult<String>> {

    override suspend fun execute(): APIResult<String> = withContext(Dispatchers.IO) {
        val userService = KmUserService(context)

        try {
            val response = userService.getBotDetailResponse(botId)

            if (response.isNullOrEmpty()) {
                return@withContext APIResult.failed(RESPONSE_STRING_NULL)
            }

            val responseType = object : TypeToken<MessageTypeKmApiResponse<List<BotDetailsResponseData>>>() {}.type
            val responseObject: MessageTypeKmApiResponse<List<BotDetailsResponseData>>? =
                GsonUtils.getObjectFromJson(response, responseType)

            val botType = responseObject?.data?.firstOrNull()?.aiPlatform

            if (!botType.isNullOrEmpty()) {
                KmBotPreference.getInstance(context).addBotType(botId, botType)
                APIResult.success(botType)
            } else {
                APIResult.failed("Bot type is empty")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val RESPONSE_STRING_NULL = "Response string for bot details null."

        /**
         * Executes the use case with a provided callback.
         *
         * @param context The Android context for accessing resources and services.
         * @param botId The bot ID to fetch its type.
         * @param callback The callback to handle success or failure.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            botId: String,
            callback: TaskListener<String>
        ): UseCaseExecutor<GetBotTypeUseCase, APIResult<String>> {

            val getBotTypeUseCase = GetBotTypeUseCase(context, botId)
            val executor = UseCaseExecutor(
                getBotTypeUseCase,
                { result: APIResult<String> ->
                    result.onSuccess { botType ->
                        callback.onSuccess(botType)
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

    data class BotDetailsResponseData(
        val aiPlatform: String? = null
    )
}
