package io.kommunicate.ui.utils

import android.content.Context
import io.kommunicate.devkit.api.account.user.MobiComUserPreference
import io.kommunicate.commons.data.PrefSettings
import io.kommunicate.ui.BuildConfig
import io.kommunicate.utils.KmUtils
import io.sentry.Scope
import io.sentry.Sentry
import io.sentry.protocol.User

object SentryUtils {

    @JvmStatic
    fun configureSentryWithKommunicateUI(context: Context, appConfigJson: String = "") {
//        if(BuildConfig.DEBUG) {
//            Sentry.init { options ->
//                options.dsn = io.kommunicate.BuildConfig.SENTRY_DSN
//                options.isEnabled = false
//            }
//            return
//        }

        val appId = PrefSettings.getInstance(context).applicationKey
        Sentry.configureScope { scope: Scope ->
            // Setup Tags
            scope.setTag(KmUtils.SENTRY_SDK_ENVIRONMENT, BuildConfig.DEBUG.toString())
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_VERSION, io.kommunicate.BuildConfig.KOMMUNICATE_VERSION)
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_APP_ID, appId)
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_UI_VERSION, BuildConfig.KOMMUNICATE_UI_VERSION)
            scope.setExtra(KmUtils.SENTRY_KOMMUNICATE_APPLOGICS_JSON, appConfigJson)
        }

        // Setup User Info
        val id =  MobiComUserPreference.getInstance(context).userId
        val user = User()
        user.id = id
        Sentry.setUser(user)
    }
}