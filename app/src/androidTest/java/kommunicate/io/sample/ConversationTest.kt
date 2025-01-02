package kommunicate.io.sample

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import com.applozic.mobicomkit.uiwidgets.R
import io.kommunicate.KmConversationBuilder
import io.kommunicate.KmSettings
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.users.KMUser
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper
import kommunicate.io.sample.utils.KmTestHelper.getBotIdsFromDashboard
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRandomKmUser
import kommunicate.io.sample.utils.getRandomString
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.log

@RunWith(AndroidJUnit4::class)
class ConversationTest {

    private val mActivityRule = ActivityScenario.launch(MainActivity::class.java)
    private lateinit var dashboardAPI: KommunicateDashboardAPI
    private lateinit var chatAPI: KommunicateChatAPI
    private lateinit var authToken: String
    private lateinit var chatAuthToken: String

    @Before
    fun setUp() {
        dashboardAPI = RetrofitClient.apiClient.create(KommunicateDashboardAPI::class.java)
        chatAPI = RetrofitClient.chatClient.create(KommunicateChatAPI::class.java)
        getAuthToken(dashboardAPI).let {
            chatAuthToken = it[0]
            authToken = it[1]
        }
        mActivityRule.onActivity {
            Kommunicate.init(it, "d6cbc2322c608519ad65ab3bcb09fe78", false)
        }
    }

    @Test
    fun testCreateSingleThreadConversation() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        var groupId = 0

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                // Login user
                loginUser(it, tempUser)
                // Create Conversation
                groupId = createConversation(it, true) as Int

                assertTrue("unable to create conversation with user from sdk. userid: ${tempUser.userId} group id: $groupId", groupId != 0)

            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))
    }

    @Test
    fun testLaunchAndSendMessageOnSingleThreadedConversationAndVerifyFromDashboard() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        var groupId = 0

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupId = createConversation(it, true) as Int
                val resultMessage = openConversation(it, groupId)
                assertEquals("conversation id miss-match created conversation with $groupId and opened $resultMessage", groupId, resultMessage)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        sendMessageAsUser(getRandomString())

        // Verify conversation id from backend.
        runBlocking {
            val serverMessageList = chatAPI.getMessageList(
                token = chatAuthToken,
                startIndex = 0,
                mainPageSize = 10,
                deletedGroupIncluded = false
            ).get("groupFeeds").asJsonArray

            val groupIdFromServer = serverMessageList.firstOrNull {
                it.asJsonObject.get("id").asInt == groupId
            }

            assertNotNull("expected groupId: $groupId from server. But unable to find", groupIdFromServer)
        }
    }

    @Test
    fun testLaunchSingleThreadConversationAndVerifyMessagesFromDashboard() {
        val tempUser = getRandomKmUser()
        val firstMessage = getRandomString()
        val secondMessage = getRandomString()
        var latch = CountDownLatch(1)
        var groupIdFirst = 0
        var groupIdSecond = 0

        // Disable single threaded conversation on dashboard to test it from here.
        setSingleThreadedConversationOnDashboard(false)

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupIdFirst = launchConversation(it, true) as Int
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        sendMessageAsUser(firstMessage)

        latch = CountDownLatch(1)
        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                groupIdSecond = launchConversation(it, true) as Int
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        assertEquals("Conversation Group ID miss-match. Single threaded conversation should have same group id irrespective of number of time launchConversation called.", groupIdFirst, groupIdSecond)
        sendMessageAsUser(secondMessage)

        // verify message on dashboard.
        verifyMessagesOnTheDashboard(groupIdFirst.toString(), listOf(firstMessage, secondMessage), tempUser.userId)
    }

    @Test
    fun testLaunchConversationWithPrefillMessageAndValidateMessageFromDashboard() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        val randomPrefillMessage = getRandomString()
        var groupId: Int = 0

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupId = launchConversation(it, chatPrefillMessage = randomPrefillMessage) as Int
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        // Verify message on Text View
        onView(withId(R.id.conversation_message))
            .check(matches(withText(randomPrefillMessage)))

        onView(withId(R.id.conversation_send))
            .perform(click())

        onView(isRoot())
            .perform(waitFor(3000))

        verifyMessagesOnTheDashboard(groupId.toString(), listOf(randomPrefillMessage), tempUser.userId)
    }

    @Test
    fun testLaunchConversationWithBotIdsAgentIDsTitleTeamId() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        val conversationTitle = getRandomString()
        var groupId: Int = 0
        val botIds = listOf("inline-code-34rpc", "kk-3s8r3", "richmessagetest-apbah")
        val teamId = "103785933"
        val agentIds = listOf("prateek.singh@kommunicate.io", "hello@gmail.com")

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupId = launchConversation(
                    it,
                    botIds = botIds,
                    teamId = teamId,
                    agentIds = agentIds,
                    title = conversationTitle
                ) as Int
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        sendMessageAsUser(getRandomString())

        // validate data on dashboard
        runBlocking {
            val dashboardGroupFeeds = chatAPI.getMessageList(
                token = chatAuthToken,
                startIndex = 0,
                groupId = groupId.toString(),
                pageSize = 20
            ).get("groupFeeds").asJsonArray.firstOrNull()?.asJsonObject

            if (dashboardGroupFeeds == null) {
                fail("unable to find group feeds on server. conversation id: $groupId")
                return@runBlocking
            }

            val conversationAssigneeIds = dashboardGroupFeeds.get("membersId").asJsonArray
            val tempList = mutableListOf<String>()
            tempList.addAll(botIds)
            tempList.addAll(agentIds)
            tempList.add(tempUser.userId)
            tempList.add("bot")
            val isAllUsersPresentInConversation = conversationAssigneeIds.all {
                tempList.contains(it.asString)
            }

            assertTrue("unable to find all the assignees of conversation on server. assignees: $tempList, server assignees $conversationAssigneeIds", isAllUsersPresentInConversation)
        }
    }

    @Test
    fun testUpdateConversationAssigneeAndVerifyFromDashboard() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        var groupId = 0
        val botIds = listOf("inline-code-34rpc")
        val updateBotAssigneeId = "kk-3s8r3"
        val agentIds = listOf("prateek.singh@kommunicate.io")

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupId = launchConversation(it, botIds = botIds) as Int
                updateConversationAssignee(it, groupId, updateBotAssigneeId)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch, waitAfterLatch = 5000))

        sendMessageAsUser(getRandomString())

        // validate data on dashboard
        runBlocking {
            val dashboardGroupFeeds = chatAPI.getMessageList(
                token = chatAuthToken,
                startIndex = 0,
                groupId = groupId.toString(),
                pageSize = 20
            ).get("groupFeeds").asJsonArray.firstOrNull()?.asJsonObject

            if (dashboardGroupFeeds == null) {
                fail("unable to find group feeds on server. conversation id: $groupId")
                return@runBlocking
            }

            val conversationAssigneeIds = dashboardGroupFeeds.get("membersId").asJsonArray
            val tempList = mutableListOf<String>()
            tempList.add(updateBotAssigneeId)
            tempList.add(tempUser.userId)
            tempList.addAll(agentIds)
            tempList.add("bot")
            val isAllUsersPresentInConversation = conversationAssigneeIds.all {
                tempList.contains(it.asString)
            }

            val isPastConversationAssigneePresent = conversationAssigneeIds.all {
                botIds.contains(it.asString)
            }

            assertTrue("unable to find all the assignees of conversation on server. assignees: $tempList, server assignees $conversationAssigneeIds", isAllUsersPresentInConversation && !isPastConversationAssigneePresent)
        }

        // Validate View for transferred conversation.
        onView(withId(R.id.km_transferred_text))
            .check(matches(withText(R.string.km_transferred_to_message)))

        onView(withId(R.id.km_transferred_to))
            .check(matches(withText("kk"))) // bot name
    }

    @Test
    fun testUpdateConversationTeamIdAndVerifyFromDashboard() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        var groupId = 0
        val botIds = listOf("inline-code-34rpc", "richmessagetest-apbah")
        val initialTeamId = "103785933"
        val updateTeamId= "106336264"
        val agentIds = listOf("prateek.singh@kommunicate.io")

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupId = launchConversation(it, botIds = botIds, teamId = initialTeamId) as Int
                updateConversationTeamId(it, groupId, updateTeamId)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        sendMessageAsUser(getRandomString())

        // validate data on dashboard
        runBlocking {
            val dashboardGroupFeeds = chatAPI.getMessageList(
                token = chatAuthToken,
                startIndex = 0,
                groupId = groupId.toString(),
                pageSize = 20
            ).get("groupFeeds").asJsonArray.firstOrNull()?.asJsonObject

            if (dashboardGroupFeeds == null) {
                fail("unable to find group feeds on server. conversation id: $groupId")
                return@runBlocking
            }

            val conversationAssigneeIds = dashboardGroupFeeds.get("membersId").asJsonArray.map {
                    it.asString
                }.toMutableList()
            conversationAssigneeIds.add(dashboardGroupFeeds.get("metadata").asJsonObject.get("KM_TEAM_ID").asString)

            val tempList = mutableListOf<String>()
            tempList.add(updateTeamId)
            tempList.add(tempUser.userId)
            tempList.addAll(agentIds)
            tempList.addAll(botIds)
            tempList.add("bot")
            val isAllUsersPresentInConversation = conversationAssigneeIds.all {
                tempList.contains(it)
            }

            val isPastConversationAssigneePresent = conversationAssigneeIds.contains(initialTeamId)

            assertTrue("unable to find all the assignees of conversation on server. assignees: $tempList, server assignees $conversationAssigneeIds", isAllUsersPresentInConversation && !isPastConversationAssigneePresent)
        }
    }

    @Test
    fun testConversationWithLanguageChange() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        val englishMessage = "hello"
        val spanishMessage = "hola"
        val newLanguageCode = "es"
        var groupId = 0

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupId = launchConversation(it) as Int
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        val botIds = getBotIdsFromDashboard(authToken, dashboardAPI)

        sendMessageAsUser(englishMessage)
        onView(isRoot()).perform(waitFor(5000))

        // validate bot reply for english message
        validateBotMessageReply(groupId, botIds)

        // change bot language.
        mActivityRule.onActivity {
            KmSettings.updateUserLanguage(it, newLanguageCode)
        }

        sendMessageAsUser(spanishMessage)
        onView(isRoot()).perform(waitFor(5000))

        // validate bot reply for spanish message
        validateBotMessageReply(groupId, botIds)
    }

    @Test
    fun testChangeAssigneeFromDashboardAndVerifyStatusOnSDK() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        var groupId = 0

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                groupId = launchConversation(it) as Int
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        sendMessageAsUser(getRandomString())

        runBlocking {
            val response = chatAPI.transferChatToBotUser(
                token = chatAuthToken,
                groupId = groupId.toString(),
                assigneeId = "prateek.singh@kommunicate.io"
            )
            if (!response.get("response").asString.equals("updated")) {
                fail("unable to transfer the chat to human")
                return@runBlocking
            }
        }

        onView(isRoot())
            .perform(waitFor(2500))

        onView(withId(R.id.toolbar_title))
            .check(matches(withText("trgfdgfd")))
    }

    private fun validateBotMessageReply(groupId: Int, bots: List<String>) = runBlocking {
        val dashboardMessages = chatAPI.getMessageList(
            token = chatAuthToken,
            startIndex = 0,
            groupId = groupId.toString(),
            pageSize = 20
        ).get("message").asJsonArray

        val lastMessage = dashboardMessages.firstOrNull()?.asJsonObject

        lastMessage?.let {
            // check message is from bot
            val to = it.get("to").asString
            assertTrue("Last message is from user only, message: ${it.get("message").asString}, botIds: $bots, Failed to receive message from bot.", bots.contains(to))
        } ?: fail("unable to find any message on dashboard of conversation id: $groupId")
    }

    private fun verifyMessagesOnTheDashboard(groupId: String, messages: List<String>, email: String) = runBlocking {
        val messagesListObject = chatAPI.getMessageList(
            token = chatAuthToken,
            startIndex = 0,
            groupId = groupId,
            pageSize = 20
        ).get("message").asJsonArray

        val tempMessageList = messages.toMutableList()
        messages.forEach { userMessage ->
            for (serverMessages in messagesListObject) {
                // Ignore bot message and message other than texts.
                if (serverMessages.asJsonObject.get("contentType").asInt != 0
                    || !serverMessages.asJsonObject.get("contactIds").asString.equals(email)){
                    continue
                }
                val currMessage = serverMessages.asJsonObject.get("message").asString
                if (userMessage == currMessage) {
                    tempMessageList.remove(userMessage)
                }
            }
        }
        if (tempMessageList.isNotEmpty()) {
            fail("unable to see the sent messages from SDK on dashboard $tempMessageList, groupId: $groupId")
        }
    }

    private suspend fun updateConversationTeamId(
        context: Context,
        conversationId: Int,
        newTeamId: String
    ) = suspendCancellableCoroutine { continuation ->
        KmSettings.updateTeamId(context, conversationId, "", newTeamId, object : KmCallback {
            override fun onSuccess(message: Any?) {
                continuation.resume("")
            }

            override fun onFailure(error: Any?) {
                continuation.resumeWithException(IllegalStateException("unable to update conversation team id throw error: $error"))
            }
        })
    }

    private suspend fun updateConversationAssignee(
        context: Context,
        conversationId: Int,
        newAssigneeId: String
    ) = suspendCancellableCoroutine { continuation ->
        KmSettings.updateConversationAssignee(context, conversationId, "", newAssigneeId, object : KmCallback {
            override fun onSuccess(message: Any?) {
                continuation.resume("")
            }

            override fun onFailure(error: Any?) {
                continuation.resumeWithException(IllegalStateException("unable to update conversation assignee throw error: $error"))
            }
        })
    }

    private suspend fun launchConversation(
        context: Context,
        singleThreaded: Boolean = false,
        chatPrefillMessage: String? = null,
        botIds: List<String>? = null,
        agentIds: List<String>? = null,
        title: String? = null,
        teamId: String? = null
    ) = suspendCancellableCoroutine { continuation ->
        val builder = KmConversationBuilder(context)
            .setSingleConversation(singleThreaded)

        chatPrefillMessage?.let {
            builder.setPreFilledMessage(it)
        }
        botIds?.let {
            builder.botIds = botIds
        }
        agentIds?.let {
            builder.agentIds = agentIds
        }
        title?.let {
            builder.conversationTitle = title
        }
        teamId?.let {
            builder.teamId = teamId
        }

        builder.launchConversation(object : KmCallback {
            override fun onSuccess(message: Any) {
                continuation.resume(message)
            }

            override fun onFailure(error: Any) {
                continuation.resumeWithException(IllegalStateException("unable to launch conversation throw error: $error"))
            }
        })
    }

    private suspend fun openConversation(context: Context, conversationId: Int) = suspendCancellableCoroutine { continuation ->
        Kommunicate.openConversation(context, conversationId, object : KmCallback {
            override fun onSuccess(message: Any) {
                continuation.resume(message)
            }

            override fun onFailure(error: Any) {
                continuation.resumeWithException(IllegalStateException("unable to create conversation throw error: $error"))
            }
        })
    }

    private suspend fun loginUser(context: Context, kmUser: KMUser) =
        suspendCancellableCoroutine { continuation ->
            Kommunicate.login(context, kmUser, object : KMLoginHandler {
                override fun onSuccess(
                    registrationResponse: RegistrationResponse?,
                    context: Context?
                ) {
                    continuation.resume(true)
                }

                override fun onFailure(
                    registrationResponse: RegistrationResponse?,
                    exception: Exception?
                ) {
                    fail("Unable to login user. Task failed with exception: $exception")
                }
            })
        }

    private suspend fun createConversation(
        context: Context,
        singleThreaded: Boolean = false,
        chatPrefillMessage: String? = null
    ) = suspendCancellableCoroutine { continuation ->
        val builder = KmConversationBuilder(context)
            .setSingleConversation(singleThreaded)

        if (chatPrefillMessage != null) {
            builder.setPreFilledMessage(chatPrefillMessage)
        }

        builder.createConversation(object : KmCallback {
            override fun onSuccess(message: Any) {
                continuation.resume(message)
            }

            override fun onFailure(error: Any) {
                continuation.resumeWithException(IllegalStateException("unable to create conversation throw error: $error"))
            }
        })
    }

    private fun setSingleThreadedConversationOnDashboard(enable: Boolean) = runBlocking {
        val jsonData = dashboardAPI.getUserSettings(authToken)
        val isPreChatEnable = jsonData.get("response").asJsonObject.get("collectLead").asBoolean
        if ((!isPreChatEnable && enable) || (isPreChatEnable && !enable)) {
            val preChatJson = """
                     {
                      "chatWidget": {
                        "popup": true,
                        "position": "right",
                        "iconIndex": 3,
                        "attachment": false,
                        "fileUpload": "awsS3Server",
                        "voiceInput": true,
                        "voiceOutput": true,
                        "primaryColor": "#ED495C",
                        "widgetImageLink": "",
                        "isSingleThreaded": $enable,
                        "notificationTone": "subtle",
                        "pseudonymsEnabled": false,
                        "preChatGreetingMsg": "PreChat Details",
                        "greetingMessageVolume": 1,
                        "botMessageDelayInterval": 1000,
                        "enableGreetingMessageInMobile": true
                      },
                      "appId": "d6cbc2322c608519ad65ab3bcb09fe78"
                    }
                """.trimIndent()
            val requestBody = preChatJson.toRequestBody("application/json".toMediaTypeOrNull())
            dashboardAPI.updateUserSettings(authToken, requestBody)
        }
    }
}