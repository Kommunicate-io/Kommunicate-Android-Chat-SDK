package io.kommunicate.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * model class for the feedback response
 *
 * @author shubham
 * @date 25/July/2019
 */
public class KmFeedback {

    public KmFeedback() {
    }

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

    public boolean isLatestFeedbackSubmitted(long messageTimeStamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
            calendar.setTime(dateFormat.parse(getUpdatedAt()));
            long messageTimeOffset = new Date(messageTimeStamp - Calendar.getInstance().getTimeZone().getOffset(messageTimeStamp)).getTime();
            return calendar.getTimeInMillis() > messageTimeOffset;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
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
}
