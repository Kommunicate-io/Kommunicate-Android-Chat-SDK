package com.applozic.mobicomkit;

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicomkit.feed.ChannelFeed;

import java.util.List;

/**
 * Created by sunil on 12/1/16.
 */
public class MultipleChannelFeedApiResponse extends JsonMarker {

    private static final String SUCCESS = "success";
    private String status;
    private String generatedAt;
    private List<ChannelFeed> response;

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
        return response;
    }

    public void setResponse(List<ChannelFeed> response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    @Override
    public String toString() {
        return "MultipleChannelFeedApiResponse{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response=" + response +
                '}';
    }
}
