package com.applozic.mobicomkit.uiwidgets.kommunicate;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.ApplozicService;

public class KmSettings {

    private static KmSettings kmSettings;
    private static SharedPreferences sharedPreferences;
    private static final String ENABLE_FAQ_OPTION = "ENABLE_FAQ_OPTION";

    private KmSettings(Context context) {
        if (!TextUtils.isEmpty(MobiComKitClientService.getApplicationKey(context))) {
            sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), Context.MODE_PRIVATE);
        }
    }

    public static KmSettings getInstance(Context context) {
        if (kmSettings == null) {
            kmSettings = new KmSettings(ApplozicService.getContext(context));
        } else {
            if (!TextUtils.isEmpty(MobiComKitClientService.getApplicationKey(context))) {
                sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), Context.MODE_PRIVATE);
            }
        }
        return kmSettings;
    }

    public KmSettings setFaqOptionEnabled(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(ENABLE_FAQ_OPTION, enable).commit();
        }
        return this;
    }

    public boolean isFaqOptionEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(ENABLE_FAQ_OPTION, false);
        }
        return false;
    }
}

