package com.applozic.mobicomkit;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.Map;

public class AlUserUpdate extends JsonMarker {
    private String displayName;
    private String imageLink;
    private String statusMessage;
    private String phoneNumber;
    private String email;
    private Map<String, String> metadata;

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "AlUserUpdate{" +
                "displayName='" + displayName + '\'' +
                ", imageLink='" + imageLink + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
