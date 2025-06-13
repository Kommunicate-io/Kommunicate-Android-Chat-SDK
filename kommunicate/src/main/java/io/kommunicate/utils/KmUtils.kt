package io.kommunicate.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.kommunicate.devkit.SettingsSharedPreference
import io.kommunicate.devkit.api.account.user.MobiComUserPreference
import io.kommunicate.devkit.api.account.user.User
import io.kommunicate.devkit.api.conversation.Message
import io.kommunicate.commons.AppContextService
import io.kommunicate.commons.commons.core.utils.Utils
import io.kommunicate.commons.file.FileUtils
import io.kommunicate.commons.json.GsonUtils
import io.kommunicate.BuildConfig
import io.kommunicate.KmSettings
import io.kommunicate.Kommunicate
import org.json.JSONException
import org.json.JSONObject
import java.io.File

object KmUtils {
    private const val TAG = "Kommunicate"
    const val LEFT_POSITION: Int = 0
    const val RIGHT_POSITION: Int = 2
    const val BOT_CUSTOMIZATION: String = "bot_customization"
    const val NAME: String = "name"
    const val ID: String = "id"
    private const val NO_CLASS_FOUND = "No class found for name : "
    const val SENTRY_KOMMUNICATE_VERSION: String = "KOMMUNICATE_VERSION"
    const val SENTRY_KOMMUNICATE_UI_VERSION: String = "KOMMUNICATE_UI_VERSION"
    const val SENTRY_SDK_ENVIRONMENT: String = "SDK_ENVIRONMENT"
    const val SENTRY_KOMMUNICATE_APP_ID: String = "KOMMUNICATE_APP_ID"
    const val SENTRY_KOMMUNICATE_APPLOGICS_JSON: String = "KOMMUNICATE_APPLOGICS_JSON"

    @JvmStatic
    fun isServiceDisconnected(
        context: Context,
        isAgentApp: Boolean,
        customToolbarLayout: RelativeLayout?
    ): Boolean {
        val isDebuggable =
            (0 != (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))
        val disconnect = (!isAgentApp
                && !isDebuggable)
                && (MobiComUserPreference.getInstance(context).pricingPackage == PackageType.STARTUP.value
                || MobiComUserPreference.getInstance(context).pricingPackage == PackageType.START_MONTHLY.value
                || MobiComUserPreference.getInstance(context).pricingPackage == PackageType.START_YEARLY.value
                || MobiComUserPreference.getInstance(context).pricingPackage == PackageType.TRIAL_ACCOUNT.value
                || MobiComUserPreference.getInstance(context).pricingPackage == PackageType.CHURNED_ACCOUNT.value)
        if (customToolbarLayout != null) {
            customToolbarLayout.visibility = View.GONE
        }
        return disconnect
    }

    @JvmStatic
    fun setBackground(context: Context?, view: View, resId: Int) {
        view.background = ContextCompat.getDrawable(context!!, resId)
    }

    @JvmStatic
    val isDeviceRooted: Boolean
        get() {
            if (!KmAppSettingPreferences.isRootDetectionEnabled || BuildConfig.DEBUG) {
                return false
            }

            var isRooted = false
            var process: Process? = null

            try {
                process = Runtime.getRuntime().exec("su")
                isRooted = true
            } catch (ex: Exception) {
                Log.d("RootDetection", "Process creation/execution failed " + ex.message)
            } finally {
                if (process != null) {
                    try {
                        process.destroy()
                    } catch (e: Exception) {
                        Log.d("RootDetection", "Process termination failed " + e.message)
                    }
                }
            }

            return isRooted
        }

    @JvmStatic
    fun setDrawableTint(textView: TextView, colorId: Int, index: Int) {
        textView.compoundDrawables[index].colorFilter =
            PorterDuffColorFilter(colorId, PorterDuff.Mode.SRC_IN)
    }

    @JvmStatic
    fun setDrawableTint(imageView: ImageView, colorId: Int) {
        imageView.drawable.colorFilter = PorterDuffColorFilter(colorId, PorterDuff.Mode.SRC_IN)
    }

    @JvmStatic
    fun showToastAndLog(context: Context?, messageResId: Int) {
        Toast.makeText(context, messageResId, Toast.LENGTH_LONG).show()
        Utils.printLog(context, TAG, Utils.getString(context, messageResId))
    }

    @JvmStatic
    fun isAgent(context: Context?): Boolean {
        return User.RoleType.AGENT.value == MobiComUserPreference.getInstance(context).userRoleType
    }

    @JvmStatic
    val isAgent: Boolean
        get() = isAgent(AppContextService.getAppContext())

    @JvmStatic
    fun setGradientSolidColor(view: View, color: Int) {
        val gradientDrawable = view.background as GradientDrawable
        gradientDrawable.setColor(color)
    }

    @JvmStatic
    fun setGradientStrokeColor(view: View, width: Int, color: Int) {
        val gradientDrawable = view.background as GradientDrawable
        gradientDrawable.setStroke(width, color)
    }

    @JvmStatic
    fun setIconInsideTextView(
        textView: TextView,
        drawableRes: Int,
        color: Int,
        position: Int,
        padding: Int,
        isDarKMode: Boolean
    ) {
        if (position == LEFT_POSITION) {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0)
        } else if (position == RIGHT_POSITION) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        }
        textView.compoundDrawablePadding = padding
        if (color != Color.TRANSPARENT) {
            textView.compoundDrawables[position].colorFilter =
                PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
        if (isDarKMode) {
            textView.compoundDrawables[position].colorFilter =
                PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
    }

    @JvmStatic
    fun setIconInsideTextView(textView: TextView) {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }

    @JvmStatic
    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    @JvmStatic
    fun getDrawable(context: Context, resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun getCustomBotName(message: Message?, context: Context): String? {
        if (message != null) {
            val metadata: Map<String, String> = SettingsSharedPreference.getInstance(context).messageMetaData?.let {
                GsonUtils.getObjectFromJson<Any>(
                    it,
                    MutableMap::class.java
                ) as Map<String, String>
            } ?: return null

            if (
                metadata.containsKey(KmSettings.KM_CHAT_CONTEXT)
                && metadata[Kommunicate.KM_CHAT_CONTEXT]!!.contains(BOT_CUSTOMIZATION)
            ) {
                try {
                    val custombotObject = JSONObject(metadata[Kommunicate.KM_CHAT_CONTEXT]!!)
                    val botDataObject = JSONObject(custombotObject.getString(BOT_CUSTOMIZATION))
                    if (
                        !TextUtils.isEmpty(message.contactIds)
                        && botDataObject.has(ID)
                        && message.contactIds == botDataObject.getString(ID)
                    ) {
                        return botDataObject.getString(NAME)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    @JvmStatic
    fun getAttachmentName(message: Message?): String {
        if (message == null) {
            return "File"
        }
        val fileMeta = message.fileMetas
        if (fileMeta == null && message.filePaths != null) {
            return File(message.filePaths[0]).name.replace(KmConstants.AWS_ENCRYPTED, "")
                .replace(message.createdAtTime.toString(), "")
        } else if (message.filePaths != null) {
            return File(message.filePaths[0]).name.replace(KmConstants.AWS_ENCRYPTED, "")
                .replace(message.createdAtTime.toString(), "")
        }
        if (fileMeta != null && fileMeta.name != null) {
            val fileName =
                FileUtils.getName(fileMeta.name) + message.createdAtTime + "." + FileUtils.getFileFormat(
                    fileMeta.name
                )
            return fileName.replace(KmConstants.AWS_ENCRYPTED, "")
                .replace(message.createdAtTime.toString(), "")
        }
        return "File"
    }

    @JvmStatic
    @Throws(ClassNotFoundException::class)
    fun getClassFromName(className: String): Class<*> {
        try {
            return Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException(NO_CLASS_FOUND + className)
        }
    }

    enum class PackageType(val value: Int) {
        STARTUP(101),
        PER_AGENT_MONTHLY(102),
        PER_AGENT_YEARLY(103),
        GROWTH_MONTHLY(104),
        ENTERPRISE_MONTHLY(105),
        ENTERPRISE_YEARLY(106),
        EARLY_BIRD_MONTHLY(107),
        EARLY_BIRD_YEARLY(108),
        START_MONTHLY(112),
        START_YEARLY(113),
        TRIAL_ACCOUNT(111),
        CHURNED_ACCOUNT(100),
    }
}
