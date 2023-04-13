package io.kommunicate.callbacks;


import android.content.Context;

import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.models.feed.ChannelFeedApiResponse;

public interface KmStartConversationHandler {
    void onSuccess(Channel channel, Context context);

    void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context);
}
