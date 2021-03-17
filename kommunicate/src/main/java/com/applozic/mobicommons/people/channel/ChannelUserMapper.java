package com.applozic.mobicommons.people.channel;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 28/12/15.
 */
public class ChannelUserMapper extends JsonMarker {

    private Integer key;
    private String userKey;
    private short status;
    private int unreadCount;
    private Integer role;
    private Integer parentKey;

    public ChannelUserMapper() {
    }

    public ChannelUserMapper(Integer key, String userKey, int unreadCount) {
        this.key = key;
        this.userKey = userKey;
        this.unreadCount = unreadCount;
    }

    public ChannelUserMapper(Integer key, String userKey) {
        this.key = key;
        this.userKey = userKey;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public Integer getRole() {
        return role == null ?0:role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getParentKey() {
        return parentKey;
    }

    public void setParentKey(Integer parentKey) {
        this.parentKey = parentKey;
    }
    public enum UserRole {
        ADMIN(1),
        MODERATOR(2),
        MEMBER(3);
        private Integer value;

        UserRole(Integer value) {
            this.value = value;
        }
        public Integer getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "ChannelUserMapper{" +
                "key=" + key +
                ", userKey='" + userKey + '\'' +
                ", status=" + status +
                ", unreadCount=" + unreadCount +
                ", role=" + role +
                ", parentKey=" + parentKey +
                '}';
    }
}
