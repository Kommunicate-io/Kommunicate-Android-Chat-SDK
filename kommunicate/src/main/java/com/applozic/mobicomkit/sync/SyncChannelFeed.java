package com.applozic.mobicomkit.sync;

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.List;

/**
 * Created by sunil on 28/1/16.
 */
public class SyncChannelFeed extends JsonMarker {


    private static final String SUCCESS = "success";
    private String status;
    private String generatedAt;
    private groupPxys response;
    private String updatedAt;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<ChannelFeed> getResponse() {
        if(response != null) {
            return response.getGroupPxys();
        }
        return null;
    }

    public void setResponse(groupPxys response) {
        this.response = response;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    @Override
    public String toString() {
        return "SyncChannelFeed{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response=" + response +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    public class groupPxys extends JsonMarker {
        private List<ChannelFeed> groupPxys;

        public List<ChannelFeed> getGroupPxys() {
            return groupPxys;
        }
    }
}
