package io.kommunicate.models;

import android.text.InputType;

import com.applozic.mobicommons.json.JsonMarker;

public class KmPrechatInputModel extends JsonMarker {
    public static final String KM_PRECHAT_MODEL_LIST = "preChatModelList";
    private String field;
    private String placeholder;
    private String type;
    private String element;
    private String validationRegex;
    private String validationError;
    private String compositeRequiredField;
    private boolean required;
    private boolean displayValidationError;
    private boolean displayEmptyFieldError;

    public String getField() {
        return field;
    }

    public KmPrechatInputModel setField(String field) {
        this.field = field;
        return this;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public KmPrechatInputModel setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public String getType() {
        return type;
    }

    public KmPrechatInputModel setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public KmPrechatInputModel setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public String getElement() {
        return element;
    }

    public KmPrechatInputModel setElement(String element) {
        this.element = element;
        return this;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public KmPrechatInputModel setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
        return this;
    }

    public String getValidationError() {
        return validationError;
    }

    public KmPrechatInputModel setValidationError(String validationError) {
        this.validationError = validationError;
        return this;
    }

    public boolean isDisplayValidationError() {
        return displayValidationError;
    }

    public KmPrechatInputModel setDisplayValidationError(boolean displayValidationError) {
        this.displayValidationError = displayValidationError;
        return this;
    }

    public boolean isDisplayEmptyFieldError() {
        return displayEmptyFieldError;
    }

    public KmPrechatInputModel setDisplayEmptyFieldError(boolean displayEmptyFieldError) {
        this.displayEmptyFieldError = displayEmptyFieldError;
        return this;
    }

    public String getCompositeRequiredField() {
        return compositeRequiredField;
    }

    public KmPrechatInputModel setCompositeRequiredField(String compositeRequiredField) {
        this.compositeRequiredField = compositeRequiredField;
        return this;
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
