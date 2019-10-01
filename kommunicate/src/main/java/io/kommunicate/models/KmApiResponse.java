package io.kommunicate.models;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by ashish on 03/04/18.
 */

public class KmApiResponse<T> extends JsonMarker {
    private String code;
    private T data;
    public static final String KM_AUTO_SUGGESSTION_SUCCESS_RESPONSE = "GOT_ALL_SUGGESTIONS_BY_APPLICATION_ID";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static class KmDataResposne extends JsonMarker {
        private List<KmMessageResponse> messageList;

        public List<KmMessageResponse> getMessageList() {
            return messageList;
        }

        public void setMessageList(List<KmMessageResponse> messageList) {
            this.messageList = messageList;
        }
    }

    public static class KmMessageResponse extends JsonMarker {
        private long id;
        private long customerId;
        private short eventId;
        private String message;
        private short status;
        private short sequence;
        private long createdBy;
        private short category;
        private String created_at;
        private String updated_at;
        private String deleted_at;
        private String applicationId;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(long customerId) {
            this.customerId = customerId;
        }

        public short getEventId() {
            return eventId;
        }

        public void setEventId(short eventId) {
            this.eventId = eventId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public short getStatus() {
            return status;
        }

        public void setStatus(short status) {
            this.status = status;
        }

        public short getSequence() {
            return sequence;
        }

        public void setSequence(short sequence) {
            this.sequence = sequence;
        }

        public long getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(long createdBy) {
            this.createdBy = createdBy;
        }

        public short getCategory() {
            return category;
        }

        public void setCategory(short category) {
            this.category = category;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        public String getDeleted_at() {
            return deleted_at;
        }

        public void setDeleted_at(String deleted_at) {
            this.deleted_at = deleted_at;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }
    }
}
