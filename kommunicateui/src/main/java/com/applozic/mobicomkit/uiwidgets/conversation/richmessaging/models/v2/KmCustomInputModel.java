package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import android.text.TextUtils;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.json.JsonMarker;

import java.util.Map;

public class KmCustomInputModel {
    private short templateId;
    private KmField KM_FIELD;
    private Map<String, String> replyMetadata;
    public static final String EMAIL = "EMAIL";
    public static final String NAME = "NAME";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String UpdateUserDetails = "updateUserDetails";

    public short getTemplateId() {
        return templateId;
    }

    public KmField getKM_FIELD() {
        return KM_FIELD;
    }

    public Map<String, String> getReplyMetadata() {
        return replyMetadata;
    }

    public static KmCustomInputModel parseCustomInputModel(Message message) {
        if (!TextUtils.isEmpty(message.getMetadata().get(Message.KM_FIELD))) {
            return (KmCustomInputModel) GsonUtils.getObjectFromJson(message.getMetadata().toString(), KmCustomInputModel.class);
        }
        return null;
    }

    public static class KmField {
        private String label;
        private String field;
        private String fieldType;
        private String placeholder;
        private Map<String, Object> action;
        private Validation validation;

        public Validation getValidation() {
            return validation;
        }

        public String getLabel() {
            return label;
        }

        public String getField() {
            return field;
        }

        public String getFieldType() {
            return fieldType;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public Map<String, Object> getAction() {
            return action;
        }

    }

    public static class Validation extends JsonMarker {
        private String regex;
        private String errorText;

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        public String getErrorText() {
            return errorText;
        }

        public void setErrorText(String errorText) {
            this.errorText = errorText;
        }
    }
}
