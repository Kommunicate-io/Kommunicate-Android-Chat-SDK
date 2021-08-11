package com.applozic.mobicommons.people.channel;

import android.text.TextUtils;

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by devashish on 5/9/14.
 */
public class Channel extends JsonMarker {

    private Map<String, String> metadata = new HashMap<>();
    private Integer key;
    private Integer parentKey;
    private String parentClientGroupId;
    private String clientGroupId;
    private int subGroupCount;
    private String name;
    private String adminKey;
    private Short type;
    private int unreadCount;
    private int userCount;
    private String imageUrl;
    @Expose
    private String localImageUri;
    private Conversation conversationPxy;
    private List<Contact> contacts = new ArrayList<Contact>();
    private Long notificationAfterTime;
    private Long deletedAtTime;
    private int kmStatus;
    public static final String AL_CATEGORY = "AL_CATEGORY";
    public static final String CONVERSATION_STATUS = "CONVERSATION_STATUS";
    public static final String CONVERSATION_ASSIGNEE = "CONVERSATION_ASSIGNEE";
    public static final String KM_TEAM_ID = "KM_TEAM_ID";
    public static final int CLOSED_CONVERSATIONS = 3;
    public static final int ASSIGNED_CONVERSATIONS = 1;
    public static final int ALL_CONVERSATIONS = 2;
    public static final int NOTSTARTED_CONVERSATIONS = -1;
    public static final String AL_BLOCK = "AL_BLOCK";

    public Channel() {

    }

    public Channel(Integer key, String name, String adminKey, Short type, int unreadCount, String imageUrl) {
        this.key = key;
        this.name = name;
        this.adminKey = adminKey;
        this.type = type;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.unreadCount = unreadCount;
    }

    public Channel(Integer key) {
        this.key = key;
    }

    public Channel(Integer key, String name) {
        this.key = key;
        this.name = name;

    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(String adminKey) {
        this.adminKey = adminKey;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public Conversation getConversationPxy() {
        return conversationPxy;
    }

    public void setConversationPxy(Conversation conversationPxy) {
        this.conversationPxy = conversationPxy;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocalImageUri() {
        return localImageUri;
    }

    public void setLocalImageUri(String localImageUri) {
        this.localImageUri = localImageUri;
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public boolean isBroadcastMessage() {
        return type.equals(GroupType.BROADCAST.getValue()) || type.equals(GroupType.BROADCAST_ONE_BY_ONE.getValue());
    }

    public Long getDeletedAtTime() {
        return deletedAtTime;
    }

    public void setDeletedAtTime(Long deletedAtTime) {
        this.deletedAtTime = deletedAtTime;
    }

    public Long getNotificationAfterTime() {
        return notificationAfterTime;
    }

    public void setNotificationAfterTime(Long notificationAfterTime) {
        this.notificationAfterTime = notificationAfterTime;
    }

    public boolean isNotificationMuted() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        return (((getNotificationAfterTime() != null) && (getNotificationAfterTime() - date.getTime() > 0))
                || ((getNotificationAfterTime() != null && getNotificationAfterTime() == 0 && isGroupDefaultMuted())));
    }

    public boolean isDeleted() {
        return (deletedAtTime != null && deletedAtTime > 0);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public boolean isGroupDefaultMuted() {
        return (getMetadata() != null && getMetadata().get(ChannelMetadata.MUTE) != null
                && getMetadata().get(ChannelMetadata.MUTE).equalsIgnoreCase("true"));
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

    public int getSubGroupCount() {
        return subGroupCount;
    }

    public void setSubGroupCount(int subGroupCount) {
        this.subGroupCount = subGroupCount;
    }

    public boolean isPartOfCategory(String category) {

        return (this.metadata != null && this.metadata.containsKey(AL_CATEGORY)
                && this.metadata.get(AL_CATEGORY).equals(category));

    }

    public boolean isContextBasedChat() {
        return (this.metadata != null && "true".equals(this.metadata.get(ChannelMetadata.AL_CONTEXT_BASED_CHAT)));
    }

    public int getKmStatus() {
        return kmStatus;
    }

    public void setKmStatus(int kmStatus) {
        this.kmStatus = kmStatus;
    }

    public boolean blockNotification(Short loggedInUserRole) {
        return isNotificationMuted() || (GroupType.SUPPORT_GROUP.getValue().equals(getType())
                && loggedInUserRole != 3
                && (getKmStatus() == ALL_CONVERSATIONS || getKmStatus() == CLOSED_CONVERSATIONS));
    }

    public int generateKmStatus(String loggedInUserId) {
        if (getMetadata() == null) {
            return ASSIGNED_CONVERSATIONS;
        }

        if (getMetadata().containsKey(CONVERSATION_STATUS) && ("2".equals(getMetadata().get(CONVERSATION_STATUS)) || "3".equals(getMetadata().get(CONVERSATION_STATUS)))) {
            return CLOSED_CONVERSATIONS;
        }

        if(getMetadata().containsKey(CONVERSATION_STATUS) && ("-1".equals(getMetadata().get(CONVERSATION_STATUS)))) {
            return NOTSTARTED_CONVERSATIONS;
        }

        if (getMetadata().containsKey(CONVERSATION_ASSIGNEE) && !TextUtils.isEmpty(getMetadata().get(CONVERSATION_ASSIGNEE))) {
            if (!TextUtils.isEmpty(loggedInUserId) && loggedInUserId.equals(getMetadata().get(CONVERSATION_ASSIGNEE))) {
                return ASSIGNED_CONVERSATIONS;
            }
            return ALL_CONVERSATIONS;
        }
        return ASSIGNED_CONVERSATIONS;
    }

    public int getConversationStatus() {
        return (GroupType.SUPPORT_GROUP.getValue().equals(getType()) && getMetadata() != null && !TextUtils.isEmpty(getMetadata().get(CONVERSATION_STATUS))) ? Integer.parseInt(getMetadata().get(CONVERSATION_STATUS)) : -1;
    }

    public String getConversationAssignee() {
        return (GroupType.SUPPORT_GROUP.getValue().equals(getType()) && getMetadata() != null && !TextUtils.isEmpty(getMetadata().get(CONVERSATION_ASSIGNEE))) ? getMetadata().get(CONVERSATION_ASSIGNEE) : null;
    }

    public String getTeamId() {
        return (GroupType.SUPPORT_GROUP.getValue().equals(getType()) && getMetadata() != null && !TextUtils.isEmpty(getMetadata().get(KM_TEAM_ID))) ? getMetadata().get(KM_TEAM_ID) : null;
    }

    public boolean isOpenGroup() {
        return GroupType.OPEN.getValue().equals(type);
    }

    public enum GroupType {

        VIRTUAL(0),
        PRIVATE(1),
        PUBLIC(2),
        SELLER(3),
        SELF(4),
        BROADCAST(5),
        OPEN(6),
        GROUPOFTWO(7),
        CONTACT_GROUP(9),
        SUPPORT_GROUP(10),
        BROADCAST_ONE_BY_ONE(106);

        private Integer value;

        GroupType(Integer value) {
            this.value = value;
        }

        public Short getValue() {
            return value.shortValue();
        }
    }

    public enum GroupMetaDataType {

        TITLE("title"),
        PRICE("price"),
        LINK("link");

        private String value;

        GroupMetaDataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class AlConversationStatus {
        public static final String RESOLVED_STATUS = "Resolved";
        public static final String SPAM_STATUS = "Spam/Irrelevant";
        public static final String OPEN_STATUS = "Open";
        public static final String CLOSED = "2";
        public static final String RESOLVED = "3";
    }

    @Override
    public String toString() {
        return "Channel{" +
                "metadata=" + metadata +
                ", key=" + key +
                ", parentKey=" + parentKey +
                ", parentClientGroupId='" + parentClientGroupId + '\'' +
                ", clientGroupId='" + clientGroupId + '\'' +
                ", subGroupCount=" + subGroupCount +
                ", name='" + name + '\'' +
                ", adminKey='" + adminKey + '\'' +
                ", type=" + type +
                ", unreadCount=" + unreadCount +
                ", userCount=" + userCount +
                ", imageUrl='" + imageUrl + '\'' +
                ", localImageUri='" + localImageUri + '\'' +
                ", conversationPxy=" + conversationPxy +
                ", contacts=" + contacts +
                ", notificationAfterTime=" + notificationAfterTime +
                ", deletedAtTime=" + deletedAtTime +
                ", kmStatus=" + kmStatus +
                '}';
    }
}
