package com.applozic.mobicomkit.uiwidgets.utils

import android.content.Context
import android.content.SharedPreferences
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmFormStateModel
import com.applozic.mobicommons.ApplozicService

object RichMessageSharedPreference {

    private val RICH_MESSAGE_PREFERENCE = "RICH_MESSAGE_PREFERENCE"

    private val preferences: SharedPreferences by lazy {
        ApplozicService.getAppContext()
            .getSharedPreferences(RICH_MESSAGE_PREFERENCE, Context.MODE_PRIVATE)
    }

    @JvmStatic
    fun setFormData(messageKey: String, formStateData: KmFormStateModel) {
       preferences.edit().putString(messageKey, formStateData.toJson()).apply()
    }

    @JvmStatic
    fun getFormData(messageKey: String): KmFormStateModel? {
        val formStateJson = preferences.getString(messageKey, null) ?: return null
        return KmFormStateModel.fromJson(formStateJson)
    }

    @JvmStatic
    fun clearPreference() {
        preferences.edit().clear().apply()
    }
}