package io.kommunicate.models;

import com.applozic.mobicommons.json.JsonMarker;

public class KmAppSettingModel extends JsonMarker {
    private String code;
    private KmResponse response;
    public static final String SUCCESS = "SUCCESS";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public KmResponse getResponse() {
        return response;
    }

    public void setResponse(KmResponse response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(getCode());
    }

    public KmChatWidget getChatWidget() {
        return response.getChatWidget();
    }

    public static class KmResponse extends JsonMarker {
        private String userName;
        private String agentId;
        private String agentName;
        private KmChatWidget chatWidget;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public String getAgentName() {
            return agentName;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public KmChatWidget getChatWidget() {
            return chatWidget;
        }

        public void setChatWidget(KmChatWidget chatWidget) {
            this.chatWidget = chatWidget;
        }
    }

    public class KmChatWidget extends JsonMarker {
        private boolean popup;
        private Short iconIndex;
        private String primaryColor;
        private String secondaryColor;
        private boolean showPoweredBy;
        private int sessionTimeout;
        private int botMessageDelayInterval;

        public boolean isPopup() {
            return popup;
        }

        public void setPopup(boolean popup) {
            this.popup = popup;
        }

        public Short getIconIndex() {
            return iconIndex;
        }

        public void setIconIndex(Short iconIndex) {
            this.iconIndex = iconIndex;
        }

        public String getPrimaryColor() {
            return primaryColor;
        }

        public void setPrimaryColor(String primaryColor) {
            this.primaryColor = primaryColor;
        }

        public String getSecondaryColor() {
            return secondaryColor;
        }

        public void setSecondaryColor(String secondaryColor) {
            this.secondaryColor = secondaryColor;
        }

        public boolean isShowPoweredBy() {
            return showPoweredBy;
        }

        public void setShowPoweredBy(boolean showPoweredBy) {
            this.showPoweredBy = showPoweredBy;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public int getBotMessageDelayInterval() {
            return botMessageDelayInterval;
        }

        public void setBotMessageDelayInterval(int botMessageDelayInterval) {
            this.botMessageDelayInterval = botMessageDelayInterval;
        }
    }
}
