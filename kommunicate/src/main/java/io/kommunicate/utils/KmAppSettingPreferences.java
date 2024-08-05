package io.kommunicate.utils;

import static io.kommunicate.KmConversationHelper.SINGLE_THREADED;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.HashMap;

import io.kommunicate.async.KmAppSettingTask;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAppSettingModel;
import io.kommunicate.services.KmService;

public class KmAppSettingPreferences {

    public static final String CLEAR_THEME_INSTANCE = "CLEAR_THEME_INSTANCE";
    private static KmAppSettingPreferences kmAppSettingPreferences;
    private static SharedPreferences preferences;
    private static SharedPreferences alpreferences;
    private KmCallback callback;
    private static final String KM_THEME_PREFERENCES = "KM_THEME_PREFERENCES";
    private static final String KM_THEME_PRIMARY_COLOR = "KM_THEME_PRIMARY_COLOR";
    private static final String KM_THEME_SECONDARY_COLOR = "KM_THEME_SECONDARY_COLOR";
    private static final String KM_COLLECT_FEEDBACK = "KM_COLLECT_FEEDBACK";
    private static final String KM_BOT_MESSAGE_DELAY_INTERVAL = "KM_BOT_MESSAGE_DELAY_INTERVAL";
    private static final String LOGGED_IN_AT_TIME = "LOGGED_IN_AT_TIME";
    private static final String CHAT_SESSION_DELETE_TIME = "CHAT_SESSION_DELETE_TIME";
    private static final String HIDE_POST_CTA = "HIDE_POST_CTA";
    private static final String UPLOAD_OVERRIDE_URL = "UPLOAD_OVERRIDE_URL";
    private static final String UPLOAD_OVERRIDE_HEADER = "UPLOAD_OVERRIDE_HEADER";
    public static final String SINGLE_THREADED = "IS_SINGLE_THREADED";

    public static final String RATING_BASE = "RATING_BASE";

    private KmAppSettingPreferences() {
        preferences = ApplozicService.getAppContext().getSharedPreferences(KM_THEME_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static KmAppSettingPreferences getInstance() {
        if (kmAppSettingPreferences == null) {
            kmAppSettingPreferences = new KmAppSettingPreferences();
        }
        return kmAppSettingPreferences;
    }

    public void setCallback(KmCallback callback) {
        this.callback = callback;
    }

    public void setAppSetting(KmAppSettingModel appSetting) {
        clearInstance();
        if (appSetting != null) {
            if (appSetting.getChatWidget() != null) {
                setPrimaryColor(appSetting.getChatWidget().getPrimaryColor());
                setSecondaryColor(appSetting.getChatWidget().getSecondaryColor());
                setKmBotMessageDelayInterval(appSetting.getChatWidget().getBotMessageDelayInterval());
                setChatSessionDeleteTime(appSetting.getChatWidget().getSessionTimeout());
                if (appSetting.getChatWidget().getDefaultUploadOverride() != null) {
                    setUploadOverrideUrl(appSetting.getChatWidget().getDefaultUploadOverride().getUrl());
                    setUploadOverrideHeader(appSetting.getChatWidget().getDefaultUploadOverride().getHeaders());
                }
                checkIsSingleThreaded(appSetting.getChatWidget().isSingleThreaded());
                setRatingBase(appSetting.getChatWidget().getCsatRatingBase());
            }
            if (appSetting.getResponse() != null) {
                setCollectFeedback(appSetting.getResponse().isCollectFeedback());
                setHidePostCTA(appSetting.getResponse().isHidePostCTA());
            }
        }
    }

    public void checkIsSingleThreaded(boolean isSingleConversation) {
        alpreferences = ApplozicService.getAppContext().getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
        if (alpreferences != null) {
            boolean isSingleThreaded = alpreferences.getBoolean(SINGLE_THREADED, false);
            if (isSingleConversation != isSingleThreaded) {
                alpreferences.edit().putBoolean(SINGLE_THREADED, isSingleConversation).apply();
            }
        }
    }

    public KmAppSettingPreferences setPrimaryColor(String primaryColor) {
        preferences.edit().putString(KM_THEME_PRIMARY_COLOR, primaryColor).commit();
        return this;
    }

    public KmAppSettingPreferences setSecondaryColor(String secondaryColor) {
        preferences.edit().putString(KM_THEME_SECONDARY_COLOR, secondaryColor).commit();
        return this;
    }

    public String getPrimaryColor() {
        return preferences.getString(KM_THEME_PRIMARY_COLOR, null);
    }

    public String getSecondaryColor() {
        return preferences.getString(KM_THEME_SECONDARY_COLOR, null);
    }

    public boolean isCollectFeedback() {
        return preferences.getBoolean(KM_COLLECT_FEEDBACK, false);
    }

    public KmAppSettingPreferences setCollectFeedback(boolean collectFeedback) {
        preferences.edit().putBoolean(KM_COLLECT_FEEDBACK, collectFeedback).commit();
        return this;
    }

    public boolean isHidePostCTA() {
        return preferences.getBoolean(HIDE_POST_CTA, false);
    }

    public KmAppSettingPreferences setHidePostCTA(boolean hidePostCTA) {
        preferences.edit().putBoolean(HIDE_POST_CTA, hidePostCTA).commit();
        return this;
    }

    public KmAppSettingPreferences setLoggedInAtTime(long loggedInAtTime) {
        preferences.edit().putLong(LOGGED_IN_AT_TIME, loggedInAtTime).commit();
        return this;
    }

    public long getLoggedInAtTime() {
        return preferences.getLong(LOGGED_IN_AT_TIME, 0);
    }

    public KmAppSettingPreferences setChatSessionDeleteTime(long chatSessionDeleteTime) {
        preferences.edit().putLong(CHAT_SESSION_DELETE_TIME, chatSessionDeleteTime).commit();
        return this;
    }

    public long getChatSessionDeleteTime() {
        return preferences.getLong(CHAT_SESSION_DELETE_TIME, 0);
    }

    public KmAppSettingPreferences setUploadOverrideUrl(String uploadOverrideUrl) {
        preferences.edit().putString(UPLOAD_OVERRIDE_URL, uploadOverrideUrl).commit();
        return this;
    }

    public String getUploadOverrideUrl() {
        return preferences.getString(UPLOAD_OVERRIDE_URL, "");
    }


    public KmAppSettingPreferences setRatingBase(int ratingBase) {
        preferences.edit().putInt(RATING_BASE, ratingBase).apply();
        return this;
    }

    public int getRatingBase() {
        return preferences.getInt(RATING_BASE, 3);
    }

    public KmAppSettingPreferences setUploadOverrideHeader(HashMap<String, String> headers) {
        preferences.edit().putString(UPLOAD_OVERRIDE_HEADER, GsonUtils.getJsonFromObject(headers, HashMap.class)).commit();
        return this;
    }

    public HashMap<String, String> getUploadOverrideHeader() {
        return (HashMap<String, String>) GsonUtils.getObjectFromJson(preferences.getString(UPLOAD_OVERRIDE_HEADER, null), HashMap.class);
    }

    public boolean isSessionExpired() {
        if (getChatSessionDeleteTime() > 0 && getLoggedInAtTime() == 0) {
            setLoggedInAtTime(System.currentTimeMillis());
        }
        return getLoggedInAtTime() > 0 && getChatSessionDeleteTime() > 0 && System.currentTimeMillis() - getLoggedInAtTime() > getChatSessionDeleteTime();
    }

    public static void fetchAppSettingAsync(final Context context) {
        new KmAppSettingTask(context, Applozic.getInstance(context).getApplicationKey(), new KmCallback() {
            @Override
            public void onSuccess(Object message) {

            }

            @Override
            public void onFailure(Object error) {

            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static KmAppSettingModel fetchAppSetting(final Context context, String appId) {
        String response = new KmService(context).getAppSetting(appId);

        if (response != null) {
            KmAppSettingModel appSettingModel = (KmAppSettingModel) GsonUtils.getObjectFromJson(response, KmAppSettingModel.class);

            if (appSettingModel != null && appSettingModel.isSuccess()) {
                getInstance().clearInstance();
                KmAppSettingPreferences.getInstance().setAppSetting(appSettingModel);
                return appSettingModel;
            }
        }
        return null;
    }

    public static void updateAppSetting(KmAppSettingModel appSettingModel) {
        if (appSettingModel != null && appSettingModel.isSuccess()) {
            getInstance().clearInstance();
            KmAppSettingPreferences.getInstance().setAppSetting(appSettingModel);
        }
    }

    public void setKmBotMessageDelayInterval(int delayInterval) {
        preferences.edit().putInt(KM_BOT_MESSAGE_DELAY_INTERVAL, delayInterval).commit();
    }

    public int getKmBotMessageDelayInterval() {
        return preferences.getInt(KM_BOT_MESSAGE_DELAY_INTERVAL, 0);
    }

    public void clearInstance() {
        if (callback != null) {
            callback.onSuccess(CLEAR_THEME_INSTANCE);
        }
    }
}
