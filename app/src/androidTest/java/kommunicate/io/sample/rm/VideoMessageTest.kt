package kommunicate.io.sample.rm

import android.view.View
import android.webkit.WebView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kommunicate.Kommunicate
import kommunicate.io.sample.MainActivity
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.getRichMessagePayload
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRecyclerViewItemCount
import kommunicate.io.sample.utils.hasWidthGreaterThan
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kommunicate.io.sample.utils.withRecyclerViewItem
import kotlinx.coroutines.launch
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class VideoMessageTest {

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
    fun testVideoRichMessage() {
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

        sendMessageAsUser("video")
        onView(isRoot())
            .perform(waitFor(3000))

        val richMessagePayloadJson = getRichMessagePayload(groupId.toString(), chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")


        // Wait for video to load.
        onView(isRoot())
            .perform(waitFor(5000))

        val recyclerViewCount = getRecyclerViewItemCount(
            allOf(
                withId(io.kommunicate.ui.R.id.videoTemplateContainer),
                hasWidthGreaterThan(0)
            )
        )

        for (position in 0 until recyclerViewCount) {
            onView(
                withRecyclerViewItem(
                    allOf(
                        withId(io.kommunicate.ui.R.id.videoTemplateContainer),
                        hasWidthGreaterThan(0)
                    ),
                    position
                )
            ).check { view, exception ->

                // Validate that there should be no error in finding the view
                assertNull(exception)

                if (richMessagePayloadJson.get(position).asJsonObject.has("source")) {
                    validateYoutubeVideo(view)
                }else {
                    validateNormalVideo(view)
                }

                if (richMessagePayloadJson.get(position).asJsonObject.has("caption")) {
                    val captionView = view.findViewById<TextView>(io.kommunicate.ui.R.id.tv_caption)
                    assertEquals("Caption of video doesn't match", richMessagePayloadJson.get(position).asJsonObject.get("caption").asString, captionView.text)
                }
            }
        }
    }

    private fun validateYoutubeVideo(view: View) {
        val webView = view.findViewById<WebView>(io.kommunicate.ui.R.id.web_view)
        assertTrue("Web View Player is not visible", webView.visibility == View.VISIBLE)
    }

    private fun validateNormalVideo(view: View) {
        val mediaPlayer = view.findViewById<View>(io.kommunicate.ui.R.id.playerView)
        assertTrue("Video Player is not visible", mediaPlayer.visibility == View.VISIBLE)
    }
}