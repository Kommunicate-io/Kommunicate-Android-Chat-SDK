package io.kommunicate.callbacks;

import com.applozic.mobicomkit.api.conversation.Message;

public interface KmPluginEventListener {
    void onPluginLaunch();
    void onPluginDismiss();
    void onConversationResolved(Integer conversationId);
    void onConversationRestarted(Integer conversationId);
    void onRichMessageButtonClick(Integer conversationId, String actionType, Object action);
    void onStartNewConversation(Integer conversationId);
    void onSubmitRatingClick(Integer conversationId, Integer rating, String feedback);
    void onMessageSent(Message message);
    void onMessageReceived(Message message);
    void onBackButtonClicked(boolean isConversationOpened);
    void onAttachmentClick(String attachmentType);
    void onFaqClick(String FaqUrl);
    void onLocationClick();
    void onNotificationClick(Message message);
    void onVoiceButtonClick(String action);
    void onRatingEmoticonsClick(Integer ratingValue);
    void onRateConversationClick();
}
