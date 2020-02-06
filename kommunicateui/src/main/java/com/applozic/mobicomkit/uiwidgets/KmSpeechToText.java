package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmRecordButton;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmRecordView;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.ArrayList;
import java.util.Locale;

public class KmSpeechToText implements RecognitionListener {
    private static final String TAG = "KmSpeechToText";
    private KmRecordView recordView;
    private KmRecordButton recordButton;
    private Context context;
    private KmTextListener listener;

    public KmSpeechToText(Context context, KmRecordView recordView, KmRecordButton recordButton, KmTextListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());

        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);
        speechRecognizer.startListening(intent);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Utils.printLog(context, TAG, "Ready for speech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Utils.printLog(context, TAG, "Beginning of speech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Utils.printLog(context, TAG, "RMS changed : " + rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //Utils.printLog(context, TAG, "Buffer received");
    }

    @Override
    public void onEndOfSpeech() {
        Utils.printLog(context, TAG, "End of speech");
    }

    @Override
    public void onError(int error) {
        Toast.makeText(context, "Some error occurred : " + error, Toast.LENGTH_SHORT).show();
        Utils.printLog(context, TAG, "Error : " + error);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(listener != null) {
            listener.onSpeechToTextResult(matches != null ? matches.get(0) : "");
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        //Utils.printLog(context, TAG, "Received partial result : " + GsonUtils.getJsonFromObject(partialResults, Bundle.class));
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Utils.printLog(context, TAG, "Received event : " + eventType);
    }

    public interface KmTextListener {
        void onSpeechToTextResult(String text);
    }
}
