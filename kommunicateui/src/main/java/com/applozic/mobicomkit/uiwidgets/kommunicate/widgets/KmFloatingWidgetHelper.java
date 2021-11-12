package com.applozic.mobicomkit.uiwidgets.kommunicate.widgets;


import android.content.Context;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.kommunicate.async.KmAppSettingTask;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAppSettingModel;

public class KmFloatingWidgetHelper implements ApplozicUIListener {
    private FloatingView kmFloatingView;

    public KmFloatingWidgetHelper(FloatingView kmFloatingView) {
        this.kmFloatingView = kmFloatingView;
    }

    @Override
    public void onMessageSent(Message message) {

    }

    @Override
    public void onMessageReceived(Message message) {
        kmFloatingView.showUnreadCount();
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
        kmFloatingView.showUnreadCount();
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
        kmFloatingView.showUnreadCount();
    }

    @Override
    public void onUserOnline() {
        kmFloatingView.showUnreadCount();
    }

    @Override
    public void onUserOffline() {
        kmFloatingView.showUnreadCount();
    }

    @Override
    public void onUserActivated(boolean isActivated) {

    }

    @Override
    public void onChannelUpdated() {

    }

    @Override
    public void onConversationRead(String userId, boolean isGroup) {

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
