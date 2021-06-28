package io.kommunicate.models;

import com.applozic.mobicommons.json.JsonMarker;
import com.google.gson.annotations.SerializedName;

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
        private boolean collectFeedback;
        private boolean hidePostCTA;
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

        public boolean isCollectFeedback() {
            return collectFeedback;
        }

        public boolean isHidePostCTA() {
            return hidePostCTA;
        }

        public void setCollectFeedback(boolean collectFeedback) {
            this.collectFeedback = collectFeedback;
        }

        public KmChatWidget getChatWidget() {
            return chatWidget;
        }

        public void setChatWidget(KmChatWidget chatWidget) {
            this.chatWidget = chatWidget;
        }
    }

    public static class KmChatWidget extends JsonMarker {
        private String primaryColor;
        private String secondaryColor;
        private boolean showPoweredBy;
        private long sessionTimeout;
        private int botMessageDelayInterval;
        @SerializedName("isSingleThreaded")
        private boolean singleThreaded;

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

        public long getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(long sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public int getBotMessageDelayInterval() {
            return botMessageDelayInterval;
        }

        public void setBotMessageDelayInterval(int botMessageDelayInterval) {
            this.botMessageDelayInterval = botMessageDelayInterval;
        }

        public boolean isSingleThreaded() {
            return singleThreaded;
        }
    }

    public static class KmCompanySetting extends JsonMarker {
        private int inactiveTime;
        private boolean enableWaitingQueue;
        private int conversationHandlingLimit;

        public int getInactiveTime() {
            return inactiveTime;
        }

        public void setInactiveTime(int inactiveTime) {
            this.inactiveTime = inactiveTime;
        }

        public boolean isEnableWaitingQueue() {
            return enableWaitingQueue;
        }

        public void setEnableWaitingQueue(boolean enableWaitingQueue) {
            this.enableWaitingQueue = enableWaitingQueue;
        }

        public int getConversationHandlingLimit() {
            return conversationHandlingLimit;
        }

        public void setConversationHandlingLimit(int conversationHandlingLimit) {
            this.conversationHandlingLimit = conversationHandlingLimit;
        }
    }
}
