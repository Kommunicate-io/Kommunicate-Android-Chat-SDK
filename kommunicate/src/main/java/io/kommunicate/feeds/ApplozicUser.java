
package io.kommunicate.feeds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApplozicUser {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("userKey")
    @Expose
    private String userKey;
    @SerializedName("deviceKey")
    @Expose
    private String deviceKey;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("displayName")
    @Expose
    private String displayName;
    @SerializedName("imageLink")
    @Expose
    private String imageLink;
    @SerializedName("lastSyncTime")
    @Expose
    private Integer lastSyncTime;
    @SerializedName("currentTimeStamp")
    @Expose
    private Integer currentTimeStamp;
    @SerializedName("deactivate")
    @Expose
    private Boolean deactivate;
    @SerializedName("brokerUrl")
    @Expose
    private String brokerUrl;
    @SerializedName("pricingPackage")
    @Expose
    private Integer pricingPackage;
    @SerializedName("totalUnreadCount")
    @Expose
    private Integer totalUnreadCount;
    @SerializedName("roleType")
    @Expose
    private Integer roleType;
    @SerializedName("metadata")
    @Expose
    private Metadata metadata;
    @SerializedName("newUser")
    @Expose
    private Boolean newUser;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public Integer getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Integer lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Integer getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public void setCurrentTimeStamp(Integer currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }

    public Boolean getDeactivate() {
        return deactivate;
    }

    public void setDeactivate(Boolean deactivate) {
        this.deactivate = deactivate;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public Integer getPricingPackage() {
        return pricingPackage;
    }

    public void setPricingPackage(Integer pricingPackage) {
        this.pricingPackage = pricingPackage;
    }

    public Integer getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(Integer totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }

}
