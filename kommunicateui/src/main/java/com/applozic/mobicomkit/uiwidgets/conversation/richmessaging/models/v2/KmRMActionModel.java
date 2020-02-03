package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import com.applozic.mobicommons.json.JsonMarker;

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
}
