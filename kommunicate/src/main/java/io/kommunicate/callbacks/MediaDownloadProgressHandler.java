package io.kommunicate.callbacks;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.exception.KommunicateException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MediaDownloadProgressHandler {
    void onDownloadStarted();

    void onProgressUpdate(int percentage, KommunicateException e);

    void onCompleted(Message message, KommunicateException e);
}