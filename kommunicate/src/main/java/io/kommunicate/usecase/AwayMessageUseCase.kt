package io.kommunicate.usecase

import android.content.Context
import com.applozic.mobicomkit.api.MobiComKitClientService
import io.kommunicate.callbacks.KmAwayMessageHandler
import io.kommunicate.models.KmApiResponse
import io.kommunicate.services.KmService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

/**
 * Use case for fetching the away message for a given group ID in the Kommunicate SDK.
 *
 * @property context The application [Context] for interacting with the SDK.
 * @property groupId The ID of the group for which the away message needs to be fetched.
 */
class AwayMessageUseCase(
    private val context: Context,
    private val groupId: Int
) : UseCase<APIResult<KmApiResponse<KmApiResponse.KmDataResponse>>> {

    override suspend fun execute(): APIResult<KmApiResponse<KmApiResponse.KmDataResponse>> = withContext(Dispatchers.IO) {
        try {
            val awayMessageJson = KmService(context).getAwayMessage(MobiComKitClientService.getApplicationKey(context), groupId)

            if (awayMessageJson != null) {
                val type = object : TypeToken<KmApiResponse<KmApiResponse.KmDataResponse>>() {}.type
                val response = Gson().fromJson<KmApiResponse<KmApiResponse.KmDataResponse>>(awayMessageJson, type)

                return@withContext if (response.code == "SUCCESS") {
                    APIResult.success(response)
                } else {
                    APIResult.failed("Failed to fetch away message.")
                }
            }

            return@withContext APIResult.failed("Failed to fetch away message. Null response.")
        } catch (e: Exception) {
            return@withContext APIResult.failedWithException(e)
        }
    }

    companion object {
        /**
         * Executes the [AwayMessageUseCase] with the provided [KmAwayMessageHandler] for handling the result.
         *
         * This method wraps the use case execution and invokes the handler methods based on the result
         * of the operation (either success or failure).
         *
         * @param context The application [Context] required to interact with the Kommunicate SDK.
         * @param groupId The group ID for fetching the away message.
         * @param handler The [KmAwayMessageHandler] to handle the success or failure of the operation.
         * @return [UseCaseExecutor] to manage the execution of the use case, including cancellation.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            groupId: Int,
            handler: KmAwayMessageHandler
        ): UseCaseExecutor<AwayMessageUseCase, APIResult<KmApiResponse<KmApiResponse.KmDataResponse>>> {

            val kmAwayMessageUseCase = AwayMessageUseCase(context, groupId)
            val executor = UseCaseExecutor(
                kmAwayMessageUseCase,
                { result ->
                    result.onSuccess { response ->
                        handler.onSuccess(context, response.data)
                    }
                    result.onFailure { errorMessage ->
                        handler.onFailure(context, errorMessage, null)
                    }
                },
                { exception ->
                    handler.onFailure(context, exception, null)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
