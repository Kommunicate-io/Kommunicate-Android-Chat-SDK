package kommunicate.io.sample

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.applozic.mobicomkit.uiwidgets.R
import io.kommunicate.Kommunicate
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kommunicate.io.sample.network.RetrofitClient
import kommunicate.io.sample.utils.KmTestHelper.getMessageAtPositionFromServer
import kommunicate.io.sample.utils.KmTestHelper.launchConversation
import kommunicate.io.sample.utils.performTapOnRecord
import kommunicate.io.sample.utils.getAuthToken
import kommunicate.io.sample.utils.waitFor
import kommunicate.io.sample.utils.waitForLatch
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AttachmentMessageTest {

    private lateinit var mActivityRule: ActivityScenario<MainActivity>
    private lateinit var dashboardAPI: KommunicateDashboardAPI
    private lateinit var chatAPI: KommunicateChatAPI
    private lateinit var authToken: String
    private lateinit var chatAuthToken: String

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Before
    fun setUp() {
        dashboardAPI = RetrofitClient.apiClient.create(KommunicateDashboardAPI::class.java)
        chatAPI = RetrofitClient.chatClient.create(KommunicateChatAPI::class.java)
        mActivityRule = ActivityScenario.launch(MainActivity::class.java)
        getAuthToken(dashboardAPI).let {
            chatAuthToken = it[0]
            authToken = it[1]
        }
        mActivityRule.onActivity {
            Kommunicate.init(it, "d6cbc2322c608519ad65ab3bcb09fe78", false)
        }
    }

    @After
    fun tearDown() {
        mActivityRule.close()
    }

    @Test
    fun testLocationAndVerifyFromDashboard() {
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

        onView(withId(R.id.location_btn))
            .perform(click())

        onView(isRoot())
            .perform(waitFor(2000))
        onView(withId(R.id.sendLocation))
            .perform(click())

        onView(isRoot())
            .perform(waitFor(5000))
        val messageFromServer =  getMessageAtPositionFromServer(chatAPI, chatAuthToken, groupId.toString(), 2)
        val message = messageFromServer?.get("message")?.asString!!
        assertTrue(message.contains("\"lat\":"))
        assertTrue(message.contains("\"lon\":"))
    }

    @Test
    fun testRecordAudioMessageVerifyFromDashboard() {
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

        onView(withId(R.id.audio_record_button))
            .perform(performTapOnRecord(5000))

        onView(isRoot())
            .perform(waitFor(10000))

        val messageFromServer =  getMessageAtPositionFromServer(chatAPI, chatAuthToken, groupId.toString(), 2)
        val fileMetadata = messageFromServer?.get("fileMeta")?.asJsonObject
        assertNotNull("Unable to find audio metadata on the server.", fileMetadata)
        assertTrue(fileMetadata?.has("url") ?: false)
        assertTrue(
            fileMetadata?.has("contentType") ?: false
            && fileMetadata?.get("contentType")?.asString.equals("audio/x-wav")
        )
    }

    @Test
    fun testCaptureImageFromCameraAndReflectOnDashboard() {
        Intents.init()
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWithFunction {
            val outputUri: Uri? = it.extras?.getParcelable(MediaStore.EXTRA_OUTPUT, Uri::class.java)
            createImageCaptureActivityResultStub(outputUri!!)
        }

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

        onView(withId(R.id.camera_btn))
            .perform(click())

        onView(isRoot())
            .perform(waitFor(15000))

        val messageFromServer =  getMessageAtPositionFromServer(chatAPI, chatAuthToken, groupId.toString(), 2)
        val fileMetadata = messageFromServer?.get("fileMeta")?.asJsonObject
        assertNotNull("Unable to find image metadata on the server.", fileMetadata)
        assertTrue(fileMetadata?.has("url") ?: false)
        assertTrue(
            fileMetadata?.has("contentType") ?: false
                    && fileMetadata?.get("contentType")?.asString.equals("image/jpeg")
        )

        Intents.release()
    }

    private fun createImageCaptureActivityResultStub(outputUri: Uri): Instrumentation.ActivityResult {
        createImageAndSaveToInternalStorage(outputUri)
        val resultData = Intent()
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }


    private fun createImageAndSaveToInternalStorage(uri: Uri) {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val contentResolver: ContentResolver = context.contentResolver

        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            val paint = Paint().apply { color = Color.GREEN }
            canvas.drawRect(0f, 0f, 200f, 200f, paint)  // Creates a red square
        }

        try {
            contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Log.d("Attachment", "Image saved successfully at $uri")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Attachment", "Failed to save image at $uri")
        }
    }

    fun savePickedImage(context: Context) {
        val bm = BitmapFactory.decodeResource(context.resources, io.kommunicate.app.R.mipmap.ic_launcher)
        val dir = context.externalCacheDir
        val file = File(dir?.path, "pickImageResult.jpeg")
        val outStream: FileOutputStream?
        try {
            outStream = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            with(outStream) {
                flush()
                close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}