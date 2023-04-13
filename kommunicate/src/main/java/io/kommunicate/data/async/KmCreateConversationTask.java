package io.kommunicate.data.async;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KmCreateConversationHandler;
import io.kommunicate.data.channel.service.ChannelService;
import io.kommunicate.data.json.GsonUtils;
import io.kommunicate.data.services.KmUserService;
import io.kommunicate.models.KMGroupInfo;
import io.kommunicate.models.KmConversationResponse;

/**
 * Created by ashish on 08/03/18.
 */

public class KmCreateConversationTask extends AsyncTask<Void, Void, KmConversationResponse> {

    KMGroupInfo groupInfo;
    Exception e;
    private WeakReference<Context> context;
    private Integer groupId;
    private String userId;
    private String applicationId;
    private String agentId;
    private KmCreateConversationHandler handler;
    private KMStartChatHandler startChatHandler;

    public KmCreateConversationTask(Context context, Integer groupId, String userId, String applicationId, String agentId, KmCreateConversationHandler handler) {
        this.context = new WeakReference<Context>(context);
        this.groupId = groupId;
        this.agentId = agentId;
        this.userId = userId;
        this.handler = handler;
        this.applicationId = applicationId;
    }

    public KmCreateConversationTask(Context context, KMGroupInfo groupInfo, KMStartChatHandler handler) {
        this.context = new WeakReference<Context>(context);
        this.groupInfo = groupInfo;
        this.startChatHandler = handler;
    }

    @Override
    protected KmConversationResponse doInBackground(Void... voids) {
        try {
            if (groupInfo != null) {
                return (KmConversationResponse) GsonUtils.getObjectFromJson(new KmUserService(context.get()).createNewConversation(groupInfo), KmConversationResponse.class);
            }
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

        if (startChatHandler != null) {
            if (response != null) {
                if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                    startChatHandler.onSuccess(ChannelService.getInstance(context.get()).getChannel(response.getResponse()), context.get());
                } else {
                    startChatHandler.onFailure(response, context.get());
                }
            } else {
                startChatHandler.onFailure(null, context.get());
            }
        }
    }
}
