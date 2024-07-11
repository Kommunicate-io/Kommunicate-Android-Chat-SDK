package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicommons.people.channel.Channel;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KmGetConversationInfoCallback;

public class KmConversationInfoTask extends AsyncTask<Object, Object, Channel> {
    private WeakReference<Context> context;
    private ChannelService channelService;
    private KmGetConversationInfoCallback conversationInfoCallback;
    private Exception exception;
    private String clientConversationId;
    private Integer conversationId;

    public KmConversationInfoTask(Context context, Integer conversationId, String clientConversationId, KmGetConversationInfoCallback conversationInfoCallback) {
        this.context = new WeakReference<>(context);
        this.channelService = ChannelService.getInstance(context);
        this.clientConversationId = clientConversationId;
        this.conversationId = conversationId;
        this.conversationInfoCallback = conversationInfoCallback;
    }

    public KmConversationInfoTask(Context context, String clientConversationId, KmGetConversationInfoCallback conversationInfoCallback) {
        this(context, null, clientConversationId, conversationInfoCallback);
    }

    public KmConversationInfoTask(Context context, Integer conversationId, KmGetConversationInfoCallback conversationInfoCallback) {
        this(context, conversationId, null, conversationInfoCallback);
    }

    @Override
    protected Channel doInBackground(Object... params) {
        try {
            if (!TextUtils.isEmpty(clientConversationId)) {
                return channelService.getChannelInfo(clientConversationId);
            } else if (conversationId != null) {
                return channelService.getChannelInfo(conversationId);
            }
        } catch (Exception e) {
            exception = e;
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Channel channel) {
        super.onPostExecute(channel);
        if (channel != null && conversationInfoCallback != null) {
            conversationInfoCallback.onSuccess(channel, context.get());
        } else if (channel == null && conversationInfoCallback != null) {
            conversationInfoCallback.onFailure(exception, context.get());
        }
    }
}
