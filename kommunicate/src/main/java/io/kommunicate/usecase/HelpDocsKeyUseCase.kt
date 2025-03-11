package io.kommunicate.usecase

import android.content.Context
import io.kommunicate.models.KmHelpDocKey
import io.kommunicate.preference.KmPreference
import io.kommunicate.services.KmUserService
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.kommunicate.devkit.api.MobiComKitClientService
import dev.kommunicate.commons.json.GsonUtils
import io.kommunicate.callbacks.TaskListener

/**
 * Use case for fetching the HelpDocs key for a given type.
 *
 * @property context The application [Context] for interacting with the SDK.
 * @property type The type of help document to fetch the key for.
 */
class HelpDocsKeyUseCase(
    private val context: Context,
    private val type: String
) : UseCase<APIResult<String>> {

    override suspend fun execute(): APIResult<String> = withContext(Dispatchers.IO) {
        try {
            var helpDocsKey = KmPreference.getInstance(context).helpDocsKey

            if (helpDocsKey == null) {
                val response = KmUserService(context).getHelpDocsKey(MobiComKitClientService.getApplicationKey(context), type)
                helpDocsKey = parseHelpDocsKey(response)
            }

            return@withContext if (helpDocsKey != null) {
                APIResult.success(helpDocsKey)
            } else {
                APIResult.failed("Unable to get Access key")
            }
        } catch (e: Exception) {
            return@withContext APIResult.failedWithException(e)
        }
    }

    /**
     * Parses the response and extracts the help docs key if successful.
     */
    private fun parseHelpDocsKey(data: String?): String? {
        val helpDocKey: KmHelpDocKey = GsonUtils.getObjectFromJson(data, KmHelpDocKey::class.java)
        return if (helpDocKey != null && "SUCCESS" == helpDocKey.code && !helpDocKey.message.isNullOrEmpty()) {
            KmPreference.getInstance(context).setHelpDocsKey(helpDocKey.message[0].accessKey)
            helpDocKey.message[0].accessKey
        } else {
            null
        }
    }

    companion object {
        /**
         * Executes the [HelpDocsKeyUseCase] and invokes the provided [KmFaqTaskListener] for success or failure handling.
         *
         * @param context The application [Context] required to interact with the Kommunicate SDK.
         * @param type The type of help document.
         * @param listener The [KmFaqTaskListener] to handle success and failure callbacks.
         * @return [UseCaseExecutor] to manage the execution of the use case, including cancellation.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            type: String,
            listener: TaskListener<String>
        ): UseCaseExecutor<HelpDocsKeyUseCase, APIResult<String>> {

            val kmHelpDocsKeyUseCase = HelpDocsKeyUseCase(context, type)
            val executor = UseCaseExecutor(
                kmHelpDocsKeyUseCase,
                { result: APIResult<String> ->
                    result.onSuccess { accessKey ->
                        listener.onSuccess(accessKey)
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
