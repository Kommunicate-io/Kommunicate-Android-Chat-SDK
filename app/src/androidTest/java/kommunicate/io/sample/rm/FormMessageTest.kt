package kommunicate.io.sample.rm

import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.forEachIndexed
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
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
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRecyclerViewItemCount
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import com.applozic.mobicomkit.uiwidgets.R
import kommunicate.io.sample.utils.KmTestHelper.getRichMessagePayload
import kommunicate.io.sample.utils.hasWidthGreaterThan
import kommunicate.io.sample.utils.withRecyclerViewItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail

@RunWith(AndroidJUnit4::class)
class FormMessageTest {

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
    fun testFormMessage() {
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

        sendMessageAsUser("form_msg")
        onView(isRoot())
            .perform(waitFor(5000))

        val richMessagePayloadJson = getRichMessagePayload(groupId.toString(), chatAPI, chatAuthToken)
            ?: return fail("unable to fetch rich message from server.")

        // wait for card to load completely.
        onView(isRoot())
            .perform(waitFor(5000))

        val count = getRecyclerViewItemCount(
            allOf(
                withId(R.id.alFormLayoutRecycler),
                hasWidthGreaterThan(0)
            )
        ) - 1

        for (position in 0 .. count) {
            val fieldTypeFromServer = richMessagePayloadJson.get(position).asJsonObject

            onView(withRecyclerViewItem(allOf(withId(R.id.alFormLayoutRecycler), hasWidthGreaterThan(0)), position))
                .check { rv, exception ->
                    // Validate that there should be no error in finding the view
                    assertNull(exception)

                    val view = (rv as RecyclerView).layoutManager?.findViewByPosition(position)
                    if (view == null) {
                        fail("unable to find item in recyclerview item at position $position")
                        return@check
                    }

                    when(fieldTypeFromServer.get("type").asString) {
                        "text", "password" -> {
                            val textLabel = view.findViewById<TextView>(R.id.km_form_label_text)
                            val editText = view.findViewById<EditText>(R.id.km_form_edit_text)

                            val dataObj = fieldTypeFromServer.get("data").asJsonObject
                            assertEquals("Text label is not same", textLabel.text.toString(), dataObj.get("label").asString)
                            assertEquals("Hint label is not same", editText.hint, dataObj.get("placeholder").asString)
                        }
                        "radio" -> {
                            val textLabel = view.findViewById<TextView>(R.id.km_form_label_text)
                            val radioContainer = view.findViewById<ViewGroup>(R.id.km_form_selection_layout)

                            val dataObj = fieldTypeFromServer.get("data").asJsonObject
                            val optionsArray = dataObj.get("options").asJsonArray
                            assertEquals("Title label is not same", textLabel.text.toString(), dataObj.get("title").asString)
                            radioContainer.forEachIndexed { index, view ->
                                val radioButton = view as RadioButton
                                assertEquals("Value radio is not same", radioButton.text, optionsArray.get(index).asJsonObject.get("label").asString)
                            }
                        }
                        "checkbox" -> {
                            val textLabel = view.findViewById<TextView>(R.id.km_form_label_text)
                            val checkBoxContainer = view.findViewById<ViewGroup>(R.id.km_form_selection_layout)

                            val dataObj = fieldTypeFromServer.get("data").asJsonObject
                            val optionsArray = dataObj.get("options").asJsonArray
                            assertEquals("Title label is not same", textLabel.text.toString(), dataObj.get("title").asString)
                            checkBoxContainer.forEachIndexed { index, view ->
                                val checkBox = view as CheckBox
                                assertEquals("Value radio is not same", checkBox.text, optionsArray.get(index).asJsonObject.get("label").asString)
                            }
                        }
                    }
                }
        }
    }
}