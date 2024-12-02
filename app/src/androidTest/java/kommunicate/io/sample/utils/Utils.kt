package kommunicate.io.sample.utils

import android.util.Base64
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.google.gson.JsonParser
import io.kommunicate.users.KMUser
import kommunicate.io.sample.data.RequestTokenData
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.junit.Assert.fail
import java.util.concurrent.CountDownLatch
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.applozic.mobicomkit.uiwidgets.R
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher

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

fun waitForLatch(latch: CountDownLatch, interval: Long = 100): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription(): String = "Wait for CountDownLatch to reach zero"
        override fun perform(uiController: UiController, v: View?) {
            while (latch.count > 0) {
                uiController.loopMainThreadForAtLeast(interval)
            }
        }
    }
}

fun withItemCount(expectedCount: Int): ViewAssertion {
    return ViewAssertion { view, _ ->
        if (view !is RecyclerView) {
            throw NoMatchingViewException.Builder()
                .build()
        }

        val actualCount = view.adapter?.itemCount ?: 0
        assertThat(actualCount, `is`(expectedCount))
    }
}

fun getRandomKmUser(): KMUser {
    return KMUser().apply {
        userId = getRandomString(10)
        userName = getRandomString(10)
        email = "${getRandomString(10)}@${getRandomString(4, ignoreNums = true)}.${getRandomString(3, ignoreNums = true)}"
        contactNumber = getRandomString(10, allNumbers = true)
        password = getRandomString(10)
        countryCode = "IN"
        displayName = getRandomString()
    }
}

fun sendMessageAsUser(message: String) {
    onView(withId(R.id.conversation_message))
        .perform(ViewActions.typeText(message))
    closeSoftKeyboard()

    onView(withId(R.id.conversation_send))
        .perform(click())
}

fun hasChildren(greaterThan: Int = 0, lessThan: Int = Int.MAX_VALUE): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun matchesSafely(view: View): Boolean {
            val viewGrp = view as? ViewGroup ?: return false
            return viewGrp.childCount in greaterThan+1 until  lessThan
        }

        override fun describeTo(description: org.hamcrest.Description?) {
            description?.appendText("child count should be in range ${greaterThan+1} and ${lessThan-1}")
        }
    }
}

fun hasWidthGreaterThan(minWidth: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: org.hamcrest.Description?) {
            description?.appendText("Finding the view with width greater than $minWidth")
        }

        override fun matchesSafely(item: View?): Boolean {
            return (item?.width ?: 0) > 0
        }
    }
}