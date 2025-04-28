package kommunicate.io.sample.rm

import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kommunicate.Kommunicate
import kommunicate.io.sample.MainActivity
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import io.kommunicate.ui.R
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import kommunicate.io.sample.utils.KmTestHelper.getRichMessagePayloadAsObject
import kommunicate.io.sample.utils.compareBitmaps
import kommunicate.io.sample.utils.drawableToBitmap
import kommunicate.io.sample.utils.getRecyclerViewItemCount
import kommunicate.io.sample.utils.hasWidthGreaterThan
import kommunicate.io.sample.utils.validateImage
import kommunicate.io.sample.utils.withRecyclerViewItem
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

@RunWith(AndroidJUnit4::class)
class ListMessageTest {

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
    fun testListMessageLayout() {
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

        sendMessageAsUser("list")
        onView(isRoot())
            .perform(waitFor(3000))

        val richMessagePayloadJson = getRichMessagePayloadAsObject(groupId.toString(), chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")

        // wait for list to load completely.
        onView(isRoot())
            .perform(waitFor(5000))

        validateUIRenderedProperly(richMessagePayloadJson)
        validateHeader(richMessagePayloadJson)
        validateListData(richMessagePayloadJson)
        validateLinkButton(richMessagePayloadJson)
    }

    private fun validateUIRenderedProperly(richMessagePayloadJson: JsonObject) {
        onView(allOf(withId(R.id.alListMessageLayout), hasWidthGreaterThan(0)))
            .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.headerImage), hasWidthGreaterThan(0)))
            .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.headerText), hasWidthGreaterThan(0)))
            .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.alListItemRecycler), hasWidthGreaterThan(0)))
            .check(matches(isDisplayed()))
    }

    private fun validateLinkButton(richMessagePayloadJson: JsonObject) {
        val recyclerViewCount = getRecyclerViewItemCount(
            allOf(withId(R.id.alActionButtonRecycler),
                hasWidthGreaterThan(0)
            )
        )
        val elementFromServer = richMessagePayloadJson.get("buttons").asJsonArray

        for (position in 0 until recyclerViewCount) {
            onView(withRecyclerViewItem(
                allOf(withId(R.id.alActionButtonRecycler),
                    hasWidthGreaterThan(0)
                ), position
            )).check { view, exception ->
                // Validate that there should be no error in finding the view
                assertNull(exception)

                val text = view.findViewById<TextView>(R.id.tv_action_button_name)

                val element = elementFromServer.get(position).asJsonObject
                val title = element.get("name").asString

                assertEquals(title, text.text.toString())
            }
        }
    }

    private fun validateListData(richMessagePayloadJson: JsonObject) {
       val recyclerViewCount = getRecyclerViewItemCount(
           allOf(withId(R.id.alListItemRecycler),
               hasWidthGreaterThan(0)
           )
       )
       val elementFromServer = richMessagePayloadJson.get("elements").asJsonArray

       for (position in 0 until recyclerViewCount) {
            onView(withRecyclerViewItem(
                allOf(withId(R.id.alListItemRecycler),
                    hasWidthGreaterThan(0)
                ), position
            )).check { view, exception ->
                // Validate that there should be no error in finding the view
                assertNull(exception)

                val imageView = view.findViewById<ImageView>(R.id.listItemImage)
                val text = view.findViewById<TextView>(R.id.listItemHeaderText)
                val messageText = view.findViewById<TextView>(R.id.listItemText)

                val element = elementFromServer.get(position).asJsonObject
                val imageUrl = element.get("imgSrc").asString
                val title = element.get("title").asString
                val desc = element.get("description").asString

                validateImage(mActivityRule, imageUrl, imageView)
                assertEquals(title, text.text.toString())
                assertEquals(desc, messageText.text.toString())
            }
        }
    }
    private fun validateHeader(richMessagePayloadJson: JsonObject) {
        // Validate header text
        val expectedText = richMessagePayloadJson.get("headerText").asString
        onView(allOf(withId(R.id.headerText), hasWidthGreaterThan(0)))
            .check(matches(withText(expectedText)))

        // Validate header image
        val imageURL = richMessagePayloadJson.get("headerImgSrc").asString
        onView(allOf(withId(R.id.alListMessageLayout), hasWidthGreaterThan(0)))
            .check { view, _ ->
                val imageView = view.findViewById<ImageView>(R.id.headerImage)
                validateImage(mActivityRule, imageURL, imageView)
            }
    }
}