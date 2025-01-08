package com.applozic.mobicomkit.uiwidgets

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class Myapp: Application() {
    override fun onCreate() {
        super.onCreate()

        // Set Hindi locale
        setLocale("hi")
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode) // Hindi locale
        Locale.setDefault(locale)

        val config = Configuration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))
        } else {
            config.locale = locale
        }

        resources.updateConfiguration(config, resources.displayMetrics)
    }
}