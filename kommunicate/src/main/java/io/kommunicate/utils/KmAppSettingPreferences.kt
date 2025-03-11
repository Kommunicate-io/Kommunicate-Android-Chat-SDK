package io.kommunicate.utils

import android.content.Context
import android.content.SharedPreferences
import annotations.CleanUpRequired
import dev.kommunicate.devkit.Applozic
import dev.kommunicate.devkit.api.account.user.MobiComUserPreference
import dev.kommunicate.commons.ApplozicService
import dev.kommunicate.commons.json.GsonUtils
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.models.KmAppSettingModel
import io.kommunicate.services.KmService
import io.kommunicate.usecase.AppSettingUseCase

object KmAppSettingPreferences {

    private var callback: KmCallback? = null
    const val CLEAR_THEME_INSTANCE: String = "CLEAR_THEME_INSTANCE"
    private const val KM_THEME_PREFERENCES = "KM_THEME_PREFERENCES"
    private const val KM_THEME_PRIMARY_COLOR = "KM_THEME_PRIMARY_COLOR"
    private const val KM_THEME_SECONDARY_COLOR = "KM_THEME_SECONDARY_COLOR"
    private const val KM_COLLECT_FEEDBACK = "KM_COLLECT_FEEDBACK"
    private const val KM_BOT_MESSAGE_DELAY_INTERVAL = "KM_BOT_MESSAGE_DELAY_INTERVAL"
    private const val BOT_TYPING_INDICATOR_INTERVAL = "BOT_TYPING_INDICATOR_INTERVAL"
    private const val LOGGED_IN_AT_TIME = "LOGGED_IN_AT_TIME"
    private const val CHAT_SESSION_DELETE_TIME = "CHAT_SESSION_DELETE_TIME"
    private const val HIDE_POST_CTA = "HIDE_POST_CTA"
    private const val UPLOAD_OVERRIDE_URL = "UPLOAD_OVERRIDE_URL"
    private const val UPLOAD_OVERRIDE_HEADER = "UPLOAD_OVERRIDE_HEADER"
    private const val SINGLE_THREADED = "IS_SINGLE_THREADED"
    private const val ROOT_DETECTION = "ROOT_DETECTION"
    private const val SSL_PINNING = "SSL_PINNING"
    private const val IN_APP_NOTIFICATION = "IN_APP_NOTIFICATION"
    private const val RATING_BASE = "RATING_BASE"
    private const val LAST_FETCH_TIME = "LAST_FETCH_TIME"

    @JvmStatic
    @CleanUpRequired(
        reason = "Not required as this is Object class",
        taskDependent = "Require all the subsequent class to be migrate or change usage."
    )
    @Deprecated(
        message = "Use the function directly without calling the instance. Changed because this class is migrated to Kotlin Object class.",
        replaceWith = ReplaceWith("eg: KmAppSettingPreferences.isRootDetectionEnabled()")
    )
    val instance = this

    private val preferences: SharedPreferences by lazy {
        ApplozicService.getAppContext()
            .getSharedPreferences(KM_THEME_PREFERENCES, Context.MODE_PRIVATE)
    }
    private val alpreferences: SharedPreferences by lazy {
        ApplozicService.getAppContext()
            .getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE)
    }

    @JvmStatic
    val isRootDetectionEnabled: Boolean
        get() = preferences.getBoolean(ROOT_DETECTION, true)

    @JvmStatic
    var isSSLPinningEnabled: Boolean
        get() = preferences.getBoolean(SSL_PINNING, false)
        set(isEnabled) {
            preferences.edit().putBoolean(SSL_PINNING, isEnabled).apply()
        }

    @JvmStatic
    var isInAppNotificationEnable: Boolean
        get() {
            return preferences.getBoolean(IN_APP_NOTIFICATION, true)
        }
        set(isEnabled) {
            preferences.edit().putBoolean(IN_APP_NOTIFICATION, isEnabled).apply()
        }

    var primaryColor: String?
        get() = preferences.getString(KM_THEME_PRIMARY_COLOR, null)
        private set(color) {
            color?.let {
                preferences.edit().putString(KM_THEME_PRIMARY_COLOR, color).apply()
            }
        }

    var secondaryColor: String?
        get() = preferences.getString(KM_THEME_SECONDARY_COLOR, null)
        private set(color) {
            color?.let {
                preferences.edit().putString(KM_THEME_SECONDARY_COLOR, color).apply()
            }
        }

    var isCollectFeedback: Boolean
        get() = preferences.getBoolean(KM_COLLECT_FEEDBACK, false)
        private set(isEnable) {
            preferences.edit().putBoolean(KM_COLLECT_FEEDBACK, isEnable).apply()
        }

    private var isHidePostCTA: Boolean
        get() = preferences.getBoolean(HIDE_POST_CTA, false)
        private set(isEnable) {
            preferences.edit().putBoolean(HIDE_POST_CTA, isEnable).apply()
        }

    private var loggedInAtTime: Long
        get() = preferences.getLong(LOGGED_IN_AT_TIME, 0)
        private set(loginTime) {
            preferences.edit().putLong(LOGGED_IN_AT_TIME, loginTime).apply()
        }

    private var chatSessionDeleteTime: Long
        get() = preferences.getLong(CHAT_SESSION_DELETE_TIME, 0)
        private set(deleteTime) {
            preferences.edit().putLong(CHAT_SESSION_DELETE_TIME, deleteTime).apply()
        }

    var uploadOverrideUrl: String?
        get() = preferences.getString(UPLOAD_OVERRIDE_URL, "")
        private set(url) {
            preferences.edit().putString(UPLOAD_OVERRIDE_URL, url).apply()
        }

    @JvmStatic
    var ratingBase: Int
        get() = preferences.getInt(RATING_BASE, 3)
        private set(base) {
            preferences.edit().putInt(RATING_BASE, base).apply()
        }

    @JvmStatic
    var lastFetchTime: Long
        get() = preferences.getLong(LAST_FETCH_TIME, 0L)
        set(base) {
            preferences.edit().putLong(LAST_FETCH_TIME, base).apply()
        }

    @Suppress("UNCHECKED_CAST")
    var uploadOverrideHeader: HashMap<String, String>
        get() = GsonUtils.getObjectFromJson<Any>(
            preferences.getString(
                UPLOAD_OVERRIDE_HEADER,
                null
            ), HashMap::class.java
        ) as HashMap<String, String>
        private set(headers) {
            preferences.edit().putString(
                UPLOAD_OVERRIDE_HEADER,
                GsonUtils.getJsonFromObject(headers, HashMap::class.java)
            ).apply()
        }

    val isSessionExpired: Boolean
        get() {
            if (chatSessionDeleteTime > 0 && loggedInAtTime == 0L) {
                loggedInAtTime = System.currentTimeMillis()
            }
            return loggedInAtTime > 0 && chatSessionDeleteTime > 0 && System.currentTimeMillis() - loggedInAtTime > chatSessionDeleteTime
        }

    var kmBotMessageDelayInterval: Int
        get() = preferences.getInt(KM_BOT_MESSAGE_DELAY_INTERVAL, 0)
        private set(delayInterval) {
            preferences.edit().putInt(KM_BOT_MESSAGE_DELAY_INTERVAL, delayInterval).apply()
        }

    var botTypingIndicatorInterval: Int
        get() = preferences.getInt(BOT_TYPING_INDICATOR_INTERVAL, 0)
        private set(delayInterval) {
            preferences.edit().putInt(BOT_TYPING_INDICATOR_INTERVAL, delayInterval).apply()
        }

    @JvmStatic
    @CleanUpRequired(
        reason = "Not used anywhere"
    )
    fun fetchAppSettingAsync(context: Context) {
        AppSettingUseCase.executeWithExecutor(
            context,
            Applozic.getInstance(context).applicationKey,
            object : KmCallback {
                override fun onSuccess(message: Any) {
                }

                override fun onFailure(error: Any) {
                }
            })
    }

    @JvmStatic
    fun fetchAppSetting(context: Context, appId: String): KmAppSettingModel? {
        val response: String? = KmService(context).getAppSetting(appId)
        val appSettingModel = response?.let {
            GsonUtils.getObjectFromJson<Any>(
                it,
                KmAppSettingModel::class.java
            ) as KmAppSettingModel
        }
        appSettingModel?.let {
            if (!it.isSuccess) {
                return@let
            }
            clearInstance()
            setAppSetting(appSettingModel)
        }
        return appSettingModel
    }

    @JvmStatic
    @CleanUpRequired(
        reason = "Not used anywhere"
    )
    @Deprecated("Not used anywhere will take in cleanup.")
    fun updateAppSetting(appSettingModel: KmAppSettingModel?) {
        if (appSettingModel != null && appSettingModel.isSuccess) {
            clearInstance()
            setAppSetting(appSettingModel)
        }
    }

    @JvmStatic
    fun setCallback(callback: KmCallback?) {
        this.callback = callback
    }


    @JvmStatic
    fun setRootDetection(isEnabled: Boolean) {
        preferences.edit().putBoolean(ROOT_DETECTION, isEnabled).apply()
    }

    fun clearInstance() {
        callback?.onSuccess(CLEAR_THEME_INSTANCE)
    }

    private fun setAppSetting(appSetting: KmAppSettingModel) {
        clearInstance()
        appSetting.chatWidget?.let {
            primaryColor = it.primaryColor
            secondaryColor = it.secondaryColor
            kmBotMessageDelayInterval = it.botMessageDelayInterval
            botTypingIndicatorInterval = it.botTypingIndicatorInterval
            chatSessionDeleteTime = it.sessionTimeout
            if (it.defaultUploadOverride != null) {
                uploadOverrideUrl = it.defaultUploadOverride.url
                uploadOverrideHeader = it.defaultUploadOverride.headers
            }
            checkIsSingleThreaded(it.isSingleThreaded)
            ratingBase = it.csatRatingBase
        }
        appSetting.response?.let {
            isCollectFeedback = it.isCollectFeedback
            isHidePostCTA = (it.isHidePostCTA)
        }
    }

    private fun checkIsSingleThreaded(isSingleConversation: Boolean) {
        val isSingleThreaded = alpreferences.getBoolean(SINGLE_THREADED, false)
        if (isSingleConversation != isSingleThreaded) {
            alpreferences.edit().putBoolean(SINGLE_THREADED, isSingleConversation).apply()
        }
    }
}
