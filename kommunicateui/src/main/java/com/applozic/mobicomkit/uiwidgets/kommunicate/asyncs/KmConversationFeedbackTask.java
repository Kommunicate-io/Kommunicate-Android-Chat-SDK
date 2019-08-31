package com.applozic.mobicomkit.uiwidgets.kommunicate.asyncs;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmFeedbackCallback;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmApiResponse;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmFeedback;
import com.applozic.mobicomkit.uiwidgets.kommunicate.services.KmService;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

/**
 * the feedback api async task
 *
 * @author shubham
 * @date 25/July/2019
 */
public class KmConversationFeedbackTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> contextWeakReference;
    private KmFeedback kmFeedback; //will pe passed null if getting the feedback
    private String conversationId;
    private KmFeedbackCallback kmFeedbackCallback;
    Exception e;

    public KmConversationFeedbackTask(Context context, String conversationId, KmFeedback kmFeedback, KmFeedbackCallback kmFeedbackCallback) {
        contextWeakReference = new WeakReference<>(context);
        this.kmFeedback = kmFeedback;
        this.conversationId = conversationId;
        this.kmFeedbackCallback = kmFeedbackCallback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            if (kmFeedback == null) {
                if (conversationId == null || TextUtils.isEmpty(conversationId)) {
                    throw new Exception("KmFeedback and conversation id parameters null");
                }
                return new KmService(contextWeakReference.get()).getConversationFeedback(conversationId);
            } else {
                return new KmService(contextWeakReference.get()).postConversationFeedback(kmFeedback);
            }
        } catch (Exception i) {
            e = i;
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (e != null) {
            kmFeedbackCallback.onFailure(contextWeakReference.get(), e, response);
        } else {
            if (response == null) {
                kmFeedbackCallback.onFailure(contextWeakReference.get(), new Exception("Feedback Response string null."), null);
            } else {
                try {
                    KmApiResponse<KmFeedback> kmApiResponse;
                    Type type = new TypeToken<KmApiResponse<KmFeedback>>() {
                    }.getType();
                    kmApiResponse = (KmApiResponse<KmFeedback>) GsonUtils.getObjectFromJson(response, type);
                    kmFeedbackCallback.onSuccess(contextWeakReference.get(), kmApiResponse);
                } catch (Exception e) {
                    kmFeedbackCallback.onFailure(contextWeakReference.get(), e, response);
                }
            }
        }
    }

    @Override
    protected void onCancelled() {
        kmFeedbackCallback.onFailure(contextWeakReference.get(), e, "Task cancelled.");
    }
}
