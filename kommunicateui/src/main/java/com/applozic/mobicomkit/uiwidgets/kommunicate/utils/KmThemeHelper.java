package com.applozic.mobicomkit.uiwidgets.kommunicate.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.ApplozicService;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.utils.KmAppSettingPreferences;

public class KmThemeHelper implements KmCallback {

    private static KmThemeHelper kmThemeHelper;
    private Context context;
    private KmAppSettingPreferences appSettingPreferences;
    private AlCustomizationSettings alCustomizationSettings;
    private int primaryColor = -1;
    private int secondaryColor = -1;
    private int sentMessageBackgroundColor = -1;
    private int sendButtonBackgroundColor = -1;
    private int sentMessageBorderColor = -1;

    public static KmThemeHelper getInstance(Context context, AlCustomizationSettings alCustomizationSettings) {
        if (kmThemeHelper == null) {
            kmThemeHelper = new KmThemeHelper(context, alCustomizationSettings);
        }
        return kmThemeHelper;
    }

    private KmThemeHelper(Context context, AlCustomizationSettings alCustomizationSettings) {
        this.context = ApplozicService.getContext(context);
        this.alCustomizationSettings = alCustomizationSettings;
        appSettingPreferences = KmAppSettingPreferences.getInstance();
        appSettingPreferences.setCallback(this);
    }

    public int getPrimaryColor() {
        if (primaryColor == -1) {
            String colorStr = appSettingPreferences.getPrimaryColor();
            primaryColor = !TextUtils.isEmpty(colorStr) ? Color.parseColor(colorStr) : context.getResources().getColor(R.color.applozic_theme_color_primary);
        }
        return primaryColor;
    }

    public int getSentMessageBackgroundColor() {
        if (sentMessageBackgroundColor == -1) {
            String colorStr = alCustomizationSettings.getSentMessageBackgroundColor();

            if (TextUtils.isEmpty(colorStr)) {
                colorStr = appSettingPreferences.getPrimaryColor();
            }
            sentMessageBackgroundColor = !TextUtils.isEmpty(colorStr) ? Color.parseColor(colorStr) : context.getResources().getColor(R.color.applozic_theme_color_primary);
        }
        return sentMessageBackgroundColor;
    }

    public int getSentMessageBorderColor() {
        if (sentMessageBackgroundColor == -1) {
            String colorStr = alCustomizationSettings.getSentMessageBorderColor();

            if (TextUtils.isEmpty(colorStr)) {
                colorStr = appSettingPreferences.getPrimaryColor();
            }
            sentMessageBorderColor = !TextUtils.isEmpty(colorStr) ? Color.parseColor(colorStr) : context.getResources().getColor(R.color.applozic_theme_color_primary);
        }
        return sentMessageBorderColor;
    }

    public int getSendButtonBackgroundColor() {
        if (sendButtonBackgroundColor == -1) {
            String colorStr = alCustomizationSettings.getSendButtonBackgroundColor();

            if (TextUtils.isEmpty(colorStr)) {
                colorStr = appSettingPreferences.getPrimaryColor();
            }
            sendButtonBackgroundColor = !TextUtils.isEmpty(colorStr) ? Color.parseColor(colorStr) : context.getResources().getColor(R.color.applozic_theme_color_primary);
        }
        return sendButtonBackgroundColor;
    }

    public int getSecondaryColor() {
        if (secondaryColor == -1) {
            String colorStr = appSettingPreferences.getSecondaryColor();
            secondaryColor = !TextUtils.isEmpty(colorStr) ? Color.parseColor(colorStr) : context.getResources().getColor(R.color.applozic_theme_color_primary_dark);
        }
        return secondaryColor;
    }

    public static void clearInstance() {
        kmThemeHelper = null;
    }

    @Override
    public void onSuccess(Object message) {
        if (message instanceof String) {
            switch ((String) message) {
                case KmAppSettingPreferences.CLEAR_THEME_INSTANCE:
                    clearInstance();
            }
        }
    }

    @Override
    public void onFailure(Object error) {

    }
}
