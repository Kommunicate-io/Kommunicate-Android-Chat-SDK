package kommunicate.io.sample.rm

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kommunicate.Kommunicate
import kommunicate.io.sample.MainActivity
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.getLastMessageFromServer
import kommunicate.io.sample.utils.KmTestHelper.getRichMessagePayload
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.hasChildren
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class SuggestedRepliesTest {

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
    fun testSuggestedReplies() {
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

        sendMessageAsUser("suggested_replies")
        onView(isRoot())
            .perform(waitFor(3000))

        validateSuggestedReplies(
            groupId = groupId.toString(),
            buttonNumber = 1,
            greaterThan = 0,
            lessThan = 2
        )

        validateSuggestedReplies(
            groupId = groupId.toString(),
            buttonNumber = 12,
            greaterThan = 10,
            lessThan = 13
        )
    }

    private fun validateSuggestedReplies(
        groupId: String,
        buttonNumber: Int,
        greaterThan: Int,
        lessThan: Int
    ) {
        sendMessageAsUser(buttonNumber.toString())
        onView(isRoot())
            .perform(waitFor(3000))

        val richMessagePayloadJson = getRichMessagePayload(groupId, chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")

        onView(allOf(
            withId(com.applozic.mobicomkit.uiwidgets.R.id.kmFlowLayout),
            hasChildren(greaterThan, lessThan)
        )).check { flexboxLayoutView, exception ->
            // Validate that there should be no error in finding the view
            assertNull(exception)

            // Cast to ViewGroup to access child views
            val flexboxLayout = flexboxLayoutView as ViewGroup
            flexboxLayout.children.forEachIndexed { index,  view ->
                val linearLayout = view as LinearLayout
                val buttonView = linearLayout.getChildAt(0) as TextView
                val buttonNameFromServer = richMessagePayloadJson.get(index).asJsonObject.get("name").asString.trim()
                // Button name should be same...
                assertEquals(buttonNameFromServer, buttonView.text.toString())
            }
        }
    }
}