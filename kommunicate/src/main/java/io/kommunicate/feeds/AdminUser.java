
package io.kommunicate.feeds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdminUser {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("emailVerified")
    @Expose
    private Boolean emailVerified;
    @SerializedName("appVersionCode")
    @Expose
    private Integer appVersionCode;
    @SerializedName("deviceType")
    @Expose
    private Integer deviceType;
    @SerializedName("notificationMode")
    @Expose
    private Integer notificationMode;
    @SerializedName("unreadCountType")
    @Expose
    private Integer unreadCountType;
    @SerializedName("displayName")
    @Expose
    private String displayName;
    @SerializedName("state")
    @Expose
    private Integer state;
    @SerializedName("totalUnreadCount")
    @Expose
    private Integer totalUnreadCount;
    @SerializedName("resetUserStatus")
    @Expose
    private Boolean resetUserStatus;
    @SerializedName("chatNotificationMailSent")
    @Expose
    private Boolean chatNotificationMailSent;
    @SerializedName("enableEncryption")
    @Expose
    private Boolean enableEncryption;
    @SerializedName("metadata")
    @Expose
    private Metadata_ metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Integer getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(Integer appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getNotificationMode() {
        return notificationMode;
    }

    public void setNotificationMode(Integer notificationMode) {
        this.notificationMode = notificationMode;
    }

    public Integer getUnreadCountType() {
        return unreadCountType;
    }

    public void setUnreadCountType(Integer unreadCountType) {
        this.unreadCountType = unreadCountType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(Integer totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    public Boolean getResetUserStatus() {
        return resetUserStatus;
    }

    public void setResetUserStatus(Boolean resetUserStatus) {
        this.resetUserStatus = resetUserStatus;
    }

    public Boolean getChatNotificationMailSent() {
        return chatNotificationMailSent;
    }

    public void setChatNotificationMailSent(Boolean chatNotificationMailSent) {
        this.chatNotificationMailSent = chatNotificationMailSent;
    }

    public Boolean getEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(Boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    public Metadata_ getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata_ metadata) {
        this.metadata = metadata;
    }

}
