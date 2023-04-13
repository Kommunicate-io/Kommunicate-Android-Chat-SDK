package io.kommunicate.callbacks;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.exception.KommunicateException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MediaUploadProgressHandler {
    void onUploadStarted(KommunicateException e, String oldMessageKey);

    void onProgressUpdate(int percentage, KommunicateException e, String oldMessageKey);

    void onCancelled(KommunicateException e, String oldMessageKey);

    void onCompleted(KommunicateException e, String oldMessageKey);

    void onSent(Message message, String oldMessageKey);
}
