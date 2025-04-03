package io.kommunicate.devkit.listners;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.exception.KommunicateException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MediaDownloadProgressHandler {
    void onDownloadStarted();

    void onProgressUpdate(int percentage, KommunicateException e);

    void onCompleted(Message message, KommunicateException e);
}
