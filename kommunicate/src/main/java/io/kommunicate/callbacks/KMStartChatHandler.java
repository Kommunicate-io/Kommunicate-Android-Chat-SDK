package io.kommunicate.callbacks;

import android.content.Context;

import dev.kommunicate.commons.people.channel.Channel;

/**
 * Created by ashish on 23/01/18.
 */

public interface KMStartChatHandler {
    void onSuccess(Channel channel, Context context);

    void onFailure(Exception exception, Context context);
}
