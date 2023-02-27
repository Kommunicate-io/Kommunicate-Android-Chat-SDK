package io.kommunicate.callbacks;

import java.util.List;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.exception.KommunicateException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MessageListHandler {
    void onResult(List<Message> messageList, KommunicateException e);
}
