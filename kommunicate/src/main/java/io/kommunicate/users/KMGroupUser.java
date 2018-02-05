package io.kommunicate.users;

/**
 * Created by ashish on 23/01/18.
 */

public class KMGroupUser {
    String userId;
    int groupRole;

    public String getUserId() {
        return userId;
    }

    public KMGroupUser setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public int getGroupRole() {
        return groupRole;
    }

    public KMGroupUser setGroupRole(int groupRole) {
        this.groupRole = groupRole;
        return this;
    }

    @Override
    public String toString() {
        return "KMGroupUser{" +
                "userId='" + userId + '\'' +
                ", groupRole=" + groupRole +
                '}';
    }
}
