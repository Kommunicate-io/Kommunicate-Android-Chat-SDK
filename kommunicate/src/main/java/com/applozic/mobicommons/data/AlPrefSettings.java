package com.applozic.mobicommons.data;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicommons.ApplozicService;

public class AlPrefSettings {

    public static final String AL_PREF_SETTING_KEY = "al_secret_key_pref";
    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    public static final String GOOGLE_API_KEY_META_DATA = "com.google.android.geo.API_KEY";
    private static String USER_ENCRYPTION_KEY = "user_encryption_key";
    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String ENCRYPTION_KEY = "encryption_key";
    private static String PASSWORD = "password";

    private static AlPrefSettings alPrefSettings;

    //Keep these handy for performance as these are unlikely to be changed during application lifecycle.
    private static String decodedAppKey;
    private static String decodedGeoApiKey;
    private static String decodedPassword;
    private static String decodedEncryptionKey;
    private static String decodedUserEncryptionKey;
    private static String decodedUserAuthToken;

    private SecureSharedPreferences sharedPreferences;

    private AlPrefSettings(Context context) {
        this.sharedPreferences = new SecureSharedPreferences(AL_PREF_SETTING_KEY, ApplozicService.getContext(context));
    }

    public static AlPrefSettings getInstance(Context context) {
        if (alPrefSettings == null) {
            alPrefSettings = new AlPrefSettings(ApplozicService.getContext(context));
        }
        return alPrefSettings;
    }

    public String getApplicationKey() {
        if (TextUtils.isEmpty(decodedAppKey)) {
            decodedAppKey = sharedPreferences.getString(APPLICATION_KEY, null);
        }
        return decodedAppKey;
    }

    public String getGeoApiKey() {
        if (TextUtils.isEmpty(decodedGeoApiKey)) {
            decodedGeoApiKey = sharedPreferences.getString(GOOGLE_API_KEY_META_DATA, null);
        }
        return decodedGeoApiKey;
    }

    public AlPrefSettings setApplicationKey(String applicationKey) {
        decodedAppKey = applicationKey;
        sharedPreferences.edit().putString(APPLICATION_KEY, applicationKey).commit();
        return this;
    }

    public AlPrefSettings setGeoApiKey(String geoApiKey) {
        decodedGeoApiKey = geoApiKey;
        sharedPreferences.edit().putString(GOOGLE_API_KEY_META_DATA, geoApiKey).commit();
        return this;
    }

    public String getUserEncryptionKey() {
        if (TextUtils.isEmpty(decodedUserEncryptionKey)) {
            decodedUserEncryptionKey = sharedPreferences.getString(USER_ENCRYPTION_KEY, null);
        }
        return decodedUserEncryptionKey;
    }

    public AlPrefSettings setUserEncryptionKey(String userEncryptionKey) {
        decodedUserEncryptionKey = userEncryptionKey;
        sharedPreferences.edit().putString(USER_ENCRYPTION_KEY, userEncryptionKey).commit();
        return this;
    }

    public String getUserAuthToken() {
        if (TextUtils.isEmpty(decodedUserAuthToken)) {
            decodedUserAuthToken = sharedPreferences.getString(USER_AUTH_TOKEN, null);
        }
        return decodedUserAuthToken;
    }

    public AlPrefSettings setUserAuthToken(String userAuthToken) {
        decodedUserAuthToken = userAuthToken;
        sharedPreferences.edit().putString(USER_AUTH_TOKEN, userAuthToken).commit();
        return this;
    }

    public String getEncryptionKey() {
        if (TextUtils.isEmpty(decodedEncryptionKey)) {
            decodedEncryptionKey = sharedPreferences.getString(ENCRYPTION_KEY, null);
        }
        return decodedEncryptionKey;
    }

    public AlPrefSettings setEncryptionKey(String encryptionKey) {
        decodedEncryptionKey = encryptionKey;
        sharedPreferences.edit().putString(ENCRYPTION_KEY, encryptionKey);
        return this;
    }

    public String getPassword() {
        if (TextUtils.isEmpty(decodedPassword)) {
            decodedPassword = sharedPreferences.getString(PASSWORD, null);
        }
        return decodedPassword;
    }

    public AlPrefSettings setPassword(String password) {
        decodedPassword = password;
        sharedPreferences.edit().putString(PASSWORD, password);
        return this;
    }
}
