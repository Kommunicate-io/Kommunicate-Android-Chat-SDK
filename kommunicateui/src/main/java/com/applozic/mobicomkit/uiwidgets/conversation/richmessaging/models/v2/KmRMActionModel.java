package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.json.JsonMarker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class KmRMActionModel<T> extends JsonMarker {
    private String name;
    private String type;
    private T action;
    public String label;
    private String postBackToKommunicate;
    private String message;
    private String requestType;
    private String formAction;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getFormAction() {
        return formAction;
    }

    public void setFormAction(String formAction) {
        this.formAction = formAction;
    }

    public void setPostBackToKommunicate(String postBackToKommunicate) {
        this.postBackToKommunicate = postBackToKommunicate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPostBackToKommunicate() {
        return postBackToKommunicate;
    }

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

    public KmRMActionModel.SubmitButton getDialogFlowAction(){
        return new Gson().fromJson(GsonUtils.getJsonFromObject(this, this.getClass()), new TypeToken<KmRMActionModel.SubmitButton>() {
        }.getType());
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
        private String postFormDataAsMessage;
        private String addFormLabelInMessage = "true";
        private String postBackToKommunicate;
        private Map<String, Object> metadata;
        private Map<String, Object> replyMetadata;
        public String label;
        public String type;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPostBackToKommunicate() {
            return postBackToKommunicate;
        }
        public String getPostFormDataAsMessage() {
            return postFormDataAsMessage;
        }

        public void setPostFormDataAsMessage(String postFormDataAsMessage) {
            this.postFormDataAsMessage = postFormDataAsMessage;
        }
        public void setAddFormLabelInMessage(String addFormLabelInMessage) {
            this.addFormLabelInMessage = addFormLabelInMessage;
        }
        public String getAddFormLabelInMessage(){
            return addFormLabelInMessage;
        }


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
        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}
