package kommunicate.io.sample.rm

import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.applozic.mobicomkit.uiwidgets.R
import com.bumptech.glide.Glide
import io.kommunicate.Kommunicate
import kommunicate.io.sample.MainActivity
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.getRichMessagePayload
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.compareBitmaps
import kommunicate.io.sample.utils.drawableToBitmap
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRecyclerViewItemCount
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.validateImage
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kommunicate.io.sample.utils.withRecyclerViewItem
import kotlinx.coroutines.launch
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class CaptionImageTest {

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
    fun testSingleImageWithCaption() {
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

        sendMessageAsUser("image_single")
        onView(isRoot())
            .perform(waitFor(3000))

        val richMessagePayloadJson = getRichMessagePayload(groupId.toString(), chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")

        // Wait for image to load.
        onView(isRoot())
            .perform(waitFor(5000))

        onView(
            allOf(withId(R.id.alImageView))
        ).check { imageViewLayout, exception ->

            // Validate that there should be no error in finding the view
            assertNull(exception)

            val imageURL = richMessagePayloadJson.get(0).asJsonObject.get("url").asString
            val imageView = imageViewLayout as ImageView

            validateImage(mActivityRule, imageURL, imageView)
        }
    }

    @Test
    fun testMultipleImageWithCaption() {
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

        sendMessageAsUser("image_multiple")
        onView(isRoot())
            .perform(waitFor(3000))

        validateSentImage(groupId = groupId.toString())
    }


    private fun validateSentImage(groupId: String) {

        val richMessagePayloadJson = getRichMessagePayload(groupId, chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")

        // Wait for image to load.
        onView(isRoot())
            .perform(waitFor(5000))

        val recyclerViewCount = getRecyclerViewItemCount(withId(R.id.alImageListContainer))

        for (position in 0 until recyclerViewCount) {

            onView(withRecyclerViewItem(withId(R.id.alImageListContainer), position)).check { view, exception ->

                // Validate that there should be no error in finding the view
                assertNull(exception)

                val imageURL = richMessagePayloadJson.get(0).asJsonObject.get("url").asString
                val imageView = view.findViewById<ImageView>(R.id.alImageView)

                validateImage(mActivityRule, imageURL, imageView)
            }
        }
    }
}