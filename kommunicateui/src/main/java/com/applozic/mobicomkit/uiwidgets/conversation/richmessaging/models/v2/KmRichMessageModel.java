package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.json.JsonMarker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class KmRichMessageModel<T> extends JsonMarker {
    private Short contentType;
    private Short templateId;
    private T payload;

    public Short getContentType() {
        return contentType;
    }

    public void setContentType(Short contentType) {
        this.contentType = contentType;
    }

    public Short getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Short templateId) {
        this.templateId = templateId;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public enum TemplateId {
        BUTTON(Short.valueOf("3")), SUGGESTED_REPLY(Short.valueOf("6")), LIST(Short.valueOf("7")), FAQ(Short.valueOf("8")),
        IMAGE(Short.valueOf("9")), CARD(Short.valueOf("10")), MIXED_BUTTONS(Short.valueOf("11")), FORM(Short.valueOf("12"));
        private Short value;

        TemplateId(Short s) {
            value = s;
        }

        public Short getValue() {
            return value;
        }
    }

    public List<KmFormPayloadModel> getFormModelList() {
        try {
            return new Gson().fromJson(getPayloadJson(), new TypeToken<List<KmFormPayloadModel>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<KmRMActionModel> getButtonList() {
        return new Gson().fromJson(getPayloadJson(), new TypeToken<List<KmFormPayloadModel>>() {
        }.getType());
    }

    private String getPayloadJson() {
        return payload instanceof String ? payload.toString() : GsonUtils.getJsonFromObject(payload, Object.class);
    }
}
