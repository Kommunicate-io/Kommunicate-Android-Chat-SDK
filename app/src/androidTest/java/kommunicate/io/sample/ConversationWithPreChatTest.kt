package kommunicate.io.sample

import android.app.ProgressDialog
import android.content.Context
import android.os.ResultReceiver
import android.view.View
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest
import io.kommunicate.ui.kommunicate.activities.LeadCollectionActivity.EMAIL_VALIDATION_REGEX
import io.kommunicate.ui.kommunicate.activities.LeadCollectionActivity.PHONE_NUMBER_VALIDATION_REGEX
import io.kommunicate.ui.kommunicate.adapters.KmPrechatInputAdapter
import com.google.android.material.textfield.TextInputEditText
import io.kommunicate.KmConversationBuilder
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.callbacks.KmPrechatCallback
import io.kommunicate.models.KmPrechatInputModel
import io.kommunicate.users.KMUser
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRandomString
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import io.kommunicate.ui.R as Rui

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class ConversationWithPreChatTest {

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
    fun testSignInWithPreChatAndConversationOnDashBoard() {
        val tempUserMail = "${getRandomString(12)}@${getRandomString(5, ignoreNums = true)}.${getRandomString(3, ignoreNums = true)}"
        val tempUserPhone = getRandomString(10, true)

        setPreChatEnabledOnDashboard(true)
        mActivityRule.onActivity {
            val progressDialog = ProgressDialog(it)
            progressDialog.setTitle("Test Login")
            progressDialog.setMessage("here is the test message")
            progressDialog.setCancelable(false)
            progressDialog.show()

            Kommunicate.launchConversationWithPreChat(it, progressDialog, object :KmCallback {
                override fun onSuccess(message: Any) {
                    progressDialog.dismiss()
                }

                override fun onFailure(error: Any?) {
                    progressDialog.dismiss()
                }
            })
        }
        onView(isRoot())
            .perform(waitFor(5000))

        onView(withId(Rui.id.kmPreChatRecyclerView))
            .perform(
                actionOnItemAtPosition<KmPrechatInputAdapter.KmPrechatInputViewHolder>(
                    0,
                    object : ViewAction {
                        override fun getConstraints(): Matcher<View> {
                            return isAssignableFrom(View::class.java)
                        }
                        override fun getDescription(): String {
                            return "test user email"
                        }
                        override fun perform(uiController: UiController?, view: View?) {
                            val editText = view?.findViewById<TextInputEditText>(Rui.id.prechatInputEt)
                            editText?.let {
                                it.performClick()
                                it.setText(tempUserMail)
                            }
                        }
                    }
                )
            )
            .perform(
                actionOnItemAtPosition<KmPrechatInputAdapter.KmPrechatInputViewHolder>(
                    1,
                    object : ViewAction {
                        override fun getConstraints(): Matcher<View> {
                            return isAssignableFrom(View::class.java)
                        }
                        override fun getDescription(): String {
                            return "test user phone"
                        }
                        override fun perform(uiController: UiController?, view: View?) {
                            val editText = view?.findViewById<TextInputEditText>(Rui.id.prechatInputEt)
                            editText?.let {
                                it.performClick()
                                it.setText(tempUserPhone)
                            }
                        }
                    }
                )
            )
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(Rui.id.start_conversation))
            .perform(click())

        onView(isRoot()).perform(waitFor(5000))

        // Send message
        val messageList = listOf(
            getRandomString(10),
            getRandomString(50),
            getRandomString(100)
        )
        messageList.forEach {
            sendMessageAsUser(it)
        }

        onView(isRoot())
            .perform(waitFor(5000))

        val groupId = getConversationGroupIdFromUserEmail(tempUserMail)
        verifyMessagesOnTheDashboard(groupId, messageList, tempUserMail)
    }

    @Test
    fun testSignInWithCustomPreChat() {
        val tempUserMail = "${getRandomString(12)}@${getRandomString(5, ignoreNums = true)}.${getRandomString(3, ignoreNums = true)}"
        val tempName = getRandomString(10)
        val tempUserPhone = getRandomString(10, true)
        val tempGender = "Female"

        mActivityRule.onActivity {
            val inputModelList: MutableList<KmPrechatInputModel> = mutableListOf()

            val emailField = KmPrechatInputModel().apply {
                this.type = KmPrechatInputModel.KmInputType.EMAIL
                this.isRequired = true
                this.placeholder = "Enter email"
                this.validationRegex = EMAIL_VALIDATION_REGEX
                this.field = "Email"
                this.compositeRequiredField = "Phone"
            }

            val nameField = KmPrechatInputModel().apply {
                this.type = KmPrechatInputModel.KmInputType.TEXT
                this.placeholder = "Enter Name"
                this.field = "Name"
            }

            val contactField = KmPrechatInputModel().apply {
                this.type = KmPrechatInputModel.KmInputType.NUMBER
                this.placeholder = "Enter Phone number"
                this.field = "Phone"
                this.validationRegex = PHONE_NUMBER_VALIDATION_REGEX
            }

            val dropdownField = KmPrechatInputModel().apply {
                this.options = mutableListOf("Male", "Female") //list of options to show
                this.placeholder = "Enter your gender"
                this.field = "Gender"
                this.element = "select" //element must be "select" for dropdown menu
            }

            inputModelList.add(emailField)
            inputModelList.add(nameField)
            inputModelList.add(contactField)
            inputModelList.add(dropdownField)

            it.lifecycleScope.launch {
                val data = launchAndHandleConversationWithPreChatForm(it, inputModelList)
                // Verify data inconsistency
                assertEquals(data["Email"], tempUserMail)
                assertEquals(data["Name"], tempName)
                assertEquals(data["Phone"], tempUserPhone)
                assertEquals(data["Gender"], tempGender)
            }
        }
        onView(withId(Rui.id.kmPreChatRecyclerView))
            .perform(
                actionOnItemAtPosition<KmPrechatInputAdapter.KmPrechatInputViewHolder>(
                    0,
                    object : ViewAction {
                        override fun getConstraints(): Matcher<View> {
                            return isAssignableFrom(View::class.java)
                        }
                        override fun getDescription(): String {
                            return "test user email"
                        }
                        override fun perform(uiController: UiController?, view: View?) {
                            val editText = view?.findViewById<TextInputEditText>(Rui.id.prechatInputEt)
                            editText?.let {
                                it.performClick()
                                it.setText(tempUserMail)
                            }
                        }
                    }
                )
            )
        onView(withId(Rui.id.kmPreChatRecyclerView))
            .perform(
                actionOnItemAtPosition<KmPrechatInputAdapter.KmPrechatInputViewHolder>(
                    1,
                    object : ViewAction {
                        override fun getConstraints(): Matcher<View> {
                            return isAssignableFrom(View::class.java)
                        }
                        override fun getDescription(): String {
                            return "test user name"
                        }
                        override fun perform(uiController: UiController?, view: View?) {
                            val editText = view?.findViewById<TextInputEditText>(Rui.id.prechatInputEt)
                            editText?.let {
                                it.performClick()
                                it.setText(tempName)
                            }
                        }
                    }
                )
            )
        onView(withId(Rui.id.kmPreChatRecyclerView))
            .perform(
                actionOnItemAtPosition<KmPrechatInputAdapter.KmPrechatInputViewHolder>(
                    2,
                    object : ViewAction {
                        override fun getConstraints(): Matcher<View> {
                            return isAssignableFrom(View::class.java)
                        }
                        override fun getDescription(): String {
                            return "test user phone"
                        }
                        override fun perform(uiController: UiController?, view: View?) {
                            val editText = view?.findViewById<TextInputEditText>(Rui.id.prechatInputEt)
                            editText?.let {
                                it.performClick()
                                it.setText(tempUserPhone)
                            }
                        }
                    }
                )
            )
        onView(withId(Rui.id.kmPreChatRecyclerView))
            .perform(
                actionOnItemAtPosition<KmPrechatInputAdapter.KmPrechatDropdownViewHolder>(
                    3,
                    object : ViewAction {
                        override fun getConstraints(): Matcher<View> {
                            return isAssignableFrom(View::class.java)
                        }
                        override fun getDescription(): String {
                            return "test user gender"
                        }
                        override fun perform(uiController: UiController?, view: View?) {
                            val spinner = view?.findViewById<Spinner>(Rui.id.prechatDropdownSpinner)
                            spinner?.let {
                                spinner.performClick()
                            }
                        }
                    }
                )
            )
        onData(
            allOf(
                `is`(instanceOf(String::class.java)),
                `is`(tempGender)
            )
        ).perform(click())

        onView(isRoot())
            .perform(waitFor(1500))

        onView(withId(Rui.id.start_conversation))
            .perform(click())
    }

    @Test
    @FlakyTest
    fun testLaunchConversationWithCustomUserWithoutUserId() {
        val tempUserMail = "${getRandomString(12)}@${getRandomString(5, ignoreNums = true)}.${getRandomString(3, ignoreNums = true)}"
        val tempName = getRandomString(10)
        val tempUserPhone = getRandomString(10, true)
        val tempGender = "Female"
        val latch = CountDownLatch(1)

        mActivityRule.onActivity { activity ->
            val user = KMUser().apply {
                email = tempUserMail
                displayName = tempName
                contactNumber = tempUserPhone
                metadata = mapOf("gender" to tempGender)
            }

            activity.lifecycleScope.launch {
                var exceptionThrown = false
                try {
                    buildAndLaunchConversationWithUser(activity, user)
                } catch (e: Exception) {
                    exceptionThrown = true
                    assertEquals("userId cannot be empty", e.message) // Verify exception message
                } finally {
                    assertTrue("Expected exception was not thrown", exceptionThrown)
                    latch.countDown()
                }
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))
    }

    @Test
    fun testLaunchConversationWithCustomUser() {
        val tempUserMail = "${getRandomString(12)}@${getRandomString(5, ignoreNums = true)}.${getRandomString(3, ignoreNums = true)}"
        val tempName = getRandomString(10)
        val tempUserPhone = getRandomString(10, true)
        val tempGender = "Female"
        val latch = CountDownLatch(1)

        mActivityRule.onActivity {
            val user = KMUser().apply {
                userId = tempUserMail
                email = tempUserMail
                displayName = tempName
                contactNumber = tempUserPhone
                metadata = mapOf("gender" to tempGender)
            }
            it.lifecycleScope.launch {
                buildAndLaunchConversationWithUser(it, user)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch,100))
        onView(isRoot())
            .perform(waitFor(5000))

        val user = getUserFromDashboardWithEmail(tempUserMail)
        assertNotNull("user not found on dashboard", user)
    }

    private suspend fun buildAndLaunchConversationWithUser(
        context: Context,
        user: KMUser
    ) = suspendCancellableCoroutine { continuation ->
        KmConversationBuilder(context).apply {
            kmUser = user
            launchConversation(object : KmCallback {
                override fun onSuccess(message: Any) {
                    continuation.resume(true)
                }
                override fun onFailure(error: Any) {
                    continuation.resumeWithException(error as Exception)
                }
            })
        }
    }

    private suspend fun launchAndHandleConversationWithPreChatForm(
        context: Context,
        inputModelList: MutableList<KmPrechatInputModel>
    ) = suspendCancellableCoroutine<Map<String, String>> { cancellable ->
        Kommunicate.launchPrechatWithResult(
            context,
            inputModelList,
            object : KmPrechatCallback<Map<String, String>> {
                override fun onReceive(
                    data: Map<String, String>,
                    context: Context?,
                    finishActivityReceiver: ResultReceiver?
                ) {
                   cancellable.resume(data)
                }

                override fun onError(error: String?) {
                   fail("conversation launch with Custom PreChat failed with exception $error")
                    cancellable.cancel(Exception(error))
                }
            }
        )
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
            fail("unable to see the send messages from SDK on dashboard $tempMessageList")
        }
    }

    private fun getConversationGroupIdFromUserEmail(email: String) = runBlocking {
        val user = getUserFromDashboardWithEmail(email)
        assertNotNull("unable to find user on dashboard", user)
        return@runBlocking user!!.messagePxy.groupId
    }

    private fun getUserFromDashboardWithEmail(email: String) = runBlocking {
        val dashboardDataResponse = chatAPI.getUsers(
            token = chatAuthToken,
            startIndex = 0,
            pageSize = 10,
            orderBy = 1,
            roleNameList = "USER",
            inactiveUser = true
        )
        val tempCreatedUser = if (dashboardDataResponse.isSuccessful && dashboardDataResponse.body() != null) {
            dashboardDataResponse.body()!!.response.users.firstOrNull {
                it.email == email
            }
        }else {
            fail("unable to communicate with users api: ${dashboardDataResponse.errorBody()}")
            return@runBlocking null
        }
        if (tempCreatedUser == null) {
            fail("no user found on the dashboard with email $email")
            return@runBlocking null
        }
        return@runBlocking tempCreatedUser
    }

    private fun setPreChatEnabledOnDashboard(enable: Boolean) = runBlocking {
        val jsonData = dashboardAPI.getUserSettings(authToken)
        val isPreChatEnable = jsonData.get("response").asJsonObject.get("collectLead").asBoolean
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
                      "collectLead": $enable,
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