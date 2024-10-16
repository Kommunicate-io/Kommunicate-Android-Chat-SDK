package kommunicate.io.sample

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.users.KMUser
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRandomKmUser
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.hamcrest.Matchers.allOf
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import org.hamcrest.Matchers.containsString

@RunWith(AndroidJUnit4::class)
class FAQTest {

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
    fun testLaunchConversationAndFAQVisibility() {
        val tempUser = getRandomKmUser()
        val latch = CountDownLatch(1)

        mActivityRule.onActivity {
            it.lifecycleScope.launch {
                loginUser(it, tempUser)
                openConversationList(it)
            }.invokeOnCompletion {
                latch.countDown()
            }
        }

        onView(isRoot())
            .perform(waitForLatch(latch))

        // Click on FAQ Button
        onView(allOf(withId(com.applozic.mobicomkit.uiwidgets.R.id.kmFaqOption), isDisplayed()))
            .perform(click())

        // Check correct page is load in web view.
        onView(isRoot())
            .perform(waitFor(5000))

        onWebView()
            .check(webMatches(getCurrentUrl(), containsString("https://helpcenter.kommunicate.io/?appId=d6cbc2322c608519ad65ab3bcb09fe78&hideChat=true")));
    }

    private suspend fun loginUser(context: Context, kmUser: KMUser) =
        suspendCancellableCoroutine { continuation ->
            Kommunicate.login(context, kmUser, object : KMLoginHandler {
                override fun onSuccess(
                    registrationResponse: RegistrationResponse?,
                    context: Context?
                ) {
                    continuation.resume(true)
                }

                override fun onFailure(
                    registrationResponse: RegistrationResponse?,
                    exception: Exception?
                ) {
                    fail("Unable to login user. Task failed with exception: $exception")
                }
            })
        }

    private suspend fun openConversationList(context: Context, conversationId: Int? = null) = suspendCancellableCoroutine { continuation ->
        Kommunicate.openConversation(context, conversationId, object : KmCallback {
            override fun onSuccess(message: Any) {
                continuation.resume(message)
            }

            override fun onFailure(error: Any) {
                continuation.resumeWithException(IllegalStateException("unable to create conversation throw error: $error"))
            }
        })
    }
}