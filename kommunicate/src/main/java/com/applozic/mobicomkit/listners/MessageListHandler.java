package com.applozic.mobicomkit.listners;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.exception.ApplozicException;

import java.util.List;

/**
 * Created by reytum on 27/11/17.
 */

public interface MessageListHandler {
    void onResult(List<Message> messageList, ApplozicException e);
}
