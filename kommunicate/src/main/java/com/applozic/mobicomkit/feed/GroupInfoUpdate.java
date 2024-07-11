package com.applozic.mobicomkit.feed;

import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicommons.json.Exclude;
import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sunil on 11/3/16.
 */
public class GroupInfoUpdate extends JsonMarker {

    private Integer groupId;
    private String clientGroupId;
    private Integer parentKey;
    private Set<Integer> childKeys = new HashSet<>();
    private String newName;
    private String imageUrl;
    @Exclude
    private String localImagePath;
    @Exclude
    private String newlocalPath;
    @Exclude
    private String contentUri;
    private int kmStatus;
    private Map<String, String> metadata = new HashMap<>();
    private List<ChannelUsersFeed> users = new ArrayList<>();

    public GroupInfoUpdate(Integer channelKey) {
        this.groupId = channelKey;
    }

    public GroupInfoUpdate(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public GroupInfoUpdate(Channel channel) {
        this.newName = channel.getName();
        this.groupId = channel.getKey();
        this.clientGroupId = channel.getClientGroupId();
        this.imageUrl = channel.getImageUrl();
        this.localImagePath = channel.getLocalImageUri();
        this.kmStatus = channel.getKmStatus();
    }

    public GroupInfoUpdate(ChannelInfo channel) {
        this.metadata = channel.getMetadata();
    }


    public GroupInfoUpdate(Map<String, String> metadata, int groupId) {
        this.metadata = metadata;
        this.groupId = groupId;
    }

    public GroupInfoUpdate(Map<String, String> metadata, String clientGroupId) {
        this.metadata = metadata;
        this.clientGroupId = clientGroupId;
    }

    public GroupInfoUpdate(String newName, int groupId) {
        this.newName = newName;
        this.groupId = groupId;
    }

    public GroupInfoUpdate(String newName, String clientGroupId) {
        this.newName = newName;
        this.clientGroupId = clientGroupId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String localImagePath) {
        this.localImagePath = localImagePath;
    }

    public String getNewlocalPath() {
        return newlocalPath;
    }

    public void setNewlocalPath(String newlocalPath) {
        this.newlocalPath = newlocalPath;
    }

    public Integer getParentKey() {
        return parentKey;
    }

    public void setParentKey(Integer parentKey) {
        this.parentKey = parentKey;
    }

    public Set<Integer> getChildKeys() {
        return childKeys;
    }

    public void setChildKeys(Set<Integer> childKeys) {
        this.childKeys = childKeys;
    }

    public String getContentUri() {
        return contentUri;
    }

    public void setContentUri(String contentUri) {
        this.contentUri = contentUri;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<ChannelUsersFeed> getUsers() {
        return users;
    }

    public void setUsers(List<ChannelUsersFeed> users) {
        this.users = users;
    }

    public int getKmStatus() {
        return kmStatus;
    }

    public void setKmStatus(int kmStatus) {
        this.kmStatus = kmStatus;
    }

    @Override
    public String toString() {
        return "GroupInfoUpdate{" +
                "groupId=" + groupId +
                ", clientGroupId='" + clientGroupId + '\'' +
                ", parentKey=" + parentKey +
                ", childKeys=" + childKeys +
                ", newName='" + newName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", localImagePath='" + localImagePath + '\'' +
                ", newlocalPath='" + newlocalPath + '\'' +
                ", contentUri='" + contentUri + '\'' +
                ", users=" + users +
                '}';
    }
}
