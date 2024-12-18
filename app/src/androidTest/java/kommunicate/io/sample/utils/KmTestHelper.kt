package kommunicate.io.sample.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.test.core.app.ActivityScenario
import com.applozic.mobicomkit.api.conversation.Message
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.kommunicate.KmConversationBuilder
import io.kommunicate.callbacks.KmCallback
import kommunicate.io.sample.MainActivity
import kommunicate.io.sample.network.KommunicateChatAPI
import kommunicate.io.sample.network.KommunicateDashboardAPI
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object KmTestHelper {

    fun getBotIdsFromDashboard(autToken: String, dashboardAPI: KommunicateDashboardAPI): List<String> = runBlocking {
        val dashboardBotDetails = dashboardAPI
            .getBotDetails(autToken)
            .get("data")
            .asJsonArray

        dashboardBotDetails.map {
            it.asJsonObject.get("userName").asString
        }
    }

    suspend fun launchConversation(
        context: Context,
        singleThreaded: Boolean = false,
        chatPrefillMessage: String? = null,
        botIds: List<String>? = null,
        agentIds: List<String>? = null,
        title: String? = null,
        teamId: String? = null
    ) = suspendCancellableCoroutine { continuation ->
        val builder = KmConversationBuilder(context)
            .setSingleConversation(singleThreaded)

        chatPrefillMessage?.let {
            builder.setPreFilledMessage(it)
        }
        botIds?.let {
            builder.botIds = botIds
        }
        agentIds?.let {
            builder.agentIds = agentIds
        }
        title?.let {
            builder.conversationTitle = title
        }
        teamId?.let {
            builder.teamId = teamId
        }

        builder.launchConversation(object : KmCallback {
            override fun onSuccess(message: Any) {
                continuation.resume(message as Int)
            }

            override fun onFailure(error: Any) {
                continuation.resumeWithException(IllegalStateException("unable to launch conversation throw error: $error"))
            }
        })
    }

    fun getRichMessagePayload(
        groupId: String,
        chatAPI: KommunicateChatAPI,
        chatAuthToken: String
    ): JsonArray? {
        val messageFromServer = getRichMessageFromServer(groupId, chatAPI, chatAuthToken)
        val richMessagePayload = messageFromServer?.asJsonObject?.get("metadata")?.asJsonObject?.get("payload")?.asString
        return richMessagePayload?.let { JsonParser.parseString(it).asJsonArray }
    }

    fun getRichMessagePayloadAsObject(
        groupId: String,
        chatAPI: KommunicateChatAPI,
        chatAuthToken: String
    ): JsonObject? {
        val messageFromServer = getRichMessageFromServer(groupId, chatAPI, chatAuthToken)
        val richMessagePayload = messageFromServer?.asJsonObject?.get("metadata")?.asJsonObject?.get("payload")?.asString
        return richMessagePayload?.let { JsonParser.parseString(it).asJsonObject }
    }

    fun getLastMessageFromServer(
        chatAPI: KommunicateChatAPI,
        chatAuthToken: String,
        groupId: String
    ): JsonObject? = runBlocking {
        val dashboardMessages = chatAPI.getMessageList(
            token = chatAuthToken,
            startIndex = 0,
            groupId = groupId,
            pageSize = 20
        ).get("message")?.asJsonArray

        dashboardMessages?.firstOrNull()?.asJsonObject
    }

    private fun getRichMessageFromServer(
        groupId: String,
        chatAPI: KommunicateChatAPI,
        chatAuthToken: String
    ): JsonObject? {
        val lastMessage = getLastMessageFromServer(chatAPI, chatAuthToken, groupId)

        lastMessage?.let {
            // check message is rich message.
            val metadata = it.get("metadata")?.asJsonObject

            val isValidMetadata = metadata != null
                    && Message.RICH_MESSAGE_CONTENT_TYPE == metadata.get("contentType").asString

            assertTrue(isValidMetadata)
        } ?: fail("unable to find any message on dashboard of conversation id: $groupId")

        return lastMessage
    }
}

fun validateImage(mActivityRule: ActivityScenario<MainActivity>, imageURL: String, imageView: ImageView) {
    val imageViewBitmap = drawableToBitmap(imageView.drawable)
    val latch = CountDownLatch(1)

    mActivityRule.onActivity {
        Glide.with(it)
            .load(imageURL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    latch.countDown()
                    fail("Failed to load image: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    latch.countDown()
                    return false
                }

            }).into(imageView)
    }

    if (!latch.await(10, TimeUnit.SECONDS)) {
        fail("Image loading timed out after 10 seconds")
    }

    val loadedBitmap = drawableToBitmap(imageView.drawable)

    assertTrue(
        "The images do not match",
        compareBitmaps(imageViewBitmap, loadedBitmap)
    )
}