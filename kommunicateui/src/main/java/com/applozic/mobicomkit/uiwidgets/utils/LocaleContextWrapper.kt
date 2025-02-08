package com.applozic.mobicomkit.uiwidgets.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LocaleContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, languageCode: String): ContextWrapper {
            val locale = Locale(languageCode) // Hindi locale
            Locale.setDefault(locale)

            val config: Configuration = Configuration()
            config.locale = locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
                config.setLocales(LocaleList(locale))
            }

            return LocaleContextWrapper(context.createConfigurationContext(config))
        }
    }
}