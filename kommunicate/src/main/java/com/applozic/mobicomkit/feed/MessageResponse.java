package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 22/10/15.
 */
public class MessageResponse extends JsonMarker {
    private String messageKey;
    private String createdAt;
    private Integer conversationId;

    public MessageResponse() {
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getCreatedAtTime() {
        return createdAt;
    }

    public void setCreatedAtTime(String createdAtTime) {
        this.createdAt = createdAtTime;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "messageKey='" + messageKey + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", conversationId=" + conversationId +
                '}';
    }

}
