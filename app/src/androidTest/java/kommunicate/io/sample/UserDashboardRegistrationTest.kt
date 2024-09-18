package kommunicate.io.sample

import android.content.Context
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import com.google.gson.JsonParser
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import io.kommunicate.users.KMUser
import kommunicate.io.sample.data.RequestTokenData
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.getRandomString
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.coroutines.resume

@RunWith(AndroidJUnit4::class)
class UserDashboardRegistrationTest {

    private lateinit var authToken: String
    private lateinit var dashboardAPI: KommunicateDashboardAPI
    private lateinit var chatAPI: KommunicateChatAPI
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dashboardAPI = RetrofitClient.apiClient.create(KommunicateDashboardAPI::class.java)
        chatAPI = RetrofitClient.chatClient.create(KommunicateChatAPI::class.java)
        authToken = getAuthToken(dashboardAPI)[0]
        Kommunicate.init(context, "d6cbc2322c608519ad65ab3bcb09fe78", false)
    }

    private fun getMockUserObject(): KMUser {
        val tempUserId = UUID.randomUUID().toString()
        val tempDisplayName = getRandomString()
        val tempEmail = "${getRandomString()}@${getRandomString(4)}.${getRandomString(3)}"
        val tempPhone = getRandomString(10, true)

        return KMUser().apply {
            userId = tempUserId
            displayName = tempDisplayName
            email = tempEmail
            contactNumber = tempPhone
        }
    }

    @Test
    fun testDashboardDetailsVerification() {
        runBlocking {
            val user = getMockUserObject()
            loginWithKommunicate(user)
            verifyUserCreationOnDashboard(user)
        }
    }

    private suspend fun loginWithKommunicate(user: KMUser): Boolean =
        suspendCancellableCoroutine { continuation ->
            Kommunicate.login(context, user, object : KMLoginHandler {
                override fun onSuccess(registrationResponse: RegistrationResponse, context: Context?) {
                    continuation.resume(true)
                }

                override fun onFailure(
                    registrationResponse: RegistrationResponse?,
                    exception: Exception?
                ) {
                    fail("User login failed with exception $exception")
                }
            })
        }

    private fun verifyUserCreationOnDashboard(user: KMUser) = runBlocking {
        val dashboardDataResponse = chatAPI.getUsers(
            token = authToken,
            startIndex = 0,
            pageSize = 10,
            orderBy = 1,
            roleNameList = "USER",
            inactiveUser = true
        )
        val chatData = if (dashboardDataResponse.isSuccessful && dashboardDataResponse.body() != null) {
            dashboardDataResponse.body()!!
        }else {
            fail(dashboardDataResponse.errorBody().toString())
            return@runBlocking
        }

        val dashboardUser = chatData.response.users.firstOrNull {
            it.userId == user.userId
        }

        if (dashboardUser == null) {
            fail("unable to find the user on dashboard.")
        }

        dashboardUser!!.let {
            assert(it.userId == user.userId) {
                "userId in dashboard is not same"
            }
            assert(it.displayName == user.displayName) {
                "displayName in dashboard is not same"
            }
            assert(it.email == user.email) {
                "email in dashboard is not same"
            }
            assert(it.phoneNumber == user.contactNumber) {
                "phoneNumber in dashboard is not same"
            }
        }
    }
}