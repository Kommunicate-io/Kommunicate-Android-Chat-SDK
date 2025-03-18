package io.kommunicate.usecase

import android.content.Context
import android.text.TextUtils
import io.kommunicate.devkit.api.account.user.MobiComUserPreference
import io.kommunicate.devkit.api.conversation.Message
import io.kommunicate.devkit.api.conversation.MobiComConversationService
import io.kommunicate.commons.commons.core.utils.DateUtils
import io.kommunicate.commons.people.channel.Channel
import io.kommunicate.commons.people.contact.Contact
import io.kommunicate.callbacks.TaskListener
import io.kommunicate.utils.APIResult
import io.kommunicate.utils.UseCaseExecutor
import io.kommunicate.utils.onFailure
import io.kommunicate.utils.onSuccess
import kotlinx.coroutines.Dispatchers
import java.util.Date

/**
 * Use case for retrieving message lists with advanced filtering and processing capabilities.
 *
 * This use case supports:
 * - Fetching latest messages grouped by people
 * - Retrieving messages for specific contact/channel
 * - Applying time range filters
 * - Optional search functionality
 *
 * @property context Android application context
 * @property searchString Optional search query to filter messages
 * @property contact Optional contact for message filtering
 * @property channel Optional channel for message filtering
 * @property startTime Start timestamp for message retrieval
 * @property endTime End timestamp for message retrieval
 * @property isForMessageList Flag to determine message list retrieval strategy
 */
class MessageListUseCase(
    private val context: Context,
    private val searchString: String?,
    private val contact: Contact?,
    private val channel: Channel?,
    private val startTime: Long?,
    private val endTime: Long? = null,
    private val isForMessageList: Boolean = false
) : UseCase<APIResult<List<Message>>> {

    /**
     * Executes message retrieval based on configured parameters.
     *
     * @return [APIResult] containing list of messages or error details
     */
    override suspend fun execute(): APIResult<List<Message>> {
        return try {
            val conversationService = MobiComConversationService(context)
            val messageList = if (isForMessageList) {
                conversationService.getLatestMessagesGroupByPeople(
                    startTime,
                    if (TextUtils.isEmpty(searchString)) null else searchString
                )
            } else {
                conversationService.getMessages(startTime, endTime, contact, channel, null)
            }

            APIResult.success(processMessageList(messageList))
        } catch (e: Exception) {
            APIResult.failedWithException(e)
        }
    }

    /**
     * Processes message list based on retrieval strategy.
     *
     * @param messageList Raw list of messages to process
     * @return Processed list of messages
     */
    private fun processMessageList(messageList: List<Message>?): List<Message> {
        return when {
            messageList.isNullOrEmpty() -> emptyList()
            isForMessageList -> processLatestMessages(messageList)
            else -> processChatMessages(messageList)
        }
    }

    /**
     * Processes messages for latest message list strategy.
     */
    private fun processLatestMessages(messageList: List<Message>): List<Message> {
        val recList = mutableListOf<String>()
        val messages = mutableListOf<Message>()

        messageList.forEach { message ->
            val uniqueKey = message.groupId?.let {
                if (it == 0) {
                    message.contactIds
                }else {
                    "group$it"
                }
            } ?: message.contactIds

            if (!recList.contains(uniqueKey)) {
                recList.add(uniqueKey)
                messages.add(message)
            }
        }

        if (messageList.isNotEmpty()) {
            MobiComUserPreference.getInstance(context)
                .setStartTimeForPagination(messageList.last().createdAtTime)
        }

        return messages
    }

    /**
     * Processes messages for chat message list strategy.
     */
    private fun processChatMessages(messageList: List<Message>): List<Message> {
        val mergedList = mutableListOf<Message>()

        if (messageList.isEmpty()) return emptyList()

        mergedList.apply {
            add(Message().apply { setInitialFirstMessage() })
            add(createDateMessage(messageList.first()))
            add(messageList.first())
        }

        for (i in 1 until messageList.size) {
            val previousMessage = messageList[i - 1]
            val currentMessage = messageList[i]

            val dayDifference = DateUtils.daysBetween(
                Date(previousMessage.createdAtTime),
                Date(currentMessage.createdAtTime)
            )

            if (dayDifference >= 1) {
                val dateMessage = createDateMessage(currentMessage)
                if (!mergedList.contains(dateMessage)) {
                    mergedList.add(dateMessage)
                }
            }

            if (!mergedList.contains(currentMessage)) {
                mergedList.add(currentMessage)
            }
        }

        return mergedList
    }

    /**
     * Creates a date marker message.
     *
     * @param message Reference message for date
     * @return Date marker message
     */
    private fun createDateMessage(message: Message): Message =
        Message().apply {
            setTempDateType(100)
            createdAtTime = message.createdAtTime
        }


    companion object {

        /**
         * @param context Android context
         * @param searchString Optional search query
         * @param contact Optional contact filter
         * @param channel Optional channel filter
         * @param startTime Message retrieval start timestamp
         * @param endTime Optional message retrieval end timestamp
         * @param isForMessageList Flag to determine retrieval strategy
         * @param callback Callback to handle result
         */
        @JvmStatic
        fun executeWithCallback(
            context: Context,
            searchString: String?,
            contact: Contact?,
            channel: Channel?,
            startTime: Long?,
            endTime: Long? = null,
            isForMessageList: Boolean = false,
            callback: TaskListener<List<Message>>
        ): UseCaseExecutor<MessageListUseCase, APIResult<List<Message>>> {
            val useCase = MessageListUseCase(
                context,
                searchString,
                contact,
                channel,
                startTime,
                endTime,
                isForMessageList
            )

            val executor = UseCaseExecutor(
                useCase,
                { result ->
                    result.onSuccess {
                        callback.onSuccess(it)
                    }
                    result.onFailure {
                        callback.onFailure(it)
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