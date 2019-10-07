package io.kommunicate.callbacks;


import android.content.Context;

import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicommons.people.channel.Channel;

public interface KmStartConversationHandler {
    void onSuccess(Channel channel, Context context);

    void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context);
}
