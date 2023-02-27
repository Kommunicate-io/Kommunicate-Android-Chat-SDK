package io.kommunicate.uiwidgets;

import android.content.Context;
import android.text.TextUtils;

import io.kommunicate.uiwidgets.kommunicate.KmPrefSettings;
import io.kommunicate.data.json.JsonMarker;

public class KmSpeechSetting extends JsonMarker {
    private boolean enabled;
    private String languageCode;
    private boolean sendMessageOnSpeechEnd = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public boolean isSendMessageOnSpeechEnd() {
        return sendMessageOnSpeechEnd;
    }

    public void setSendMessageOnSpeechEnd(boolean sendMessageOnSpeechEnd) {
        this.sendMessageOnSpeechEnd = sendMessageOnSpeechEnd;
    }

    public static String getSpeechToTextLanguageCode(Context context, AlCustomizationSettings alCustomizationSettings) {
        if (!TextUtils.isEmpty(KmPrefSettings.getInstance(context).getSpeechToTextLanguage())) {
            return KmPrefSettings.getInstance(context).getSpeechToTextLanguage();
        }
        return alCustomizationSettings.getSpeechToText().getLanguageCode();
    }

    public static String getTextToSpeechLanguageCode(Context context, AlCustomizationSettings alCustomizationSettings) {
        if (!TextUtils.isEmpty(KmPrefSettings.getInstance(context).getTextToSpeechLanguage())) {
            return KmPrefSettings.getInstance(context).getTextToSpeechLanguage();
        }
        return alCustomizationSettings.getTextToSpeech().getLanguageCode();
    }
}
