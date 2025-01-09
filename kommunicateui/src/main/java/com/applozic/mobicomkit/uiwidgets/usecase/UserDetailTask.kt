package com.applozic.mobicomkit.uiwidgets.usecase

import android.content.Context
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.applozic.mobicomkit.api.account.user.UserService
import com.applozic.mobicomkit.contact.AppContactService
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComConversationFragment
import com.applozic.mobicommons.people.contact.Contact
import io.kommunicate.models.SessionCache
import io.kommunicate.usecase.UseCase

/**
 * Use case for fetching user details for a given user ID in the Kommunicate SDK.
 *
 * @property context The Android context for accessing resources and services.
 * @property userId The ID of the user whose details need to be fetched.
 */
class UserDetailUseCase(
    private val context: Context,
    private val userId: String
) : UseCase<APIResult<Contact>> {

    override suspend fun execute(): APIResult<Contact> = withContext(Dispatchers.IO) {
        try {
            val contactFromCache = SessionCache.get<Contact>(userId)
            if (contactFromCache != null) {
                return@withContext APIResult.success(contactFromCache)
            }

            val userIdSet = setOf(userId)
            val userService = UserService.getInstance(context)
            val appContactService = AppContactService(context)

            userService.processUserDetailsByUserIds(userIdSet)
            val contact = appContactService.getContactById(userId)

            if (contact != null) {
                SessionCache.put(userId, contact, CACHE_REFRESH_TIME)
                APIResult.success(contact)
            } else {
                APIResult.failed("User details not found.")
            }
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    companion object {
        private const val CACHE_REFRESH_TIME: Long = 30 * 1000 // 30 sec

        /**
         * Executes the use case with the given [UseCaseExecutor]. This helper function invokes the
         * coroutine internally; don't invoke explicitly.
         *
         * This method provides a simpler way to execute the use case and handle success or failure
         * using a provided [KmUserDetailsCallback].
         *
         * @param context The Android context for accessing resources and services.
         * @param userId The ID of the user whose details need to be fetched.
         * @param callback The callback to handle success or failure responses.
         * @return [UseCaseExecutor] for cancelling the coroutine.
         */
        @JvmStatic
        fun executeWithExecutor(
            context: Context,
            userId: String,
            callback: MobiComConversationFragment.KmUserDetailsCallback? = null
        ): UseCaseExecutor<UserDetailUseCase, APIResult<Contact>> {

            val kmUserDetailUseCase = UserDetailUseCase(context, userId)
            val executor = UseCaseExecutor(
                kmUserDetailUseCase,
                { result: APIResult<Contact> ->
                    result.onSuccess { contact: Contact? ->
                        callback?.hasFinished(contact)
                    }
                    result.onFailure { errorMessage: Exception ->
                        callback?.hasFinished(null)
                    }
                },
                { _: Exception? ->
                    callback?.hasFinished(null)
                },
                Dispatchers.IO
            )
            executor.invoke()
            return executor
        }
    }
}
