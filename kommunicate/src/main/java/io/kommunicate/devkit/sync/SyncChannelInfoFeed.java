package io.kommunicate.devkit.sync;

import io.kommunicate.commons.json.JsonMarker;
import io.kommunicate.devkit.feed.ChannelFeed;

public class SyncChannelInfoFeed extends JsonMarker {

    private static final String SUCCESS = "success";

    private String status;
    private String generatedAt;
    private ChannelFeed response; // Changed from groupPxys to ChannelFeed

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

    public ChannelFeed getResponse() {
        return response;
    }

    public void setResponse(ChannelFeed response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    @Override
    public String toString() {
        return "SyncChannelInfoFeed{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response=" + response +
                '}';
    }
}
