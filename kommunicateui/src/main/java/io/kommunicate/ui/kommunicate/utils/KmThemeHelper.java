package io.kommunicate.ui.kommunicate.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.commons.AppContextService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.utils.KmAppSettingPreferences;

public class KmThemeHelper implements KmCallback {

    private static final String SUBMIT_BUTTON = "submit";
    private static final String LINK_BUTTON = "link";
    private static final String QUICK_REPLIES = "quickReply";
    private static final String FORM_SUBMIT_BUTTON = "hidePostFormSubmit";
    private static KmThemeHelper kmThemeHelper;
    private final Context context;
    private final KmAppSettingPreferences appSettingPreferences;
    private final CustomizationSettings customizationSettings;
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

    public static KmThemeHelper getInstance(Context context, CustomizationSettings customizationSettings) {
        if (kmThemeHelper == null) {
            kmThemeHelper = new KmThemeHelper(context, customizationSettings);
        }
        return kmThemeHelper;
    }

    private KmThemeHelper(Context context, CustomizationSettings alCustomizationSettings) {
        this.context = AppContextService.getContext(context);
        this.customizationSettings = alCustomizationSettings;
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
        primaryColor = parseColorWithDefault(appSettingPreferences.getPrimaryColor(), context.getResources().getColor(R.color.core_theme_color_primary));
        return primaryColor;
    }

    public int getToolbarTitleColor() {
        toolbarTitleColor = parseColorWithDefault(isDarkModeEnabledForSDK() ? customizationSettings.getToolbarTitleColor().get(1) : customizationSettings.getToolbarTitleColor().get(0), context.getResources().getColor(R.color.toolbar_title_color));
        return toolbarTitleColor;
    }

    public int getToolbarSubtitleColor() {
        toolbarSubtitleColor = parseColorWithDefault(isDarkModeEnabledForSDK() ? customizationSettings.getToolbarSubtitleColor().get(1) : customizationSettings.getToolbarSubtitleColor().get(0), context.getResources().getColor(R.color.toolbar_subtitle_color));
        return toolbarSubtitleColor;
    }

    public int getSentMessageBackgroundColor() {
        String colorStr = isDarkModeEnabledForSDK() ? customizationSettings.getSentMessageBackgroundColor().get(1) : customizationSettings.getSentMessageBackgroundColor().get(0);

        if (TextUtils.isEmpty(colorStr)) {
            colorStr = appSettingPreferences.getPrimaryColor();
        }
        sentMessageBackgroundColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.core_theme_color_primary));
        return sentMessageBackgroundColor;
    }

    public boolean isCollectFeedback() {
        if (collectFeedback == null) {
            collectFeedback = appSettingPreferences.isCollectFeedback();
        }
        return collectFeedback;
    }

    public Map<String, Boolean> getHidePostCTA() {
        hidePostCTA = customizationSettings.isHidePostCTA();
        if (hidePostCTA == null) {
            hidePostCTA = new HashMap<>();
        }
        return hidePostCTA;
    }

    public boolean isDisableFormPostSubmit() {
        return customizationSettings.isDisableFormPostSubmit();
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
    public boolean hideFormSubmitButtonsPostCTA() {
        return getHidePostCTA().get(FORM_SUBMIT_BUTTON) != null ? getHidePostCTA().get(FORM_SUBMIT_BUTTON) : false;
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
        String colorStr = isDarkModeEnabledForSDK() ? customizationSettings.getSentMessageBorderColor().get(1) : customizationSettings.getSentMessageBorderColor().get(0);

        if (TextUtils.isEmpty(colorStr)) {
            colorStr = appSettingPreferences.getPrimaryColor();
        }
        sentMessageBorderColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.core_theme_color_primary));
        return sentMessageBorderColor;
    }

    public int getSendButtonBackgroundColor() {
        String colorStr = isDarkModeEnabledForSDK() ? customizationSettings.getSendButtonBackgroundColor().get(1) : customizationSettings.getSendButtonBackgroundColor().get(0);

        if (TextUtils.isEmpty(colorStr)) {
            colorStr = appSettingPreferences.getPrimaryColor();
        }
        sendButtonBackgroundColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.core_theme_color_primary));
        return sendButtonBackgroundColor;
    }

    public int getMessageStatusIconColor() {
        String colorStr = isDarkModeEnabledForSDK() ? customizationSettings.getMessageStatusIconColor().get(1) : customizationSettings.getMessageStatusIconColor().get(0);

        if (TextUtils.isEmpty(colorStr)) {
            colorStr = appSettingPreferences.getPrimaryColor();
        }

        messageStatusIconColor = parseColorWithDefault(colorStr, context.getResources().getColor(R.color.message_status_icon_colors));
        return messageStatusIconColor;
    }

    public int getSecondaryColor() {
        secondaryColor = parseColorWithDefault(appSettingPreferences.getSecondaryColor(), context.getResources().getColor(R.color.core_theme_color_primary_dark));
        return secondaryColor;
    }

    public int getToolbarColor() {
        toolbarColor = parseColorWithDefault(isDarkModeEnabledForSDK() ? customizationSettings.getToolbarColor().get(1) : customizationSettings.getToolbarColor().get(0), getPrimaryColor());
        return toolbarColor;
    }

    public int getStatusBarColor() {
        statusBarColor = parseColorWithDefault(customizationSettings.getStatusBarColor().get(isDarkModeEnabledForSDK() ? 1 : 0),
                parseColorWithDefault(customizationSettings.getToolbarColor().get(isDarkModeEnabledForSDK() ? 1 : 0), getPrimaryColor()));
        return statusBarColor;
    }

    public int getRichMessageThemeColor() {
        richMessageThemeColor = parseColorWithDefault(isDarkModeEnabledForSDK() ? customizationSettings.getRichMessageThemeColor().get(1) : customizationSettings.getRichMessageThemeColor().get(0), getPrimaryColor());
        return richMessageThemeColor;
    }

    public boolean isDarkModeEnabledForSDK() {
        return !customizationSettings.isAgentApp() && customizationSettings.getUseDarkMode() && ((context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
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
