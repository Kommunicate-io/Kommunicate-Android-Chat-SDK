package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.Map;

public class KmRMActionModel<T> extends JsonMarker {
    private String name;
    private String type;
    private T action;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getAction() {
        return action;
    }

    public void setAction(T action) {
        this.action = action;
    }

    public enum Type {
        SUBMIT("submit"), LINK("link"), SUGGESTED_REPLY("suggestedReply");
        private String value;

        Type(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SubmitButton extends JsonMarker {
        public static final String KM_POST_DATA_TO_BOT_PLATFORM = "postBackToBotPlatform";
        private String message;
        private Map<String, String> formData;
        private String formAction;
        private String requestType;
        private Map<String, Object> replyMetadata;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getFormData() {
            return formData;
        }

        public void setFormData(Map<String, String> formData) {
            this.formData = formData;
        }

        public String getFormAction() {
            return formAction;
        }

        public void setFormAction(String formAction) {
            this.formAction = formAction;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public Map<String, Object> getReplyMetadata() {
            return replyMetadata;
        }

        public void setReplyMetadata(Map<String, Object> replyMetadata) {
            this.replyMetadata = replyMetadata;
        }
    }
}
