package io.kommunicate.users;

import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicommons.people.channel.Channel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ashish on 31/01/18.
 */

public class KmUserDetailResponse extends JsonMarker implements Serializable {
    private List<UserDetail> users;
    private List<Channel> groups;
    private List<String> devices;
    private Long lastFetchTime;
    private int lastFetchIndex;
    private int totalUnreadCount;

    public List<UserDetail> getUsers() {
        return users;
    }

    public void setUsers(List<UserDetail> users) {
        this.users = users;
    }

    public List<Channel> getGroups() {
        return groups;
    }

    public void setGroups(List<Channel> groups) {
        this.groups = groups;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public long getLastFetchTime() {
        return lastFetchTime;
    }

    public void setLastFetchTime(long lastFetchTime) {
        this.lastFetchTime = lastFetchTime;
    }

    public int getLastFetchIndex() {
        return lastFetchIndex;
    }

    public void setLastFetchIndex(int lastFetchIndex) {
        this.lastFetchIndex = lastFetchIndex;
    }

    public int getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(int totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    @Override
    public String toString() {
        return "KmUserDetailResponse{" +
                "users=" + users +
                ", groups=" + groups +
                ", devices=" + devices +
                ", lastFetchTime=" + lastFetchTime +
                ", lastFetchIndex=" + lastFetchIndex +
                ", totalUnreadCount=" + totalUnreadCount +
                '}';
    }
}
