package com.applozic.mobicomkit.api.account.user;

import android.text.TextUtils;

import com.applozic.mobicommons.json.JsonMarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by devashish on 22/12/14.
 */
public class User extends JsonMarker {

    private static final String DEFAULT_USER_ID_REGEX = "^[a-zA-Z0-9_+#@.?|=;-]+$";
    private String userIdRegex;
    private String userId;
    private String email;
    private String password;
    private String registrationId;
    private String applicationId;
    private String contactNumber;
    private String countryCode;
    private Short prefContactAPI = Short.valueOf("2");
    private boolean emailVerified = true;
    private String timezone;
    private Short appVersionCode;
    private String roleName = "USER";
    private Short deviceType;
    private String imageLink;
    private boolean enableEncryption;
    private Short pushNotificationFormat;
    private Short authenticationTypeId = AuthenticationType.CLIENT.getValue();
    private String displayName;
    private String appModuleName;
    private Short userTypeId;
    private List<String> features;
    private String notificationSoundFilePath;
    private Long lastMessageAtTime;
    private Map<String, String> metadata;
    private String alBaseUrl;
    private String kmBaseUrl;
    private String status;
    private String localImageUri;
    private boolean skipDeletedGroups;
    private boolean hideActionMessages;
    private Short roleType = RoleType.USER_ROLE.getValue();

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocalImageUri() {
        return localImageUri;
    }

    public void setLocalImageUri(String localImageUri) {
        this.localImageUri = localImageUri;
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

    public void setEmail(String emailId) {
        this.email = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Short getPrefContactAPI() {
        return prefContactAPI;
    }

    public void setPrefContactAPI(Short prefContactAPI) {
        this.prefContactAPI = prefContactAPI;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Short getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(Short appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Short getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Short deviceType) {
        this.deviceType = deviceType;
    }

    public Short getAuthenticationTypeId() {
        return authenticationTypeId;
    }

    public void setAuthenticationTypeId(Short authenticationTypeId) {
        this.authenticationTypeId = authenticationTypeId;
    }

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    public Short getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getNotificationSoundFilePath() {
        return notificationSoundFilePath;
    }

    public void setNotificationSoundFilePath(String notificationSoundFilePath) {
        this.notificationSoundFilePath = notificationSoundFilePath;
    }

    public Short getPushNotificationFormat() {
        return pushNotificationFormat;
    }

    public void setPushNotificationFormat(Short pushNotificationFormat) {
        this.pushNotificationFormat = pushNotificationFormat;
    }

    public Long getLastMessageAtTime() {
        return lastMessageAtTime;
    }

    public void setLastMessageAtTime(Long lastMessageAtTime) {
        this.lastMessageAtTime = lastMessageAtTime;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setRoleType(Short roleType) {
        this.roleType = roleType;
    }

    public Short getRoleType() {
        return roleType;
    }

    public String getAlBaseUrl() {
        return alBaseUrl;
    }

    public void setAlBaseUrl(String alBaseUrl) {
        this.alBaseUrl = alBaseUrl;
    }

    public String getKmBaseUrl() {
        return kmBaseUrl;
    }

    public void setKmBaseUrl(String kmBaseUrl) {
        this.kmBaseUrl = kmBaseUrl;
    }

    public boolean isSkipDeletedGroups() {
        return skipDeletedGroups;
    }

    public void setSkipDeletedGroups(boolean skipDeletedGroups) {
        this.skipDeletedGroups = skipDeletedGroups;
    }

    public boolean isHideActionMessages() {
        return hideActionMessages;
    }

    public void setHideActionMessages(boolean hideActionMessages) {
        this.hideActionMessages = hideActionMessages;
    }

    public String getUserIdRegex() {
        return userIdRegex;
    }

    public void setUserIdRegex(String regex) {
        this.userIdRegex = regex;
    }

    public boolean isValidUserId() {
        if (TextUtils.isEmpty(userIdRegex)) {
            setUserIdRegex(DEFAULT_USER_ID_REGEX);
        }
        return Pattern.compile(userIdRegex).matcher(getUserId()).matches();
    }

    public static String getEncodedUserId(String userId) {
        if (!TextUtils.isEmpty(userId) && (userId.contains("+") || userId.contains("#"))) {
            try {
                return URLEncoder.encode(userId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return userId;
    }

    public static String getDecodedUserId(String encodedId) {
        if (!TextUtils.isEmpty(encodedId)) {
            try {
                return URLDecoder.decode(encodedId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return encodedId;
    }

    public enum AuthenticationType {

        CLIENT(Short.valueOf("0")), APPLOZIC(Short.valueOf("1")), FACEBOOK(Short.valueOf("2"));
        private Short value;

        AuthenticationType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum Features {

        IP_AUDIO_CALL("100"), IP_VIDEO_CALL("101");
        private String value;

        Features(String c) {
            value = c;
        }

        public String getValue() {
            return value;
        }
    }

    public enum RoleType {
        BOT(Short.valueOf("1")),
        APPLICATION_ADMIN(Short.valueOf("2")),
        USER_ROLE(Short.valueOf("3")),
        ADMIN_ROLE(Short.valueOf("4")),
        BUSINESS(Short.valueOf("5")),
        APPLICATION_BROADCASTER(Short.valueOf("6")),
        SUPPORT(Short.valueOf("7")),
        AGENT(Short.valueOf("8"));

        private Short value;

        RoleType(Short r) {
            value = r;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum PushNotificationFormat {
        NATIVE(Short.valueOf("0")),
        PHONEGAP(Short.valueOf("1")),
        IONIC(Short.valueOf("2")),
        NATIVESCRIPT(Short.valueOf("3")),
        PUSHY_ME(Short.valueOf("4"));

        private Short value;

        PushNotificationFormat(Short p) {
            value = p;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum RoleName {
        BOT("BOT"),
        APPLICATION_ADMIN("APPLICATION_ADMIN"),
        USER("USER"),
        ADMIN("ADMIN"),
        BUSINESS("BUSINESS"),
        APPLICATION_BROADCASTER("APPLICATION_BROADCASTER"),
        SUPPORT("SUPPORT"),
        APPLICATION_WEB_ADMIN("APPLICATION_WEB_ADMIN");

        private String value;

        RoleName(String r) {
            value = r;
        }

        public String getValue() {
            return value;
        }
    }
}
