package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.applozic.mobicommons.commons.core.utils.Utils;

import java.util.Locale;

public class KmTextToSpeech implements TextToSpeech.OnInitListener {

    private Context context;
    private TextToSpeech textToSpeech;
    private static final String TAG = "KmSpeechToText";

    public KmTextToSpeech(Context context) {
        this.context = context;
    }

    public void initialize() {
        this.textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int ttsLang = textToSpeech.setLanguage(Locale.US);

            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "The Language is not supported", Toast.LENGTH_SHORT).show();
                Utils.printLog(context, TAG, "The Language is not supported");
            } else {
                Utils.printLog(context, TAG, "Language Supported");
            }
            Utils.printLog(context, TAG, "Text to Speech initialization successfull");
        } else {
            Toast.makeText(context, "Text to Speech initialization failed!", Toast.LENGTH_SHORT).show();
            Utils.printLog(context, TAG, "Text to Speech initialization failed!");
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
}
