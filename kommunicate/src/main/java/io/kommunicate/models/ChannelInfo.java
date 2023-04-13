package io.kommunicate.models;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kommunicate.data.json.JsonMarker;
import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.channel.ChannelMetadata;

/**
 * Created by sunil on 29/1/16.
 */
public class ChannelInfo extends JsonMarker {

    List<GroupUser> users;
    private String clientGroupId;
    private String groupName;
    private List<String> groupMemberList;
    private String imageUrl;
    private int type = Channel.GroupType.PUBLIC.getValue().intValue();
    private Map<String, String> metadata;
    private String admin;
    private Integer parentKey;
    private String parentClientGroupId;
    private ChannelMetadata channelMetadata;

    public ChannelInfo() {
        this.metadata = new HashMap<>();
    }

    public ChannelInfo(String groupName, List<String> groupMemberList) {
        this();
        this.groupName = groupName;
        this.groupMemberList = groupMemberList;
    }

    public ChannelInfo(String groupName, List<String> groupMemberList, String imageLink) {
        this(groupName, groupMemberList);
        this.imageUrl = imageLink;
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getGroupMemberList() {
        return groupMemberList;
    }

    public void setGroupMemberList(List<String> groupMemberList) {
        this.groupMemberList = groupMemberList;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ChannelMetadata getChannelMetadata() {
        return channelMetadata;
    }

    public void setChannelMetadata(ChannelMetadata channelMetadata) {
        this.channelMetadata = channelMetadata;
        this.metadata = channelMetadata.getMetadata();
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public List<GroupUser> getUsers() {
        return users;
    }

    public void setUsers(List<GroupUser> users) {
        this.users = users;
    }

    public Integer getParentKey() {
        return parentKey;
    }

    public void setParentKey(Integer parentKey) {
        this.parentKey = parentKey;
    }

    public String getParentClientGroupId() {
        return parentClientGroupId;
    }

    public void setParentClientGroupId(String parentClientGroupId) {
        this.parentClientGroupId = parentClientGroupId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ChannelInfo{" +
                "clientGroupId='" + clientGroupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", users=" + users +
                ", groupMemberList=" + groupMemberList +
                ", imageUrl='" + imageUrl + '\'' +
                ", type=" + type +
                ", metadata=" + metadata +
                ", admin='" + admin + '\'' +
                ", parentKey=" + parentKey +
                ", channelMetadata=" + channelMetadata +
                '}';
    }

    public class GroupUser extends JsonMarker {
        String userId;
        int groupRole;

        public String getUserId() {
            return userId;
        }

        public GroupUser setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public int getGroupRole() {
            return groupRole;
        }

        public GroupUser setGroupRole(int groupRole) {
            this.groupRole = groupRole;
            return this;
        }

        @Override
        public String toString() {
            return "GroupUser{" +
                    "userId='" + userId + '\'' +
                    ", groupRole=" + groupRole +
                    '}';
        }
    }
}
