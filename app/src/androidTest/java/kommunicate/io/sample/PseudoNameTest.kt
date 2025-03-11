package kommunicate.io.sample

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.kommunicate.devkit.api.account.register.RegistrationResponse
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import okhttp3.RequestBody.Companion.toRequestBody
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRandomString
import kommunicate.io.sample.utils.sendMessageAsUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume

@RunWith(AndroidJUnit4::class)
class PseudoNameTest {

    private val mActivityRule = ActivityScenario.launch(MainActivity::class.java)
    private lateinit var authToken: String
    private lateinit var dashboardAPI: KommunicateDashboardAPI
    private lateinit var chatAPI: KommunicateChatAPI
    private lateinit var chatAuthToken: String
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dashboardAPI = RetrofitClient.apiClient.create(KommunicateDashboardAPI::class.java)
        chatAPI = RetrofitClient.chatClient.create(KommunicateChatAPI::class.java)
        getAuthToken(dashboardAPI).let {
            chatAuthToken = it[0]
            authToken = it[1]
        }
        mActivityRule.onActivity {
            Kommunicate.init(context, "d6cbc2322c608519ad65ab3bcb09fe78", false)
        }
    }

    @Test
    fun testPseudoNameIsCorrectWhenSettingsIsEnabled() {
        val latch = CountDownLatch(1)

        setPseudoNameOnDashboard(true)
        val randomUserName = createRandomUser()
        val correctDisplayNameRegex = "^([a-zA-Z]{2,}\\s[a-zA-Z]{1,}'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]{1,})?)".toRegex()
        if (!correctDisplayNameRegex.matches(randomUserName)) {
            fail("random user name is not as per the pseudo name")
        }

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                launchConversation(it)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))
        sendMessageAsUser(getRandomString())
        onView(isRoot())
            .perform(waitFor(3000))

        val userName = getUserFromDashboardWithUserName(randomUserName)
        assertNotNull("Expected the same username from dashboard.", userName)
    }

    @Test
    fun testPseudoNameIsNotCorrectWhenSettingsIsDisabled() {
        setPseudoNameOnDashboard(false)
        val randomUserName = createRandomUser()
        val correctDisplayNameRegex = "^([a-zA-Z]{2,}\\s[a-zA-Z]{1,}'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]{1,})?)".toRegex()
        if (correctDisplayNameRegex.matches(randomUserName)) {
            fail("random user name is as per the pseudo name")
        }
    }

    private fun setPseudoNameOnDashboard(enable: Boolean) = runBlocking {
        val jsonData =  dashboardAPI.getUserSettings(authToken)
        val isPseudoNameEnable = jsonData.get("response").asJsonObject.get("chatWidget").asJsonObject.get("pseudonymsEnabled").asBoolean
        if ((!isPseudoNameEnable && enable) || (isPseudoNameEnable && !enable)) {
            val postEnableData = """
                {
                  "chatWidget": {
                    "pseudonymsEnabled": $enable
                  },
                  "appId": "d6cbc2322c608519ad65ab3bcb09fe78"
                }
            """.trimIndent()
            val requestBody = postEnableData.toRequestBody("application/json".toMediaTypeOrNull())
            dashboardAPI.updateUserSettings(authToken, requestBody)
        }
    }

    private fun createRandomUser(): String = runBlocking {
        suspendCancellableCoroutine { continuation ->
            Kommunicate.loginAsVisitor(context, object : KMLoginHandler {
                override fun onSuccess(
                    registrationResponse: RegistrationResponse,
                    context: Context
                ) {
                    continuation.resume(registrationResponse.displayName)
                }

                override fun onFailure(
                    registrationResponse: RegistrationResponse,
                    exception: Exception
                ) {
                    fail("creation of user as visitor failed with exception $exception")
                    continuation.cancel(exception)
                }
            })
        }
    }

    private fun getUserFromDashboardWithUserName(userName: String) = runBlocking {
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
                it.userName == userName
            }
        } else {
            fail("unable to communicate with users api: ${dashboardDataResponse.errorBody()}")
            return@runBlocking null
        }
        if (tempCreatedUser == null) {
            fail("no user found on the dashboard with email $userName")
            return@runBlocking null
        }
        return@runBlocking tempCreatedUser
    }
}