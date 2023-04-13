package io.kommunicate.uiwidgets.kommunicate.settings;

import android.content.Context;
import android.content.SharedPreferences;

import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.KommunicateService;
import io.kommunicate.data.json.GsonUtils;

import java.util.Map;

public class KmSpeechToTextSetting {
    private static KmSpeechToTextSetting kmSpeechToTextSetting;
    private static SharedPreferences sharedPreferences;
    private static final String S2T_LANGUAGES = "S2T_LANGUAGES";
    private static final String ENABLE_MULTIPLE_SPEECH_TO_TEXT = "ENABLE_MULTIPLE_SPEECH_TO_TEXT";
    private static final String SEND_MESSAGE_ON_SPEECH_END = "SEND_MESSAGE_ON_SPEECH_END";
    private static final String SHOW_LANGUAGE_CODE = "SHOW_LANGUAGE_CODE";


    private KmSpeechToTextSetting(Context context) {
        sharedPreferences = context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static KmSpeechToTextSetting getInstance(Context context) {
        if (kmSpeechToTextSetting == null) {
            kmSpeechToTextSetting = new KmSpeechToTextSetting(KommunicateService.getContext(context));
        } else {
            sharedPreferences = context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
        }
        return kmSpeechToTextSetting;
    }

    public KmSpeechToTextSetting setMultipleLanguage(Map<String, String> languages) {
        if(sharedPreferences != null) {
            String langaugesJson = GsonUtils.getJsonFromObject(languages, Map.class);
            sharedPreferences.edit().putString(S2T_LANGUAGES, langaugesJson).apply();
        }
        return this;
    }
    public Map<String, String> getMultipleLanguage() {
        if(sharedPreferences != null) {
            Map<String, String> languages = (Map<String, String>) GsonUtils.getObjectFromJson(sharedPreferences.getString(S2T_LANGUAGES, ""), Map.class);
            return languages;
        }
        return null;
    }

    public KmSpeechToTextSetting enableMultipleSpeechToText(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(ENABLE_MULTIPLE_SPEECH_TO_TEXT, enable).commit();
        }
        return this;
    }

    public boolean isMultipleSpeechToTextEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(ENABLE_MULTIPLE_SPEECH_TO_TEXT, false);
        }
        return false;
    }

    public KmSpeechToTextSetting sendMessageOnSpeechEnd(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(SEND_MESSAGE_ON_SPEECH_END, enable).apply();
        }
        return this;
    }
    public boolean isSendMessageOnSpeechEnd() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(SEND_MESSAGE_ON_SPEECH_END, false);
        }
        return false;
    }

    public KmSpeechToTextSetting showLanguageCode(boolean enable) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(SHOW_LANGUAGE_CODE, enable).apply();
        }
        return this;
    }
    public boolean isShowLanguageCode() {
        if(sharedPreferences != null) {
            return sharedPreferences.getBoolean(SHOW_LANGUAGE_CODE, false);
        }
        return false;
    }
}
