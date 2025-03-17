package kommunicate.io.sample

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kommunicate.devkit.api.account.register.RegistrationResponse
import io.kommunicate.KmConversationBuilder
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.users.KMUser
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRandomKmUser
import kommunicate.io.sample.utils.getRandomString
import kommunicate.io.sample.utils.waitForLatch
import kommunicate.io.sample.utils.withItemCount
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import kotlin.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@RunWith(AndroidJUnit4::class)
class ConversationListTest {

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
    fun testConversationCountOfUserAndVerifyConversationOnUIAndFromDashboard() {
        val kmUser = getRandomKmUser()
        var conversationIdList = emptyList<String>()
        val latch = CountDownLatch(1)

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                // Login user
                loginUser(it, kmUser)

                // Create conversations
                conversationIdList = listOf(
                    createConversation(it),
                    createConversation(it),
                    createConversation(it)
                )

                // Launch Conversation
                openConversationList(it)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch, 500))

        if (conversationIdList.isEmpty()) {
            fail("unable to create conversation with user from sdk. userid: ${kmUser.userId} conversation ids: $conversationIdList")
        }

        runBlocking {
            val serverMessageList = chatAPI.getMessageList(
                token = chatAuthToken,
                startIndex = 0,
                mainPageSize = 10,
                deletedGroupIncluded = false
            ).get("groupFeeds").asJsonArray

            val finalGroupConversations = serverMessageList.filter {
                conversationIdList.contains(it.asJsonObject.get("id").asString)
            }

            assertEquals(finalGroupConversations.size, conversationIdList.size)
        }

        onView(withId(com.applozic.mobicomkit.uiwidgets.R.id.messageList))
            .check(withItemCount(conversationIdList.size))
    }

    @Test
    fun testCreateRandomConversationAndOpenWithGroupId() {
        val kmUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        var conversationId = 0

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                // Login user
                loginUser(it, kmUser)

                // Create conversations
                conversationId = Integer.parseInt(createConversation(it))

                // Launch conversation with group id
                if (conversationId == 0) {
                    fail("unable to create the conversation with user id: ${kmUser.userId}")
                }

                val responseMessage = openConversationList(it, conversationId)
                assertEquals("conversation id doesn't match. Opened wrong conversation.", responseMessage, conversationId)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))
    }

    @Test
    fun testOpenConversationWithInvalidGroupId() {
        val kmUser = getRandomKmUser()
        val latch = CountDownLatch(1)
        val conversationId = Integer.parseInt(getRandomString(8, allNumbers = true))

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                try {
                    // Login user
                    loginUser(it, kmUser)
                    openConversationList(it, conversationId)
                } catch (e: IllegalStateException) {
                    assertEquals("Invalid Conversation id, Unable to find conversation with given ID.", e.message)
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for the coroutine to complete
        onView(isRoot()).perform(waitForLatch(latch))
    }

    private suspend fun createConversation(context: Context) =
        suspendCancellableCoroutine {  continuation ->
            KmConversationBuilder(context)
                .createConversation(object : KmCallback {
                    override fun onSuccess(message: Any) {
                        return continuation.resume(message.toString())
                    }

                    override fun onFailure(error: Any) {
                        fail("unable to create conversation error thrown: $error")
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

    private suspend fun openConversationList(context: Context, conversationId: Int? = null) = suspendCancellableCoroutine { continuation ->
        Kommunicate.openConversation(context, conversationId, object : KmCallback {
            override fun onSuccess(message: Any) {
                continuation.resume(message)
            }

            override fun onFailure(error: Any) {
                continuation.resumeWithException(IllegalStateException("$error"))
            }
        })
    }
}