package com.applozic.mobicomkit.uiwidgets.utils

import android.content.Context
import android.content.pm.PackageManager
import com.applozic.mobicomkit.uiwidgets.BuildConfig
import com.applozic.mobicommons.data.AlPrefSettings
import io.kommunicate.utils.KmUtils
import io.sentry.Scope
import io.sentry.Sentry

object SentryUtils {

    @JvmStatic
    fun configureSentryWithKommunicateUI(context: Context, appConfigJson: String = "") {
        if(BuildConfig.DEBUG) {
            Sentry.init { options ->
                options.dsn = io.kommunicate.BuildConfig.SENTRY_DSN
                options.isEnabled = false
            }
            return
        }

        val appId = AlPrefSettings.getInstance(context).applicationKey
        Sentry.configureScope { scope: Scope ->
            // Setup Tags
            scope.setTag(KmUtils.SENTRY_SDK_ENVIRONMENT, BuildConfig.DEBUG.toString())
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_VERSION, io.kommunicate.BuildConfig.KOMMUNICATE_VERSION)
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_APP_ID, appId)
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_UI_VERSION, BuildConfig.KOMMUNICATE_UI_VERSION)
            scope.setExtra(KmUtils.SENTRY_KOMMUNICATE_APPLOGICS_JSON, appConfigJson)
        }
    }
}