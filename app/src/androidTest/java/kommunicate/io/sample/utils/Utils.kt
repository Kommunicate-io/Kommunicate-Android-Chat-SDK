package kommunicate.io.sample.utils

import android.util.Base64
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.google.gson.JsonParser
import kommunicate.io.sample.data.RequestTokenData
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.junit.Assert.fail

/**
 * chatAuth = 0
 * dashboardAuth = 1
 * */
fun getAuthToken(dashboardAPI: KommunicateDashboardAPI): List<String> {
    val token = mutableListOf<String>()
    runBlocking {
        val tokenData = RequestTokenData(
            true,
            "JTdCJTIydXNlck5hbWUlMjIlM0ElMjJwcmF0ZWVrLnNpbmdoJTQwa29tbXVuaWNhdGUuaW8lMjIlMkMlMjJwYXNzd29yZCUyMiUzQSUyMkNvZGVtb25rMSUyNCUyMiUyQyUyMmFwcGxpY2F0aW9uTmFtZSUyMiUzQSUyMiUyMiUyQyUyMmFwcGxpY2F0aW9uSWQlMjIlM0ElMjJkNmNiYzIzMjJjNjA4NTE5YWQ2NWFiM2JjYjA5ZmU3OCUyMiUyQyUyMmRldmljZVR5cGUlMjIlM0EwJTdE"
        )
        val data = dashboardAPI.login(tokenData).data
        val decodedBytes: ByteArray = Base64.decode(data, Base64.URL_SAFE)
        val jsonData = String(decodedBytes, charset("UTF-8"))
        val jsonObject = JsonParser.parseString(jsonData).getAsJsonObject()
        token.add(jsonObject.get("result").asJsonObject.get("applozicUser").asJsonObject.get("authToken").asString)
        token.add(jsonObject.get("result").asJsonObject.get("token").asString)
    }
    if (token.isEmpty()) {
        fail("unable to get token from server")
    }
    return token
}

fun getRandomString(length: Int = 10, allNumbers: Boolean = false, ignoreNums: Boolean = false) : String {
    val allowedChars = if (allNumbers) {('0'..'9') + ('0'..'2')} else if (ignoreNums) {('A'..'Z') + ('a'..'z')} else { ('A'..'Z') + ('a'..'z') + ('0'..'9')}
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription(): String = "wait for $delay milliseconds"
        override fun perform(uiController: UiController, v: View?) {
            uiController.loopMainThreadForAtLeast(delay)
        }
    }
}