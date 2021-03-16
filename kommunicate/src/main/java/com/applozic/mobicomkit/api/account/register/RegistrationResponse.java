package com.applozic.mobicomkit.api.account.register;

import android.text.TextUtils;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.Map;

/**
 * @author devashish
 */
public class RegistrationResponse extends JsonMarker {

    private String message;
    private String deviceKey;
    private String userKey;
    private String userId;
    private String contactNumber;
    private Long lastSyncTime;
    private Long currentTimeStamp;
    private String displayName;
    private String notificationResponse;
    private String brokerUrl;
    private String imageLink;
    private String statusMessage;
    private String encryptionKey;
    private String userEncryptionKey;
    private boolean enableEncryption;
    private Map<String, String> metadata;
    private Short roleType;
    private String authToken;
    private Short pricingPackage = PricingType.STARTER.getValue();
    private Long notificationAfter;
    private boolean deactivate;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKeyString) {
        this.deviceKey = deviceKeyString;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Long getLastSyncTime() {
        return lastSyncTime == null ? 0L : lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Long getCurrentTimeStamp() {
        return currentTimeStamp == null ? 0L : currentTimeStamp;
    }

    public void setCurrentTimeStamp(Long currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }

    public String getNotificationResponse() {
        return notificationResponse;
    }

    public void setNotificationResponse(String notificationResponse) {
        this.notificationResponse = notificationResponse;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public boolean isPasswordInvalid() {
        return (!TextUtils.isEmpty(message) && ("PASSWORD_INVALID".equals(message) || "PASSWORD_REQUIRED".equals(message)));
    }

    public Short getPricingPackage() {
        return pricingPackage;
    }

    public void setPricingPackage(Short pricingPackage) {
        this.pricingPackage = pricingPackage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    public Short getRoleType() {
        return roleType;
    }

    public void setRoleType(Short roleType) {
        this.roleType = roleType;
    }

    public String getUserEncryptionKey() {
        return userEncryptionKey;
    }

    public void setUserEncryptionKey(String userEncryptionKey) {
        this.userEncryptionKey = userEncryptionKey;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Long getNotificationAfter() {
        return notificationAfter;
    }

    public void setNotificationAfter(Long notificationAfter) {
        this.notificationAfter = notificationAfter;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isDeactivate() {
        return deactivate;
    }

    public void setDeactivate(boolean deactivate) {
        this.deactivate = deactivate;
    }

    public static enum PricingType {

        CLOSED(Short.valueOf("-1")), BETA(Short.valueOf("0")), STARTER(Short.valueOf("1")), LAUNCH(Short.valueOf("2")), GROWTH(Short.valueOf("3")), ENTERPRISE(
                Short.valueOf("4")), UNSUBSCRIBED(Short.valueOf("6"));
        private final Short value;

        private PricingType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public boolean isRegistrationSuccess() {
        return (!TextUtils.isEmpty(message) && (SuccessResponse.UPDATED.getValue().equals(message) || SuccessResponse.REGISTERED.getValue().equals(message) || SuccessResponse.REGISTERED_WITHOUTREGISTRATIONID.getValue().equals(message)));
    }

    public static enum SuccessResponse {
        UPDATED("UPDATED"), REGISTERED("REGISTERED"), REGISTERED_WITHOUTREGISTRATIONID("REGISTERED.WITHOUTREGISTRATIONID");
        private final String value;

        private SuccessResponse(String c) {
            value = c;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "message='" + message + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", userKey='" + userKey + '\'' +
                ", userId='" + userId + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", lastSyncTime=" + lastSyncTime +
                ", currentTimeStamp=" + currentTimeStamp +
                ", displayName='" + displayName + '\'' +
                ", notificationResponse='" + notificationResponse + '\'' +
                ", brokerUrl='" + brokerUrl + '\'' +
                ", imageLink='" + imageLink + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", encryptionKey='" + encryptionKey + '\'' +
                ", userEncryptionKey='" + userEncryptionKey + '\'' +
                ", enableEncryption=" + enableEncryption +
                ", metadata=" + metadata +
                ", roleType=" + roleType +
                ", authToken='" + authToken + '\'' +
                ", pricingPackage=" + pricingPackage +
                ", notificationAfter=" + notificationAfter +
                '}';
    }
}
