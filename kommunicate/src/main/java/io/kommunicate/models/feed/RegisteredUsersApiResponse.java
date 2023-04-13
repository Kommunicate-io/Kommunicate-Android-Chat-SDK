package io.kommunicate.models.feed;

import java.util.Set;

import io.kommunicate.data.account.user.UserDetail;
import io.kommunicate.data.json.JsonMarker;

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
