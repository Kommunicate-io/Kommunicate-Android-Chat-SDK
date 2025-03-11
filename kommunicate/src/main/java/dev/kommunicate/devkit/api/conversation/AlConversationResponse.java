package dev.kommunicate.devkit.api.conversation;

import dev.kommunicate.devkit.api.account.user.UserDetail;
import dev.kommunicate.devkit.feed.ChannelFeed;
import dev.kommunicate.commons.json.JsonMarker;

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
