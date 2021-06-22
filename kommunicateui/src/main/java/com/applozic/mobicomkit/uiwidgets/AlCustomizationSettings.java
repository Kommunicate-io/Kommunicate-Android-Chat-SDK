package com.applozic.mobicomkit.uiwidgets;

import com.applozic.mobicomkit.uiwidgets.conversation.MobicomMessageTemplate;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmFontModel;
import com.applozic.mobicommons.json.JsonMarker;

import java.util.Map;

/**
 * Created by sunil on 10/10/16.
 */
public class AlCustomizationSettings extends JsonMarker {
    public static final int DEFAULT_MESSAGE_CHAR_LIMIT = 2000;

    private String customMessageBackgroundColor = "#e6e5ec";
    private String sentMessageBackgroundColor = "";
    private String receivedMessageBackgroundColor = "#e6e5ec";
    private String sendButtonBackgroundColor = "";
    private String attachmentIconsBackgroundColor = "#FF03A9F4";
    private String chatBackgroundColorOrDrawable;
    private String editTextBackgroundColorOrDrawable;
    private String editTextLayoutBackgroundColorOrDrawable;
    private String channelCustomMessageBgColor = "#cccccc";
    private String toolbarTitleColor = "#ffffff";
    private String toolbarSubtitleColor = "#ffffff";
    private String receiverNameTextColor = "#5C6677";

    private String sentContactMessageTextColor = "#5fba7d";
    private String receivedContactMessageTextColor = "#646262";
    private String sentMessageTextColor = "#FFFFFFFF";
    private String receivedMessageTextColor = "#646262";
    private String messageEditTextTextColor = "#000000";
    private String sentMessageLinkTextColor = "#FFFFFFFF";
    private String receivedMessageLinkTextColor = "#5fba7d";
    private String messageEditTextHintTextColor = "#bdbdbd";
    private String typingTextColor;
    private String noConversationLabelTextColor = "#000000";
    private String conversationDateTextColor = "#333333";
    private String conversationDayTextColor = "#333333";
    private String messageTimeTextColor = "#ede6e6";
    private String channelCustomMessageTextColor = "#666666";
    private String sentMessageBorderColor = "";
    private String receivedMessageBorderColor = "#e6e5ec";
    private String channelCustomMessageBorderColor = "#cccccc";
    private String collapsingToolbarLayoutColor = "#5c5aa7";
    private String groupParticipantsTextColor = "#5c5aa7";
    private String groupDeleteButtonBackgroundColor = "#5c5aa7";
    private String groupExitButtonBackgroundColor = "#5c5aa7";
    private String adminTextColor = "#5c5aa7";
    private String adminBackgroundColor = "#FFFFFFFF";
    private String attachCameraIconName = "applozic_ic_action_camera_new";
    private String adminBorderColor = "#5c5aa7";
    private String userNotAbleToChatTextColor = "#000000";
    private String chatBackgroundImageName;

    private String audioPermissionNotFoundMsg;
    private String noConversationLabel = "You have no conversations";
    private String noSearchFoundForChatMessages = "No conversation found";
    private String restrictedWordMessage = "Restricted words are not allowed";
    private boolean locationShareViaMap = true;
    private boolean startNewFloatingButton = false;
    private boolean startNewButton = false;
    private boolean onlineStatusMasterList = true;
    private boolean priceWidget;
    private boolean startNewGroup = false;
    private boolean imageCompression = false;
    private boolean inviteFriendsInContactActivity = false;
    private boolean registeredUserContactListCall = false;
    private boolean createAnyContact = false;
    private boolean showActionDialWithOutCalling = false;
    private boolean profileLogoutButton = false;
    private boolean userProfileFragment = false;
    private boolean messageSearchOption = false;
    private boolean conversationContactImageVisibility = true;
    private boolean hideGroupAddMembersButton = false;
    private boolean hideGroupNameUpdateButton = false;
    private boolean hideGroupExitButton = false;
    private boolean hideGroupRemoveMemberOption = false;
    private boolean profileOption = false;
    private boolean broadcastOption = false;
    private boolean hideAttachmentButton = false;
    private boolean groupUsersOnlineStatus = false;
    private boolean refreshOption = false;
    private boolean deleteOption = false;
    private boolean blockOption = true;
    private boolean muteOption = true;
    private MobicomMessageTemplate messageTemplate;
    private String logoutPackageName = "kommunicate.io.sample.MainActivity";
    private boolean logoutOption = false;
    private int defaultGroupType = 2;
    private boolean muteUserChatOption = false;
    private String restrictedWordRegex;
    private int totalRegisteredUserToFetch = 100;
    private int maxAttachmentAllowed = 5;
    private int maxAttachmentSizeAllowed = 30;
    private int messageCharacterLimit = DEFAULT_MESSAGE_CHAR_LIMIT;
    private int totalOnlineUsers = 0;
    private String themeColorPrimary;
    private String themeColorPrimaryDark;
    private String editTextHintText = "Write a Message..";
    private boolean replyOption = false;
    private String replyMessageLayoutSentMessageBackground = "#C0C0C0";
    private String replyMessageLayoutReceivedMessageBackground = "#F5F5F5";
    private boolean groupInfoScreenVisible = true;
    private boolean forwardOption = false;
    private String sentMessageCreatedAtTimeColor = "#ede6e6";
    private String receivedMessageCreatedAtTimeColor = "#8a8686";
    private boolean showStartNewConversation = true;
    private boolean enableAwayMessage = true;
    private String awayMessageTextColor = "#A9A4A4";
    private boolean isAgentApp = false;
    private boolean hideGroupSubtitle = false;
    private boolean disableGlobalStoragePermission = true;

    private boolean launchChatFromProfilePicOrName = false;
    private Map<String, Boolean> filterGallery;
    private boolean enableShareConversation = false;
    private String messageStatusIconColor = "";
    private float[] sentMessageCornerRadii;
    private float[] receivedMessageCornerRadii;
    private KmFontModel fontModel;
    private boolean isFaqOptionEnabled = false;
    private boolean[] enableFaqOption = {true, false};
    private String toolbarColor = "";
    private String statusBarColor = "";
    private String richMessageThemeColor = "";
    private Map<String, Boolean> attachmentOptions;
    private KmSpeechSetting textToSpeech;
    private KmSpeechSetting speechToText;
    private boolean restrictMessageTypingWithBots = false;
    private Map<String, Boolean> hidePostCTA;

    public String getNoConversationLabel() {
        return noConversationLabel;
    }

    public String getCustomMessageBackgroundColor() {
        return customMessageBackgroundColor;
    }


    public String getSentMessageBackgroundColor() {
        return sentMessageBackgroundColor;
    }

    public String getReceivedMessageBackgroundColor() {
        return receivedMessageBackgroundColor;
    }

    public boolean isOnlineStatusMasterList() {
        return onlineStatusMasterList;
    }

    public boolean isPriceWidget() {
        return priceWidget;
    }

    public String getSendButtonBackgroundColor() {
        return sendButtonBackgroundColor;
    }

    public boolean isImageCompression() {
        return imageCompression;
    }


    public boolean isInviteFriendsInContactActivity() {
        return inviteFriendsInContactActivity;
    }

    public String getAttachmentIconsBackgroundColor() {
        return attachmentIconsBackgroundColor;
    }

    public boolean isLocationShareViaMap() {
        return locationShareViaMap;
    }

    public boolean isConversationContactImageVisibility() {
        return conversationContactImageVisibility;
    }

    public String getSentContactMessageTextColor() {
        return sentContactMessageTextColor;
    }

    public String getReceivedContactMessageTextColor() {
        return receivedContactMessageTextColor;
    }

    public String getSentMessageTextColor() {
        return sentMessageTextColor;
    }

    public String getReceivedMessageTextColor() {
        return receivedMessageTextColor;
    }

    public String getSentMessageBorderColor() {
        return sentMessageBorderColor;
    }

    public String getReceivedMessageBorderColor() {
        return receivedMessageBorderColor;
    }

    public String getChatBackgroundColorOrDrawable() {
        return chatBackgroundColorOrDrawable;
    }

    public String getMessageEditTextTextColor() {
        return messageEditTextTextColor;
    }

    public String getAudioPermissionNotFoundMsg() {
        return audioPermissionNotFoundMsg;
    }

    public boolean isRegisteredUserContactListCall() {
        return registeredUserContactListCall;
    }

    public boolean isCreateAnyContact() {
        return createAnyContact;
    }

    public boolean isShowActionDialWithOutCalling() {
        return showActionDialWithOutCalling;
    }

    public String getSentMessageLinkTextColor() {
        return sentMessageLinkTextColor;
    }

    public String getReceivedMessageLinkTextColor() {
        return receivedMessageLinkTextColor;
    }

    public String getMessageEditTextHintTextColor() {
        return messageEditTextHintTextColor;
    }

    public boolean isHideGroupAddMembersButton() {
        return hideGroupAddMembersButton;
    }

    public boolean isHideGroupNameUpdateButton() {
        return hideGroupNameUpdateButton;
    }

    public boolean isHideGroupExitButton() {
        return hideGroupExitButton;
    }

    public boolean isHideGroupRemoveMemberOption() {
        return hideGroupRemoveMemberOption;
    }


    public String getEditTextBackgroundColorOrDrawable() {
        return editTextBackgroundColorOrDrawable;
    }

    public String getEditTextLayoutBackgroundColorOrDrawable() {
        return editTextLayoutBackgroundColorOrDrawable;
    }

    public String getTypingTextColor() {
        return typingTextColor;
    }

    public boolean isProfileOption() {
        return profileOption;
    }

    public String getNoConversationLabelTextColor() {
        return noConversationLabelTextColor;
    }

    public String getConversationDateTextColor() {
        return conversationDateTextColor;
    }

    public String getConversationDayTextColor() {
        return conversationDayTextColor;
    }

    public String getMessageTimeTextColor() {
        return messageTimeTextColor;
    }

    public String getChannelCustomMessageBgColor() {
        return channelCustomMessageBgColor;
    }

    public String getChannelCustomMessageBorderColor() {
        return channelCustomMessageBorderColor;
    }

    public String getChannelCustomMessageTextColor() {
        return channelCustomMessageTextColor;
    }

    public String getNoSearchFoundForChatMessages() {
        return noSearchFoundForChatMessages;
    }

    public boolean isProfileLogoutButton() {
        return profileLogoutButton;
    }

    public boolean isUserProfileFragment() {
        return userProfileFragment;
    }

    public boolean isMessageSearchOption() {
        return messageSearchOption;
    }


    public int getTotalRegisteredUserToFetch() {
        return totalRegisteredUserToFetch;
    }


    public int getMaxAttachmentAllowed() {
        return maxAttachmentAllowed;
    }

    public int getMaxAttachmentSizeAllowed() {
        return maxAttachmentSizeAllowed;
    }

    public int getTotalOnlineUsers() {
        return totalOnlineUsers;
    }

    public String getCollapsingToolbarLayoutColor() {
        return collapsingToolbarLayoutColor;
    }

    public String getGroupParticipantsTextColor() {
        return groupParticipantsTextColor;
    }

    public String getGroupExitButtonBackgroundColor() {
        return groupExitButtonBackgroundColor;
    }

    public String getGroupDeleteButtonBackgroundColor() {
        return groupDeleteButtonBackgroundColor;
    }

    public String getAdminTextColor() {
        return adminTextColor;
    }

    public String getAdminBackgroundColor() {
        return adminBackgroundColor;
    }

    public String getAttachCameraIconName() {
        return attachCameraIconName;
    }

    public String getAdminBorderColor() {
        return adminBorderColor;
    }

    public String getUserNotAbleToChatTextColor() {
        return userNotAbleToChatTextColor;
    }

    public String getChatBackgroundImageName() {
        return chatBackgroundImageName;
    }

    public Map<String, Boolean> getAttachmentOptions() {
        return attachmentOptions;
    }

    public void setAttachmentOptions(Map<String, Boolean> attachmentOptions) {
        this.attachmentOptions = attachmentOptions;
    }


    public boolean isHideAttachmentButton() {
        return hideAttachmentButton;
    }

    public void setHideAttachmentButton(boolean hideAttachmentButton) {
        this.hideAttachmentButton = hideAttachmentButton;
    }

    public String getRestrictedWordMessage() {
        return restrictedWordMessage;
    }

    public void setRestrictedWordMessage(String restrictedWordMessage) {
        this.restrictedWordMessage = restrictedWordMessage;
    }

    public boolean isLaunchChatFromProfilePicOrName() {
        return launchChatFromProfilePicOrName;
    }

    public boolean isGroupUsersOnlineStatus() {
        return groupUsersOnlineStatus;
    }


    public boolean isRefreshOption() {
        return refreshOption;
    }

    public void setRefreshOption(boolean refreshOption) {
        this.refreshOption = refreshOption;
    }

    public boolean isDeleteOption() {
        return deleteOption;
    }

    public void setDeleteOption(boolean deleteOption) {
        this.deleteOption = deleteOption;
    }

    public boolean isBlockOption() {
        return blockOption;
    }

    public void setBlockOption(boolean blockOption) {
        this.blockOption = blockOption;
    }

    public boolean isMuteOption() {
        return muteOption;
    }

    public void setMuteOption(boolean muteOption) {
        this.muteOption = muteOption;
    }

    public boolean isLogoutOption() {
        return logoutOption;
    }

    public void setLogout(boolean logoutOption) {
        this.logoutOption = logoutOption;
    }

    public String getLogoutPackage() {
        return logoutPackageName;
    }

    public void setLogoutPackageName(String logoutPackageName) {
        this.logoutPackageName = logoutPackageName;
    }

    public String getThemeColorPrimary() {
        return themeColorPrimary;
    }

    public String getThemeColorPrimaryDark() {
        return themeColorPrimaryDark;
    }

    public String getEditTextHintText() {
        return editTextHintText;
    }

    public boolean isReplyOption() {
        return replyOption;
    }

    public void setReplyOption(boolean replyOption) {
        this.replyOption = replyOption;
    }

    public String getReplyMessageLayoutSentMessageBackground() {
        return replyMessageLayoutSentMessageBackground;
    }

    public String getReplyMessageLayoutReceivedMessageBackground() {
        return replyMessageLayoutReceivedMessageBackground;
    }

    public void setUserChatMuteOption(boolean muteUserChatOption) {
        this.muteUserChatOption = muteUserChatOption;
    }

    public boolean isMuteUserChatOption() {
        return muteUserChatOption;
    }

    public boolean isGroupInfoScreenVisible() {
        return groupInfoScreenVisible;
    }

    public boolean isForwardOption() {
        return forwardOption;
    }

    public void setForwardOption(boolean forwardOption) {
        this.forwardOption = forwardOption;
    }

    public int getDefaultGroupType() {
        return defaultGroupType;
    }

    public void setDefaultGroupType(int defaultGroupType) {
        this.defaultGroupType = defaultGroupType;
    }

    public void setMessageTemplate(MobicomMessageTemplate messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public int getMessageCharacterLimit() {
        return messageCharacterLimit;
    }

    public MobicomMessageTemplate getMessageTemplate() {
        return messageTemplate;
    }

    public String getSentMessageCreatedAtTimeColor() {
        return sentMessageCreatedAtTimeColor;
    }

    public void setSentMessageCreatedAtTimeColor(String sentMessageCreatedAtTimeColor) {
        this.sentMessageCreatedAtTimeColor = sentMessageCreatedAtTimeColor;
    }

    public String getReceivedMessageCreatedAtTimeColor() {
        return receivedMessageCreatedAtTimeColor;
    }

    public void setReceivedMessageCreatedAtTimeColor(String receivedMessageCreatedAtTimeColor) {
        this.receivedMessageCreatedAtTimeColor = receivedMessageCreatedAtTimeColor;
    }

    public boolean isShowStartNewConversation() {
        return showStartNewConversation;
    }

    public void setShowStartNewConversation(boolean showStartNewConversation) {
        this.showStartNewConversation = showStartNewConversation;
    }

    public boolean isEnableAwayMessage() {
        return enableAwayMessage;
    }

    public void setEnableAwayMessage(boolean enableAwayMessage) {
        this.enableAwayMessage = enableAwayMessage;
    }

    public String getAwayMessageTextColor() {
        return awayMessageTextColor;
    }

    public void setAwayMessageTextColor(String awayMessageTextColor) {
        this.awayMessageTextColor = awayMessageTextColor;
    }

    public boolean isAgentApp() {
        return isAgentApp;
    }

    public boolean isGroupSubtitleHidden() {
        return hideGroupSubtitle;
    }

    public boolean isGlobalStoragePermissionDisabled() {
        return disableGlobalStoragePermission;
    }

    public Map<String, Boolean> getFilterGallery() {
        return filterGallery;
    }

    public void setFilterGallery(Map<String, Boolean> filterGallery) {
        this.filterGallery = filterGallery;
    }

    public boolean isEnableShareConversation() {
        return enableShareConversation;
    }

    public void setEnableShareConversation(boolean enableShareConversation) {
        this.enableShareConversation = enableShareConversation;
    }

    public float[] getSentMessageCornerRadii() {
        return sentMessageCornerRadii;
    }

    public float[] getReceivedMessageCornerRadii() {
        return receivedMessageCornerRadii;
    }

    public String getLogoutPackageName() {
        return logoutPackageName;
    }

    public KmFontModel getFontModel() {
        return fontModel;
    }

    public boolean isFaqOptionEnabled() {
        return isFaqOptionEnabled;
    }

    public boolean isFaqOptionEnabled(int screen) {
        return enableFaqOption[screen - 1];
    }

    public String getMessageStatusIconColor() {
        return messageStatusIconColor;
    }

    public void setMessageStatusIconColor(String messageStatusIconColor) {
        this.messageStatusIconColor = messageStatusIconColor;
    }

    public String getRestrictedWordRegex() {
        return restrictedWordRegex;
    }

    public String getToolbarTitleColor() {
        return toolbarTitleColor;
    }

    public void setToolbarTitleColor(String toolbarTitleColor) {
        this.toolbarTitleColor = toolbarTitleColor;
    }

    public String getToolbarSubtitleColor() {
        return toolbarSubtitleColor;
    }

    public void setToolbarSubtitleColor(String toolbarSubtitleColor) {
        this.toolbarSubtitleColor = toolbarSubtitleColor;
    }

    public String getToolbarColor() {
        return toolbarColor;
    }

    public String getStatusBarColor() {
        return statusBarColor;
    }

    public String getRichMessageThemeColor() {
        return richMessageThemeColor;
    }

    public boolean isRestrictMessageTypingWithBots() {
        return restrictMessageTypingWithBots;
    }

    public KmSpeechSetting getTextToSpeech() {
        return textToSpeech == null ? new KmSpeechSetting() : textToSpeech;
    }

    public KmSpeechSetting getSpeechToText() {
        return speechToText == null ? new KmSpeechSetting() : speechToText;
    }

    public String getReceiverNameTextColor() {
        return receiverNameTextColor;
    }

    public Map<String, Boolean> isHidePostCTA() {
        return hidePostCTA;
    }

    @Override
    public String toString() {
        return "AlCustomizationSettings{" +
                "customMessageBackgroundColor='" + customMessageBackgroundColor + '\'' +
                ", sentMessageBackgroundColor='" + sentMessageBackgroundColor + '\'' +
                ", receivedMessageBackgroundColor='" + receivedMessageBackgroundColor + '\'' +
                ", sendButtonBackgroundColor='" + sendButtonBackgroundColor + '\'' +
                ", attachmentIconsBackgroundColor='" + attachmentIconsBackgroundColor + '\'' +
                ", chatBackgroundColorOrDrawable='" + chatBackgroundColorOrDrawable + '\'' +
                ", editTextBackgroundColorOrDrawable='" + editTextBackgroundColorOrDrawable + '\'' +
                ", editTextLayoutBackgroundColorOrDrawable='" + editTextLayoutBackgroundColorOrDrawable + '\'' +
                ", channelCustomMessageBgColor='" + channelCustomMessageBgColor + '\'' +
                ", sentContactMessageTextColor='" + sentContactMessageTextColor + '\'' +
                ", receivedContactMessageTextColor='" + receivedContactMessageTextColor + '\'' +
                ", sentMessageTextColor='" + sentMessageTextColor + '\'' +
                ", receivedMessageTextColor='" + receivedMessageTextColor + '\'' +
                ", messageEditTextTextColor='" + messageEditTextTextColor + '\'' +
                ", sentMessageLinkTextColor='" + sentMessageLinkTextColor + '\'' +
                ", receivedMessageLinkTextColor='" + receivedMessageLinkTextColor + '\'' +
                ", messageEditTextHintTextColor='" + messageEditTextHintTextColor + '\'' +
                ", typingTextColor='" + typingTextColor + '\'' +
                ", noConversationLabelTextColor='" + noConversationLabelTextColor + '\'' +
                ", conversationDateTextColor='" + conversationDateTextColor + '\'' +
                ", conversationDayTextColor='" + conversationDayTextColor + '\'' +
                ", messageTimeTextColor='" + messageTimeTextColor + '\'' +
                ", channelCustomMessageTextColor='" + channelCustomMessageTextColor + '\'' +
                ", sentMessageBorderColor='" + sentMessageBorderColor + '\'' +
                ", receivedMessageBorderColor='" + receivedMessageBorderColor + '\'' +
                ", channelCustomMessageBorderColor='" + channelCustomMessageBorderColor + '\'' +
                ", audioPermissionNotFoundMsg='" + audioPermissionNotFoundMsg + '\'' +
                ", noConversationLabel='" + noConversationLabel + '\'' +
                ", noSearchFoundForChatMessages='" + noSearchFoundForChatMessages + '\'' +
                ", locationShareViaMap=" + locationShareViaMap +
                ", startNewFloatingButton=" + startNewFloatingButton +
                ", startNewButton=" + startNewButton +
                ", onlineStatusMasterList=" + onlineStatusMasterList +
                ", priceWidget=" + priceWidget +
                ", startNewGroup=" + startNewGroup +
                ", imageCompression=" + imageCompression +
                ", inviteFriendsInContactActivity=" + inviteFriendsInContactActivity +
                ", registeredUserContactListCall=" + registeredUserContactListCall +
                ", createAnyContact=" + createAnyContact +
                ", showActionDialWithOutCalling=" + showActionDialWithOutCalling +
                ", profileLogoutButton=" + profileLogoutButton +
                ", userProfileFragment=" + userProfileFragment +
                ", messageSearchOption=" + messageSearchOption +
                ", conversationContactImageVisibility=" + conversationContactImageVisibility +
                ", hideGroupAddMembersButton=" + hideGroupAddMembersButton +
                ", hideGroupNameUpdateButton=" + hideGroupNameUpdateButton +
                ", hideGroupExitButton=" + hideGroupExitButton +
                ", hideGroupRemoveMemberOption=" + hideGroupRemoveMemberOption +
                ", profileOption=" + profileOption +
                ", totalRegisteredUserToFetch=" + totalRegisteredUserToFetch +
                ", maxAttachmentAllowed=" + maxAttachmentAllowed +
                ", maxAttachmentSizeAllowed=" + maxAttachmentSizeAllowed +
                ", totalOnlineUsers=" + totalOnlineUsers +
                ", messageCharacterLimit" + messageCharacterLimit +
                ", toolbarTitleColor=" + toolbarTitleColor +
                ", toolbarSubtitleColor=" + toolbarSubtitleColor +
                '}';
    }
}
