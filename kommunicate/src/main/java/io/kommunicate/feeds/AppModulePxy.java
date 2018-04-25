
package io.kommunicate.feeds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppModulePxy {

    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("applicationId")
    @Expose
    private String applicationId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}
