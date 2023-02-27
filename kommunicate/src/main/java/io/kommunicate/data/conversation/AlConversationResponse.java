package io.kommunicate.data.conversation;

import io.kommunicate.data.account.user.UserDetail;
import io.kommunicate.data.json.JsonMarker;
import io.kommunicate.models.feed.ChannelFeed;

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
