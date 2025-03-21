package kommunicate.io.sample

import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kommunicate.Kommunicate
import io.kommunicate.ui.R
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.getLastMessageFromServer
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class CSATRatingTest {

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
    fun testCsatWithPoorRating() {
        var groupId: Int? = null
        val latch = CountDownLatch(1)

        setCsatEnableOnDashboard(true)
        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                groupId = launchConversation(it, botIds = listOf("richmessagetest-apbah"))
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        assertTrue("Unable to start the conversation.", groupId != null)

        sendMessageAsUser("csat_resolve")
        onView(isRoot())
            .perform(waitFor(3000))
        resolveConversationOnDashboard(groupId = groupId.toString())

        onView(isRoot())
            .perform(waitFor(2000))

        writeFeedback(1)
        waitFor(2000)
        validateRating(groupId.toString(), 1)
    }

    @Test
    fun testCsatWithAverageRating() {
        var groupId: Int? = null
        val latch = CountDownLatch(1)

        setCsatEnableOnDashboard(true)
        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                groupId = launchConversation(it, botIds = listOf("richmessagetest-apbah"))
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        assertTrue("Unable to start the conversation.", groupId != null)

        sendMessageAsUser("csat_resolve")
        onView(isRoot())
            .perform(waitFor(3000))
        resolveConversationOnDashboard(groupId = groupId.toString())

        onView(isRoot())
            .perform(waitFor(2000))

        writeFeedback(2)
        waitFor(2000)
        validateRating(groupId.toString(), 2)
    }

    @Test
    fun testCsatWithGoodRating() {
        var groupId: Int? = null
        val latch = CountDownLatch(1)

        setCsatEnableOnDashboard(true)
        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                groupId = launchConversation(it, botIds = listOf("richmessagetest-apbah"))
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        assertTrue("Unable to start the conversation.", groupId != null)

        sendMessageAsUser("csat_resolve")
        onView(isRoot())
            .perform(waitFor(3000))
        resolveConversationOnDashboard(groupId = groupId.toString())

        onView(isRoot())
            .perform(waitFor(2000))

        writeFeedback(3)
        waitFor(2000)
        validateRating(groupId.toString(), 3)
    }

    private fun validateRating(groupId: String, value: Int) {
        val lastMessage = getLastMessageFromServer(chatAPI, chatAuthToken, groupId) ?: return fail("Unable to find the last message in the chat.")
        val message = lastMessage.get("message").asString
        assertEquals(message, "rated the conversation")
        val feedback = lastMessage.get("metadata").asJsonObject.get("feedback").asString
        when(value) {
            1 -> {
                assertTrue(feedback.contains("poor"))
                assertTrue(feedback.contains(":1"))
            }
            2 -> {
                assertTrue(feedback.contains("average"))
            }
            else -> {
                assertTrue(feedback.contains("good"))
                assertTrue(feedback.contains(":10"))
            }
        }
    }

    private fun writeFeedback(value: Int) {
        when(value) {
            1 -> {
                onView(withId(R.id.idRatingPoor))
                    .perform(click())
                onView(withId(R.id.idEditTextFeedback))
                    .perform(click(), typeText("poor"))
            }
            2 -> {
                onView(withId(R.id.idRatingAverage))
                    .perform(click())
                onView(withId(R.id.idEditTextFeedback))
                    .perform(click(), typeText("average"))
            }
            else -> {
                onView(withId(R.id.idRatingGood))
                    .perform(click())
                onView(withId(R.id.idEditTextFeedback))
                    .perform(click(), typeText("good"))
            }
        }

        onView(withId(R.id.idButtonSubmit))
            .perform(click())
    }


    private fun resolveConversationOnDashboard(groupId: String) = runBlocking {
        val response = chatAPI.resolveConversation(
            token = chatAuthToken,
            groupId = groupId
        )

        if (response.get("status").asString != "success") {
            fail("Unable to resolve conversation on dashboard. $response")
        }
    }

    private fun setCsatEnableOnDashboard(enable: Boolean) = runBlocking {
        val jsonData = dashboardAPI.getUserSettings(authToken)
        val isPreChatEnable = jsonData.get("response").asJsonObject.get("collectFeedback").asBoolean
        if ((!isPreChatEnable && enable) || (isPreChatEnable && !enable)) {
            val preChatJson = """
                    {
                      "leadCollection": [
                        {
                          "type": "email",
                          "field": "Email",
                          "required": false,
                          "placeholder": "Enter Email"
                        },
                        {
                          "type": "number",
                          "field": "Phone",
                          "required": false,
                          "placeholder": "Enter Phone Number"
                        }
                      ],
                      "collectLead": true,
                      "collectFeedback": $enable,
                      "chatWidget": {
                        "preChatGreetingMsg": "PreChat Details"
                      },
                      "appId": "d6cbc2322c608519ad65ab3bcb09fe78"
                    }
                """.trimIndent()
            val requestBody = preChatJson.toRequestBody("application/json".toMediaTypeOrNull())
            dashboardAPI.updateUserSettings(authToken, requestBody)
        }
    }
}