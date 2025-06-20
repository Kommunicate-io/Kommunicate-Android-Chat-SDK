package io.kommunicate.devkit.feed;

import io.kommunicate.devkit.api.account.user.UserDetail;
import io.kommunicate.commons.json.JsonMarker;

import java.util.Set;

/**
 * Created by sunil on 28/4/16.
 */
public class RegisteredUsersApiResponse extends JsonMarker {

    private Set<UserDetail> users;
    private long lastFetchTime;
    private Integer totalUnreadCount;

    public Set<UserDetail> getUsers() {
        return users;
    }

    public void setUsers(Set<UserDetail> users) {
        this.users = users;
    }

    public long getLastFetchTime() {
        return lastFetchTime;
    }

    public void setLastFetchTime(long lastFetchTime) {
        this.lastFetchTime = lastFetchTime;
    }

    public Integer getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(Integer totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    @Override
    public String toString() {
        return "RegisteredUsersApiResponse{" +
                "users=" + users +
                ", lastFetchTime=" + lastFetchTime +
                ", totalUnreadCount=" + totalUnreadCount +
                '}';
    }
}
