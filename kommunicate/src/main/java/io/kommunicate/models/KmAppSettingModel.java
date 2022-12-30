package io.kommunicate.models;

import com.applozic.mobicommons.json.JsonMarker;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KmAppSettingModel extends JsonMarker {
    private String code;
    private KmResponse response;
    public static final String SUCCESS = "SUCCESS";
    public static final String PRE_CHAT_GREETINGS = "PRE_CHAT_GREETINGS";

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
        private boolean collectLead;
        private List<KmPrechatInputModel> leadCollection;
        private KmCompanySetting companySetting;
        private KmSubscriptionDetails subscriptionDetails;

        public KmSubscriptionDetails getSubscriptionDetails() {
            return subscriptionDetails;
        }

        public KmCompanySetting getCompanySetting() {
            return companySetting;
        }

        public void setCompanySetting(KmCompanySetting companySetting) {
            this.companySetting = companySetting;
        }

        public List<KmPrechatInputModel> getLeadCollection() {
            return leadCollection;
        }

        public void setLeadCollection(List<KmPrechatInputModel> leadCollection) {
            this.leadCollection = leadCollection;
        }

        public boolean isCollectLead() {
            return collectLead;
        }

        public void setCollectLead(boolean collectLead) {
            this.collectLead = collectLead;
        }

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
        private String position;
        private String widgetImageLink;
        private String iconIndex;
        private boolean showPoweredBy;
        private long sessionTimeout;
        private int botMessageDelayInterval;
        private boolean pseudonymsEnabled;

        public String getPosition() {
            return position;
        }

        public boolean isPseudonymsEnabled() {return pseudonymsEnabled;}

        public void setWidgetImageLink(String widgetImageLink) {
            this.widgetImageLink = widgetImageLink;
        }

        public String getIconIndex() {
            return iconIndex;
        }

        public void setIconIndex(String iconIndex) {
            this.iconIndex = iconIndex;
        }

        public String getWidgetImageLink() {
            return widgetImageLink;
        }

        public void setPosition(String position) {
            this.position = position;
        }





        @SerializedName("isSingleThreaded")
        private boolean singleThreaded;
        private String preChatGreetingMsg;

        public String getPreChatGreetingMsg() {
            return preChatGreetingMsg;
        }

        public void setPreChatGreetingMsg(String preChatGreetingMsg) {
            this.preChatGreetingMsg = preChatGreetingMsg;
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
        private KMRolesAndPermissions rolesAndPermissions;

        public KMRolesAndPermissions getRolesAndPermissions() {
            return rolesAndPermissions;
        }

        public void setRolesAndPermissions(KMRolesAndPermissions rolesAndPermissions) {
            this.rolesAndPermissions = rolesAndPermissions;
        }

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

    public static class KmSubscriptionDetails extends JsonMarker {
        private int id;
        private boolean trialExpired;
        private String subscriptionPlan;

        public int getId() {
            return id;
        }

        public boolean isTrialExpired() {
            return trialExpired;
        }

        public String getSubscriptionPlan() {
            return subscriptionPlan;
        }

    }

    public static class KMRolesAndPermissions extends JsonMarker {
        @SerializedName("bot")
        private boolean isBotAssignmentAllowed = false;
        @SerializedName("teammate")
        private boolean isTeamMateAssignmentAllowed = false;
        @SerializedName("team")
        private boolean isTeamAssignmentAllowed = false;

        public boolean isBotAssignmentAllowed() {
            return isBotAssignmentAllowed;
        }

        public void setBotAssignmentAllowed(boolean botAssignmentAllowed) {
            isBotAssignmentAllowed = botAssignmentAllowed;
        }

        public boolean isTeamMateAssignmentAllowed() {
            return isTeamMateAssignmentAllowed;
        }

        public void setTeamMateAssignmentAllowed(boolean teamMateAssignmentAllowed) {
            isTeamMateAssignmentAllowed = teamMateAssignmentAllowed;
        }

        public boolean isTeamAssignmentAllowed() {
            return isTeamAssignmentAllowed;
        }

        public void setTeamAssignmentAllowed(boolean teamAssignmentAllowed) {
            isTeamAssignmentAllowed = teamAssignmentAllowed;
        }
    }
}
