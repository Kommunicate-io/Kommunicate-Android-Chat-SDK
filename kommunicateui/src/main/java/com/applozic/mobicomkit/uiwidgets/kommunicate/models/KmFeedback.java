package com.applozic.mobicomkit.uiwidgets.kommunicate.models;

/**
 * model class for the feedback response
 * @author shubham
 * @date 25/July/2019
 */
public class KmFeedback {

    public KmFeedback() {}

    private int groupId;
    private int id;
    private String comments[];
    private int rating;
    private Object type;
    private String createdAt;
    private String updatedAt;
    private String deleteAt;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String[] getComments() {
        return comments;
    }

    public void setComments(String comments[]) {
        this.comments = comments;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeleteAt() {
        return deleteAt;
    }

    public void setDeleteAt(String deleteAt) {
        this.deleteAt = deleteAt;
    }

    public class UserInfo {
        String name;
        String email;
        String userName;
    }
}
