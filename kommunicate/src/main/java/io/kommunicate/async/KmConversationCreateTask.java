package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KmStartConversationHandler;

public class KmConversationCreateTask extends AsyncTask<Void, Void, ChannelFeedApiResponse> {
    private WeakReference<Context> context;
    private ChannelService channelService;
    private ChannelInfo channelInfo;
    private KmStartConversationHandler startConversationHandler;
    private KMStartChatHandler startChatHandler;


    public KmConversationCreateTask(Context context, ChannelInfo channelInfo, KmStartConversationHandler startConversationHandler) {
        this(context, channelInfo, null, startConversationHandler);
    }

    public KmConversationCreateTask(Context context, ChannelInfo channelInfo, KMStartChatHandler startChatHandler) {
        this(context, channelInfo, startChatHandler, null);
    }

    private KmConversationCreateTask(Context context, ChannelInfo channelInfo, KMStartChatHandler startChatHandler, KmStartConversationHandler startConversationHandler) {
        this.context = new WeakReference<>(context);
        this.startConversationHandler = startConversationHandler;
        this.startChatHandler = startChatHandler;
        this.channelInfo = channelInfo;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected ChannelFeedApiResponse doInBackground(Void... voids) {
        if (channelInfo != null) {
            return channelService.createChannelWithResponse(channelInfo);
        }
        return null;
    }

    @Override
    protected void onPostExecute(ChannelFeedApiResponse channelFeedApiResponse) {
        super.onPostExecute(channelFeedApiResponse);
        if (channelFeedApiResponse != null) {
            if (channelFeedApiResponse.isSuccess()) {
                if (startConversationHandler != null) {
                    startConversationHandler.onSuccess(channelService.getChannel(channelFeedApiResponse.getResponse()), context.get());
                }
                if (startChatHandler != null) {
                    startChatHandler.onSuccess(channelService.getChannel(channelFeedApiResponse.getResponse()), context.get());
                }
            } else {
                sendFailureResult(channelFeedApiResponse);
            }
        } else {
            sendFailureResult(null);
        }
    }

    private void sendFailureResult(ChannelFeedApiResponse channelFeedApiResponse) {
        if (startConversationHandler != null) {
            startConversationHandler.onFailure(channelFeedApiResponse, context.get());
        }
        if (startChatHandler != null) {
            startChatHandler.onFailure(channelFeedApiResponse, context.get());
        }
    }
}
