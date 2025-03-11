package com.applozic.mobicomkit.uiwidgets.kommunicate.models;

import dev.kommunicate.commons.json.JsonMarker;

public class KmSpeechToTextModel extends JsonMarker {
    private String code;
    private String name;
    private String messageToSend;
    private boolean sendMessageOnClick;

    public KmSpeechToTextModel(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public KmSpeechToTextModel(String code, String name, String messageToSend, boolean sendMessageOnClick) {
        this.code = code;
        this.name = name;
        this.messageToSend = messageToSend;
        this.sendMessageOnClick = sendMessageOnClick;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageToSend() {
        return messageToSend;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
    }

    public boolean isSendMessageOnClick() {
        return sendMessageOnClick;
    }

    public void setSendMessageOnClick(boolean sendMessageOnClick) {
        this.sendMessageOnClick = sendMessageOnClick;
    }
}
