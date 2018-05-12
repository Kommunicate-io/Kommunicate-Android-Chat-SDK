package io.kommunicate;

import com.applozic.mobicomkit.api.people.ChannelInfo;

import java.util.List;


/**
 * Created by ashish on 23/01/18.
 */

public class KMGroupInfo extends ChannelInfo {

    public KMGroupInfo(String groupName, List<String> groupMemberList) {
        super(groupName, groupMemberList);


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
                ", users=" + getUsers() +
                '}';
    }
}
