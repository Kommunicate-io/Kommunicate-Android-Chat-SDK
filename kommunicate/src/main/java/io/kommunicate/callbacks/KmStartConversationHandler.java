package io.kommunicate.callbacks;


import android.content.Context;

import dev.kommunicate.commons.people.channel.Channel;

public interface KmStartConversationHandler {
    void onSuccess(Channel channel, Context context);

    void onFailure(Exception error, Context context);
}
