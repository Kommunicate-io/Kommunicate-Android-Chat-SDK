package com.applozic.mobicomkit.listners;

import com.applozic.mobicomkit.api.conversation.Message;

/**
 * Created by reytum on 5/12/17.
 */

public interface ApplozicUIListener {
    void onMessageSent(Message message);

    void onMessageReceived(Message message);

    void onLoadMore(boolean loadMore);

    void onMessageSync(Message message, String key);

    void onMessageDeleted(String messageKey, String userId);

    void onMessageDelivered(Message message, String userId);

    void onAllMessagesDelivered(String userId);

    void onAllMessagesRead(String userId);

    void onConversationDeleted(String userId, Integer channelKey, String response);

    void onUpdateTypingStatus(String userId, String isTyping);

    void onUpdateLastSeen(String userId);

    void onMqttDisconnected();

    void onMqttConnected();

    void onUserOnline();

    void onUserOffline();

    void onUserActivated(boolean isActivated);

    void onChannelUpdated();

    void onConversationRead(String userId, boolean isGroup);

    void onUserDetailUpdated(String userId);

    void onMessageMetadataUpdated(String keyString);

    void onUserMute(boolean mute, String userId);

    void onGroupMute(Integer groupId);
}
