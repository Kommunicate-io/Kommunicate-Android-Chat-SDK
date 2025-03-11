package com.applozic.mobicomkit.uiwidgets.kommunicate.settings;

import android.content.Context;
import android.content.SharedPreferences;

import dev.kommunicate.devkit.api.account.user.MobiComUserPreference;

import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmSpeechToTextModel;
import dev.kommunicate.commons.ApplozicService;
import dev.kommunicate.commons.json.GsonUtils;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public class KmSpeechToTextSetting {
    private static KmSpeechToTextSetting kmSpeechToTextSetting;
    private static SharedPreferences sharedPreferences;
    private static final String S2T_LANGUAGES = "S2T_LANGUAGES";
    private static final String ENABLE_MULTIPLE_SPEECH_TO_TEXT = "ENABLE_MULTIPLE_SPEECH_TO_TEXT";
    private static final String SEND_MESSAGE_ON_SPEECH_END = "SEND_MESSAGE_ON_SPEECH_END";
    private static final String SHOW_LANGUAGE_CODE = "SHOW_LANGUAGE_CODE";
    private static final String S2T_LANGUAGE_LIST = "S2T_LANGUAGE_LIST";


    private KmSpeechToTextSetting(Context context) {
        sharedPreferences = context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static KmSpeechToTextSetting getInstance(Context context) {
        if (kmSpeechToTextSetting == null) {
            kmSpeechToTextSetting = new KmSpeechToTextSetting(ApplozicService.getContext(context));
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

    public KmSpeechToTextSetting setSpeechToTextList(List<KmSpeechToTextModel> speechToTextList) {
        if(sharedPreferences != null) {
            String listJson = GsonUtils.getJsonFromObject(speechToTextList,  new
                    TypeToken<List<KmSpeechToTextModel>>() {
                    }.getType());
            sharedPreferences.edit().putString(S2T_LANGUAGE_LIST, listJson).apply();
        }
        return this;
    }

    public List<KmSpeechToTextModel> getSpeechToTextList() {
        if(sharedPreferences != null) {
            return (List<KmSpeechToTextModel>) GsonUtils.getObjectFromJson(sharedPreferences.getString(S2T_LANGUAGE_LIST, ""), new TypeToken<List<KmSpeechToTextModel>>() {
                    }.getType());
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
