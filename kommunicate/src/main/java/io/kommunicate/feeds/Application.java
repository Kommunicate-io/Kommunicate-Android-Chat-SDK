
package io.kommunicate.feeds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Application {

    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("applicationId")
    @Expose
    private String applicationId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("adminUser")
    @Expose
    private AdminUser adminUser;
    @SerializedName("pricingPackage")
    @Expose
    private Integer pricingPackage;
    @SerializedName("createdAtTime")
    @Expose
    private Long createdAtTime;
    @SerializedName("secretKey")
    @Expose
    private String secretKey;
    @SerializedName("websiteUrl")
    @Expose
    private String websiteUrl;
    @SerializedName("expectedReleaseDateTime")
    @Expose
    private Long expectedReleaseDateTime;
    @SerializedName("validUptoTime")
    @Expose
    private Long validUptoTime;
    @SerializedName("defaultModuleName")
    @Expose
    private String defaultModuleName;
    @SerializedName("defaultModuleId")
    @Expose
    private Integer defaultModuleId;
    @SerializedName("monthlySmsSent")
    @Expose
    private Integer monthlySmsSent;
    @SerializedName("monthlyEmailSent")
    @Expose
    private Integer monthlyEmailSent;
    @SerializedName("totalSmsSent")
    @Expose
    private Integer totalSmsSent;
    @SerializedName("totalEmailSent")
    @Expose
    private Integer totalEmailSent;
    @SerializedName("smsCredits")
    @Expose
    private Integer smsCredits;
    @SerializedName("encryptionEnabled")
    @Expose
    private Boolean encryptionEnabled;
    @SerializedName("expectedMAU")
    @Expose
    private Integer expectedMAU;
    @SerializedName("broadcastGroupKey")
    @Expose
    private Integer broadcastGroupKey;
    @SerializedName("groupUserLimit")
    @Expose
    private Integer groupUserLimit;
    @SerializedName("appModulePxys")
    @Expose
    private List<AppModulePxy> appModulePxys = null;
    @SerializedName("applicationWebhookPxys")
    @Expose
    private List<ApplicationWebhookPxy> applicationWebhookPxys = null;
    @SerializedName("videoCallMinutes")
    @Expose
    private Integer videoCallMinutes;
    @SerializedName("interestedIncareProgram")
    @Expose
    private Boolean interestedIncareProgram;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AdminUser getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    public Integer getPricingPackage() {
        return pricingPackage;
    }

    public void setPricingPackage(Integer pricingPackage) {
        this.pricingPackage = pricingPackage;
    }

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public Long getExpectedReleaseDateTime() {
        return expectedReleaseDateTime;
    }

    public void setExpectedReleaseDateTime(Long expectedReleaseDateTime) {
        this.expectedReleaseDateTime = expectedReleaseDateTime;
    }

    public Long getValidUptoTime() {
        return validUptoTime;
    }

    public void setValidUptoTime(Long validUptoTime) {
        this.validUptoTime = validUptoTime;
    }

    public String getDefaultModuleName() {
        return defaultModuleName;
    }

    public void setDefaultModuleName(String defaultModuleName) {
        this.defaultModuleName = defaultModuleName;
    }

    public Integer getDefaultModuleId() {
        return defaultModuleId;
    }

    public void setDefaultModuleId(Integer defaultModuleId) {
        this.defaultModuleId = defaultModuleId;
    }

    public Integer getMonthlySmsSent() {
        return monthlySmsSent;
    }

    public void setMonthlySmsSent(Integer monthlySmsSent) {
        this.monthlySmsSent = monthlySmsSent;
    }

    public Integer getMonthlyEmailSent() {
        return monthlyEmailSent;
    }

    public void setMonthlyEmailSent(Integer monthlyEmailSent) {
        this.monthlyEmailSent = monthlyEmailSent;
    }

    public Integer getTotalSmsSent() {
        return totalSmsSent;
    }

    public void setTotalSmsSent(Integer totalSmsSent) {
        this.totalSmsSent = totalSmsSent;
    }

    public Integer getTotalEmailSent() {
        return totalEmailSent;
    }

    public void setTotalEmailSent(Integer totalEmailSent) {
        this.totalEmailSent = totalEmailSent;
    }

    public Integer getSmsCredits() {
        return smsCredits;
    }

    public void setSmsCredits(Integer smsCredits) {
        this.smsCredits = smsCredits;
    }

    public Boolean getEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionEnabled(Boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }

    public Integer getExpectedMAU() {
        return expectedMAU;
    }

    public void setExpectedMAU(Integer expectedMAU) {
        this.expectedMAU = expectedMAU;
    }

    public Integer getBroadcastGroupKey() {
        return broadcastGroupKey;
    }

    public void setBroadcastGroupKey(Integer broadcastGroupKey) {
        this.broadcastGroupKey = broadcastGroupKey;
    }

    public Integer getGroupUserLimit() {
        return groupUserLimit;
    }

    public void setGroupUserLimit(Integer groupUserLimit) {
        this.groupUserLimit = groupUserLimit;
    }

    public List<AppModulePxy> getAppModulePxys() {
        return appModulePxys;
    }

    public void setAppModulePxys(List<AppModulePxy> appModulePxys) {
        this.appModulePxys = appModulePxys;
    }

    public List<ApplicationWebhookPxy> getApplicationWebhookPxys() {
        return applicationWebhookPxys;
    }

    public void setApplicationWebhookPxys(List<ApplicationWebhookPxy> applicationWebhookPxys) {
        this.applicationWebhookPxys = applicationWebhookPxys;
    }

    public Integer getVideoCallMinutes() {
        return videoCallMinutes;
    }

    public void setVideoCallMinutes(Integer videoCallMinutes) {
        this.videoCallMinutes = videoCallMinutes;
    }

    public Boolean getInterestedIncareProgram() {
        return interestedIncareProgram;
    }

    public void setInterestedIncareProgram(Boolean interestedIncareProgram) {
        this.interestedIncareProgram = interestedIncareProgram;
    }

}
