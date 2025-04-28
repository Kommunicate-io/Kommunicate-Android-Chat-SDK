package io.kommunicate.ui.conversation;

import io.kommunicate.devkit.api.conversation.Message;

/**
 * Created by devashish on 10/2/15.
 */
public interface MessageCommunicator {

    void updateLatestMessage(Message message, String formattedContactNumber);

    void removeConversation(Message message, String formattedContactNumber);
}