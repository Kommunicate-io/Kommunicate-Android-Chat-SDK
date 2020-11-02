package com.applozic.mobicomkit.uiwidgets.kommunicate;

import android.content.Context;
import android.content.SharedPreferences;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicommons.ApplozicService;

public class KmPrefSettings {

    private static KmPrefSettings kmPrefSettings;
    private static SharedPreferences sharedPreferences;
    private static final String ENABLE_FAQ_OPTION = "ENABLE_FAQ_OPTION";
    private static final String ENABLE_SPEECH_TO_TEXT = "ENABLE_SPEECH_TO_TEXT";
    private static final String ENABLE_TEXT_TO_SPEECH = "ENABLE_TEXT_TO_SPEECH";
    private static final String SEND_MESSAGE_ON_SPEECH_END = "SEND_MESSAGE_ON_SPEECH_END";
    private static final String SPEECH_TO_TEXT_LANGUAGE = "SPEECH_TO_TEXT_LANGUAGE";
    private static final String TEXT_TO_SPEECH_LANGUAGE = "TEXT_TO_SPEECH_LANGUAGE";

    private KmPrefSettings(Context context) {
        sharedPreferences = context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static KmPrefSettings getInstance(Context context) {
        if (kmPrefSettings == null) {
            kmPrefSettings = new KmPrefSettings(ApplozicService.getContext(context));
        } else {
            sharedPreferences = context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
        }
        return kmPrefSettings;
    }

    public KmPrefSettings setFaqOptionEnabled(boolean enable) {
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

    public boolean isSpeechToTextEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(ENABLE_SPEECH_TO_TEXT, false);
        }
        return false;
    }

    public boolean isTextToSpeechEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(ENABLE_TEXT_TO_SPEECH, false);
        }
        return false;
    }

    public boolean isSendMessageOnSpeechEnd() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(SEND_MESSAGE_ON_SPEECH_END, false);
        }
        return false;
    }

    public String getSpeechToTextLanguage() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(SPEECH_TO_TEXT_LANGUAGE, null);
        }
        return null;
    }

    public String getTextToSpeechLanguage() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(TEXT_TO_SPEECH_LANGUAGE, null);
        }
        return null;
    }

    public KmPrefSettings enableSpeechToText(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(ENABLE_SPEECH_TO_TEXT, enable).commit();
        }
        return this;
    }

    public KmPrefSettings enableTextToSpeech(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(ENABLE_TEXT_TO_SPEECH, enable).commit();
        }
        return this;
    }

    public KmPrefSettings setSendMessageOnSpeechEnd(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(SEND_MESSAGE_ON_SPEECH_END, enable).commit();
        }
        return this;
    }

    public KmPrefSettings setSpeechToTextLanguage(String language) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(SPEECH_TO_TEXT_LANGUAGE, language).commit();
        }
        return this;
    }

    public KmPrefSettings setTextToSpeechLanguage(String language) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(TEXT_TO_SPEECH_LANGUAGE, language).commit();
        }
        return this;
    }


}

