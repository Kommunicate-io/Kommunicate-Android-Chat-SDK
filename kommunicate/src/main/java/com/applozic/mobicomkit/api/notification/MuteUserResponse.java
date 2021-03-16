package com.applozic.mobicomkit.api.notification;

import java.util.Map;

/**
 * Created by reytum on 20/11/17.
 */

public class MuteUserResponse {
    private String userId;
    private boolean connected;
    private int unreadCount;
    private String imageLink;
    private boolean deactivated;
    private int connectedClientCount;
    private boolean active;
    private Map<String, String> metadata;
    private Long notificationAfterTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public int getConnectedClientCount() {
        return connectedClientCount;
    }

    public void setConnectedClientCount(int connectedClientCount) {
        this.connectedClientCount = connectedClientCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Long getNotificationAfterTime() {
        return notificationAfterTime;
    }

    public void setNotificationAfterTime(Long notificationAfterTime) {
        this.notificationAfterTime = notificationAfterTime;
    }

    @Override
    public String toString() {
        return "MuteUserResponse{" +
                "userId='" + userId + '\'' +
                ", connected=" + connected +
                ", unreadCount=" + unreadCount +
                ", imageLink='" + imageLink + '\'' +
                ", deactivated=" + deactivated +
                ", connectedClientCount=" + connectedClientCount +
                ", active=" + active +
                ", metadata=" + metadata +
                ", notificationAfterTime=" + notificationAfterTime +
                '}';
    }
}
