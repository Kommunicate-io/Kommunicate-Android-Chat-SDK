package com.applozic.mobicomkit.uiwidgets.conversation.stt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmRecordButton;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class KmSpeechToText implements RecognitionListener {
    private static final String TAG = "KmSpeechToText";
    private KmRecordButton recordButton;
    private Context context;
    private KmTextListener listener;
    private SpeechRecognizer speechRecognizer;
    private boolean isStopped;

    public KmSpeechToText(Context context, KmRecordButton recordButton, KmTextListener listener) {
        this.context = context;
        this.listener = listener;
        this.recordButton = recordButton;
    }

    public void startListening() {
        isStopped = false;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);
        speechRecognizer.startListening(intent);
    }

    public void stopListening() {
        isStopped = true;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
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
        if (rmsdB >= 1.0f) {
            recordButton.startScaleWithValue(1.0f + rmsdB / 15);
        }
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // Utils.printLog(context, TAG, "Buffer received");
    }

    @Override
    public void onEndOfSpeech() {
        if (listener != null) {
            listener.onSpeechEnd(-1);
        }
        Utils.printLog(context, TAG, "End of speech");
    }

    @Override
    public void onError(int error) {
        if (listener != null) {
            listener.onSpeechEnd(error);
        }
        //Utils.printLog(context, TAG, "Error : " + error);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (listener != null && !isStopped) {
            listener.onSpeechToTextResult(matches != null ? matches.get(0) : "");
        }
        //Utils.printLog(context, TAG, "Received result : " + results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> result = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (listener != null && result != null && !result.isEmpty()) {
            listener.onSpeechToTextPartialResult(result.get(0));
        }
        Utils.printLog(context, TAG, "Received partial result : " + partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // Utils.printLog(context, TAG, "Received event : " + eventType);
    }

    public boolean isStopped() {
        return isStopped;
    }

    public interface KmTextListener {
        void onSpeechToTextResult(String text);

        void onSpeechToTextPartialResult(String text);

        void onSpeechEnd(int errorCode);
    }
}