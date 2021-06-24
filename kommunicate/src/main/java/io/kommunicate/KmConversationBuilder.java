package io.kommunicate;

import android.content.Context;

import com.applozic.mobicommons.json.Exclude;
import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;
import java.util.Map;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KMUser;

public class KmConversationBuilder extends JsonMarker {
    @Exclude
    private transient Context context;
    private boolean isSingleConversation = true;
    private boolean withPreChat = false;
    private String preFilledMessage;
    private KMUser kmUser;
    private String appId;
    private List<String> agentIds;
    private List<String> botIds;
    private List<String> userIds;
    private boolean skipConversationList = true;
    private String conversationId;
    private String fcmDeviceToken;
    private String clientConversationId;
    private boolean skipRouting;
    private String conversationAssignee;
    private String conversationTitle;
    private boolean useOriginalTitle = false;
    private String teamId;
    private Map<String, String> messageMetadata;
    private Map<String, String> conversationInfo;

    public KmConversationBuilder(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public KmConversationBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    public boolean isSingleConversation() {
        return isSingleConversation;
    }

    public KmConversationBuilder setSingleConversation(boolean isSingleConversation) {
        this.isSingleConversation = isSingleConversation;
        return this;
    }

    public boolean isWithPreChat() {
        return withPreChat;
    }

    public KmConversationBuilder setWithPreChat(boolean withPreChat) {
        this.withPreChat = withPreChat;
        return this;
    }

    public KMUser getKmUser() {
        return kmUser;
    }

    public KmConversationBuilder setKmUser(KMUser kmUser) {
        this.kmUser = kmUser;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public KmConversationBuilder setPreFilledMessage(String messageString) {
        preFilledMessage = messageString;
        return this;
    }

    public String getPreFilledMessage() {
        return preFilledMessage;
    }

    public KmConversationBuilder setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public List<String> getAgentIds() {
        return agentIds;
    }

    public KmConversationBuilder setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
        return this;
    }

    public List<String> getBotIds() {
        return botIds;
    }

    public KmConversationBuilder setBotIds(List<String> botIds) {
        this.botIds = botIds;
        return this;
    }

    public KmConversationBuilder setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public boolean isSkipConversationList() {
        return skipConversationList;
    }

    public KmConversationBuilder setSkipConversationList(boolean skipConversationList) {
        this.skipConversationList = skipConversationList;
        return this;
    }

    public String getFcmDeviceToken() {
        return fcmDeviceToken;
    }

    public KmConversationBuilder setFcmDeviceToken(String fcmDeviceToken) {
        this.fcmDeviceToken = fcmDeviceToken;
        return this;
    }

    public KmConversationBuilder setMessageMetadata(Map<String, String> messageMetadata) {
        this.messageMetadata = messageMetadata;
        return this;
    }

    public Map<String, String> getMessageMetadata() {
        return messageMetadata;
    }

    public String getClientConversationId() {
        return clientConversationId;
    }

    public KmConversationBuilder setClientConversationId(String clientConversationId) {
        this.clientConversationId = clientConversationId;
        return this;
    }

    public boolean isSkipConversationRoutingRules() {
        return skipRouting;
    }

    public KmConversationBuilder skipConversationRoutingRules(boolean skipRouting) {
        this.skipRouting = skipRouting;
        return this;
    }

    public String getConversationAssignee() {
        return conversationAssignee;
    }

    public KmConversationBuilder useSavedOptions() {
        KmConversationBuilder builder = KmPreference.getInstance(context).getKmConversationBuilder();
        builder.setContext(context);
        return builder;
    }

    public KmConversationBuilder saveOptions() {
        KmPreference.getInstance(context).setKmConversationBuilder(this);
        return this;
    }

    public KmConversationBuilder setConversationAssignee(String conversationAssignee) {
        this.conversationAssignee = conversationAssignee;
        return this;
    }

    public boolean isUseOriginalTitle() {
        return useOriginalTitle;
    }

    public KmConversationBuilder setUseOriginalTitle(boolean useOriginalTitle) {
        this.useOriginalTitle = useOriginalTitle;
        return this;
    }

    public Map<String, String> getConversationInfo() {
        return conversationInfo;
    }

    public KmConversationBuilder setConversationInfo(Map<String, String> conversationInfo) {
        this.conversationInfo = conversationInfo;
        return this;
    }

    public String getConversationTitle() {
        return conversationTitle;
    }

    public KmConversationBuilder setConversationTitle(String conversationTitle) {
        this.conversationTitle = conversationTitle;
        return this;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public KmConversationBuilder setUserIds(List<String> userIds) {
        this.userIds = userIds;
        return this;
    }

    public void createConversation(KmCallback callback) {
        KmConversationHelper.createOrLaunchConversation(this, false, callback);
    }

    public void launchConversation(KmCallback callback) {
        KmConversationHelper.createOrLaunchConversation(this, true, callback);
    }

    public void launchAndCreateIfEmpty(KmCallback callback) {
        KmConversationHelper.launchAndCreateIfEmpty(this, callback);
    }

    public String getTeamId() {
        return teamId;
    }

    public KmConversationBuilder setTeamId(String teamId) {
        this.teamId = teamId;
        return this;
    }
}
