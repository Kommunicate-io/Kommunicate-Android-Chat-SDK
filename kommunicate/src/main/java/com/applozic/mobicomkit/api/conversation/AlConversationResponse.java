package com.applozic.mobicomkit.api.conversation;

import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicommons.json.JsonMarker;

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
