package io.kommunicate.callbacks;

import android.content.Context;

import io.kommunicate.commons.people.channel.Channel;

import annotations.CleanUpRequired;

@Deprecated
@CleanUpRequired(reason = "Migrated this to TaskListener")
public interface KmGetConversationInfoCallback {
    void onSuccess(Channel channel, Context context);

    void onFailure(Exception e, Context context);
}
