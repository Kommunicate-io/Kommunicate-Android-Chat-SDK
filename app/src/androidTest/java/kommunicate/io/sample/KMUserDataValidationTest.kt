package kommunicate.io.sample

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.applozic.mobicomkit.uiwidgets.R
import io.kommunicate.KmConversationBuilder
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.users.KMUser
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRandomString
import kommunicate.io.sample.utils.waitFor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@RunWith(AndroidJUnit4::class)
class KMUserDataValidationTest {

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
    fun testKmUserAndValidateOnDashboard() {
        val kmTempUser = getTempKmUser(isWrongMetadata = false)
        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                buildAndLaunchConversationWithUser(it, kmTempUser)
            }
        }

        onView(isRoot())
            .perform(waitFor(8000))

        sendMessageAsUser(getRandomString(10))

        val userFromServer = getUserFromDashboardWithEmail(kmTempUser.email)
        assertNotNull("user not found on dashboard", userFromServer)

        val currUserFromServerMetadata = userFromServer!!.metadata
        kmTempUser.metadata.forEach {
            if (!currUserFromServerMetadata.contains(it.key)) {
                fail("unable to find the key ${it.key} on Dashboard metadata: $currUserFromServerMetadata")
            }
            if (!it.value.equals(currUserFromServerMetadata[it.key])) {
                fail("values of key: ${it.key} is not matching. Server value: ${it.value}, Temp user value: ${currUserFromServerMetadata[it.key]} Dashboard metadata: $currUserFromServerMetadata")
            }
        }
    }

    @Test
    fun testKmUserWithIncorrectMetaDataAndValidateOnDashboard() {
        val kmTempUser = getTempKmUser(isWrongMetadata = true)

        assertThrows("Expected Exception when keys with more that 30 char passed in metadata ${kmTempUser.metadata}", NullPointerException::class.java) {
            mActivityRule.onActivity {
                it.lifecycleScope.launch {
                    buildAndLaunchConversationWithUser(it, kmTempUser)
                }
            }

            onView(isRoot())
                .perform(waitFor(8000))
        }
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
                    continuation.resumeWithException(error as NullPointerException)
                }
            })
        }
    }

    private fun sendMessageAsUser(message: String) {
        onView(withId(R.id.conversation_message))
            .perform(ViewActions.typeText(message))
        closeSoftKeyboard()

        onView(withId(R.id.conversation_send))
            .perform(click())
    }

    private fun getTempKmUser(isWrongMetadata: Boolean): KMUser {
        val randomStringsMap = if (isWrongMetadata) mapOf(
            // Keys with more that 30 chars is invalid and must throw exception.
            getRandomString(40) to getRandomString(2),
            getRandomString(100) to getRandomString(2),
            getRandomString(1000) to getRandomString(2),
        ) else mapOf(
            getRandomString(10) to getRandomString(20),
            getRandomString(20) to getRandomString(40),
            getRandomString(30) to getRandomString(60),
        )

        return KMUser().apply {
            userId = getRandomString(10)
            userName = getRandomString(10)
            email = "${getRandomString(10)}@${getRandomString(5, ignoreNums = true)}.${getRandomString(3, ignoreNums = true)}"
            contactNumber = getRandomString(10, allNumbers = true)
            password = getRandomString(10)
            countryCode = "IN"
            metadata = randomStringsMap
        }
    }
}