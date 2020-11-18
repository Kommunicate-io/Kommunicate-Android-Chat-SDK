package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.channel.service.ChannelService;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KmCallback;

public class KmDeleteConversationTask extends AsyncTask<Void, Void, String> {

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private WeakReference<Context> contextWeakReference;
    private Integer conversationId;
    private boolean updateClientConversationId;
    private KmCallback kmCallback;

    public KmDeleteConversationTask(Context context, Integer conversationId, boolean updateClientConversationId, KmCallback kmCallback) {
        this.contextWeakReference = new WeakReference<>(context);
        this.conversationId = conversationId;
        this.updateClientConversationId = updateClientConversationId;
        this.kmCallback = kmCallback;
    }

    public KmDeleteConversationTask(Context context, Integer conversationId, KmCallback kmCallback) {
        this(context, conversationId, true, kmCallback);
    }

    @Override
    protected String doInBackground(Void... voids) {
        return ChannelService.getInstance(contextWeakReference.get()).deleteChannel(conversationId, updateClientConversationId, true);
    }

    @Override
    protected void onPostExecute(String status) {
        if (kmCallback != null) {
            if (SUCCESS.equals(status)) {
                kmCallback.onSuccess(status);
            } else {
                kmCallback.onFailure(ERROR);
            }
        }
        super.onPostExecute(status);
    }
}
