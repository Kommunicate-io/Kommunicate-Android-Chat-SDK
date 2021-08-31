package io.kommunicate.callbacks;

public interface KmPluginEventListener {
    void onPluginLaunch();
    void onPluginDismiss();
    void onConversationResolved(Integer conversationId);
    void onConversationRestarted(Integer conversationId);
    void onRichMessageButtonClick(Integer conversationId, String actionType, Object action);
}
