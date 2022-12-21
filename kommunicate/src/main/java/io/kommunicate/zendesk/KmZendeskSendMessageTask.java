package io.kommunicate.zendesk;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.callbacks.KmCallback;
import zendesk.chat.Attachment;

public class KmZendeskSendMessageTask extends AsyncTask<Void, Void, KmZendeskApiModel> {
    private Context context;
    private String message;
    private String displayName;
    private String agentId;
    private Integer conversationId;
    private Long messageTimestamp;
    private Attachment attachment;
    private KmCallback kmCallback;


    private KmZendeskSendMessageTask(Context context, String message, Attachment attachment, String displayName, String agentId, Integer conversationId, Long messageTimestamp, KmCallback kmCallback) {
        this.context = context;
        this.message = message;
        this.displayName = displayName;
        this.agentId = agentId.replace(":", "-"); //Kommunicate server accepts "-" but Zendesk sends ":"
        this.conversationId = conversationId;
        this.messageTimestamp = messageTimestamp;
        this.attachment = attachment;
        this.kmCallback = kmCallback;
    }
    public KmZendeskSendMessageTask(Context context, String message, String displayName, String agentId, Integer conversationId, Long messageTimestamp, KmCallback kmCallback) {
        this(context, message, null, displayName, agentId, conversationId, messageTimestamp, kmCallback);
    }
    public KmZendeskSendMessageTask(Context context, Attachment attachment, String displayName, String agentId, Integer conversationId, Long messageTimestamp, KmCallback kmCallback) {
        this(context, null, attachment, displayName, agentId, conversationId, messageTimestamp, kmCallback);

    }

    @Override
    protected KmZendeskApiModel doInBackground(Void... voids) {
        if(message != null) {
            return new KmZendeskClientService(context).sendZendeskMessage(message, displayName, agentId, conversationId, messageTimestamp);

        } else if(attachment != null){
            return new KmZendeskClientService(context).sendZendeskAttachment(attachment, displayName, agentId, conversationId, messageTimestamp);
        }
        return null;
    }

    @Override
    protected void onPostExecute(KmZendeskApiModel zendeskApiModel) {
        if(kmCallback != null && zendeskApiModel != null) {
            if(zendeskApiModel.isSuccess()) {
                kmCallback.onSuccess(zendeskApiModel);
            } else {
                kmCallback.onFailure(zendeskApiModel);
            }
        }
        super.onPostExecute(zendeskApiModel);
    }
}
