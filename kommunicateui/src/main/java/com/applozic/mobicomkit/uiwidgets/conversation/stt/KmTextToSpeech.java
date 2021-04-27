package com.applozic.mobicomkit.uiwidgets.conversation.stt;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.util.Locale;

public class KmTextToSpeech implements TextToSpeech.OnInitListener {

    private Context context;
    private TextToSpeech textToSpeech;
    private String languageCode;
    private static final String TAG = "KmTextToSpeech";

    public KmTextToSpeech(Context context, String languageCode) {
        this.context = context;
        this.languageCode = languageCode;
    }

    public void initialize() {
        this.textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int ttsLang = textToSpeech.setLanguage(getLanguage());

            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                KmToast.error(context, Utils.getString(context, R.string.km_language_not_supported), Toast.LENGTH_SHORT).show();
                Utils.printLog(context, TAG, "The Language is not supported");
            } else {
                Utils.printLog(context, TAG, "Language Supported");
            }
            Utils.printLog(context, TAG, "Text to Speech initialization successfull");
        } else {
            KmToast.error(context, Utils.getString(context, R.string.km_text_to_speech_init_failed), Toast.LENGTH_SHORT).show();
            Utils.printLog(context, TAG, Utils.getString(context, R.string.km_text_to_speech_init_failed));
        }
    }

    public void speak(String text) {
        int speechStatus = textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Utils.printLog(context, TAG, "Failed to convert the Text to Speech");
        }
    }

    public void destroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public Locale getLanguage() {
        if (Build.VERSION.SDK_INT >= 21 && !TextUtils.isEmpty(languageCode)) {
            return Locale.forLanguageTag(languageCode);
        }
        return Locale.getDefault();
    }
}