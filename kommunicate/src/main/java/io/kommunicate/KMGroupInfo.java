package io.kommunicate;

import com.applozic.mobicomkit.api.people.ChannelInfo;

import java.util.List;

import io.kommunicate.users.KMGroupUser;

/**
 * Created by ashish on 23/01/18.
 */

public class KMGroupInfo extends ChannelInfo {

    List<KMGroupUser> users;

    public KMGroupInfo(String groupName, List<String> groupMemberList) {
        super(groupName, groupMemberList);
    }

    public List<KMGroupUser> getUsers() {
        return users;
    }

    public void setUsers(List<KMGroupUser> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "KMGroupInfo{" +
                "clientGroupId='" + getClientGroupId() + '\'' +
                ", groupName='" + getGroupName() + '\'' +
                ", groupMemberList=" + getGroupMemberList() +
                ", imageUrl='" + getImageUrl() + '\'' +
                ", type=" + getType() +
                ", metadata=" + getMetadata() +
                ", admin='" + getAdmin() + '\'' +
                ", channelMetadata=" + getChannelMetadata() +
                ", users=" + users +
                '}';
    }
}
