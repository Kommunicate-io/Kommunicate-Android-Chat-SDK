package io.kommunicate.utils

import android.content.Context
import com.applozic.mobicommons.data.AlPrefSettings
import io.kommunicate.BuildConfig
import io.kommunicate.users.KMUser
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
    }

    @JvmStatic
    fun configureSentrySessionWithUser(kmUser: KMUser?) {
        if (kmUser == null) {
            return
        }
        // Setup User Info
        val user = User()
        user.id = kmUser.userId
        user.name = kmUser.userName
        Sentry.setUser(user)
    }
}