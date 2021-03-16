package com.applozic.mobicomkit.broadcast;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.json.JsonMarker;

public class AlMessageEvent extends JsonMarker {
    private String action;
    private Message message;
    private String userId;
    private Integer groupId;
    private String messageKey;
    private String response;
    private String isTyping;
    private boolean loadMore;
    private boolean isGroup;

    public String getAction() {
        return action;
    }

    public AlMessageEvent setAction(String action) {
        this.action = action;
        return this;
    }

    public Message getMessage() {
        return message;
    }

    public AlMessageEvent setMessage(Message message) {
        this.message = message;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public AlMessageEvent setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public AlMessageEvent setGroupId(Integer groupId) {
        this.groupId = groupId;
        return this;
    }

    public boolean isLoadMore() {
        return loadMore;
    }

    public AlMessageEvent setLoadMore(boolean loadMore) {
        this.loadMore = loadMore;
        return this;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public AlMessageEvent setGroup(boolean group) {
        isGroup = group;
        return this;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public AlMessageEvent setMessageKey(String messageKey) {
        this.messageKey = messageKey;
        return this;
    }

    public String isTyping() {
        return isTyping;
    }

    public AlMessageEvent setTyping(String typing) {
        isTyping = typing;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public AlMessageEvent setResponse(String response) {
        this.response = response;
        return this;
    }

    public static class ActionType extends JsonMarker {
        public static final String MESSAGE_SENT = "MESSAGE_SENT";
        public static final String MESSAGE_RECEIVED = "MESSAGE_RECEIVED";
        public static final String MESSAGE_SYNC = "MESSAGE_SYNC";
        public static final String LOAD_MORE = "LOAD_MORE";
        public static final String MESSAGE_DELETED = "MESSAGE_DELETED";
        public static final String MESSAGE_DELIVERED = "MESSAGE_DELIVERED";
        public static final String ALL_MESSAGES_DELIVERED = "ALL_MESSAGES_DELIVERED";
        public static final String ALL_MESSAGES_READ = "ALL_MESSAGES_READ";
        public static final String CONVERSATION_DELETED = "CONVERSATION_DELETED";
        public static final String UPDATE_TYPING_STATUS = "UPDATE_TYPING_STATUS";
        public static final String UPDATE_LAST_SEEN = "UPDATE_LAST_SEEN";
        public static final String MQTT_DISCONNECTED = "MQTT_DISCONNECTED";
        public static final String MQTT_CONNECTED = "MQTT_CONNECTED";
        public static final String CURRENT_USER_ONLINE = "USER_ONLINE";
        public static final String CURRENT_USER_OFFLINE = "USER_OFFLINE";
        public static final String CHANNEL_UPDATED = "CHANNEL_UPDATED";
        public static final String CONVERSATION_READ = "CONVERSATION_READ";
        public static final String USER_DETAILS_UPDATED = "USER_DETAILS_UPDATED";
        public static final String MESSAGE_METADATA_UPDATED = "MESSAGE_METADATA_UPDATED";
        public static final String ON_USER_MUTE = "ON_USER_MUTE";
        public static final String GROUP_MUTE = "GROUP_MUTE";
        public static final String USER_ACTIVATED = "USER_ACTIVATED";
        public static final String USER_DEACTIVATED = "USER_DEACTIVATED";
    }
}
