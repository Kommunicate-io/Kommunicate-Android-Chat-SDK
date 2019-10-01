package io.kommunicate.callbacks;

import android.content.Context;

import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicommons.people.channel.Channel;

/**
 * Created by ashish on 23/01/18.
 */

public interface KMStartChatHandler {
    void onSuccess(Channel channel, Context context);

    void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context);
}
