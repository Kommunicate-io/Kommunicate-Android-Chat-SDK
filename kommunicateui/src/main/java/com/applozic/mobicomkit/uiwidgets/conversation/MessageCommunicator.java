package com.applozic.mobicomkit.uiwidgets.conversation;

import com.applozic.mobicomkit.api.conversation.Message;

/**
 * Created by devashish on 10/2/15.
 */
public interface MessageCommunicator {

    void updateLatestMessage(Message message, String formattedContactNumber);

    void removeConversation(Message message, String formattedContactNumber);
}