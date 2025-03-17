package kommunicate.io.sample.rm

import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.applozic.mobicomkit.uiwidgets.R
import io.kommunicate.Kommunicate
import kommunicate.io.sample.MainActivity
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.getLastMessageFromServer
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.clickSend
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.typeMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AutoSuggestionsTest {

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
    fun testAutoSuggestions() {
        var groupId: Int? = null
        val latch = CountDownLatch(1)

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

        sendMessageAsUser("auto_suggestions")
        onView(isRoot())
            .perform(waitFor(5000))

        typeMessageAsUser("lon")
        onView(isRoot())
            .perform(waitFor(2000))

        onData(equalTo("London"))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())

        clickSend()
        waitFor(5000)

        val message = getLastMessageFromServer(chatAPI, chatAuthToken, groupId.toString())
            ?: return fail("Unable to fetch last message from the server.")

        assertEquals("Message doesn't match", message.get("message").asString, "London")
    }
}