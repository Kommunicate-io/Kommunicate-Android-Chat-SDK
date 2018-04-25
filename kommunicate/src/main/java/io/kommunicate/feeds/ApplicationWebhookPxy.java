
package io.kommunicate.feeds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApplicationWebhookPxy {

    @SerializedName("key")
    @Expose
    private Integer key;
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("applicationId")
    @Expose
    private String applicationId;
    @SerializedName("notifyVia")
    @Expose
    private Long notifyVia;
    @SerializedName("fallbackTime")
    @Expose
    private Long fallbackTime;
    @SerializedName("createdAt")
    @Expose
    private Long createdAt;
    @SerializedName("updatedAt")
    @Expose
    private Long updatedAt;

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Long getNotifyVia() {
        return notifyVia;
    }

    public void setNotifyVia(Long notifyVia) {
        this.notifyVia = notifyVia;
    }

    public Long getFallbackTime() {
        return fallbackTime;
    }

    public void setFallbackTime(Long fallbackTime) {
        this.fallbackTime = fallbackTime;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

}
