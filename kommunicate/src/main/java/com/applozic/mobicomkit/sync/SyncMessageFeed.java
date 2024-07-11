package com.applozic.mobicomkit.sync;

/**
 * Created by devashish on 15/1/15.
 */

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicomkit.api.conversation.Message;

import java.util.List;

/**
 * @author devashish
 */
public class SyncMessageFeed extends JsonMarker {

    private Long lastSyncTime;
    private Long currentSyncTime;
    private List<Message> messages;
    private boolean regIdInvalid;
    private List<String> deliveredMessageKeys;

    public Long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Long getCurrentSyncTime() {
        return currentSyncTime;
    }

    public void setCurrentSyncTime(Long currentSyncTime) {
        this.currentSyncTime = currentSyncTime;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public boolean isRegIdInvalid() {
        return regIdInvalid;
    }

    public void setRegIdInvalid(boolean regIdInvalid) {
        this.regIdInvalid = regIdInvalid;
    }

    public List<String> getDeliveredMessageKeys() {
        return deliveredMessageKeys;
    }

    public void setDeliveredMessageKeys(List<String> deliveredMessageKeys) {
        this.deliveredMessageKeys = deliveredMessageKeys;
    }
}
