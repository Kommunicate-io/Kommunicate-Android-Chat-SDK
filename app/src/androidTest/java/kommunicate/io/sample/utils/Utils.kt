package kommunicate.io.sample.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import io.kommunicate.ui.R
import com.google.gson.JsonParser
import io.kommunicate.users.KMUser
import kommunicate.io.sample.data.RequestTokenData
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.fail
import java.util.concurrent.CountDownLatch


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

fun waitForLatch(latch: CountDownLatch, interval: Long = 100, waitAfterLatch: Long = 1000): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription(): String = "Wait for CountDownLatch to reach zero"
        override fun perform(uiController: UiController, v: View?) {
            while (latch.count > 0) {
                uiController.loopMainThreadForAtLeast(interval)
            }
            uiController.loopMainThreadForAtLeast(waitAfterLatch)
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
    typeMessageAsUser(message)
    clickSend()
}

fun clickSend() {
    onView(withId(R.id.conversation_send))
        .perform(click())
}

fun typeMessageAsUser(message: String) {
    onView(withId(R.id.conversation_message))
        .perform(click(), typeText(message))
    closeSoftKeyboard()
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
            return (item?.width ?: 0) > minWidth
        }
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun compareBitmaps(bitmap1: Bitmap, bitmap2: Bitmap): Boolean {
    return bitmap1.sameAs(bitmap2)
}


fun withRecyclerViewItem(viewMatcher: Matcher<View>, position: Int): Matcher<View> {
    return allOf(
        viewMatcher,
        atPosition(position)
    )
}

fun getRecyclerViewItemCount(viewMatcher: Matcher<View>): Int {
    var size = 0
    onView(viewMatcher)
        .check { view, _ ->
            size = (view as RecyclerView).size
        }
    return size
}

private fun atPosition(position: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun matchesSafely(view: View): Boolean {
            if (view !is RecyclerView) return false
            val layoutManager = view.layoutManager ?: return false
            return layoutManager.findViewByPosition(position) != null
        }

        override fun describeTo(description: Description?) {
            description?.appendText("at position $position")
        }
    }
}

fun disableAnimations() {
    val disableAnimationsCommand = listOf(
        "settings put global window_animation_scale 0",
        "settings put global transition_animation_scale 0",
        "settings put global animator_duration_scale 0"
    )

    disableAnimationsCommand.forEach { command ->
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
    }
}

fun clearAppData() {
    val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    uiAutomation.executeShellCommand("pm clear $packageName").close()
    Log.d("as", packageName)
}

fun performTapOnRecord(duration: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isDisplayed()
        }

        override fun getDescription(): String {
            return "Continuous press for $duration milliseconds"
        }

        override fun perform(uiController: UiController, view: View) {
            val coordinates = floatArrayOf(view.width / 2f, view.height / 2f)
            val startTime = SystemClock.uptimeMillis()

            // Initial DOWN event
            val downEvent = MotionEvent.obtain(
                startTime,
                startTime,
                MotionEvent.ACTION_DOWN,
                coordinates[0],
                coordinates[1],
                1.0f,
                1.0f,
                0,
                1.0f,
                1.0f,
                0,
                0
            )
            view.dispatchTouchEvent(downEvent)

            Thread.sleep(duration)

            // Final UP event
            val upEvent = MotionEvent.obtain(
                startTime,
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                coordinates[0],
                coordinates[1],
                1.0f,
                1.0f,
                0,
                1.0f,
                1.0f,
                0,
                0
            )
            view.dispatchTouchEvent(upEvent)

            // Cleanup
            downEvent.recycle()
            upEvent.recycle()

            // Ensure all events are processed
            uiController.loopMainThreadUntilIdle()
        }
    }
}