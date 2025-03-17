package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.devkit.api.people.ChannelInfo;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.feed.ChannelFeedApiResponse;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KmStartConversationHandler;

@Deprecated
@CleanUpRequired(reason = "Migrated KmConversationCreateTask to ConversationCreateTask")
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
                sendFailureResult(null);
            }
        } else {
            sendFailureResult(null);
        }
    }

    private void sendFailureResult(Exception exception) {
        if (startConversationHandler != null) {
            startConversationHandler.onFailure(exception, context.get());
        }
        if (startChatHandler != null) {
            startChatHandler.onFailure(exception, context.get());
        }
    }
}
