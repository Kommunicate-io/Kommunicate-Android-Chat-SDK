package io.kommunicate.commons;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 * Created by ashish on 24/04/18.
 * Do not touch settings from this file, unless asked to do so by Applozic
 * This may result in break down of an update to already existing application.
 */

public class AppSpecificSettings {
    private static final String APPLOZIC_SUPPORT = "support@applozic.com";
    private static final String MY_PREFERENCE = "applozic_internal_preference_key";
    private SharedPreferences sharedPreferences;
    private static final String KOMMUNICATE_LOGS = "kommunicate_logs";
    private static AppSpecificSettings applozicSettings;
    private static final String DATABASE_NAME = "DATABASE_NAME";
    private static final String ENABLE_TEXT_LOGGING = "ENABLE_TEXT_LOGGING";
    private static final String TEXT_LOG_FILE_NAME = "TEXT_LOG_FILE_NAME";
    private static final String AL_BASE_URL = "AL_BASE_URL";
    private static final String KM_BASE_URL = "KM_BASE_URL";
    private static final String AL_SUPPORT_EMAIL_ID = "AL_SUPPORT_EMAIL_ID";
    private static final String ENABLE_LOGGING_IN_RELEASE_BUILD = "ENABLE_LOGGING_IN_RELEASE_BUILD";
    private static final String AL_NOTIFICATION_AFTER_TIME = "AL_NOTIFICATION_AFTER_TIME";

    private AppSpecificSettings(Context context) {
        this.sharedPreferences = ApplozicService.getContext(context).getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static AppSpecificSettings getInstance(Context context) {
        if (applozicSettings == null) {
            applozicSettings = new AppSpecificSettings(ApplozicService.getContext(context));
        }
        return applozicSettings;
    }

    public AppSpecificSettings setDatabaseName(String dbName) {
        sharedPreferences.edit().putString(DATABASE_NAME, dbName).apply();
        return this;
    }

    public String getDatabaseName() {
        return sharedPreferences.getString(DATABASE_NAME, null);
    }

    public AppSpecificSettings enableTextLogging(boolean enable) {
        sharedPreferences.edit().putBoolean(ENABLE_TEXT_LOGGING, enable).apply();
        return this;
    }

    public boolean isTextLoggingEnabled() {
        return sharedPreferences.getBoolean(ENABLE_TEXT_LOGGING, false);
    }

    public AppSpecificSettings setTextLogFileName(String textLogFileName) {
        sharedPreferences.edit().putString(TEXT_LOG_FILE_NAME, textLogFileName).apply();
        return this;
    }

    public String getTextLogFileName() {
        return sharedPreferences.getString(TEXT_LOG_FILE_NAME, KOMMUNICATE_LOGS);
    }

    public String getAlBaseUrl() {
        return sharedPreferences.getString(AL_BASE_URL, null);
    }

    public AppSpecificSettings setAlBaseUrl(String url) {
        sharedPreferences.edit().putString(AL_BASE_URL, url).commit();
        return this;
    }

    public String getKmBaseUrl() {
        return sharedPreferences.getString(KM_BASE_URL, null);
    }

    public AppSpecificSettings setKmBaseUrl(String url) {
        sharedPreferences.edit().putString(KM_BASE_URL, url).commit();
        return this;
    }

    public String getSupportEmailId() {
        return sharedPreferences.getString(AL_SUPPORT_EMAIL_ID, APPLOZIC_SUPPORT);
    }

    public AppSpecificSettings setSupportEmailId(String emailId) {
        sharedPreferences.edit().putString(AL_SUPPORT_EMAIL_ID, emailId).commit();
        return this;
    }

    public AppSpecificSettings enableLoggingForReleaseBuild(boolean enable) {
        sharedPreferences.edit().putBoolean(ENABLE_LOGGING_IN_RELEASE_BUILD, enable).commit();
        return this;
    }

    public boolean isLoggingEnabledForReleaseBuild() {
        return sharedPreferences.getBoolean(ENABLE_LOGGING_IN_RELEASE_BUILD, false);
    }

    public AppSpecificSettings setNotificationAfterTime(long notificationAfterTime) {
        sharedPreferences.edit().putLong(AL_NOTIFICATION_AFTER_TIME, notificationAfterTime).commit();
        return this;
    }

    public boolean isAllNotificationMuted() {
        long notificationAfterTime = sharedPreferences.getLong(AL_NOTIFICATION_AFTER_TIME, 0);
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        return (notificationAfterTime - date.getTime() > 0);
    }

    public boolean clearAll() {
        if (sharedPreferences != null) {
            return sharedPreferences.edit().clear().commit();
        }

        return false;
    }
}
