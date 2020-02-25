package com.applozic.mobicomkit.uiwidgets.kommunicate.models;

import android.text.InputType;

import com.applozic.mobicommons.json.JsonMarker;

public class KmPrechatInputModel extends JsonMarker {

    private String field;
    private String placeholder;
    private String type;
    private boolean required;
    private String element;
    private String validationRegex;
    private String validationError;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public String getValidationError() {
        return validationError;
    }

    public void setValidationError(String validationError) {
        this.validationError = validationError;
    }

    public static class KmInputType {
        public static final String PASSWORD = "password";
        public static final String TEXT = "text";
        public static final String NUMBER = "number";
        public static final String EMAIL = "email";

        public static int getInputType(String type) {
            switch (type) {
                case NUMBER:
                    return InputType.TYPE_CLASS_PHONE;
                case EMAIL:
                    return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                case PASSWORD:
                    return InputType.TYPE_TEXT_VARIATION_PASSWORD;
                default:
                    return InputType.TYPE_CLASS_TEXT;
            }
        }
    }
}
