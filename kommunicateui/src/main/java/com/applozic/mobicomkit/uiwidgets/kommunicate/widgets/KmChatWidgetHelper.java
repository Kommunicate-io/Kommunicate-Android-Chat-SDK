package com.applozic.mobicomkit.uiwidgets.kommunicate.widgets;


import android.content.Context;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.listners.ApplozicUIListener;

public class KmChatWidgetHelper implements ApplozicUIListener {
    private KmChatWidget kmChatWidget;
    private MessageDatabaseService messageDatabaseService;

    public KmChatWidgetHelper(KmChatWidget kmFloatingView, Context context) {
        this.kmChatWidget = kmFloatingView;
        messageDatabaseService = new MessageDatabaseService(context);
        setUnreadCount();
    }

    private void setUnreadCount() {
        kmChatWidget.showUnreadCount(messageDatabaseService.getTotalUnreadCount());
    }

    @Override
    public void onMessageReceived(Message message) {
        int messageUnReadCount = 0;
        messageUnReadCount = messageDatabaseService.getTotalUnreadCount();
        kmChatWidget.showUnreadCount(messageUnReadCount);
    }

    @Override
    public void onConversationRead(String userId, boolean isGroup) {
        kmChatWidget.showUnreadCount(messageDatabaseService.getTotalUnreadCount());
    }

    @Override
    public void onMessageSent(Message message) {
    }

    @Override
    public void onLoadMore(boolean loadMore) {
    }

    @Override
    public void onMessageSync(Message message, String key) {
    }

    @Override
    public void onMessageDeleted(String messageKey, String userId) {
    }

    @Override
    public void onMessageDelivered(Message message, String userId) {
    }

    @Override
    public void onAllMessagesDelivered(String userId) {
    }

    @Override
    public void onAllMessagesRead(String userId) {
    }

    @Override
    public void onConversationDeleted(String userId, Integer channelKey, String response) {
    }

    @Override
    public void onUpdateTypingStatus(String userId, String isTyping) {
    }

    @Override
    public void onUpdateLastSeen(String userId) {

    }

    @Override
    public void onMqttDisconnected() {
    }

    @Override
    public void onMqttConnected() {
    }

    @Override
    public void onUserOnline() {
    }

    @Override
    public void onUserOffline() {
    }

    @Override
    public void onUserActivated(boolean isActivated) {

    }

    @Override
    public void onChannelUpdated() {
    }

    @Override
    public void onUserDetailUpdated(String userId) {

    }

    @Override
    public void onMessageMetadataUpdated(String keyString) {
    }

    @Override
    public void onUserMute(boolean mute, String userId) {
    }

    @Override
    public void onGroupMute(Integer groupId) {
    }
}
