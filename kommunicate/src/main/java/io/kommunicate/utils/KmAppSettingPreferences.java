package io.kommunicate.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.GsonUtils;

import io.kommunicate.async.KmAppSettingTask;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAppSettingModel;
import io.kommunicate.services.KmService;

public class KmAppSettingPreferences {

    public static final String CLEAR_THEME_INSTANCE = "CLEAR_THEME_INSTANCE";
    private static KmAppSettingPreferences kmAppSettingPreferences;
    private static SharedPreferences preferences;
    private KmCallback callback;
    private static final String KM_THEME_PREFERENCES = "KM_THEME_PREFERENCES";
    private static final String KM_THEME_PRIMARY_COLOR = "KM_THEME_PRIMARY_COLOR";
    private static final String KM_THEME_SECONDARY_COLOR = "KM_THEME_SECONDARY_COLOR";

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
        if (appSetting != null) {
            setPrimaryColor(appSetting.getChatWidget().getPrimaryColor());
            setSecondaryColor(appSetting.getChatWidget().getSecondaryColor());
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

    public void clearInstance() {
        if (callback != null) {
            callback.onSuccess(CLEAR_THEME_INSTANCE);
        }
    }
}
