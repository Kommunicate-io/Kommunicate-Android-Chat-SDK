package io.kommunicate.utils

import android.content.Context
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference
import com.applozic.mobicommons.data.AlPrefSettings
import io.kommunicate.BuildConfig
import io.sentry.Scope
import io.sentry.Sentry
import io.sentry.protocol.User

object SentryUtils {

    @JvmStatic
    fun configureSentryWithKommunicate(context: Context) {
        if(BuildConfig.DEBUG) {
            Sentry.init { options ->
                options.dsn = BuildConfig.SENTRY_DSN
                options.isEnabled = false
            }
            return
        }

        val appId = AlPrefSettings.getInstance(context).applicationKey
        Sentry.configureScope { scope: Scope ->
            // Setup Tags
            scope.setTag(KmUtils.SENTRY_SDK_ENVIRONMENT, BuildConfig.DEBUG.toString())
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_VERSION, BuildConfig.KOMMUNICATE_VERSION)
            scope.setTag(KmUtils.SENTRY_KOMMUNICATE_APP_ID, appId)
        }

        // Setup User Info
        val id =  MobiComUserPreference.getInstance(context).userId
        val user = User()
        user.id = id
        Sentry.setUser(user)
    }
}