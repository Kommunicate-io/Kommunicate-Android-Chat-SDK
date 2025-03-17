package io.kommunicate.devkit.api.conversation;

import io.kommunicate.devkit.api.account.user.UserDetail;
import io.kommunicate.devkit.feed.ChannelFeed;
import io.kommunicate.commons.json.JsonMarker;

public class AlConversationResponse extends JsonMarker {
    private Message[] message;
    private ChannelFeed[] groupFeeds;
    private UserDetail[] userDetails;

    public Message[] getMessage() {
        return message;
    }

    public ChannelFeed[] getGroupFeeds() {
        return groupFeeds;
    }

    public UserDetail[] getUserDetails() {
        return userDetails;
    }
}
