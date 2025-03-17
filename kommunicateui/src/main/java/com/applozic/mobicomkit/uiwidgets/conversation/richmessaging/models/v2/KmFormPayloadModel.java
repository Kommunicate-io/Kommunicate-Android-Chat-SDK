package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import dev.kommunicate.commons.json.GsonUtils;
import dev.kommunicate.commons.json.JsonMarker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public class KmFormPayloadModel<T> extends JsonMarker {
    public static final String KM_FORM_DATA = "formData";
    private String type;
    private T data;
    private List<Options> options;
    private String title;
    private String name;
    private String label;
    private String value;
    private String placeholder;
    private String formAction;
    private String message;
    private String requestType;
    private String postFormDataAsMessage;
    private Boolean postBackToKommunicate;
    private Map<String,String> metadata;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isTypeText() {
        return KmFormPayloadModel.Type.TEXT.getValue().equals(type) || KmFormPayloadModel.Type.PASSWORD.getValue().equals(type);
    }

    public boolean isTypeDateTime() {
        return KmFormPayloadModel.Type.DATE.getValue().equals(type)
                || KmFormPayloadModel.Type.TIME.getValue().equals(type)
                || KmFormPayloadModel.Type.DATE_TIME.getValue().equals(type);
    }

    public boolean isTypeDropdown() {
        return Type.DROPDOWN.getValue().equals(type);
    }

    public boolean isTypeSelection() {
        return Type.RADIO.getValue().equals(type) || Type.CHECKBOX.getValue().equals(type);
    }

    public boolean isTypeTextArea(){
        return Type.TEXTAREA.getValue().equals(type);
    }
    public boolean isTypeHidden() {
        return Type.HIDDEN.getValue().equals(type);
    }

    public boolean isTypeAction() {
        return Type.ACTION.getValue().equals(type) || Type.SUBMIT.getValue().equals(type);
    }

    public List<Options> getOptions() {
        return options;
    }

    public void setOptions(List<Options> options) {
        this.options = options;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getFormAction() {
        return formAction;
    }

    public void setFormAction(String formAction) {
        this.formAction = formAction;
    }

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

    public String getPostFormDataAsMessage() {
        return postFormDataAsMessage;
    }

    public void setPostFormDataAsMessage(String postFormDataAsMessage) {
        this.postFormDataAsMessage = postFormDataAsMessage;
    }

    public Boolean getPostBackToKommunicate() {
        return postBackToKommunicate;
    }

    public void setPostBackToKommunicate(Boolean postBackToKommunicate) {
        this.postBackToKommunicate = postBackToKommunicate;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public KmRMActionModel<KmRMActionModel.SubmitButton> getDialogFlowActionModel(){
        return new Gson().fromJson(GsonUtils.getJsonFromObject(this, KmFormPayloadModel.class), new TypeToken<KmRMActionModel<KmRMActionModel.SubmitButton>>() {
        }.getType());
    }

    public static class Text extends JsonMarker {
        private String label;
        private String placeholder;
        private Validation validation;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }
    }
    public static class TextArea extends JsonMarker {
        private String title;
        private String name;
        private String placeholder;
        private int cols;
        private int rows;
        private Validation validation;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public int getCols() {
            return cols;
        }

        public void setCols(int cols) {
            this.cols = cols;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }
    }


    public static class Hidden extends JsonMarker {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public static class Selections extends JsonMarker {
        private String title;
        private String name;
        private List<Options> options;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Options> getOptions() {
            return options;
        }

        public void setOptions(List<Options> options) {
            this.options = options;
        }
    }

    public static class Options extends JsonMarker {
        private String label;
        private String value;
        private boolean selected;
        private boolean disabled;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Options options = (Options) o;
            return label.equals(options.label);
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

    public static class DateTimePicker extends JsonMarker {
        private String label;
        private boolean amPm = true;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isAmPm() {
            return amPm;
        }

        public void setAmPm(boolean amPm) {
            this.amPm = amPm;
        }
    }

    public static class DropdownList extends Selections {
        private Validation validation;

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }
    }

    public enum Type {
        TEXT("text"), PASSWORD("password"),TEXTAREA("textarea"),
        HIDDEN("hidden"), RADIO("radio"),
        CHECKBOX("checkbox"),
        DATE("date"),
        TIME("time"),
        DROPDOWN("dropdown"),
        DATE_TIME("datetime-local"),
        ACTION("action"),
        SUBMIT("submit");

        private String value;

        Type(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }
    }

    public Text getTextModel() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<Text>() {
        }.getType());
    }

    public TextArea getTextAreaModel(){
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<TextArea>() {
        }.getType());
    }

    public KmFormPayloadModel.Hidden getHiddenModel() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmFormPayloadModel.Hidden>() {
        }.getType());
    }

    public KmFormPayloadModel.Selections getSelectionModel() {
        if (data != null) {
            return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmFormPayloadModel.Selections>() {
            }.getType());
        } else {
            return new Gson().fromJson(GsonUtils.getJsonFromObject(this, this.getClass()), new TypeToken<KmFormPayloadModel.Selections>() {
            }.getType());
        }
    }

    public KmRMActionModel<KmRMActionModel.SubmitButton> getAction() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmRMActionModel<KmRMActionModel.SubmitButton>>() {
        }.getType());
    }

    public KmFormPayloadModel.DateTimePicker getDatePickerModel() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmFormPayloadModel.DateTimePicker>() {
        }.getType());
    }

    public KmFormPayloadModel.DropdownList getDropdownList() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmFormPayloadModel.DropdownList>() {
        }.getType());
    }

    @Override
    public String toString() {
        return "KmFormPayloadModel{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }
}
