package io.kommunicate.devkit.api.conversation;

import static io.kommunicate.commons.AppContextService.getAppContext;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Patterns;


import io.kommunicate.devkit.SettingsSharedPreference;
import io.kommunicate.devkit.api.account.user.User;
import io.kommunicate.devkit.api.notification.VideoCallNotificationHelper;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.commons.json.JsonMarker;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.attachment.FileMeta;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.channel.ChannelMetadata;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class Message extends JsonMarker implements Parcelable {

    private static final long serialVersionUID = 1990184800447349902L;
    private Long createdAtTime = new Date().getTime();
    private String to;
    private String message;
    private String key;
    private String deviceKey;
    private String userKey;
    private String emailIds;
    private boolean shared;
    private boolean sent;
    private Boolean delivered;
    private Short type = MessageType.MT_OUTBOX.getValue();
    private boolean storeOnDevice;
    private String contactIds = "";
    private Integer groupId;
    private Short groupStatus;
    private boolean sendToDevice;
    private Long scheduledAt;
    private Short source = Source.MT_MOBILE_APP.getValue();
    private Integer timeToLive;
    private boolean sentToServer = true;
    private String fileMetaKey;
    private List<String> filePaths;
    private String pairedMessageKey;
    private long sentMessageTimeAtServer;
    private boolean canceled = false;
    private String clientGroupId;
    @SerializedName("fileMeta")
    private FileMeta fileMeta;
    @SerializedName("id")
    private Long messageId;
    private Boolean read = false;
    private boolean attDownloadInProgress;
    private String applicationId;
    private Integer conversationId;
    private String topicId;
    private boolean connected = false;
    private short contentType = ContentType.DEFAULT.getValue();
    private Map<String, String> metadata = new HashMap<>();
    private short status = Status.READ.getValue();
    private boolean hidden;
    private int replyMessage;
    private String supportCustomerName;
    private String groupAssignee;
    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String CONTACT = "contact";
    public static final String LOCATION = "location";
    public static final String OTHER = "other";
    public static final String BOT_ASSIGN = "KM_ASSIGN";
    public static final String KM_ASSIGN_TO = "KM_ASSIGN_TO";
    public static final String KM_ASSIGN_TEAM = "KM_ASSIGN_TEAM";
    public static final String CONVERSATION_STATUS = "KM_STATUS";
    public static final String FEEDBACK_METADATA_KEY = "feedback";
    public static final String SKIP_BOT = "skipBot";
    public static final String AL_DELETE_MESSAGE_FOR_ALL_KEY = "AL_DELETE_GROUP_MESSAGE_FOR_ALL";
    public static final String AUTO_SUGGESTION_TYPE_MESSAGE = "KM_AUTO_SUGGESTION";
    public static final String KM_FIELD = "KM_FIELD";

    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_OPEN = "open";
    public static final String RICH_MESSAGE_CONTENT_TYPE = "300";
    private static final String AWS_ENCRYPTED = "AWS-ENCRYPTED-";
    private static final String LOCALIZATION_VALUE = "LOCALIZATION_VALUE";
    private static final HashSet<String> hiddenMetadataKeys = new HashSet<>(Arrays.asList(
            "KM_STATUS","KM_ASSIGN_TO","KM_ASSIGN_TEAM")); // in future if there are some more hidden keys, just add here
    public Message() {

    }

    public Message(String to, String body) {
        this.to = to;
        this.message = body;
    }

    public Message(Parcel in) {
        createdAtTime = in.readLong();
        to = in.readString();
        message = in.readString();
        key = in.readString();
        deviceKey = in.readString();
        userKey = in.readString();
        emailIds = in.readString();
        shared = in.readByte() != 0;
        sent = in.readByte() != 0;
        delivered = (Boolean) in.readValue(Boolean.class.getClassLoader());
        type = (short) in.readInt();
        storeOnDevice = in.readByte() != 0;
        contactIds = in.readString();
        groupId = (Integer) in.readValue(Integer.class.getClassLoader());
        groupStatus = (Short) in.readValue(Short.class.getClassLoader());
        sendToDevice = in.readByte() != 0;
        scheduledAt = (Long) in.readValue(Long.class.getClassLoader());
        source = (short) in.readInt();
        timeToLive = (Integer) in.readValue(Integer.class.getClassLoader());
        sentToServer = in.readByte() != 0;
        fileMetaKey = in.readString();
        filePaths = in.createStringArrayList();
        pairedMessageKey = in.readString();
        sentMessageTimeAtServer = in.readLong();
        canceled = in.readByte() != 0;
        clientGroupId = in.readString();
        fileMeta = in.readParcelable(FileMeta.class.getClassLoader());
        messageId = (Long) in.readValue(Long.class.getClassLoader());
        read = (Boolean) in.readValue(Boolean.class.getClassLoader());
        attDownloadInProgress = in.readByte() != 0;
        applicationId = in.readString();
        conversationId = (Integer) in.readValue(Integer.class.getClassLoader());
        topicId = in.readString();
        connected = in.readByte() != 0;
        contentType = (short) in.readInt();
        metadata = in.readHashMap(String.class.getClassLoader());
        status = (short) in.readInt();
        hidden = in.readByte() != 0;
        replyMessage = in.readInt();
        supportCustomerName = in.readString();
        groupAssignee = in.readString();
    }

    //copy constructor
    public Message(Message message) {
        //this.setKeyString(message.getKeyString());
        this.setMessage(message.getMessage());
        this.setContactIds(message.getContactIds());
        this.setCreatedAtTime(message.getCreatedAtTime());
        this.setDeviceKeyString(message.getDeviceKeyString());
        this.setSendToDevice(message.isSendToDevice());
        this.setTo(message.getTo());
        this.setType(message.getType());
        this.setSent(message.isSent());
        this.setDelivered(message.getDelivered());
        this.setStoreOnDevice(message.isStoreOnDevice());
        this.setScheduledAt(message.getScheduledAt());
        this.setSentToServer(message.isSentToServer());
        this.setSource(message.getSource());
        this.setTimeToLive(message.getTimeToLive());
        this.setFileMetas(message.getFileMetas());
        this.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
        this.setFilePaths(message.getFilePaths());
        this.setGroupId(message.getGroupId());
        this.setRead(message.isRead());
        this.setApplicationId(message.getApplicationId());
        this.setContentType(message.getContentType());
        this.setStatus(message.getStatus());
        this.setConversationId(message.getConversationId());
        this.setTopicId(message.getTopicId());
        this.setMetadata(message.getMetadata());
        this.setHidden(message.hasHideKey());
    }

    public long getSentMessageTimeAtServer() {
        return sentMessageTimeAtServer;
    }

    public void setSentMessageTimeAtServer(long sentMessageTimeAtServer) {
        this.sentMessageTimeAtServer = sentMessageTimeAtServer;
    }

    public boolean isAttDownloadInProgress() {
        return attDownloadInProgress;
    }

    public void setAttDownloadInProgress(boolean attDownloadInProgress) {
        this.attDownloadInProgress = attDownloadInProgress;
    }

    public Boolean isRead() {
        return read || isTypeOutbox() || getScheduledAt() != null;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public boolean isSelfDestruct() {
        return getTimeToLive() != null;
    }

    public boolean isUploadRequired() {
        return hasAttachment() && (fileMeta == null);
    }

    public boolean hasAttachment() {
        return ((filePaths != null && !filePaths.isEmpty()) || (fileMeta != null));
    }

    public boolean isAttachmentUploadInProgress() {
        return filePaths != null && !filePaths.isEmpty() && FileUtils.isFileExist(filePaths.get(0)) && !sentToServer;
    }

    public boolean isAttachmentDownloaded() {
        return filePaths != null && !filePaths.isEmpty() && FileUtils.isFileExist(filePaths.get(0));
    }

    public boolean isCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isOutgoingCall() {
        return MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isIncomingCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type);
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public boolean isDummyEmptyMessage() {
        return getCreatedAtTime() != null && getCreatedAtTime() == 0 && TextUtils.isEmpty(getMessage());
    }

    public boolean isLocalMessage() {
        return TextUtils.isEmpty(getKeyString()) && isSentToServer();
    }

    public String getKeyString() {
        return key;
    }

    public void setKeyString(String keyString) {
        this.key = keyString;
    }

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Boolean getDelivered() {
        return delivered != null ? delivered : false;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isStoreOnDevice() {
        return storeOnDevice;
    }

    public void setStoreOnDevice(boolean storeOnDevice) {
        this.storeOnDevice = storeOnDevice;
    }

    public String getDeviceKeyString() {
        return deviceKey;
    }

    public void setDeviceKeyString(String deviceKeyString) {
        this.deviceKey = deviceKeyString;
    }

    public String getSuUserKeyString() {
        return userKey;
    }

    public void setSuUserKeyString(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public void processContactIds(Context context) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (TextUtils.isEmpty(getContactIds())) {
            setContactIds(getTo());

        }
    }

    public String getContactIds() {
        return getTo();
    }

    public void setContactIds(String contactIds) {
        this.contactIds = contactIds;
    }

    public boolean isSendToDevice() {
        return sendToDevice;
    }

    public void setSendToDevice(boolean sendToDevice) {
        this.sendToDevice = sendToDevice;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public boolean isSentToMany() {
        return !TextUtils.isEmpty(getTo()) && getTo().split(",").length > 1;
    }

    public boolean isSentToServer() {
        return sentToServer;
    }

    public void setSentToServer(boolean sentToServer) {
        this.sentToServer = sentToServer;
    }

    public boolean isTypeOutbox() {
        return MessageType.OUTBOX.getValue().equals(type) || MessageType.MT_OUTBOX.getValue().equals(type) ||
                MessageType.OUTBOX_SENT_FROM_DEVICE.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isSentViaApp() {
        return MessageType.MT_OUTBOX.getValue().equals(this.type);
    }

    public boolean isSentViaCarrier() {
        return MessageType.OUTBOX.getValue().equals(type);
    }

    public Short getSource() {
        return source;
    }

    public void setSource(Short source) {
        this.source = source;
    }

    public Integer getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Integer timeToLive) {
        this.timeToLive = timeToLive;
    }

    public String getFileMetaKeyStrings() {
        return fileMetaKey;
    }

    public void setFileMetaKeyStrings(String fileMetaKeyStrings) {
        this.fileMetaKey = fileMetaKeyStrings;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getPairedMessageKeyString() {
        return pairedMessageKey;
    }

    public void setPairedMessageKeyString(String pairedMessageKeyString) {
        this.pairedMessageKey = pairedMessageKeyString;
    }

    public FileMeta getFileMetas() {
        return fileMeta;
    }

    public void setFileMetas(FileMeta fileMetas) {
        this.fileMeta = fileMetas;
    }

    public String getEmailIds() {
        return emailIds;
    }

    public void setEmailIds(String emailIds) {
        this.emailIds = emailIds;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public short getContentType() {
        return contentType;
    }

    public void setContentType(short contentType) {
        this.contentType = contentType;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getCurrentId() {
        return getGroupId() != null ? String.valueOf(getGroupId()) : getContactIds();
    }

    public boolean isTypeUrl() {
        return !TextUtils.isEmpty(getFirstUrl());
    }

    public String getFirstUrl() {
        Matcher matcher = Patterns.WEB_URL.matcher(getMessage());
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getSupportCustomerName() {
        return supportCustomerName;
    }

    public void setSupportCustomerName(String supportCustomerName) {
        this.supportCustomerName = supportCustomerName;
    }

    public String getGroupAssignee() {
        return groupAssignee;
    }

    public String getLocalizationValue(){
        return metadata != null ? metadata.get(LOCALIZATION_VALUE) : "";
    }

    public void setGroupAssignee(String groupAssignee) {
        this.groupAssignee = groupAssignee;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;

        if (message.isTempDateType() && isTempDateType()) {
            return DateUtils.getDate(message.getCreatedAtTime()).equals(DateUtils.getDate(getCreatedAtTime()));
        }

        if (getMessageId() != null && message.getMessageId() != null && getMessageId().equals(message.getMessageId())) {
            return true;
        }

        if (getKeyString() != null && message.getKeyString() != null) {
            return (getKeyString().equals(message.getKeyString()));
        }
        return false;
    }

    public String getAttachmentType() {
        String type = "no attachment";

        if (getContentType() == Message.ContentType.LOCATION.getValue()) {
            type = LOCATION;
        } else if (getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            type = AUDIO;
        } else if (getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            type = VIDEO;
        } else if (getContentType() == Message.ContentType.ATTACHMENT.getValue()) {
            if (getFilePaths() != null) {
                String filePath = getFilePaths().get(getFilePaths().size() - 1);
                String mimeType = FileUtils.getMimeType(filePath);

                if (mimeType != null) {
                    if (mimeType.startsWith(IMAGE)) {
                        type = IMAGE;
                    } else if (mimeType.startsWith(AUDIO)) {
                        type = AUDIO;
                    } else if (mimeType.startsWith(VIDEO)) {
                        type = VIDEO;
                    } else {
                        type = "others";
                    }
                }
            } else if (getFileMetas() != null) {
                if (getFileMetas().getContentType().contains(IMAGE)) {
                    type = IMAGE;
                } else if (getFileMetas().getContentType().contains(AUDIO)) {
                    type = AUDIO;
                } else if (getFileMetas().getContentType().contains(VIDEO)) {
                    type = VIDEO;
                } else {
                    type = "others";
                }
            }
        } else if (getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
            type = CONTACT;
        } else if (hasAttachment()) {
            type = OTHER;
        }
        return type;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        if (isTempDateType()) {
            result = 31 * result + DateUtils.getDate(getCreatedAtTime()).hashCode();
        }
        return result;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public boolean isTempDateType() {
        return type.equals(MessageType.DATE_TEMP.value);
    }

    public boolean isInitialFirstMessage() {
        return type.equals(MessageType.INITIAL_FIRST_MESSAGE.value);
    }
    public void setInitialFirstMessage() {
        this.type = MessageType.INITIAL_FIRST_MESSAGE.value;
    }
    public boolean isTypingMessage() {
        return type != null && type.equals(MessageType.TYPING_MESSAGE.value);
    }
    public void setTypingMessage() {
        this.type = MessageType.TYPING_MESSAGE.value;
    }
    public void setTempDateType(short tempDateType) {
        this.type = tempDateType;
    }

    public boolean isCustom() {
        return contentType == ContentType.CUSTOM.value;
    }

    public boolean isChannelCustomMessage() {
        return contentType == ContentType.CHANNEL_CUSTOM_MESSAGE.getValue();
    }

    public boolean isDeliveredAndRead() {
        return Message.Status.DELIVERED_AND_READ.getValue().equals(getStatus());
    }

    public boolean isReadStatus() {
        return Status.READ.getValue() == getStatus();
    }

    public boolean isReadStatusForUpdate() {
        return Status.READ.getValue() == getStatus() || isTypeOutbox();
    }

    public boolean isContactMessage() {
        return ContentType.CONTACT_MSG.getValue().equals(getContentType());
    }

    public boolean isLocationMessage() {
        return ContentType.LOCATION.getValue().equals(getContentType());
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public boolean isGroupMessage() {
        return (this.groupId != null);
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public String getMetaDataValueForKey(String key) {
        return getMetadata() != null ? getMetadata().get(key) : null;
    }

    public String getAssigneId() {
        if (isActionMessage()) {
            return !TextUtils.isEmpty(getMetadata().get(BOT_ASSIGN)) ? getMetadata().get(BOT_ASSIGN) : !TextUtils.isEmpty(getMetadata().get(KM_ASSIGN_TO)) ?
                    getMetadata().get(KM_ASSIGN_TO) : "";
        }
        return null;
    }

    public boolean isGroupDeleteAction() {
        return getMetadata() != null && getMetadata().containsKey(ChannelMetadata.AL_CHANNEL_ACTION)
                && Integer.parseInt(getMetadata().get(ChannelMetadata.AL_CHANNEL_ACTION)) == GroupAction.DELETE_GROUP.getValue();
    }

    public boolean isUpdateMessage() {
        return !Message.ContentType.HIDDEN.getValue().equals(contentType)
                && (!Message.MetaDataType.ARCHIVE.getValue().equals(getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || !isHidden())
                && !isVideoNotificationMessage();

    }

    public boolean isVideoNotificationMessage() {
        return ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue().equals(getContentType());
    }

    public boolean isVideoCallMessage() {
        return ContentType.VIDEO_CALL_STATUS_MSG.getValue().equals(getContentType());
    }

    public boolean isVideoOrAudioCallMessage() {
        String msgType = getMetaDataValueForKey(VideoCallNotificationHelper.MSG_TYPE);
        return (VideoCallNotificationHelper.CALL_STARTED.equals(msgType)
                || VideoCallNotificationHelper.CALL_REJECTED.equals(msgType)
                || VideoCallNotificationHelper.CALL_CANCELED.equals(msgType)
                || VideoCallNotificationHelper.CALL_ANSWERED.equals(msgType)
                || VideoCallNotificationHelper.CALL_END.equals(msgType)
                || VideoCallNotificationHelper.CALL_DIALED.equals(msgType)
                || VideoCallNotificationHelper.CALL_ANSWERED.equals(msgType)
                || VideoCallNotificationHelper.CALL_MISSED.equals(msgType));
    }

    public boolean isConsideredForCount() {
        return (!Message.ContentType.HIDDEN.getValue().equals(getContentType()) &&
                !ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue().equals(getContentType()) && !isReadStatus() && !hasHideKey() && !isDeletedForAll());
    }

    public boolean hasHideKey() {
        int loggedInUserRole = MobiComUserPreference.getInstance(getAppContext()).getUserRoleType();
        return GroupMessageMetaData.TRUE.getValue().equals(getMetaDataValueForKey(GroupMessageMetaData.HIDE_KEY.getValue())) || Message.ContentType.HIDDEN.getValue().equals(getContentType()) || hidden || (loggedInUserRole != User.RoleType.AGENT.getValue() && ( Message.MetaDataType.HIDDEN.getValue().equals(getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || containsHiddenKeys()));
    }

    private boolean containsHiddenKeys() {
        if (metadata == null){
            return false;
        }
        for (String key : hiddenMetadataKeys){
            if (metadata.containsKey(key)){
                return true;
            }
        }
        return false;
    }

    public boolean isGroupMetaDataUpdated() {
        return ContentType.CHANNEL_CUSTOM_MESSAGE.getValue().equals(this.getContentType()) && this.getMetadata() != null && this.getMetadata().containsKey("action") && GroupAction.GROUP_META_DATA_UPDATED.getValue().toString().equals(this.getMetadata().get("action"));
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public int isReplyMessage() {
        return replyMessage;
    }

    public void setReplyMessage(int replyMessage) {
        this.replyMessage = replyMessage;
    }


    public Short getGroupStatus() {

        return groupStatus;
    }

    public void setGroupStatus(Short groupStatus) {
        this.groupStatus = groupStatus;
    }


    public boolean isActionMessage() {
        boolean isAgent = MobiComUserPreference.getInstance(getAppContext()).getUserRoleType().equals(User.RoleType.AGENT.getValue());
        return getMetadata() != null && ((isAgent && getMetadata().containsKey(BOT_ASSIGN)) || getMetadata().containsKey(KM_ASSIGN_TO) || getMetadata().containsKey(KM_ASSIGN_TEAM) || getMetadata().containsKey(CONVERSATION_STATUS) || getMetadata().containsKey(AL_DELETE_MESSAGE_FOR_ALL_KEY));
    }

    public boolean isFeedbackMessage() {
        return getMetadata() != null && getMetadata().containsKey(FEEDBACK_METADATA_KEY);
    }

    public boolean isAutoSuggestion() {
        return getMetadata() != null && getMetadata().containsKey(AUTO_SUGGESTION_TYPE_MESSAGE);
    }

    public boolean isTypeResolved() {
        return getMetadata() != null && STATUS_CLOSED.equalsIgnoreCase(getMetadata().get(CONVERSATION_STATUS));
    }

    public boolean isTypeOpen() {
        return getMetadata() != null && STATUS_OPEN.equalsIgnoreCase(getMetadata().get(CONVERSATION_STATUS));
    }

    public String getConversationStatus() {
        return (getMetadata() != null && getMetadata().containsKey(CONVERSATION_STATUS)) ? getMetadata().get(CONVERSATION_STATUS) : null;
    }

    public String getConversationAssignee() {
        return (getMetadata() != null && getMetadata().containsKey(BOT_ASSIGN)) ? getMetadata().get(BOT_ASSIGN) : null;
    }

    public boolean isDeletedForAll() {
        return getMetadata() != null
                && getMetadata().containsKey(AL_DELETE_MESSAGE_FOR_ALL_KEY)
                && GroupMessageMetaData.TRUE.getValue().equals(getMetadata().get(AL_DELETE_MESSAGE_FOR_ALL_KEY));
    }

    public void setAsDeletedForAll() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        metadata.put(AL_DELETE_MESSAGE_FOR_ALL_KEY, GroupMessageMetaData.TRUE.getValue());
    }

    public boolean isIgnoreMessageAdding(Context context) {
        if (SettingsSharedPreference.getInstance(context).isSubGroupEnabled() && MobiComUserPreference.getInstance(context).getParentGroupKey() != null || !TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getCategoryName())) {
            Channel channel = ChannelService.getInstance(context).getChannelByChannelKey(getGroupId());
            boolean subGroupFlag = channel != null && channel.getParentKey() != null && MobiComUserPreference.getInstance(context).getParentGroupKey().equals(channel.getParentKey());
            boolean categoryFlag = channel != null && channel.isPartOfCategory(MobiComUserPreference.getInstance(context).getCategoryName());
            return (subGroupFlag || categoryFlag || SettingsSharedPreference.getInstance(context).isSubGroupEnabled() || !TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getCategoryName()));
        }
        return ((SettingsSharedPreference.getInstance(context).isActionMessagesHidden() && isActionMessage()) || hasHideKey());
    }

    public boolean isRichMessage() {
        return metadata != null && RICH_MESSAGE_CONTENT_TYPE.equals(metadata.get("contentType"));
    }

    public boolean isCustomInputField() {
        return metadata != null && metadata.containsKey(KM_FIELD);
    }

    public boolean isAttachmentEncrypted() {
        return fileMeta != null && !TextUtils.isEmpty(fileMeta.getName()) && fileMeta.getName().startsWith(AWS_ENCRYPTED);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(createdAtTime);
        dest.writeString(to);
        dest.writeString(message);
        dest.writeString(key);
        dest.writeString(deviceKey);
        dest.writeString(userKey);
        dest.writeString(emailIds);
        dest.writeByte((byte) (shared ? 1 : 0));
        dest.writeByte((byte) (sent ? 1 : 0));
        dest.writeValue(delivered);
        dest.writeInt(type);
        dest.writeByte((byte) (storeOnDevice ? 1 : 0));
        dest.writeString(contactIds);
        dest.writeValue(groupId);
        dest.writeValue(groupStatus);
        dest.writeByte((byte) (sendToDevice ? 1 : 0));
        dest.writeValue(scheduledAt);
        dest.writeInt(source);
        dest.writeValue(timeToLive);
        dest.writeByte((byte) (sentToServer ? 1 : 0));
        dest.writeString(fileMetaKey);
        dest.writeStringList(filePaths);
        dest.writeString(pairedMessageKey);
        dest.writeLong(sentMessageTimeAtServer);
        dest.writeByte((byte) (canceled ? 1 : 0));
        dest.writeString(clientGroupId);
        dest.writeParcelable(fileMeta, flags);
        dest.writeValue(messageId);
        dest.writeValue(read);
        dest.writeByte((byte) (attDownloadInProgress ? 1 : 0));
        dest.writeString(applicationId);
        dest.writeValue(conversationId);
        dest.writeString(topicId);
        dest.writeByte((byte) (connected ? 1 : 0));
        dest.writeInt(contentType);
        dest.writeMap(metadata);
        dest.writeInt(status);
        dest.writeByte((byte) (hidden ? 1 : 0));
        dest.writeInt(replyMessage);
        dest.writeString(supportCustomerName);
        dest.writeString(groupAssignee);
    }

    @Override
    public int describeContents() {
        if (fileMeta != null) {
            return fileMeta.describeContents(); // Will return the value defined in the FileMeta class
        }
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public enum GroupStatus {
        INITIAL(Short.valueOf("-1")), OPEN(Short.valueOf("0")), PROGRESS(Short.valueOf("1")),
        RESOLVED(Short.valueOf("2")), CLOSED(Short.valueOf("2")),
        SPAM(Short.valueOf("3")), DUPLICATE(Short.valueOf("4")), ARCHIVE(Short.valueOf("5")),
        UNRESPONDED(Short.valueOf("6")), WAITING(Short.valueOf("7"));
        private Short value;

        GroupStatus(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum Source {

        DEVICE_NATIVE_APP(Short.valueOf("0")), WEB(Short.valueOf("1")), MT_MOBILE_APP(Short.valueOf("2")), API(Short.valueOf("3"));
        private Short value;

        Source(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum MessageType {

        INBOX(Short.valueOf("0")), OUTBOX(Short.valueOf("1")), DRAFT(Short.valueOf("2")),
        OUTBOX_SENT_FROM_DEVICE(Short.valueOf("3")), MT_INBOX(Short.valueOf("4")),
        MT_OUTBOX(Short.valueOf("5")), CALL_INCOMING(Short.valueOf("6")), CALL_OUTGOING(Short.valueOf("7")),
        DATE_TEMP(Short.valueOf("100")), INITIAL_FIRST_MESSAGE(Short.valueOf("101")), TYPING_MESSAGE(Short.valueOf("102"));
        private Short value;

        MessageType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum ContentType {

        DEFAULT(Short.valueOf("0")), ATTACHMENT(Short.valueOf("1")), LOCATION(Short.valueOf("2")),
        TEXT_HTML(Short.valueOf("3")), PRICE(Short.valueOf("4")), TEXT_URL(Short.valueOf("5")), CONTACT_MSG(Short.valueOf("7")), AUDIO_MSG(Short.valueOf("8")), VIDEO_MSG(Short.valueOf("9")), CHANNEL_CUSTOM_MESSAGE(Short.valueOf("10")), CUSTOM(Short.valueOf("101")), HIDDEN(Short.valueOf("11")), BLOCK_NOTIFICATION_IN_GROUP(Short.valueOf("13")), VIDEO_CALL_NOTIFICATION_MSG(Short.valueOf("102")),
        VIDEO_CALL_STATUS_MSG(Short.valueOf("103"));
        private Short value;

        ContentType(Short value) {
            this.value = value;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum Status {

        UNREAD(Short.valueOf("0")), READ(Short.valueOf("1")), PENDING(Short.valueOf("2")),
        SENT(Short.valueOf("3")), DELIVERED(Short.valueOf("4")), DELIVERED_AND_READ(Short.valueOf("5"));
        private Short value;

        Status(Short value) {
            this.value = value;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum MetaDataType {
        KEY("category"),
        HIDDEN("HIDDEN"),
        PUSHNOTIFICATION("PUSHNOTIFICATION"),
        ARCHIVE("ARCHIVE"), AL_REPLY("AL_REPLY");
        private String value;

        MetaDataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum GroupMessageMetaData {
        KEY("show"),
        HIDE_KEY("hide"),
        FALSE("false"),
        TRUE("true");
        private String value;

        GroupMessageMetaData(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ReplyMessage {
        NON_HIDDEN(0),
        REPLY_MESSAGE(1),
        HIDE_MESSAGE(2);
        private Integer value;

        ReplyMessage(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    public enum GroupAction {
        CREATE(0),
        ADD_MEMBER(1),
        REMOVE_MEMBER(2),
        LEFT(3),
        DELETE_GROUP(4),
        CHANGE_GROUP_NAME(5),
        CHANGE_IMAGE_URL(6),
        JOIN(7),
        GROUP_USER_ROLE_UPDATED(8),
        GROUP_META_DATA_UPDATED(9);
        private Integer value;

        GroupAction(Integer value) {
            this.value = value;
        }

        public Short getValue() {
            return value.shortValue();
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "createdAtTime=" + createdAtTime +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                ", key='" + key + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", userKey='" + userKey + '\'' +
                ", emailIds='" + emailIds + '\'' +
                ", shared=" + shared +
                ", sent=" + sent +
                ", delivered=" + delivered +
                ", type=" + type +
                ", storeOnDevice=" + storeOnDevice +
                ", contactIds='" + contactIds + '\'' +
                ", groupId=" + groupId +
                ", sendToDevice=" + sendToDevice +
                ", scheduledAt=" + scheduledAt +
                ", source=" + source +
                ", timeToLive=" + timeToLive +
                ", sentToServer=" + sentToServer +
                ", fileMetaKey='" + fileMetaKey + '\'' +
                ", filePaths=" + filePaths +
                ", pairedMessageKey='" + pairedMessageKey + '\'' +
                ", sentMessageTimeAtServer=" + sentMessageTimeAtServer +
                ", canceled=" + canceled +
                ", clientGroupId='" + clientGroupId + '\'' +
                ", fileMeta=" + fileMeta +
                ", messageId=" + messageId +
                ", read=" + read +
                ", attDownloadInProgress=" + attDownloadInProgress +
                ", applicationId='" + applicationId + '\'' +
                ", conversationId=" + conversationId +
                ", topicId='" + topicId + '\'' +
                ", connected=" + connected +
                ", contentType=" + contentType +
                ", metadata=" + metadata +
                ", status=" + status +
                ", hidden=" + hidden +
                ", replyMessage=" + replyMessage +
                ", groupAssignee=" + groupAssignee +
                '}';
    }
}
