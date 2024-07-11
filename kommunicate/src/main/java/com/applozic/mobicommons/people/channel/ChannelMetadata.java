package com.applozic.mobicommons.people.channel;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunil on 3/9/16.
 */
public class ChannelMetadata extends JsonMarker {

    public final static String CREATE_GROUP_MESSAGE = "CREATE_GROUP_MESSAGE";
    public final static String REMOVE_MEMBER_MESSAGE = "REMOVE_MEMBER_MESSAGE";
    public final static String ADD_MEMBER_MESSAGE = "ADD_MEMBER_MESSAGE";
    public final static String JOIN_MEMBER_MESSAGE = "JOIN_MEMBER_MESSAGE";
    public final static String GROUP_NAME_CHANGE_MESSAGE = "GROUP_NAME_CHANGE_MESSAGE";
    public final static String GROUP_ICON_CHANGE_MESSAGE = "GROUP_ICON_CHANGE_MESSAGE";
    public final static String GROUP_LEFT_MESSAGE = "GROUP_LEFT_MESSAGE";
    public final static String DELETED_GROUP_MESSAGE = "DELETED_GROUP_MESSAGE";
    public final static String HIDE_METADATA_NOTIFICATION = "HIDE";
    public final static String ALERT_METADATA_NOTIFICATION = "ALERT";
    public final static String MUTE = "MUTE";
    public final static String AL_CONTEXT_BASED_CHAT = "AL_CONTEXT_BASED_CHAT";
    public static final String AL_CHANNEL_ACTION = "action";

    public static final String ADMIN_NAME = ":adminName";
    public static final String GROUP_NAME = ":groupName";
    public static final String USER_NAME = ":userName";

    private String createGroupMessage;
    private String removeMemberMessage;
    private String addMemberMessage;
    private String JoinMemberMessage;
    private String groupNameChangeMessage;
    private String groupIconChangeMessage;
    private String groupLeftMessage;
    private String deletedGroupMessage;
    private boolean hideMetaDataNotification;
    private boolean alertMetaDataNotfication;
    private boolean defaultMute;
    private boolean contextBasedChat;

    public String getCreateGroupMessage() {
        return createGroupMessage;
    }

    public void setCreateGroupMessage(String createGroupMessage) {
        this.createGroupMessage = createGroupMessage;
    }

    public String getRemoveMemberMessage() {
        return removeMemberMessage;
    }

    public void setRemoveMemberMessage(String removeMemberMessage) {
        this.removeMemberMessage = removeMemberMessage;
    }

    public String getAddMemberMessage() {
        return addMemberMessage;
    }

    public void setAddMemberMessage(String addMemberMessage) {
        this.addMemberMessage = addMemberMessage;
    }

    public String getJoinMemberMessage() {
        return JoinMemberMessage;
    }

    public void setJoinMemberMessage(String joinMemberMessage) {
        JoinMemberMessage = joinMemberMessage;
    }

    public String getGroupNameChangeMessage() {
        return groupNameChangeMessage;
    }

    public void setGroupNameChangeMessage(String groupNameChangeMessage) {
        this.groupNameChangeMessage = groupNameChangeMessage;
    }

    public String getGroupIconChangeMessage() {
        return groupIconChangeMessage;
    }

    public void setGroupIconChangeMessage(String groupIconChangeMessage) {
        this.groupIconChangeMessage = groupIconChangeMessage;
    }

    public String getGroupLeftMessage() {
        return groupLeftMessage;
    }

    public void setGroupLeftMessage(String groupLeftMessage) {
        this.groupLeftMessage = groupLeftMessage;
    }

    public String getDeletedGroupMessage() {
        return deletedGroupMessage;
    }

    public void setDeletedGroupMessage(String deletedGroupMessage) {
        this.deletedGroupMessage = deletedGroupMessage;
    }

    public boolean isHideMetaDataNotification() {
        return hideMetaDataNotification;
    }

    public void setHideMetaDataNotification(boolean hideMetaDataNotification) {
        this.hideMetaDataNotification = hideMetaDataNotification;
    }

    private void buildEmptyMetadata() {
        this.createGroupMessage = "";
        this.removeMemberMessage = "";
        this.addMemberMessage = "";
        this.JoinMemberMessage = "";
        this.groupIconChangeMessage = "";
        this.groupNameChangeMessage = "";
        this.groupLeftMessage = "";
        this.deletedGroupMessage = "";
    }

    public void hideAllMetadataMessages() {
        buildEmptyMetadata();
        this.hideMetaDataNotification = true;
        this.alertMetaDataNotfication = false;
    }

    public boolean isAlertMetaDataNotfication() {
        return alertMetaDataNotfication;
    }

    public void setAlertMetaDataNotfication(boolean alertMetaDataNotfication) {
        if (!alertMetaDataNotfication) {
            buildEmptyMetadata();
        }
        this.alertMetaDataNotfication = alertMetaDataNotfication;
    }

    public boolean isDefaultMute() {
        return defaultMute;
    }

    public void setDefaultMute(boolean defaultMute) {
        this.defaultMute = defaultMute;
    }

    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(ChannelMetadata.CREATE_GROUP_MESSAGE, this.getCreateGroupMessage());
        metadata.put(ChannelMetadata.ADD_MEMBER_MESSAGE, this.getAddMemberMessage());
        metadata.put(ChannelMetadata.GROUP_NAME_CHANGE_MESSAGE, this.getGroupNameChangeMessage());
        metadata.put(ChannelMetadata.GROUP_ICON_CHANGE_MESSAGE, this.getGroupIconChangeMessage());
        metadata.put(ChannelMetadata.GROUP_LEFT_MESSAGE, this.getGroupLeftMessage());
        metadata.put(ChannelMetadata.JOIN_MEMBER_MESSAGE, this.getJoinMemberMessage());
        metadata.put(ChannelMetadata.DELETED_GROUP_MESSAGE, this.getDeletedGroupMessage());
        metadata.put(ChannelMetadata.REMOVE_MEMBER_MESSAGE, this.getRemoveMemberMessage());
        metadata.put(ChannelMetadata.HIDE_METADATA_NOTIFICATION, this.isHideMetaDataNotification() + "");
        metadata.put(ChannelMetadata.ALERT_METADATA_NOTIFICATION, this.isAlertMetaDataNotfication() + "");
        metadata.put(ChannelMetadata.MUTE, this.isDefaultMute() + "");
        return metadata;
    }

    public boolean isContextBasedChat() {
        return contextBasedChat;
    }

    public void setContextBasedChat(boolean contextBasedChat) {
        this.contextBasedChat = contextBasedChat;
    }
}
