package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicommons.json.GsonUtils;

import java.lang.ref.WeakReference;

import io.kommunicate.KmConversationResponse;
import io.kommunicate.callbacks.KmCreateConversationHandler;
import io.kommunicate.services.KmUserService;

/**
 * Created by ashish on 08/03/18.
 */

public class KmCreateConversationTask extends AsyncTask<Void, Void, KmConversationResponse> {

    private WeakReference<Context> context;
    private Integer groupId;
    private String userId;
    private String applicationId;
    private String agentId;
    private KmCreateConversationHandler handler;
    Exception e;

    public KmCreateConversationTask(Context context, Integer groupId, String userId, String applicationId, String agentId, KmCreateConversationHandler handler) {
        this.context = new WeakReference<Context>(context);
        this.groupId = groupId;
        this.agentId = agentId;
        this.userId = userId;
        this.handler = handler;
        this.applicationId = applicationId;
    }

    @Override
    protected KmConversationResponse doInBackground(Void... voids) {
        try {
            return (KmConversationResponse) GsonUtils.getObjectFromJson(new KmUserService(context.get()).createConversation(groupId, userId, agentId, applicationId), KmConversationResponse.class);
        } catch (Exception e) {
            this.e = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(KmConversationResponse response) {
        super.onPostExecute(response);

        if (handler != null) {
            if (response != null) {
                if ("SUCCESS".equals(response.getCode())) {
                    handler.onSuccess(context.get(), response);
                } else {
                    handler.onFailure(context.get(), e, response.getCode());
                }
            } else {
                handler.onFailure(context.get(), e, "Some error occurred");
            }
        }
    }
}
