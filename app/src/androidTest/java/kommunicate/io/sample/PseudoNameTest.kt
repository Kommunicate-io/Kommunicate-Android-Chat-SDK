package kommunicate.io.sample

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import okhttp3.RequestBody.Companion.toRequestBody
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.getAuthToken
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.math.exp

@RunWith(AndroidJUnit4::class)
class PseudoNameTest {

    private lateinit var authToken: String
    private lateinit var dashboardAPI: KommunicateDashboardAPI
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dashboardAPI = RetrofitClient.apiClient.create(KommunicateDashboardAPI::class.java)
        authToken = getAuthToken(dashboardAPI)[1]
        Kommunicate.init(context, "d6cbc2322c608519ad65ab3bcb09fe78", false)
    }

    @Test
    fun testPseudoNameIsCorrectWhenSettingsIsEnabled() {
        setPseudoNameOnDashboard(true)
        val randomUserName = createRandomUser()
        val correctDisplayNameRegex = "^([a-zA-Z]{2,}\\s[a-zA-Z]{1,}'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]{1,})?)".toRegex()
        if (!correctDisplayNameRegex.matches(randomUserName)) {
            fail("random user name is not as per the pseudo name")
        }
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
}