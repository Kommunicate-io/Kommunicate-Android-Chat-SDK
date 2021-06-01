package com.applozic.mobicomkit.uiwidgets.kommunicate.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.ApplozicService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.utils.KmAppSettingPreferences;

public class KmThemeHelper implements KmCallback {

    private static final String SUBMIT_BUTTON = "submit";
    private static final String LINK_BUTTON = "link";
    private static final String QUICK_REPLIES = "quickReply";
    private static KmThemeHelper kmThemeHelper;
    private final Context context;
    private final KmAppSettingPreferences appSettingPreferences;
    private final AlCustomizationSettings alCustomizationSettings;
    private Boolean collectFeedback;
    private int primaryColor = -1;
    private int secondaryColor = -1;
    private int sentMessageBackgroundColor = -1;
    private int sendButtonBackgroundColor = -1;
    private int sentMessageBorderColor = -1;
    private int messageStatusIconColor = -1;
    private int toolbarTitleColor = -1;
    private int toolbarSubtitleColor = -1;
    private int toolbarColor = -1;
    private int statusBarColor = -1;
    private int richMessageThemeColor = -1;
    private Map<String, Boolean> hidePostCTA = new HashMap<>();

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

    public int parseColorWithDefault(String color, int defaultColor) {
        try {
            return Color.parseColor(color);
        } catch (Exception invalidColorException) {
            return defaultColor;
        }
    }

    public int getPrimaryColor() {
        if (primaryColor == -1) {
            primaryColor = parseColorWithDefault(appSettingPreferences.getPrimaryColor(), context.getResources().getColor(R.color.applozic_theme_color_primary));
        }
        return primaryColor;
    }

    public int getToolbarTitleColor() {
        if (toolbarTitleColor == -1) {
            toolbarTitleColor = parseColorWithDefault(alCustomizationSettings.getToolbarTitleColor(), context.getResources().getColor(R.color.toolbar_title_color));
        }
        return toolbarTitleColor;
    }

    public int getToolbarSubtitleColor() {
        if (toolbarSubtitleColor == -1) {
            toolbarSubtitleColor = parseColorWithDefault(alCustomizationSettings.getToolbarSubtitleColor(), context.getResources().getColor(R.color.toolbar_subtitle_color));
        }
        return toolbarSubtitleColor;
    }

    public int getSentMessageBackgroundColor() {
        if (sentMessageBackgroundColor == -1) {
            String colorStr = alCustomizationSettings.getSentMessageBackgroundColor();

            if (TextUtils.isEmpty(colorStr)) {
                colorStr = appSettingPreferences.getPrimaryColor();
            }
            sentMessageBackgroundColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.applozic_theme_color_primary));
        }
        return sentMessageBackgroundColor;
    }

    public boolean isCollectFeedback() {
        if (collectFeedback == null) {
            collectFeedback = appSettingPreferences.isCollectFeedback();
        }
        return collectFeedback;
    }

    public Map<String, Boolean> getHidePostCTA() {
        hidePostCTA = alCustomizationSettings.isHidePostCTA();
        if (hidePostCTA == null) {
            hidePostCTA = new HashMap<>();
        }
        return hidePostCTA;
    }

    public boolean isHidePostCTA() {
        if (getHidePostCTA().isEmpty()) {
            return false;
        }
        HashSet<Boolean> values = new HashSet<>(getHidePostCTA().values());
        return !(values.size() == 1 && values.contains(false));
    }

    @SuppressWarnings("ConstantConditions")
    public boolean hideSubmitButtonsPostCTA() {
        return getHidePostCTA().get(SUBMIT_BUTTON) != null ? getHidePostCTA().get(SUBMIT_BUTTON) : false;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean hideLinkButtonsPostCTA() {
        return getHidePostCTA().get(LINK_BUTTON) != null ? getHidePostCTA().get(LINK_BUTTON) : false;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean hideQuickRepliesPostCTA() {
        return getHidePostCTA().get(QUICK_REPLIES) != null ? getHidePostCTA().get(QUICK_REPLIES) : false;
    }

    public int getSentMessageBorderColor() {
        if (sentMessageBorderColor == -1) {
            String colorStr = alCustomizationSettings.getSentMessageBorderColor();

            if (TextUtils.isEmpty(colorStr)) {
                colorStr = appSettingPreferences.getPrimaryColor();
            }
            sentMessageBorderColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.applozic_theme_color_primary));
        }
        return sentMessageBorderColor;
    }

    public int getSendButtonBackgroundColor() {
        if (sendButtonBackgroundColor == -1) {
            String colorStr = alCustomizationSettings.getSendButtonBackgroundColor();

            if (TextUtils.isEmpty(colorStr)) {
                colorStr = appSettingPreferences.getPrimaryColor();
            }
            sendButtonBackgroundColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.applozic_theme_color_primary));
        }
        return sendButtonBackgroundColor;
    }

    public int getMessageStatusIconColor() {
        if (messageStatusIconColor == -1) {
            String colorStr = alCustomizationSettings.getMessageStatusIconColor();

            if (TextUtils.isEmpty(colorStr)) {
                colorStr = appSettingPreferences.getPrimaryColor();
            }

            messageStatusIconColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.message_status_icon_colors));
        }
        return messageStatusIconColor;
    }

    public int getSecondaryColor() {
        if (secondaryColor == -1) {
            secondaryColor = parseColorWithDefault(appSettingPreferences.getSecondaryColor(), context.getResources().getColor(R.color.applozic_theme_color_primary_dark));
        }
        return secondaryColor;
    }

    public int getToolbarColor() {
        if (toolbarColor == -1) {
            toolbarColor = parseColorWithDefault(alCustomizationSettings.getToolbarColor(), getPrimaryColor());
        }
        return toolbarColor;
    }

    public int getStatusBarColor() {
        if (statusBarColor == -1) {
            statusBarColor = parseColorWithDefault(alCustomizationSettings.getStatusBarColor(), getSecondaryColor());
        }
        return statusBarColor;
    }

    public int getRichMessageThemeColor() {
        if (richMessageThemeColor == -1) {
            richMessageThemeColor = parseColorWithDefault(alCustomizationSettings.getRichMessageThemeColor(), getPrimaryColor());
        }
        return richMessageThemeColor;
    }

    public static void clearInstance() {
        kmThemeHelper = null;
    }

    @Override
    public void onSuccess(Object message) {
        if (message instanceof String && KmAppSettingPreferences.CLEAR_THEME_INSTANCE.equals((String) message)) {
            clearInstance();
        }
    }

    @Override
    public void onFailure(Object error) {
    }
}
