package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.json.JsonMarker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class KmFormPayloadModel<T> extends JsonMarker {
    public static final String KM_FORM_DATA = "formData";
    private String type;
    private T data;

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

    public static class Text extends JsonMarker {
        private String label;
        private String placeholder;

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
    }

    public enum Type {
        TEXT("text"), PASSWORD("password"), HIDDEN("hidden"), RADIO("radio"), CHECKBOX("checkbox"), SUBMIT("submit");

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

    public KmFormPayloadModel.Hidden getHiddenModel() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmFormPayloadModel.Hidden>() {
        }.getType());
    }

    public KmFormPayloadModel.Selections getSelectionModel() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmFormPayloadModel.Selections>() {
        }.getType());
    }

    public KmRMActionModel<KmRMActionModel.SubmitButton> getAction() {
        return new Gson().fromJson(GsonUtils.getJsonFromObject(data, Object.class), new TypeToken<KmRMActionModel<KmRMActionModel.SubmitButton>>() {
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
