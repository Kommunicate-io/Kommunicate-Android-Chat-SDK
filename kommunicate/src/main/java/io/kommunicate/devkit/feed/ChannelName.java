package io.kommunicate.devkit.feed;

import io.kommunicate.commons.json.JsonMarker;

/**
 * Created by sunil on 11/3/16.
 */
public class ChannelName extends JsonMarker {

    private Integer groupId;
    private String newName;

    public ChannelName(String newName, Integer groupId) {
        this.newName = newName;
        this.groupId = groupId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
