package io.kommunicate.commons.data;

import android.content.Context;
import android.text.TextUtils;

import io.kommunicate.commons.AppContextService;

public class PrefSettings {

    public static final String AL_PREF_SETTING_KEY = "al_secret_key_pref";
    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    public static final String GOOGLE_API_KEY_META_DATA = "com.google.android.geo.API_KEY";
    public static final String DEFAULT_LANGUAGE = "DEFAULT_LANGUAGE";
    private static String USER_ENCRYPTION_KEY = "user_encryption_key";
    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String ENCRYPTION_KEY = "encryption_key";
    private static String PASSWORD = "password";

    private static PrefSettings prefSettings;

    //Keep these handy for performance as these are unlikely to be changed during application lifecycle.
    private static String decodedAppKey;
    private static String decodedGeoApiKey;
    private static String decodedPassword;
    private static String decodedEncryptionKey;
    private static String decodedUserEncryptionKey;
    private static String decodedUserAuthToken;

    private SecureSharedPreferences sharedPreferences;

    private PrefSettings(Context context) {
        this.sharedPreferences = new SecureSharedPreferences(AL_PREF_SETTING_KEY, AppContextService.getContext(context));
    }

    public static PrefSettings getInstance(Context context) {
        if (prefSettings == null) {
            prefSettings = new PrefSettings(AppContextService.getContext(context));
        }
        return prefSettings;
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

    public PrefSettings setApplicationKey(String applicationKey) {
        decodedAppKey = applicationKey;
        sharedPreferences.edit().putString(APPLICATION_KEY, applicationKey).commit();
        return this;
    }

    public PrefSettings setGeoApiKey(String geoApiKey) {
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

    public PrefSettings setUserEncryptionKey(String userEncryptionKey) {
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

    public PrefSettings setUserAuthToken(String userAuthToken) {
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

    public PrefSettings setEncryptionKey(String encryptionKey) {
        decodedEncryptionKey = encryptionKey;
        sharedPreferences.edit().putString(ENCRYPTION_KEY, encryptionKey).commit();
        return this;
    }

    public String getPassword() {
        if (TextUtils.isEmpty(decodedPassword)) {
            decodedPassword = sharedPreferences.getString(PASSWORD, null);
        }
        return decodedPassword;
    }

    public PrefSettings setPassword(String password) {
        decodedPassword = password;
        sharedPreferences.edit().putString(PASSWORD, password).commit();
        return this;
    }
    public void setDeviceDefaultLanguageToBot(String languageCode) {
       sharedPreferences.edit().putString(DEFAULT_LANGUAGE, languageCode).apply();
    }
    public String getDeviceDefaultLanguageToBot() {
        return sharedPreferences.getString(DEFAULT_LANGUAGE, null);
    }
}
