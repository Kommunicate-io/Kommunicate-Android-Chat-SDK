package com.applozic.mobicomkit.api.people;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by sunil on 29/1/16.
 */
public class ChannelCreate extends JsonMarker {
    private String groupName;
    private List<String> groupMemberList;


    public ChannelCreate(String groupName, List<String> groupMemberList) {
        this.groupName = groupName;
        this.groupMemberList = groupMemberList;
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

    @Override
    public String toString() {
        return "ChannelCreate{" +
                "groupName='" + groupName + '\'' +
                ", groupMemberList=" + groupMemberList +
                '}';
    }
}
