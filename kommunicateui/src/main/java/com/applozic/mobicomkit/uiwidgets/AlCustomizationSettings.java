package com.applozic.mobicomkit.uiwidgets;

import com.applozic.mobicomkit.uiwidgets.conversation.MobicomMessageTemplate;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmFontModel;
import com.applozic.mobicommons.json.JsonMarker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by sunil on 10/10/16.
 */
public class AlCustomizationSettings extends JsonMarker {
    public static final int DEFAULT_MESSAGE_CHAR_LIMIT = 2000;

    private Object customMessageBackgroundColor = "#e6e5ec";
    private Object sentMessageBackgroundColor = "";
    private Object startNewConversationButtonBackgroundColor = "";
    private Object receivedMessageBackgroundColor = Arrays.asList("#e6e5ec","#313131");
    private Object sendButtonBackgroundColor = "";
    private Object attachmentIconsBackgroundColor = "";
    private Object chatBackgroundColorOrDrawable;
    private Object editTextBackgroundColorOrDrawable;
    private Object editTextLayoutBackgroundColorOrDrawable;
    private Object channelCustomMessageBgColor = Arrays.asList("#000000","#FFFFFFFF");
    private Object toolbarTitleColor = "#ffffff";
    private Object toolbarSubtitleColor = "#ffffff";
    private Object receiverNameTextColor = "#5C6677";

    private Object sentContactMessageTextColor = "#5fba7d";
    private Object receivedContactMessageTextColor = "#646262";
    private Object sentMessageTextColor = "#FFFFFFFF";
    private Object receivedMessageTextColor = Arrays.asList("#646262","#FFFFFF");
    private Object messageEditTextTextColor = "#000000";
    private Object messageEditTextBackgroundColor = "";
    private Object autoSuggestionButtonBackgroundColor= "";
    private Object autoSuggestionButtonTextColor = "";
    private Object sentMessageLinkTextColor = "#FFFFFFFF";
    private Object receivedMessageLinkTextColor = "#5fba7d";
    private Object messageEditTextHintTextColor = "#bdbdbd";
    private Object typingTextColor;
    private Object noConversationLabelTextColor = Arrays.asList("#000000", "#FFFFFFFF");
    private Object conversationDateTextColor = Arrays.asList("#333333", "#FFFFFFFF");
    private Object conversationDayTextColor = Arrays.asList("#333333", "#FFFFFFFF");
    private Object messageTimeTextColor = "#ede6e6";
    private Object channelCustomMessageTextColor = Arrays.asList("#000000", "#FFFFFFFF");
    private Object sentMessageBorderColor = "";
    private Object receivedMessageBorderColor = "#e6e5ec";
    private Object channelCustomMessageBorderColor = "#cccccc";
    private Object collapsingToolbarLayoutColor = "#5c5aa7";
    private Object groupParticipantsTextColor = "#5c5aa7";
    private Object groupDeleteButtonBackgroundColor = "#5c5aa7";
    private Object groupExitButtonBackgroundColor = "#5c5aa7";
    private Object adminTextColor = "#5c5aa7";
    private Object adminBackgroundColor = "#FFFFFFFF";
    private String attachCameraIconName = "applozic_ic_action_camera_new";
    private Object adminBorderColor = "#5c5aa7";
    private Object userNotAbleToChatTextColor = "#000000";
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
    private boolean logoutOption = true;
    private boolean logoutOptionFromConversation = true;
    private int defaultGroupType = 2;
    private boolean muteUserChatOption = false;
    private String restrictedWordRegex;
    private int totalRegisteredUserToFetch = 100;
    private int maxAttachmentAllowed = 5;
    private int maxAttachmentSizeAllowed = 30;
    private int messageCharacterLimit = DEFAULT_MESSAGE_CHAR_LIMIT;
    private int totalOnlineUsers = 0;
    private Object themeColorPrimary;
    private Object themeColorPrimaryDark;
    private String editTextHintText = "";
    private boolean replyOption = false;
    private Object replyMessageLayoutSentMessageBackground = "#C0C0C0";
    private Object replyMessageLayoutReceivedMessageBackground = "#F5F5F5";
    private boolean groupInfoScreenVisible = true;
    private boolean forwardOption = false;
    private Boolean innerTimestampDesign = false;
    private String sentMessageCreatedAtTimeColor;
    private String receivedMessageCreatedAtTimeColor;
    private boolean showStartNewConversation = true;
    private boolean enableAwayMessage = true;
    private Object awayMessageTextColor = Arrays.asList("#A9A4A4","#FFFFFFFF");
    private boolean isAgentApp = false;
    private boolean hideGroupSubtitle = false;
    private boolean disableGlobalStoragePermission = true;

    private boolean launchChatFromProfilePicOrName = false;
    private boolean useDeviceDefaultLanguage = true;
    private boolean showTypingIndicatorWhileFetchingResponse = false;
    private Map<String, Boolean> filterGallery;
    private boolean enableShareConversation = false;
    private Object messageStatusIconColor = "";
    private float[] sentMessageCornerRadii;
    private float[] receivedMessageCornerRadii;
    private KmFontModel fontModel;
    private boolean isFaqOptionEnabled = false;
    private boolean[] enableFaqOption = {true, false};
    private Object toolbarColor = "";
    private Object statusBarColor = "";
    private Object richMessageThemeColor = "";
    private Map<String, Boolean> attachmentOptions;
    private KmSpeechSetting textToSpeech;
    private KmSpeechSetting speechToText;
    private boolean restrictMessageTypingWithBots = false;
    private Map<String, Boolean> hidePostCTA;
    private boolean oneTimeRating;
    private boolean restartConversationButtonVisibility = true;
    private boolean rateConversationMenuOption;
    private boolean javaScriptEnabled = true;
    private boolean hideChatInHelpcenter = true;
    private boolean checkboxAsMultipleButton = false;
    private String staticTopMessage = "";
    private String staticTopIcon = "";
    private String menuIconOnConversationScreen = "";
    private boolean toolbarTitleCenterAligned = false;
    private boolean disableFormPostSubmit = false;
    private Object chatBarTopLineViewColor = "";
    private boolean isMultipleAttachmentSelectionEnabled = false;
    private boolean useDarkMode = true;
    private boolean isImageCompressionEnabled = false;
    private int minimumCompressionThresholdForImagesInMB = 5;
  
  
    public boolean getUseDarkMode() {
        return useDarkMode;
    }

    public String getStaticTopMessage() {
        return staticTopMessage;
    }

    public String getStaticTopIcon() {
        return staticTopIcon;
    }

    public String getMenuIconOnConversationScreen() {
        return menuIconOnConversationScreen;
    }
    public boolean isCheckboxAsMultipleButton() {
        return checkboxAsMultipleButton;
    }
    public boolean isRateConversationMenuOption() {
        return rateConversationMenuOption;
    }
        
    public boolean isRestartConversationButtonVisibility() {
        return restartConversationButtonVisibility;
    }
    
    public boolean isJavaScriptEnabled() {
        return javaScriptEnabled;
    }

    public Boolean getInnerTimestampDesign() {
        return innerTimestampDesign;
    }


    public boolean isOneTimeRating() {
        return oneTimeRating;
    }


    public void setInnerTimestampDesign(Boolean innerTimestampDesign) {
        this.innerTimestampDesign = innerTimestampDesign;
    }

    public String getNoConversationLabel() {
        return noConversationLabel;
    }

    public List<String> getCustomMessageBackgroundColor() {
        return getColorList(customMessageBackgroundColor);
    }


    public List<String> getSentMessageBackgroundColor() {
        return getColorList(sentMessageBackgroundColor);
    }

    public List<String> getReceivedMessageBackgroundColor() {
        return getColorList(receivedMessageBackgroundColor);
    }

    public boolean isOnlineStatusMasterList() {
        return onlineStatusMasterList;
    }

    public boolean isPriceWidget() {
        return priceWidget;
    }

    public List<String> getSendButtonBackgroundColor() {
        return getColorList(sendButtonBackgroundColor);
    }

    public boolean isImageCompression() {
        return imageCompression;
    }


    public boolean isInviteFriendsInContactActivity() {
        return inviteFriendsInContactActivity;
    }

    public List<String> getAttachmentIconsBackgroundColor() {
        return getColorList(attachmentIconsBackgroundColor);
    }

    public boolean isLocationShareViaMap() {
        return locationShareViaMap;
    }

    public boolean isConversationContactImageVisibility() {
        return conversationContactImageVisibility;
    }

    public List<String> getSentContactMessageTextColor() {
        return getColorList(sentContactMessageTextColor);
    }

    public List<String> getReceivedContactMessageTextColor() {
        return getColorList(receivedContactMessageTextColor);
    }

    public List<String> getSentMessageTextColor() {
        return getColorList(sentMessageTextColor);
    }

    public List<String> getReceivedMessageTextColor() {
        return getColorList(receivedMessageTextColor);
    }

    public List<String> getSentMessageBorderColor() {
        return getColorList(sentMessageBorderColor);
    }

    public List<String> getReceivedMessageBorderColor() {
        return getColorList(receivedMessageBorderColor);
    }

    public List<String> getChatBackgroundColorOrDrawable() {
        return getColorList(chatBackgroundColorOrDrawable);
    }

    public List<String> getMessageEditTextTextColor() {
        return getColorList(messageEditTextTextColor);
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

    public List<String> getSentMessageLinkTextColor() {
        return getColorList(sentMessageLinkTextColor);
    }

    public List<String> getReceivedMessageLinkTextColor() {
        return getColorList(receivedMessageLinkTextColor);
    }

    public List<String> getMessageEditTextHintTextColor() {
        return getColorList(messageEditTextHintTextColor);
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


    public List<String> getEditTextBackgroundColorOrDrawable() {
        return getColorList(editTextBackgroundColorOrDrawable);
    }

    public List<String> getEditTextLayoutBackgroundColorOrDrawable() {
        return getColorList(editTextLayoutBackgroundColorOrDrawable);
    }

    public List<String> getTypingTextColor() {
        return getColorList(typingTextColor);
    }

    public boolean isProfileOption() {
        return profileOption;
    }

    public List<String> getNoConversationLabelTextColor() {
        return getColorList(noConversationLabelTextColor);
    }

    public List<String> getConversationDateTextColor() {
        return getColorList(conversationDateTextColor);
    }

    public List<String> getConversationDayTextColor() {
        return getColorList(conversationDayTextColor);
    }

    public List<String> getMessageTimeTextColor() {
        return getColorList(messageTimeTextColor);
    }

    public List<String> getChannelCustomMessageBgColor() {
        return getColorList(channelCustomMessageBgColor);
    }

    public List<String> getChannelCustomMessageBorderColor() {
        return getColorList(channelCustomMessageBorderColor);
    }

    public List<String> getChannelCustomMessageTextColor() {
        return getColorList(channelCustomMessageTextColor);
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

    public List<String> getCollapsingToolbarLayoutColor() {
        return getColorList(collapsingToolbarLayoutColor);
    }

    public List<String> getGroupParticipantsTextColor() {
        return getColorList(groupParticipantsTextColor);
    }

    public List<String> getGroupExitButtonBackgroundColor() {
        return getColorList(groupExitButtonBackgroundColor);
    }

    public List<String> getGroupDeleteButtonBackgroundColor() {
        return getColorList(groupDeleteButtonBackgroundColor);
    }

    public List<String> getAdminTextColor() {
        return getColorList(adminTextColor);
    }

    public List<String> getAdminBackgroundColor() {
        return getColorList(adminBackgroundColor);
    }

    public String getAttachCameraIconName() {
        return attachCameraIconName;
    }

    public List<String> getAdminBorderColor() {
        return getColorList(adminBorderColor);
    }

    public List<String> getUserNotAbleToChatTextColor() {
        return getColorList(userNotAbleToChatTextColor);
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

    public boolean isLogoutOptionFromConversation() {
        return logoutOptionFromConversation;
    }

    public void setLogoutOptionFromConversation(boolean logoutOptionFromConversation) {
        this.logoutOptionFromConversation = logoutOptionFromConversation;
    }

    public String getLogoutPackage() {
        return logoutPackageName;
    }

    public void setLogoutPackageName(String logoutPackageName) {
        this.logoutPackageName = logoutPackageName;
    }

    public List<String> getThemeColorPrimary() {
        return getColorList(themeColorPrimary);
    }

    public List<String> getThemeColorPrimaryDark() {
        return getColorList(themeColorPrimaryDark);
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

    public List<String> getReplyMessageLayoutSentMessageBackground() {
        return getColorList(replyMessageLayoutSentMessageBackground);
    }

    public List<String> getReplyMessageLayoutReceivedMessageBackground() {
        return getColorList(replyMessageLayoutReceivedMessageBackground);
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

    public List<String> getStartNewConversationButtonBackgroundColor() {
        return getColorList(startNewConversationButtonBackgroundColor);
    }

    public List<String> getMessageEditTextBackgroundColor() {
        return getColorList(messageEditTextBackgroundColor);
    }
    public List<String> getAutoSuggestionButtonBackgroundColor() {
        return getColorList(autoSuggestionButtonBackgroundColor);
    }
    public List<String> getAutoSuggestionButtonTextColor() {
        return getColorList(autoSuggestionButtonTextColor);
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

    public List<String> getAwayMessageTextColor() {
        return getColorList(awayMessageTextColor);
    }

    public void setAwayMessageTextColor(String awayMessageTextColor) {
        this.awayMessageTextColor = awayMessageTextColor;
    }

    public boolean isAgentApp() {
        return isAgentApp;
    }
    public boolean isUseDeviceDefaultLanguage(){
        return useDeviceDefaultLanguage;
    }
    public boolean isShowTypingIndicatorWhileFetchingResponse() {
        return showTypingIndicatorWhileFetchingResponse;
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

    public List<String> getMessageStatusIconColor() {
        return getColorList(messageStatusIconColor);
    }

    public void setMessageStatusIconColor(String messageStatusIconColor) {
        this.messageStatusIconColor = messageStatusIconColor;
    }

    public String getRestrictedWordRegex() {
        return restrictedWordRegex;
    }

    public List<String> getToolbarTitleColor() {
        return getColorList(toolbarTitleColor);
    }
    public List<String> getChatBarTopLineViewColor() {
        return getColorList(chatBarTopLineViewColor);
    }

    public void setToolbarTitleColor(String toolbarTitleColor) {
        this.toolbarTitleColor = toolbarTitleColor;
    }

    public List<String> getToolbarSubtitleColor() {
        return getColorList(toolbarSubtitleColor);
    }

    public void setToolbarSubtitleColor(String toolbarSubtitleColor) {
        this.toolbarSubtitleColor = toolbarSubtitleColor;
    }

    public List<String> getToolbarColor() {
        return getColorList(toolbarColor);
    }

    public List<String> getStatusBarColor() {
        return getColorList(statusBarColor);
    }

    public List<String> getRichMessageThemeColor() {
        return getColorList(richMessageThemeColor);
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

    public List<String> getReceiverNameTextColor() {
        return getColorList(receiverNameTextColor);
    }

    public Map<String, Boolean> isHidePostCTA() {
        return hidePostCTA;
    }

    public boolean isHideChatInHelpcenter() {
        return hideChatInHelpcenter;
    }

    public boolean isToolbarTitleCenterAligned() {
        return toolbarTitleCenterAligned;
    }

    public boolean isDisableFormPostSubmit() {
        return disableFormPostSubmit;
    }

    public boolean isMultipleAttachmentSelectionEnabled() {
        return isMultipleAttachmentSelectionEnabled;
    }

    private List<String> getColorList(Object color) {
        if (color instanceof String) {
            return Arrays.asList((String) color, (String) color);
        } else if (color instanceof List) {
            return (List<String>) color;
        }
        return Arrays.asList("","");
    }

    public boolean isImageCompressionEnabled() {
        return isImageCompressionEnabled;
    }

    public int getMinimumCompressionThresholdForImagesInMB() {
        return minimumCompressionThresholdForImagesInMB;
    }

    @Override
    public String toString() {
        return "AlCustomizationSettings{" +
                "customMessageBackgroundColor='" + customMessageBackgroundColor + '\'' +
                ", sentMessageBackgroundColor='" + sentMessageBackgroundColor + '\'' +
                ", messageEditTextBackgroundColor='" + messageEditTextBackgroundColor + '\'' +
                ", chatBarTopLineViewColor='" + chatBarTopLineViewColor + '\'' +
                ", autoSuggestionButtonBackgroundColor='" + autoSuggestionButtonBackgroundColor + '\'' +
                ", startNewConversationButtonBackgroundColor='" + startNewConversationButtonBackgroundColor + '\'' +
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
                ", useDeviceDefaultLanguage=" + useDeviceDefaultLanguage +
                ", showTypingIndicatorWhileFetchingResponse=" + showTypingIndicatorWhileFetchingResponse+
                '}';
    }
}
