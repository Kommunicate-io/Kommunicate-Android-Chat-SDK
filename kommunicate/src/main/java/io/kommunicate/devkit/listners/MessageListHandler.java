package io.kommunicate.devkit.listners;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.exception.KommunicateException;

import java.util.List;

import annotations.CleanUpRequired;

/**
 * Created by reytum on 27/11/17.
 */

@Deprecated
@CleanUpRequired(reason = "Migrated MessageListHandler to more generalized TaskListener.")
public interface MessageListHandler {
    void onResult(List<Message> messageList, KommunicateException e);
}
