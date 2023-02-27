package io.kommunicate.callbacks;

import android.content.Context;

import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.models.feed.ChannelFeedApiResponse;

/**
 * Created by ashish on 23/01/18.
 */

public interface KMStartChatHandler {
    void onSuccess(Channel channel, Context context);

    void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context);
}
