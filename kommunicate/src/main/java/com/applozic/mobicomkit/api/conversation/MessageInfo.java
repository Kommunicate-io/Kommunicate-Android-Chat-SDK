package com.applozic.mobicomkit.api.conversation;

/**
 * Created by devashish on 28/03/16.
 */

import com.applozic.mobicommons.json.JsonMarker;


public class MessageInfo extends JsonMarker {

    String userId;
    Short status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public boolean isRead() {
        return Message.Status.READ.getValue().equals(getStatus()) || Message.Status.DELIVERED_AND_READ.getValue().equals(getStatus());
    }

    public boolean isDelivered() {
        return Message.Status.DELIVERED.getValue().equals(getStatus());
    }

}
