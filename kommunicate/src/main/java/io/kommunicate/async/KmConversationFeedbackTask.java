package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import io.kommunicate.commons.json.GsonUtils;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

import annotations.CleanUpRequired;
import io.kommunicate.callbacks.KmFeedbackCallback;
import io.kommunicate.models.KmApiResponse;
import io.kommunicate.models.KmFeedback;
import io.kommunicate.services.KmService;

/**
 * the feedback api async task
 *
 * @author shubham
 * @date 25/July/2019
 */
@Deprecated
@CleanUpRequired(reason = "Migrated KmConversationFeedbackTask to ConversationFeedbackUseCase")
public class KmConversationFeedbackTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> contextWeakReference;
    private KmFeedback kmFeedback; //will pe passed null if getting the feedback
    private KmFeedbackDetails kmFeedbackDetails;
    private KmFeedbackCallback kmFeedbackCallback;
    private static final String TASK_CANCELLED = "Task cancelled.";
    private static final String FEEDBACK_NULL = "Feedback Response string null.";
    private static final String KMFeedback_ID_NULL = "KmFeedback and conversation id parameters null";
    Exception e;

    public KmConversationFeedbackTask(Context context, KmFeedback kmFeedback, KmFeedbackDetails kmFeedbackDetails, KmFeedbackCallback kmFeedbackCallback) {
        contextWeakReference = new WeakReference<>(context);
        this.kmFeedback = kmFeedback;
        this.kmFeedbackDetails = kmFeedbackDetails;
        this.kmFeedbackCallback = kmFeedbackCallback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            String conversationId = kmFeedbackDetails.getConversationId();
            if (kmFeedback == null) {
                if (conversationId == null || TextUtils.isEmpty(conversationId)) {
                    throw new Exception(KMFeedback_ID_NULL);
                }
                return new KmService(contextWeakReference.get()).getConversationFeedback(conversationId);
            } else {
                return new KmService(contextWeakReference.get()).postConversationFeedback(kmFeedback, null);
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
                kmFeedbackCallback.onFailure(contextWeakReference.get(), new Exception(FEEDBACK_NULL), null);
            } else {
                try {
                    KmApiResponse<KmFeedback> kmApiResponse;
                    Type type = new TypeToken<KmApiResponse<KmFeedback>>() {
                    }.getType();
                    kmApiResponse = (KmApiResponse<KmFeedback>) GsonUtils.getObjectFromJson(response, type);
                    kmFeedbackCallback.onSuccess(contextWeakReference.get(), kmApiResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    kmFeedbackCallback.onFailure(contextWeakReference.get(), e, response);
                }
            }
        }
    }

    @Override
    protected void onCancelled() {
        kmFeedbackCallback.onFailure(contextWeakReference.get(), e, TASK_CANCELLED);
    }

    @Deprecated
    @CleanUpRequired(reason = "Migrated to FeedbackDetailsData")
    public static class KmFeedbackDetails {
        private String conversationId;
        private String userName;
        private String userId;
        private String supportAgentId;

        public KmFeedbackDetails(String conversationId, String userName, String userId, String supportAgentId) {
            this.conversationId = conversationId;
            this.userName = userName;
            this.userId = userId;
            this.supportAgentId = supportAgentId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserId() {
            return userId;
        }

        public String getSupportAgentId() {
            return supportAgentId;
        }
    }
}
