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
}
