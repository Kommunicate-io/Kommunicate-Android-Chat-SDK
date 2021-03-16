package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 1/12/16.
 */

public class ChannelUsersFeed extends JsonMarker {
    private String userId;
    private Integer parentGroupKey;
    private Integer role;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getParentGroupKey() {
        return parentGroupKey;
    }

    public void setParentGroupKey(Integer parentGroupKey) {
        this.parentGroupKey = parentGroupKey;
    }

    public Integer getRole() {
        return role == null ?0:role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ChannelUsersFeed{" +
                "userId='" + userId + '\'' +
                ", parentGroupKey=" + parentGroupKey +
                ", role=" + role +
                '}';
    }
}

