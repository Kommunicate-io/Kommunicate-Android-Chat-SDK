package kommunicate.io.sample.rm

import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonArray
import io.kommunicate.Kommunicate
import kommunicate.io.sample.MainActivity
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.getRichMessagePayload
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRecyclerViewItemCount
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import com.applozic.mobicomkit.uiwidgets.R
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmCardRMAdapter
import kommunicate.io.sample.utils.hasWidthGreaterThan
import kommunicate.io.sample.utils.validateImage
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
class CardTest {

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
    fun testCardGenericMessageLayout() {
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

        sendMessageAsUser("card_generic")
        onView(isRoot())
            .perform(waitFor(3000))

        val richMessagePayloadJson = getRichMessagePayload(groupId.toString(), chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")

        // wait for card to load completely.
        onView(isRoot())
            .perform(waitFor(5000))

        val cardsCount = getRecyclerViewItemCount(allOf(withId(R.id.alGenericCardContainer), hasWidthGreaterThan(0)))

        for (position in 0 until cardsCount) {
            val cardContentFromServer = richMessagePayloadJson.get(position).asJsonObject

            onView(withRecyclerViewItem(allOf(withId(R.id.alGenericCardContainer), hasWidthGreaterThan(0)), position))
                .check { view, exception ->
                    // Validate that there should be no error in finding the view
                    assertNull(exception)

                    val headerImage = view.findViewById<ImageView>(R.id.productImage)
                    val overlayTextView = view.findViewById<TextView>(R.id.productPrice)
                    val productNameTextView = view.findViewById<TextView>(R.id.productName)
                    val productRatingTextView = view.findViewById<TextView>(R.id.productRating)
                    val productLocationTextView = view.findViewById<TextView>(R.id.productLocation)
                    val productDescriptionTextView = view.findViewById<TextView>(R.id.productDescription)
                    val productBooking1TextView = view.findViewById<TextView>(R.id.bookingAction1)
                    val productBooking2TextView = view.findViewById<TextView>(R.id.bookingAction2)
                    val productBooking3TextView = view.findViewById<TextView>(R.id.bookingAction3)

                    val cardTitle = cardContentFromServer.get("title").asString
                    val cardSubtitle = cardContentFromServer.get("subtitle").asString
                    val cardOverlayText = cardContentFromServer.get("header").asJsonObject.get("overlayText").asString
                    val cardHeaderImage = cardContentFromServer.get("header").asJsonObject.get("imgSrc").asString
                    val cardDescription = cardContentFromServer.get("description").asString
                    val cardTitleExt = cardContentFromServer.get("titleExt").asString
                    val cardButtons = cardContentFromServer.get("buttons").asJsonArray


                    assertEquals("Card title don't match", cardTitle, productNameTextView.text.toString())
                    assertEquals("Card sub title don't match", cardSubtitle, productLocationTextView.text.toString())
                    assertEquals("Card overlay don't match", cardOverlayText, overlayTextView.text.toString())
                    assertEquals("Card description don't match", cardDescription, productDescriptionTextView.text.toString())
                    assertEquals("Card title extension don't match", cardTitleExt, productRatingTextView.text.toString())

                    val text1 = cardButtons.get(0).asJsonObject.get("name").asString
                    assertEquals("Button 1 text doesn't match", text1, productBooking1TextView.text.toString())
                    val text2 = cardButtons.get(1).asJsonObject.get("name").asString
                    assertEquals("Button 2 text doesn't match", text2, productBooking2TextView.text.toString())
                    val text3 = cardButtons.get(2).asJsonObject.get("name").asString
                    assertEquals("Button 3 text doesn't match", text3, productBooking3TextView.text.toString())

                    validateImage(mActivityRule, cardHeaderImage, headerImage)
                }
        }
    }

    @Test
    fun testCardCarousalMessageLayout() {
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

        sendMessageAsUser("card_carousel")
        onView(isRoot())
            .perform(waitFor(3000))

        val richMessagePayloadJson = getRichMessagePayload(groupId.toString(), chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")

        // wait for card to load completely.
        onView(isRoot())
            .perform(waitFor(5000))

        val cardsCount = getRecyclerViewItemCount(allOf(withId(R.id.alGenericCardContainer), hasWidthGreaterThan(0)))

        for (position in 0 .. cardsCount) {
            val cardContentFromServer = richMessagePayloadJson.get(position).asJsonObject

            onView(withRecyclerViewItem(allOf(withId(R.id.alGenericCardContainer), hasWidthGreaterThan(0)), position))
                .check { rv, exception ->
                    // Validate that there should be no error in finding the view
                    assertNull(exception)

                    val view = (rv as RecyclerView).layoutManager?.findViewByPosition(position)
                    if (view == null) {
                        fail("unable to find item in recyclerview item at position $position")
                        return@check
                    }

                    val headerImage = view.findViewById<ImageView>(R.id.productImage)
                    val overlayTextView = view.findViewById<TextView>(R.id.productPrice)
                    val productNameTextView = view.findViewById<TextView>(R.id.productName)
                    val productRatingTextView = view.findViewById<TextView>(R.id.productRating)
                    val productLocationTextView = view.findViewById<TextView>(R.id.productLocation)
                    val productDescriptionTextView = view.findViewById<TextView>(R.id.productDescription)
                    val productBooking1TextView = view.findViewById<TextView>(R.id.bookingAction1)
                    val productBooking2TextView = view.findViewById<TextView>(R.id.bookingAction2)
                    val productBooking3TextView = view.findViewById<TextView>(R.id.bookingAction3)

                    val cardTitle = cardContentFromServer.get("title").asString
                    val cardSubtitle = cardContentFromServer.get("subtitle").asString
                    val cardOverlayText = cardContentFromServer.get("header").asJsonObject.get("overlayText").asString
                    val cardHeaderImage = cardContentFromServer.get("header").asJsonObject.get("imgSrc").asString
                    val cardDescription = cardContentFromServer.get("description").asString
                    val cardTitleExt = cardContentFromServer.get("titleExt").asString
                    val cardButtons = cardContentFromServer.get("buttons").asJsonArray


                    assertEquals("Card title don't match", cardTitle, productNameTextView.text.toString())
                    assertEquals("Card sub title don't match", cardSubtitle, productLocationTextView.text.toString())
                    assertEquals("Card overlay don't match", cardOverlayText, overlayTextView.text.toString())
                    assertEquals("Card description don't match", cardDescription, productDescriptionTextView.text.toString())
                    assertEquals("Card title extension don't match", cardTitleExt, productRatingTextView.text.toString())

                    val text1 = cardButtons.get(0).asJsonObject.get("name").asString
                    assertEquals("Button 1 text doesn't match", text1, productBooking1TextView.text.toString())
                    val text2 = cardButtons.get(1).asJsonObject.get("name").asString
                    assertEquals("Button 2 text doesn't match", text2, productBooking2TextView.text.toString())
                    val text3 = cardButtons.get(2).asJsonObject.get("name").asString
                    assertEquals("Button 3 text doesn't match", text3, productBooking3TextView.text.toString())

                    validateImage(mActivityRule, cardHeaderImage, headerImage)
                }

            onView(allOf(withId(R.id.alGenericCardContainer), hasWidthGreaterThan(0)))
                .perform(RecyclerViewActions.scrollToPosition<KmCardRMAdapter.CardViewHolder>(position + 1));
        }
    }
}